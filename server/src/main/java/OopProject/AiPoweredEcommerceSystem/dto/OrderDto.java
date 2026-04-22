package OopProject.AiPoweredEcommerceSystem.dto;

import OopProject.AiPoweredEcommerceSystem.entity.Order;
import OopProject.AiPoweredEcommerceSystem.entity.OrderItem;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * DTO representation of a customer order and its line items.
 */
public class OrderDto {

    private Long           orderId;
    private String         status;
    private BigDecimal     totalPrice;
    private LocalDateTime  createdAt;
    private List<ItemDto>  items;

    //New attributes
    private LocalDateTime  updatedAt;
    // ── Shipping ──────────────────────────────────────────────
    private String shippingAddress;
    private String shippingCity;
    private String shippingPostalCode;
    private String contactPhone;
    private String deliveryNotes;

    // ── Payment ───────────────────────────────────────────────
    private String paymentMethod;
    private String paymentStatus;

    // ── Cancellation ──────────────────────────────────────────
    /** Reason supplied by the seller (or null for customer cancellations). */
    private String cancellationReason;
    /** "CUSTOMER" or "SELLER" — who cancelled this order. */
    private String cancelledBy;

    // ── Status note (set by seller on status updates) ─────────
    private String statusNote;

    // ── Customer info (shown to seller) ──────────────────────
    private String customerName;
    private String customerEmail;

    // ── Factory ───────────────────────────────────────────────

    public static OrderDto from(Order order) {
        OrderDto dto = new OrderDto();
        dto.orderId    = order.getId();
        dto.status     = order.getStatus().name();
        dto.totalPrice = order.getTotalPrice();
        dto.createdAt  = order.getCreatedAt();

        // Newly added
        dto.updatedAt           = order.getUpdatedAt();

        // Shipping
        dto.shippingAddress     = order.getShippingAddress();
        dto.shippingCity        = order.getShippingCity();
        dto.shippingPostalCode  = order.getShippingPostalCode();
        dto.contactPhone        = order.getContactPhone();
        dto.deliveryNotes       = order.getDeliveryNotes();

        // Payment
        dto.paymentMethod       = order.getPaymentMethod() != null
                ? order.getPaymentMethod().name() : null;
        dto.paymentStatus       = order.getPaymentStatus() != null
                ? order.getPaymentStatus().name() : null;

        // Cancellation
        dto.cancellationReason  = order.getCancellationReason();
        dto.cancelledBy         = order.getCancelledBy();

        // Status note
        dto.statusNote          = order.getStatusNote();

        // Customer info (for seller dashboard)
        if (order.getUser() != null) {
            dto.customerName  = order.getUser().getName();
            dto.customerEmail = order.getUser().getEmail();
        }
        dto.items      = order.getItems().stream()
                .map(ItemDto::from)
                .collect(Collectors.toList());
        return dto;
    }

    // ── Nested Item DTO ───────────────────────────────────────

    public static class ItemDto {
        private Long       orderItemId;
        private Long       productId;
        private String     productName;
        private Integer    quantity;
        private BigDecimal unitPrice;
        private BigDecimal lineTotal;

        public static ItemDto from(OrderItem item) {
            ItemDto d = new ItemDto();
            d.orderItemId  = item.getId();
            d.productId    = item.getProduct().getId();
            d.productName  = item.getProduct().getName();
            d.quantity     = item.getQuantity();
            d.unitPrice    = item.getPrice();
            d.lineTotal    = item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity()));
            return d;
        }

        public Long getOrderItemId()                   { return orderItemId; }
        public void setOrderItemId(Long orderItemId)   { this.orderItemId = orderItemId; }
        public Long getProductId()                     { return productId; }
        public void setProductId(Long productId)       { this.productId = productId; }
        public String getProductName()                 { return productName; }
        public void setProductName(String productName) { this.productName = productName; }
        public Integer getQuantity()                   { return quantity; }
        public void setQuantity(Integer quantity)      { this.quantity = quantity; }
        public BigDecimal getUnitPrice()               { return unitPrice; }
        public void setUnitPrice(BigDecimal unitPrice) { this.unitPrice = unitPrice; }
        public BigDecimal getLineTotal()               { return lineTotal; }
        public void setLineTotal(BigDecimal lineTotal) { this.lineTotal = lineTotal; }
    }

    // ── Getters & Setters ─────────────────────────────────────

    public Long getOrderId()                          { return orderId; }
    public void setOrderId(Long orderId)              { this.orderId = orderId; }

    public String getStatus()                         { return status; }
    public void setStatus(String status)              { this.status = status; }

    public BigDecimal getTotalPrice()                 { return totalPrice; }
    public void setTotalPrice(BigDecimal totalPrice)  { this.totalPrice = totalPrice; }

    public LocalDateTime getCreatedAt()               { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public List<ItemDto> getItems()                   { return items; }
    public void setItems(List<ItemDto> items)         { this.items = items; }

    // New getters and setters
    public LocalDateTime getUpdatedAt()                   { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt)     { this.updatedAt = updatedAt; }

    public String getShippingCity()                            { return shippingCity; }
    public void setShippingCity(String shippingCity)           { this.shippingCity = shippingCity; }

    public String getShippingPostalCode()                      { return shippingPostalCode; }
    public void setShippingPostalCode(String shippingPostalCode) { this.shippingPostalCode = shippingPostalCode; }

    public String getContactPhone()                            { return contactPhone; }
    public void setContactPhone(String contactPhone)           { this.contactPhone = contactPhone; }

    public String getDeliveryNotes()                           { return deliveryNotes; }
    public void setDeliveryNotes(String deliveryNotes)         { this.deliveryNotes = deliveryNotes; }

    public String getPaymentMethod()                           { return paymentMethod; }
    public void setPaymentMethod(String paymentMethod)         { this.paymentMethod = paymentMethod; }

    public String getPaymentStatus()                           { return paymentStatus; }
    public void setPaymentStatus(String paymentStatus)         { this.paymentStatus = paymentStatus; }

    public String getCancellationReason()                      { return cancellationReason; }
    public void setCancellationReason(String cancellationReason) { this.cancellationReason = cancellationReason; }

    public String getCancelledBy()                             { return cancelledBy; }
    public void setCancelledBy(String cancelledBy)             { this.cancelledBy = cancelledBy; }

    public String getStatusNote()                              { return statusNote; }
    public void setStatusNote(String statusNote)               { this.statusNote = statusNote; }

    public String getCustomerName()                            { return customerName; }
    public void setCustomerName(String customerName)           { this.customerName = customerName; }

    public String getCustomerEmail()                           { return customerEmail; }
    public void setCustomerEmail(String customerEmail)         { this.customerEmail = customerEmail; }
}