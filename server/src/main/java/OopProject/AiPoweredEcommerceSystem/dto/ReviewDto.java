package OopProject.AiPoweredEcommerceSystem.dto;

import OopProject.AiPoweredEcommerceSystem.entity.Review;

/**
 * DTO for returning review data in API responses.
 */
public class ReviewDto {

    private Long    id;
    private Long    userId;
    private String  userName;
    private Long    productId;
    private Integer rating;
    private String  comment;

    // ── Factory ───────────────────────────────────────────────

    public static ReviewDto from(Review review) {
        ReviewDto dto = new ReviewDto();
        dto.id        = review.getId();
        dto.userId    = review.getUser().getId();
        dto.userName  = review.getUser().getName();
        dto.productId = review.getProduct().getId();
        dto.rating    = review.getRating();
        dto.comment   = review.getComment();
        return dto;
    }

    // ── Getters & Setters ─────────────────────────────────────

    public Long getId()                        { return id; }
    public void setId(Long id)                 { this.id = id; }

    public Long getUserId()                    { return userId; }
    public void setUserId(Long userId)         { this.userId = userId; }

    public String getUserName()                { return userName; }
    public void setUserName(String userName)   { this.userName = userName; }

    public Long getProductId()                 { return productId; }
    public void setProductId(Long productId)   { this.productId = productId; }

    public Integer getRating()                 { return rating; }
    public void setRating(Integer rating)      { this.rating = rating; }

    public String getComment()                 { return comment; }
    public void setComment(String comment)     { this.comment = comment; }
}
