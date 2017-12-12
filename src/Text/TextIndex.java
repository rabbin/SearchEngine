package Text;

//import org.apache.lucene.analysis.standard.StandardAnalyzer;

import org.apache.lucene.analysis.cn.smart.SmartChineseAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
//import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.*;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.search.highlight.Formatter;
import org.apache.lucene.search.highlight.Highlighter;
import org.apache.lucene.search.highlight.QueryScorer;
import org.apache.lucene.search.highlight.Scorer;
import org.apache.lucene.search.highlight.SimpleHTMLFormatter;

//import java.util.ArrayList;
import java.util.LinkedList;
import java.io.*;
import java.nio.file.Paths;



public class TextIndex {

    /**
     *
     * @param args
     * @throws Exception
     */
    public static void main(String[] args)throws Exception{


        TextIndex textIndex = new TextIndex();

        File fileSet[] = new File(IndexDir).listFiles();
        if (fileSet.length == 0) {
            System.out.println("##########The IndexDir is empty! Begin to create index files!");
            textIndex.createIndex(urlDir, IndexDir);
        }


        BufferedReader input = new BufferedReader(new InputStreamReader(System.in));
        System.out.print("Please input the keyword you want to search:");
        String keyword = input.readLine();
        textIndex.search(keyword);
    }

    /**************************************************/

    static final String IndexDir = "C:\\Users\\rabbin\\Desktop\\File\\IndexDir";
    static final String urlDir = "C:\\Users\\rabbin\\Desktop\\File\\zolMobile";
    static final int MAXDOCNUM = 100;

    //StandardAnalyzer analyzer = new StandardAnalyzer();
    static final SmartChineseAnalyzer analyzer = new SmartChineseAnalyzer();
    static final String preTag= "<font color='red'>";
    static final String postTag= "</font>";

    public LinkedList<String> links = new LinkedList<String>();
    public LinkedList<String> titles = new LinkedList<String>();
    public LinkedList<String> descriptions = new LinkedList<String>();


    /**************************************************/

    public void search(String keywords) throws Exception {


        System.out.println("##########begin to search");
        System.out.println("open the index files!");
        Directory directory = openIndexFile(IndexDir);

        IndexSearcher indexSearcher = new IndexSearcher(DirectoryReader.open(directory));

//        ArrayList<String> keyWords = new ArrayList<String>();

        String[] keyWords = keywords.split(" ");

        BooleanQuery query = TextIndex.query(keyWords);


//        QueryParser parser = new QueryParser("content", analyzer);
//
//        Query query = parser.parse(keyWord);

        TopDocs topDocs = indexSearcher.search(query, TextIndex.MAXDOCNUM);

        Formatter formatter = new SimpleHTMLFormatter(TextIndex.preTag,TextIndex.postTag);
        Scorer scorer = new QueryScorer(query);
        Highlighter highlighter = new Highlighter(formatter, scorer);


        if (topDocs != null) {
            System.out.println("符合条件第文档总数：" + topDocs.totalHits);
            for (int i = 0; i < topDocs.scoreDocs.length; i++) {
                Document doc = indexSearcher.doc(topDocs.scoreDocs[i].doc);

                links.add(doc.get("link"));
                String titleHighLight = highlighter.getBestFragment(analyzer, "titile", doc.get("title"));
                String descriptionHighLight = highlighter.getBestFragment(analyzer, "description", doc.get("description"));
                if(titleHighLight!=null){
                    titles.add(titleHighLight);
                }
                else{
                    titles.add(doc.get("title"));
                }
                if(descriptionHighLight!=null){
                    descriptions.add(descriptionHighLight);
                }
                else{
                    descriptions.add(doc.get("description"));
                }


//                System.out.println(titleHighLight);
//                System.out.println(descriptionHighLight);
//                System.out.println("link："+links.get(i));
//                System.out.println("title："+titles.get(i));
//                System.out.println("description: "+description.get(i));
//                System.out.println("????????????????????????");


            }
        }
    }

    private static BooleanQuery query(String[] keyWords)throws Exception{
        BooleanQuery query =null;
        if(keyWords.length ==1){
            System.out.println(keyWords[0]);
            QueryParser parser = new QueryParser("content", analyzer);

            Query query1 = parser.parse(keyWords[0]);
//            Query query1 = new TermQuery(new Term("content",keyWords[0]));
            BooleanClause bc1 = new BooleanClause(query1, BooleanClause.Occur.MUST);
            query = new BooleanQuery.Builder().add(bc1).build();
        }
        else {
            System.out.println(keyWords[0]);
            System.out.println(keyWords[1]);
            QueryParser parser = new QueryParser("content", analyzer);

            Query query1 = parser.parse(keyWords[0]);
            Query query2 = parser.parse(keyWords[1]);
//            Query query1 = new TermQuery(new Term("content",keyWords[0]));
//            Query query2 = new TermQuery(new Term("content",keyWords[1]));

            BooleanClause bc1 = new BooleanClause(query1, BooleanClause.Occur.MUST);
            BooleanClause bc2 = new BooleanClause(query2, BooleanClause.Occur.MUST);
            query = new BooleanQuery.Builder().add(bc1).add(bc2).build();

        }
        return query;
    }

    public static void createIndex(String dir, String IndexDir) throws Exception{



        IndexWriterConfig indexConfig = new IndexWriterConfig(analyzer);
        Directory directory = openIndexFile(IndexDir);
        IndexWriter indexWriter = new IndexWriter(directory, indexConfig);
        System.out.println("open the files to be indexed!");
        File fileSet[] = new File(dir).listFiles();

        File temp;

        for (int i = 0; i < fileSet.length; i++) {
            temp = fileSet[i];
            System.out.println("   The file to be indexed is named " + temp);
            BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(temp), "UTF-8"));
            String url = br.readLine();
            String title = br.readLine();
            String content = br.readLine();
            String description = br.readLine();
            content += description;
            indexWriter.addDocument(createDocment(url, title, description, content));
            System.out.println("**" + temp + " has been indexed!");
            indexWriter.close();
            System.out.println("##########all file has been indexed!");
        }



    }

    private static Directory openIndexFile(String IndexDir) throws Exception {
        return FSDirectory.open(Paths.get(IndexDir));
    }

    private static Document createDocment(String link, String title, String description, String content) throws Exception{
        Document doc = new Document();
//        Field linkField = new Field("link",link,TextField.TYPE_STORED);
//        linkField.setFloatValue(10);
        doc.add(new Field("link", link, TextField.TYPE_STORED));
        doc.add(new Field("title", title, TextField.TYPE_STORED));
        doc.add(new Field("description", description, TextField.TYPE_STORED));
        doc.add(new Field("content", content, TextField.TYPE_NOT_STORED));

        return doc;
    }

}
