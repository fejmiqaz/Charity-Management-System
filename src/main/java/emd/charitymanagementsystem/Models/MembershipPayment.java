package emd.charitymanagementsystem.Models;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.*;

/**
 * Historical receipt. Scalar member ID and name deliberately survive member deletion.
 */
@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(indexes = {@Index(name = "membership_member_year", columnList = "memberId,membershipYear")})
public class MembershipPayment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false)
    private Long memberId;
    @Column(nullable = false)
    private String memberName;
    @Column(nullable = false)
    private Integer membershipYear;
    // A nullable unique key permits many voided receipts, but only one paid receipt per member/year.
    @Column(unique = true, length = 64)
    private String activeKey;
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal amount;
    @Column(nullable = false)
    private LocalDate paidOn;
    @Column(nullable = false)
    private Instant recordedAt;
    @Column(nullable = false)
    private String recordedBy;
    private Instant voidedAt;
    private String voidedBy;
    @Column(length = 500)
    private String voidReason;

    public boolean isPaid() {
        return voidedAt == null;
    }
}
