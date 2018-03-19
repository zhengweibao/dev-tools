package com.zdevzone.tools.rankings;

/**
 * 榜单排名元素
 *
 * @author zhengcanlai
 */
public class RankingElement {

	/**
	 * 元素标识Id
	 */
	private String identityId;

	/**
	 * 元素排名(基于1)
	 */
	private Long rank;

	/**
	 * 元素分数
	 */
	private Long score;

	/**
	 * 调节因子值
	 */
	private Double tuningFactor;

	/**
	 * 元素总分数
	 */
	private Double totalScore;

	/**
	 * 该元素落后前一名的分数,当该元素为第一个元素时,其值为null
	 */
	private Long scoreBehind;

	/**
	 * 该元素领先后一名的分数,当该元素为最后一个元素时,其值为null
	 */
	private Long scoreExceed;

	public RankingElement() {

	}

	public RankingElement(String identityId) {
		this.identityId = identityId;
	}

	public String getIdentityId() {
		return identityId;
	}

	public void setIdentityId(String identityId) {
		this.identityId = identityId;
	}

	public Long getRank() {
		return rank;
	}

	public void setRank(Long rank) {
		this.rank = rank;
	}

	public Long getScore() {
		return score;
	}

	public void setScore(Long score) {
		this.score = score;
	}

	public Double getTuningFactor() {
		return tuningFactor;
	}

	public void setTuningFactor(Double tuningFactor) {
		this.tuningFactor = tuningFactor;
	}

	public Double getTotalScore() {
		return totalScore;
	}

	public void setTotalScore(Double totalScore) {
		this.totalScore = totalScore;
	}

	public Long getScoreBehind() {
		return scoreBehind;
	}

	public void setScoreBehind(Long scoreBehind) {
		this.scoreBehind = scoreBehind;
	}

	public Long getScoreExceed() {
		return scoreExceed;
	}

	public void setScoreExceed(Long scoreExceed) {
		this.scoreExceed = scoreExceed;
	}

}
