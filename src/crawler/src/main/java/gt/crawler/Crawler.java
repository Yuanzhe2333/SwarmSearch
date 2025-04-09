package gt.crawler;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashSet;
import java.util.Set;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class Crawler implements Runnable {

  private MongoClient mc;
  private Elastic elastic;

  private String startingUrl;
  private int bfsPerDfsRatio;

  private boolean llmFlag = false;

  public Crawler(String startingUrl, int bfsPerDfsRatio) {
    this.mc = MongoClient.getInstance();
    this.startingUrl = startingUrl;
    this.bfsPerDfsRatio = bfsPerDfsRatio;

    Config config = Config.getInstance();
    String host = config.getConfig().getProperty("elastic.host");
    int port = Integer.parseInt(config.getConfig().getProperty("elastic.port"));
    String scheme = config.getConfig().getProperty("elastic.scheme");
    String apiKey = config.getConfig().getProperty("elastic.apikey").trim();
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
        try {
          visitPage(url, visitedCache);
        } catch (Exception e) {
          System.err.println(e);
        }
      }

      org.bson.Document doc = mc.popUrlFromBack();

      if (doc == null) {
        return;
      }

      String url = doc.getString("url");
      try {
        visitPage(url, visitedCache);
      } catch (Exception e) {
        System.err.println(e);
      }
    }
  }

  private void visitPage(String url, Set<String> visitedCache) throws URISyntaxException, InterruptedException {
    try {
      if (visitedCache.contains(url) || mc.getDocumentFromCollection("visited", url) != null) {
        visitedCache.add(url);
        System.out.println("Already visited: " + url);
        return;
      }

      visitedCache.add(url);

      if (!RobotsTxtHandler.isUrlAllowed(url)) {
        System.out.println("Url not allowed: " + url);
        return;
      }

      RobotsTxtHandler.handleRobotsTxt(url);
      mc.insertIntoCollection("visited", new org.bson.Document("_id", url));

      Document doc = Jsoup.connect(url)
          .userAgent("Mozilla/5.0 (compatible; GTCrawler/1.0)")
          .get();

      if (llmFlag) {
        elastic.insertHtml("crawled-pages", parsePageWithLLM(doc, url));
      } else {
        org.bson.Document elasticDoc = new org.bson.Document("html", doc.html())
            .append("url", url);

        elastic.insertHtml("crawled-pages", elasticDoc);
      }

      Elements links = doc.select("a[href]");
      for (Element link : links) {
        // Remove protocol and fragments
        URI uri = new URI(link.absUrl("href"));

        String host = uri.getHost();
        String path = uri.getPath();

        if (path == "/") {
          path = "";
        }

        mc.addUrlToBack(host + path);
      }

    } catch (IOException e) {
      System.err.println("Failed to visit page: " + e.getMessage());
    }
  }

  private org.bson.Document parsePageWithLLM(Document doc, String url) {
    String rawText = doc.body().text();

    // OPTIONAL: limit content length
    if (rawText.length() > 4000) {
      rawText = rawText.substring(0, 4000);
    }

    // Get JSON string from LLM
    String llmResponse = LLMClient.analyzePageContent(rawText);

    // Convert to MongoDB document and print it
    org.bson.Document parsedDoc = org.bson.Document.parse(llmResponse);
    parsedDoc.append("url", url);

    return parsedDoc;
  }
}
