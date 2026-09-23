package emd.charitymanagementsystem.Models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import java.time.Instant;

@Entity
@Table(name = "notifications", indexes = @Index(name = "notification_recipient_created", columnList = "recipient_id,createdAt"))
@Getter @Setter @NoArgsConstructor
public class Notification {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private UserAccount recipient;
    @Column(nullable = false, unique = true, length = 200) private String deduplicationKey;
    @Column(nullable = false, length = 300) private String title;
    @Column(nullable = false, length = 2000) private String message;
    private Long eventId;
    @Column(nullable = false) private Instant createdAt;
    private Instant readAt;
}
