package com.zdevzone.tools.exception;

/**
 * @author zhengweibao
 */
public class DelayMessageHandleException extends IllegalStateException {

	public DelayMessageHandleException(Throwable cause) {
		super("There a error happen while handle delay message : " + cause.getMessage(), cause);
	}
}
