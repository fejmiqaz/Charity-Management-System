package emd.charitymanagementsystem.Security;

import emd.charitymanagementsystem.Service.Implementation.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.util.Set;

public class ActivityAuditFilter extends OncePerRequestFilter {
    private final ActivityAuditService audit;
    private final ActiveSessionService sessions;
    public ActivityAuditFilter(ActivityAuditService audit, ActiveSessionService sessions) { this.audit=audit; this.sessions=sessions; }
    private String email(Authentication auth) { return auth != null && auth.isAuthenticated() && !(auth instanceof AnonymousAuthenticationToken) ? auth.getName() : null; }
    private void track(HttpServletRequest request, String email) {
        var session = request.getSession(false);
        if (session != null && Boolean.TRUE.equals(session.getAttribute("accountAuthenticated"))) sessions.track(session,email);
    }
    @Override protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res, FilterChain chain) throws IOException, ServletException {
        String before = email(SecurityContextHolder.getContext().getAuthentication());
        track(req,before);
        chain.doFilter(req,res);
        String after = email(SecurityContextHolder.getContext().getAuthentication());
        track(req,after);
        String path=req.getRequestURI().substring(req.getContextPath().length());
        boolean write=Set.of("POST","PUT","PATCH","DELETE").contains(req.getMethod());
        boolean login=("POST".equals(req.getMethod()) && (path.equals("/api/auth/login") || path.equals("/login"))) || path.startsWith("/login/oauth2/code/");
        String action=null, resource=null, recordId=null;
        if (login) {
            boolean failed=res.getStatus() >= 400 || (res.getHeader("Location") != null && res.getHeader("Location").contains("/login?error"));
            action=failed ? "SIGN_IN_FAILED" : after != null ? "SIGN_IN" : null;
            resource="Account";
        } else if (write && (path.startsWith("/api/") || path.equals("/logout")) && res.getStatus() < 400 && (before != null || after != null)) {
            if (path.equals("/api/auth/logout") || path.equals("/logout")) { action="SIGN_OUT"; resource="Account"; }
            else if (path.equals("/api/profile/password")) { action="PASSWORD_CHANGED"; resource="Account"; }
            else if (path.matches("/api/accounts/\\d+/role")) { action="ROLE_CHANGED"; resource="Account"; recordId=path.split("/")[3]; }
            else if (path.matches("/api/management/security/sessions/[a-f0-9]{64}/revoke")) { action="SESSION_REVOKED"; resource="Session"; }
            else {
                String[] parts=path.split("/");
                for (int i=1;i<parts.length;i++) {
                    String found=switch(parts[i]) { case "projects"->"Project"; case "events"->"Event"; case "donations"->"Donation"; case "members"->"Member"; case "budget"->"Budget"; case "years"->"Year"; case "profile"->"Profile"; case "memberships"->"Membership"; case "tasks"->"Task"; case "revenues"->"Income"; case "payments"->"Payment"; default->null; };
                    if(found != null) {resource=found; recordId=i+1<parts.length && parts[i+1].matches("\\d+") ? parts[i+1] : null;}
                }
                if(resource != null) action=switch(req.getMethod()) {case "DELETE"->"DELETE"; case "POST"->path.matches(".*/(edit|update|void|complete|toggle)(/.*)?$") ? "UPDATE" : "CREATE"; default->"UPDATE";};
            }
        }
        if(action != null) {
            String location=res.getHeader("Location");
            if(recordId == null && location != null && location.matches(".*/\\d+$")) recordId=location.substring(location.lastIndexOf('/')+1);
            // Never record credentials, request bodies, query strings, tokens or session IDs.
            try { audit.record(before != null ? before : after,action,resource,recordId,path.length()>500 ? path.substring(0,500) : path); }
            catch (RuntimeException e) { org.slf4j.LoggerFactory.getLogger(getClass()).error("Could not persist activity audit entry",e); }
        }
    }
}
