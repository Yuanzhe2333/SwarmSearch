package gt.crawler;

public class Crawler {

  public Crawler() {
      
  }
  
  public void startCrawler() {}

  // private static void crawl(String url, int depth) {
  //   if (visited.contains(url) || depth > MAX_DEPTH) return;
  //   visited.add(url);
  //
  //   try {
  //     Document doc = Jsoup.connect(url)
  //     .userAgent("Mozilla/5.0 (compatible; GTCrawler/1.0)")
  //     .get();
  //
  //     System.out.println("Crawling: " + url);
  //
  //     Map<String, Integer> wordScores = scoreWordsFromPage(doc);
  //     wordScores.entrySet().stream()
  //       .sorted((e1, e2) -> Integer.compare(e2.getValue(), e1.getValue()))
  //       .limit(10)
  //       .forEach(entry -> System.out.println(entry.getKey() + " : " + entry.getValue()));
  //     System.out.println();
  //
  //     Elements links = doc.select("a[href]");
  //     for (Element link : links) {
  //       String absHref = link.absUrl("href");
  //       if (absHref.startsWith(BASE_URL)) {
  //         crawl(absHref, depth); // Internal: same depth
  //       } else {
  //         crawl(absHref, depth + 1); // External: one hop only
  //       }
  //     }
  //
  //   } catch (IOException e) {
  //     System.err.println("Failed to crawl: " + url + " | " + e.getMessage());
  //   }
  // }
}
