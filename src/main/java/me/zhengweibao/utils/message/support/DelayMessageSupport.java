package me.zhengweibao.utils.message.support;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.core.MethodIntrospector;
import org.springframework.core.MethodIntrospector.MetadataLookup;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author zhengweibao
 */
public class DelayMessageSupport implements BeanPostProcessor{

	private static final Logger logger = LoggerFactory.getLogger(DelayMessageSupport.class);

	private static Map<String, DelayMessageCallback> delayMessageHandlerMap = new ConcurrentHashMap<>();

	private class DelayMessageCallback {
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
				String id = annotation.id();

				if (delayMessageHandlerMap.containsKey(id)){
					logger.error("There already exist one delayMessageHandler's id is {}.", id);
					throw new IllegalStateException("The delayMessageHandler already exists. id = " + id);
				}

				DelayMessageCallback delayMessageCallback = new DelayMessageCallback(bean, method);

				if (logger.isDebugEnabled()) {
					logger.debug(String.format("Mapping DelayMessageHandler Id [%s] ----> DelayMessageCallback [%s].", id, delayMessageCallback));
				}

				delayMessageHandlerMap.put(id, delayMessageCallback);
			});
		}

		return bean;
	}

	public void handleMessage(String delayMessageHandlerId, String payload) throws InvocationTargetException, IllegalAccessException {
		DelayMessageCallback delayMessageCallback = delayMessageHandlerMap.get(delayMessageHandlerId);

		if (delayMessageCallback == null){
			throw new IllegalStateException("There is no delayMessageHandler's id is " + delayMessageHandlerId);
		}

		delayMessageCallback.handleMessage(payload);
	}
}
