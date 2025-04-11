package gt.crawler;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse.BodyHandlers;
import java.util.HashSet;
import java.util.Set;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class Crawler implements Runnable {

  private MongoClient mc;

  private String startingUrl;
  private int bfsPerDfsRatio;

  private boolean llmFlag = false;

  public Crawler(String startingUrl, int bfsPerDfsRatio) {
    this.mc = MongoClient.getInstance();
    this.startingUrl = startingUrl;
    this.bfsPerDfsRatio = bfsPerDfsRatio;

    // Config config = Config.getInstance();
    // String host = config.getConfig().getProperty("elastic.host");
    // int port = Integer.parseInt(config.getConfig().getProperty("elastic.port"));
    // String scheme = config.getConfig().getProperty("elastic.scheme");
    // String apiKey = config.getConfig().getProperty("elastic.apikey").trim();
    // this.elastic = new Elastic(host, port, scheme, apiKey);
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

      HttpClient client = HttpClient.newHttpClient();
      String reqBody;
      if (llmFlag) {
        reqBody = parsePageWithLLM(doc, url);
      } else {
        reqBody = String.format("""
            {
                "title": "Test Item",
                "explanation": %s,
                "url": "%s",
                "date": "2025-04-10"
            }
            """, doc.html(), url);
      }

      try {
        HttpRequest req = HttpRequest.newBuilder()
            .uri(URI.create("http://localhost:8000/api/v1/index_doc"))
            .POST(HttpRequest.BodyPublishers.ofString(reqBody))
            .build();

        client.send(req, BodyHandlers.ofString());
      } catch (Error e) {
        System.err.println(e);
      }

      Elements links = doc.select("a[href]");
      for (Element link : links) {
        String href = link.absUrl("href");

        System.out.println(href);
        // Remove protocol and fragments
        try {
          URI uri = new URI(href);
          String host = uri.getHost();
          String path = uri.getPath();
          String protocol = uri.getScheme() + "://";

          if (path == "/") {
            path = "";
          }

          mc.addUrlToBack(protocol + host + path);
        } catch (Error e) {
          continue;
        }
      }

    } catch (IOException e) {
      System.err.println("Failed to visit page: " + e);
    }
  }

  private String parsePageWithLLM(Document doc, String url) {
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

    // Convert to MongoDB document and add URL
    org.bson.Document parsedDoc = org.bson.Document.parse(llmResponse);
    parsedDoc.append("source_url", url);

    System.out.println("====== Parsed JSON String ======");
    System.out.println(parsedDoc.toJson());
    System.out.println("================================");

    return parsedDoc.toJson();
  }
}
