package OopProject.AiPoweredEcommerceSystem.service.Abstraction;

import OopProject.AiPoweredEcommerceSystem.dto.SellerDto;
import OopProject.AiPoweredEcommerceSystem.dto.SellerRequest;

abstract public class SellerServiceAbstraction {
    abstract public SellerDto createSellerProfile(SellerRequest req);
    abstract public SellerDto updateSellerProfile(SellerRequest req);
    abstract public SellerDto getMyProfile();
    abstract public SellerDto getSellerById(Long id);
}
