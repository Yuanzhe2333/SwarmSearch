package gt.crawler;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Properties;
import org.json.JSONObject;

public class LLMClient {
  private static final String OPENAI_ENDPOINT = "https://api.openai.com/v1/chat/completions";
  private static final String OPENAI_API_KEY;

  static {
    // Load from config file
    Properties props = Config.getInstance().getConfig();
    OPENAI_API_KEY = props.getProperty("openai.api.key");

    if (OPENAI_API_KEY == null || OPENAI_API_KEY.isBlank()) {
      throw new RuntimeException("Missing OpenAI API key in config.properties");
    }
  }

  public static String analyzePageContent(String content) {
    try {
        String requestBody = "{"
        + "\"model\": \"gpt-4o-mini\","
        + "\"messages\": ["
        + "  {"
        + "    \"role\": \"system\","
        + "    \"content\": \"You are a helpful assistant that extracts and returns only compact JSON (no formatting, no line breaks). Your response should ONLY contain a valid JSON object with keys in the defined order: title: if any, explaination: summarize what the html content has, date: if any , and authors: if any. Do not explain anything else.\""
        + "  },"
        + "  {"
        + "    \"role\": \"user\","
        + "    \"content\": \"Please extract structured JSON from the following html content:\\n" + content.replace("\"", "\\\"") + "\""
        + "  }"
        + "],"
        + "\"temperature\": 0.1"
        + "}";

      HttpRequest request = HttpRequest.newBuilder()
          .uri(URI.create(OPENAI_ENDPOINT))
          .header("Authorization", "Bearer " + OPENAI_API_KEY)
          .header("Content-Type", "application/json")
          .POST(HttpRequest.BodyPublishers.ofString(requestBody))
          .build();

      HttpClient client = HttpClient.newHttpClient();
      HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

      return extractJsonFromLLMResponse(response.body());

    } catch (Exception e) {
      System.err.println("Failed to analyze content: " + e.getMessage());
      return "{}";
    }
  }

  private static String extractJsonFromLLMResponse(String responseBody) {
        try {
            JSONObject full = new JSONObject(responseBody);
            String content = full
                .getJSONArray("choices")
                .getJSONObject(0)
                .getJSONObject("message")
                .getString("content");

            // Remove all whitespace characters that may corrupt the structure
            // This handles cases like '\nt"title": ...' or '\t"title": ...'
            content = content
                .replaceAll("[\\n\\r\\t]", "") // remove newlines, carriage returns, tabs
                .trim();

            // Now parse the cleaned string into a proper JSONObject
            JSONObject parsed = new JSONObject(content);
            return parsed.toString(); // minified JSON

        } catch (Exception e) {
            System.err.println("❌ Failed to parse LLM response: " + e.getMessage());
            return responseBody;
        }
    }
}
