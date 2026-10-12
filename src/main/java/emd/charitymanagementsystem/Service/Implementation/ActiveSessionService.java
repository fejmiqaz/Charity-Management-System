package emd.charitymanagementsystem.Service.Implementation;

import jakarta.servlet.http.*;
import org.springframework.stereotype.Service;
import java.time.Instant;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class ActiveSessionService implements HttpSessionListener, HttpSessionIdListener {
    private record Tracked(HttpSession session, String email, Instant signedInAt, Instant lastSeen) {}
    public record SessionView(String reference, String email, Instant signedInAt, Instant lastSeenAt, boolean current) {}
    private final Map<String,Tracked> sessions = new ConcurrentHashMap<>();
    public static String reference(String id) {
        try { return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(id.getBytes(StandardCharsets.UTF_8))); }
        catch (NoSuchAlgorithmException e) { throw new IllegalStateException(e); }
    }
    public void track(HttpSession session, String email) {
        if (session == null || email == null) return;
        try { sessions.compute(reference(session.getId()), (ref, existing) -> new Tracked(session,email,existing == null ? Instant.now() : existing.signedInAt(),Instant.now())); }
        catch (IllegalStateException ignored) { /* Session ended concurrently. */ }
    }
    public List<SessionView> list(HttpSession current) {
        String currentRef = current == null ? "" : reference(current.getId());
        var result = new ArrayList<SessionView>();
        sessions.forEach((ref, tracked) -> {
            try {
                int timeout = tracked.session().getMaxInactiveInterval();
                if (timeout > 0 && tracked.lastSeen().plusSeconds(timeout).isBefore(Instant.now())) { sessions.remove(ref,tracked); return; }
                tracked.session().getCreationTime(); // Verify that the servlet session is still valid.
                result.add(new SessionView(ref,tracked.email(),tracked.signedInAt(),tracked.lastSeen(),ref.equals(currentRef)));
            } catch (IllegalStateException e) { sessions.remove(ref,tracked); }
        });
        result.sort(Comparator.comparing(SessionView::lastSeenAt).reversed());
        return result;
    }
    public void revoke(String ref, HttpSession current) {
        if (current != null && ref.equals(reference(current.getId()))) throw new IllegalArgumentException("Your current session cannot be ended here.");
        var tracked = sessions.remove(ref);
        if (tracked == null) throw new NoSuchElementException();
        try { tracked.session().invalidate(); } catch (IllegalStateException ignored) {}
    }
    @Override public void sessionDestroyed(HttpSessionEvent event) { sessions.remove(reference(event.getSession().getId())); }
    @Override public void sessionIdChanged(HttpSessionEvent event, String oldId) {
        var tracked = sessions.remove(reference(oldId));
        if (tracked != null) track(event.getSession(),tracked.email());
    }
}
