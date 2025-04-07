package gt.crawler;

import org.bson.Document;

public class App {
    public static void main(String[] args) {
        String[] seedUrls = {"https://www.cc.gatech.edu/", "https://coe.gatech.edu/", "https://cos.gatech.edu/", "https://design.gatech.edu/", "https://iac.gatech.edu/", "https://www.scheller.gatech.edu/index.html", "https://lifetimelearning.gatech.edu/"};

        for (int i = 0; i < seedUrls.length; i++) {
            Crawler c = new Crawler(seedUrls[i], 5);
            new Thread(c).start();
            System.out.println("Thread for seed url " + seedUrls[i] + " started.");
        }
    }
}