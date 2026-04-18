package OopProject.AiPoweredEcommerceSystem.service.Abstraction;

import OopProject.AiPoweredEcommerceSystem.dto.PagedResponse;
import OopProject.AiPoweredEcommerceSystem.dto.ReviewDto;
import OopProject.AiPoweredEcommerceSystem.dto.ReviewRequest;

abstract public class ReviewServiceAbstraction {
    abstract public ReviewDto addReview(Long productId, ReviewRequest req);
    abstract public ReviewDto updateReview(Long reviewId, ReviewRequest req);
    abstract public void deleteReview(Long reviewId);
    abstract public PagedResponse<ReviewDto> getProductReviews(Long productId, int page, int size);
}
