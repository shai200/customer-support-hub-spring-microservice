package net.shailev.customer_service_hub;

import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
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
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody CreateCustomerRequest request
    ) {
        return userProfileService.createCustomer(userDetails.getUsername(), request);
    }

    @GetMapping("/agent/customers")
    public List<UserProfileResponse> getCustomers(
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        return userProfileService.getCustomersForActor(userDetails.getUsername());
    }

    @GetMapping("/profile/me")
    public UserProfileResponse getOwnProfile(
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        return userProfileService.getOwnProfile(userDetails.getUsername());
    }

    @PutMapping("/profile/me")
    public UserProfileResponse updateOwnProfile(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody UpdateProfileRequest request
    ) {
        return userProfileService.updateOwnProfile(userDetails.getUsername(), request);
    }
}
