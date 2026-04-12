package OopProject.AiPoweredEcommerceSystem.repository;

import OopProject.AiPoweredEcommerceSystem.entity.Product;
import OopProject.AiPoweredEcommerceSystem.entity.Review;
import OopProject.AiPoweredEcommerceSystem.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository for {@link Review} entities.
 */
@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {

    /** All reviews for a product, paginated. */
    Page<Review> findByProduct(Product product, Pageable pageable);

    /** Find a user's specific review for a product (at most one per constraint). */
    Optional<Review> findByUserAndProduct(User user, Product product);

    /** Check whether the user has already reviewed this product. */
    boolean existsByUserAndProduct(User user, Product product);
}
