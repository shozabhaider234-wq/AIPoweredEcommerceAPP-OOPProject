package OopProject.AiPoweredEcommerceSystem.controller;

import OopProject.AiPoweredEcommerceSystem.dto.ApiResponse;
import OopProject.AiPoweredEcommerceSystem.dto.PagedResponse;
import OopProject.AiPoweredEcommerceSystem.dto.ProductDto;
import OopProject.AiPoweredEcommerceSystem.dto.ProductRequest;
import OopProject.AiPoweredEcommerceSystem.service.ImageUploadService;
import OopProject.AiPoweredEcommerceSystem.service.ProductService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;

/**
 * Product catalogue endpoints.
 *
 * <p>Read endpoints (GET) are public — no JWT needed.
 * Write endpoints require SELLER role.
 * Image upload requires SELLER role.
 */
@RestController
@RequestMapping("/api/products")
public class ProductController {

    private final ProductService      productService;
    private final ImageUploadService  imageUploadService;

    public ProductController(ProductService productService,
                             ImageUploadService imageUploadService) {
        this.productService     = productService;
        this.imageUploadService = imageUploadService;
    }

    // ── Read (public) ─────────────────────────────────────────

    /**
     * GET /api/products
     * Paginated list of all products.
     */
    @GetMapping
    public ResponseEntity<ApiResponse<PagedResponse<ProductDto>>> list(
            @RequestParam(defaultValue = "0")    int    page,
            @RequestParam(defaultValue = "12")   int    size,
            @RequestParam(defaultValue = "name") String sortBy) {
        return ResponseEntity.ok(ApiResponse.success(
                productService.getAllProducts(page, size, sortBy)));
    }

    /**
     * GET /api/products/search
     * Multi-filter search — all params optional.
     * Example: /api/products/search?name=shoe&maxPrice=100&categoryId=3
     */
    @GetMapping("/search")
    public ResponseEntity<ApiResponse<PagedResponse<ProductDto>>> search(
            @RequestParam(required = false)       String     name,
            @RequestParam(required = false)       Long       categoryId,
            @RequestParam(required = false)       BigDecimal minPrice,
            @RequestParam(required = false)       BigDecimal maxPrice,
            @RequestParam(defaultValue = "0")     int        page,
            @RequestParam(defaultValue = "12")    int        size,
            @RequestParam(defaultValue = "price") String     sortBy) {
        return ResponseEntity.ok(ApiResponse.success(
                productService.searchProducts(name, categoryId, minPrice, maxPrice,
                        page, size, sortBy)));
    }

    /**
     * GET /api/products/{id}
     * Full product detail. Pass userId query param to track VIEW interactions.
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ProductDto>> getById(
            @PathVariable                  Long id,
            @RequestParam(required = false) Long userId) {
        return ResponseEntity.ok(ApiResponse.success(
                productService.getProductById(id, userId)));
    }

    // ── Write (SELLER only) ───────────────────────────────────

    /** POST /api/products */
    @PostMapping
    @PreAuthorize("hasRole('SELLER')")
    public ResponseEntity<ApiResponse<ProductDto>> create(
            @Valid @RequestBody ProductRequest req) {
        return ResponseEntity.ok(ApiResponse.success(productService.createProduct(req)));
    }

    /** PUT /api/products/{id} */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('SELLER')")
    public ResponseEntity<ApiResponse<ProductDto>> update(
            @PathVariable Long id,
            @Valid @RequestBody ProductRequest req) {
        return ResponseEntity.ok(ApiResponse.success(productService.updateProduct(id, req)));
    }

    /** DELETE /api/products/{id} */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('SELLER')")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        productService.deleteProduct(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Product deleted"));
    }

    // ── Image Upload (SELLER only) ────────────────────────────

    /**
     * POST /api/products/{id}/images
     * Multipart file upload — stores the image on disk and records the path in DB.
     * Returns the public URL: /images/products/{filename}
     */
    @PostMapping("/{id}/images")
    @PreAuthorize("hasRole('SELLER')")
    public ResponseEntity<ApiResponse<String>> uploadImage(
            @PathVariable Long id,
            @RequestParam("file") MultipartFile file) {
        String url = imageUploadService.uploadProductImage(id, file);
        return ResponseEntity.ok(ApiResponse.success(url, "Image uploaded successfully"));
    }
}
