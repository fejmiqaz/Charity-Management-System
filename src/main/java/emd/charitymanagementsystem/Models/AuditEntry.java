package emd.charitymanagementsystem.Models;

import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;

@Entity
@Table(name="audit_entries", indexes={@Index(name="audit_time_idx",columnList="occurredAt"), @Index(name="audit_action_idx",columnList="action")})
@Getter @Setter @NoArgsConstructor
public class AuditEntry {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
    @Column(nullable=false) private Instant occurredAt;
    private Long actorId;
    @Column(length=254) private String actorEmail;
    @Column(length=254) private String actorName;
    @Column(nullable=false,length=40) private String action;
    @Column(nullable=false,length=40) private String resource;
    @Column(length=100) private String recordId;
    @Column(nullable=false,length=500) private String path;
}
