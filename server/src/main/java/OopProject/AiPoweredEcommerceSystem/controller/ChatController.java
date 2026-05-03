package OopProject.AiPoweredEcommerceSystem.controller;

import OopProject.AiPoweredEcommerceSystem.ai.chatbot.ChatService;
import OopProject.AiPoweredEcommerceSystem.dto.ApiResponse;
import OopProject.AiPoweredEcommerceSystem.dto.ChatRequest;
import OopProject.AiPoweredEcommerceSystem.dto.ChatResponse;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * AI Chatbot endpoint powered by Spring AI + Groq (llama-3.1-8b-instant).
 *
 * <p>The model can call tools (searchProducts, getProductDetails,
 * recommendProducts, getOrderStatus) automatically based on the user's message.
 *
 * <p>Example request:
 * <pre>
 * POST /api/chat
 * {
 *   "userId": 1,
 *   "message": "Suggest me running shoes under $100"
 * }
 * </pre>
 */
@RestController
@RequestMapping("/api/chat")
public class ChatController {

    private final ChatService chatService;

    public ChatController(ChatService chatService) {
        this.chatService = chatService;
    }

    /**
     * POST /api/chat
     * Sends the user's message to the Groq LLM and returns the AI reply.
     * Tool calling happens transparently inside the service layer.
     */
    @PostMapping
    public ResponseEntity<ApiResponse<ChatResponse>> chat(
            @Valid @RequestBody ChatRequest request) {
        return ResponseEntity.ok(ApiResponse.success(chatService.chat(request)));
    }
}

