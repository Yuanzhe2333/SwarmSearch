package gt.crawler;

import org.bson.Document;

public class App {
    public static void main(String[] args) {
        System.out.println("Starting Crawler");

        MongoClient mc = MongoClient.getInstance();

        Crawler c1 = new Crawler();
        c1.startCrawler("https://fingertip-onstage.netlify.app/", 2);

        mc.close();
    }
}
