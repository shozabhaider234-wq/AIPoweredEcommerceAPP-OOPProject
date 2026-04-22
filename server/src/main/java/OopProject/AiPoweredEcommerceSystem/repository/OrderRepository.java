package OopProject.AiPoweredEcommerceSystem.repository;

import OopProject.AiPoweredEcommerceSystem.entity.Order;
import OopProject.AiPoweredEcommerceSystem.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository for {@link Order} entities.
 */
@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    Page<Order> findByUser(User user, Pageable pageable);

    Page<Order> findDistinctByItems_Product_Seller_Id(Long sellerId, Pageable pageable);
}
