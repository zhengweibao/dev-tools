package com.zdevzone.tools.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.zdevzone.tools.dao.RankingsRankDao;
import com.zdevzone.tools.domain.RankingsRank;
import com.zdevzone.tools.rankings.MultiRankingElementWrapper;
import com.zdevzone.tools.rankings.RankingElement;
import com.zdevzone.tools.rankings.Rankings;
import com.zdevzone.tools.rankings.SingleRankingElementWrapper;
import com.zdevzone.tools.service.RankingsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.connection.StringRedisConnection;
import org.springframework.data.redis.core.DefaultTypedTuple;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ZSetOperations.TypedTuple;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scripting.support.ResourceScriptSource;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * @author zhengcanlai
 */
@SuppressWarnings({"rawtypes", "unchecked", "Duplicates"})
public class RedisRankingsServiceImpl implements RankingsService {

	private static Logger logger = LoggerFactory.getLogger(RedisRankingsServiceImpl.class);

	private DefaultRedisScript<List> multikeyZaddScript;

	private DefaultRedisScript<List> multikeyZincrbyScript;

	private DefaultRedisScript<List> multikeyZremScript;

	private DefaultRedisScript<List> multikeyZincrPosScoreAndSetTuningFactorScript;

	private DefaultRedisScript<List> multikeyZincrAllScoreAndSetTuningFactorScript;

	/**
	 * 存储已设置过过期时间的key
	 */
	private Cache<Object, Object> expiredKeyCache = CacheBuilder.newBuilder().initialCapacity(100).maximumSize(10000).build();

	private static final String RANKINGS_SCORE_SYNC_FAIL_KEY = "rankings:score_sync_fail";

	private volatile Set<RankingsRank> rankingsRankSyncSet = ConcurrentHashMap.newKeySet();

	private volatile boolean shuttingDown = false;

	private String getRankingsRankSyncFailKey() {
		return RANKINGS_SCORE_SYNC_FAIL_KEY;
	}

	private StringRedisTemplate redisTemplate;

	private RankingsRankDao rankingsRankDao;

	private ObjectMapper objectMapper;

	public void setRedisTemplate(StringRedisTemplate redisTemplate) {
		this.redisTemplate = redisTemplate;
	}

	public void setRankingsRankDao(RankingsRankDao rankingsRankDao) {
		this.rankingsRankDao = rankingsRankDao;
	}

	public void setObjectMapper(ObjectMapper objectMapper) {
		this.objectMapper = objectMapper;
	}

	@PostConstruct
	public void init() {
		try {
			multikeyZaddScript = new DefaultRedisScript<>();
			multikeyZaddScript.setScriptSource(new ResourceScriptSource(new ClassPathResource("lua/multikey_zadd.lua")));
			multikeyZaddScript.setResultType(List.class);

			multikeyZincrbyScript = new DefaultRedisScript<>();
			multikeyZincrbyScript.setScriptSource(new ResourceScriptSource(new ClassPathResource("lua/multikey_zincrby.lua")));
			multikeyZincrbyScript.setResultType(List.class);

			multikeyZremScript = new DefaultRedisScript<>();
			multikeyZremScript.setScriptSource(new ResourceScriptSource(new ClassPathResource("lua/multikey_zrem.lua")));
			multikeyZremScript.setResultType(List.class);

			multikeyZincrPosScoreAndSetTuningFactorScript = new DefaultRedisScript<>();
			multikeyZincrPosScoreAndSetTuningFactorScript.setScriptSource(new ResourceScriptSource(new ClassPathResource("lua/multikey_zincr_pos_score_set_tuningfactor.lua")));
			multikeyZincrPosScoreAndSetTuningFactorScript.setResultType(List.class);

			multikeyZincrAllScoreAndSetTuningFactorScript = new DefaultRedisScript<>();
			multikeyZincrAllScoreAndSetTuningFactorScript.setScriptSource(new ResourceScriptSource(new ClassPathResource("lua/multikey_zincr_all_score_set_tuningfactor.lua")));
			multikeyZincrAllScoreAndSetTuningFactorScript.setResultType(List.class);

			String rankingsRankSynFailKey = getRankingsRankSyncFailKey();

			while (true) {
				// 检查上次退出时是否有同步失败的榜单元素,如果是,则进行恢复
				String syncFailRankingsRankJson = redisTemplate.opsForSet().pop(rankingsRankSynFailKey);

				if (StringUtils.isEmpty(syncFailRankingsRankJson)) {
					break;
				} else {
					RankingsRank rankingsRank = objectMapper.readValue(syncFailRankingsRankJson, RankingsRank.class);
					rankingsRankSyncSet.add(rankingsRank);
				}
			}
		} catch (Exception e) {
			logger.error("Exception happen while initialize RedidRankingServiceImpl.", e);
			throw new RuntimeException(e);
		}
	}

	private List<String> fetchKeys(List<Rankings> rankingsList) {
		List<String> keys = new ArrayList<>(rankingsList.size());
		rankingsList.forEach(ranking -> keys.add(ranking.getName()));
		return keys;
	}

	private void expireRankingsIfNecessary(List<Rankings> rankingsList) {
		List<Rankings> shouldExpiredRankingsList = new ArrayList<>(rankingsList.size());

		for (Rankings ranking : rankingsList) {
			String name = ranking.getName();

			if (expiredKeyCache.getIfPresent(name) == null) {
				shouldExpiredRankingsList.add(ranking);
			}
		}

		if (!CollectionUtils.isEmpty(shouldExpiredRankingsList)) {
			redisTemplate.executePipelined((RedisCallback<?>) connection -> {
				StringRedisConnection conn = (StringRedisConnection) connection;

				shouldExpiredRankingsList.forEach(item -> {
					if (item.getExpTime() != null) {
						conn.expireAt(item.getName(), item.getExpTime().getEpochSecond());
					}
				});

				return null;
			});

			Map<String, Long> expiredRankingsMap = new HashMap<>(shouldExpiredRankingsList.size());
			shouldExpiredRankingsList.forEach(item -> expiredRankingsMap.put(item.getName(), System.currentTimeMillis()));

			expiredKeyCache.putAll(expiredRankingsMap);
		}
	}

	/**
	 * 标记榜单分数信息需要(稍后)重新同步到数据库
	 *
	 * @param rankings 需要同步的榜单
	 */
	@Override
	public void markRankingsNeedSync(Rankings rankings) {
		String rankingsName = rankings.getName();
		String relatedId = rankings.getRelatedId();

		Set<String> identityIds = redisTemplate.opsForZSet().reverseRange(rankingsName, 0, -1);

		if (CollectionUtils.isEmpty(identityIds)) {
			return;
		}

		rankingsRankSyncSet.addAll(identityIds.stream().map(identityId -> new RankingsRank(rankingsName, identityId, relatedId)).collect(Collectors.toSet()));
	}

	/**
	 * 标记榜单分数信息需要(稍后)同步至数据库
	 *
	 * @param rankingsList 榜单列表
	 * @param identityIdList 榜单标识Id列表
	 */
	private void markRankingsScoreNeedSync(List<Rankings> rankingsList, List<String> identityIdList) {
		Assert.isTrue(rankingsList.size() == identityIdList.size(), "rankingsList's size is not equal to identityIdList's size");
		int i = 0;

		for (Rankings rankings : rankingsList) {
			String rankingsName = rankings.getName();
			String identityId = identityIdList.get(i++);

			rankingsRankSyncSet.add(new RankingsRank(rankingsName, identityId, rankings.getRelatedId()));
		}
	}

	private void markRankingsScoreNeedSyncAndRankToDel(List<Rankings> rankingsList, List<String> identityIdList, List<Double> scoreList) {
		Assert.isTrue(rankingsList.size() == identityIdList.size() && rankingsList.size() == scoreList.size(), "rankingsList's size is not equal to identityIdList's size or rankingsList's size is not equal to scoreList's size");

		int i = 0;

		for (Rankings rankings : rankingsList) {
			String rankingsName = rankings.getName();
			Double score = scoreList.get(i);
			String identityId = identityIdList.get(i++);

			RankingsRank rankingsRank = new RankingsRank(rankingsName, identityId, rankings.getRelatedId());
			rankingsRank.setDeleted(score < 1);
			rankingsRankSyncSet.add(rankingsRank);
		}
	}

	private List<Boolean> convertLongListToBooleanList(List<Long> list) {
		return list.stream().map(item -> item == 1L).collect(Collectors.toList());
	}

	private List<Double> convertStringListToDoubleList(List<String> list) {
		return list.stream().map(Double::valueOf).collect(Collectors.toList());
	}

	private void checkRankingsListAndIdentityIdList(List<Rankings> rankingsList, List<String> identityIdList) {
		Assert.notEmpty(rankingsList, "rankingsList can not be null or empty");
		Assert.notEmpty(identityIdList, "identityIdList can not be null or empty");
		Assert.isTrue(rankingsList.size() == identityIdList.size(), "the size of rankingsList must be equal to the size of identityIdList");
	}

	private void checkScore(Long score) {
		Assert.notNull(score, "score can not be null");
	}

	private void checkTuningFactor(Double tuningFactor) {
		Assert.isTrue(tuningFactor != null && tuningFactor >= 0 && tuningFactor < 1, "tuningFactor can not be null and must be between 0 and 1");
	}

	@Override
	public List<Boolean> setToRankings(List<Rankings> rankingsList, List<String> identityIdList, Long score, Double tuningFactor) {
		checkRankingsListAndIdentityIdList(rankingsList, identityIdList);
		checkScore(score);
		checkTuningFactor(tuningFactor);

		List<String> keys = fetchKeys(rankingsList);
		Double totalScore = score + tuningFactor;

		List<String> args = new ArrayList<>(identityIdList.size() + 1);
		args.add(String.valueOf(totalScore));
		args.addAll(identityIdList);

		List<Long> scriptResult = redisTemplate.execute(multikeyZaddScript, keys, args.toArray(new Object[0]));
		expireRankingsIfNecessary(rankingsList);
		markRankingsScoreNeedSync(rankingsList, identityIdList);

		return convertLongListToBooleanList(scriptResult);
	}

	private List<Double> incrValueToToRankings(List<Rankings> rankingsList, List<String> identityIdList, Double value) {
		checkRankingsListAndIdentityIdList(rankingsList, identityIdList);

		List<String> keys = fetchKeys(rankingsList);

		List<String> args = new ArrayList<>(identityIdList.size() + 1);
		args.add(String.valueOf(value));
		args.addAll(identityIdList);

		List<String> scriptResult = redisTemplate.execute(multikeyZincrbyScript, keys, args.toArray(new Object[0]));
		expireRankingsIfNecessary(rankingsList);
		markRankingsScoreNeedSync(rankingsList, identityIdList);

		return convertStringListToDoubleList(scriptResult);
	}

	@Override
	public List<Double> incrScoreToRankings(List<Rankings> rankingsList, List<String> identityIdList, Long score) {
		checkScore(score);
		return incrValueToToRankings(rankingsList, identityIdList, score.doubleValue());
	}

	@Override
	public List<Double> incrTuningFactorToRankings(List<Rankings> rankingsList, List<String> identityIdList, Double tuningFactor) {
		checkTuningFactor(tuningFactor);
		return incrValueToToRankings(rankingsList, identityIdList, tuningFactor);
	}

	@Override
	public List<Double> incrScoreAndTuningFactorToRankings(List<Rankings> rankingsList, List<String> identityIdList, Long score, Double tuningFactor) {
		checkScore(score);
		checkTuningFactor(tuningFactor);

		return incrValueToToRankings(rankingsList, identityIdList, score + tuningFactor);
	}

	@Override
	public List<Double> incrScoreAndSetTuningFactorToRankings(List<Rankings> rankingsList, List<String> identityIdList, List<Long> scoreList, Double tuningFactor) {
		Assert.isTrue(rankingsList.size() == identityIdList.size() && rankingsList.size() == scoreList.size(), "identityList and scoreList must have same size with rankingsList!");
		checkTuningFactor(tuningFactor);

		List<String> keys = fetchKeys(rankingsList);
		Object[] params = new Object[rankingsList.size() * 2 + 1];
		params[0] = String.valueOf(tuningFactor);

		for (int i = 0; i < rankingsList.size(); i++) {
			params[i * 2 + 1] = identityIdList.get(i);
			params[i * 2 + 2] = String.valueOf(scoreList.get(i));
		}

		List<String> scriptResult = redisTemplate.execute(multikeyZincrAllScoreAndSetTuningFactorScript, keys, params);
		expireRankingsIfNecessary(rankingsList);
		List<Double> returnResult = convertStringListToDoubleList(scriptResult);

		markRankingsScoreNeedSyncAndRankToDel(rankingsList, identityIdList, returnResult);

		return returnResult;
	}

	@Override
	public List<Double> incrScoreAndSetTuningFactorToRankings(List<Rankings> rankingsList, List<String> identityIdList, Long score, Double tuningFactor) {
		checkScore(score);
		checkTuningFactor(tuningFactor);

		List<String> keys = fetchKeys(rankingsList);
		List<String> args = new ArrayList<>(identityIdList.size());
		args.add(String.valueOf(score));
		args.add(String.valueOf(tuningFactor));
		args.addAll(identityIdList);

		List<String> scriptResult = redisTemplate.execute(multikeyZincrPosScoreAndSetTuningFactorScript, keys, args.toArray(new Object[0]));
		expireRankingsIfNecessary(rankingsList);
		markRankingsScoreNeedSync(rankingsList, identityIdList);

		return convertStringListToDoubleList(scriptResult);
	}

	@Override
	public List<Boolean> removeFromRankings(List<Rankings> rankingsList, List<String> identityIdList) {
		List<String> keys = fetchKeys(rankingsList);
		List<Long> scriptResult = redisTemplate.execute(multikeyZremScript, keys, identityIdList.toArray());

		return convertLongListToBooleanList(scriptResult);
	}

	private boolean isRankingsExpired(Rankings rankings) {
		return rankings.getExpTime() != null && rankings.getExpTime().isBefore(Instant.now());
	}

	@Override
	public Long getRank(String identityId, Rankings rankings, boolean reverse) {
		if (isRankingsExpired(rankings)) {
			return null;
		}

		String name = rankings.getName();
		Long rank;

		if (reverse) {
			rank = redisTemplate.opsForZSet().reverseRank(name, identityId);
		} else {
			rank = redisTemplate.opsForZSet().rank(name, identityId);
		}

		return rank == null ? null : rank + 1;
	}

	@Override
	public List<Long> getMultiRank(String identityId, List<Rankings> rankingsList, boolean reverse) {

		List<Object> rawRankList = redisTemplate.executePipelined((RedisCallback<Object>) rawConnection -> {
			StringRedisConnection connection = (StringRedisConnection) rawConnection;

			rankingsList.forEach(rankings -> {
				if (reverse) {
					connection.zRevRank(rankings.getName(), identityId);
				} else {
					connection.zRank(rankings.getName(), identityId);
				}
			});

			return null;
		});

		return rawRankList.stream().map(rawRank -> {
			if (rawRank == null) {
				return null;
			} else {
				return (Long) rawRank + 1;
			}
		}).collect(Collectors.toList());
	}

	@Override
	public List<Double> getTotalScore(List<String> identityIdList, Rankings rankings) {
		if (isRankingsExpired(rankings)) {
			return Collections.emptyList();
		}

		List<Object> rawResultList = redisTemplate.executePipelined((RedisCallback<Object>) connection -> {
			StringRedisConnection conn = (StringRedisConnection) connection;

			for (String identityId : identityIdList) {
				conn.zScore(rankings.getName(), identityId);
			}

			return null;
		});

		return rawResultList.stream().map(result -> (Double)result).collect(Collectors.toList());
	}

	@Override
	public Double getTotalScore(String identityId, Rankings rankings) {
		if (isRankingsExpired(rankings)) {
			return null;
		}

		return redisTemplate.opsForZSet().score(rankings.getName(), identityId);
	}

	@Override
	public Double getTotalScore(Long rank, Rankings rankings) {
		if (isRankingsExpired(rankings)) {
			return null;
		}

		Assert.isTrue(rank != null && rank != 0, "rank can not be null and must not be equal to 0.");

		long pos = rank > 0 ? rank - 1 : rank;
		Set<TypedTuple<String>> tupleSet = redisTemplate.opsForZSet().reverseRangeWithScores(rankings.getName(), pos, pos);

		if (CollectionUtils.isEmpty(tupleSet)) {
			return null;
		}

		return tupleSet.iterator().next().getScore();
	}

	@Override
	public Long countRankings(Rankings rankings) {
		if (isRankingsExpired(rankings)) {
			return null;
		}

		return redisTemplate.opsForZSet().zCard(rankings.getName());
	}

	@Override
	public RankingElement getRanking(String identityId, Rankings rankings) {
		if (isRankingsExpired(rankings)) {
			return null;
		}

		String name = rankings.getName();

		List<Object> result = redisTemplate.executePipelined((RedisCallback<?>) connection -> {
			StringRedisConnection conn = (StringRedisConnection) connection;

			conn.zRevRank(name, identityId);
			conn.zCard(name);

			return null;
		});

		Long rank = (Long) result.get(0);

		if (rank == null) {
			return null;
		}

		Long count = (Long) result.get(1);

		long start = rank == 0 ? 0 : rank - 1;
		long end = rank == count - 1 ? -1 : rank + 1;

		Set<TypedTuple<String>> tupleSet = redisTemplate.opsForZSet().reverseRangeWithScores(name, start, end);
		List<TypedTuple<String>> tupleList = new ArrayList<>(tupleSet);

		RankingElement rankingElement = new RankingElement();
		rankingElement.setIdentityId(identityId);
		Double totalScore;

		if (count == 1) {
			totalScore = tupleList.get(0).getScore();
		} else if (rank == 0) {
			totalScore = tupleList.get(0).getScore();
			rankingElement.setScoreExceed(totalScore.longValue() - tupleList.get(1).getScore().longValue());
		} else if (rank == count - 1) {
			totalScore = tupleList.get(1).getScore();
			rankingElement.setScoreBehind(tupleList.get(0).getScore().longValue() - totalScore.longValue());
		} else {
			totalScore = tupleList.get(1).getScore();
			rankingElement.setScoreExceed(totalScore.longValue() - tupleList.get(2).getScore().longValue());
			rankingElement.setScoreBehind(tupleList.get(0).getScore().longValue() - totalScore.longValue());
		}

		rankingElement.setRank(rank + 1);
		rankingElement.setTotalScore(totalScore);
		rankingElement.setScore(totalScore.longValue());
		rankingElement.setTuningFactor(totalScore - rankingElement.getScore());

		return rankingElement;
	}

	@Override
	public <T> T getRanking(String identityId, Rankings rankings, SingleRankingElementWrapper wrapper) {
		RankingElement rankingElement = getRanking(identityId, rankings);
		return rankingElement == null ? null : wrapper.warp(rankingElement);
	}

	private List<RankingElement> convertTypedTupleSetToRankingElementList(Set<TypedTuple<String>> tupleSet, long start, long end, boolean withRank, boolean withBehindAndExceed) {
		List<RankingElement> rankingElementList = new ArrayList<>(tupleSet.size());
		long rank = start;
		RankingElement previousRankingElement = null;

		for (TypedTuple<String> tuple : tupleSet) {
			RankingElement currentRankingElement = new RankingElement(tuple.getValue());

			Double totalScore = tuple.getScore();
			currentRankingElement.setTotalScore(totalScore);
			currentRankingElement.setScore(totalScore.longValue());
			currentRankingElement.setTuningFactor(totalScore - currentRankingElement.getScore());

			if (withRank) {
				currentRankingElement.setRank(rank++);
			}

			if (withBehindAndExceed && previousRankingElement != null) {
				long offset = previousRankingElement.getScore() - currentRankingElement.getScore();

				previousRankingElement.setScoreExceed(offset);
				currentRankingElement.setScoreBehind(offset);
			}

			rankingElementList.add(currentRankingElement);
			previousRankingElement = currentRankingElement;
		}

		return rankingElementList;
	}

	@Override
	public List<RankingElement> getRankingsByPage(Rankings rankings, long start, long end, boolean withRank, boolean withBehindAndExceed) {
		if (isRankingsExpired(rankings)) {
			return Collections.emptyList();
		}

		String name = rankings.getName();
		Set<TypedTuple<String>> tupleSet = redisTemplate.opsForZSet().reverseRangeWithScores(name, start - 1, end < 0 ? end : end - 1);

		if (CollectionUtils.isEmpty(tupleSet)) {
			return Collections.emptyList();
		}

		return convertTypedTupleSetToRankingElementList(tupleSet, start, end, withRank, withBehindAndExceed);
	}

	@Override
	public <T> List<T> getRankingsByPage(Rankings rankings, long start, long end, boolean withRank, boolean withBehindAndExceed, MultiRankingElementWrapper wrapper) {
		List<RankingElement> rankingElementList = getRankingsByPage(rankings, start, end, withRank, withBehindAndExceed);
		return CollectionUtils.isEmpty(rankingElementList) ? Collections.emptyList() : wrapper.wrap(rankingElementList);
	}

	private List<RankingElement> convertDefaultTypedTupleSetToRankingElementList(Set<DefaultTypedTuple<String>> tupleSet, long start, long end, boolean withRank, boolean withBehindAndExceed) {
		List<RankingElement> rankingElementList = new ArrayList<>(tupleSet.size());
		long rank = start;
		RankingElement previousRankingElement = null;

		for (DefaultTypedTuple<String> tuple : tupleSet) {
			RankingElement currentRankingElement = new RankingElement(tuple.getValue());

			Double totalScore = tuple.getScore();
			currentRankingElement.setTotalScore(totalScore);
			currentRankingElement.setScore(totalScore.longValue());
			currentRankingElement.setTuningFactor(totalScore - currentRankingElement.getScore());

			if (withRank) {
				currentRankingElement.setRank(rank++);
			}

			if (withBehindAndExceed && previousRankingElement != null) {
				long offset = previousRankingElement.getScore() - currentRankingElement.getScore();

				previousRankingElement.setScoreExceed(offset);
				currentRankingElement.setScoreBehind(offset);
			}

			rankingElementList.add(currentRankingElement);
			previousRankingElement = currentRankingElement;
		}

		return rankingElementList;
	}

	@Override
	public List<List<RankingElement>> getRankingsByPage(long start, long end, boolean withRank, boolean withBehindAndExceed, Rankings... rankingsList) {
		List<List<RankingElement>> resultList = new ArrayList<>(rankingsList.length);

		List<Object> tupleSetList = redisTemplate.executePipelined((RedisCallback<?>) connection -> {
			StringRedisConnection conn = (StringRedisConnection) connection;

			for (Rankings rankings : rankingsList) {
				String name = rankings.getName();
				conn.zRevRangeWithScores(name, start - 1, end < 0 ? end : end - 1);
			}

			return null;
		});

		int i = 0;

		for (Rankings rankings : rankingsList) {
			if (isRankingsExpired(rankings)) {
				resultList.add(Collections.emptyList());
			} else {
				Set<DefaultTypedTuple<String>> tupleSet = (Set<DefaultTypedTuple<String>>) tupleSetList.get(i);

				if (CollectionUtils.isEmpty(tupleSet)) {
					resultList.add(Collections.emptyList());
				} else {
					List<RankingElement> rankingElementList = convertDefaultTypedTupleSetToRankingElementList(tupleSet, start, end, withRank, withBehindAndExceed);
					resultList.add(rankingElementList);
				}
			}

			++i;
		}

		return resultList;
	}

	@Override
	public void deleteRankings(List<Rankings> rankingsList) {
		List<String> keys = fetchKeys(rankingsList);
		redisTemplate.delete(keys);
	}

	@Override
	public boolean isRankingsExists(Rankings rankings) {
		if (isRankingsExpired(rankings)) {
			return false;
		} else {
			return redisTemplate.hasKey(rankings.getName());
		}
	}

	@PreDestroy
	public void destroy() {
		shuttingDown = true;
		syncRankingsRankToDB();
	}

	@Scheduled(cron = "0 */3 * * * *")
	public void syncRankingsRankToDB() {
		if (rankingsRankDao == null) {
			if (logger.isDebugEnabled()) {
				logger.debug("There are no rankingsRankDao found, cannot sync to DB.");
			}

			return;
		}

		if (logger.isDebugEnabled()) {
			logger.debug("Starting to sync rankings rank to db.");
		}

		if (rankingsRankSyncSet.isEmpty()) {
			if (logger.isDebugEnabled()) {
				logger.debug("No any rankings rank need to be sync to db.");
			}
			return;
		}

		Set<RankingsRank> copyOfRankingsRankSyncSet = rankingsRankSyncSet;
		rankingsRankSyncSet = ConcurrentHashMap.newKeySet();

		List<RankingsRank> updatedRankingsRankList = new ArrayList<>(copyOfRankingsRankSyncSet);
		List<RankingsRank> deletedRankingRankList = new ArrayList<>();

		try {
			Set<String> updatedRankingsName = new HashSet<>();

			List<Object> resultList = redisTemplate.executePipelined((RedisCallback<Object>) connection -> {
				StringRedisConnection conn = (StringRedisConnection) connection;

				updatedRankingsRankList.forEach(rankingsRank -> {
					String rankingsName = rankingsRank.getRankingsName();
					updatedRankingsName.add(rankingsName);

					conn.zScore(rankingsName, rankingsRank.getIdentityId());
				});

				return null;
			});

			int i = 0;
			Iterator<RankingsRank> iterator = updatedRankingsRankList.iterator();

			while (iterator.hasNext()) {
				RankingsRank rankingsRank = iterator.next();
				Double score = (Double) resultList.get(i++);

				if (score != null) {
					rankingsRank.setScore(score);
				} else {
					if (rankingsRank.isDeleted()) {
						deletedRankingRankList.add(rankingsRank);
					}

					// 可能榜单数据已过期
					iterator.remove();
				}
			}

			rankingsRankDao.saveOrUpdate(updatedRankingsRankList);

			if (!deletedRankingRankList.isEmpty()) {
				rankingsRankDao.deleteRankingsRanks(deletedRankingRankList);
			}

			// 更新榜单排名信息
			updatedRankingsName.forEach(rankingsName -> rankingsRankDao.updateRankingsRank(rankingsName));
		} catch (Exception e) {
			// @formatter:off
			List<String> syncFailRankingsElementList = updatedRankingsRankList.stream()
					.map(rankingsRank -> (rankingsRank.getRankingsName() + ":" + rankingsRank.getIdentityId()))
					.collect(Collectors.toList());
			// @formatter:on

			logger.error("Error while sync rankings rank to db for rankings rank [{}].", StringUtils.collectionToCommaDelimitedString(syncFailRankingsElementList), e);

			if (shuttingDown) {
				// 如果退出时保存至数据库失败,则暂存至redis中
				String rankingsRankSynFailKey = getRankingsRankSyncFailKey();

				redisTemplate.opsForSet().add(rankingsRankSynFailKey, updatedRankingsRankList.stream().map(rankingsRank -> {
					try {
						return objectMapper.writeValueAsString(rankingsRank);
					} catch (JsonProcessingException jsonProcessingException) {
						logger.error("Error while serializing object to json.", e);
						throw new RuntimeException(e);
					}
				}).toArray(String[]::new));
			} else {
				// 重新标记需要同步
				rankingsRankSyncSet.addAll(updatedRankingsRankList);
			}
		}
	}

}
