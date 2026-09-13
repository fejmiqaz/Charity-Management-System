package emd.charitymanagementsystem.Models;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;

@Entity @Getter @Setter @NoArgsConstructor
public class MembershipFee {
    @Id private Integer membershipYear;
    @Column(nullable=false,precision=12,scale=2) private BigDecimal amount;
}
