package gt.crawler;

import java.io.IOException;
import java.util.ArrayDeque;
import java.util.Deque;
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

  public Crawler(String startingUrl, int bfsPerDfsRatio) {
    this.mc = MongoClient.getInstance();
    this.startingUrl = startingUrl;
    this.bfsPerDfsRatio = bfsPerDfsRatio;
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
        return;
      }

      visitedCache.add(url);
      mc.insertIntoCollection("visited", new org.bson.Document("_id", url));

      Document doc = Jsoup.connect(url)
          .userAgent("Mozilla/5.0 (compatible; GTCrawler/1.0)")
          .get();

      Elements links = doc.select("a[href]");
      for (Element link : links) {
        String absHref = link.absUrl("href");

        // remove fragments
        String parsedHref = absHref.split("#")[0];
        mc.addUrlToBack(parsedHref);
      }
    } catch (IOException e) {
      System.err.println("Failed to visit page: " + e.getMessage());
    }
  }
}
