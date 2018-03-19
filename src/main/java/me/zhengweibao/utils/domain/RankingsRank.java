package me.zhengweibao.utils.domain;

public class RankingsRank {

	private String rankingsName;

	private String identityId;

	private Double score;

	private Long rank;

	private String relatedId;

	private boolean deleted = false;

	public RankingsRank() {

	}

	public RankingsRank(String rankingsName, String identityId, String relatedId) {
		this.rankingsName = rankingsName;
		this.identityId = identityId;
		this.relatedId = relatedId;
	}

	public String getRankingsName() {
		return rankingsName;
	}

	public void setRankingsName(String rankingsName) {
		this.rankingsName = rankingsName;
	}

	public String getIdentityId() {
		return identityId;
	}

	public void setIdentityId(String identityId) {
		this.identityId = identityId;
	}

	public Double getScore() {
		return score;
	}

	public void setScore(Double score) {
		this.score = score;
	}

	public Long getRank() {
		return rank;
	}

	public void setRank(Long rank) {
		this.rank = rank;
	}

	public String getRelatedId() {
		return relatedId;
	}

	public void setRelatedId(String relatedId) {
		this.relatedId = relatedId;
	}

	public boolean isDeleted() {
		return deleted;
	}

	public void setDeleted(boolean deleted) {
		this.deleted = deleted;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((identityId == null) ? 0 : identityId.hashCode());
		result = prime * result + ((rankingsName == null) ? 0 : rankingsName.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		RankingsRank other = (RankingsRank) obj;
		if (identityId == null) {
			if (other.identityId != null)
				return false;
		} else if (!identityId.equals(other.identityId))
			return false;
		if (rankingsName == null) {
			if (other.rankingsName != null)
				return false;
		} else if (!rankingsName.equals(other.rankingsName))
			return false;
		return true;
	}

}
