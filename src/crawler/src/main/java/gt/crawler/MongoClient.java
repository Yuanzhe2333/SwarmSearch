package gt.crawler;

import java.util.ArrayList;
import java.util.List;

import org.bson.Document;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.ServerApi;
import com.mongodb.ServerApiVersion;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.FindOneAndDeleteOptions;
import com.mongodb.client.model.Indexes;
import com.mongodb.client.model.Sorts;

public class MongoClient {
  private static MongoClient instance;
  private com.mongodb.client.MongoClient mongoClient;
  private String DATABASE_NAME = "CrawlData";

  private MongoClient() {
    Config config = Config.getInstance();

    String connectionString = config.getConfig().getProperty("mongodb.uri");
    ServerApi serverApi = ServerApi.builder()
        .version(ServerApiVersion.V1)
        .build();
    MongoClientSettings settings = MongoClientSettings.builder()
        .applyConnectionString(new ConnectionString(connectionString))
        .serverApi(serverApi)
        .build();

    mongoClient = MongoClients.create(settings);

    // create index for mongo
    MongoDatabase database = mongoClient.getDatabase(DATABASE_NAME);
    MongoCollection<Document> col = database.getCollection("PageQueue");

    col.createIndex(Indexes.ascending("sequence"));

  }

  public static MongoClient getInstance() {
    if (instance == null) {
      instance = new MongoClient();
    }
    return instance;
  }

  public com.mongodb.client.MongoClient getMongoClient() {
    return mongoClient;
  }

  /**
   * Insert a document into a collection in the CrawlData database.
   *
   * @param collectionName The name of the collection to insert the document into.
   * @param doc            The document to insert.
   */
  public void insertIntoCollection(String collectionName, Document doc) {
    MongoDatabase database = mongoClient.getDatabase(DATABASE_NAME);
    MongoCollection<Document> collection = database.getCollection(collectionName);

    collection.insertOne(doc);
  }

  /**
   * Get a document from a collection in the CrawlData database.
   *
   * @param collectionName The name of the collection to get the document from.
   * @return The document from the collection.
   */
  public Document getDocumentFromCollection(String collectionName, String objId) {
    MongoDatabase database = mongoClient.getDatabase(DATABASE_NAME);
    MongoCollection<Document> collection = database.getCollection(collectionName);

    return collection.find(new Document("_id", objId)).first();
  }

  /**
   * Pops the lowest sequence number in the MongoDB "PageQueue" Collection
   */
  public Document popUrlFromFront() {
    MongoDatabase database = mongoClient.getDatabase(DATABASE_NAME);
    MongoCollection<Document> collection = database.getCollection("PageQueue");

    Document doc = collection.findOneAndDelete(new Document(),
        new FindOneAndDeleteOptions().sort(Sorts.ascending("sequence")));

    return doc;
  }

  /**
   * Pops the highest sequence number in the MongoDB "PageQueue" Collection
   */
  public Document popUrlFromBack() {
    MongoDatabase database = mongoClient.getDatabase(DATABASE_NAME);
    MongoCollection<Document> collection = database.getCollection("PageQueue");

    Document doc = collection.findOneAndDelete(new Document(),
        new FindOneAndDeleteOptions().sort(Sorts.descending("sequence")));

    return doc;
  }

  public long countDocuments(String collectionName) {
    MongoDatabase database = mongoClient.getDatabase(DATABASE_NAME);
    MongoCollection<Document> collection = database.getCollection(collectionName);

    try {
      long cnt = collection.countDocuments();
      return cnt;
    } catch (Error e) {
      return 0;
    }

  }

  public void addUrlsToBack(List<String> urls) {
    MongoDatabase database = mongoClient.getDatabase(DATABASE_NAME);
    MongoCollection<Document> collection = database.getCollection("PageQueue");

    Document doc = collection.find().sort(Sorts.descending("sequence")).limit(1)
        .first();

    long newSeq;
    if (doc == null) {
      newSeq = 0;
    } else {
      newSeq = doc.getLong("sequence") - 1;
    }

    List<Document> docs = new ArrayList<>();

    for (int i = 0; i < urls.size(); i++) {
      // leads to duplicate key errors but slows down the program too much if I check
      // every one
      docs.add(new Document().append("url", urls.get(i)).append("sequence", newSeq + i));
    }

    collection.insertMany(docs);
  }

  /**
   * Close the MongoClient.
   */
  public void close() {
    mongoClient.close();
  }
}
