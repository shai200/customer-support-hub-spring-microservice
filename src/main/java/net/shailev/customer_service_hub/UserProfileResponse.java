package net.shailev.customer_service_hub;

public record UserProfileResponse(
        Long id,
        String username,
        RoleType roleType,
        String fullName,
        String email,
        String agentUsername
) {
}
