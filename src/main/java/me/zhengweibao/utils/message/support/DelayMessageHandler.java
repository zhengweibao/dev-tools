package me.zhengweibao.utils.message.support;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author zhengweibao
 */
@Target({ ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
public @interface DelayMessageHandler{

	/**
	 * 唯一标识ID，同一个context下不允许存在两个相同ID的手动触发任务
	 */
	String id();
}
