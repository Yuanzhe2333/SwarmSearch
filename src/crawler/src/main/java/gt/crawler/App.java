package gt.crawler;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.*;

public class App {

    private static final String BASE_URL = "https://www.cc.gatech.edu";
    private static final Map<String, Integer> TAG_WEIGHTS = Map.of(
        "title", 5,
        "h1", 4,
        "h2", 3,
        "h3", 2,
        "p", 1
    );

    private static final Set<String> visited = new HashSet<>();
    private static final int MAX_DEPTH = 1;

    public static void main(String[] args) {
        System.out.println("Starting Generic Web Crawler with Word Ranking...\n");
        crawl(BASE_URL, 0);
    }

    private static void crawl(String url, int depth) {
        if (visited.contains(url) || depth > MAX_DEPTH) return;
        visited.add(url);

        try {
            Document doc = Jsoup.connect(url)
                    .userAgent("Mozilla/5.0 (compatible; GTCrawler/1.0)")
                    .get();

            System.out.println("Crawling: " + url);

            Map<String, Integer> wordScores = scoreWordsFromPage(doc);
            wordScores.entrySet().stream()
                    .sorted((e1, e2) -> Integer.compare(e2.getValue(), e1.getValue()))
                    .limit(10)
                    .forEach(entry -> System.out.println(entry.getKey() + " : " + entry.getValue()));
            System.out.println();

            Elements links = doc.select("a[href]");
            for (Element link : links) {
                String absHref = link.absUrl("href");
                if (absHref.startsWith(BASE_URL)) {
                    crawl(absHref, depth); // Internal: same depth
                } else {
                    crawl(absHref, depth + 1); // External: one hop only
                }
            }

        } catch (IOException e) {
            System.err.println("Failed to crawl: " + url + " | " + e.getMessage());
        }
    }

    private static Map<String, Integer> scoreWordsFromPage(Document doc) {
        Map<String, Integer> wordScores = new HashMap<>();

        for (Map.Entry<String, Integer> entry : TAG_WEIGHTS.entrySet()) {
            String tag = entry.getKey();
            int weight = entry.getValue();

            Elements elements = doc.select(tag);
            for (Element el : elements) {
                String[] words = el.text().toLowerCase().split("\\W+");
                for (String word : words) {
                    if (!word.isBlank()) {
                        wordScores.put(word, wordScores.getOrDefault(word, 0) + weight);
                    }
                }
            }
        }

        return wordScores;
    }
}
