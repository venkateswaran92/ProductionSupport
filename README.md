# ProductionSupport - Spring Boot AI Assistant

This project demonstrates a RAG (Retrieval-Augmented Generation) system using Spring Boot and Spring AI. It provides a web-based interface for users to ask questions and receive context-aware responses based on reference documents.

## Features

- Web-based chat interface with real-time responses
- Document-based AI assistant with RAG capabilities
- Spring AI integration for vector store and chat capabilities
- Modern UI with formatted responses (code blocks, links, etc.)
- Automatic document loading and processing
- Semantic search for relevant document retrieval
- Server-Sent Events (SSE) support for real-time updates

## Prerequisites

- Java 17 or later
- Maven
- PostgreSQL 14 or later with pgvector extension
- Docker (optional, for development)
- OpenAI API key

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

## Usage

1. **Web Interface**
   - Access the application at `http://localhost:8080`
   - Enter your question in the chat interface
   - View formatted responses with code blocks, links, and lists
   - Real-time typing indicator shows when the AI is processing

2. **Document Loading**
   - Place reference documents in `src/main/resources/docs/`
   - Supported formats: PDF and Excel
   - Documents are automatically loaded into the vector store

3. **Document Retrieval**
   - The system retrieves relevant documents based on semantic similarity
   - Top 3 most similar documents are used to provide context
   - Responses include both AI-generated answers and supporting documents

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