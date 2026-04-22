package OopProject.AiPoweredEcommerceSystem.controller;

import OopProject.AiPoweredEcommerceSystem.dto.ApiResponse;
import OopProject.AiPoweredEcommerceSystem.dto.ProductDto;
import OopProject.AiPoweredEcommerceSystem.service.WishlistService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Wishlist management — save and remove products for later.
 * All endpoints require authentication.
 */
@RestController
@RequestMapping("/api/wishlist")
public class WishlistController {

    private final WishlistService wishlistService;

    public WishlistController(WishlistService wishlistService) {
        this.wishlistService = wishlistService;
    }

    /** GET /api/wishlist — get all wishlist items for the current user. */
    @GetMapping
    public ResponseEntity<ApiResponse<List<ProductDto>>> getWishlist() {
        return ResponseEntity.ok(ApiResponse.success(wishlistService.getWishlist()));
    }

    /** POST /api/wishlist/{productId} — add a product to the wishlist. */
    @PostMapping("/{productId}")
    public ResponseEntity<ApiResponse<Void>> add(@PathVariable Long productId) {
        wishlistService.addToWishlist(productId);
        return ResponseEntity.ok(ApiResponse.success(null, "Added to wishlist"));
    }

    /** DELETE /api/wishlist/{productId} — remove a product from the wishlist. */
    @DeleteMapping("/{productId}")
    public ResponseEntity<ApiResponse<Void>> remove(@PathVariable Long productId) {
        wishlistService.removeFromWishlist(productId);
        return ResponseEntity.ok(ApiResponse.success(null, "Removed from wishlist"));
    }
}
