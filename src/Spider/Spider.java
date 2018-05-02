package Spider;
import java.util.ArrayList;
import java.util.HashMap;
import java.net.URL;
import java.net.URLConnection;
import java.io.*;
import org.jsoup.*;
import org.jsoup.nodes.*;
import org.jsoup.select.Elements;


public class Spider {



    public static void main(String[] agrs) {

        long start = System.currentTimeMillis();

        Spider spider = new Spider();

        spider.addUrl("http://mobile.zol.com.cn/", 0);

        spider.begin();
        while (true) {
            if (spider.notCrawledUrlSet.isEmpty() &&Thread.activeCount() == 1) {
                long end = System.currentTimeMillis();
                System.out.println("总共爬了" + spider.allUrlSet.size() + "个网页");
                System.out.println("总共耗时" + (end - start) / 1000 + "秒");
                System.exit(0);
            }
        }
    }


    ArrayList<String> notCrawledUrlSet = new ArrayList<String>();           //未被爬取的网页
    ArrayList<String> allUrlSet = new ArrayList<String>();                  //所有网页
    HashMap<String, Integer> depthTable = new HashMap<String, Integer>();           //网页+深度 哈希表
    private static final int maxDepth = 2;                             //爬取网页最大深度-1
    private static final int threadNum = 10;                            //线程数量
    public  Object urlSetLock = new Object();                               //线程间通信变量
    public  Object urlNumlock = new Object();
    static int  urlNum = 0;                                                     //下载网页的数量

    public void begin() {
        for (int i = 0; i < threadNum; i++) {
            new crawlThread(this).start();
        }
    }

    public  void addUrl(String url, int depth) {
        synchronized(urlSetLock){
            notCrawledUrlSet.add(url);          //添加没有爬取过的url
            allUrlSet.add(url);                 //添加爬取过的url
            depthTable.put(url, depth);         //记录当前url的深度
            urlSetLock.notifyAll();                 //唤醒所有被阻塞的进程

        }

    }


    public  String getUrl() {
        synchronized(urlSetLock){
            if (notCrawledUrlSet.isEmpty()) {
                return null;
            }
            String url = notCrawledUrlSet.get(0);
            notCrawledUrlSet.remove(0);
            return url;
        }

    }

    public void crawl(String url) {
        try {
            URLConnection conn = new URL(url).openConnection();
            conn.setRequestProperty("User-Agent", "Mozilla/4.0 (compatible; MSIE 5.0; Windows XP; DigExt)");
//            InputStream is = conn.getInputStream();
//
//            String fileName = "C:\\Users\\rabbin\\Desktop\\File\\zolMobile\\"+urlNum+".txt";
//            System.out.println(fileName);
//            FileOutputStream out = new FileOutputStream(fileName);
//            int a = 0;
//            while ((a = is.read()) != -1) {
//                out.write(a);
//            }
//            out.close();
//            is.close();
            parseContxt(url);
        } catch (Exception e) {
            System.out.println(e);
        }

    }

    public void parseContxt(String  url){
        try{
            Document context = Jsoup.connect(url).get();
            String title = context.getElementsByTag("title").get(0).text();
            Elements links= context.getElementsByTag("a");
            String keywords = getContent(context,1);
            String description = getContent(context,2);
            if(keywords!= null ||description!= null){

                synchronized (urlNumlock){
                    urlNum++;
                }
                String fileName = "C:\\Users\\rabbin\\Desktop\\File\\zolMobile\\"+urlNum+".txt";
                System.out.println(fileName);
//                System.out.println(url);

                FileWriter writer = new FileWriter(fileName);
                PrintWriter out = new PrintWriter(writer,true);

                out.println(url);
                out.println(title);
                out.println(keywords);
                out.println(description);
                out.close();
                writer.close();
            }
            int depth = depthTable.get(url);
            if(depth <maxDepth){
                for(Element link:links){
                    String newLink= link.attr("abs:href");
                    if(!newLink.equals("")&&!allUrlSet.contains(newLink)){
                        addUrl(newLink,depth+1);
                    }
                }
            }

        }catch (Exception e){
                System.out.println(e);

        }
    }

    private String getContent(Document temp,int flag)throws Exception{
        String content = new String();          //返回关键内容

        Elements metas = temp.getElementsByTag("meta");
        if(flag==1){                    //1为获取关键字
            for(Element li:metas){
                if(li.attr("name").equals("keywords")){
//                    System.out.print("keywords: ");
//                    System.out.println(li.attr("content"));
                    content+=li.attr("content");
                }
            }
        }
        else{                       //2为获取描述
            for(Element li:metas){
                if(li.attr("name").equals("description")){
//                    System.out.print("keywords: ");
//                    System.out.println(li.attr("content"));
                    content+=li.attr("content");
                }
            }


        }

        return content;
    }
}

class crawlThread extends Thread {
    Spider spider = null;

    crawlThread(Spider spider) {
        this.spider = spider;
    }

    public void run() {
        String url ;
        while(true){
            url = spider.getUrl();
            if(url!=null){
//                System.out.println(this.getName());
                spider.crawl(url);
            }

        }

    }

}



