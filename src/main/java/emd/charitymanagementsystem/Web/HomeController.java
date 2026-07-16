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

    @GetMapping("/")
    public String home(Model model) {
        long totalYears = yearsService.yearsCount();
        long totalMembers = memberService.membersCount();
        long totalProjects = projectService.projectsCount();
        long totalDonationsCount = donationService.donationsCount();

        double totalDonationsAmount = donationService.getTotalDonations();
        double totalProjectCost = projectService.getTotalProjectCost();
        double totalBudgetAmount = budgetService.getTotalBudgetAmouunt();
        double remainingBudget = (totalDonationsAmount + totalBudgetAmount) - totalProjectCost;

        model.addAttribute("totalYears", totalYears);
        model.addAttribute("totalMembers", totalMembers);
        model.addAttribute("totalProjects", totalProjects);
        model.addAttribute("totalDonationsCount", totalDonationsCount);
        model.addAttribute("totalDonationsAmount", totalDonationsAmount);
        model.addAttribute("totalProjectCost", totalProjectCost);
        model.addAttribute("remainingBudget", remainingBudget);

        return "dashboard";
    }
}