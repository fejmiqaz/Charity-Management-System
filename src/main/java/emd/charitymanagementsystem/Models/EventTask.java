package emd.charitymanagementsystem.Models;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.util.*;

@Entity @Getter @Setter @NoArgsConstructor
public class EventTask {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @ManyToOne(optional = false) private Event event;
    @Column(nullable = false, length = 160) private String title;
    @Column(length = 800) private String description;
    @Column(nullable = false, precision = 12, scale = 2) private BigDecimal price = BigDecimal.ZERO;
    @Column(nullable = false) private boolean completed;
    @ManyToMany
    @JoinTable(name = "event_task_members", joinColumns = @JoinColumn(name = "task_id"), inverseJoinColumns = @JoinColumn(name = "member_id"))
    private Set<Member> members = new LinkedHashSet<>();
    @OneToMany(mappedBy = "task", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<TaskPayment> payments = new ArrayList<>();
    @Transient public BigDecimal getTotalPaid() { return getPaymentTotals().get(Currency.EUR); }
    @Transient public Map<Currency, BigDecimal> getPaymentTotals() {
        Map<Currency, BigDecimal> totals = new EnumMap<>(Currency.class);
        for (Currency currency : Currency.values()) totals.put(currency, BigDecimal.ZERO.setScale(2));
        for (TaskPayment payment : payments) totals.merge(payment.getCurrency(), payment.getAmount(), BigDecimal::add);
        return totals;
    }
}
