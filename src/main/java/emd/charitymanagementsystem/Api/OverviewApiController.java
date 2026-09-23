package emd.charitymanagementsystem.Api;

import emd.charitymanagementsystem.Models.*;
import emd.charitymanagementsystem.Models.Currency;
import emd.charitymanagementsystem.DTO.years.YearsResponseDto;
import emd.charitymanagementsystem.Service.*;
import emd.charitymanagementsystem.Service.Implementation.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.*;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class OverviewApiController {
    private final YearsService years;
    private final MemberService members;
    private final ProjectService projects;
    private final DonationService donations;
    private final BudgetService budgets;
    private final MembershipService memberships;
    private final ActivityFinanceService activity;
    private final BudgetWarningService warnings;

    public record Dashboard(long totalYears, long totalMembers, long totalProjects, long totalDonationsCount,
                            Map<Currency, BigDecimal> donationTotals, double totalProjectCost, double membershipIncome,
                            double projectIncome,
                            double remainingBudget, List<BudgetWarningService.Warning> budgetWarnings) {
    }

    @GetMapping("/dashboard")
    public Dashboard dashboard() {
        var totals = donations.totalsByCurrency(null);
        double cost = projects.getTotalProjectCost();
        double membership = memberships.total().doubleValue();
        double income = activity.totalRevenue().doubleValue();
        return new Dashboard(years.yearsCount(), members.membersCount(), projects.projectsCount(), donations.donationsCount(),
                totals, cost, membership, income, budgets.getTotalBudgetAmouunt() + totals.get(Currency.EUR).doubleValue() + membership + income - cost, warnings.warnings());
    }

    @GetMapping("/options/years")
    public List<YearsResponseDto> years() {
        return years.listAll();
    }

    @GetMapping("/options/members")
    @PreAuthorize("hasAnyRole('HEAD','SUBHEAD','TREASURER','PROJECT_MANAGER','EVENT_MANAGER')")
    public List<ActivityApiController.MemberRef> members() {
        return members.listAll().stream().map(m -> new ActivityApiController.MemberRef(m.getId(), m.getName(), m.getSurname())).toList();
    }

    @GetMapping("/options/enums")
    public Map<String, Object> enums() {
        return Map.of("roles", Role.values(), "currencies", Currency.values(), "projectTypes", ProjectType.values(),
                "projectStatuses", ProjectStatus.values(), "eventTypes", EventType.values(), "eventStatuses", EventStatus.values());
    }
}
