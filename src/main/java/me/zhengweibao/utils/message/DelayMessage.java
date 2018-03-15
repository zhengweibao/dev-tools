package me.zhengweibao.utils.message;

import me.zhengweibao.utils.constant.MessageType;

/**
 * @author zhengweibao
 */
public class DelayMessage{

	private MessageType messageType;

	private Long targetTimestamp;

	private String targetHandlerId;

	private boolean relayed;

	private String payload;

	public DelayMessage() {
	}

	public DelayMessage(String payload) {
		this.payload = payload;
	}

	public MessageType getMessageType() {
		return messageType;
	}

	public void setMessageType(MessageType messageType) {
		this.messageType = messageType;
	}

	public Long getTargetTimestamp() {
		return targetTimestamp;
	}

	public void setTargetTimestamp(Long targetTimestamp) {
		this.targetTimestamp = targetTimestamp;
	}

	public String getTargetHandlerId() {
		return targetHandlerId;
	}

	public void setTargetHandlerId(String targetHandlerId) {
		this.targetHandlerId = targetHandlerId;
	}

	public boolean isRelayed() {
		return relayed;
	}

	public void setRelayed(boolean relayed) {
		this.relayed = relayed;
	}

	public String getPayload() {
		return payload;
	}

	public void setPayload(String payload) {
		this.payload = payload;
	}
}
