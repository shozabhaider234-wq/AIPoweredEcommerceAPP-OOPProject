package com.ecommerce.ai.chatbot;

import com.ecommerce.ai.tools.EcommerceAiTools;
import com.ecommerce.dto.ChatRequest;
import com.ecommerce.dto.ChatResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

/**
 * AI chatbot service that wraps Spring AI's {@link ChatClient}.
 *
 * <p>Architecture:
 * <pre>
 *   User message
 *       ↓
 *   ChatService.chat()
 *       ↓
 *   Spring AI ChatClient  ──→  Groq API (llama-3.1-8b-instant)
 *                                   ↓ (tool call needed?)
 *                              EcommerceAiTools
 *                                   ↓
 *                              DB / RecommendationService
 *                                   ↓
 *                              Tool result back to Groq
 *                                   ↓
 *                              Natural language reply
 *       ↓
 *   ChatResponse returned to controller
 * </pre>
 *
 * <p>The system prompt instructs the model to act as an ecommerce
 * shopping assistant and to always use the provided tools for data
 * rather than guessing product details or prices.
 *
 * <p>The user's ID is prepended to every message as a context hint
 * ({@code [UserId:N]}) so tools like {@code recommendProducts} know
 * which user to personalise for without requiring a separate API call.
 */
@Service
public class ChatService {

    private static final Logger log = LoggerFactory.getLogger(ChatService.class);

    private final ChatClient chatClient;

    /**
     * System prompt sent to the model before every user message.
     * Defines the assistant's persona, capabilities, and behaviour rules.
     */
    private static final String SYSTEM_PROMPT = """
            You are a helpful and friendly AI shopping assistant for an ecommerce platform.

            Your capabilities:
            - Help customers find products using the searchProducts tool
            - Provide detailed product information using the getProductDetails tool
            - Give personalised recommendations using the recommendProducts tool
            - Check order status using the getOrderStatus tool

            Rules:
            - ALWAYS use the provided tools to fetch real data — never invent product names, prices, or order statuses
            - Be concise and friendly in your responses
            - When listing products, always mention the product ID so customers can look it up
            - If a tool returns no results, suggest the customer try different search terms
            - The user context header [UserId:N] tells you the user's ID — use it for personalised tools
            - If asked about things outside shopping (e.g. politics, coding), politely redirect to shopping assistance
            """;

    /**
     * Build the ChatClient with the system prompt and all AI tools registered.
     *
     * <p>Spring AI's {@code ChatClient.Builder} is auto-configured by the
     * {@code spring-ai-openai-spring-boot-starter} which is pointed at
     * Groq via {@code spring.ai.openai.base-url} in application.properties.
     *
     * @param builder           auto-configured Spring AI chat client builder
     * @param ecommerceAiTools  all {@code @Tool} annotated methods to register
     */
    public ChatService(ChatClient.Builder builder,
                       EcommerceAiTools ecommerceAiTools) {
        this.chatClient = builder
                .defaultSystem(SYSTEM_PROMPT)
                .defaultTools(ecommerceAiTools)   // registers all @Tool methods
                .build();
    }

    /**
     * Process a user's chat message and return the AI-generated reply.
     *
     * <p>The userId is prepended to the message so the LLM has context
     * for calling personalised tools (e.g. recommendProducts(userId)).
     *
     * @param request contains userId and message text
     * @return AI response
     */
    public ChatResponse chat(ChatRequest request) {
        // Prepend user ID context header
        String contextualMessage = "[UserId:" + request.getUserId() + "] "
                + request.getMessage();

        log.debug("Chat request from userId={}: {}", request.getUserId(), request.getMessage());

        String reply = chatClient
                .prompt()
                .user(contextualMessage)
                .call()
                .content();

        log.debug("Chat reply: {}", reply);
        return new ChatResponse(reply);
    }
}
