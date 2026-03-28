package net.shailev.customer_service_hub.validators;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.Email;

public record CreateCustomerRequest(
        @NotBlank(message = "username is required")
        @Size(max = 100, message = "username must be at most 100 characters")
        String username,
        @Size(max = 200, message = "fullName must be at most 200 characters")
        String fullName,
        @Email(message = "email must be a valid email address")
        @Size(max = 255, message = "email must be at most 255 characters")
        String email,
        @Size(max = 100, message = "agentUsername must be at most 100 characters")
        String agentUsername
) {
}
