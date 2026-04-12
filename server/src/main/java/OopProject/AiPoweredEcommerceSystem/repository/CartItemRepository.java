package OopProject.AiPoweredEcommerceSystem.repository;

import OopProject.AiPoweredEcommerceSystem.entity.Cart;
import OopProject.AiPoweredEcommerceSystem.entity.CartItem;
import OopProject.AiPoweredEcommerceSystem.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository for {@link CartItem} entities.
 */
@Repository
public interface CartItemRepository extends JpaRepository<CartItem, Long> {

    /**
     * Find an existing line-item for a specific product in a cart.
     * Used to check if the product is already in the cart before adding a duplicate.
     */
    Optional<CartItem> findByCartAndProduct(Cart cart, Product product);
}
