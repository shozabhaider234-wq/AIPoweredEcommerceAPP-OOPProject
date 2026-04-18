package OopProject.AiPoweredEcommerceSystem.service.Abstraction;

import OopProject.AiPoweredEcommerceSystem.dto.ProductDto;

import java.util.List;

abstract public class WishlistServiceAbstraction {
    abstract public void addToWishlist(Long productId);
    abstract public void removeFromWishlist(Long productId);
    abstract public List<ProductDto> getWishlist();
}
