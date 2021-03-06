import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.MultiFields;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.Terms;
import org.apache.lucene.index.TermsEnum;
import org.apache.lucene.index.IndexWriterConfig.OpenMode;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.BytesRef;

public class generateIndex {

	public static void main(String[] args) {
		
		Directory dir;
		String pathToIndex = "./Results/Part1";
		File folder = new File("./corpus/corpus");
		File[] files = folder.listFiles(new FilenameFilter() {
		    public boolean accept(File folder, String fileName) {
		        return fileName.endsWith(".trectext");
		    }
		});
		
		try {
			dir = FSDirectory.open(Paths.get(pathToIndex));
			Analyzer analyzer = new StandardAnalyzer();
						
			IndexWriterConfig iwc = new IndexWriterConfig(analyzer);
			iwc.setOpenMode(OpenMode.CREATE);
			try {
				IndexWriter writer = new IndexWriter(dir, iwc);
				for(File file : files){
					System.out.println(file);
					BufferedReader fileReader = null;
					ArrayList<String> tags = new ArrayList<>();
					tags.add("DOCNO");
					tags.add("HEAD");
					tags.add("BYLINE");
					tags.add("DATELINE");
					tags.add("TEXT");
					try {
					    fileReader = new BufferedReader(new FileReader(file));
					    String lineText = null;
					    while ((lineText = fileReader.readLine()) != null) {
					        if(lineText.contains("<DOC>")){
					        	
					        	Document luceneDoc = new Document();
					        	HashMap<String, String> maps = new HashMap<>();
					        	while (!(lineText =fileReader.readLine()).contains("</DOC>")){
					        		String startTag = "";
					        		if(!lineText.contains("<")){
					        			continue;
					        		}
					        		startTag = lineText.substring(lineText.indexOf("<") + 1, lineText.indexOf(">"));
					        		if(tags.contains(startTag)){
					        			String value = "", tempText = "";
					        			String[] splitText = lineText.split("<"+startTag+">");
					        			if(splitText.length > 1 ){
					        				tempText = splitText[1];
					        			}
					        			while(!tempText.contains("</"+startTag+">")){
					        				value += tempText;
					        				lineText = fileReader.readLine(); 
					        				tempText = lineText;
					        			}
					        			splitText = tempText.split("</"+startTag+">");
					        			if(splitText.length == 1 ){
					        				value += splitText[0].trim();
					        			}
				        				
				        				String newValue = null;
				        				if(maps.get(startTag) == null){
				        					newValue = value;
				        				}else{
				        					String existingValue = maps.get(startTag);
				        					newValue = existingValue + " " + value;
				        				}
				        				if(newValue.equalsIgnoreCase("AP890118-0168")){
				        					System.out.println("");
				        				}
				        				maps.put(startTag, newValue);
					        		}
					        	}
					        	
					        	if(maps.get("DOCNO") != null){
					        		String DOCNO = maps.get("DOCNO");
					        		luceneDoc.add(new StringField("DOCNO", DOCNO, Field.Store.YES));
					        	}
					        	if(maps.get("HEAD")!= null){
					        		String HEAD = maps.get("HEAD");
					        		luceneDoc.add(new TextField("HEAD", HEAD, Field.Store.YES));
					        	}
					        	if(maps.get("BYLINE") != null){
					        		String BYLINE = maps.get("BYLINE");
					        		luceneDoc.add(new TextField("BYLINE", BYLINE, Field.Store.YES));
					        	}
					        	if(maps.get("DATELINE") != null){
					        		String DATALINE = maps.get("DATELINE");
					        		luceneDoc.add(new TextField("DATELINE", DATALINE, Field.Store.YES));
					        	}
						        if(maps.get("TEXT") != null){
						        	String TEXT = maps.get("TEXT");
						        	luceneDoc.add(new TextField("TEXT", TEXT, Field.Store.YES));
						        }
						        writer.addDocument(luceneDoc);
					        }
					    }
					} catch (FileNotFoundException e) {
					    e.printStackTrace();
					} catch (IOException e) {
					    e.printStackTrace();
					} finally {
					    try {
					        if (fileReader != null) {
					        	fileReader.close();
					        }
					    } catch (IOException e) {}
					}
				}
				writer.forceMerge(1);
				writer.close();
				
				IndexReader reader = DirectoryReader.open(FSDirectory.open(Paths.get(pathToIndex)));
				
				//Print the total number of documents in the corpus
				System.out.println("Total number of documents in the corpus:"+reader.maxDoc());
				
				reader.close();
			} catch (IOException e1) {
				e1.printStackTrace();
			}
		} catch (IOException e1) {
			e1.printStackTrace();
		}	
	}
}
 
