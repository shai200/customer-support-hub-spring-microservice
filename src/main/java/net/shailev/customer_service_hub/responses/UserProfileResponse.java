package net.shailev.customer_service_hub.responses;

import net.shailev.customer_service_hub.types.RoleType;

public record UserProfileResponse(
        Long id,
        String username,
        RoleType roleType,
        String fullName,
        String email,
        String agentUsername
) {
}
