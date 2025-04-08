package gt.crawler;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;


import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;


public class Crawler implements Runnable {

  private MongoClient mc;
  private String startingUrl;
  private int bfsPerDfsRatio;
  private Elastic elastic;

  public Crawler(String startingUrl, int bfsPerDfsRatio) {
    this.mc = MongoClient.getInstance();
    this.startingUrl = startingUrl;
    this.bfsPerDfsRatio = bfsPerDfsRatio;

    Config config = Config.getInstance();
    String host = config.getConfig().getProperty("elastic.host");
    int port = Integer.parseInt(config.getConfig().getProperty("elastic.port"));
    String scheme = config.getConfig().getProperty("elastic.scheme");
    String apiKey = config.getConfig().getProperty("elastic.apikey").trim();;
    this.elastic = new Elastic(host, port, scheme, apiKey);
  }

  @Override
  public void run() {
    Set<String> visitedCache = new HashSet<>();
    mc.addUrlToBack(this.startingUrl);

    while (true) {
      for (int i = 0; i < this.bfsPerDfsRatio; i++) {
        org.bson.Document doc = mc.popUrlFromFront();

        if (doc == null) {
          break;
        }

        String url = doc.getString("url");
        visitPage(url, visitedCache);
      }

      org.bson.Document doc = mc.popUrlFromBack();

      if (doc == null) {
        return;
      }

      String url = doc.getString("url");
      visitPage(url, visitedCache);
    }
  }

  private void visitPage(String url, Set<String> visitedCache) {
    try {
      
      if (visitedCache.contains(url) || mc.getDocumentFromCollection("visited", url) != null) {
        visitedCache.add(url);
        System.out.println("Already visited: " + url);
        return;
      }

      visitedCache.add(url);
      mc.insertIntoCollection("visited", new org.bson.Document("_id", url));

      Document doc = Jsoup.connect(url)
          .userAgent("Mozilla/5.0 (compatible; GTCrawler/1.0)")
          .get();

      elastic.insertHtml("crawled-pages", url, doc.html());

      Elements links = doc.select("a[href]");
      for (Element link : links) {
        String absHref = link.absUrl("href");

        // remove fragments
        String parsedHref = absHref.split("#")[0];
        mc.addUrlToBack(parsedHref);
      }
      // Use this flag to enable LLM parsing
      // 0 = no LLM parsing, 1 = LLM parsing
      int LLMFlag = 0;
      if (LLMFlag == 1) {
        parsePageWithLLM(doc, url);
      } 
      
      

    } catch (IOException e) {
      System.err.println("Failed to visit page: " + e.getMessage());
    }
  }

  private void parsePageWithLLM(Document doc, String url) {
    // Call LLM to parse the page content into structured JSON
    String rawText = doc.body().text();

    // OPTIONAL: limit content length
    if (rawText.length() > 4000) {
      rawText = rawText.substring(0, 4000);
    }

    System.out.println("====== Raw Text Preview ======");
    System.out.println(rawText.substring(0, Math.min(rawText.length(), 500))); // show first 500 chars
    System.out.println("================================");

    // Get JSON string from LLM
    String llmResponse = LLMClient.analyzePageContent(rawText);

    System.out.println("====== LLM Response ======");
    System.out.println(llmResponse);
    System.out.println("================================");

    // Convert to MongoDB document and print it
    org.bson.Document parsedDoc = org.bson.Document.parse(llmResponse);
    parsedDoc.append("source_url", url);

    System.out.println("====== Parsed JSON for MongoDB ======");
    System.out.println(parsedDoc.toJson());
    System.out.println("================================");

    // Store into MongoDB
    mc.insertIntoCollection("ParsedPages", parsedDoc);
  }
}