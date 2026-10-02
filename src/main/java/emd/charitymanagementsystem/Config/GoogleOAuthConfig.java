package emd.charitymanagementsystem.Config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.*;
import org.springframework.security.config.oauth2.client.CommonOAuth2Provider;
import org.springframework.security.oauth2.client.registration.*;

@Configuration
@ConditionalOnProperty(name = "app.auth.google-enabled", havingValue = "true")
public class GoogleOAuthConfig {
    @Bean
    public ClientRegistrationRepository clientRegistrationRepository(
            @Value("${app.auth.google-client-id}") String clientId,
            @Value("${app.auth.google-client-secret}") String secret,
            @Value("${app.auth.google-redirect-uri}") String redirectUri) {
        if (clientId.isBlank() || secret.isBlank() || redirectUri.isBlank())
            throw new IllegalStateException("Google OAuth requires a client ID, secret and exact redirect URI.");
        return new InMemoryClientRegistrationRepository(CommonOAuth2Provider.GOOGLE.getBuilder("google")
                .clientId(clientId).clientSecret(secret).scope("openid", "email", "profile")
                .redirectUri(redirectUri).build());
    }
}
