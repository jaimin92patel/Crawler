package Link_Analysis;

import java.io.*;
import java.util.*;
import javax.swing.text.MutableAttributeSet;
import javax.swing.text.html.HTML.Attribute;
import javax.swing.text.html.HTML.Tag;
import javax.swing.text.html.HTMLEditorKit.ParserCallback;
import javax.swing.text.html.parser.ParserDelegator;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.json.simple.parser.ParseException;
import Database.Database;
import Models.DocumentModel;

public class LinkAnalysis {

	List<DocumentModel> allDocs = new ArrayList<DocumentModel>();
	final String indexPath = "C:/Project/NewIndex/";
	
	Map<String, HashSet<String>> outLinks = new HashMap<String, HashSet<String>>();
	Map<String, HashSet<String>> inLinks = new HashMap<String, HashSet<String>>();
	int documentNumber;
	
	//Database connectivity
	Database transactionDB = new Database();

	@SuppressWarnings("unused")
	public static void main(String[] args) throws IOException, ParseException {
		LinkAnalysis LA = new LinkAnalysis();
	}

	public LinkAnalysis() throws IOException {
		
		getOutGoingLinks();
		getInComingLinks();
	
	}

	@SuppressWarnings("deprecation")
	private void getOutGoingLinks() throws IOException {

		Directory dir = FSDirectory.open(new File(indexPath));
		IndexReader ir = IndexReader.open(dir);
		
		FileReader stored_File;
		Set<String> set;
		DocumentModel doc_model;
		int totalDoc = ir.maxDoc();
		setDocumentNumber(ir.maxDoc());

		for (int docNum = 0; docNum < ir.numDocs(); docNum++) {

			String filePath = ir.document(docNum).getField("path").stringValue().toString();
			String documentUrl = ir.document(docNum).getField("url").stringValue().toString();

			//Extract Links from Files
			stored_File = new FileReader(filePath);
			List<String> links = extractLinks(stored_File);

			set = new HashSet<String>(links);
			doc_model = new DocumentModel();
			doc_model.setId("" + docNum + 1);
			doc_model.setPath(filePath);
			doc_model.setUrl(documentUrl);
			doc_model.setDefaultRank((double) 1 / totalDoc);
			doc_model.setOutGoingLink(set);

			//Store Outgoing links to outLinks Hashmap
			outLinks.put(documentUrl, (HashSet<String>) set);
			allDocs.add(doc_model);

		}
	}

	public void getInComingLinks() {
		Set<String> list;
		for (DocumentModel doc_model_1 : allDocs) {
			list = new HashSet<String>();
			for (DocumentModel dm2 : allDocs) {
				if (doc_model_1.getUrl() != dm2.getUrl())
					if (dm2.getOutGoingLink().contains(doc_model_1.getUrl()))
						list.add(dm2.getUrl());
			}
			inLinks.put(doc_model_1.getUrl(), (HashSet<String>) list);
			doc_model_1.setInComingLink(list);
	
		}
	}

	public List<String> extractLinks(Reader reader) throws IOException {
		
		//Extract Links using ParserDelegator
		final ArrayList<String> list = new ArrayList<String>();
		ParserDelegator parse_Del = new ParserDelegator();
		ParserCallback parserCallback = new ParserCallback() {
			public void handleText(final char[] data, final int pos) {}

			public void handleStartTag(Tag tag, MutableAttributeSet attribute, int pos) {
				if (tag == Tag.A) {
					String address = (String) attribute
							.getAttribute(Attribute.HREF);
					list.add(address);
				}
			}
			public void handleEndTag(Tag t, final int pos) {}
			public void handleSimpleTag(Tag t, MutableAttributeSet a, final int pos) {}
			public void handleComment(final char[] data, final int pos) {}
			public void handleError(final java.lang.String errMsg, final int pos) {}
		};
		parse_Del.parse(reader, parserCallback, true);
		return list;
	}

	public List<DocumentModel> getAllLinks() {
		return allDocs;
	}

	public Map<String, HashSet<String>> getOutLinks() {
		return outLinks;
	}

	public Map<String, HashSet<String>> getInLinks() {
		return inLinks;
	}

	public List<DocumentModel> getAllDocs() {
		return allDocs;
	}

	public int getDocumentNumber() {
		return documentNumber;
	}

	public void setDocumentNumber(int documentNumber) {
		this.documentNumber = documentNumber;
	}
}