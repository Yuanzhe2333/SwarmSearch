package gt.crawler;

import org.bson.Document;

public class App {
    public static void main(String[] args) {
        String[] seedUrls = { "https://www.cc.gatech.edu/", "https://coe.gatech.edu/", "https://cos.gatech.edu/",
                "https://design.gatech.edu/", "https://iac.gatech.edu/", "https://www.scheller.gatech.edu/index.html",
                "https://lifetimelearning.gatech.edu/" };
        MongoClient mc = MongoClient.getInstance();

        if (mc.countDocuments("visited") == 0) {
            System.out.println("Inserting Seeds");
            for (String url : seedUrls) {
                Document doc = new Document();
                doc.append("url", url);
                doc.append("sequence", 1);

                mc.insertIntoCollection("PageQueue", doc);
            }
        }

        int numThreads = 250;
        for (int i = 0; i < numThreads; i++) {
            Crawler c = new Crawler(1,0);
            new Thread(c).start();
        }

    }
}
