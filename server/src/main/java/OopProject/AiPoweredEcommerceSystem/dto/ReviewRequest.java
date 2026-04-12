package OopProject.AiPoweredEcommerceSystem.dto;

import com.ecommerce.entity.Review;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

/**
 * Request body for creating / updating a review.
 */
public class ReviewRequest {

    @NotNull(message = "Rating is required")
    @Min(value = 1, message = "Rating must be at least 1")
    @Max(value = 5, message = "Rating must be at most 5")
    private Integer rating;

    private String comment;

    public Integer getRating()                 { return rating; }
    public void setRating(Integer rating)      { this.rating = rating; }

    public String getComment()                 { return comment; }
    public void setComment(String comment)     { this.comment = comment; }
}

