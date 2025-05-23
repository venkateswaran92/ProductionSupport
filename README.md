# ProductionSupport - AI-Powered Document Assistant

![Application Screenshot](https://via.placeholder.com/1200x600/2d3748/ffffff?text=ProductionSupport+UI+Screenshot)

A sophisticated RAG (Retrieval-Augmented Generation) system built with Spring Boot and Spring AI, featuring an intuitive web interface for interacting with your documents through natural language queries.

## ✨ Features

### User Interface
- 🎨 **Modern, Responsive Design**
  - Clean, intuitive interface that works on all devices
  - Dark/Light theme support
  - Animated transitions and loading states

- 💬 **Interactive Chat Interface**
  - Real-time message streaming
  - Typing indicators
  - Message timestamps
  - Copy-to-clipboard functionality
  - Support for markdown formatting in responses

- 📂 **Document Management**
  - Drag-and-drop document upload
  - Document preview
  - File type indicators
  - Upload progress tracking

- 🔍 **Search & Navigation**
  - Instant document search
  - Filter by document type
  - Search history
  - Suggested queries

### Technical Features
- 🤖 AI-Powered Document Analysis
- 📊 Vector-based semantic search
- ⚡ Real-time updates via SSE
- 🔄 Automatic document processing
- 🔒 Secure API key management

## Prerequisites

- Java 17 or later
- Maven
- PostgreSQL 14 or later with pgvector extension
- Docker (optional, for development)
- OpenAI API key

## 🖥️ UI Components

### 1. Main Chat Interface
- **Header Bar**
  - Application logo and title
  - Theme toggle (light/dark)
  - User profile/account menu

- **Sidebar**
  - Document library
  - Upload new documents
  - Recent searches
  - Settings

- **Chat Area**
  - Message history
  - Typing indicators
  - Message status (sent, delivered, read)
  - Context-aware suggestions

- **Input Area**
  - Rich text editor with formatting options
  - File attachment button
  - Voice input (coming soon)
  - Send button with loading state

### 2. Document Management
- **Document Grid/List View**
  - Thumbnail previews
  - File metadata
  - Quick actions (preview, download, delete)

- **Document Preview**
  - Full-page document viewer
  - Page navigation
  - Zoom controls
  - Text selection and highlighting

### 3. Settings Panel
- **Profile Settings**
  - Personal information
  - Notification preferences
  - Theme customization

- **AI Settings**
  - Model selection
  - Response length
  - Temperature control
  - API key management

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

### Web Interface Walkthrough

1. **First-Time Setup**
   - Sign up for an account or log in
   - Configure your API keys in Settings
   - Upload your first document

2. **Main Chat**
   - Type your question in the message box
   - Use `@` to reference specific documents
   - Click the paperclip icon to attach files
   - Press Enter to send or Shift+Enter for a new line

3. **Document Management**
   - Drag and drop files into the upload area
   - View document details by clicking on them
   - Search through your document library
   - Organize with tags and folders

4. **Advanced Features**
   - Use `/commands` for special actions
   - Bookmark important responses
   - Export chat history
   - Share conversations with team members

## 🎨 UI Customization

### Themes
- **Light Theme**: Clean, distraction-free interface
- **Dark Theme**: Reduced eye strain for extended use
- **High Contrast**: Improved accessibility

### Layout Options
- **Compact View**: See more content on screen
- **Reading Mode**: Focus on the content
- **Split Screen**: View documents and chat side by side

## 📱 Mobile Experience
- Fully responsive design
- Touch-optimized controls
- Offline support (coming soon)
- Push notifications

## 🔄 Real-time Updates
- See when others are typing
- Live document updates
- Synchronized across devices
- Notification center for important events

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