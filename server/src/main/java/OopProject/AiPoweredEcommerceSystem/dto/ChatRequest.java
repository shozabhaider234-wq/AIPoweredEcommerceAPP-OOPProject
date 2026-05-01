package OopProject.AiPoweredEcommerceSystem.dto;


import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * Request body for POST /api/chat.
 */
public class ChatRequest {

    /** The ID of the currently logged-in user (provides personalization context). */
    @NotNull(message = "userId is required")
    private Long userId;

    /** The natural-language message from the user. */
    @NotBlank(message = "message is required")
    private String message;

    public Long getUserId()                  { return userId; }
    public void setUserId(Long userId)       { this.userId = userId; }

    public String getMessage()               { return message; }
    public void setMessage(String message)   { this.message = message; }
}

