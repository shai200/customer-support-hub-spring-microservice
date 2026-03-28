package net.shailev.customer_service_hub;

import jakarta.validation.constraints.NotBlank;

public record CreateCustomerRequest(
        @NotBlank String username,
        String fullName,
        String email,
        String agentUsername
) {
}
