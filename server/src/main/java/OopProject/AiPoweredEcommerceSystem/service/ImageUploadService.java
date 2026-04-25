package OopProject.AiPoweredEcommerceSystem.service;

import OopProject.AiPoweredEcommerceSystem.entity.Product;
import OopProject.AiPoweredEcommerceSystem.entity.ProductImage;
import OopProject.AiPoweredEcommerceSystem.entity.Seller;
import OopProject.AiPoweredEcommerceSystem.entity.User;
import OopProject.AiPoweredEcommerceSystem.exception.BadRequestException;
import OopProject.AiPoweredEcommerceSystem.exception.ResourceNotFoundException;
import OopProject.AiPoweredEcommerceSystem.exception.UnauthorizedException;
import OopProject.AiPoweredEcommerceSystem.repository.ProductRepository;
import OopProject.AiPoweredEcommerceSystem.repository.SellerRepository;
import OopProject.AiPoweredEcommerceSystem.util.SecurityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

/**
 * Handles uploading product images to the local filesystem.
 *
 * <p>Upload flow:
 * <ol>
 *   <li>Verify the caller is a SELLER who owns the product.</li>
 *   <li>Validate the file type (only JPEG/PNG/WebP allowed).</li>
 *   <li>Generate a UUID-based filename to prevent collisions.</li>
 *   <li>Save the file to {@code app.upload.dir} on disk.</li>
 *   <li>Persist the relative URL in the {@code product_images} table.</li>
 *   <li>Return the public URL for immediate use by the frontend.</li>
 * </ol>
 *
 * <p>Images are served by the Spring MVC resource handler configured in
 * Web Config
 */
@Service
public class ImageUploadService {

    private static final Logger log = LoggerFactory.getLogger(ImageUploadService.class);

    /** Allowed MIME types — reject anything else to prevent malicious uploads. */
    private static final List<String> ALLOWED_TYPES =
            Arrays.asList("image/jpeg", "image/png", "image/webp", "image/gif");

    @Value("${app.upload.dir}")
    private String uploadDir;

    private final ProductRepository productRepository;
    private final SellerRepository  sellerRepository;
    private final SecurityUtils     securityUtils;

    public ImageUploadService(ProductRepository productRepository,
                              SellerRepository sellerRepository,
                              SecurityUtils securityUtils) {
        this.productRepository = productRepository;
        this.sellerRepository  = sellerRepository;
        this.securityUtils     = securityUtils;
    }

    /**
     * Upload an image for a product.
     *
     * @param productId the product to attach the image to
     * @param file      the multipart image file
     * @return the public URL path  e.g. {@code /images/products/abc123.jpg}
     */
    public String uploadProductImage(Long productId, MultipartFile file) {

        // 1. Validate file is not empty
        if (file.isEmpty()) {
            throw new BadRequestException("Uploaded file is empty");
        }

        // 2. Validate content type
        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_TYPES.contains(contentType)) {
            throw new BadRequestException(
                    "Unsupported file type: " + contentType +
                            ". Allowed types: JPEG, PNG, WebP, GIF");
        }

        // 3. Verify ownership
        User   user   = securityUtils.getCurrentUser();
        Seller seller = sellerRepository.findByUser(user)
                .orElseThrow(() -> new UnauthorizedException(
                        "You must have a seller profile to upload images"));

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product", productId));

        if (!product.getSeller().getId().equals(seller.getId())) {
            throw new UnauthorizedException(
                    "You can only upload images for your own products");
        }

        // 4. Build a unique filename
        String originalName = StringUtils.cleanPath(
                file.getOriginalFilename() != null ? file.getOriginalFilename() : "image");
        String extension    = getExtension(originalName);
        String filename     = UUID.randomUUID().toString() + extension;

        // 5. Save file to disk
        Path uploadPath = Paths.get(uploadDir);
        try {
            Files.createDirectories(uploadPath);
            Files.copy(file.getInputStream(),
                    uploadPath.resolve(filename),
                    StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            log.error("Failed to store image file '{}': {}", filename, e.getMessage(), e);
            throw new RuntimeException("Could not store file. Please try again.", e);
        }

        // 6. Persist the URL path in the database
        String imageUrl = "/images/products/" + filename;
        product.getImages().add(new ProductImage(imageUrl, product));
        productRepository.save(product);

        log.info("Image uploaded for product {}: {}", productId, imageUrl);
        return imageUrl;
    }

    // ── helpers ───────────────────────────────────────────────

    private String getExtension(String filename) {
        int dot = filename.lastIndexOf('.');
        return (dot >= 0) ? filename.substring(dot).toLowerCase() : ".jpg";
    }
}