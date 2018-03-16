package me.zhengweibao.utils.message;

import me.zhengweibao.utils.constant.MessageType;
import me.zhengweibao.utils.message.support.DelayMessageSupport;

/**
 * @author zhengweibao
 */
public class DelayMessageService {

	private final DelayMessageSupport delayMessageSupport;

	public DelayMessageService(DelayMessageSupport delayMessageSupport) {
		this.delayMessageSupport = delayMessageSupport;
	}

	public void sendDelayMessageWithDelay(String targetHandlerId, String payload, MessageType messageType, Long delayTimeMillis){
		sendMessageAtTargetTimestamp(targetHandlerId, payload, messageType, System.currentTimeMillis() + delayTimeMillis);
	}

	public void sendMessageAtTargetTimestamp(String targetHandlerId, String payload, MessageType messageType, Long targetTimestamp){
		delayMessageSupport.checkHandlerId(targetHandlerId);

		DelayMessage delayMessage = new DelayMessage(payload);
		delayMessage.setTargetHandlerId(targetHandlerId);
		delayMessage.setMessageType(messageType);
		delayMessage.setTargetTimestamp(targetTimestamp);

		delayMessageSupport.sendDelayMessage(delayMessage);
	}
}
