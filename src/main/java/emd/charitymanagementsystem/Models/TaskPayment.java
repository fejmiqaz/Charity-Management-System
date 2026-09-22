package emd.charitymanagementsystem.Models;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.*;

@Entity @Getter @Setter @NoArgsConstructor
public class TaskPayment {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @ManyToOne(optional = false) private EventTask task;
    @ManyToOne private Member member;
    @Column(nullable = false, precision = 12, scale = 2) private BigDecimal amount;
    @Enumerated(EnumType.STRING) @Column(nullable = false, columnDefinition = "varchar(3) default 'EUR'") private Currency currency = Currency.EUR;
    @Column(nullable = false) private LocalDate paidOn;
    @Column(length = 400) private String note;
    @Column(nullable = false) private String recordedBy;
    @Column(nullable = false) private Instant recordedAt;
}
