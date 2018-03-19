package me.zhengweibao.utils.rankings;

import java.util.List;

/**
 * 多榜单排名元素包装器
 * 
 * @author zhengcanlai
 */
public interface MultiRankingElementWrapper {

	/**
	 * 将榜单排名元素列表包装为另一目标元素列表
	 * 
	 * @param rankingElementList 榜单排名元素列表
	 * @param <T> 目标包装元素
	 * @return 目标元素列表
	 */
	<T> List<T> wrap(List<RankingElement> rankingElementList);
}
