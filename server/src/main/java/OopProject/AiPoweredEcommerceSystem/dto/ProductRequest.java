package OopProject.AiPoweredEcommerceSystem.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

/**
 * Request body for creating or updating a product.
 *
 * <p>Used by POST /api/products and PUT /api/products/{id}.
 * The seller is derived from the authenticated principal — not passed in the body.
 */
public class ProductRequest {

    @NotBlank(message = "Product name is required")
    private String name;

    private String description;

    @NotNull(message = "Price is required")
    @DecimalMin(value = "0.01", message = "Price must be greater than zero")
    private BigDecimal price;

    @Min(value = 0, message = "Stock cannot be negative")
    private Integer stock = 0;

    /** ID of the category to assign. May be null for uncategorised products. */
    private Long categoryId;

    // ── Getters & Setters ─────────────────────────────────────

    public String getName()                        { return name; }
    public void setName(String name)               { this.name = name; }

    public String getDescription()                 { return description; }
    public void setDescription(String description) { this.description = description; }

    public BigDecimal getPrice()                   { return price; }
    public void setPrice(BigDecimal price)         { this.price = price; }

    public Integer getStock()                      { return stock; }
    public void setStock(Integer stock)            { this.stock = stock; }

    public Long getCategoryId()                    { return categoryId; }
    public void setCategoryId(Long categoryId)     { this.categoryId = categoryId; }
}
