package emd.charitymanagementsystem.Models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.Instant;
import java.time.LocalDateTime;

/** Durable outbox: written with the business transaction, sent only after commit. */
@Entity
@Table(name = "email_deliveries", indexes = @Index(name = "email_delivery_due", columnList = "status,nextAttemptAt"))
@Getter @Setter @NoArgsConstructor
public class EmailDelivery {
    public enum Status { PENDING, SENT, CANCELLED, FAILED }
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(nullable = false, unique = true, length = 200) private String deduplicationKey;
    private Long accountId;
    private Long memberId;
    @Column(nullable = false) private Long eventId;
    private Long taskId;
    private LocalDateTime eventDate;
    private Integer reminderDays;
    @Column(nullable = false, length = 300) private String subject;
    @Column(nullable = false, length = 3000) private String body;
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 20) private Status status = Status.PENDING;
    @Column(nullable = false) private int attempts;
    @Column(nullable = false) private Instant nextAttemptAt;
    private Instant sentAt;
}
