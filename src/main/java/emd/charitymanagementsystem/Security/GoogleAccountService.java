package emd.charitymanagementsystem.Security;

import emd.charitymanagementsystem.Repository.UserAccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.core.*;
import org.springframework.security.oauth2.core.oidc.user.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.*;

@Service
@RequiredArgsConstructor
public class GoogleAccountService {
    private final UserAccountRepository accounts;

    @Transactional
    public OidcUser authenticate(OidcUser google) {
        if (!Boolean.TRUE.equals(google.getEmailVerified()) || google.getEmail() == null || google.getSubject() == null)
            throw denied();
        var account = accounts.findByGoogleSubject(google.getSubject()).orElse(null);
        boolean linking = account == null;
        if (account == null) {
            // Linking requires possession of the local account as well as Google's identity.
            // This also prevents a pre-registration attacker from choosing a victim's password.
            var current = SecurityContextHolder.getContext().getAuthentication();
            if (current == null || !current.isAuthenticated() || current instanceof AnonymousAuthenticationToken)
                throw denied();
            account = accounts.findByEmailIgnoreCase(current.getName()).orElseThrow(GoogleAccountService::denied);
            if (!account.getEmail().equalsIgnoreCase(google.getEmail()) || account.getGoogleSubject() != null)
                throw denied();
        }
        if (!account.isEnabled()
                || !account.getEmail().equalsIgnoreCase(google.getEmail())) throw denied();
        if (linking) account.setGoogleSubject(google.getSubject());
        Map<String, Object> claims = new HashMap<>(google.getClaims());
        claims.put("email", account.getEmail());
        return new DefaultOidcUser(List.of(new SimpleGrantedAuthority("ROLE_" + account.getRole().name())),
                google.getIdToken(), new org.springframework.security.oauth2.core.oidc.OidcUserInfo(claims), "email");
    }

    private static OAuth2AuthenticationException denied() {
        return new OAuth2AuthenticationException(new OAuth2Error("account_unavailable"),
                "Sign in with your password before linking the matching Google account.");
    }
}
