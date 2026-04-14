package OopProject.AiPoweredEcommerceSystem.dto;

import OopProject.AiPoweredEcommerceSystem.dto.*;
import OopProject.AiPoweredEcommerceSystem.entity.Product;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Read-only DTO returned by product endpoints.
 *
 * <p>Flattens the entity graph into a simple JSON-friendly structure so that
 * lazy-loaded associations (Seller, Category) are resolved inside the
 * service layer — not during JSON serialisation.
 */
public class ProductDto {

    private Long          id;
    private String        name;
    private String        description;
    private BigDecimal    price;
    private Integer       stock;
    private String        categoryName;
    private Long          categoryId;
    private String        sellerStoreName;
    private Long          sellerId;
    private List<String>  imageUrls;

    // ── Static Factory ────────────────────────────────────────

    /**
     * Convert a {@link Product} entity to a {@link ProductDto}.
     *
     * <p>Call this inside an active transaction so lazy associations can load.
     */
    public static ProductDto from(Product p) {
        ProductDto dto = new ProductDto();
        dto.id              = p.getId();
        dto.name            = p.getName();
        dto.description     = p.getDescription();
        dto.price           = p.getPrice();
        dto.stock           = p.getStock();

        if (p.getCategory() != null) {
            dto.categoryId   = p.getCategory().getId();
            dto.categoryName = p.getCategory().getName();
        }
        if (p.getSeller() != null) {
            dto.sellerId        = p.getSeller().getId();
            dto.sellerStoreName = p.getSeller().getStoreName();
        }

        dto.imageUrls = p.getImages().stream()
                .map(img -> img.getImageUrl())
                .collect(Collectors.toList());

        return dto;
    }

    // ── Getters & Setters ─────────────────────────────────────

    public Long getId()                              { return id; }
    public void setId(Long id)                       { this.id = id; }

    public String getName()                          { return name; }
    public void setName(String name)                 { this.name = name; }

    public String getDescription()                   { return description; }
    public void setDescription(String description)   { this.description = description; }

    public BigDecimal getPrice()                     { return price; }
    public void setPrice(BigDecimal price)           { this.price = price; }

    public Integer getStock()                        { return stock; }
    public void setStock(Integer stock)              { this.stock = stock; }

    public String getCategoryName()                  { return categoryName; }
    public void setCategoryName(String categoryName) { this.categoryName = categoryName; }

    public Long getCategoryId()                      { return categoryId; }
    public void setCategoryId(Long categoryId)       { this.categoryId = categoryId; }

    public String getSellerStoreName()                        { return sellerStoreName; }
    public void setSellerStoreName(String sellerStoreName)    { this.sellerStoreName = sellerStoreName; }

    public Long getSellerId()                        { return sellerId; }
    public void setSellerId(Long sellerId)           { this.sellerId = sellerId; }

    public List<String> getImageUrls()               { return imageUrls; }
    public void setImageUrls(List<String> imageUrls) { this.imageUrls = imageUrls; }
}
