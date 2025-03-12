package gt.crawler;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

public class App {

    private static final String BASE_URL = "https://www.cc.gatech.edu";
    private static final String FACULTY_DIRECTORY_URL = "https://www.cc.gatech.edu/people/faculty";

    public static void main(String[] args) {
        System.out.println("Starting Georgia Tech CoC Faculty Crawler...");
        crawlFacultyDirectory();
    }

    private static void crawlFacultyDirectory() {
        try {
            Document doc = Jsoup.connect(FACULTY_DIRECTORY_URL)
                                .userAgent("Mozilla/5.0 (compatible; GTCrawler/1.0)")
                                .get();

            Elements profileLinks = doc.select("a[href^=/people/]");
            Set<String> uniqueProfiles = new HashSet<>();

            for (Element link : profileLinks) {
                String profileUrl = BASE_URL + link.attr("href");

                // Avoid duplicate profiles
                if (uniqueProfiles.add(profileUrl)) {
                    crawlFacultyProfile(profileUrl);
                    Thread.sleep(1000); // polite crawling (1-second delay)
                }
            }
        } catch (IOException | InterruptedException e) {
            System.err.println("Error crawling directory: " + e.getMessage());
        }
    }

    private static void crawlFacultyProfile(String url) {
        try {
            System.out.println("\nCrawling: " + url);

            Document doc = Jsoup.connect(url)
                                .userAgent("Mozilla/5.0 (compatible; GTCrawler/1.0)")
                                .get();

            String name = doc.selectFirst("h1.page-title").text();
            Element titleElement = doc.selectFirst(".field--name-field-position");
            String title = titleElement != null ? titleElement.text() : "No title listed";
            Element emailElement = doc.selectFirst("a[href^=mailto:]");
            String email = emailElement != null ? emailElement.text() : "No email listed";
            Elements researchElements = doc.select(".field--name-field-research-interests .field__item");
            String interests = researchElements.eachText().isEmpty() ? "Not listed" : String.join(", ", researchElements.eachText());

            System.out.println("Name: " + name);
            System.out.println("Title: " + title);
            System.out.println("Email: " + email);
            System.out.println("Research Interests: " + interests);

        } catch (IOException e) {
            System.err.println("Error crawling profile: " + url);
            e.printStackTrace();
        }
    }
}
