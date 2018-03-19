package com.zdevzone.tools.rankings;

import org.springframework.util.Assert;

import java.time.Instant;

/**
 * 榜单
 *
 * @author zhengcanlai
 */
public class RedisRankings implements Rankings{

	private static final String HASH_TAG_PREFIX_FORMAT = "{%s}:";

	/**
	 * 榜单名
	 */
	private String name;

	/**
	 * 榜单过期时间
	 */
	private Instant expTime;

	/**
	 * 关联Id，归类标识，非空
	 */
	private String relatedId;

	public RedisRankings() {
	}

	public RedisRankings(String name, Instant expTime, String relatedId) {
		Assert.hasText(name, "The name cannot be empty!");
		Assert.hasText(relatedId, "The relatedId cannot be empty!");


		this.name = name;
		this.expTime = expTime;
		this.relatedId = relatedId;
	}

	@Override
	public String getName() {
		Assert.hasText(relatedId, "The relatedId cannot be empty!");

		return String.format(HASH_TAG_PREFIX_FORMAT, relatedId) + name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@Override
	public Instant getExpTime() {
		return expTime;
	}

	public void setExpTime(Instant expTime) {
		this.expTime = expTime;
	}

	@Override
	public String getRelatedId() {
		return relatedId;
	}

	public void setRelatedId(String relatedId) {
		this.relatedId = relatedId;
	}

}
