package net.shailev.customer_service_hub;

public record OAuthTokenResponse(
        String access_token,
        String token_type,
        long expires_in
) {
}
