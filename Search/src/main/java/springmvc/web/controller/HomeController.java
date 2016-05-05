package springmvc.web.controller;

import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.*;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.*;
import org.tartarus.snowball.ext.PorterStemmer;

import springmvc.web.model.Database;
import springmvc.web.model.ResultDocument;

@Controller
public class HomeController {

	@RequestMapping(value = ("/home.html"), method = RequestMethod.GET)
	public String index() {
		return "home";
	}

	@RequestMapping(value = ("/google.html"), method = RequestMethod.GET)
	public String google() {
		return "google";
	}
	
	@RequestMapping(value = "/search.html", method = RequestMethod.GET)
	public String searchTerm(@RequestParam String term, ModelMap models)
			throws IOException {

		
		PorterStemmer PorterStemmer = new PorterStemmer();
		Database db1 = new Database(true);
		Database db2 = new Database(false);
		if (!term.toLowerCase().contains("or")
				&& !term.toLowerCase().contains("and")) {

			System.out.println("Single Term Searching");

			PorterStemmer.setCurrent(term.toLowerCase());
			PorterStemmer.stem();
			term = PorterStemmer.getCurrent();

			Map<String, Double> tfidf = db1.getTFIDFFromDB(term);
			List<ResultDocument> list = null;

			if (tfidf != null) {

				Map<String, Double> rankNormMap = new HashMap<String, Double>();
				Map<String, Double> rankMap = new HashMap<String, Double>();
				Map<String, Double> tfidfNormMap = new HashMap<String, Double>();

				for (String s : tfidf.keySet()) {
					Double rank = Double.parseDouble(db2.getPageRank(s)
							.get("rank").toString());
					rankMap.put(s, rank);
				}

				double maxTfidfValue = Collections.max(tfidf.values());
				double maxRankValue = Collections.max(rankMap.values());
				ResultDocument rd;
				list = new ArrayList<ResultDocument>();

				for (String s : tfidf.keySet()) {
					rd = new ResultDocument();
					tfidfNormMap.put(s, (tfidf.get(s) / maxTfidfValue) * 0.1);
					rankNormMap.put(s, (rankMap.get(s) / maxRankValue) * 0.9);

					rd.setPageRank(Double.parseDouble(new DecimalFormat(
							"##.###").format(rankMap.get(s))));
					rd.setPageRankNorm(Double.parseDouble(new DecimalFormat(
							"##.###").format(rankNormMap.get(s))));
					rd.setTfidf(Double.parseDouble(new DecimalFormat("##.###")
							.format(tfidf.get(s))));
					rd.setTfidfNorm(Double.parseDouble(new DecimalFormat(
							"##.###").format(tfidfNormMap.get(s))));
					rd.setTotalScore(Double.parseDouble(new DecimalFormat(
							"##.###").format(tfidfNormMap.get(s)
							+ rankNormMap.get(s))));
					rd.setUrl(s);
					String filePath = "C:/Project/NewData/"
							+ s;

					rd.setPath(filePath);
					File storageFile = new File(filePath);
					Document document = Jsoup.parse(storageFile, "ISO-8859-1");
					rd.setTitle(document.title());

					list.add(rd);
				}

				Collections.sort(list, new Comparator<ResultDocument>() {
					@Override
					public int compare(ResultDocument r1, ResultDocument r2) {
						if (r1.getTotalScore() < r2.getTotalScore())
							return 1;
						if (r1.getTotalScore() > r2.getTotalScore())
							return -1;
						return 0;
					}
				});

					models.put("result", list);
			
			} else
				models.put("result", list);

		} else if (term.toLowerCase().contains("or")
				&& !term.toLowerCase().contains("and")) {

			String term_arr[] = term.split(" ");
			String find_term[] = new String[term_arr.length - 1];
			int j = 0;
			for (int i = 0; i < term_arr.length; i++) {
				if (!term_arr[i].toLowerCase().equals("or")) {
					PorterStemmer.setCurrent(term_arr[i]);
					PorterStemmer.stem();
					find_term[j++] = PorterStemmer.getCurrent();
				}
			}

			Map<String, Double> resultMap1 = db1.getTFIDFFromDB(find_term[0]);
			Map<String, Double> resultMap2 = db1.getTFIDFFromDB(find_term[1]);

			Double max = 0.0;

			if (resultMap1 != null || resultMap2 != null) {

				ResultDocument rd;
				List<ResultDocument> list = new ArrayList<ResultDocument>();
				double maxValueInMap1 = 0.0;
				double maxValueInMap2 = 0.0;

				if (resultMap1 != null && resultMap1.size() != 0) {
					for (String str : resultMap1.keySet()) {
						rd = new ResultDocument();
						rd.setUrl(str);
						rd.setTfidf(resultMap1.get(str));
						list.add(rd);
					}
					maxValueInMap1 = Collections.max(resultMap1.values());
				}

				if (resultMap2 != null && resultMap2.size() != 0) {
					for (String str : resultMap2.keySet()) {
						if (resultMap1 != null) {
							if (!resultMap1.containsKey(str)) {
								rd = new ResultDocument();
								rd.setUrl(str);
								rd.setTfidf(resultMap2.get(str));
								list.add(rd);
							}
						} else {
							rd = new ResultDocument();
							rd.setUrl(str);
							rd.setTfidf(resultMap2.get(str));
							list.add(rd);
						}
					}
					maxValueInMap2 = Collections.max(resultMap2.values());
				}

				if (maxValueInMap1 < maxValueInMap2)
					max = maxValueInMap2;
				else
					max = maxValueInMap1;

				Map<String, Double> tfidfNorm1 = null;
				Map<String, Double> tfidfNorm2 = null;

				// Normalize tfidf values
				if (resultMap1 != null) {
					tfidfNorm1 = new HashMap<String, Double>();
					for (String str : resultMap1.keySet())
						tfidfNorm1.put(str,
								(double) (resultMap1.get(str) / max) * 0.6);
				}

				if (resultMap2 != null) {
					tfidfNorm2 = new HashMap<String, Double>();
					for (String str : resultMap2.keySet())
						tfidfNorm2.put(str,
								(double) (resultMap2.get(str) / max) * 0.6);
				}

				Map<String, Double> rankMap1 = null;
				Map<String, Double> rankMap2 = null;

				double maxpagerank1 = 0.0;
				double maxpagerank2 = 0.0;
				double maxPageRank = 0.0;

				// get page rank of all document
				if (resultMap1 != null) {
					rankMap1 = new HashMap<String, Double>();
					for (String str : resultMap1.keySet()) {
						rankMap1.put(
								str,
								Double.parseDouble(db2.getPageRank(str)
										.get("rank").toString()));
					}
					maxpagerank1 = Collections.max(rankMap1.values());
				}

				if (resultMap2 != null) {
					rankMap2 = new HashMap<String, Double>();
					for (String str : resultMap2.keySet()) {
						rankMap2.put(
								str,
								Double.parseDouble(db2.getPageRank(str)
										.get("rank").toString()));
					}
					maxpagerank2 = Collections.max(rankMap2.values());
				}

				if (maxpagerank1 < maxpagerank2)
					maxPageRank = maxpagerank2;
				else
					maxPageRank = maxpagerank1;

				// Normalize pagerank values
				Map<String, Double> rankNorm1 = null;
				Map<String, Double> rankNorm2 = null;

				if (rankMap1 != null) {
					rankNorm1 = new HashMap<String, Double>();
					for (String str : rankMap1.keySet()) {
						rankNorm1
								.put(str,
										(double) (rankMap1.get(str) / maxPageRank) * 0.4);
					}
				}

				if (rankMap2 != null) {
					rankNorm2 = new HashMap<String, Double>();
					for (String str : rankMap2.keySet()) {
						rankNorm2
								.put(str,
										(double) (rankMap2.get(str) / maxPageRank) * 0.4);
					}
				}

				Map<String, Double> totalScore = new HashMap<String, Double>();

				if (resultMap1 != null) {
					for (String str : resultMap1.keySet())
						totalScore.put(str,
								tfidfNorm1.get(str) + rankNorm1.get(str));
				}

				if (resultMap2 != null) {
					for (String str : resultMap2.keySet())
						totalScore.put(str,
								tfidfNorm2.get(str) + rankNorm2.get(str));
				}

				List<ResultDocument> newResultList = new ArrayList<ResultDocument>();
				for (ResultDocument doc : list) {

					if (rankMap1 != null
							&& tfidfNorm1.containsKey(doc.getUrl()))
						doc.setTfidfNorm(Double.parseDouble(new DecimalFormat(
								"##.###").format(tfidfNorm1.get(doc.getUrl()))));
					else
						doc.setTfidfNorm(Double.parseDouble(new DecimalFormat(
								"##.###").format(tfidfNorm2.get(doc.getUrl()))));

					if (rankMap1 != null && rankMap1.containsKey(doc.getUrl()))
						doc.setPageRank(Double.parseDouble(new DecimalFormat(
								"##.###").format(rankMap1.get(doc.getUrl()))));
					else
						doc.setPageRank(Double.parseDouble(new DecimalFormat(
								"##.###").format(rankMap2.get(doc.getUrl()))));

					if (rankMap1 != null && rankNorm1.containsKey(doc.getUrl()))
						doc.setPageRankNorm(Double
								.parseDouble(new DecimalFormat("##.###")
										.format(rankNorm1.get(doc.getUrl()))));
					else
						doc.setPageRankNorm(Double
								.parseDouble(new DecimalFormat("##.###")
										.format(rankNorm2.get(doc.getUrl()))));

					doc.setTotalScore(Double.parseDouble(new DecimalFormat(
							"##.###").format(totalScore.get(doc.getUrl()))));
					String filePath = "C:/Project/NewData/"
							+ doc.getUrl();

					doc.setPath(filePath);
					File storageFile = new File(filePath);
					Document document = Jsoup.parse(storageFile, "ISO-8859-1");
					doc.setTitle(document.title());

					newResultList.add(doc);
				}

				Collections.sort(newResultList,
						new Comparator<ResultDocument>() {
							@Override
							public int compare(ResultDocument r1,
									ResultDocument r2) {
								if (r1.getTotalScore() < r2.getTotalScore())
									return 1;
								if (r1.getTotalScore() > r2.getTotalScore())
									return -1;
								return 0;
							}
						});

				if (newResultList.size() > 5) {
					List<ResultDocument> newList = new ArrayList<ResultDocument>();
					for (int i = 0; i < 5; i++)
						newList.add(newResultList.get(i));
					models.put("result", newList);
				} else
					models.put("result", newResultList);

			
			}

		} else if (!term.toLowerCase().contains("or")
				&& term.toLowerCase().contains("and")) {

			String term_arr[] = term.split(" ");
			String find_term[] = new String[term_arr.length - 1];
			int j = 0;
			for (int i = 0; i < term_arr.length; i++) {
				if (!term_arr[i].toLowerCase().equals("and")) {
					PorterStemmer.setCurrent(term_arr[i]);
					PorterStemmer.stem();
					find_term[j++] = PorterStemmer.getCurrent();
				}
			}

			Map<String, Double> resultMap1 = db1.getTFIDFFromDB(find_term[0]);
			Map<String, Double> resultMap2 = db1.getTFIDFFromDB(find_term[1]);
			List<ResultDocument> list = null;
			ResultDocument rd;

			if (resultMap1 != null && resultMap2 != null
					&& resultMap1.size() != 0 && resultMap2.size() != 0
					&& !resultMap1.isEmpty() && !resultMap2.isEmpty()) {
				Map<String, Double> matchingRecord = new HashMap<String, Double>();
				Map<String, Double> pageRank = new HashMap<String, Double>();

				for (String s : resultMap1.keySet()) {
					if (resultMap2.containsKey(s)) {
						matchingRecord.put(s,resultMap1.get(s) + resultMap2.get(s));
						pageRank.put(s, Double.parseDouble(db2.getPageRank(s).get("rank").toString()));
					}
				}

				if (matchingRecord.size() != 0) {

					list = new ArrayList<ResultDocument>();

					double maxptfidf = Collections.max(matchingRecord.values());
					double maxpageRank = Collections.max(pageRank.values());

					for (String s : matchingRecord.keySet()) {
						rd = new ResultDocument();

						rd.setUrl(s);
						rd.setTfidf(Double.parseDouble(new DecimalFormat(
								"##.###").format(matchingRecord.get(s))));
						rd.setTfidfNorm(Double.parseDouble(new DecimalFormat(
								"##.###").format((matchingRecord.get(s) / maxptfidf) * 0.6)));
						rd.setPageRank(Double.parseDouble(new DecimalFormat(
								"##.###").format(pageRank.get(s))));
						rd.setPageRankNorm(Double.parseDouble(new DecimalFormat(
								"##.###").format((pageRank.get(s) / maxpageRank) * 0.4)));
						rd.setTotalScore(Double.parseDouble(new DecimalFormat(
								"##.###").format(rd.getTfidfNorm()
								+ rd.getPageRankNorm())));
						String filePath = "C:/Project/NewData/"
								+ s;
						rd.setPath(filePath);
						File storageFile = new File(filePath);
						Document document = Jsoup.parse(storageFile,
								"ISO-8859-1");
						rd.setTitle(document.title());

						list.add(rd);
					}

					Collections.sort(list, new Comparator<ResultDocument>() {
						@Override
						public int compare(ResultDocument r1, ResultDocument r2) {
							if (r1.getTotalScore() < r2.getTotalScore())
								return 1;
							if (r1.getTotalScore() > r2.getTotalScore())
								return -1;
							return 0;
						}
					});

					if (list.size() > 5) {
						List<ResultDocument> newList = new ArrayList<ResultDocument>();
						for (int i = 0; i < 5; i++)
							newList.add(list.get(i));
						models.put("result", newList);
					} else
					{
						models.put("result", list);
					}
						
				} else
					models.put("result", list);
			} 
		}
		return "result";
	}
}