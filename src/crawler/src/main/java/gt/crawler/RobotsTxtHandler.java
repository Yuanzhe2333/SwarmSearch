package gt.crawler;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.ArrayList;

import org.bson.Document;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;

class RobotsTxtHandler {
  private static String DATABASE_NAME = "CrawlData";
  private static com.mongodb.client.MongoClient mc = MongoClient.getInstance().getMongoClient();

  /**
   * Visits the robots.txt if there is one, and adds disallowed urls to a database
   *
   * @param url the host and path of a url
   **/
  static void handleRobotsTxt(String url) throws URISyntaxException, IOException, InterruptedException {

    HttpClient client = HttpClient.newHttpClient();

    System.out.println("Visiting robots.txt for " + url);
    HttpRequest req = HttpRequest.newBuilder()
        .uri(new URI(url + "/robots.txt"))
        .GET()
        .build();

    HttpResponse<String> res = client.send(req, HttpResponse.BodyHandlers.ofString());
    String robotsContent = res.body();

    List<Document> disallowedUrls = new ArrayList<>();

    // purely looking for * user-agent
    try (BufferedReader reader = new BufferedReader(new StringReader(robotsContent))) {
      String line;
      String userAgent = "";

      while ((line = reader.readLine()) != null) {
        line = line.trim();

        if (line.isEmpty() || line.startsWith("#")) {
          continue;
        }

        int pivot = line.indexOf(":");

        if (pivot == -1) {
          continue;
        }

        String field = line.substring(0, pivot).trim().toLowerCase();
        String value = line.substring(pivot + 1).trim();

        if (field.equals("user-agent")) {
          userAgent = value;
        } else {
          if (!userAgent.equals("*") || !field.equals( "disallow")) {
            continue;
          }

          if (value.charAt(0) == '/') {
            value = value.substring(1);
            disallowedUrls.add(new Document("_id", url + value));
          } else {
            // if the value is not a path, we assume it's a full url
            // and add it to the disallowed urls
            disallowedUrls.add(new Document("_id", value));
          }
        }
      }
    }

    MongoDatabase database = mc.getDatabase(DATABASE_NAME);
    MongoCollection<Document> collection = database.getCollection("DisallowedUrls");

    if (!disallowedUrls.isEmpty()) {
      collection.insertMany(disallowedUrls);
    }
  }

  static boolean isUrlAllowed(String url) {
    MongoDatabase database = mc.getDatabase(DATABASE_NAME);
    MongoCollection<Document> collection = database.getCollection("DisallowedUrls");

    return collection.find(Filters.eq("_id", url)).first() == null;
  }
}
