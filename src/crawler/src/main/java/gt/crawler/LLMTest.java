package gt.crawler;

public class LLMTest {
    public static void main(String[] args) {
        // Sample HTML-like content
        String shortText = "Georgia Tech’s MSCS program allows students to specialize in computing systems, theory, or applications. Professors like Dr. Cecilia Testart offer cutting-edge research opportunities.";
        String longText = shortText.repeat(500); // This will exceed 4000 characters

        System.out.println("====== TEST 1: Short Text (OpenAI) ======");
        String resultOpenAI = LLMClient.analyzePageContent(shortText);
        System.out.println(resultOpenAI);

        System.out.println("\n====== TEST 2: Long Text (DeepSeek) ======");
        String resultDeepSeek = LLMClient.analyzePageContent(longText);
        System.out.println(resultDeepSeek);
    }
}
