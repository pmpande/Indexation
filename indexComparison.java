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
import org.apache.lucene.analysis.core.KeywordAnalyzer;
import org.apache.lucene.analysis.core.SimpleAnalyzer;
import org.apache.lucene.analysis.core.StopAnalyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.IndexWriterConfig.OpenMode;
import org.apache.lucene.index.MultiFields;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.Terms;
import org.apache.lucene.index.TermsEnum;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.BytesRef;

public class indexComparison {

	public static void main(String[] args) {
		
		Directory dir, dirK, dirS, dirSt;
		String pathToIndex = "./Results/Standard";
		String pathToIndexK = "./Results/Keyword";
		String pathToIndexS = "./Results/Simple";
		String pathToIndexSt = "./Results/Stop";
		
		File folder = new File("./corpus/corpus");
		File[] files = folder.listFiles(new FilenameFilter() {
		    public boolean accept(File folder, String fileName) {
		        return fileName.endsWith(".trectext");
		    }
		});
		
		try {
			dir = FSDirectory.open(Paths.get(pathToIndex));
			dirK = FSDirectory.open(Paths.get(pathToIndexK));
			dirS = FSDirectory.open(Paths.get(pathToIndexS));
			dirSt = FSDirectory.open(Paths.get(pathToIndexSt));
			Analyzer analyzer = new StandardAnalyzer();
			Analyzer analyzerKeyword = new KeywordAnalyzer();
			Analyzer analyzerSimple = new SimpleAnalyzer();
			Analyzer analyzerStop = new StopAnalyzer();
			
			IndexWriterConfig iwc = new IndexWriterConfig(analyzer);
			IndexWriterConfig iwcK = new IndexWriterConfig(analyzerKeyword);
			IndexWriterConfig iwcS = new IndexWriterConfig(analyzerSimple);
			IndexWriterConfig iwcSt = new IndexWriterConfig(analyzerStop);
			iwc.setOpenMode(OpenMode.CREATE);
			iwcK.setOpenMode(OpenMode.CREATE);
			iwcS.setOpenMode(OpenMode.CREATE);
			iwcSt.setOpenMode(OpenMode.CREATE);
			try {
				IndexWriter writer = new IndexWriter(dir, iwc);
				IndexWriter writerK = new IndexWriter(dirK, iwcK);
				IndexWriter writerS = new IndexWriter(dirS, iwcS);
				IndexWriter writerSt = new IndexWriter(dirSt, iwcSt);
				for(File file : files){
					System.out.println(file);
					BufferedReader fileReader = null;
					ArrayList<String> tags = new ArrayList<>();
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
					     
						        if(maps.get("TEXT") != null){
						        	String TEXT = maps.get("TEXT");
						        	luceneDoc.add(new TextField("TEXT", TEXT, Field.Store.YES));
						        }
						        writer.addDocument(luceneDoc);
						        writerK.addDocument(luceneDoc);
						        writerS.addDocument(luceneDoc);
						        writerSt.addDocument(luceneDoc);
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
				writerK.forceMerge(1);
				writerS.forceMerge(1);
				writerSt.forceMerge(1);

				writer.close();
				writerK.close();
				writerS.close();
				writerSt.close();

				dir.close();
				dirK.close();
				dirS.close();
				dirSt.close();
				
				IndexReader reader = DirectoryReader.open(FSDirectory.open(Paths.get(pathToIndex)));
				IndexReader readerK = DirectoryReader.open(FSDirectory.open(Paths.get(pathToIndexK)));
				IndexReader readerS = DirectoryReader.open(FSDirectory.open(Paths.get(pathToIndexS)));
				IndexReader readerSt = DirectoryReader.open(FSDirectory.open(Paths.get(pathToIndexSt)));
				
				//Print the total number of documents in the corpus
				System.out.println("Total number of documents in the corpus:"+reader.maxDoc());
				System.out.println("Total number of documents in the corpus:"+readerK.maxDoc());
				System.out.println("Total number of documents in the corpus:"+readerS.maxDoc());
				System.out.println("Total number of documents in the corpus:"+readerSt.maxDoc());
				
				Terms vocabulary = MultiFields.getTerms(reader, "TEXT");
				Terms vocabularyK = MultiFields.getTerms(readerK, "TEXT");
				Terms vocabularyS = MultiFields.getTerms(readerS, "TEXT");
				Terms vocabularySt = MultiFields.getTerms(readerSt, "TEXT");

				System.out.println("Number of terms in the dictionary with Standard Analyzer:"+vocabulary.size());
				System.out.println("Number of terms in the dictionary with Keyword Analyzer::"+vocabularyK.size());
				System.out.println("Number of terms in the dictionary with Simple Analyzer::"+vocabularyS.size());
				System.out.println("Number of terms in the dictionary with Stop Analyzer::"+vocabularySt.size());
				
				System.out.println("Number of tokens for this field with Standard Analyzer:"+vocabulary.getSumTotalTermFreq());
				System.out.println("Number of tokens for this field with Keyword Analyzer:"+vocabularyK.getSumTotalTermFreq());
				System.out.println("Number of tokens for this field with Simple Analyzer:"+vocabularyS.getSumTotalTermFreq());
				System.out.println("Number of tokens for this field with Stop Analyzer:"+vocabularySt.getSumTotalTermFreq());
				
				reader.close();
				readerK.close();
				readerS.close();
				readerSt.close();

			} catch (IOException e1) {
				e1.printStackTrace();
			}
		} catch (IOException e1) {
			e1.printStackTrace();
		}	
	}
}