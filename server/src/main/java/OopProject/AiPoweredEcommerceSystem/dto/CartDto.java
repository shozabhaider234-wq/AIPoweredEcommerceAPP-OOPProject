package OopProject.AiPoweredEcommerceSystem.dto;

import OopProject.AiPoweredEcommerceSystem.entity.CartItem;
import OopProject.AiPoweredEcommerceSystem.entity.Cart;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

/**
 * DTO representation of a user's shopping cart.
 * Includes line items and a computed grand total.
 */

public class CartDto {

    private Long           cartId;
    private List<ItemDto>  items;
    private BigDecimal     grandTotal;

    // ── Factory ───────────────────────────────────────────────

    public static CartDto from(Cart cart) {
        CartDto dto = new CartDto();
        dto.cartId = cart.getId();

        dto.items = cart.getItems().stream()
                .map(ItemDto::from)
                .collect(Collectors.toList());

        dto.grandTotal = dto.items.stream()
                .map(ItemDto::getLineTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return dto;
    }

    // ── Nested Item DTO ───────────────────────────────────────

    public static class ItemDto {
        private Long       cartItemId;
        private Long       productId;
        private String     productName;
        private BigDecimal unitPrice;
        private Integer    quantity;
        private BigDecimal lineTotal;

        public static ItemDto from(CartItem item) {
            ItemDto d = new ItemDto();
            d.cartItemId   = item.getId();
           d.productId    = item.getProduct().getId();
          d.productName  = item.getProduct().getName();
           d.unitPrice    = item.getProduct().getPrice();
            d.lineTotal    = item.getProduct().getPrice()
                   .multiply(BigDecimal.valueOf(item.getQuantity()));
            return d;
        }

        public Long getCartItemId()                { return cartItemId; }
        public void setCartItemId(Long cartItemId) { this.cartItemId = cartItemId; }

        public Long getProductId()                 { return productId; }
        public void setProductId(Long productId)   { this.productId = productId; }

        public String getProductName()                 { return productName; }
        public void setProductName(String productName) { this.productName = productName; }

        public BigDecimal getUnitPrice()               { return unitPrice; }
        public void setUnitPrice(BigDecimal unitPrice) { this.unitPrice = unitPrice; }

        public Integer getQuantity()                   { return quantity; }
        public void setQuantity(Integer quantity)      { this.quantity = quantity; }

        public BigDecimal getLineTotal()               { return lineTotal; }
        public void setLineTotal(BigDecimal lineTotal) { this.lineTotal = lineTotal; }
    }

    // ── Getters & Setters ─────────────────────────────────────

    public Long getCartId()                        { return cartId; }
    public void setCartId(Long cartId)             { this.cartId = cartId; }

    public List<ItemDto> getItems()                { return items; }
    public void setItems(List<ItemDto> items)      { this.items = items; }

    public BigDecimal getGrandTotal()              { return grandTotal; }
    public void setGrandTotal(BigDecimal grandTotal) { this.grandTotal = grandTotal; }
}
