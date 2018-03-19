package me.zhengweibao.utils.rankings;

/**
 * 单榜单排名元素包装器
 * 
 * @author zhengcanlai
 */
public interface SingleRankingElementWrapper {

	/**
	 * 将榜单排名元素包装为另一目标元素
	 * 
	 * @param rankingElement 榜单排名元素
	 * @return 目标元素
	 */
	<T> T warp(RankingElement rankingElement);
}
