package me.zhengweibao.utils.rankings;

import me.zhengweibao.utils.config.RedisRankingsConfiguration;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * @author zhengweibao
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {RedisRankingsTestConfig.class, RedisRankingsConfiguration.class})
public class RedisRankingsAbstractTest {
}
