package net.shailev.customer_service_hub;

import jakarta.validation.constraints.NotBlank;

public record OAuthTokenRequest(
        @NotBlank String username,
        @NotBlank String password
) {
}
