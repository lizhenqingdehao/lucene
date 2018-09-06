package cn.itcast.lucene;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.cn.smart.SmartChineseAnalyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;
import org.junit.Test;
import org.wltea.analyzer.lucene.IKAnalyzer;

public class IndexManager {

	@Test
	public void addIndex() throws IOException {
		// 1.指定索引库目录
		FSDirectory directory = FSDirectory.open(new File("d:\\index"));
		// 2.指定分词器改成中文分词器
		// Analyzer analyzer = new StandardAnalyzer();
		// SmartChineseAnalyzer analyzer = new SmartChineseAnalyzer();
		// 换成ik中文分词器
		IKAnalyzer analyzer = new IKAnalyzer();

		// 3.创建配置对象(1.版本号 2.分词器对象)
		IndexWriterConfig config = new IndexWriterConfig(Version.LATEST, analyzer);
		// 4.创建写入索引对象
		IndexWriter indexWriter = new IndexWriter(directory, config);
		// 5.写入对象（通过File获取到文件路径，file.listFiles循环所有文件）
		File files = new File("d:\\searchsource");
		File[] listFiles = files.listFiles();
		for (File file : listFiles) {
			Document doc = new Document();
			// 获取文件名称保存到域()
			TextField fileNameField = new TextField("name", file.getName(), Store.YES);
			doc.add(fileNameField);
			// 文件路径
			TextField filePathField = new TextField("path", file.getPath(), Store.YES);
			doc.add(filePathField);
			// 文件大小
			long sizeOf = FileUtils.sizeOf(file);
			TextField fileSizeFiled = new TextField("size", sizeOf + "", Store.YES);
			doc.add(fileSizeFiled);
			// 文件内容
			String fileContent = FileUtils.readFileToString(file);
			TextField fileContentField = new TextField("content", fileContent, Store.YES);
			doc.add(fileContentField);

			indexWriter.addDocument(doc);
		}
		// 6.关闭IndexWriter对象
		indexWriter.close();
	}

	@Test
	public void searchIndex() throws IOException {
		// 1.执行索引库的目录
		FSDirectory directory = FSDirectory.open(new File("d:\\index"));
		// 2.DirectoryReader获取读取索引对象
		DirectoryReader indexReader = DirectoryReader.open(directory);
		// 3.创建搜索索引的对象
		IndexSearcher indexSearcher = new IndexSearcher(indexReader);
		// 4.执行查询,name换成content查找内容的
		TermQuery query = new TermQuery(new Term("name", "传智播客"));
		TopDocs topDocs = indexSearcher.search(query, 50);
		System.out.println("总记录条数" + topDocs.totalHits);
		ScoreDoc[] scoreDocs = topDocs.scoreDocs;

		for (ScoreDoc scoreDoc : scoreDocs) {
			int docId = scoreDoc.doc; // 能够获取到文档的ID值
			Document doc = indexSearcher.doc(docId); // 根据id值查询出文档对象
			System.out.println("name=" + doc.get("name"));
			System.out.println("size=" + doc.get("size"));
			System.out.println("path=" + doc.get("path"));
			System.out.println("content=" + doc.get("content"));
		}

		// 5.关闭资源
		indexReader.close();
	}

	@Test
	public void deleteIndex() throws IOException {
		FSDirectory directory = FSDirectory.open(new File("d:\\index"));
		IKAnalyzer analyzer = new IKAnalyzer();
		IndexWriterConfig config = new IndexWriterConfig(Version.LATEST, analyzer);
		IndexWriter indexWriter = new IndexWriter(directory, config);
		//indexWriter.deleteDocuments(new Term("name","传智播客"));
		indexWriter.deleteAll(); //全部删除
		indexWriter.commit();
		indexWriter.close();
	}
}
