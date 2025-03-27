package gt.crawler;

import java.io.IOException;

import java.util.HashSet;
import java.util.Set;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class Crawler {
  private Set<String> visitedCache = new HashSet<>();
  private MongoClient mc;

  public Crawler() {
    mc = MongoClient.getInstance();
  }

  public void startCrawler() {
  }

  private void crawl(String url, int depth) {
    if (visitedCache.contains(url)) {

    }

    visitedCache.add(url);

    try {
      Document doc = Jsoup.connect(url)
          .userAgent("Mozilla/5.0 (compatible; GTCrawler/1.0)")
          .get();

      System.out.println("Crawling: " + url);

      Elements links = doc.select("a[href]");
      for (Element link : links) {
        String absHref = link.absUrl("href");
        // if (absHref.startsWith(BASE_URL)) {
        // crawl(absHref, depth); // Internal: same depth
        // } else {
        // crawl(absHref, depth + 1); // External: one hop only
        // }
      }

    } catch (IOException e) {
      System.err.println("Failed to crawl: " + url + " | " + e.getMessage());
    }
  }
}
