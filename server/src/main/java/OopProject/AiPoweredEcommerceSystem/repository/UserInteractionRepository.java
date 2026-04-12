package OopProject.AiPoweredEcommerceSystem.repository;


import OopProject.AiPoweredEcommerceSystem.entity.UserInteraction;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository for {@link UserInteraction} entities.
 *
 * <p>Powers the recommendation engine with behaviour-based queries.
 */
@Repository
public interface UserInteractionRepository extends JpaRepository<UserInteraction, Long> {

    /** All interactions for a user, newest first — used for "recently viewed". */
    List<UserInteraction> findByUserIdOrderByTimestampDesc(Long userId);

    /**
     * Returns category IDs the user has interacted with most (by count).
     *
     * <p>The recommendation engine uses this to prioritise products from
     * categories the user is already interested in.
     */
    @Query("SELECT p.category.id FROM UserInteraction ui " +
            "JOIN ui.product p " +
            "WHERE ui.user.id = :userId AND p.category IS NOT NULL " +
            "GROUP BY p.category.id " +
            "ORDER BY COUNT(ui) DESC")
    List<Long> findTopCategoryIdsByUserId(@Param("userId") Long userId);

    /**
     * Returns the product IDs the user has purchased.
     *
     * <p>These are excluded from recommendations — no point suggesting
     * something the customer has already bought.
     */
    @Query("SELECT DISTINCT ui.product.id FROM UserInteraction ui " +
            "WHERE ui.user.id = :userId AND ui.interactionType = 'PURCHASE'")
    List<Long> findPurchasedProductIdsByUserId(@Param("userId") Long userId);

    /**
     * Returns the product IDs the user has viewed or added to cart recently.
     * Capped to the 20 most recent records.
     */
    @Query("SELECT DISTINCT ui.product.id FROM UserInteraction ui " +
            "WHERE ui.user.id = :userId " +
            "AND ui.interactionType IN ('VIEW', 'ADD_TO_CART') " +
            "ORDER BY ui.timestamp DESC")
    List<Long> findRecentlyEngagedProductIds(@Param("userId") Long userId,
                                             Pageable pageable);
}

