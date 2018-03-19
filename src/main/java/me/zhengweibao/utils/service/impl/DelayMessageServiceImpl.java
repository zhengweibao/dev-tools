package me.zhengweibao.utils.service.impl;

import me.zhengweibao.utils.constant.MessageType;
import me.zhengweibao.utils.message.DelayMessage;
import me.zhengweibao.utils.message.support.DelayMessageSupport;

/**
 * @author zhengweibao
 */
public class DelayMessageServiceImpl implements me.zhengweibao.utils.service.DelayMessageService {

	private final DelayMessageSupport delayMessageSupport;

	public DelayMessageServiceImpl(DelayMessageSupport delayMessageSupport) {
		this.delayMessageSupport = delayMessageSupport;
	}

	@Override
	public void sendDelayMessageWithDelay(String targetHandlerId, String payload, MessageType messageType, Long delayTimeMillis){
		sendMessageAtTargetTimestamp(targetHandlerId, payload, messageType, System.currentTimeMillis() + delayTimeMillis);
	}

	@Override
	public void sendMessageAtTargetTimestamp(String targetHandlerId, String payload, MessageType messageType, Long targetTimestamp){
		delayMessageSupport.checkHandlerId(targetHandlerId);

		DelayMessage delayMessage = new DelayMessage(payload);
		delayMessage.setTargetHandlerId(targetHandlerId);
		delayMessage.setMessageType(messageType);
		delayMessage.setTargetTimestamp(targetTimestamp);

		delayMessageSupport.sendDelayMessage(delayMessage);
	}
}
