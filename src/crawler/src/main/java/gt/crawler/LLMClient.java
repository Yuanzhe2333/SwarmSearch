package gt.crawler;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Properties;

import org.json.JSONObject;

public class LLMClient {
  private static final String OPENAI_ENDPOINT;
  private static final String OPENAI_API_KEY;
  private static final String DEEPSEEK_ENDPOINT;
  private static final String DEEPSEEK_API_KEY;

  static {
    Properties props = Config.getInstance().getConfig();
    OPENAI_ENDPOINT = props.getProperty("openai.endpoint", "https://api.openai.com/v1/chat/completions");
    OPENAI_API_KEY = props.getProperty("openai.api.key");

    DEEPSEEK_ENDPOINT = props.getProperty("deepseek.endpoint","https://api.deepseek.com/chat/completions");
    DEEPSEEK_API_KEY = props.getProperty("deepseek.api.key");

    if (OPENAI_API_KEY == null || OPENAI_API_KEY.isBlank()) {
      throw new RuntimeException("Missing OpenAI API key");
    }
    if (DEEPSEEK_API_KEY == null || DEEPSEEK_API_KEY.isBlank()) {
      throw new RuntimeException("Missing DeepSeek API key");
    }
  }

  public static String analyzePageContent(String content) {
    if (content.length() > 4000) {
      return callLLM(content, "deepseek");
    } else {
      return callLLM(content, "openai");
    }
  }

  private static String callLLM(String content, String provider) {
    try {
      String endpoint = provider.equals("deepseek") ? DEEPSEEK_ENDPOINT : OPENAI_ENDPOINT;
      String apiKey = provider.equals("deepseek") ? DEEPSEEK_API_KEY : OPENAI_API_KEY;
      String model = provider.equals("deepseek") ? "deepseek-chat" : "gpt-4o-mini";

      String requestBody = new JSONObject()
        .put("model", model)
        .put("messages", new org.json.JSONArray()
          .put(new JSONObject().put("role", "system").put("content",
            "You are a helpful assistant that extracts and returns only compact JSON (no formatting, no line breaks). Your response should ONLY contain a valid JSON object with keys in the defined order: title: give a short, clear, representative title for the content, explaination: summarize what the html content has, date: if any , and authors: if any. Do not explain anything else."))
          .put(new JSONObject().put("role", "user").put("content",
            "Please extract structured JSON from the following html content:\n" + content)))
        .put("temperature", 0.1)
        .toString();

      HttpRequest request = HttpRequest.newBuilder()
        .uri(URI.create(endpoint))
        .header("Authorization", "Bearer " + apiKey)
        .header("Content-Type", "application/json")
        .POST(HttpRequest.BodyPublishers.ofString(requestBody))
        .build();

      HttpClient client = HttpClient.newHttpClient();
      HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

      return extractJsonFromLLMResponse(response.body());

    } catch (Exception e) {
      System.err.println("❌ Failed to analyze content via " + provider + ": " + e.getMessage());
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

        // Strip markdown code block if present (DeepSeek adds ```json ... ```)
        if (content.startsWith("```json")) {
            content = content.replaceAll("(?s)```json\\s*(\\{.*?\\})\\s*```", "$1");
        }

        // Remove any extra newlines, tabs, and trim
        content = content.replaceAll("[\\n\\r\\t]", "").trim();

        // Now parse
        JSONObject parsed = new JSONObject(content);
        return parsed.toString();

    } catch (Exception e) {
        System.err.println("❌ Failed to parse LLM response: " + e.getMessage());
        return responseBody;
    }
  }
}
