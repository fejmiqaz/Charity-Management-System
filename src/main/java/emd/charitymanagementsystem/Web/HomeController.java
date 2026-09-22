package emd.charitymanagementsystem.Web;

import emd.charitymanagementsystem.Service.*;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@AllArgsConstructor
public class HomeController {

    private final YearsService yearsService;
    private final MemberService memberService;
    private final ProjectService projectService;
    private final DonationService donationService;
    private final BudgetService budgetService;
    private final emd.charitymanagementsystem.Service.Implementation.MembershipService memberships;
    private final emd.charitymanagementsystem.Service.Implementation.BudgetWarningService budgetWarnings;
    private final emd.charitymanagementsystem.Service.Implementation.ActivityFinanceService activityFinance;

    @GetMapping({"/dashboard", "/home"})
    public String home(Model model) {
        long totalYears = yearsService.yearsCount();
        long totalMembers = memberService.membersCount();
        long totalProjects = projectService.projectsCount();
        long totalDonationsCount = donationService.donationsCount();

        var donationTotals = donationService.totalsByCurrency(null);
        double totalDonationsAmount = donationTotals.get(emd.charitymanagementsystem.Models.Currency.EUR).doubleValue();
        double totalProjectCost = projectService.getTotalProjectCost();
        double totalBudgetAmount = budgetService.getTotalBudgetAmouunt();
        double membershipIncome = memberships.total().doubleValue();
        double projectIncome = activityFinance.totalRevenue().doubleValue();
        double remainingBudget = (totalDonationsAmount + totalBudgetAmount + membershipIncome + projectIncome) - totalProjectCost;
        model.addAttribute("membershipIncome", membershipIncome);
        model.addAttribute("projectIncome", projectIncome);

        model.addAttribute("totalYears", totalYears);
        model.addAttribute("totalMembers", totalMembers);
        model.addAttribute("totalProjects", totalProjects);
        model.addAttribute("totalDonationsCount", totalDonationsCount);
        model.addAttribute("totalDonationsAmount", totalDonationsAmount);
        model.addAttribute("donationTotals", donationTotals);
        model.addAttribute("totalProjectCost", totalProjectCost);
        model.addAttribute("remainingBudget", remainingBudget);
        model.addAttribute("budgetWarnings", budgetWarnings.warnings());

        return "dashboard";
    }
}
