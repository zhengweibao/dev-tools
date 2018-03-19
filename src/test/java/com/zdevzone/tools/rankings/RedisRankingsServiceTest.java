package com.zdevzone.tools.rankings;

import com.zdevzone.tools.service.RankingsService;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Collections;

/**
 * @author zhengweibao
 */
public class RedisRankingsServiceTest extends RedisRankingsAbstractTest{

	private static final Logger logger = LoggerFactory.getLogger(RedisRankingsServiceTest.class);

	@Autowired
	private RankingsService rankingsService;

	@Test
	public void testForSomething() {
		logger.info("Test for something use rankingsService!");

		RedisRankings redisRankings = new RedisRankings("test_rankings", null, "test");

		rankingsService.incrScoreAndSetTuningFactorToRankings(Collections.singletonList(redisRankings), Collections.singletonList("test_user"), 1222L, 0.11);
	}
}
