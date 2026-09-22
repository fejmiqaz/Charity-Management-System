package emd.charitymanagementsystem.Service.Implementation;

import emd.charitymanagementsystem.Models.*;
import emd.charitymanagementsystem.Models.Currency;
import emd.charitymanagementsystem.Repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.access.prepost.PreAuthorize;
import jakarta.persistence.EntityManager;
import jakarta.persistence.LockModeType;

import java.math.*;
import java.time.*;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MembershipService {
    private final MembershipPaymentRepository payments;
    private final MembershipFeeRepository fees;
    private final YearsRepository organizationYears;
    private final EntityManager entities;
    @Value("${app.membership.default-fee:10.00}")
    private BigDecimal defaultFee;

    public int currentYear() {
        return LocalDate.now(ZoneId.of("Europe/Skopje")).getYear();
    }

    public int checkedYear(Integer year) {
        int value = year == null ? currentYear() : year;
        if (value < 1900 || value > 2200)
            throw new IllegalArgumentException("Choose a membership year between 1900 and 2200.");
        return value;
    }

    public BigDecimal fee(int year) {
        return fees.findById(checkedYear(year)).map(MembershipFee::getAmount).orElse(defaultFee);
    }

    public List<Integer> years() {
        var years = new TreeSet<Integer>(Comparator.reverseOrder());
        years.add(currentYear());
        years.add(currentYear() - 1);
        years.add(currentYear() + 1);
        organizationYears.findAll().forEach(y -> years.add(y.getYearValue()));
        years.addAll(payments.years());
        fees.findAll().forEach(f -> years.add(f.getMembershipYear()));
        return new ArrayList<>(years);
    }

    public List<MembershipPayment> payments(int year) {
        return payments.findByMembershipYearOrderByRecordedAtDesc(checkedYear(year));
    }

    public List<MembershipPayment> history(Long memberId) {
        return payments.findByMemberIdOrderByMembershipYearDescRecordedAtDesc(memberId);
    }

    public Map<Long, MembershipPayment> paidMembers(int year) {
        return payments(year).stream().filter(MembershipPayment::isPaid).collect(Collectors.toMap(MembershipPayment::getMemberId, p -> p));
    }

    public BigDecimal total() {
        return paymentTotals(null).get(Currency.EUR);
    }

    public BigDecimal total(int year) {
        return paymentTotals(checkedYear(year)).get(Currency.EUR);
    }

    public Map<Currency, BigDecimal> paymentTotals(Integer year) {
        Map<Currency, BigDecimal> totals = new EnumMap<>(Currency.class);
        for (Currency currency : Currency.values()) totals.put(currency, BigDecimal.ZERO.setScale(2));
        for (Object[] row : payments.totalsByCurrency(year))
            totals.put((Currency) row[0], (BigDecimal) row[1]);
        return totals;
    }

    @Transactional
    @PreAuthorize("hasAnyRole('HEAD','SUBHEAD','TREASURER')")
    public void setFee(int year, BigDecimal amount) {
        checkedYear(year);
        validateAmount(amount);
        MembershipFee fee = fees.findById(year).orElseGet(MembershipFee::new);
        fee.setMembershipYear(year);
        fee.setAmount(amount.setScale(2));
        fees.save(fee);
    }

    private void validateAmount(BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0 || amount.compareTo(new BigDecimal("9999999999.99")) > 0 || amount.stripTrailingZeros().scale() > 2)
            throw new IllegalArgumentException("Enter a positive fee with at most two decimal places.");
    }

    @Transactional
    @PreAuthorize("hasAnyRole('HEAD','SUBHEAD','TREASURER')")
    public MembershipPayment record(Long memberId, int year, LocalDate paidOn, String recordedBy) {
        return record(memberId, year, fee(year), Currency.EUR, paidOn, recordedBy);
    }

    @Transactional
    @PreAuthorize("hasAnyRole('HEAD','SUBHEAD','TREASURER')")
    public MembershipPayment record(Long memberId, int year, BigDecimal amount, Currency currency, LocalDate paidOn, String recordedBy) {
        checkedYear(year);
        if (paidOn == null || paidOn.isAfter(LocalDate.now(ZoneId.of("Europe/Skopje"))))
            throw new IllegalArgumentException("Payment date must be today or earlier.");
        // Serialize recording for this member so double clicks cannot create two active receipts.
        Member member = entities.find(Member.class, memberId, LockModeType.PESSIMISTIC_WRITE);
        if (member == null) throw new IllegalArgumentException("Member not found.");
        if (payments.existsByMemberIdAndMembershipYearAndVoidedAtIsNull(memberId, year))
            throw new IllegalArgumentException("Membership is already paid for this year.");
        validateAmount(amount);
        if (currency == null) throw new IllegalArgumentException("Choose a currency.");
        var payment = new MembershipPayment();
        payment.setMemberId(memberId);
        payment.setMemberName(member.getName() + " " + member.getSurname());
        payment.setMembershipYear(year);
        payment.setActiveKey(memberId + ":" + year);
        payment.setAmount(amount.setScale(2));
        payment.setCurrency(currency);
        payment.setPaidOn(paidOn);
        payment.setRecordedAt(Instant.now());
        payment.setRecordedBy(recordedBy);
        return payments.saveAndFlush(payment);
    }

    @Transactional
    @PreAuthorize("hasAnyRole('HEAD','SUBHEAD','TREASURER')")
    public void voidPayment(Long id, String reason, String actor) {
        if (reason == null || reason.isBlank() || reason.length() > 500)
            throw new IllegalArgumentException("Give a correction reason (up to 500 characters).");
        var payment = entities.find(MembershipPayment.class, id, LockModeType.PESSIMISTIC_WRITE);
        if (payment == null) throw new IllegalArgumentException("Payment not found.");
        if (!payment.isPaid()) throw new IllegalArgumentException("This payment has already been voided.");
        payment.setVoidedAt(Instant.now());
        payment.setVoidedBy(actor);
        payment.setVoidReason(reason.trim());
        payment.setActiveKey(null);
    }
}
