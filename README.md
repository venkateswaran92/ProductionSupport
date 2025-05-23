# Kafka Support Assistant

![Application Screenshot](https://via.placeholder.com/1200x600/2d3748/ffffff?text=Kafka+Support+Assistant)

A production support assistant for Kafka-related queries, built with Spring Boot and Spring AI. This application provides a clean, responsive chat interface for technical support and documentation.

## ✨ Features

### User Interface
- 🎨 **Clean, Responsive Design**
  - Mobile-friendly interface
  - Dark/Light theme support
  - Smooth animations and transitions

- 💬 **Chat Interface**
  - Real-time message display
  - Typing indicators
  - Message formatting support
  - Simple, focused layout

### Technical Features
- 🤖 AI-Powered Responses
- 🔍 Document-based knowledge
- 🔒 Secure authentication
- ⚡ Server-Sent Events for real-time updates

## Prerequisites

- Java 17 or later
- Maven
- PostgreSQL 14 or later with pgvector extension
- Docker (optional, for development)
- OpenAI API key

## 🖥️ UI Components

### 1. Chat Interface
- **Header**
  - Application title
  - Theme toggle
  - User menu with sign-out option

- **Message Area**
  - Welcome message with quick action buttons
  - Message history
  - Typing indicators
  - Formatted message display

- **Input Area**
  - Simple text input
  - Send button
  - Basic message formatting

### 2. User Authentication
- Login page
- Secure session management
- User greeting in the header

## Project Structure

```
src/main/java/com/psyncopate/rag/rag/
├── controller/                 # Web controllers
│   ├── ChatController.java    # Main web controller
│   └── RagController.java     # RAG API controller
├── dto/                       # Data transfer objects
│   ├── RagRequest.java        # Request DTO
│   └── RagResponse.java       # Response DTO
└── service/                   # Service layer implementation
├── static/                    # Frontend static files
│   ├── css/                   # Stylesheets
│   ├── js/                    # JavaScript files
│   └── images/                # Image assets
└── templates/                 # Thymeleaf templates
    ├── index.html            # Main template
    └── fragments/            # Reusable UI components
```

## Configuration

The project uses `application.yml` for configuration. Key settings include:

```yaml
spring:
  application:
    name: ragExample

ai:
  openai:
    api-key: YOUR_API_KEY      # Replace with your OpenAI API key
    model: gpt-4
    temperature: 0.7
    max-tokens: 2000

vectorstore:
  pgvector:
    index-type: HNSW
    distance-type: COSINE_DISTANCE
    dimension: 1536
```

## Setup Instructions

1. **Database Setup**
   ```sql
   -- Create database
   CREATE DATABASE ragExample;
   
   -- Enable pgvector extension
   CREATE EXTENSION IF NOT EXISTS vector;
   ```

2. **Dependencies**
   - Add OpenAI API key to `application.yml`
   - Configure PostgreSQL connection settings
   - Ensure Docker is installed if using Docker Compose

3. **Build and Run**
   ```bash
   # Build the project
   mvn clean install
   
   # Run the application
   mvn spring-boot:run
   ```

## 🚀 Getting Started

### Using the Application

1. **Login**
   - Enter your credentials on the login page
   - You'll be redirected to the chat interface

2. **Start Chatting**
   - Type your message in the input field
   - Press Enter to send
   - View responses in the message area

3. **Quick Actions**
   - Use the suggestion chips for common queries
   - Toggle between light/dark theme
   - Sign out from the user menu

## 🎨 Interface

### Themes
- **Dark Theme**: Default theme for better visibility
- **Light Theme**: Alternative light color scheme

### Responsive Design
- Adapts to different screen sizes
- Touch-friendly controls
- Optimized for both desktop and mobile use

## Components

1. **ReferenceDocsLoader**
   - Loads PDF and Excel documents into the vector store
   - Processes documents at application startup
   - Uses Apache POI for Excel processing
   - Uses PDF reader for PDF processing

2. **SpringAssistantCommand**
   - Implements the command-line interface
   - Uses OpenAI's GPT-4 for response generation
   - Retrieves relevant documents using vector similarity search
   - Combines document context with AI generation

## Security Notes

- Keep your OpenAI API key secure
- Consider using environment variables for sensitive configurations
- Never commit API keys to version control

## Development

The project uses Docker Compose for development. To start the development environment:

```bash
# Start Docker containers
mvn spring-boot:run

# Stop containers when done
# (Note: lifecycle-management is set to start_only, so containers won't stop automatically)
```