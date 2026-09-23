package emd.charitymanagementsystem.Api;

import emd.charitymanagementsystem.DTO.auth.RegistrationDto;
import emd.charitymanagementsystem.Service.UserAccountService;
import emd.charitymanagementsystem.Service.Implementation.ProfileService;
import jakarta.servlet.http.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.*;
import org.springframework.security.core.*;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.session.SessionAuthenticationStrategy;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.HttpStatus;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthApiController {
    private final AuthenticationManager apiAuthenticationManager;
    private final SessionAuthenticationStrategy apiSessionStrategy;
    private final HttpSessionSecurityContextRepository apiSecurityContextRepository;
    private final UserAccountService accounts;
    private final ProfileService profiles;

    public record Login(@NotBlank @Size(max = 254) String email, @NotBlank @Size(max = 128) String password) {
    }

    public record SessionUser(Long id, String name, String email, String role) {
    }

    public record Csrf(String headerName, String token) {
    }

    @GetMapping("/csrf")
    public Csrf csrf(CsrfToken csrf) {
        return new Csrf(csrf.getHeaderName(), csrf.getToken());
    }

    @GetMapping("/me")
    public SessionUser me(Authentication authentication) {
        var account = profiles.account(authentication.getName());
        return new SessionUser(account.getId(), account.getName(), account.getEmail(), account.getRole().name());
    }

    @PostMapping("/login")
    public SessionUser login(@Valid @RequestBody Login login, HttpServletRequest request, HttpServletResponse response) {
        Authentication auth = apiAuthenticationManager.authenticate(UsernamePasswordAuthenticationToken.unauthenticated(login.email().trim(), login.password()));
        apiSessionStrategy.onAuthentication(auth, request, response);
        var context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(auth);
        SecurityContextHolder.setContext(context);
        apiSecurityContextRepository.saveContext(context, request, response);
        return me(auth);
    }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public SessionUser register(@Valid @RequestBody RegistrationDto form) {
        var account = accounts.register(form);
        return new SessionUser(account.getId(), account.getName(), account.getEmail(), account.getRole().name());
    }
}
