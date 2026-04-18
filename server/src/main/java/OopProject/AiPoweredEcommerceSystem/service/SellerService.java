package OopProject.AiPoweredEcommerceSystem.service;

import OopProject.AiPoweredEcommerceSystem.dto.SellerDto;
import OopProject.AiPoweredEcommerceSystem.dto.SellerRequest;
import OopProject.AiPoweredEcommerceSystem.entity.Seller;
import OopProject.AiPoweredEcommerceSystem.entity.User;
import OopProject.AiPoweredEcommerceSystem.exception.BadRequestException;
import OopProject.AiPoweredEcommerceSystem.exception.ResourceNotFoundException;
import OopProject.AiPoweredEcommerceSystem.repository.SellerRepository;
import OopProject.AiPoweredEcommerceSystem.service.Abstraction.SellerServiceAbstraction;
import OopProject.AiPoweredEcommerceSystem.util.SecurityUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Business logic for seller profile management.
 *
 * <p>Each user with role=SELLER may have exactly one seller profile.
 * The profile stores the store name and description shown on the storefront.
 */
@Service
@Transactional
public class SellerService extends SellerServiceAbstraction {

    private final SellerRepository sellerRepository;
    private final SecurityUtils    securityUtils;

    public SellerService(SellerRepository sellerRepository, SecurityUtils securityUtils) {
        this.sellerRepository = sellerRepository;
        this.securityUtils    = securityUtils;
    }

    /**
     * Create a seller profile for the currently authenticated SELLER user.
     *
     * @throws BadRequestException if a profile already exists
     */
    @Override
    public SellerDto createSellerProfile(SellerRequest req) {
        User user = securityUtils.getCurrentUser();

        if (sellerRepository.existsByUser(user)) {
            throw new BadRequestException(
                    "A seller profile already exists for your account. Use PUT /api/sellers/me to update it.");
        }

        Seller seller = new Seller(req.getStoreName(), req.getDescription(), user);
        return SellerDto.from(sellerRepository.save(seller));
    }

    /**
     * Update the authenticated seller's store profile.
     *
     * @throws ResourceNotFoundException if no profile exists yet
     */
    @Override
    public SellerDto updateSellerProfile(SellerRequest req) {
        User   user   = securityUtils.getCurrentUser();
        Seller seller = sellerRepository.findByUser(user)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No seller profile found. Create one first via POST /api/sellers"));

        seller.setStoreName(req.getStoreName());
        seller.setDescription(req.getDescription());
        return SellerDto.from(sellerRepository.save(seller));
    }

    /** Returns the authenticated user's seller profile. */
    @Override
    @Transactional(readOnly = true)
    public SellerDto getMyProfile() {
        User user = securityUtils.getCurrentUser();
        return SellerDto.from(sellerRepository.findByUser(user)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Seller profile not found for current user")));
    }

    /** Returns any seller's public profile by their seller ID. */
    @Override
    @Transactional(readOnly = true)
    public SellerDto getSellerById(Long id) {
        return SellerDto.from(sellerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Seller", id)));
    }
}

