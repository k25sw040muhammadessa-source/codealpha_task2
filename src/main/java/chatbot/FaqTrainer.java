package chatbot;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;

/**
 * FAQ Trainer: Loads and manages frequently asked questions training data.
 * Provides methods to train the chatbot with Q&A pairs and manage training data.
 */
public class FaqTrainer {
    
    private Map<String, String> faqMap;
    private List<String> questions;
    private NlpProcessor nlpProcessor;

    public FaqTrainer() {
        this.faqMap = new LinkedHashMap<>();
        this.questions = new ArrayList<>();
        this.nlpProcessor = new NlpProcessor();
    }

    /**
     * Loads FAQ data from a file.
     * Expected format: Q: question text|A: answer text (one per line)
     */
    public boolean loadFaqFromFile(String filePath) {
        try {
            Path path = Paths.get(filePath);

            List<String> lines = null;

            if (Files.exists(path)) {
                System.out.println("Loading FAQ file from path: " + path.toAbsolutePath());
                lines = Files.readAllLines(path, StandardCharsets.UTF_8);
            } else {
                // try classpath resource fallback (e.g. when running from JAR)
                InputStream resourceStream = getClass().getClassLoader().getResourceAsStream(filePath);
                if (resourceStream == null) {
                    resourceStream = getClass().getClassLoader().getResourceAsStream("faq_data.txt");
                }

                if (resourceStream != null) {
                    System.out.println("Loading FAQ file from classpath resource: " + (filePath == null ? "faq_data.txt" : filePath));
                    try (BufferedReader reader = new BufferedReader(new InputStreamReader(resourceStream, StandardCharsets.UTF_8))) {
                        lines = new ArrayList<>();
                        String line;
                        while ((line = reader.readLine()) != null) {
                            lines.add(line);
                        }
                    }
                }
            }

            if (lines == null) {
                System.err.println("FAQ file not found: " + filePath + " (checked filesystem and classpath)");
                return false;
            }

            int loadedCount = 0;
            for (String line : lines) {
                if (line.trim().isEmpty()) {
                    continue;
                }

                if (line.contains("|")) {
                    String[] parts = line.split("\\|", 2);
                    if (parts.length == 2) {
                        String question = parts[0].replaceFirst("^Q:\\s*", "").trim();
                        String answer = parts[1].replaceFirst("^A:\\s*", "").trim();

                        if (!question.isEmpty() && !answer.isEmpty()) {
                            addFaq(question, answer);
                            loadedCount++;
                        }
                    }
                }
            }

            System.out.println("Loaded " + loadedCount + " FAQ items from: " + filePath);
            return true;
        } catch (IOException e) {
            System.err.println("Error reading FAQ file: " + e.getMessage());
            return false;
        }
    }

    /**
     * Adds a new Q&A pair to the training data.
     */
    public void addFaq(String question, String answer) {
        if (question != null && !question.isEmpty() && answer != null && !answer.isEmpty()) {
            String normalizedQuestion = nlpProcessor.normalizeText(question).toLowerCase();
            faqMap.put(normalizedQuestion, answer);
            questions.add(normalizedQuestion);
        }
    }

    /**
     * Checks if FAQ data has been loaded.
     */
    public boolean hasFaqData() {
        return !faqMap.isEmpty();
    }

    /**
     * Finds the best matching answer for a query using NLP similarity.
     */
    public String findBestMatch(String query) {
        if (query == null || query.isEmpty() || faqMap.isEmpty()) {
            return null;
        }

        String normalizedQuery = nlpProcessor.normalizeText(query).toLowerCase();
        double bestSimilarity = 0.0;
        String bestAnswer = null;

        // Calculate similarity for each FAQ question
        for (String faqQuestion : questions) {
            double similarity = nlpProcessor.calculateSimilarity(normalizedQuery, faqQuestion);
            
            if (similarity > bestSimilarity) {
                bestSimilarity = similarity;
                bestAnswer = faqMap.get(faqQuestion);
            }
        }

        // Return answer only if similarity exceeds threshold
        if (bestSimilarity >= NlpProcessor.SIMILARITY_THRESHOLD) {
            return bestAnswer;
        }

        return null;
    }

    /**
     * Finds all matching answers with confidence scores.
     */
    public List<Map<String, Object>> findMatches(String query, int topN) {
        List<Map<String, Object>> matches = new ArrayList<>();
        
        if (query == null || query.isEmpty() || faqMap.isEmpty()) {
            return matches;
        }

        String normalizedQuery = nlpProcessor.normalizeText(query).toLowerCase();

        // Calculate similarity for each FAQ question
        for (String faqQuestion : questions) {
            double similarity = nlpProcessor.calculateSimilarity(normalizedQuery, faqQuestion);
            
            if (similarity >= 0.3) {
                Map<String, Object> match = new HashMap<>();
                match.put("question", faqQuestion);
                match.put("answer", faqMap.get(faqQuestion));
                match.put("confidence", Math.round(similarity * 100.0) / 100.0);
                matches.add(match);
            }
        }

        // Sort by confidence (similarity) descending
        matches.sort((a, b) -> Double.compare(
            (double) b.get("confidence"),
            (double) a.get("confidence")
        ));

        // Return top N matches
        return matches.stream()
            .limit(topN)
            .collect(ArrayList::new, List::add, List::addAll);
    }

    /**
     * Gets statistics about the training data.
     */
    public Map<String, Object> getStatistics() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("total_questions", faqMap.size());
        stats.put("avg_answer_length", faqMap.values().stream()
            .mapToInt(String::length)
            .average()
            .orElse(0.0));
        stats.put("avg_question_length", questions.stream()
            .mapToInt(String::length)
            .average()
            .orElse(0.0));
        return stats;
    }

    /**
     * Gets all FAQ questions for learning/reference.
     */
    public List<String> getAllQuestions() {
        return new ArrayList<>(questions);
    }

    /**
     * Gets answer for a specific question.
     */
    public String getAnswer(String question) {
        String normalized = nlpProcessor.normalizeText(question).toLowerCase();
        return faqMap.get(normalized);
    }

    /**
     * Clears all FAQ data.
     */
    public void clear() {
        faqMap.clear();
        questions.clear();
    }

    /**
     * Gets the size of FAQ dataset.
     */
    public int size() {
        return faqMap.size();
    }

    /**
     * Exports current FAQ data to a file.
     */
    public boolean exportFaqToFile(String filePath) {
        try {
            List<String> lines = new ArrayList<>();
            for (String question : questions) {
                String answer = faqMap.get(question);
                lines.add("Q: " + question + "|A: " + answer);
            }
            
            Files.write(Paths.get(filePath), lines, StandardCharsets.UTF_8);
            System.out.println("Exported " + lines.size() + " FAQ items to: " + filePath);
            return true;
        } catch (IOException e) {
            System.err.println("Error exporting FAQ file: " + e.getMessage());
            return false;
        }
    }
}
