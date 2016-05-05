package Models;


import java.text.DecimalFormat;
import java.util.*;
import com.mongodb.BasicDBObject;

public class DocumentModel {

	private String id;
	private String path;
	private Set<String> outGoingLink;
	private Set<String> inComingLink;
	private Double defaultRank;
	private double newRank;
	private String url;
	private double finalRank;
	private String title;
	private String description;
	BasicDBObject data;

	public Double getDefaultRank() {
		return defaultRank;
	}

	public void setDefaultRank(Double defaultRank) {
		this.defaultRank = defaultRank;
	}

	public DocumentModel() {
		super();
		this.outGoingLink = new HashSet<String>();
		this.data = new BasicDBObject();
		this.inComingLink = new HashSet<String>();
	}

	
	public void round(){
		this.finalRank = Double.parseDouble(new DecimalFormat("##.####").format(this.finalRank));
	}
	
	public void createJSON(){
		this.data.put("url", this.url);
		this.data.put("path", this.path.replaceAll("/", "\\\\"));
		this.data.put("rank",this.finalRank);
	}
	
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public Set<String> getOutGoingLink() {
		return outGoingLink;
	}

	public void setOutGoingLink(Set<String> outGoingLink) {
		this.outGoingLink = outGoingLink;
	}

	public Set<String> getInComingLink() {
		return inComingLink;
	}

	public void setInComingLink(Set<String> inComingLink) {
		this.inComingLink = inComingLink;
	}

	public double getRank() {
		return defaultRank;
	}

	public void setRank(double rank) {
		this.defaultRank = rank;
	}

	public double getNewRank() {
		return newRank;
	}

	public void setNewRank(double newRank) {
		this.newRank = newRank;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public double getFinalRank() {
		return finalRank;
	}

	public void setFinalRank(double finalRank) {
		this.finalRank = finalRank;
	}

	public BasicDBObject getData() {
		return data;
	}

	public void setData(BasicDBObject data) {
		this.data = data;
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