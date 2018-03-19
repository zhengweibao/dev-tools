package com.zdevzone.tools.rankings;

import java.time.Instant;

/**
 * 榜单
 *
 * @author zhengcanlai
 */
public interface Rankings {

	/**
	 * 榜单名称
	 *
	 * @return 榜单名称
	 */
	String getName();

	/**
	 * 榜单过期时间
	 *
	 * @return 过期时间
	 */
	Instant getExpTime();

	/**
	 * 榜单关联ID
	 *
	 * @return 关联ID
	 */
	String getRelatedId();
}
