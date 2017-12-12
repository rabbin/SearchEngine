this is a java program based on lucene 7.1.0
==
1.The followings are the core jars used
--
  * jsoup-1.8.1.jar
  * lucene-analyzers-common-7.1.0.jar
  * lucene-analyzers-smartcn-7.1.0.jar
  * lucene-core-7.1.0.jar
  * lucene-highlighter-7.1.0.jar
  * lucene-memory-7.1.0.jar
  * lucene-queryparser-7.1.0.jar
  
2.src
--
  * Spider.java:<p>
    >multithreading spider that crawls the mobile phone information on http://mobile.zol.com.cn/
  * TextIndex.java:<p>
    >1.the methods search and createIndex is the External interface. <p>
    >2.`createIndex`:create reverse index for the mobile phone information crawled.<p>
    >3.`search`according to the keyword you have input,return the results ,and the keywords will be highlighted.<p>
