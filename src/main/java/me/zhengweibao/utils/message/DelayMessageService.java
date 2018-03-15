package me.zhengweibao.utils.message;

import me.zhengweibao.utils.constant.MessageType;
import org.springframework.amqp.core.AmqpTemplate;

/**
 * @author zhengweibao
 */
public class DelayMessageService {

	private AmqpTemplate amqpTemplate;

	private String clientShareQueue;

	private String currentNodeQueue;

	public void setAmqpTemplate(AmqpTemplate amqpTemplate) {
		this.amqpTemplate = amqpTemplate;
	}

	public void setClientShareQueue(String clientShareQueue) {
		this.clientShareQueue = clientShareQueue;
	}

	public void setCurrentNodeQueue(String currentNodeQueue) {
		this.currentNodeQueue = currentNodeQueue;
	}

	public void sendDelayMessage(MessageType messageType, String payload, Long delayTimeMillis){
		DelayMessage delayMessage = new DelayMessage(payload);
		delayMessage.setMessageType(messageType);
		delayMessage.setTargetTimestamp(System.currentTimeMillis() + delayTimeMillis);


	}

	public void sendMessageAtTargetTimestamp(String payload, MessageType messageType, Long targetTimestamp){

	}
}
