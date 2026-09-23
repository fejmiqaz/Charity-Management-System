package emd.charitymanagementsystem.Repository;

import emd.charitymanagementsystem.Models.Notification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import java.time.Instant;
import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
    interface Summary {
        Long getId();
        String getTitle();
        String getMessage();
        Instant getCreatedAt();
        Instant getReadAt();
    }
    long countByRecipientEmailIgnoreCaseAndRecipientEnabledTrueAndReadAtIsNull(String email);
    List<Summary> findTop5ByRecipientEmailIgnoreCaseAndRecipientEnabledTrueOrderByCreatedAtDescIdDesc(String email);
    boolean existsByDeduplicationKey(String key);
    long countByRecipientIdAndReadAtIsNull(Long recipientId);
    Page<Notification> findByRecipientIdOrderByCreatedAtDescIdDesc(Long recipientId, Pageable pageable);
    @Modifying
    @Query("update Notification n set n.readAt = :now where n.id = :id and n.recipient.id = :recipientId and n.readAt is null")
    int markRead(Long id, Long recipientId, Instant now);
    @Modifying
    @Query("update Notification n set n.readAt = :now where n.recipient.id = :recipientId and n.readAt is null")
    int markAllRead(Long recipientId, Instant now);
}
