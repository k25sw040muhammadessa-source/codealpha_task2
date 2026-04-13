// Test utility for the chatbot
// Save this file as TestChatbot.java in src/main/java/chatbot/

package chatbot;

import java.util.*;

/**
 * Standalone test utility for testing chatbot functionality.
 * Run: javac -d target/classes src/main/java/chatbot/*.java && java -cp target/classes chatbot.TestChatbot
 */
public class TestChatbot {

    public static void main(String[] args) {
        System.out.println("========================================");
        System.out.println("AI Chatbot Test Utility");
        System.out.println("========================================\n");

        // Initialize chatbot
        Chatbot chatbot = new Chatbot();
        
        // Load FAQ data
        String faqPath = "src/main/resources/faq_data.txt";
        if (chatbot.trainWithFaqFile(faqPath)) {
            System.out.println("✓ FAQ data loaded successfully\n");
        } else {
            System.out.println("⚠ Warning: Could not load FAQ data\n");
        }

        // Test cases
        String[] testQueries = {
            "Hello",
            "What services do you offer?",
            "How much does it cost?",
            "Can I integrate with my systems?",
            "What about security?",
            "How do I get started?",
            "Do you offer training?",
            "Thanks!",
            "What is your uptime guarantee?",
            "Can I customize responses?"
        };

        System.out.println("Running test queries...\n");
        System.out.println("========================================");

        for (String query : testQueries) {
            String response = chatbot.processInput(query);
            
            System.out.println("\nUser: " + query);
            System.out.println("Bot:  " + response);
            System.out.println("----------------------------------------");
        }

        // Test NLP features
        System.out.println("\n========================================");
        System.out.println("NLP Feature Tests");
        System.out.println("========================================\n");

        testNlpFeatures();

        // Print diagnostics
        System.out.println("\n========================================");
        System.out.println("System Diagnostics");
        System.out.println("========================================");
        Map<String, Object> diagnostics = chatbot.getDiagnostics();
        diagnostics.forEach((key, value) -> System.out.println(key + ": " + value));
    }

    private static void testNlpFeatures() {
        NlpProcessor nlp = new NlpProcessor();

        // Test tokenization
        System.out.println("1. Tokenization Test");
        String text = "How can I integrate your API?";
        List<String> tokens = nlp.tokenize(text);
        System.out.println("   Input: " + text);
        System.out.println("   Tokens: " + tokens);

        // Test stop word removal
        System.out.println("\n2. Stop Word Removal Test");
        List<String> filtered = nlp.removeStopWords(tokens);
        System.out.println("   Filtered: " + filtered);

        // Test stemming
        System.out.println("\n3. Stemming Test");
        String[] words = {"running", "integration", "services", "helped"};
        for (String word : words) {
            System.out.println("   " + word + " → " + nlp.stem(word));
        }

        // Test similarity
        System.out.println("\n4. Similarity Matching Test");
        String q1 = "How can I integrate your system?";
        String q2 = "Can I integrate with existing systems?";
        double similarity = nlp.calculateSimilarity(q1, q2);
        System.out.println("   Q1: " + q1);
        System.out.println("   Q2: " + q2);
        System.out.println("   Similarity: " + String.format("%.2f", similarity * 100) + "%");

        // Test intent recognition
        System.out.println("\n5. Intent Recognition Test");
        String[] intents = {
            "Hello there!",
            "How can you help?",
            "What's the price?",
            "How to contact you?"
        };
        for (String query : intents) {
            String intent = nlp.extractIntent(query);
            System.out.println("   \"" + query + "\" → " + intent);
        }

        // Test sentiment
        System.out.println("\n6. Sentiment Analysis Test");
        String[] sentiments = {
            "This is great!",
            "That's terrible",
            "It's okay"
        };
        for (String query : sentiments) {
            String sentiment = nlp.analyzeSentiment(query);
            System.out.println("   \"" + query + "\" → " + sentiment);
        }

        // Test entity extraction
        System.out.println("\n7. Entity Extraction Test");
        String entityText = "Contact us at support@example.com or call 1-800-123-4567";
        List<String> entities = nlp.extractEntities(entityText);
        System.out.println("   Input: " + entityText);
        System.out.println("   Entities: " + entities);
    }
}
