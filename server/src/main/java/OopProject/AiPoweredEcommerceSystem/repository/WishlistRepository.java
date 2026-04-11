package OopProject.AiPoweredEcommerceSystem.repository;

import OopProject.AiPoweredEcommerceSystem.entity.User;
import OopProject.AiPoweredEcommerceSystem.entity.Wishlist;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Repository for {@link Wishlist} entities.
 */
@Repository
public interface WishlistRepository extends JpaRepository<Wishlist, Long> {

    /** All wishlist entries for a user. */
    List<Wishlist> findByUser(User user);

    /** Check duplicate before adding. */
    boolean existsByUserIdAndProductId(Long userId, Long productId);

    /** Remove a specific product from a user's wishlist. */
    @Transactional
    void deleteByUserIdAndProductId(Long userId, Long productId);
}
