package emd.charitymanagementsystem.Repository;

import emd.charitymanagementsystem.Models.MembershipPayment;
import org.springframework.data.jpa.repository.*;

import java.math.BigDecimal;
import java.util.*;

public interface MembershipPaymentRepository extends JpaRepository<MembershipPayment, Long> {
    List<MembershipPayment> findByMembershipYearOrderByRecordedAtDesc(Integer year);

    List<MembershipPayment> findByMemberIdOrderByMembershipYearDescRecordedAtDesc(Long memberId);

    boolean existsByMemberIdAndMembershipYearAndVoidedAtIsNull(Long memberId, Integer year);

    @Query("select distinct p.membershipYear from MembershipPayment p")
    List<Integer> years();

    @Query("select coalesce(sum(p.amount),0) from MembershipPayment p where p.voidedAt is null and p.currency = emd.charitymanagementsystem.Models.Currency.EUR")
    BigDecimal total();

    @Query("select coalesce(sum(p.amount),0) from MembershipPayment p where p.voidedAt is null and p.membershipYear=:year and p.currency = emd.charitymanagementsystem.Models.Currency.EUR")
    BigDecimal totalForYear(Integer year);
    @Query("select p.currency, sum(p.amount) from MembershipPayment p where p.voidedAt is null and (:year is null or p.membershipYear=:year) group by p.currency")
    List<Object[]> totalsByCurrency(Integer year);
}
