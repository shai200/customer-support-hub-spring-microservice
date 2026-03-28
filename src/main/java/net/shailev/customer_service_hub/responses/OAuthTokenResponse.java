package net.shailev.customer_service_hub.responses;

public record OAuthTokenResponse(
        String access_token,
        String token_type,
        long expires_in
) {
}
