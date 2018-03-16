package me.zhengweibao.utils.message;

import me.zhengweibao.utils.constant.MessageType;

/**
 * @author zhengweibao
 */
public class DelayMessage{

	private MessageType messageType;

	/**
	 * 延时消息的目标时间戳，单位毫秒
	 */
	private Long targetTimestamp;

	/**
	 * 标识该延时消息的callback方法
	 * @see me.zhengweibao.utils.message.support.DelayMessageHandler
	 */
	private String targetHandlerId;

	/**
	 * 是否是时间接力消息
	 * 对于rabbitMQ的延时消息插件，最长延时时间为2^32 - 1毫秒
	 * 大约为40+天左右， 而项目中使用的amqpTemplate中定义的delayTime的数据类型为int，
	 * 在JAVA中int类型的最大值只有2^31 - 1, 换算为天数大约为20+
	 * 因此为了避免数值溢出和实现长时间的延时消息发送，这里采用了接力的形式，
	 * 当延时消息延时间隔大于指定值({@code me.zhengweibao.utils.config.DelayMessageConfiguration#MAX_DELAY_MESSAGE_TIME}, 15天)
	 * 将以接力的形式发送该延时消息
	 */
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
