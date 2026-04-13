package chatbot;

import com.sun.net.httpserver.*;
import java.io.*;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.Executors;

/**
 * REST API Server for the AI Chatbot.
 * Provides HTTP endpoints for chatbot interaction and management.
 * Runs on http://localhost:8080
 */
public class ApiServer {
    
    private static final int PORT = 8080;
    
    private HttpServer server;
    private Chatbot chatbot;

    public ApiServer() {
        this.chatbot = new Chatbot();
    }

    /**
     * Starts the API server on the configured port.
     */
    public void start() {
        try {
            server = HttpServer.create(new InetSocketAddress(PORT), 0);
            server.setExecutor(Executors.newFixedThreadPool(10));

            // ✅ FIXED: All endpoints use anonymous HttpHandler classes (Java 7+ compatible)
            server.createContext("/api/chat", new HttpHandler() {
                @Override
                public void handle(HttpExchange exchange) throws IOException {
                    handleChatRequest(exchange);
                }
            });
            
            server.createContext("/api/health", new HttpHandler() {
                @Override
                public void handle(HttpExchange exchange) throws IOException {
                    handleHealthCheck(exchange);
                }
            });
            
            server.createContext("/api/diagnostics", new HttpHandler() {
                @Override
                public void handle(HttpExchange exchange) throws IOException {
                    handleDiagnostics(exchange);
                }
            });
            
            server.createContext("/api/train", new HttpHandler() {
                @Override
                public void handle(HttpExchange exchange) throws IOException {
                    handleTrain(exchange);
                }
            });
            
            server.createContext("/", new HttpHandler() {
                @Override
                public void handle(HttpExchange exchange) throws IOException {
                    handleRootRequest(exchange);
                }
            });

            server.start();
            System.out.println("================================================");
            System.out.println("✓ AI Chatbot Server Started Successfully!");
            System.out.println("================================================");
            System.out.println("Server running on: http://localhost:" + PORT);
            System.out.println("API Base: http://localhost:" + PORT + "/api/");
            System.out.println("Chat Endpoint: POST http://localhost:" + PORT + "/api/chat");
            System.out.println("Health Check: GET http://localhost:" + PORT + "/api/health");
            System.out.println("Diagnostics: GET http://localhost:" + PORT + "/api/diagnostics");
            System.out.println("================================================");
            
            // Train chatbot with FAQ data
            String[] faqPaths = {
                "faq_data.txt",                     // CWD fallback
                "src/main/resources/faq_data.txt", // typical dev path
                "faq_data.txt"                     // classpath file name fallback (handled in FaqTrainer)
            };

            boolean trained = false;
            String loadedFrom = "";
            for (String path : faqPaths) {
                if (chatbot.trainWithFaqFile(path)) {
                    trained = true;
                    loadedFrom = path;
                    break;
                }
            }

            if (trained) {
                System.out.println("✓ Chatbot trained with FAQ data (source: " + loadedFrom + ")");
            } else {
                System.out.println("⚠ Could not load FAQ file from file system or classpath. Using rule-based responses only.");
            }
            System.out.println("================================================\n");

        } catch (IOException e) {
            System.err.println("Error starting server: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Handles chat requests from the frontend.
     * POST /api/chat
     * Body: {"message": "user_input"}
     * Response: {"response": "chatbot_response"}
     */
    private void handleChatRequest(HttpExchange exchange) throws IOException {
        String method = exchange.getRequestMethod().toUpperCase();
        if (method.equals("OPTIONS")) {
            sendResponse(exchange, 204, "");
            return;
        }
        if (!method.equals("POST")) {
            sendResponse(exchange, 405, "{\"error\": \"Method not allowed. Use POST.\"}");
            return;
        }

        try {
            // Read request body
            InputStreamReader isr = new InputStreamReader(exchange.getRequestBody(), StandardCharsets.UTF_8);
            BufferedReader reader = new BufferedReader(isr);
            StringBuilder body = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                body.append(line);
            }
            reader.close();

            // Parse JSON (simple parsing without external library)
            String userMessage = extractJsonValue(body.toString(), "message");
            
            if (userMessage == null || userMessage.trim().isEmpty()) {
                sendResponse(exchange, 400, "{\"error\": \"Missing or empty 'message' field\"}");
                return;
            }

            // Process message through chatbot
            String botResponse = chatbot.processInput(userMessage.trim());

            // Send response
            String jsonResponse = "{\"response\": " + jsonEscape(botResponse) + "}";
            sendResponse(exchange, 200, jsonResponse);

        } catch (Exception e) {
            System.err.println("Error processing chat request: " + e.getMessage());
            e.printStackTrace();
            sendResponse(exchange, 500, "{\"error\": \"Internal server error\"}");
        }
    }

    /**
     * Health check endpoint for monitoring.
     * GET /api/health
     */
    private void handleHealthCheck(HttpExchange exchange) throws IOException {
        try {
            String response = "{"
                + "\"status\": \"healthy\","
                + "\"server\": \"AI Chatbot API\","
                + "\"version\": \"1.0\","
                + "\"timestamp\": " + System.currentTimeMillis() + ","
                + "\"uptime\": \"" + getUptime() + "\","
                + "\"faq_loaded\": " + chatbot.isFaqLoaded()
                + "}";
            sendResponse(exchange, 200, response);
        } catch (Exception e) {
            sendResponse(exchange, 500, "{\"error\": \"Health check failed\"}");
        }
    }

    /**
     * Diagnostics endpoint for debugging and monitoring.
     * GET /api/diagnostics
     */
    private void handleDiagnostics(HttpExchange exchange) throws IOException {
        try {
            Map<String, Object> diag = chatbot.getDiagnostics();
            String response = mapToJson(diag);
            sendResponse(exchange, 200, response);
        } catch (Exception e) {
            sendResponse(exchange, 500, "{\"error\": \"Diagnostics retrieval failed\"}");
        }
    }

    /**
     * Training endpoint for adding new FAQ data.
     * POST /api/train
     * Body: {"question": "q", "answer": "a"}
     */
    private void handleTrain(HttpExchange exchange) throws IOException {
        if (!exchange.getRequestMethod().equals("POST")) {
            sendResponse(exchange, 405, "{\"error\": \"Method not allowed. Use POST.\"}");
            return;
        }

        try {
            InputStreamReader isr = new InputStreamReader(exchange.getRequestBody(), StandardCharsets.UTF_8);
            BufferedReader reader = new BufferedReader(isr);
            StringBuilder body = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                body.append(line);
            }
            reader.close();

            String question = extractJsonValue(body.toString(), "question");
            String answer = extractJsonValue(body.toString(), "answer");

            if (question == null || question.trim().isEmpty() || answer == null || answer.trim().isEmpty()) {
                sendResponse(exchange, 400, "{\"error\": \"Missing 'question' or 'answer' field\"}");
                return;
            }

            chatbot.addTrainingData(question.trim(), answer.trim());
            
            String response = "{\"status\": \"success\", \"message\": \"Training data added successfully\"}";
            sendResponse(exchange, 200, response);

        } catch (Exception e) {
            System.err.println("Training error: " + e.getMessage());
            sendResponse(exchange, 500, "{\"error\": \"Training failed\"}");
        }
    }

    /**
     * Root endpoint with API documentation.
     */
    private void handleRootRequest(HttpExchange exchange) throws IOException {
        String path = exchange.getRequestURI().getPath();
        if (path.equals("/")) {
            String html = ""
                + "<!DOCTYPE html>"
                + "<html>"
                + "<head>"
                + "    <title>AI Chatbot API</title>"
                + "    <style>"
                + "        body { font-family: Arial, sans-serif; max-width: 800px; margin: 50px auto; padding: 20px; }"
                + "        h1 { color: #333; }"
                + "        .endpoint { background: #f5f5f5; padding: 15px; margin: 10px 0; border-radius: 5px; }"
                + "        code { background: #eee; padding: 2px 5px; border-radius: 3px; }"
                + "        a { color: #007bff; text-decoration: none; }"
                + "    </style>"
                + "</head>"
                + "<body>"
                + "    <h1>🤖 AI Chatbot API</h1>"
                + "    <p><strong>Server Status:</strong> <span style=\"color: green;\">✅ Running</span></p>"
                + "    <p><strong>Base URL:</strong> http://localhost:8080</p>"
                + "    "
                + "    <h2>📋 Endpoints</h2>"
                + "    <div class=\"endpoint\">"
                + "        <strong>POST /api/chat</strong><br>"
                + "        <code>{\"message\": \"Hello, how are you?\"}</code><br>"
                + "        <em>Response: {\"response\": \"I'm great! How can I help?\"}</em>"
                + "    </div>"
                + "    "
                + "    <div class=\"endpoint\">"
                + "        <strong>GET /api/health</strong><br>"
                + "        <em>Server health check</em>"
                + "    </div>"
                + "    "
                + "    <div class=\"endpoint\">"
                + "        <strong>GET /api/diagnostics</strong><br>"
                + "        <em>Chatbot statistics</em>"
                + "    </div>"
                + "    "
                + "    <div class=\"endpoint\">"
                + "        <strong>POST /api/train</strong><br>"
                + "        <code>{\"question\": \"What is Java?\", \"answer\": \"Java is a programming language.\"}</code>"
                + "    </div>"
                + "    "
                + "    <p><a href=\"chat.html\">🌐 Open Chat Interface</a> | "
                + "       <a href=\"/api/health\">🩺 Health Check</a></p>"
                + "</body>"
                + "</html>";
            exchange.getResponseHeaders().set("Content-Type", "text/html; charset=UTF-8");
            sendResponse(exchange, 200, html);
        } else {
            sendResponse(exchange, 404, "{\"error\": \"Endpoint not found\"}");
        }
    }

    /**
     * Sends HTTP response with CORS headers.
     */
    private void sendResponse(HttpExchange exchange, int statusCode, String response) throws IOException {
        byte[] bytes = response.getBytes(StandardCharsets.UTF_8);
        
        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");
        // ✅ FIXED: Use add() instead of set() for CORS (Line 187 issue)
        exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
        exchange.getResponseHeaders().add("Access-Control-Allow-Methods", "GET, POST, OPTIONS");
        exchange.getResponseHeaders().add("Access-Control-Allow-Headers", "Content-Type");
        
        exchange.sendResponseHeaders(statusCode, bytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }
    }

    /**
     * Extracts a JSON value from a string (simple implementation).
     */
    private String extractJsonValue(String json, String key) {
        try {
            String searchKey = "\"" + key + "\":";
            int startIndex = json.indexOf(searchKey);
            if (startIndex == -1) return null;

            // Skip to opening quote after key
            startIndex = json.indexOf("\"", startIndex + searchKey.length());
            if (startIndex == -1) return null;

            // Find closing quote (handle escaped quotes)
            int endIndex = startIndex + 1;
            while (endIndex < json.length()) {
                endIndex = json.indexOf("\"", endIndex);
                if (endIndex == -1) return null;
                
                // Check if escaped
                if (json.charAt(endIndex - 1) != '\\') {
                    break;
                }
                endIndex++;
            }
            
            return json.substring(startIndex + 1, endIndex);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Escapes a string for JSON output.
     */
    private String jsonEscape(String str) {
        if (str == null) return "null";
        return "\"" + str
            .replace("\\", "\\\\")
            .replace("\"", "\\\"")
            .replace("\n", "\\n")
            .replace("\r", "\\r")
            .replace("\t", "\\t")
            .replace("\b", "\\b")
            .replace("\f", "\\f") + "\"";
    }

    /**
     * Converts a map to JSON string (simple implementation).
     */
    private String mapToJson(Map<String, Object> map) {
        StringBuilder sb = new StringBuilder("{");
        boolean first = true;
        
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            if (!first) sb.append(",");
            sb.append("\"").append(entry.getKey()).append("\":");
            
            Object value = entry.getValue();
            if (value instanceof String) {
                sb.append(jsonEscape((String) value));
            } else if (value instanceof Number || value instanceof Boolean) {
                sb.append(value);
            } else {
                sb.append("\"").append(value).append("\"");
            }
            first = false;
        }
        
        sb.append("}");
        return sb.toString();
    }

    private String getUptime() {
        return String.format("%.2f seconds", (System.currentTimeMillis() - startTime) / 1000.0);
    }
    
    private long startTime = System.currentTimeMillis();

    /**
     * Stops the server.
     */
    public void stop() {
        if (server != null) {
            server.stop(0);
            System.out.println("Server stopped gracefully.");
        }
    }

    /**
     * Main entry point to start the API server.
     */
    public static void main(String[] args) {
        ApiServer apiServer = new ApiServer();
        apiServer.start();

        // Graceful shutdown
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("\nShutting down server...");
            apiServer.stop();
        }));
        
        System.out.println("Press Ctrl+C to stop the server.");
    }
}