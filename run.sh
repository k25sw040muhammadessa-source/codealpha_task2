#!/bin/bash
# Quick Start Script for AI Chatbot

echo "=================================="
echo "AI Chatbot - Quick Start"
echo "=================================="
echo ""

# Check Java installation
if ! command -v java &> /dev/null; then
    echo "Error: Java is not installed"
    echo "Please install Java 11 or higher"
    exit 1
fi

echo "Java version:"
java -version
echo ""

# Create target directory if it doesn't exist
mkdir -p target/classes

# Compile Java source files
echo "Compiling Java source files..."
javac -d target/classes src/main/java/chatbot/*.java

if [ $? -ne 0 ]; then
    echo "Compilation failed!"
    exit 1
fi

echo "✓ Compilation successful"
echo ""
echo "Starting AI Chatbot Server..."
echo ""

# Run the server
java -cp target/classes chatbot.ApiServer

# Keep script running if server stops
wait
