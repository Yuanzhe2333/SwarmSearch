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

public class Crawler {
  private MongoClient mc;

  public Crawler() {
    this.mc = MongoClient.getInstance();
  }

  public void startCrawler(String startingUrl, int bfsPerDFS) {
    // might not be a necessary function but considering doing setup beforhand. Can
    // remove if not necessary
    crawl(startingUrl, bfsPerDFS);
  }

  private void crawl(String startingUrl, int bfsPerDFS) {
    Set<String> visitedCache = new HashSet<>();
    Deque<String> dq = new ArrayDeque<String>();
    dq.offerLast(startingUrl);

    while (true) {
      for (int i = 0; i < bfsPerDFS; i++) {
        if (dq.size() == 0) {
          return;
        }

        String url = dq.pollFirst();
        visitPage(url, visitedCache, dq);
      }

      if (dq.size() == 0) {
        return;
      }

      String url = dq.pollLast();
      visitPage(url, visitedCache, dq);
    }
  }

  private void visitPage(String url, Set<String> visitedCache, Deque<String> dq) {
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
        String parsedRef = absHref.split("#")[0];
        dq.offerLast(parsedRef);
      }
    } catch (IOException e) {
      System.err.println("Failed to visit page: " + e.getMessage());
    }
  }
}
