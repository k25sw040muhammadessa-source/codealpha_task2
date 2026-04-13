@echo off
REM Quick Start Script for AI Chatbot (Windows)

echo ==================================
echo AI Chatbot - Quick Start
echo ==================================
echo.

REM Check Java installation
java -version >nul 2>&1
if errorlevel 1 (
    echo Error: Java is not installed
    echo Please install Java 11 or higher
    pause
    exit /b 1
)

echo Java detected:
java -version
echo.

REM Create target directory if it doesn't exist
if not exist "target\classes" mkdir target\classes

REM Compile Java source files
echo Compiling Java source files...
javac -d target\classes src\main\java\chatbot\*.java

if errorlevel 1 (
    echo Compilation failed!
    pause
    exit /b 1
)

echo Compilation successful
echo.
echo Starting AI Chatbot Server...
echo.

REM Run the server
java -cp target\classes chatbot.ApiServer

pause
