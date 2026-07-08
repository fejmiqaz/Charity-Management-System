package emd.charitymanagementsystem.Web;

import emd.charitymanagementsystem.DTO.years.YearsDetailsDto;
import emd.charitymanagementsystem.DTO.years.YearsFormDto;
import emd.charitymanagementsystem.Service.DonationService;
import emd.charitymanagementsystem.Service.ProjectService;
import emd.charitymanagementsystem.Service.YearsService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/years")
@AllArgsConstructor
public class YearsController {

    private final YearsService yearsService;
    private final DonationService donationService;
    private final ProjectService projectService;

    @PreAuthorize("hasAnyRole('HEAD', 'SUBHEAD', 'TREASURER', 'MEMBER')")
    @GetMapping
    public String getYearsPage(Model model) {
        model.addAttribute("years", yearsService.listAll());
        return "years/list";
    }

    @PreAuthorize("hasAnyRole('HEAD')")
    @GetMapping("/add-form")
    public String getAddForm(Model model) {
        model.addAttribute("year", new YearsFormDto());
        return "years/form";
    }

    @PreAuthorize("hasAnyRole('HEAD')")
    @PostMapping("/add")
    public String saveYear(@Valid @ModelAttribute("year") YearsFormDto yearsFormDto,
                           BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return "years/form";
        }

        yearsService.create(yearsFormDto);
        return "redirect:/years";
    }

    @PreAuthorize("hasAnyRole('HEAD', 'SUBHEAD', 'TREASURER', 'MEMBER')")
    @GetMapping("/{id}")
    public String getYearDetails(@PathVariable Long id, Model model) {
        YearsDetailsDto year = yearsService.findById(id);

        double totalDonations = donationService.totalDonationsAmount(id);
        double totalProjectCosts = projectService.totalProjectCostsByYear(id);

        double budgetAmount = year.getBudgetAmount() != null ? year.getBudgetAmount() : 0.0;
        double remainingBudget = budgetAmount - totalProjectCosts;
        boolean exceedsBudget = totalProjectCosts > budgetAmount;

        model.addAttribute("year", year);
        model.addAttribute("totalDonations", totalDonations);
        model.addAttribute("totalProjectCosts", totalProjectCosts);
        model.addAttribute("budgetAmount", budgetAmount);
        model.addAttribute("remainingBudget", remainingBudget);
        model.addAttribute("exceedsBudget", exceedsBudget);

        return "years/details";
    }

    @PreAuthorize("hasAnyRole('HEAD')")
    @GetMapping("/edit-form/{id}")
    public String getEditForm(@PathVariable Long id, Model model) {
        model.addAttribute("year", yearsService.findById(id));
        return "years/form";
    }

    @PreAuthorize("hasAnyRole('HEAD')")
    @PostMapping("/edit/{id}")
    public String updateYear(@PathVariable Long id,
                             @Valid @ModelAttribute("year") YearsFormDto yearsFormDto,
                             BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return "years/form";
        }

        yearsService.update(id, yearsFormDto);
        return "redirect:/years";
    }

    @PreAuthorize("hasAnyRole('HEAD')")
    @PostMapping("/delete/{id}")
    public String deleteYear(@PathVariable Long id) {
        yearsService.delete(id);
        return "redirect:/years";
    }
}