package me.zhengweibao.utils.message;

import me.zhengweibao.utils.constant.MessageType;
import me.zhengweibao.utils.message.support.DelayMessageHandler;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author zhengweibao
 */
public class DelayMessageServiceTest extends DelayMessageAbstractTest{

	private static final Logger logger = LoggerFactory.getLogger(DelayMessageServiceTest.class);

	@Autowired
	private DelayMessageService delayMessageService;

	@Test
	public void sendDelayMessageWithDelay() {
		delayMessageService.sendDelayMessageWithDelay(DelayMessageTestConfig.TEST_HANDLER_ID, "Hello world!", MessageType.SINGLE_NODE, 10000L);

		logger.info("Hello world!");
	}

	@DelayMessageHandler(delayMessageHandlerId = DelayMessageTestConfig.TEST_HANDLER_ID)
	private void handleDelayMessage(String payload){
		logger.info("Receive message : " + payload);
	}
}