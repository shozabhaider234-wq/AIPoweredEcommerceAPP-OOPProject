package OopProject.AiPoweredEcommerceSystem.service.Abstraction;

import OopProject.AiPoweredEcommerceSystem.dto.PagedResponse;
import OopProject.AiPoweredEcommerceSystem.dto.ProductDto;
import OopProject.AiPoweredEcommerceSystem.dto.ProductRequest;

import java.math.BigDecimal;

abstract public class ProductServiceAbstraction {
    abstract public ProductDto createProduct(ProductRequest req);
    abstract public ProductDto updateProduct(Long productId, ProductRequest req);
    abstract public void deleteProduct(Long productId);
    abstract public ProductDto getProductById(Long productId, Long userId);
    abstract public PagedResponse<ProductDto> getAllProducts(int page, int size, String sortBy);
    abstract public PagedResponse<ProductDto> searchProducts(String name, Long categoryId, BigDecimal minPrice,
                                                             BigDecimal maxPrice, int page, int size,
                                                             String sortBy);
}
