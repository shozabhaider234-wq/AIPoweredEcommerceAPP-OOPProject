package OopProject.AiPoweredEcommerceSystem.repository;

import OopProject.AiPoweredEcommerceSystem.entity.Cart;
import OopProject.AiPoweredEcommerceSystem.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository for {@link Cart} entities.
 * Each user has at most one active cart (enforced by unique FK).
 */
@Repository
public interface CartRepository extends JpaRepository<Cart, Long> {

    /** Retrieve the cart belonging to a given user. */
    Optional<Cart> findByUser(User user);
}