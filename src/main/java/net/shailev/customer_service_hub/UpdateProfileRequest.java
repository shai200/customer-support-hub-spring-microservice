package net.shailev.customer_service_hub;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;

public record UpdateProfileRequest(
        @Size(max = 200, message = "fullName must be at most 200 characters")
        String fullName,
        @Email(message = "email must be a valid email address")
        @Size(max = 255, message = "email must be at most 255 characters")
        String email
) {
}
