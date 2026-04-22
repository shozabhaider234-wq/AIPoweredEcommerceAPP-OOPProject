package OopProject.AiPoweredEcommerceSystem.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

/**
 * Request body for POST /api/orders.
 *
 * <p>The customer must supply full shipping details and confirm their
 * payment method at checkout. Currently only CASH_ON_DELIVERY is accepted.
 *
 * <p>Example JSON:
 * <pre>
 * {
 *   "shippingAddress"  : "House 12, Street 4, G-9/2",
 *   "shippingCity"     : "Islamabad",
 *   "shippingPostalCode": "44000",
 *   "contactPhone"     : "0300-1234567",
 *   "deliveryNotes"    : "Leave at gate if no answer",
 *   "paymentMethod"    : "CASH_ON_DELIVERY"
 * }
 * </pre>
 */
public class PlaceOrderRequest {

    // ── Shipping details ──────────────────────────────────────

    @NotBlank(message = "Shipping address is required")
    private String shippingAddress;

    @NotBlank(message = "City is required")
    private String shippingCity;

    /** Optional — postal / ZIP code */
    private String shippingPostalCode;

    @NotBlank(message = "Contact phone is required")
    @Pattern(
            regexp = "^[\\+]?[0-9\\-\\s]{7,15}$",
            message = "Enter a valid phone number"
    )
    private String contactPhone;

    /** Optional delivery instructions for the rider */
    private String deliveryNotes;

    // ── Payment ───────────────────────────────────────────────

    /**
     * Payment method selected by the customer.
     * Must be "CASH_ON_DELIVERY" — the only supported method right now.
     */
    @NotBlank(message = "Payment method is required")
    private String paymentMethod = "CASH_ON_DELIVERY";

    // ── Getters & Setters ─────────────────────────────────────

    public String getShippingAddress()                       { return shippingAddress; }
    public void   setShippingAddress(String shippingAddress) { this.shippingAddress = shippingAddress; }

    public String getShippingCity()                          { return shippingCity; }
    public void   setShippingCity(String shippingCity)       { this.shippingCity = shippingCity; }

    public String getShippingPostalCode()                    { return shippingPostalCode; }
    public void   setShippingPostalCode(String code)         { this.shippingPostalCode = code; }

    public String getContactPhone()                          { return contactPhone; }
    public void   setContactPhone(String contactPhone)       { this.contactPhone = contactPhone; }

    public String getDeliveryNotes()                         { return deliveryNotes; }
    public void   setDeliveryNotes(String deliveryNotes)     { this.deliveryNotes = deliveryNotes; }

    public String getPaymentMethod()                         { return paymentMethod; }
    public void   setPaymentMethod(String paymentMethod)     { this.paymentMethod = paymentMethod; }
}