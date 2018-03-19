package me.zhengweibao.utils.lock;

import me.zhengweibao.utils.config.SingleRedisDLConfiguration;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * @author zhengweibao
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {SingleRedisDLTestConfig.class, SingleRedisDLConfiguration.class})
public abstract class SingleRedisDLAbstractTest {
}
