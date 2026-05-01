package OopProject.AiPoweredEcommerceSystem.controller;



import OopProject.AiPoweredEcommerceSystem.dto.*;
import OopProject.AiPoweredEcommerceSystem.service.SellerService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * Manages seller store profiles.
 *
 * <p>Creating / updating a profile requires the SELLER role.
 * Reading a seller by ID is public.
 */
@RestController
@RequestMapping("/api/sellers")
public class SellerController {

    private final SellerService sellerService;

    public SellerController(SellerService sellerService) {
        this.sellerService = sellerService;
    }

    /** POST /api/sellers — create a seller profile for the current user. */
    @PostMapping
    @PreAuthorize("hasRole('SELLER')")
    public ResponseEntity<ApiResponse<SellerDto>> createProfile(
            @Valid @RequestBody SellerRequest req) {
        return ResponseEntity.ok(ApiResponse.success(sellerService.createSellerProfile(req)));
    }

    /** PUT /api/sellers/me — update the current user's seller profile. */
    @PutMapping("/me")
    @PreAuthorize("hasRole('SELLER')")
    public ResponseEntity<ApiResponse<SellerDto>> updateProfile(
            @Valid @RequestBody SellerRequest req) {
        return ResponseEntity.ok(ApiResponse.success(sellerService.updateSellerProfile(req)));
    }

    /** GET /api/sellers/me — get own seller profile. */
    @GetMapping("/me")
    @PreAuthorize("hasRole('SELLER')")
    public ResponseEntity<ApiResponse<SellerDto>> getMyProfile() {
        return ResponseEntity.ok(ApiResponse.success(sellerService.getMyProfile()));
    }

    /** GET /api/sellers/{id} — public seller profile. */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<SellerDto>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(sellerService.getSellerById(id)));
    }
    @GetMapping("/myproducts")
    public ResponseEntity<ApiResponse<PagedResponse<ProductDto>>> getSellersProducts(
            @RequestParam(defaultValue = "0")    int    page,
            @RequestParam(defaultValue = "12")   int    size){
        return ResponseEntity.ok(ApiResponse.success(sellerService.getSellerProducts(page,size)));
    }
}
