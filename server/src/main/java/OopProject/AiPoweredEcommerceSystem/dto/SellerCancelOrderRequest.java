package OopProject.AiPoweredEcommerceSystem.dto;


import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Request body for PUT /api/seller/orders/{id}/cancel.
 *
 * <p>A seller must supply a non-blank reason when cancelling an order.
 * This reason is stored on the Order and shown to the customer so they
 * understand why their order was cancelled.
 *
 * <p>Example JSON:
 * <pre>
 * {
 *   "reason": "Item is currently out of stock at our warehouse. We apologise for the inconvenience."
 * }
 * </pre>
 */
public class SellerCancelOrderRequest {

    @NotBlank(message = "Cancellation reason is required")
    @Size(min = 10, max = 500,
            message = "Reason must be between 10 and 500 characters")
    private String reason;

    // ── Getters & Setters ─────────────────────────────────────

    public String getReason()              { return reason; }
    public void   setReason(String reason) { this.reason = reason; }
}
