package emd.charitymanagementsystem.Repository;

import emd.charitymanagementsystem.Models.EmailDelivery;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface EmailDeliveryRepository extends JpaRepository<EmailDelivery, Long> {
    boolean existsByDeduplicationKey(String key);
    @Modifying
    @Query("update EmailDelivery e set e.status = :cancelled where e.taskId = :taskId and e.status = :pending")
    int cancelTaskEmails(Long taskId, EmailDelivery.Status pending, EmailDelivery.Status cancelled);
    @Query("select e.id from EmailDelivery e where e.status = :status and e.nextAttemptAt <= :now order by e.nextAttemptAt, e.id")
    List<Long> findDue(EmailDelivery.Status status, Instant now, Pageable pageable);
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select e from EmailDelivery e where e.id = :id")
    Optional<EmailDelivery> findLockedById(Long id);
}
