package com.zdevzone.tools.message;

import com.zdevzone.tools.constant.MessageType;
import com.zdevzone.tools.message.support.DelayMessageHandler;
import com.zdevzone.tools.service.impl.DelayMessageServiceImpl;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author zhengweibao
 */
public class DelayMessageServiceImplTest extends DelayMessageAbstractTest{

	private static final Logger logger = LoggerFactory.getLogger(DelayMessageServiceImplTest.class);

	@Autowired
	private DelayMessageServiceImpl delayMessageServiceImpl;

	@Test
	public void sendDelayMessageWithDelay() {
		delayMessageServiceImpl.sendDelayMessageWithDelay(DelayMessageTestConfig.TEST_HANDLER_ID, "Hello world!", MessageType.SINGLE_NODE, 10000L);

		logger.info("Hello world!");
	}

	@DelayMessageHandler(delayMessageHandlerId = DelayMessageTestConfig.TEST_HANDLER_ID)
	private void handleDelayMessage(String payload){
		logger.info("Receive message : " + payload);
	}
}