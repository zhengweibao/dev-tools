package me.zhengweibao.utils.service;

import me.zhengweibao.utils.constant.MessageType;

/**
 * @author zhengweibao
 */
public interface DelayMessageService {

	/**
	 * 根据指定时延发送延时消息
	 * 消息的delayTimeMillis后被消费
	 *
	 * @param targetHandlerId 消息回调处理方法ID
	 * @see me.zhengweibao.utils.message.support.DelayMessageHandler
	 * @param payload 消息内容
	 * @param messageType 消息类型
	 * @param delayTimeMillis 延时时间，单位为毫秒
	 */
	void sendDelayMessageWithDelay(String targetHandlerId, String payload, MessageType messageType, Long delayTimeMillis);

	/**
	 * 设定消息被消费的时间，
	 * 当消费时间早于当前时间时，消息会被立即消费
	 * 否则，消息将在指定时间被消费
	 *
	 * @param targetHandlerId 消息回调处理方法ID
	 * @see me.zhengweibao.utils.message.support.DelayMessageHandler
	 * @param payload 消息内容
	 * @param messageType 消息类型
	 * @param targetTimestamp 延时消息的目标时间戳
	 */
	void sendMessageAtTargetTimestamp(String targetHandlerId, String payload, MessageType messageType, Long targetTimestamp);
}
