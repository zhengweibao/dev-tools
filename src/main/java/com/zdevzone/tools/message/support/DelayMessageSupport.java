package com.zdevzone.tools.message.support;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zdevzone.tools.config.DelayMessageConfiguration;
import com.zdevzone.tools.constant.MessageType;
import com.zdevzone.tools.exception.DelayMessageHandleException;
import com.zdevzone.tools.message.DelayMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageListener;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.core.MethodIntrospector;
import org.springframework.core.MethodIntrospector.MetadataLookup;
import org.springframework.util.Assert;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author zhengweibao
 */
public class DelayMessageSupport implements BeanPostProcessor, MessageListener{

	private static final class DelayMessageCallback {
		private Object bean;

		private Method targetMethod;

		private DelayMessageCallback(Object bean, Method targetMethod) {
			this.bean = bean;
			this.targetMethod = targetMethod;
		}

		private void handleMessage(String payload) throws InvocationTargetException, IllegalAccessException {
			targetMethod.invoke(bean, payload);
		}

		@Override
		public String toString() {
			return bean.getClass().getCanonicalName() + "." + targetMethod.getName();
		}
	}

	private static final Logger logger = LoggerFactory.getLogger(DelayMessageSupport.class);

	private static Map<String, DelayMessageCallback> delayMessageHandlerMap = new ConcurrentHashMap<>();

	private AmqpTemplate amqpTemplate;

	private ObjectMapper objectMapper;

	private String clientShareQueue;

	private String currentNodeQueue;

	public void setAmqpTemplate(AmqpTemplate amqpTemplate) {
		this.amqpTemplate = amqpTemplate;
	}

	public void setObjectMapper(ObjectMapper objectMapper) {
		this.objectMapper = objectMapper;
	}

	public void setClientShareQueue(String clientShareQueue) {
		this.clientShareQueue = clientShareQueue;
	}

	public void setCurrentNodeQueue(String currentNodeQueue) {
		this.currentNodeQueue = currentNodeQueue;
	}

	@Override
	public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
		return bean;
	}

	@Override
	public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
		Class<?> targetClass = AopUtils.getTargetClass(bean);

		Map<Method, DelayMessageHandler> annotatedMethods = MethodIntrospector.selectMethods(targetClass,
				(MetadataLookup<DelayMessageHandler>) method -> {
					DelayMessageHandler annotation = method.getAnnotation(DelayMessageHandler.class);

					if (annotation != null){
						Class<?>[] parameterTypes = method.getParameterTypes();

						if (parameterTypes.length != 1 || !String.class.isAssignableFrom(parameterTypes[0])) {
							throw new IllegalStateException(String.format("The parameter type of Method %s should be String.", method.toString()));
						}
					}

					return annotation;
				});

		if (annotatedMethods.isEmpty()){
			if (logger.isTraceEnabled()) {
				logger.trace("No @DelayMessageHandler annotations found on bean class: " + bean.getClass());
			}
		} else {
			annotatedMethods.forEach((method, annotation) -> {
				String delayMessageHandlerId = annotation.delayMessageHandlerId();

				if (delayMessageHandlerMap.containsKey(delayMessageHandlerId)){
					logger.error("There already exist one delayMessageHandler's delayMessageHandlerId is {}.", delayMessageHandlerId);
					throw new IllegalStateException("The delayMessageHandler already exists. delayMessageHandlerId = " + delayMessageHandlerId);
				}

				method.setAccessible(true);

				DelayMessageCallback delayMessageCallback = new DelayMessageCallback(bean, method);

				if (logger.isDebugEnabled()) {
					logger.debug(String.format("Mapping DelayMessageHandler Id [%s] ----> DelayMessageCallback [%s].", delayMessageHandlerId, delayMessageCallback));
				}

				delayMessageHandlerMap.put(delayMessageHandlerId, delayMessageCallback);
			});
		}

		return bean;
	}

	@Override
	public void onMessage(Message message) {
		try {
			DelayMessage delayMessage = objectMapper.readValue(message.getBody(), DelayMessage.class);

			if (delayMessage.isRelayed()) {
				if (logger.isDebugEnabled()) {
					logger.debug("The delay message need relay[target_timestamp = {}, target_handler_id = {}, payload = {}].", delayMessage.getTargetTimestamp(), delayMessage.getTargetHandlerId(), delayMessage.getPayload());
				}

				checkHandlerId(delayMessage.getTargetHandlerId());
				sendDelayMessage(delayMessage);
				return;
			}

			if (logger.isDebugEnabled()) {
				logger.debug("Receive a delay message.[target_timestamp = {}, target_handler_id = {}, payload = {}]", delayMessage.getTargetTimestamp(), delayMessage.getTargetHandlerId(), delayMessage.getPayload());
			}

			handleMessage(delayMessage.getTargetHandlerId(), delayMessage.getPayload());
		} catch (Exception e) {
			logger.error("There a exception happen while handle delayMessage[{}], Exception : {}.", objectMapper.convertValue(message.getBody(), String.class), e.getMessage());
			throw new DelayMessageHandleException(e);
		}
	}

	private void handleMessage(String delayMessageHandlerId, String payload) throws InvocationTargetException, IllegalAccessException {
		DelayMessageCallback delayMessageCallback = delayMessageHandlerMap.get(delayMessageHandlerId);

		if (delayMessageCallback == null){
			throw new IllegalStateException("There is no delayMessageHandler's delayMessageHandlerId is " + delayMessageHandlerId);
		}

		delayMessageCallback.handleMessage(payload);
	}

	public void sendDelayMessage(DelayMessage delayMessage) {
		Long targetTimestamp = delayMessage.getTargetTimestamp();
		Assert.notNull(targetTimestamp, "The targetTimestamp cannot be null!");

		MessageType messageType = delayMessage.getMessageType();
		Assert.notNull(messageType, "The messageType cannot be null!");

		Long actualDelay = Duration.between(Instant.now(), Instant.ofEpochMilli(targetTimestamp)).toMillis();

		boolean isRelayed =  actualDelay > DelayMessageConfiguration.MAX_DELAY_MESSAGE_TIME;
		delayMessage.setRelayed(isRelayed);

		Long sendDelay = isRelayed ? DelayMessageConfiguration.MAX_DELAY_MESSAGE_TIME : actualDelay;

		String targetQueue;
		if (isRelayed || messageType == MessageType.SINGLE_NODE) {
			if (logger.isDebugEnabled() && isRelayed) {
				logger.debug("Prepare a relay message[targetTimestamp = {}, targetHandlerId = {}, payload = {}].", targetTimestamp, delayMessage.getTargetHandlerId(), delayMessage.getPayload());
			}

			targetQueue = clientShareQueue;
		} else {
			targetQueue = currentNodeQueue;
		}

		MessageProperties properties = new MessageProperties();
		properties.setContentType(MessageProperties.CONTENT_TYPE_TEXT_PLAIN);
		properties.setContentEncoding("UTF-8");
		properties.setDelay(sendDelay.intValue());

		try {
			if (logger.isDebugEnabled()) {
				logger.debug("Send delay message [delay={}ms] using routingKey [{}]. [targetHandlerId = {}, payload = {}]", properties.getDelay(), targetQueue, delayMessage.getTargetHandlerId(), delayMessage.getPayload());
			}

			byte[] messageByte = objectMapper.writeValueAsBytes(delayMessage);
			Message mqMessage = new Message(messageByte, properties);

			amqpTemplate.send(targetQueue, mqMessage);
		} catch (Exception e) {
			logger.error("Error while send delay message to target queue : {}.", targetQueue);
			throw new RuntimeException(e);
		}
	}

	public void checkHandlerId(String delayMessageHandlerId){
		if (!delayMessageHandlerMap.containsKey(delayMessageHandlerId)) {
			throw new IllegalStateException("There is no delayMessageHandler's delayMessageHandlerId is " + delayMessageHandlerId);
		}
	}
}
