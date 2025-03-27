package gt.crawler;

import org.bson.Document;

public class App {
    public static void main(String[] args) {
        System.out.println("Starting Crawler");

        MongoClient mc = MongoClient.getInstance();

        mc.insertIntoCollection("visited", new Document("url", "bing.com"));

        // crawl(BASE_URL, 0);
        mc.close();
    }
}
