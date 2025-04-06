package gt.crawler;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.http.HttpHost;
import org.apache.http.message.BasicHeader;
import org.apache.http.Header;
import org.elasticsearch.client.RestClient;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.core.IndexResponse;
import co.elastic.clients.transport.rest_client.RestClientTransport;
import co.elastic.clients.json.jackson.JacksonJsonpMapper;

public class Elastic {
    private ElasticsearchClient client;

     /**
     * Constructs the Elasticsearch client using the new Java API client and API key authentication.
     *
     * @param host   The Elasticsearch host (e.g., "your-es-host.com")
     * @param port   The port (e.g., 9243 if using a cloud service)
     * @param scheme The scheme ("http" or "https")
     * @param apiKey Your Base64-encoded API key
     */
    public Elastic(String host, int port, String scheme, String apiKey) {
        // configure the default header to use the API key for authentication.
        Header[] defaultHeaders = new Header[] {
            new BasicHeader("Authorization", "ApiKey " + apiKey)
        };

        // create the low-level client with the host, port, scheme, and default headers.
        RestClient restClient = RestClient.builder(new HttpHost(host, port, scheme))
            .setDefaultHeaders(defaultHeaders)
            .build();

        // create the transport with a Jackson mapper.
        RestClientTransport transport = new RestClientTransport(restClient, new JacksonJsonpMapper());

        // create the Elasticsearch Java API client.
        client = new ElasticsearchClient(transport);
    }

    /**
     * Insert HTML content into the specified Elasticsearch index.
     *
     * @param index The name of the Elasticsearch index
     * @param id    URL as unique identifier
     * @param html  The full HTML content of the page
     */
    public void insertHtml(String index, String id, String html) {
        // build the document as a Map
        Map<String, Object> document = new HashMap<>();
        document.put("url", id);
        document.put("html", html);
        document.put("timestamp", System.currentTimeMillis());

        try {
            // index the document using a lambda to create the IndexRequest
            IndexResponse response = client.index(i -> i
                .index(index)
                .id(id)
                .document(document)
            );
            System.out.println("Indexed document with result: " + response.result());
        } catch (IOException e) {
            System.err.println("Error indexing document: " + e.getMessage());
        }
    }

    /**
     * Closes the underlying transport, which also closes the low-level client.
     */
    public void close() throws IOException {
        client._transport().close();
    }
}
