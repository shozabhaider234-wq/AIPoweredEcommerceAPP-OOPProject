package OopProject.AiPoweredEcommerceSystem.repository;

import OopProject.AiPoweredEcommerceSystem.entity.Category;
import OopProject.AiPoweredEcommerceSystem.entity.Product;
import OopProject.AiPoweredEcommerceSystem.entity.Seller;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

/**
 * Spring Data repository for {@link Product} entities.
 *
 * <p>Provides custom JPQL queries for:
 * <ul>
 *   <li>Multi-field product search with optional filters</li>
 *   <li>Category-based browsing</li>
 *   <li>Recommendation engine support (popular products, same-category)</li>
 * </ul>
 */
@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    // ── Category & Seller Browsing ────────────────────────────

    Page<Product> findByCategory(Category category, Pageable pageable);

    Page<Product> findBySeller(Seller seller, Pageable pageable);

    // ── Text Search ───────────────────────────────────────────

    Page<Product> findByNameContainingIgnoreCase(String name, Pageable pageable);

    // ── Advanced Search (multi-filter) ────────────────────────

    /**
     * Dynamic search with all parameters optional.
     *
     * <p>Each filter is only applied when the parameter is non-null,
     * which is achieved via the {@code :param IS NULL OR ...} pattern.
     */
    @Query("SELECT p FROM Product p " +
           "WHERE (:name IS NULL OR LOWER(p.name) LIKE LOWER(CONCAT('%', :name, '%'))) " +
           "AND   (:categoryId IS NULL OR p.category.id = :categoryId) " +
           "AND   (:minPrice IS NULL OR p.price >= :minPrice) " +
           "AND   (:maxPrice IS NULL OR p.price <= :maxPrice)")
    Page<Product> searchProducts(
            @Param("name")       String name,
            @Param("categoryId") Long categoryId,
            @Param("minPrice")   BigDecimal minPrice,
            @Param("maxPrice")   BigDecimal maxPrice,
            Pageable pageable);

    // ── Recommendation Support ────────────────────────────────

    /**
     * Returns products in the same category, excluding the seed product.
     * Used to find "similar items" for a product the user has viewed.
     */
    List<Product> findByCategoryAndIdNot(Category category, Long excludeId, Pageable pageable);

    /**
     * Returns products ordered by stock descending.
     *
     * <p>Stock level is used as a simple proxy for popularity in the
     * recommendation engine (high-stock items are typically high-demand).
     * In a real system, replace with a sales-count column.
     */
    @Query("SELECT p FROM Product p ORDER BY p.stock DESC")
    List<Product> findPopularProducts(Pageable pageable);

    /**
     * Find products whose IDs are in the given list.
     * Used by the recommendation engine to resolve product IDs to entities.
     */
    List<Product> findByIdIn(List<Long> ids);
}
