package me.zhengweibao.utils.dao;

import me.zhengweibao.utils.domain.RankingsRank;

import java.util.List;

/**
 * 榜单排名dao
 * 
 * @author zhengcanlai
 */
public interface RankingsRankDao {

	/**
	 * 保存榜单排名记录
	 * 
	 * @param records 榜单记录
	 */
	void saveOrUpdate(List<RankingsRank> records);

	/**
	 * 更新榜单排名信息
	 * 
	 * @param rankingsName 榜单名称
	 */
	void updateRankingsRank(String rankingsName);

	/**
	 * 删除排行榜上数据
	 * @param records 要删除的记录
	 */
	void deleteRankingsRanks(List<RankingsRank> records);
}
