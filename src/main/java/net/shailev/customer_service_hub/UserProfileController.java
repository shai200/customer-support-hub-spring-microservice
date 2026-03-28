package net.shailev.customer_service_hub;

import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping
public class UserProfileController {

    private final UserProfileService userProfileService;

    public UserProfileController(UserProfileService userProfileService) {
        this.userProfileService = userProfileService;
    }

    @PostMapping("/agent/customers")
    public UserProfileResponse createCustomer(
            Authentication authentication,
            @Valid @RequestBody CreateCustomerRequest request
    ) {
        return userProfileService.createCustomer(authentication.getName(), request);
    }

    @GetMapping("/agent/customers")
    public List<UserProfileResponse> getCustomers(
            Authentication authentication
    ) {
        return userProfileService.getCustomersForActor(authentication.getName());
    }

    @GetMapping("/profile/me")
    public UserProfileResponse getOwnProfile(
            Authentication authentication
    ) {
        return userProfileService.getOwnProfile(authentication.getName());
    }

    @PutMapping("/profile/me")
    public UserProfileResponse updateOwnProfile(
            Authentication authentication,
            @Valid @RequestBody UpdateProfileRequest request
    ) {
        return userProfileService.updateOwnProfile(authentication.getName(), request);
    }
}
