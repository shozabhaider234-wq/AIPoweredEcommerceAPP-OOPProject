package OopProject.AiPoweredEcommerceSystem.dto;


import jakarta.validation.constraints.NotBlank;

/**
 * Request body for PUT /api/seller/orders/{id}/status.
 *
 * <p>The seller advances the order to the next logical status.
 * An optional note (e.g. a tracking number) can be included and is
 * stored on the order so the customer can see it.
 *
 * <p>Valid status values the seller may set:
 * <ul>
 *   <li>CONFIRMED — seller has accepted and is preparing the order</li>
 *   <li>SHIPPED   — order has been dispatched; include tracking info in note</li>
 *   <li>DELIVERED — rider has confirmed delivery</li>
 * </ul>
 *
 * <p>Example JSON:
 * <pre>
 * {
 *   "status": "SHIPPED",
 *   "statusNote": "Dispatched via TCS. Tracking: TCS-987654321"
 * }
 * </pre>
 */
public class UpdateOrderStatusRequest {

    @NotBlank(message = "Status is required")
    private String status;

    /**
     * Optional note visible to the customer — e.g. tracking numbers,
     * estimated delivery times, or any other update.
     */
    private String statusNote;

    // ── Getters & Setters ─────────────────────────────────────

    public String getStatus()                    { return status; }
    public void   setStatus(String status)       { this.status = status; }

    public String getStatusNote()                { return statusNote; }
    public void   setStatusNote(String note)     { this.statusNote = note; }
}
