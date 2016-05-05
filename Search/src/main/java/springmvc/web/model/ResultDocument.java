package springmvc.web.model;

public class ResultDocument {

	String url;
	String path;
	Double tfidf;
	Double tfidfNorm;
	Double pageRank;
	Double pageRankNorm;
	Double totalScore;
	String title;
	String description;
	
	public ResultDocument() {
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}
	
	public Double getTfidfNorm() {
		return tfidfNorm;
	}

	public void setTfidfNorm(Double tfidfNorm) {
		this.tfidfNorm = tfidfNorm;
	}

	public Double getPageRankNorm() {
		return pageRankNorm;
	}

	public void setPageRankNorm(Double pageRankNorm) {
		this.pageRankNorm = pageRankNorm;
	}

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public Double getTfidf() {
		return tfidf;
	}

	public void setTfidf(Double tfidf) {
		this.tfidf = tfidf;
	}

	public Double getPageRank() {
		return pageRank;
	}

	public void setPageRank(Double pageRank) {
		this.pageRank = pageRank;
	}

	public Double getTotalScore() {
		return totalScore;
	}

	public void setTotalScore(Double totalScore) {
		this.totalScore = totalScore;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}	
}