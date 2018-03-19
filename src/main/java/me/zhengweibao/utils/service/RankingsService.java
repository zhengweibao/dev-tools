package me.zhengweibao.utils.service;

import me.zhengweibao.utils.rankings.MultiRankingElementWrapper;
import me.zhengweibao.utils.rankings.RankingElement;
import me.zhengweibao.utils.rankings.Rankings;
import me.zhengweibao.utils.rankings.SingleRankingElementWrapper;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 榜单Service
 * <p>
 * 榜单{@link Rankings}:由榜单名和榜单过期时间组成
 * </p>
 * <p>
 * 榜单排名元素{@link RankingElement}:由元素标识Id,分数,调节因子值组成;<br/>
 * 榜单元素的分数值必须为long类型,如果调用者的原始元素分数值为浮点值,则调用者必须负责对其进行转化;<br/>
 * 调节因子用于控制当榜单元素分数相同时的排名先后顺序,同时,调用者也需要自己保证多次增加调解因子值后其结果值小于1
 * </p>
 * 
 * @author zhengcanlai
 */
public interface RankingsService {

	/**
	 * 标记榜单是否需要同步至数据库
	 *
	 * @param rankings 榜单
	 */
	void markRankingsNeedSync(Rankings rankings);

	/**
	 * 将一系列榜单中的对应的元素标识Id设置为指定的分数,若某一元素标识Id在某一榜单中已存在,其分数和调节因子值将被覆写
	 * <p>
	 * Note:实现类必须保证该操作的原子性
	 * </p>
	 * 
	 * @param rankingsList 榜单列表
	 * @param identityIdList 榜单元素标识Id列表,其值的顺序与rankingsList的顺序相同
	 * @param score 欲设置的榜单元素的分数
	 * @param tuningFactor 欲设置的调节因子值,0<=tuningFactor<1
	 * @return 对应的榜单元素标识Id是否首次添加至榜单中,其列表值的顺序与rankings列表的顺序相同
	 */
	List<Boolean> setToRankings(List<Rankings> rankingsList, List<String> identityIdList, Long score, Double tuningFactor);

	/**
	 * 为一系列榜单中的对应的元素标识Id增加对应的分数值
	 * <p>
	 * Note:实现类必须保证该操作的原子性
	 * </p>
	 * 
	 * @param rankingsList 榜单列表
	 * @param identityIdList 榜单元素标识Id列表,其值的顺序与rankingsList的顺序相同
	 * @param score 欲增加的分数值
	 * @return 对应的榜单元素标识Id在各个榜单中增加分数后的总分数值列表,其列表值的顺序与rankings列表的顺序相同
	 */
	List<Double> incrScoreToRankings(List<Rankings> rankingsList, List<String> identityIdList, Long score);

	/**
	 * 为一系列榜单中的对应的元素标识Id增加对应的调节因子值
	 * <p>
	 * Note:实现类必须保证该操作的原子性;调用者必须保证多次调用后的tuningFactor值小于1
	 * </p>
	 * 
	 * @param rankingsList 榜单列表
	 * @param identityIdList 榜单元素标识Id列表,其值的顺序与rankingsList的顺序相同
	 * @param tuningFactor 欲增加的调节因子值,0<=tuningFactor<1
	 * @return 对应的榜单元素标识Id在各个榜单中增加调节因子值后的总分数值列表,其列表值的顺序与rankings列表的顺序相同
	 */
	List<Double> incrTuningFactorToRankings(List<Rankings> rankingsList, List<String> identityIdList, Double tuningFactor);

	/**
	 * 为一系列榜单中的对应的元素标识Id增加对应的分数值和调节因子值
	 * <p>
	 * Note:实现类必须保证该操作的原子性;调用者必须保证多次调用后的tuningFactor值小于1
	 * </p>
	 * 
	 * @param rankingsList 榜单列表
	 * @param identityIdList 榜单元素标识Id列表,其值的顺序与rankingsList的顺序相同
	 * @param score 欲增加的分数值
	 * @param tuningFactor 欲增加的调节因子值,0<=tuningFactor<1
	 * @return 对应的榜单元素标识Id在各个榜单中增加分数值和调节因子值后的总分数值列表,其列表值的顺序与rankings列表的顺序相同
	 */
	List<Double> incrScoreAndTuningFactorToRankings(List<Rankings> rankingsList, List<String> identityIdList, Long score, Double tuningFactor);

	/**
	 * 为一系列榜单中的对应的元素标识Id增加对应的分数值和调节因子值
	 * <p>
	 * Note:实现类必须保证该操作的原子性;调用者必须保证多次调用后的tuningFactor值小于1
	 * </p>
	 *
	 * @param rankingsList 榜单列表
	 * @param identityIdList 榜单元素标识Id列表,其值的顺序与rankingsList的顺序相同
	 * @param scoreList 欲增加的分数值列表，其值的顺序与rankingsList的顺序相同
	 * @param tuningFactor 欲增加的调节因子值,0<=tuningFactor<1
	 * @return 对应的榜单元素标识Id在各个榜单中增加分数值和调节因子值后的总分数值列表,其列表值的顺序与rankings列表的顺序相同
	 */
	List<Double> incrScoreAndSetTuningFactorToRankings(List<Rankings> rankingsList, List<String> identityIdList, List<Long> scoreList, Double tuningFactor);

	/**
	 * 为一系列榜单中的对应的元素标识Id增加对应的分数值,并同时设置其调节因子值
	 * <p>
	 * Note:实现类必须保证该操作的原子性
	 * </p>
	 * 
	 * @param rankingsList 榜单列表
	 * @param identityIdList 榜单元素标识Id列表,其值的顺序与rankingsList的顺序相同
	 * @param score 欲增加的分数值
	 * @param tuningFactor 欲设置的调节因子值,0<=tuningFactor<1
	 * @return 对应的榜单元素标识Id在各个榜单中增加分数值和设置调节因子值后的总分数值列表,其列表值的顺序与rankings列表的顺序相同
	 */
	List<Double> incrScoreAndSetTuningFactorToRankings(List<Rankings> rankingsList, List<String> identityIdList, Long score, Double tuningFactor);

	/**
	 * 从一系列榜单中移除对应的榜单元素标识Id
	 * 
	 * @param rankingsList 榜单列表
	 * @param identityIdList 欲移除的榜单元素标识Id列表,其值的顺序与rankingsList的顺序相同
	 * @return 对应的榜单元素标识Id之前是否存在榜单中,其列表值的顺序与rankings列表的顺序相同
	 */
	List<Boolean> removeFromRankings(List<Rankings> rankingsList, List<String> identityIdList);

	/**
	 * 获取某一元素标识Id在榜单中的按总分数值由高至低的排名(基于1)
	 * 
	 * @param identityId 榜单元素标识Id
	 * @param rankings 榜单
	 * @return 如果榜单已过期或不存在或对应的元素不存在该榜单中,返回null,否则返回其对应的由高至低的基于1的排名
	 */
	default Long getRank(String identityId, Rankings rankings) {
		return getRank(identityId, rankings, true);
	}

	/**
	 * 获取某一元素标识Id在榜单中的排名(基于1)
	 * 
	 * @param identityId 榜单元素标识Id
	 * @param rankings 榜单
	 * @param reverse 如果为true,按分数值由高至低计算排名,否则按分数值由低至高计算排名
	 * @return 如果榜单已过期或不存在或对应的元素不存在该榜单中,返回null,否则返回其对应的基于1的排名
	 */
	Long getRank(String identityId, Rankings rankings, boolean reverse);

	/**
	 * 获取某一元素标识Id在所有榜单中的排名
	 *
	 * @param identityId 榜单元素标识Id
	 * @param rankingsList 榜单列表
	 * @param reverse 如果为true,按分数值由高至低计算排名,否则按分数值由低至高计算排名
	 * @return 如果榜单已过期或不存在或对应的元素不存在该榜单中,返回列表中对应的index为null,否则返回其对应的排名
	 */
	List<Long> getMultiRank(String identityId, List<Rankings> rankingsList, boolean reverse);

	/**
	 * 获取某一元素标识Id在榜单中的分数值
	 *
	 * @param identityIdList 榜单元素标识Id列表
	 * @param rankings 榜单
	 * @return 如果榜单已过期或不存在或对应的元素不存在该榜单中,返回null,否则返回其对应的分数值
	 */
	default List<Long> getScore(List<String> identityIdList, Rankings rankings) {
		List<Double> totalScore = getTotalScore(identityIdList, rankings);
		return totalScore.stream().map(score -> score == null ? null : score.longValue()).collect(Collectors.toList());
	}

	/**
	 * 获取某一元素标识Id在榜单中的分数值
	 *
	 * @param identityId 榜单元素标识Id
	 * @param rankings 榜单
	 * @return 如果榜单已过期或不存在或对应的元素不存在该榜单中,返回null,否则返回其对应的分数值
	 */
	default Long getScore(String identityId, Rankings rankings) {
		Double totalScore = getTotalScore(identityId, rankings);
		return totalScore == null ? null : totalScore.longValue();
	}

	/**
	 * 获取榜单中排名为rank的元素的分数值
	 * 
	 * @param rank 榜单排名(基于1),可接受负值
	 * @param rankings 榜单
	 * @return 如果榜单已过期或不存在或对应的元素不存在该榜单中,返回null,否则返回其对应的分数值
	 */
	default Long getScore(Long rank, Rankings rankings) {
		Double totalScore = getTotalScore(rank, rankings);
		return totalScore == null ? null : totalScore.longValue();
	}

	/**
	 * 获取某一元素标识Id在榜单中的调节因子值
	 * 
	 * @param identityId 榜单元素标识Id
	 * @param rankings 榜单
	 * @return 如果榜单已过期或不存在或对应的元素不存在该榜单中,返回null,否则返回其对应的调节因子值
	 */
	default Double getTuningFactor(String identityId, Rankings rankings) {
		Double totalScore = getTotalScore(identityId, rankings);
		return totalScore == null ? null : totalScore - totalScore.longValue();
	}

	/**
	 * 获取榜单中排名为rank的元素的调节因子值
	 * 
	 * @param rank 榜单排名(基于1),可接受负值
	 * @param rankings 榜单
	 * @return 如果榜单已过期或不存在或对应的元素不存在该榜单中,返回null,否则返回其对应的调节因子值
	 */
	default Double getTuningFactor(Long rank, Rankings rankings) {
		Double totalScore = getTotalScore(rank, rankings);
		return totalScore == null ? null : totalScore - totalScore.longValue();
	}

	/**
	 * 获取一系列用户对应的分数
	 *
	 * @param identityIdList 榜单元素标识Id列表
	 * @param rankings 榜单
	 * @return 如果榜单已过期或不存在或对应的元素不存在该榜单中,返回null,否则返回其对应的总分数值
	 */
	List<Double> getTotalScore(List<String> identityIdList, Rankings rankings);

	/**
	 * 获取某一元素标识Id在榜单中的总分数值
	 * 
	 * @param identityId 榜单元素标识Id
	 * @param rankings 榜单
	 * @return 如果榜单已过期或不存在或对应的元素不存在该榜单中,返回null,否则返回其对应的总分数值
	 */
	Double getTotalScore(String identityId, Rankings rankings);

	/**
	 * 获取榜单中排名为rank的元素的总分数值
	 * 
	 * @param rank 榜单排名(基于1),可接受负值
	 * @param rankings 榜单
	 * @return 如果榜单已过期或不存在或对应的元素不存在该榜单中,返回null,否则返回其对应的总分数值
	 */
	Double getTotalScore(Long rank, Rankings rankings);

	/**
	 * 计算某一榜单中的元素个数
	 * 
	 * @param rankings 榜单
	 * @return 如果榜单已过期或不存在,返回null,否则返回其对应的元素个数
	 */
	Long countRankings(Rankings rankings);

	/**
	 * 根据榜单标识Id获取榜单排名元素信息
	 * 
	 * @param identityId 榜单元素标识Id
	 * @param rankings 榜单
	 * @return 如果榜单已过期或不存在,返回null,否则返回其对应的榜单排名元素信息
	 */
	RankingElement getRanking(String identityId, Rankings rankings);

	/**
	 * 根据榜单标识Id获取榜单排名元素信息,并对结果元素信息进行封装
	 * 
	 * @param identityId 榜单元素标识Id
	 * @param rankings 榜单
	 * @param wrapper 用于封装结果数据的封装器
	 * @return 如果榜单已过期或不存在,返回null,否则返回其对应的封装结果
	 */
	<T> T getRanking(String identityId, Rankings rankings, SingleRankingElementWrapper wrapper);

	/**
	 * 分页获取榜单排名元素信息列表,榜单元素按总分数值由高至底排序
	 * 
	 * @param rankings 榜单
	 * @param start 榜单排名起始值,基于1
	 * @param end 榜单排名结束值(含),可接受负值.当为正值时,基于1,如果数据足够的话,返回end-start+1条数据,否则返回剩余的所有数据;当为负值时,基于-1
	 * @param withRank 结果列表中的元素是否设置排名顺序信息(基于1)
	 * @param withBehindAndExceed 结果列表中的元素是否设置领先和落后排名信息
	 * @return 如果榜单已过期或不存在,返回空列表,否则对应的分页榜单排名元素信息列表
	 */
	List<RankingElement> getRankingsByPage(Rankings rankings, long start, long end, boolean withRank, boolean withBehindAndExceed);

	/**
	 * 分页获取榜单排名元素信息列表,榜单排名元素按总分数值由高至底排序,并对结果列表数据进行封装
	 * 
	 * @param rankings 榜单
	 * @param start 榜单排名起始值,基于1
	 * @param end 榜单排名结束值(含),可接受负值.当为正值时,基于1,如果数据足够的话,返回end-start+1条数据,否则返回剩余的所有数据;当为负值时,基于-1
	 * @param withRank 结果列表中的元素是否设置排名顺序信息(基于1)
	 * @param withBehindAndExceed 结果列表中的元素是否设置领先和落后排名信息
	 * @param wrapper 用于封装结果数据列表的封装器
	 * @return 如果榜单已过期或不存在,返回空列表,否则返回对应的分页结果数据列表
	 */
	<T> List<T> getRankingsByPage(Rankings rankings, long start, long end, boolean withRank, boolean withBehindAndExceed, MultiRankingElementWrapper wrapper);

	/**
	 * 获取一个或多个榜单的分页榜单排名元素信息列表,榜单元素按总分数值由高至底排序
	 * 
	 * @param start 榜单排名起始值,基于1
	 * @param end 榜单排名结束值(含),可接受负值.当为正值时,基于1,如果数据足够的话,返回end-start+1条数据,否则返回剩余的所有数据;当为负值时,基于-1
	 * @param withRank 结果列表中的元素是否设置排名顺序信息(基于1)
	 * @param withBehindAndExceed 结果列表中的元素是否设置领先和落后排名信息
	 * @param rankingsList 榜单列表
	 * @return 由各个榜单的分页榜单排名元素信息列表所组成的列表,其列表的顺序与rankings列表的顺序相同;如果某一榜单已过期或不存在, 返回的对应榜单排名元素信息列表为空列表
	 */
	List<List<RankingElement>> getRankingsByPage(long start, long end, boolean withRank, boolean withBehindAndExceed, Rankings... rankingsList);

	/**
	 * 删除一个或多个榜单
	 * 
	 * @param rankingsList 榜单列表
	 */
	void deleteRankings(List<Rankings> rankingsList);

	/**
	 * 判断是否存在某一榜单
	 * 
	 * @param rankings 榜单
	 * @return T/F
	 */
	boolean isRankingsExists(Rankings rankings);

}
