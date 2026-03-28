package net.shailev.customer_service_hub.validators;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateTicketRequest(
        @NotBlank(message = "title is required")
        @Size(max = 255, message = "title must be at most 255 characters")
        String title,
        @Size(max = 4000, message = "description must be at most 4000 characters")
        String description,
        @Size(max = 100, message = "customerUsername must be at most 100 characters")
        String customerUsername
) {
}
