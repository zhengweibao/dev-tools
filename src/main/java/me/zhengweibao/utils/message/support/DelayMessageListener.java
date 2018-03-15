package me.zhengweibao.utils.message.support;

import com.fasterxml.jackson.databind.ObjectMapper;
import me.zhengweibao.utils.config.DelayMessageConfiguration;
import me.zhengweibao.utils.exception.DelayMessageHandleException;
import me.zhengweibao.utils.message.DelayMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageListener;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.util.StringUtils;

/**
 * @author zhengweibao
 */
public class DelayMessageListener implements MessageListener {

	private static final Logger logger = LoggerFactory.getLogger(DelayMessageListener.class);

	private final DelayMessageSupport delayMessageSupport;

	private final ObjectMapper objectMapper;

	private final AmqpTemplate amqpTemplate;

	public DelayMessageListener(DelayMessageSupport delayMessageSupport, ObjectMapper objectMapper, AmqpTemplate amqpTemplate) {
		this.delayMessageSupport = delayMessageSupport;
		this.objectMapper = objectMapper;
		this.amqpTemplate = amqpTemplate;
	}

	@Override
	public void onMessage(Message message) {
		try {
			MessageProperties messageProperties = message.getMessageProperties();
			String targetHandlerId = (String) messageProperties.getHeaders().get(DelayMessageConfiguration.HANDLER_ID_PROPERTY_NAME);

			if (StringUtils.isEmpty(targetHandlerId)) {
				logger.warn("Receive a message, but target handler id is empty!");
				return;
			}

			DelayMessage delayMessage = objectMapper.convertValue(message.getBody(), DelayMessage.class);

			if (logger.isDebugEnabled()) {
				logger.debug("Receive a delay message, sendTime is {} and delayTime is {} which is send to queue : {}[target_handler_id = {}, payload = {}].", messageProperties.getTimestamp(), messageProperties.getDelay(), messageProperties.getConsumerQueue(), targetHandlerId, delayMessage.getPayload());
			}

			delayMessageSupport.handleMessage(targetHandlerId, delayMessage.getPayload());
		} catch (Exception e) {
			logger.error("There a exception happen while handle delayMessage[{}].", objectMapper.convertValue(message.getBody(), String.class));
			throw new DelayMessageHandleException(e);
		}
	}
}
