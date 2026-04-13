package chatbot;

import java.util.*;

/**
 * Main Chatbot class that orchestrates NLP processing and generates intelligent responses.
 * Combines rule-based answers with machine learning to provide context-aware responses.
 */
public class Chatbot {
    
    private NlpProcessor nlpProcessor;
    private FaqTrainer faqTrainer;
    private List<String> conversationHistory;
    private Map<String, String> contextMap;
    private int responseCount;

    public Chatbot() {
        this.nlpProcessor = new NlpProcessor();
        this.faqTrainer = new FaqTrainer();
        this.conversationHistory = new ArrayList<>();
        this.contextMap = new HashMap<>();
        this.responseCount = 0;
        
        initializeRules();
    }

    /**
     * Initializes the chatbot with default rule-based responses.
     */
    private void initializeRules() {
        contextMap.put("greeting", "Hi there! I'm here to help. What would you like to know?");
        contextMap.put("gratitude", "You're welcome! Feel free to ask anything else.");
        contextMap.put("help_request", "I'm here to help! Please be more specific about what you need.");
        contextMap.put("unknown", "That's an interesting question. Let me check our database for information about that.");
    }

    /**
     * Trains the chatbot with FAQ data from a file.
     */
    public boolean trainWithFaqFile(String filePath) {
        return faqTrainer.loadFaqFromFile(filePath);
    }

    /**
     * Manually adds training data to the chatbot.
     */
    public void addTrainingData(String question, String answer) {
        faqTrainer.addFaq(question, answer);
    }

    /**
     * Processes user input and generates a response.
     * This is the main interaction point for the chatbot.
     */
    public String processInput(String userInput) {
        if (userInput == null || userInput.isEmpty()) {
            return "Please enter a message.";
        }

        // Track conversation
        conversationHistory.add(userInput);
        responseCount++;

        // Normalize input
        String normalizedInput = nlpProcessor.normalizeText(userInput);

        // Analyze user intent and sentiment
        String intent = nlpProcessor.extractIntent(normalizedInput);
        String sentiment = nlpProcessor.analyzeSentiment(normalizedInput);

        // Try to find FAQ match first
        String faqResponse = faqTrainer.findBestMatch(normalizedInput);
        if (faqResponse != null) {
            return addConfidenceContext(faqResponse, "high");
        }

        // Rule-based response generation
        String response = generateRuleBasedResponse(intent, sentiment, normalizedInput);

        return response;
    }

    /**
     * Generates responses based on intent and rules.
     */
    private String generateRuleBasedResponse(String intent, String sentiment, String input) {
        String baseResponse;

        switch (intent) {
            case "greeting":
                String[] greetings = {
                    "Hello! Welcome to our AI Assistant. How can I help you today?",
                    "Hi! I'm here to assist you. What's your question?",
                    "Welcome! Ask me anything about our services or general FAQs."
                };
                baseResponse = greetings[responseCount % greetings.length];
                break;

            case "gratitude":
                return "You're welcome! Is there anything else I can help you with?";

            case "help_request":
                if (input.toLowerCase().contains("integrat")) {
                    baseResponse = "Our integration process is straightforward. We support REST APIs and webhooks. " +
                        "Would you like more detailed information about specific integrations?";
                } else if (input.toLowerCase().contains("price") || input.toLowerCase().contains("cost")) {
                    baseResponse = "Our pricing starts at $99/month for basic plans. Custom enterprise solutions are also available. " +
                        "Would you like information about specific features?";
                } else if (input.toLowerCase().contains("security")) {
                    baseResponse = "We take security seriously with enterprise-grade encryption (AES-256) and GDPR compliance. " +
                        "Your data is protected with industry-leading standards.";
                } else {
                    baseResponse = "I'd be happy to help! Could you provide more details about what you need assistance with?";
                }
                break;

            case "pricing":
                baseResponse = "We offer flexible pricing: Basic ($99/mo), Professional ($299/mo), and Enterprise (custom). " +
                    "All plans include API access and 24/7 support. Would you like more details?";
                break;

            case "contact":
                baseResponse = "You can reach our support team at support@example.com or call 1-800-AI-HELP. " +
                    "Business hours: Monday-Friday, 9 AM - 6 PM EST.";
                break;

            case "onboarding":
                baseResponse = "Getting started is easy! Sign up for a free account, explore our dashboard, " +
                    "and integrate our API into your platform. We'll guide you through every step. Ready to begin?";
                break;

            default:
                baseResponse = generateSmartResponse(input);
                break;
        }

        // Adjust response based on sentiment
        if (sentiment.equals("negative")) {
            baseResponse += " I apologize if you're experiencing any issues. How can I make this better for you?";
        } else if (sentiment.equals("positive")) {
            baseResponse = "Great! " + baseResponse;
        }

        return baseResponse;
    }

    /**
     * Generates smart contextual responses for general queries.
     */
    private String generateSmartResponse(String input) {
        // Keyword-based smart responses
        if (containsAnyKeyword(input, "api", "code", "development", "feature")) {
            return "That's a great technical question! Our API documentation is comprehensive and available on our developer portal. " +
                "We support multiple languages and have code examples for all major platforms.";
        }

        if (containsAnyKeyword(input, "security", "safe", "protect", "encrypt")) {
            return "Security is our top priority. We use enterprise-grade encryption, comply with GDPR and CCPA, " +
                "and conduct regular security audits. Your data is in safe hands.";
        }

        if (containsAnyKeyword(input, "performance", "fast", "speed", "slow")) {
            return "Our infrastructure is optimized for performance with 99.9% uptime guarantee. " +
                "We use CDN and caching to ensure fast response times.";
        }

        if (containsAnyKeyword(input, "customize", "custom", "modification", "configure")) {
            return "We offer extensive customization options! You can tailor responses, appearance, and behavior to match your needs. " +
                "Our team can help with complex custom implementations.";
        }

        // Return a helpful generic response
        return "That's an interesting question! While I don't have specific information about that topic, " +
            "I recommend checking our documentation or contacting our support team for detailed assistance.";
    }

    /**
     * Helper method to check for keywords.
     */
    private boolean containsAnyKeyword(String text, String... keywords) {
        String lower = text.toLowerCase();
        for (String keyword : keywords) {
            if (lower.contains(keyword.toLowerCase())) {
                return true;
            }
        }
        return false;
    }

    /**
     * Adds confidence level indication to the response.
     */
    private String addConfidenceContext(String response, String confidence) {
        // In production, this could be enhanced to show confidence levels
        // For now, we'll just return the response as is
        return response;
    }

    /**
     * Checks if FAQ data has been loaded.
     */
    public boolean isFaqLoaded() {
        return faqTrainer.hasFaqData();
    }

    /**
     * Gets conversation history.
     */
    public List<String> getConversationHistory() {
        return new ArrayList<>(conversationHistory);
    }

    /**
     * Gets chatbot statistics and diagnostics.
     */
    public Map<String, Object> getDiagnostics() {
        Map<String, Object> diagnostics = new HashMap<>();
        diagnostics.put("response_count", responseCount);
        diagnostics.put("conversation_length", conversationHistory.size());
        diagnostics.put("faq_database_size", faqTrainer.size());
        diagnostics.put("faq_statistics", faqTrainer.getStatistics());
        diagnostics.put("timestamp", System.currentTimeMillis());
        return diagnostics;
    }

    /**
     * Clears conversation history.
     */
    public void clearHistory() {
        conversationHistory.clear();
        responseCount = 0;
    }

    /**
     * Gets FAQ data for reference.
     */
    public Map<String, Object> getFaqReference() {
        Map<String, Object> reference = new HashMap<>();
        reference.put("total_questions", faqTrainer.size());
        reference.put("sample_questions", faqTrainer.getAllQuestions().stream()
            .limit(5)
            .toList());
        return reference;
    }

    /**
     * Advanced method to find multiple matches for query.
     */
    public List<String> findSimilarQuestions(String query, int limit) {
        List<Map<String, Object>> matches = faqTrainer.findMatches(query, limit);
        List<String> results = new ArrayList<>();
        for (Map<String, Object> match : matches) {
            double confidence = (double) match.get("confidence");
            String question = (String) match.get("question");
            results.add(String.format("[%.0f%% match] %s", confidence * 100, question));
        }
        return results;
    }
}
