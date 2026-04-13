package chatbot;

import java.util.*;
import java.util.regex.*;
import java.util.stream.Collectors;

/**
 * Natural Language Processing (NLP) module for text analysis and similarity matching.
 * Implements basic NLP techniques including tokenization, stemming, and similarity scoring.
 */
public class NlpProcessor {
    
    private static final Map<String, String> STEM_MAP = new HashMap<>();
    private static final Set<String> STOP_WORDS = new HashSet<>();
    public static final double SIMILARITY_THRESHOLD = 0.5;

    static {
        // Initialize stemming rules (simplified Porter Stemming)
        STEM_MAP.put("running", "run");
        STEM_MAP.put("runs", "run");
        STEM_MAP.put("ran", "run");
        STEM_MAP.put("working", "work");
        STEM_MAP.put("works", "work");
        STEM_MAP.put("helping", "help");
        STEM_MAP.put("helps", "help");
        STEM_MAP.put("integration", "integrat");
        STEM_MAP.put("integrated", "integrat");
        STEM_MAP.put("offering", "offer");
        STEM_MAP.put("offered", "offer");
        STEM_MAP.put("services", "servic");
        STEM_MAP.put("support", "support");
        STEM_MAP.put("supported", "support");
        STEM_MAP.put("security", "secur");
        STEM_MAP.put("secured", "secur");
        STEM_MAP.put("training", "train");
        STEM_MAP.put("trained", "train");
        
        // Common stop words to ignore
        Collections.addAll(STOP_WORDS, 
            "a", "an", "and", "are", "as", "at", "be", "but", "by", "for", "from",
            "has", "he", "in", "is", "it", "its", "of", "on", "or", "that", "the",
            "to", "was", "will", "with", "i", "me", "you", "we", "us", "can", "do",
            "what", "how", "when", "where", "why", "if", "does", "did", "have", "had"
        );
    }

    /**
     * Tokenizes input text into words.
     */
    public List<String> tokenize(String text) {
        if (text == null || text.isEmpty()) {
            return new ArrayList<>();
        }
        
        return Arrays.asList(text.toLowerCase()
            .replaceAll("[^a-z0-9\\s]", "")
            .split("\\s+"))
            .stream()
            .filter(token -> !token.isEmpty())
            .collect(Collectors.toList());
    }

    /**
     * Removes common stop words from tokens.
     */
    public List<String> removeStopWords(List<String> tokens) {
        return tokens.stream()
            .filter(token -> !STOP_WORDS.contains(token))
            .collect(Collectors.toList());
    }

    /**
     * Applies stemming to reduce words to their root form.
     */
    public String stem(String word) {
        String lower = word.toLowerCase();
        
        // Check stem map first
        if (STEM_MAP.containsKey(lower)) {
            return STEM_MAP.get(lower);
        }
        
        // Simple suffix removal rules
        if (lower.endsWith("ing")) {
            return lower.substring(0, lower.length() - 3);
        }
        if (lower.endsWith("ed")) {
            return lower.substring(0, lower.length() - 2);
        }
        if (lower.endsWith("s") && lower.length() > 1) {
            return lower.substring(0, lower.length() - 1);
        }
        if (lower.endsWith("es") && lower.length() > 2) {
            return lower.substring(0, lower.length() - 2);
        }
        
        return lower;
    }

    /**
     * Processes text: tokenize, remove stop words, and stem.
     */
    public List<String> processText(String text) {
        List<String> tokens = tokenize(text);
        List<String> filtered = removeStopWords(tokens);
        return filtered.stream()
            .map(this::stem)
            .collect(Collectors.toList());
    }

    /**
     * Calculates Jaccard similarity between two texts.
     * Returns a value between 0 and 1, where 1 is perfect match.
     */
    public double calculateSimilarity(String text1, String text2) {
        List<String> tokens1 = new HashSet<>(processText(text1)).stream().collect(Collectors.toList());
        List<String> tokens2 = new HashSet<>(processText(text2)).stream().collect(Collectors.toList());

        if (tokens1.isEmpty() || tokens2.isEmpty()) {
            return 0.0;
        }

        Set<String> intersection = new HashSet<>(tokens1);
        intersection.retainAll(tokens2);

        Set<String> union = new HashSet<>(tokens1);
        union.addAll(tokens2);

        return (double) intersection.size() / union.size();
    }

    /**
     * Checks if text contains specific keywords.
     */
    public boolean containsKeywords(String text, String... keywords) {
        String lower = text.toLowerCase();
        for (String keyword : keywords) {
            if (lower.contains(keyword.toLowerCase())) {
                return true;
            }
        }
        return false;
    }

    /**
     * Extracts intent from user input.
     */
    public String extractIntent(String text) {
        String lower = text.toLowerCase();

        if (containsKeywords(lower, "thanks", "thank you", "appreciate", "perfect", "great")) {
            return "gratitude";
        }
        if (containsKeywords(lower, "help", "can you", "how to", "how do", "assist")) {
            return "help_request";
        }
        if (containsKeywords(lower, "price", "cost", "pay", "charge", "fee")) {
            return "pricing";
        }
        if (containsKeywords(lower, "contact", "reach", "call", "phone", "email")) {
            return "contact";
        }
        if (containsKeywords(lower, "start", "begin", "get started", "signup", "register")) {
            return "onboarding";
        }
        if (containsKeywords(lower, "hello", "hi", "hey", "what's up", "greetings")) {
            return "greeting";
        }
        
        return "general_query";
    }

    /**
     * Finds the sentiment of the text (positive, negative, neutral).
     */
    public String analyzeSentiment(String text) {
        String lower = text.toLowerCase();
        
        int positiveScore = 0;
        int negativeScore = 0;

        String[] positiveWords = {"good", "great", "excellent", "amazing", "love", "best", "happy", "perfect", "wonderful"};
        String[] negativeWords = {"bad", "terrible", "hate", "worst", "poor", "awful", "angry", "broken", "issue"};

        for (String word : positiveWords) {
            if (lower.contains(word)) positiveScore++;
        }
        for (String word : negativeWords) {
            if (lower.contains(word)) negativeScore++;
        }

        if (positiveScore > negativeScore) {
            return "positive";
        } else if (negativeScore > positiveScore) {
            return "negative";
        } else {
            return "neutral";
        }
    }

    /**
     * Normalizes text for better processing.
     */
    public String normalizeText(String text) {
        if (text == null) {
            return "";
        }
        
        return text.trim()
            .replaceAll("\\s+", " ")
            .replaceAll("[^a-zA-Z0-9\\s?!.]", "");
    }

    /**
     * Extracts named entities (key information) from text.
     */
    public List<String> extractEntities(String text) {
        List<String> entities = new ArrayList<>();
        
        // Extract email addresses
        Pattern emailPattern = Pattern.compile("[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}");
        Matcher emailMatcher = emailPattern.matcher(text);
        while (emailMatcher.find()) {
            entities.add(emailMatcher.group());
        }

        // Extract phone numbers
        Pattern phonePattern = Pattern.compile("\\b\\d{3}[-.]?\\d{3}[-.]?\\d{4}\\b");
        Matcher phoneMatcher = phonePattern.matcher(text);
        while (phoneMatcher.find()) {
            entities.add(phoneMatcher.group());
        }

        // Extract numbers
        Pattern numberPattern = Pattern.compile("\\b\\d+\\b");
        Matcher numberMatcher = numberPattern.matcher(text);
        while (numberMatcher.find()) {
            entities.add(numberMatcher.group());
        }

        return entities;
    }

    /**
     * Checks text readability and language statistics.
     */
    public Map<String, Object> analyzeText(String text) {
        Map<String, Object> analysis = new HashMap<>();
        
        List<String> tokens = tokenize(text);
        List<String> words = removeStopWords(tokens);
        
        analysis.put("total_words", tokens.size());
        analysis.put("unique_words", new HashSet<>(tokens).size());
        analysis.put("meaningful_words", words.size());
        analysis.put("average_word_length", tokens.isEmpty() ? 0 : 
            (double) tokens.stream().mapToInt(String::length).sum() / tokens.size());
        analysis.put("sentiment", analyzeSentiment(text));
        analysis.put("entities", extractEntities(text));
        
        return analysis;
    }
}
