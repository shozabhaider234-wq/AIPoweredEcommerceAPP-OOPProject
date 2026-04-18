package OopProject.AiPoweredEcommerceSystem.service.Abstraction;

import OopProject.AiPoweredEcommerceSystem.dto.CartDto;

abstract public class CartServiceAbstraction {
    abstract public CartDto addToCart(Long productId, int quantity);
    abstract public CartDto updateQuantity(Long cartItemId, int quantity);
    abstract public CartDto removeFromCart(Long cartItemId);
    abstract public CartDto getCart();
}
