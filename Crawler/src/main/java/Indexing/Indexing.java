package Indexing;

import org.apache.lucene.analysis.*;
import org.apache.lucene.analysis.standard.*;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.document.*;
import org.apache.lucene.index.FieldInfo.IndexOptions;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig.OpenMode;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.Term;
import org.apache.lucene.store.*;
import org.apache.lucene.util.Version;
import org.apache.tika.exception.TikaException;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.*;
import org.apache.tika.sax.*;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

import Crawler.Crawler.*;

import java.io.*;
import java.util.*;

public class Indexing {


	static final String indexPath = "C:/Crawler_Project/Index/";
	static final String FIELD_PATH = "path";
	static final String FIELD_CONTENTS = "contents";
	
	static Set<String> list = StopWords.getSmartStopWords();

	@SuppressWarnings("deprecation")
	public Indexing(String doc_dir) throws SAXException, TikaException	{
		
		boolean create = true;
		final File docDir = new File(doc_dir);
		if (!docDir.exists() || !docDir.canRead()) {
			System.out
					.println("Document directory does not exist, please check..");
			System.exit(1);
		}

		try {

			System.out.println("Start Indexing To Directory :" + indexPath);
			Directory dir = FSDirectory.open(new File(indexPath));
			Analyzer analyzer = new StandardAnalyzer(Version.LUCENE_35);
			IndexWriterConfig iwc = new IndexWriterConfig(Version.LUCENE_35,
					analyzer);

			if (create) {
				// creates a new index or overwrites an existing one
				iwc.setOpenMode(OpenMode.CREATE);
			} else {
				// Add new documents to an existing index:
				iwc.setOpenMode(OpenMode.CREATE_OR_APPEND);
			}
			
			//Set RAM utilization
			iwc.setRAMBufferSizeMB(1024.0);
			
			IndexWriter writer = new IndexWriter(dir, iwc);
			
			//Indexing Documents
			indexDocuments(writer, docDir);
			writer.optimize();
			writer.close();

			System.out.println("Indexing Completed Successfully");

		} catch (IOException e) {
			System.out.println(e+" :Error Occured");
		}		
	}
	
	@SuppressWarnings("resource")
	static void indexDocuments(IndexWriter writer, File file) throws IOException,
			SAXException, TikaException {
		if (file.canRead()) {
			if (file.isDirectory()) {
				String[] files = file.list();
				if (files != null) {
					for (int i = 0; i < files.length; i++) {
						indexDocuments(writer, new File(file, files[i]));
					}
				}
			} else {
				FileInputStream file_stream;
				try {
					file_stream = new FileInputStream(file);
				} catch (FileNotFoundException fnfe) {
					return;
				}
				try {

					int maxStringLength = 10 * 1024 * 1024;
					WriteOutContentHandler handler = new WriteOutContentHandler(
							maxStringLength);
					ContentHandler contenthandler = new BodyContentHandler(
							handler);
					Metadata metadata = new Metadata();
					Parser parser = new AutoDetectParser();
					parser.parse(file_stream, contenthandler, metadata,new ParseContext());
					String newString = contenthandler.toString()
							.replaceAll("/[^a-zA-Z 0-9]+/g", " ")
							.replaceAll("\\s+", " ").trim();

					Tokenizer tokenizer = new StandardTokenizer(
							Version.LUCENE_35, new StringReader(
									newString.toLowerCase()));
					final StandardFilter standardFilter = new StandardFilter(
							Version.LUCENE_35, tokenizer);
					final StopFilter stopFilter = new StopFilter(
							Version.LUCENE_35, standardFilter, list);
					final CharTermAttribute charTermAttribute = tokenizer
							.addAttribute(CharTermAttribute.class);
					stopFilter.reset();
					StringBuilder sb = new StringBuilder();

					while (stopFilter.incrementToken()) {
						final String token = charTermAttribute.toString()
								.toString();
						
						sb.append(token).append(System.getProperty("line.separator"));
					}

					Document doc = new Document();

					Field pathField = new Field("path", file.getPath(),	Field.Store.YES, Field.Index.NO);
					pathField.setIndexOptions(IndexOptions.DOCS_ONLY);
					doc.add(pathField);

					
					String document_url = file.getPath();
					String docName[] = document_url.split("\\\\");
					document_url = docName[docName.length - 1];

					// Add Document Name
					Field urlField = new Field("url", document_url, Field.Store.YES, Field.Index.NO);
					urlField.setIndexOptions(IndexOptions.DOCS_ONLY);
					doc.add(urlField);

					NumericField modifiedField = new NumericField("modified");
					modifiedField.setLongValue(file.lastModified());
					doc.add(modifiedField);

					doc.add(new Field("contents", new BufferedReader(new InputStreamReader(new ByteArrayInputStream(sb.toString().getBytes()), "UTF-8")),Field.TermVector.YES));

					if (writer.getConfig().getOpenMode() == OpenMode.CREATE) {
						writer.addDocument(doc);
					} else {
						System.out.println("Updating this File: " + file);
						writer.updateDocument(new Term("path", file.getPath()),
								doc);
					}
				} finally {
					file_stream.close();
				}
			}
		}
	}
	@SuppressWarnings("unused")
	public static void main(String[] args) throws SAXException, TikaException {
		Indexing index = new Indexing("C:/Crawler_Project/Data/");
	}
}