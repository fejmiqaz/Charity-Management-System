package emd.charitymanagementsystem.Models;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.*;

@Entity @Getter @Setter @NoArgsConstructor
@Table(indexes = @Index(name="project_revenue_month", columnList="project_id,revenueMonth"))
public class ProjectRevenue {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @ManyToOne(optional = false) private Project project;
    @Column(nullable = false) private LocalDate revenueMonth;
    @Column(nullable = false, length = 160) private String customer;
    @Column(nullable = false, precision = 12, scale = 2) private BigDecimal amount;
    @Enumerated(EnumType.STRING) @Column(nullable = false, columnDefinition = "varchar(3) default 'EUR'") private Currency currency = Currency.EUR;
    @Column(length = 400) private String note;
    @Column(nullable = false) private String recordedBy;
    @Column(nullable = false) private Instant recordedAt;
}
