package TFIDF;


import java.io.*;
import java.util.*;
import org.apache.lucene.index.*;
import org.apache.lucene.store.*;
import org.json.simple.*;

import Database.Database;

public class TFIDF_Calculation {
	
	//TF calculation
	Map<String, HashMap<String, Integer>> tfMap = new HashMap<String, HashMap<String, Integer>>();
	
	//TF IDF calculation
	Map<String, HashMap<String, Double>> tfidfMap = new HashMap<String, HashMap<String, Double>>();
	
	final static String indexPath = "C:\\Project\\NewIndex\\";
	
	Database db = new Database(true);
	Database transactionDB = new Database();
	
	public static void main(String[] args) throws Exception {	
		TFIDF_Calculation tfidf = new TFIDF_Calculation();
		tfidf.calculateTFIDF(indexPath);
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public Map<String, Double> sortByValues(Map<String, Double> map) {
		List list = new LinkedList(map.entrySet());
		Collections.sort(list, new Comparator() {
			public int compare(Object o1, Object o2) {
				return ((Comparable) ((Map.Entry) (o2)).getValue())
						.compareTo(((Map.Entry) (o1)).getValue());
			}
		});
		HashMap sortedHashMap = new LinkedHashMap();
		for (Iterator it = list.iterator(); it.hasNext();) {
			Map.Entry entry = (Map.Entry) it.next();
			sortedHashMap.put(entry.getKey(), entry.getValue());
		}
		return sortedHashMap;
	}

	@SuppressWarnings({ "deprecation", "unchecked" })
	public void calculateTFIDF(String indexPath) throws IOException {
		
		Directory dir = FSDirectory.open(new File(indexPath));
		IndexReader ir = IndexReader.open(dir);

		int N_Docs = ir.maxDoc();
		
		System.out.println("TF_IDF Calculation Starts....");
		
		for (int docNum = 0; docNum < N_Docs; docNum++) {
			
			TermFreqVector tfv = ir.getTermFreqVector(docNum, "contents");
			if (tfv == null || tfv.size() == 0) {
				continue;
			}
			
			//Terms
			String terms[] = tfv.getTerms();
			int termCount = terms.length;
			
			//Frequencies
			int freqs[] = tfv.getTermFrequencies();
			String url = ir.document(docNum).getField("url").stringValue().toString();

			for (int t = 0; t < termCount; t++) {
				
				//Check if it contains terms
				if (!tfMap.containsKey("" + terms[t])) {
					Map<String, Integer> map = new HashMap<String, Integer>();
					map.put(url, freqs[t]);
					tfMap.put("" + terms[t], (HashMap<String, Integer>) map);
				} else {
					Map<String, Integer> map = tfMap.get("" + terms[t]);
					map.put(url, freqs[t]);
					tfMap.put("" + terms[t], (HashMap<String, Integer>) map);
				}
			}
		}
		
		Map<String, Integer> temp_Map;
		JSONArray jsonArray;
		JSONObject jsonObject;

		for (String key : tfMap.keySet()) {

			if(!key.isEmpty() && key.length() != 0 && !key.trim().equals("") )		{

				//IDF Calculation
				Double idf = (double) Math.log10((double) N_Docs / tfMap.get(key).size());
				temp_Map = (HashMap<String, Integer>) tfMap.get(key);
				jsonArray = new JSONArray();
				
				if(key.contains("."))
					key = key.replaceAll(".", "");
				
				//Store TFIDF into Database
				for (String str : temp_Map.keySet())	{
						jsonObject = new JSONObject();
						jsonObject.put("tfidf", temp_Map.get(str) * idf);
						jsonObject.put("url",str);
						jsonArray.add(jsonObject);
				}
				db.insertScore(key, jsonArray);
			}
		}
		System.out.println("TF_IDF Calculation Done....");
	}
	
	public Map<String, Double> getTFIDFValueByTerm(String term) {
		return db.getTFIDFFromDB(term);
	}
}