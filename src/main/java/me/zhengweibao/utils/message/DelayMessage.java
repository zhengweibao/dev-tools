package me.zhengweibao.utils.message;

/**
 * @author zhengweibao
 */
public class DelayMessage{

	private Long targetTimestamp;

	private String targetHandlerId;

	private boolean relayed;

	private String payload;

	public DelayMessage() {
	}

	public DelayMessage(String payload) {
		this.payload = payload;
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
