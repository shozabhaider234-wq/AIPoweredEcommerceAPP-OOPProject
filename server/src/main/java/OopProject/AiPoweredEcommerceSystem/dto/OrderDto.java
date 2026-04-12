package OopProject.AiPoweredEcommerceSystem.dto;

import com.ecommerce.entity.Order;
import com.ecommerce.entity.OrderItem;

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

    // ── Factory ───────────────────────────────────────────────

    public static OrderDto from(Order order) {
        OrderDto dto = new OrderDto();
        dto.orderId    = order.getId();
        dto.status     = order.getStatus().name();
        dto.totalPrice = order.getTotalPrice();
        dto.createdAt  = order.getCreatedAt();
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
}

