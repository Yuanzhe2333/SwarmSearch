package gt.crawler;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.ServerApi;
import com.mongodb.ServerApiVersion;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;

import org.bson.Document;

public class MongoClient {
  private static MongoClient instance;
  private com.mongodb.client.MongoClient mongoClient;

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
    MongoDatabase database = mongoClient.getDatabase("CrawlData");
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
    MongoDatabase database = mongoClient.getDatabase("CrawlData");
    MongoCollection<Document> collection = database.getCollection(collectionName);

    return collection.find(new Document("_id", objId)).first();
  }

  /**
   * Close the MongoClient.
   */
  public void close() {
    mongoClient.close();
  }
}
