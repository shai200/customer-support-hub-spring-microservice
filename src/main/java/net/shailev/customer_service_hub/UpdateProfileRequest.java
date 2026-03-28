package net.shailev.customer_service_hub;

public record UpdateProfileRequest(
        String fullName,
        String email
) {
}
