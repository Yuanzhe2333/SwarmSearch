package gt.crawler;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import org.apache.http.HttpHost;
import org.apache.http.message.BasicHeader;
import org.bson.Document;
import org.apache.http.Header;
import org.elasticsearch.client.RestClient;
import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.core.IndexResponse;
import co.elastic.clients.transport.rest_client.RestClientTransport;
import co.elastic.clients.json.jackson.JacksonJsonpMapper;

// elastic logic moved to search engine API, keeping for now
public class Elastic {
    private ElasticsearchClient client;

    /**
     * Constructs the Elasticsearch client using the new Java API client and API key
     * authentication.
     *
     * @param host   The Elasticsearch host
     * @param port   The port
     * @param scheme The scheme
     * @param apiKey API key
     */
    public Elastic(String host, int port, String scheme, String apiKey) {
        Header[] defaultHeaders = new Header[] {
                new BasicHeader("Authorization", "ApiKey " + apiKey)
        };

        RestClient restClient = RestClient.builder(new HttpHost(host, port, scheme))
                .setDefaultHeaders(defaultHeaders)
                .build();

        RestClientTransport transport = new RestClientTransport(restClient, new JacksonJsonpMapper());
        client = new ElasticsearchClient(transport);
    }

    /**
     * Inserts HTML content into the specified Elasticsearch index.
     *
     * @param index The name of the Elasticsearch index.
     * @param doc   document with html and url
     */
    public void insertHtml(String index, Document doc) {
        doc.put("timestamp", System.currentTimeMillis());

        try {
            IndexResponse response = client.index(i -> i
                    .index(index)
                    .id(doc.getString("url"))
                    .document(doc));
            System.out.println("Indexed document with result: " + response.result());
        } catch (IOException e) {
            System.err.println("Error indexing document: " + e.getMessage());
        }
    }

    public void close() throws IOException {
        client._transport().close();
    }
}
