package emd.charitymanagementsystem.Api;

import emd.charitymanagementsystem.Service.*;
import emd.charitymanagementsystem.Service.Implementation.*;
import emd.charitymanagementsystem.Models.Currency;
import emd.charitymanagementsystem.Models.MembershipPayment;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import java.math.BigDecimal;
import java.time.*;
import java.util.*;

/** Read models matching the existing Thymeleaf pages without serializing entities. */
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TemplateApiController {
    private final YearsService years;
    private final DonationService donations;
    private final ProjectService projects;
    private final EventService events;
    private final MemberService members;
    private final BudgetService budgets;
    private final MembershipService memberships;
    private final ActivityFinanceService activity;
    private final ProfileService profiles;
    private final ApiScope scope;

    public record Receipt(Long id, Long memberId, String memberName, Integer membershipYear,
                          BigDecimal amount, Currency currency, LocalDate paidOn, boolean paid,
                          String recordedBy, String voidReason) {
        static Receipt of(MembershipPayment p) {
            return new Receipt(p.getId(), p.getMemberId(), p.getMemberName(), p.getMembershipYear(),
                    p.getAmount(), p.getCurrency(), p.getPaidOn(), p.isPaid(), p.getRecordedBy(), p.getVoidReason());
        }
    }

    @GetMapping("/years/{id}/overview")
    @PreAuthorize("hasAnyRole('HEAD','SUBHEAD','TREASURER','MEMBER')")
    public Map<String, Object> year(@PathVariable Long id, Authentication auth) {
        scope.year(id);
        var year = years.findById(id);
        var result = new LinkedHashMap<String, Object>();
        var donationTotals = donations.totalsByCurrency(id);
        var membershipTotals = memberships.paymentTotals(year.getYearValue());
        var projectIncomeTotals = activity.yearRevenueTotals(id);
        var taskPaymentTotals = activity.yearTaskPaymentTotals(id);
        var balance = new EnumMap<Currency, BigDecimal>(Currency.class);
        for (var c : Currency.values()) balance.put(c, donationTotals.get(c).add(projectIncomeTotals.get(c))
                .add(membershipTotals.get(c)).subtract(taskPaymentTotals.get(c)));
        double budget = year.getBudgetAmount() == null ? 0 : year.getBudgetAmount();
        double cost = projects.totalProjectCostsByYear(id);
        double remaining = budget + donations.totalDonationsAmount(id)
                + memberships.total(year.getYearValue()).doubleValue() + activity.yearRevenue(id).doubleValue() - cost;
        result.put("year", year);
        result.put("budgetAmount", budget);
        result.put("totalProjectCosts", cost);
        result.put("remainingBudget", remaining);
        result.put("exceedsBudget", remaining < 0);
        result.put("donationTotals", donationTotals);
        result.put("membershipTotals", membershipTotals);
        result.put("projectIncomeTotals", projectIncomeTotals);
        result.put("taskPaymentTotals", taskPaymentTotals);
        result.put("activityBalance", balance);
        result.put("budget", year.getBudgetId() == null ? null : budgets.findById(year.getBudgetId()));
        var donationRows = donations.findByYearId(id);
        result.put("donations", donationRows);
        result.put("donatingMembers", members.findAllByIds(donationRows.stream()
                .filter(d -> d.getMemberIds() != null).flatMap(d -> d.getMemberIds().stream()).distinct().toList()));
        result.put("yearMembers", members.findByYearId(id));
        result.put("membershipFee", memberships.fee(year.getYearValue()));
        var paid = new LinkedHashMap<Long, Receipt>();
        memberships.paidMembers(year.getYearValue()).forEach((member, receipt) -> paid.put(member, Receipt.of(receipt)));
        result.put("paidMemberships", paid);
        if (hasRole(auth, "HEAD", "SUBHEAD", "MEMBER")) {
            result.put("projects", projects.findByYearId(id));
            result.put("events", events.findAllByYearId(id));
        }
        if (hasRole(auth, "HEAD", "SUBHEAD", "TREASURER"))
            result.put("membershipPayments", memberships.payments(year.getYearValue()).stream().map(Receipt::of).toList());
        return result;
    }

    @GetMapping("/years/{id}/projects/summary")
    @PreAuthorize("hasAnyRole('HEAD','SUBHEAD','PROJECT_MANAGER','VOLUNTEER','MEMBER')")
    public Map<String, Object> projectSummary(@PathVariable Long id) {
        scope.year(id);
        var year = years.findById(id);
        return Map.of("budgetAmount", year.getBudgetAmount() == null ? 0 : year.getBudgetAmount(),
                "totalProjectCost", projects.totalProjectCostsByYear(id));
    }

    @GetMapping("/profile/overview")
    public Map<String, Object> profile(Authentication auth) {
        var account = profiles.account(auth.getName());
        var member = account.getMember();
        int year = memberships.currentYear();
        var result = new LinkedHashMap<String, Object>();
        result.put("name", account.getName()); result.put("email", account.getEmail()); result.put("role", account.getRole());
        result.put("member", member == null ? null : profiles.form(auth.getName()));
        result.put("membershipYear", year); result.put("membershipFee", memberships.fee(year));
        result.put("membershipPaid", member != null && memberships.paidMembers(year).containsKey(member.getId()));
        result.put("membershipHistory", member == null ? List.of() : memberships.history(member.getId()).stream().map(Receipt::of).toList());
        return result;
    }

    @GetMapping("/memberships/status")
    @PreAuthorize("hasAnyRole('HEAD','SUBHEAD','TREASURER','MEMBER')")
    public Map<String, Object> status(@RequestParam(required = false) Integer year) {
        int selected = memberships.checkedYear(year);
        var paid = new LinkedHashMap<Long, Map<String, Object>>();
        memberships.paidMembers(selected).forEach((id, p) -> paid.put(id, Map.of("amount", p.getAmount(), "currency", p.getCurrency())));
        return Map.of("year", selected, "fee", memberships.fee(selected), "paidMemberships", paid);
    }

    @GetMapping("/memberships/overview")
    @PreAuthorize("hasAnyRole('HEAD','SUBHEAD','TREASURER')")
    public Map<String, Object> ledger(@RequestParam(required = false) Integer year,
                                      @RequestParam(required = false) Long memberId) {
        int selected = memberships.checkedYear(year);
        return Map.of("year", selected, "fee", memberships.fee(selected), "totals", memberships.paymentTotals(selected),
                "payments", (memberId == null ? memberships.payments(selected) : memberships.history(memberId)).stream().map(Receipt::of).toList());
    }

    private static boolean hasRole(Authentication auth, String... roles) {
        var allowed = Arrays.stream(roles).map(r -> "ROLE_" + r).toList();
        return auth.getAuthorities().stream().anyMatch(a -> allowed.contains(a.getAuthority()));
    }
}
