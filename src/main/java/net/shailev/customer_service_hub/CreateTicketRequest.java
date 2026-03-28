package net.shailev.customer_service_hub;

import jakarta.validation.constraints.NotBlank;

public record CreateTicketRequest(
        @NotBlank String title,
        String description,
        String customerUsername
) {
}
