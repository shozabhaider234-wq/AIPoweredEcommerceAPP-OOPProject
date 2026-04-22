package OopProject.AiPoweredEcommerceSystem.controller;

import OopProject.AiPoweredEcommerceSystem.dto.ApiResponse;
import OopProject.AiPoweredEcommerceSystem.dto.CartDto;
import OopProject.AiPoweredEcommerceSystem.service.CartService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Shopping cart management.
 * All endpoints require authentication (JWT).
 */
@RestController
@RequestMapping("/api/cart")
public class CartController {

    private final CartService cartService;

    public CartController(CartService cartService) {
        this.cartService = cartService;
    }

    /** GET /api/cart — get the current user's cart (creates one if it doesn't exist). */
    @GetMapping
    public ResponseEntity<ApiResponse<CartDto>> getCart() {
        return ResponseEntity.ok(ApiResponse.success(cartService.getCart()));
    }

    /**
     * POST /api/cart/items?productId=1&quantity=2
     * Add a product to the cart. If the product is already in the cart,
     * the quantity is incremented.
     */
    @PostMapping("/items")
    public ResponseEntity<ApiResponse<CartDto>> addItem(
            @RequestParam Long productId,
            @RequestParam(defaultValue = "1") int quantity) {
        return ResponseEntity.ok(ApiResponse.success(cartService.addToCart(productId, quantity)));
    }

    /**
     * PUT /api/cart/items/{itemId}?quantity=3
     * Update the quantity of a cart line item.
     * Setting quantity to 0 removes the item.
     */
    @PutMapping("/items/{itemId}")
    public ResponseEntity<ApiResponse<CartDto>> updateItem(
            @PathVariable Long itemId,
            @RequestParam int quantity) {
        return ResponseEntity.ok(ApiResponse.success(cartService.updateQuantity(itemId, quantity)));
    }

    /** DELETE /api/cart/items/{itemId} — remove a specific item from the cart. */
    @DeleteMapping("/items/{itemId}")
    public ResponseEntity<ApiResponse<CartDto>> removeItem(@PathVariable Long itemId) {
        return ResponseEntity.ok(ApiResponse.success(cartService.removeFromCart(itemId)));
    }
}
