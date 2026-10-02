package emd.charitymanagementsystem.Security;

import emd.charitymanagementsystem.Repository.UserAccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.authentication.*;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.web.filter.OncePerRequestFilter;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.util.List;

@RequiredArgsConstructor
public class AccountPermissionsFilter extends OncePerRequestFilter {
    private final UserAccountRepository accounts;
    @Override protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        var context = SecurityContextHolder.getContext();
        var auth = context.getAuthentication();
        var session = request.getSession(false);
        if (session != null && Boolean.TRUE.equals(session.getAttribute("accountAuthenticated"))
                && auth != null && auth.isAuthenticated() && !(auth instanceof AnonymousAuthenticationToken)) {
            var account = accounts.findByEmailIgnoreCase(auth.getName()).orElse(null);
            if (account == null) context.setAuthentication(null);
            else {
                if (!account.isEnabled()) context.setAuthentication(null);
                else {
                    var authorities = List.of(new SimpleGrantedAuthority("ROLE_" + account.getRole().name()));
                    if (!auth.getAuthorities().equals(authorities)) {
                        org.springframework.security.authentication.AbstractAuthenticationToken refreshed;
                        if (auth instanceof org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken oauth)
                            refreshed = new org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken(
                                    oauth.getPrincipal(), authorities, oauth.getAuthorizedClientRegistrationId());
                        else refreshed = UsernamePasswordAuthenticationToken.authenticated(auth.getPrincipal(), auth.getCredentials(), authorities);
                        refreshed.setDetails(auth.getDetails());
                        context.setAuthentication(refreshed);
                    }
                }
            }
        }
        chain.doFilter(request, response);
    }
}
