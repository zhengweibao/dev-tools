package com.zdevzone.tools.service.impl;

import com.zdevzone.tools.constant.MessageType;
import com.zdevzone.tools.message.DelayMessage;
import com.zdevzone.tools.message.support.DelayMessageSupport;
import com.zdevzone.tools.service.DelayMessageService;

/**
 * @author zhengweibao
 */
public class DelayMessageServiceImpl implements DelayMessageService {

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
