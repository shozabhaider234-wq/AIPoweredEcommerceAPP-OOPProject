package OopProject.AiPoweredEcommerceSystem.service.Abstraction;

import OopProject.AiPoweredEcommerceSystem.dto.*;

abstract public class AuthServiceAbstraction {
    abstract public AuthResponse register(RegisterRequest req);
    abstract public AuthResponse login(AuthRequest req);
    abstract public UserDto getProfile();
    abstract public UserDto updateProfile(UpdateProfileRequest req);

}
