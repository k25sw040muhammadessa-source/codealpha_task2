# Quick Start Guide for AI Chatbot

## One-Minute Setup

### Windows Users

1. **Double-click** `run.bat`
2. Wait for "Server running on: http://localhost:8080"
3. Open browser and visit `http://localhost:8080`
4. Start chatting! 🤖

### Mac/Linux Users

```bash
chmod +x run.sh
./run.sh
```

Then open `http://localhost:8080` in your browser.

## What Happens

1. Script checks for Java installation
2. Creates `target/classes` directory
3. Compiles all Java files
4. Loads FAQ training data (20+ Q&A pairs)
5. Starts REST API server on port 8080
6. Server is ready to handle requests

## Testing

Once you see "✓ Chatbot trained with FAQ data", try:

### In the Web Interface
- Type: "What services do you offer?"
- Type: "How much does it cost?"
- Type: "How can I contact support?"

### From Command Line (cURL)
```bash
curl -X POST http://localhost:8080/api/chat \
  -H "Content-Type: application/json" \
  -d '{"message":"Hello!"}'
```

## Troubleshooting

### "Port 8080 already in use"
- Find and close existing Java process
- Or modify PORT in `src/main/java/chatbot/ApiServer.java`

### "Java not found"
- Install Java 11+: https://www.oracle.com/java/technologies/downloads/
- Verify with: `java -version`

### "FAQ file not found"
- Ensure `src/main/resources/faq_data.txt` exists
- Check file path is correct

## Next Steps

1. ✅ Open web interface
2. ✅ Test with sample questions
3. ✅ Add custom FAQ data in `faq_data.txt`
4. ✅ Customize responses in `Chatbot.java`
5. ✅ Integrate into your application via `/api/chat` endpoint

Happy chatting! 🚀
