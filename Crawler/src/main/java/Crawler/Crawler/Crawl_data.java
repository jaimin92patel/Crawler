package Crawler.Crawler;

//Reference: http://www.tutorialspoint.com/tika/tika_metadata_extraction.htm

import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

import org.apache.tika.exception.TikaException;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.parser.html.HtmlParser;
import org.apache.tika.sax.BodyContentHandler;
import org.apache.tika.sax.LinkContentHandler;
import org.apache.tika.sax.TeeContentHandler;
import org.apache.tika.sax.ToHTMLContentHandler;
import org.apache.tika.sax.WriteOutContentHandler;
import org.json.simple.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.jsoup.nodes.Element;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

import Database.Database;

class Crawler {

	private static String startURL;
	private static int depth;
	private Crawler_Depth crawlerDepth;
	private static boolean extraction = false;

	public Crawler(String[] args, String filedir) throws SAXException, TikaException {

		if (args.length != 4 && args.length != 5) {
			System.out.println("Invalid numbers of Argument passed. Please pass in this format: java -jar Crawler.jar -d <depth> -u <url> -e");
		} else {
			startURL = args[3];
			depth = Integer.parseInt(args[1]);
			if (args.length == 5)
				if (args[4].equals("-e"))
					extraction = true;
			crawlerDepth = new Crawler_Depth(filedir);
			
			//Add Data from Starting URL using depth
			crawlerDepth.search(startURL, depth, extraction);
		
		}
	}
}

class Crawler_Depth {

	//Created two Hashmap for crawling
	private Set<String> visitedPages = new HashSet<String>();
	private List<String> remainingPagesToVisit = new LinkedList<String>();
	
	static boolean extraction_mode = false;
	String fileDir;
	
	Crawler_Depth(String filedir)	{
		this.fileDir = filedir;
	}
	
	public void search(String url, int depth, boolean ext) throws SAXException, TikaException {
		extraction_mode = ext;

		while (this.visitedPages.size() < depth) {
			String currentUrl;
			CrawlerDocument crawlerDoc = new CrawlerDocument(fileDir);
			if (this.remainingPagesToVisit.isEmpty()) {
				currentUrl = url;
				this.visitedPages.add(url);
			} else
				currentUrl = this.nextUrl();

			crawlerDoc.crawlUrls(currentUrl, extraction_mode);
			this.remainingPagesToVisit.addAll(crawlerDoc.getLinks());
		}
	}

	private String nextUrl() {
		String nextUrl;
		
		do {
			nextUrl = this.remainingPagesToVisit.remove(0);
		} while (this.visitedPages.contains(nextUrl));
		
		this.visitedPages.add(nextUrl);
		return nextUrl;
	}
}

class CrawlerDocument {
	
	private List<String> links = new LinkedList<String>();
	private Document htmlDocument;
	static Database db = new Database();
	private static String folderPath;
	private String filePath;
	
	public CrawlerDocument(String filedir) {
		folderPath = filedir;
	}
	

	public void crawlUrls(String url, boolean extraction)
			throws SAXException, TikaException {
		try {
	
			Document htmlDocument = Jsoup.connect(url).timeout(0).get();
			this.htmlDocument = htmlDocument;
			String temp= extractMetaData(this.htmlDocument.toString());
			
			filePath = folderPath +"start_url"+ ".html";
			db.insertDB(url, filePath, temp);
			saveDocumentToFile(htmlDocument, filePath);

			//Select links from page
			Elements linksOnPage = this.htmlDocument.select("a[href]");

			for (Element link : linksOnPage) {
				String urlLink = link.absUrl("href");
				htmlDocument = Jsoup.connect(urlLink).timeout(0).get();
				String metadata = extractMetaData(htmlDocument.toString());
				String doc_arr[] = urlLink.split("/");
				filePath = folderPath + doc_arr[doc_arr.length - 1];
				db.insertDB(doc_arr[doc_arr.length - 1], filePath, metadata);
				saveDocumentToFile(htmlDocument, filePath);
				this.links.add(urlLink);
			}
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}
	}

	public List<String> getLinks() {
		return this.links;
	}

	@SuppressWarnings("unchecked")
	public String extractMetaData(String htmlText) throws SAXException,	TikaException {

		WriteOutContentHandler handler = new WriteOutContentHandler(10 * 1024 * 1024);
		
		Metadata metadata = new Metadata();
		InputStream inputstream = new ByteArrayInputStream(htmlText.getBytes());
		ParseContext pcontext = new ParseContext();
		ContentHandler textHandler = new BodyContentHandler(handler);
		ToHTMLContentHandler toHTMLHandler = new ToHTMLContentHandler();
		LinkContentHandler linkHandler = new LinkContentHandler();
		TeeContentHandler teeHandler = new TeeContentHandler(linkHandler, textHandler, toHTMLHandler);

		HtmlParser htmlparser = new HtmlParser();

		try {
			htmlparser.parse(inputstream, teeHandler, metadata, pcontext);
		} catch (IOException e) {
			e.printStackTrace();
		}

		String[] metadataNames = metadata.names();
		JSONObject json = new JSONObject();
		for (String name : metadataNames) {
			json.put(name.trim(), metadata.get(name).trim());
		}
		return json.toJSONString();
	}

	public boolean saveDocumentToFile(Document document, String filePath) {
		try {
			String documentString = document.html();
			FileWriter fileWriter = new FileWriter(filePath);
			BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
			bufferedWriter.write(documentString);
			bufferedWriter.close();
			fileWriter.close();
		} catch (IOException ex) {
			return false;
		}
		return true;
	}
}

public class Crawl_data {
	Crawl_data(String[] args, String filedir) throws SAXException, TikaException {
		deleteFiles(new File(filedir));
		new Crawler(args, filedir);
	}

	public static void deleteFiles(File file_to_delete) {
		try {
			if (file_to_delete.isDirectory()) {
				for (File f : file_to_delete.listFiles()) {
					if (f.isDirectory()) {
						deleteFiles(f);
					}
					f.delete();
				}
			}
		} catch (Exception e) {
			e.printStackTrace(System.err);
		}
	}
	
	public static void main(String[] args) throws SAXException, TikaException {
		System.out.println("Start Crawling...");
		Crawl_data cd = new Crawl_data(args, "C:/Crawler_Project/Data/");
		System.out.println("Data Crawling Done Successfully....");
	}
}