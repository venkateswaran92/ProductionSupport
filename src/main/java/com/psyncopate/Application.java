package com.psyncopate;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

/**
 * Entry point for the RAG (Retrieval-Augmented Generation) Spring Boot application.
 *
 * This application integrates OpenAI's GPT model using Spring AI, and provides
 * endpoints and shell commands to query documents retrieved from a Vector Store.
 */
@SpringBootApplication
public class Application {

	/**
	 * Main method to bootstrap the Spring Boot application.
	 *
	 * @param args Command-line arguments
	 */
	public static void main(String[] args) {
		SpringApplication.run(Application.class, args);
	}

	/**
	 * Defines a {@link ChatClient} bean using the configured {@link OpenAiChatModel}.
	 * This ChatClient will be used to interact with the OpenAI API for chat-based responses.
	 *
	 * @param chatModel the OpenAI chat model configured via Spring Boot
	 * @return a {@link ChatClient} instance
	 */
	@Bean
	public ChatClient openAiChatClient(OpenAiChatModel chatModel) {
		return ChatClient.create(chatModel);
	}
}
