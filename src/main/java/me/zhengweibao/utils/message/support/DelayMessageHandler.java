package me.zhengweibao.utils.message.support;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 延时消息的callback方法的标识标签
 *
 * @author zhengweibao
 */
@Target({ ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
public @interface DelayMessageHandler{

	/**
	 * 延时消息的callback方法唯一标识ID，对应发送延时消息时的delayMessageHandlerId
	 */
	String delayMessageHandlerId();
}
