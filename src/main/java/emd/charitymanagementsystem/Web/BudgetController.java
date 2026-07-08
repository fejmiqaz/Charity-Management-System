package emd.charitymanagementsystem.Web;

import emd.charitymanagementsystem.DTO.budget.BudgetFormDto;
import emd.charitymanagementsystem.DTO.budget.BudgetResponseDto;
import emd.charitymanagementsystem.Models.Years;
import emd.charitymanagementsystem.Service.BudgetService;
import emd.charitymanagementsystem.Service.YearsService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/years/{yearId}/budget")
@AllArgsConstructor
public class BudgetController {

    private final BudgetService budgetService;
    private final YearsService yearsService;

    @PreAuthorize("hasAnyRole('HEAD', 'SUBHEAD', 'TREASURER', 'MEMBER')")
    @GetMapping
    public String getBudgetPage(@PathVariable Long yearId, Model model) {
        Years year = yearsService.findEntityById(yearId);
        model.addAttribute("year", year);

        if (year.getBudget() != null) {
            model.addAttribute("budgets", List.of(year.getBudget()));
        } else {
            model.addAttribute("budgets", List.of());
        }

        return "budgets/list";
    }

    @PreAuthorize("hasAnyRole('HEAD', 'TREASURER')")
    @GetMapping("/add-form")
    public String getAddForm(@PathVariable Long yearId, Model model) {
        Years year = yearsService.findEntityById(yearId);

        BudgetFormDto budgetFormDto = new BudgetFormDto();
        budgetFormDto.setYearId(yearId);

        model.addAttribute("year", year);
        model.addAttribute("budget", budgetFormDto);

        return "budgets/form";
    }

    @PreAuthorize("hasAnyRole('HEAD', 'TREASURER')")
    @PostMapping("/add")
    public String saveBudget(@PathVariable Long yearId,
                             @Valid @ModelAttribute("budget") BudgetFormDto budgetFormDto,
                             BindingResult bindingResult,
                             Model model) {
        Years year = yearsService.findEntityById(yearId);
        budgetFormDto.setYearId(yearId);

        if (bindingResult.hasErrors()) {
            model.addAttribute("year", year);
            return "budgets/form";
        }

        budgetService.create(budgetFormDto);
        return "redirect:/years/" + yearId + "/budget";
    }

    @PreAuthorize("hasAnyRole('HEAD', 'SUBHEAD', 'TREASURER', 'MEMBER')")
    @GetMapping("/details/{budgetId}")
    public String getDetailsPage(@PathVariable Long yearId,
                                 @PathVariable Long budgetId,
                                 Model model) {
        Years year = yearsService.findEntityById(yearId);
        BudgetResponseDto budget = budgetService.findById(budgetId);

        model.addAttribute("year", year);
        model.addAttribute("budget", budget);
        return "budgets/details";
    }

    @PreAuthorize("hasAnyRole('HEAD', 'TREASURER')")
    @GetMapping("/edit-form/{budgetId}")
    public String getEditForm(@PathVariable Long yearId,
                              @PathVariable Long budgetId,
                              Model model) {
        Years year = yearsService.findEntityById(yearId);
        BudgetResponseDto budget = budgetService.findById(budgetId);

        BudgetFormDto budgetFormDto = new BudgetFormDto();
        budgetFormDto.setId(budget.getId());
        budgetFormDto.setBudgetAmount(budget.getBudgetAmount());
        budgetFormDto.setDescription(budget.getDescription());
        budgetFormDto.setDonationIds(budget.getDonationIds());
        budgetFormDto.setYearId(budget.getYearId());
        budgetFormDto.setMemberIds(budget.getMemberIds());

        model.addAttribute("year", year);
        model.addAttribute("budget", budgetFormDto);

        return "budgets/form";
    }

    @PreAuthorize("hasAnyRole('HEAD', 'TREASURER')")
    @PostMapping("/edit/{budgetId}")
    public String updateBudget(@PathVariable Long yearId,
                               @PathVariable Long budgetId,
                               @Valid @ModelAttribute("budget") BudgetFormDto budgetFormDto,
                               BindingResult bindingResult,
                               Model model) {
        Years year = yearsService.findEntityById(yearId);
        budgetFormDto.setId(budgetId);
        budgetFormDto.setYearId(yearId);

        if (bindingResult.hasErrors()) {
            model.addAttribute("year", year);
            return "budgets/form";
        }

        budgetService.update(budgetId, budgetFormDto);
        return "redirect:/years/" + yearId + "/budget";
    }

    @PreAuthorize("hasAnyRole('HEAD', 'TREASURER')")
    @PostMapping("/delete/{budgetId}")
    public String deleteBudget(@PathVariable Long yearId,
                               @PathVariable Long budgetId) {
        budgetService.delete(budgetId);
        return "redirect:/years/" + yearId + "/budget";
    }
}