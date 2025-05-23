package com.psyncopate.rag.shell;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellOption;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * A Spring Shell command component that allows users to interact
 * with an AI assistant through the command line.
 * <p>
 * This component uses:
 * <ul>
 *     <li>{@link ChatClient} to send prompts and receive responses</li>
 *     <li>{@link VectorStore} to retrieve relevant documents based on user input</li>
 *     <li>{@link PromptTemplate} loaded from a resource to structure the assistant prompt</li>
 * </ul>
 */
@ShellComponent
public class SpringAssistantCommand {

    private final ChatClient chatClient;
    private final VectorStore vectorStore;

    @Value("classpath:/prompts/prompt.st")
    private Resource sbPromptTemplate;

    @Autowired
    public SpringAssistantCommand(ChatClient chatClient, VectorStore vectorStore) {
        this.chatClient = chatClient;
        this.vectorStore = vectorStore;
    }

    /**
     * Shell command that allows the user to ask a question.
     *
     * @param message the question or input from the user
     * @return the assistant's response as a string
     */
    @ShellMethod(key = "q", value = "Ask a question to the assistant")
    public String question(@ShellOption(defaultValue = "") String message) {
        // Load prompt template and prepare parameters
        PromptTemplate promptTemplate = new PromptTemplate(sbPromptTemplate);
        Map<String, Object> promptParameters = new HashMap<>();

        promptParameters.put("input", message);
        promptParameters.put("documents", String.join("\n", findSimilarDocuments(message)));

        // Create the final prompt with parameters
        Prompt prompt = promptTemplate.create(promptParameters);

        // Send prompt to chat client and return the AI response
        return chatClient.prompt(prompt).call().content();
    }

    /**
     * Finds similar documents from the vector store based on the user's question.
     *
     * @param message the input query
     * @return a list of top-k similar document contents (formatted as strings)
     */
    private List<String> findSimilarDocuments(String message) {
        SearchRequest searchRequest = SearchRequest.builder()
                .query(message)
                .topK(3)
                .build();

        List<Document> similarDocuments = vectorStore.similaritySearch(searchRequest);

        // Extract formatted content of each document
        return similarDocuments.stream()
                .map(Document::getFormattedContent)
                .collect(Collectors.toList());
    }
}
