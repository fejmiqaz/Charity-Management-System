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

    @Query("select coalesce(sum(p.amount),0) from MembershipPayment p where p.voidedAt is null")
    BigDecimal total();

    @Query("select coalesce(sum(p.amount),0) from MembershipPayment p where p.voidedAt is null and p.membershipYear=:year")
    BigDecimal totalForYear(Integer year);
}
