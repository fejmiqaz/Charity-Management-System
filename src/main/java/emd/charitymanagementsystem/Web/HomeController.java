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

    @GetMapping("/")
    public String home(Model model) {
        long totalYears = yearsService.yearsCount();
        long totalMembers = memberService.membersCount();
        long totalProjects = projectService.projectsCount();
        long totalDonationsCount = donationService.donationsCount();

        double totalDonationsAmount = donationService.getTotalDonations();
        double totalProjectCost = projectService.getTotalProjectCost();
        double totalBudgetAmount = budgetService.getTotalBudgetAmouunt();
        double membershipIncome = memberships.total().doubleValue();
        double remainingBudget = (totalDonationsAmount + totalBudgetAmount + membershipIncome) - totalProjectCost;
        model.addAttribute("membershipIncome", membershipIncome);

        model.addAttribute("totalYears", totalYears);
        model.addAttribute("totalMembers", totalMembers);
        model.addAttribute("totalProjects", totalProjects);
        model.addAttribute("totalDonationsCount", totalDonationsCount);
        model.addAttribute("totalDonationsAmount", totalDonationsAmount);
        model.addAttribute("totalProjectCost", totalProjectCost);
        model.addAttribute("remainingBudget", remainingBudget);
        model.addAttribute("budgetWarnings", budgetWarnings.warnings());

        return "dashboard";
    }
}
