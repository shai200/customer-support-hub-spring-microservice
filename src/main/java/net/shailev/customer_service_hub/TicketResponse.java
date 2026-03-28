package net.shailev.customer_service_hub;

import java.time.Instant;

public record TicketResponse(
        Long id,
        String title,
        String description,
        String customerUsername,
        String agentUsername,
        Instant createdAt
) {
}
