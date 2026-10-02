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
    private final emd.charitymanagementsystem.Repository.MemberRepository members;
    private final emd.charitymanagementsystem.Service.Implementation.RegistrationLimitService limit;
    private final emd.charitymanagementsystem.Service.Implementation.UsernameService usernames;

    @Transactional
    public OidcUser authenticate(OidcUser google) {
        if (!Boolean.TRUE.equals(google.getEmailVerified()) || google.getEmail() == null || google.getSubject() == null)
            throw denied();
        var account = accounts.findByGoogleSubject(google.getSubject()).orElse(null);
        boolean linking = account == null;
        if (account == null) {
            // Serialize all registration paths, including email lookup, across app instances.
            limitLock();
            account = accounts.findByEmailIgnoreCase(google.getEmail().trim()).orElse(null);
            var current = SecurityContextHolder.getContext().getAuthentication();
            boolean localProof = current != null && current.isAuthenticated()
                    && !(current instanceof AnonymousAuthenticationToken) && current.getName().equalsIgnoreCase(google.getEmail());
            // For third-party email addresses Google may not be authoritative for current ownership.
            boolean authoritative = google.getEmail().toLowerCase(Locale.ROOT).endsWith("@gmail.com")
                    || google.getClaimAsString("hd") != null;
            if (account != null) {
                if (account.getGoogleSubject() != null || (!authoritative && !localProof)) throw denied();
            } else {
                if (current != null && current.isAuthenticated() && !(current instanceof AnonymousAuthenticationToken)) throw denied();
                try { limit.check(); } catch (IllegalArgumentException ex) {
                    throw new OAuth2AuthenticationException(new OAuth2Error("registration_full"), ex.getMessage());
                }
                String first = google.getGivenName();
                String last = google.getFamilyName();
                first = first == null || first.isBlank() ? "Google" : first.trim();
                last = last == null || last.isBlank() ? "User" : last.trim();
                // Google-only accounts have no usable local password.
                account = accounts.save(emd.charitymanagementsystem.Models.UserAccount.builder()
                        .name(first + " " + last).username(usernames.generate(first, last))
                        .email(google.getEmail().trim().toLowerCase(Locale.ROOT)).password("!")
                        .role(emd.charitymanagementsystem.Models.Role.MEMBER).enabled(true).build());
                var member = members.save(emd.charitymanagementsystem.Models.Member.builder()
                        .name(first).surname(last).email(account.getEmail()).password("!")
                        .role(account.getRole()).userAccount(account).build());
                account.setMember(member);
            }
        }
        if (!account.isEnabled()
                || !account.getEmail().equalsIgnoreCase(google.getEmail())) throw denied();
        if (linking) account.setGoogleSubject(google.getSubject());
        Map<String, Object> claims = new HashMap<>(google.getClaims());
        claims.put("email", account.getEmail());
        return new DefaultOidcUser(List.of(new SimpleGrantedAuthority("ROLE_" + account.getRole().name())),
                google.getIdToken(), new org.springframework.security.oauth2.core.oidc.OidcUserInfo(claims), "email");
    }

    private final emd.charitymanagementsystem.Repository.RegistrationQuotaRepository quota;
    private void limitLock() { quota.lockQuota(); }

    private static OAuth2AuthenticationException denied() {
        return new OAuth2AuthenticationException(new OAuth2Error("account_unavailable"),
                "Google sign-in is unavailable for this account. For third-party email addresses, sign in with your password and link Google from your profile.");
    }
}
