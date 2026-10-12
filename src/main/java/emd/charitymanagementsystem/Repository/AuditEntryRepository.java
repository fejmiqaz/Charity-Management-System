package emd.charitymanagementsystem.Repository;

import emd.charitymanagementsystem.Models.AuditEntry;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.data.domain.*;
import java.time.Instant;

public interface AuditEntryRepository extends JpaRepository<AuditEntry,Long> {
    @Query("select a from AuditEntry a where (:action = '' or a.action = :action) and (:actorId is null or a.actorId = :actorId)")
    Page<AuditEntry> activity(@Param("action") String action, @Param("actorId") Long actorId, Pageable pageable);
    long countByActionAndOccurredAtAfter(String action, Instant since);
}
