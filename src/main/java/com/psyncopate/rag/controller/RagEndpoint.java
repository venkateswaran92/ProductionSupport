package com.psyncopate.rag.controller;


import com.psyncopate.rag.dto.RagRequest;
import com.psyncopate.rag.dto.RagResponse;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * REST controller that exposes an endpoint for Retrieval-Augmented Generation (RAG) using Spring AI.
 * <p>
 * The controller accepts a user query, retrieves relevant documents from the {@link VectorStore},
 * and sends a prompt to the {@link ChatClient} to generate a context-aware answer.
 */
@RestController
@RequestMapping("/api/rag")
public class RagEndpoint {

    private final ChatClient chatClient;
    private final VectorStore vectorStore;
    private final Resource promptTemplate;

    /**
     * Constructs the RagEndpoint with required dependencies.
     *
     * @param chatClient     the AI chat client used to process prompts
     * @param vectorStore    the vector store used to retrieve similar documents
     * @param promptTemplate the resource template for structuring AI prompts
     */
    public RagEndpoint(ChatClient chatClient, VectorStore vectorStore,
                       @Value("classpath:/prompts/prompt.st") Resource promptTemplate) {
        this.chatClient = chatClient;
        this.vectorStore = vectorStore;
        this.promptTemplate = promptTemplate;
    }

    /**
     * POST endpoint to submit a question and receive an AI-generated answer based on relevant documents.
     *
     * @param request the user's question payload
     * @return the AI-generated answer and supporting documents
     */
    @PostMapping("/query")
    public RagResponse query(@RequestBody RagRequest request) {
        try {
            List<String> relevantDocuments = findSimilarDocuments(request.question());
            
            // Build prompt parameters with user question and retrieved documents
            Map<String, Object> promptParameters = Map.of(
                    "input", request.question(),
                    "documents", String.join("\n", relevantDocuments)
            );

            // Generate the prompt and get the response from the chat client
            String answer = chatClient
                    .prompt(new PromptTemplate(promptTemplate).create(promptParameters))
                    .call()
                    .content();

            // Return response with answer and documents
            return new RagResponse(answer, relevantDocuments);
        } catch (Exception e) {
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Failed to process request: " + e.getMessage()
            );
        }
    }


    /**
     * Helper method to search the vector store for documents similar to the given question.
     *
     * @param question the user's query
     * @return a list of formatted document contents related to the query
     */
    private List<String> findSimilarDocuments(String question) {
        SearchRequest searchRequest = SearchRequest.builder()
                .query(question)
                .topK(3) // retrieve top 3 matches
                .build();

        List<Document> similarDocuments = vectorStore.similaritySearch(searchRequest);

        // Extract the textual content of each similar document
        return similarDocuments.stream()
                .map(Document::getFormattedContent)
                .collect(Collectors.toList());
    }
}
