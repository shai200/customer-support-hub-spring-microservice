package net.shailev.customer_service_hub;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record OAuthTokenRequest(
        @NotBlank(message = "username is required")
        @Size(max = 100, message = "username must be at most 100 characters")
        String username,
        @NotBlank(message = "password is required")
        @Size(max = 255, message = "password must be at most 255 characters")
        String password
) {
}
