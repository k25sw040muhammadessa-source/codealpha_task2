// Chatbot JavaScript Frontend
const chatMessages = document.getElementById('chatMessages');
const userInput = document.getElementById('userInput');
const sendBtn = document.getElementById('sendBtn');
const status = document.getElementById('status');
const connectionStatus = document.getElementById('connectionStatus');

const API_URL = 'http://localhost:8080/api';
let isWaitingForResponse = false;

// Initialize
document.addEventListener('DOMContentLoaded', () => {
    checkServerConnection();
    setupEventListeners();
    userInput.focus();
});

function setupEventListeners() {
    sendBtn.addEventListener('click', sendMessage);
    userInput.addEventListener('keypress', (e) => {
        if (e.key === 'Enter' && !isWaitingForResponse) {
            sendMessage();
        }
    });
}

function checkServerConnection() {
    fetch(`${API_URL}/health`)
        .then(response => response.json())
        .then(data => {
            connectionStatus.textContent = '🟢 Connected';
            connectionStatus.style.color = '#28a745';
        })
        .catch(() => {
            connectionStatus.textContent = '🔴 Disconnected';
            connectionStatus.style.color = '#dc3545';
            showNotification('Server not responding. Make sure the Java server is running on port 8080.');
        });
}

function sendMessage() {
    const message = userInput.value.trim();
    if (!message) return;

    // Add user message to chat
    addMessageToChat(message, 'user');
    userInput.value = '';
    isWaitingForResponse = true;
    sendBtn.disabled = true;
    status.textContent = 'Typing...';

    // Show typing indicator
    showTypingIndicator();

    // Send to server
    fetch(`${API_URL}/chat`, {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
        },
        body: JSON.stringify({ message: message })
    })
    .then(response => {
        if (!response.ok) throw new Error('Server error');
        return response.json();
    })
    .then(data => {
        removeTypingIndicator();
        addMessageToChat(data.response, 'bot');
        status.textContent = 'Ready';
        isWaitingForResponse = false;
        sendBtn.disabled = false;
        userInput.focus();
    })
    .catch(error => {
        console.error('Error:', error);
        removeTypingIndicator();
        addMessageToChat('Sorry, I encountered an error. Please make sure the server is running.', 'bot');
        status.textContent = 'Error';
        isWaitingForResponse = false;
        sendBtn.disabled = false;
        checkServerConnection();
    });
}

function addMessageToChat(message, sender) {
    const messageDiv = document.createElement('div');
    messageDiv.className = `message ${sender}-message`;

    const label = document.createElement('span');
    label.className = `${sender}-label`;
    label.textContent = sender === 'user' ? 'You:' : 'Bot:';

    const content = document.createElement('div');
    content.className = 'message-content';
    content.textContent = message;

    messageDiv.appendChild(label);
    messageDiv.appendChild(content);

    chatMessages.appendChild(messageDiv);
    scrollToBottom();
}

function showTypingIndicator() {
    const messageDiv = document.createElement('div');
    messageDiv.className = 'message bot-message';
    messageDiv.id = 'typingIndicator';

    const label = document.createElement('span');
    label.className = 'bot-label';
    label.textContent = 'Bot:';

    const content = document.createElement('div');
    content.className = 'typing-indicator';
    content.innerHTML = '<div class="typing-dot"></div><div class="typing-dot"></div><div class="typing-dot"></div>';

    messageDiv.appendChild(label);
    messageDiv.appendChild(content);
    chatMessages.appendChild(messageDiv);
    scrollToBottom();
}

function removeTypingIndicator() {
    const indicator = document.getElementById('typingIndicator');
    if (indicator) {
        indicator.remove();
    }
}

function scrollToBottom() {
    setTimeout(() => {
        chatMessages.scrollTop = chatMessages.scrollHeight;
    }, 0);
}

function showNotification(message) {
    const notification = document.createElement('div');
    notification.style.cssText = `
        position: fixed;
        top: 20px;
        right: 20px;
        background: #dc3545;
        color: white;
        padding: 15px 20px;
        border-radius: 8px;
        box-shadow: 0 4px 12px rgba(0, 0, 0, 0.2);
        z-index: 1000;
        font-size: 14px;
        animation: slideIn 0.3s ease-out;
    `;
    notification.textContent = message;
    document.body.appendChild(notification);

    setTimeout(() => {
        notification.style.animation = 'slideOut 0.3s ease-out';
        setTimeout(() => notification.remove(), 300);
    }, 3000);
}

// Periodically check server connection
setInterval(checkServerConnection, 30000);
