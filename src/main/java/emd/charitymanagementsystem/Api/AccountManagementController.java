package emd.charitymanagementsystem.Api;

import emd.charitymanagementsystem.Models.*;
import emd.charitymanagementsystem.Repository.UserAccountRepository;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequiredArgsConstructor
public class AccountManagementController {
    private final UserAccountRepository accounts;
    private final PasswordEncoder passwords;
    public record Account(Long id, String name, String email, String role) {}
    public record RoleChange(@NotNull Role role) {}
    public record PasswordChange(String currentPassword, @NotBlank @Size(min=8,max=72) String password,
                                 @NotBlank @Size(max=72) String confirmPassword) {}

    private UserAccount actor(Authentication auth) {
        return accounts.findByEmailIgnoreCase(auth.getName()).orElseThrow(() -> new AccessDeniedException("Account unavailable."));
    }
    private UserAccount head(Authentication auth) {
        var account = actor(auth);
        if (!account.isEnabled() || account.getRole() != Role.HEAD) throw new AccessDeniedException("Only the Head can change roles.");
        return account;
    }

    @GetMapping("/api/accounts")
    @PreAuthorize("hasRole('HEAD')")
    public List<Account> list(Authentication auth) {
        head(auth);
        return accounts.findAll().stream().map(a -> new Account(a.getId(), a.getName(), a.getEmail(), a.getRole().name())).toList();
    }

    @PutMapping("/api/accounts/{id}/role")
    @PreAuthorize("hasRole('HEAD')")
    @Transactional
    public void role(@PathVariable Long id, @Valid @RequestBody RoleChange change, Authentication auth) {
        var head = head(auth);
        if (head.getId().equals(id)) throw new IllegalArgumentException("Change another user's role; your own role cannot be changed here.");
        var account = accounts.findById(id).orElseThrow();
        account.setRole(change.role());
        if (account.getMember() != null) account.getMember().setRole(change.role());
    }

    @PostMapping("/api/profile/password")
    @Transactional
    public void password(@Valid @RequestBody PasswordChange change, Authentication auth) {
        var account = actor(auth);
        if (!account.isEnabled()) throw new AccessDeniedException("Account unavailable.");
        if (!"!".equals(account.getPassword()) && (change.currentPassword() == null
                || !passwords.matches(change.currentPassword(), account.getPassword())))
            throw new IllegalArgumentException("Current password is incorrect.");
        if (!change.password().equals(change.confirmPassword())) throw new IllegalArgumentException("Passwords do not match.");
        if (change.password().getBytes(java.nio.charset.StandardCharsets.UTF_8).length > 72)
            throw new IllegalArgumentException("Password must contain at most 72 UTF-8 bytes.");
        account.setPassword(passwords.encode(change.password()));
        if (account.getMember() != null) account.getMember().setPassword(account.getPassword());
    }
}
