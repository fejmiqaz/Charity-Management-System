package emd.charitymanagementsystem.Service.Implementation;

import emd.charitymanagementsystem.Models.AuditEntry;
import emd.charitymanagementsystem.Repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.*;
import java.time.Instant;

@Service @RequiredArgsConstructor
public class ActivityAuditService {
    private final AuditEntryRepository entries;
    private final UserAccountRepository accounts;
    // Snapshots have no FK: deleting a member must not delete their audit history.
    @Transactional(propagation=Propagation.REQUIRES_NEW)
    public void record(String email, String action, String resource, String recordId, String path) {
        var entry = new AuditEntry();
        entry.setOccurredAt(Instant.now()); entry.setAction(action); entry.setResource(resource);
        entry.setRecordId(recordId); entry.setPath(path);
        if (email != null) {
            entry.setActorEmail(email.substring(0, Math.min(email.length(),254)));
            accounts.findByEmailIgnoreCase(email).ifPresent(a -> {
                entry.setActorId(a.getId());
                entry.setActorName(a.getName().substring(0, Math.min(a.getName().length(),254)));
            });
        }
        entries.save(entry);
    }
}
