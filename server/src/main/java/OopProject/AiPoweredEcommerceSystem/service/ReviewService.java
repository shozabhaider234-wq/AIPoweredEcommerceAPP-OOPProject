package OopProject.AiPoweredEcommerceSystem.service;

import OopProject.AiPoweredEcommerceSystem.dto.PagedResponse;
import OopProject.AiPoweredEcommerceSystem.dto.ReviewDto;
import OopProject.AiPoweredEcommerceSystem.dto.ReviewRequest;
import OopProject.AiPoweredEcommerceSystem.entity.Product;
import OopProject.AiPoweredEcommerceSystem.entity.Review;
import OopProject.AiPoweredEcommerceSystem.entity.User;
import OopProject.AiPoweredEcommerceSystem.entity.Order;
import OopProject.AiPoweredEcommerceSystem.exception.BadRequestException;
import OopProject.AiPoweredEcommerceSystem.exception.ResourceNotFoundException;
import OopProject.AiPoweredEcommerceSystem.exception.UnauthorizedException;
import OopProject.AiPoweredEcommerceSystem.repository.ProductRepository;
import OopProject.AiPoweredEcommerceSystem.repository.ReviewRepository;
import OopProject.AiPoweredEcommerceSystem.repository.OrderRepository;
import OopProject.AiPoweredEcommerceSystem.service.Abstraction.ReviewServiceAbstraction;
import OopProject.AiPoweredEcommerceSystem.util.SecurityUtils;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Product review business logic.
 *
 * <p>Rules:
 * <ul>
 *   <li>A user can review a product at most once (enforced by DB unique constraint).</li>
 *   <li>Only the review author may update or delete their review.</li>
 * </ul>
 */
@Service
@Transactional
public class ReviewService extends ReviewServiceAbstraction {

    private final ReviewRepository  reviewRepository;
    private final ProductRepository productRepository;
    private final SecurityUtils     securityUtils;
    private final OrderRepository   orderRepository;

    public ReviewService(ReviewRepository reviewRepository,
                         ProductRepository productRepository,
                         SecurityUtils securityUtils,
                         OrderRepository orderRepository) {
        this.reviewRepository  = reviewRepository;
        this.productRepository = productRepository;
        this.securityUtils     = securityUtils;
        this.orderRepository   = orderRepository;
    }

    /** Add a new review for a product. */
    @Override
    public ReviewDto addReview(Long productId, ReviewRequest req) {
        User    user    = securityUtils.getCurrentUser();
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product", productId));

        if (!orderRepository.existsByUserAndItemsProductAndStatus(
                user, product, Order.OrderStatus.DELIVERED)) {
            throw new BadRequestException(
                    "You can only review products you have purchased and received");
        }

        if (reviewRepository.existsByUserAndProduct(user, product)) {
            throw new BadRequestException("You have already reviewed this product");
        }

        Review review = new Review();
        review.setUser(user);
        review.setProduct(product);
        review.setRating(req.getRating());
        review.setComment(req.getComment());

        return ReviewDto.from(reviewRepository.save(review));
    }

    /** Update the authenticated user's existing review. */
    @Override
    public ReviewDto updateReview(Long reviewId, ReviewRequest req) {
        User   user   = securityUtils.getCurrentUser();
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ResourceNotFoundException("Review", reviewId));

        if (!review.getUser().getId().equals(user.getId())) {
            throw new UnauthorizedException("You can only edit your own reviews");
        }

        review.setRating(req.getRating());
        review.setComment(req.getComment());
        return ReviewDto.from(reviewRepository.save(review));
    }

    /** Delete the authenticated user's review. */
    @Override
    public void deleteReview(Long reviewId) {
        User   user   = securityUtils.getCurrentUser();
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ResourceNotFoundException("Review", reviewId));

        if (!review.getUser().getId().equals(user.getId())) {
            throw new UnauthorizedException("You can only delete your own reviews");
        }

        reviewRepository.delete(review);
    }

    /** Get paginated reviews for a product — newest first. */
    @Override
    @Transactional(readOnly = true)
    public PagedResponse<ReviewDto> getProductReviews(Long productId, int page, int size) {
        Product  product  = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product", productId));
        Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());
        return PagedResponse.of(
                reviewRepository.findByProduct(product, pageable).map(ReviewDto::from));
    }
}
