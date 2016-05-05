package Link_Analysis;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import Database.Database;
import Models.DocumentModel;

public class PageRank {

	LinkAnalysis linkAnalysis;
	List<DocumentModel> allDocs;
	int totalDocument;
	Map<String, HashSet<String>> outLinks;
	Map<String, HashSet<String>> inLinks;
	Database db;
	
	public PageRank() throws IOException {
		linkAnalysis = new LinkAnalysis();
		totalDocument = linkAnalysis.getDocumentNumber();
		allDocs = linkAnalysis.getAllDocs();
		outLinks = linkAnalysis.getOutLinks();
		inLinks = linkAnalysis.getInLinks();
		db = new Database(false);
		this.startRanking();
	}

	public static void main(String[] args) throws IOException {
		new PageRank();
	}

	public void startRanking() {

		Map<String, Double> defaultRankMap = new HashMap<String, Double>();
		Double defaultRank = (double) (1 / (float) totalDocument);
		Double d = 0.85;
		
		for (DocumentModel dm : allDocs)
			defaultRankMap.put(dm.getUrl(), defaultRank);
		
		for (int i = 0; i < 3; i++) {
			for (DocumentModel dm : allDocs) {
				Double sum = 0.0;
				Double rank = 0.0;
				if (dm.getInComingLink() != null) {
					for (String in : dm.getInComingLink()) {
						rank = (double)( defaultRankMap.get(in) / (float)outLinks.get(in).size());
						sum = sum + rank;
					}
					rank = ((1 - d) + d * sum);
				} else {
					rank = rank + defaultRankMap.get(dm.getUrl());
				}
				defaultRankMap.replace(dm.getUrl(), rank);
			}
		}

		for(DocumentModel dm : allDocs)
			dm.setFinalRank(defaultRankMap.get(dm.getUrl()));
		
		for (DocumentModel dm : allDocs) {
			dm.round();
			dm.createJSON();
			db.insertRank(dm.getData());
		}
	}
}