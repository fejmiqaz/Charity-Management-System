package emd.charitymanagementsystem.Api;

import emd.charitymanagementsystem.Models.*;
import emd.charitymanagementsystem.Repository.*;
import emd.charitymanagementsystem.Service.Implementation.ActiveSessionService;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.data.domain.*;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import java.time.Instant;
import java.util.*;

@RestController @RequestMapping("/api/management") @RequiredArgsConstructor
@PreAuthorize("hasRole('HEAD')")
public class ManagementController {
    private final AuditEntryRepository audit;
    private final UserAccountRepository accounts;
    private final ActiveSessionService sessions;
    private void head(Authentication auth) {
        var account=accounts.findByEmailIgnoreCase(auth.getName()).orElseThrow(() -> new AccessDeniedException("Account unavailable."));
        if(!account.isEnabled() || account.getRole()!=Role.HEAD) throw new AccessDeniedException("Account unavailable.");
    }
    @GetMapping("/activity")
    public ApiPage<AuditEntry> activity(@RequestParam(defaultValue="0") int page, @RequestParam(defaultValue="20") int size,
            @RequestParam(defaultValue="") String action, @RequestParam(required=false) Long actorId, Authentication auth) {
        head(auth);
        return ApiPage.of(audit.activity(action,actorId,PageRequest.of(Math.max(0,page),Math.max(1,Math.min(100,size)),Sort.by(Sort.Direction.DESC,"occurredAt","id"))));
    }
    public record AccountSecurity(Long id, String name, String email, String role, boolean enabled, boolean passwordSet, boolean googleLinked) {}
    public record SecurityOverview(Instant checkedAt, long failedSignIns24h, List<ActiveSessionService.SessionView> sessions, List<AccountSecurity> accounts) {}
    @GetMapping("/security")
    public SecurityOverview security(Authentication auth,HttpServletRequest request) {
        head(auth);
        return new SecurityOverview(Instant.now(),audit.countByActionAndOccurredAtAfter("SIGN_IN_FAILED",Instant.now().minusSeconds(86400)),
                sessions.list(request.getSession(false)),accounts.findAll().stream().map(a -> new AccountSecurity(a.getId(),a.getName(),a.getEmail(),a.getRole().name(),a.isEnabled(),!"!".equals(a.getPassword()),a.getGoogleSubject()!=null)).toList());
    }
    @PostMapping("/security/sessions/{reference}/revoke")
    public void revoke(@PathVariable String reference, Authentication auth,HttpServletRequest request) {
        head(auth); sessions.revoke(reference,request.getSession(false));
    }
}
