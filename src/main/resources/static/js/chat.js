document.addEventListener('DOMContentLoaded', function() {
    // Get DOM elements
    const chatMessages = document.getElementById('chat-messages');
    const userInput = document.getElementById('user-input');
    const sendBtn = document.getElementById('send-btn');

    // Check if elements exist
    if (!chatMessages || !userInput || !sendBtn) {
        console.error('Missing required DOM elements');
        return;
    }

    console.log('DOM elements found:', {
        chatMessages: !!chatMessages,
        userInput: !!userInput,
        sendBtn: !!sendBtn
    });

    // Add click event listener to button
    sendBtn.addEventListener('click', function(e) {
        console.log('Button click event listener added');
    });

    // Add mouseover event to check if button is clickable
    sendBtn.addEventListener('mouseover', function() {
        console.log('Button is clickable');
    });

    // Enhanced message display function
    function addMessage(message, isUser) {
        const messageDiv = document.createElement('div');
        messageDiv.className = `message ${isUser ? 'user-message' : 'bot-message'} visible`;
        
        // Create message content
        const messageContent = document.createElement('div');
        messageContent.className = 'message-content';
        
        // Format message text
        const formattedMessage = message
            .replace(/`([^`]+)`/g, '<code>$1</code>') // Inline code
            .replace(/```([^`]+)```/g, '<pre>$1</pre>') // Code blocks
            .replace(/\n/g, '<br>') // Newlines
            .replace(/\s{2,}/g, (spaces) => '<br>' + ' '.repeat(spaces.length - 1)); // Multiple spaces
        
        messageContent.innerHTML = formattedMessage;
        
        // Append content to message container
        messageDiv.appendChild(messageContent);
        
        // Add to chat messages
        chatMessages.appendChild(messageDiv);
        
        // Scroll to bottom
        chatMessages.scrollTop = chatMessages.scrollHeight;
    }

    // Add typing indicator
    function addTypingIndicator() {
        const typingDiv = document.createElement('div');
        typingDiv.className = 'message typing-indicator visible';
        typingDiv.innerHTML = `
            <div class="typing-dots">
                <span></span>
                <span></span>
                <span></span>
            </div>
        `;
        chatMessages.appendChild(typingDiv);
        chatMessages.scrollTop = chatMessages.scrollHeight;
        return typingDiv;
    }

    // Remove typing indicator
    function removeTypingIndicator(typingDiv) {
        typingDiv.remove();
    }

    // Handle sending message
    async function sendMessage(message) {
        try {
            console.log('Sending message:', message);
            const response = await fetch('/api/rag/query', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify({ question: message })
            });

            console.log('Response status:', response.status);
            
            if (!response.ok) {
                throw new Error(`HTTP error! status: ${response.status}`);
            }

            const data = await response.json();
            console.log('Server response:', data);
            return data.answer;
        } catch (error) {
            console.error('Error:', error);
            return 'Sorry, I encountered an error. Please try again.';
        }
    }

    // Handle send button click
    sendBtn.addEventListener('click', async function(e) {
        e.preventDefault();
        console.log('Send button clicked');
        
        const message = userInput.value.trim();
        if (!message) return;

        // Add user message
        addMessage(message, true);
        userInput.value = '';

        // Show typing indicator
        const typingDiv = addTypingIndicator();

        // Get bot response
        const response = await sendMessage(message);
        removeTypingIndicator(typingDiv);
        
        // Add bot response
        addMessage(response, false);
    });

    // Handle Enter key
    userInput.addEventListener('keypress', async function(e) {
        if (e.key === 'Enter' && !e.shiftKey) {
            e.preventDefault();
            console.log('Enter key pressed');
            
            const message = userInput.value.trim();
            if (!message) return;

            // Add user message
            addMessage(message, true);
            userInput.value = '';

            // Show typing indicator
            const typingDiv = addTypingIndicator();

            // Get bot response
            const response = await sendMessage(message);
            removeTypingIndicator(typingDiv);
            
            // Add bot response
            addMessage(response, false);
        }
    });

    // Auto-resize input
    userInput.addEventListener('input', () => {
        userInput.style.height = 'auto';
        userInput.style.height = userInput.scrollHeight + 'px';
    });

    // Initialize input height
    userInput.style.height = userInput.scrollHeight + 'px';
});
