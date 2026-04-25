package OopProject.AiPoweredEcommerceSystem.controller;

import OopProject.AiPoweredEcommerceSystem.dto.ApiResponse;
import OopProject.AiPoweredEcommerceSystem.dto.PagedResponse;
import OopProject.AiPoweredEcommerceSystem.dto.ReviewDto;
import OopProject.AiPoweredEcommerceSystem.dto.ReviewRequest;
import OopProject.AiPoweredEcommerceSystem.service.ReviewService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Product review endpoints.
 *
 * <p>Reading reviews is public.
 * Adding, updating, and deleting require authentication.
 */
@RestController
@RequestMapping("/api")
public class ReviewController {

    private final ReviewService reviewService;

    public ReviewController(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    /** GET /api/products/{productId}/reviews */
    @GetMapping("/products/{productId}/reviews")
    public ResponseEntity<ApiResponse<PagedResponse<ReviewDto>>> getReviews(
            @PathVariable Long productId,
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(ApiResponse.success(
                reviewService.getProductReviews(productId, page, size)));
    }

    /** POST /api/products/{productId}/reviews */
    @PostMapping("/products/{productId}/reviews")
    public ResponseEntity<ApiResponse<ReviewDto>> addReview(
            @PathVariable Long productId,
            @Valid @RequestBody ReviewRequest req) {
        return ResponseEntity.ok(ApiResponse.success(reviewService.addReview(productId, req)));
    }

    /** PUT /api/reviews/{reviewId} */
    @PutMapping("/reviews/{reviewId}")
    public ResponseEntity<ApiResponse<ReviewDto>> updateReview(
            @PathVariable Long reviewId,
            @Valid @RequestBody ReviewRequest req) {
        return ResponseEntity.ok(ApiResponse.success(reviewService.updateReview(reviewId, req)));
    }

    /** DELETE /api/reviews/{reviewId} */
    @DeleteMapping("/reviews/{reviewId}")
    public ResponseEntity<ApiResponse<Void>> deleteReview(@PathVariable Long reviewId) {
        reviewService.deleteReview(reviewId);
        return ResponseEntity.ok(ApiResponse.success(null, "Review deleted"));
    }
}
