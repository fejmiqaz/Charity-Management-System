package emd.charitymanagementsystem.Web;

import emd.charitymanagementsystem.DTO.years.YearsDetailsDto;
import emd.charitymanagementsystem.DTO.years.YearsFormDto;
import emd.charitymanagementsystem.DTO.years.YearsResponseDto;
import emd.charitymanagementsystem.Service.DonationService;
import emd.charitymanagementsystem.Service.ProjectService;
import emd.charitymanagementsystem.Service.YearsService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
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
    private final emd.charitymanagementsystem.Service.EventService eventService;
    private final emd.charitymanagementsystem.Service.MemberService memberService;
    private final emd.charitymanagementsystem.Service.BudgetService budgetService;
    private final emd.charitymanagementsystem.Service.Implementation.MembershipService memberships;
    private final emd.charitymanagementsystem.Service.Implementation.ActivityFinanceService activityFinance;

    @PreAuthorize("hasAnyRole('HEAD', 'SUBHEAD', 'TREASURER', 'MEMBER')")
    @GetMapping
    public String getYearsPage(
            @RequestParam(required = false) Integer yearValue,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "desc") String sortDir,
            Model model) {

        Page<YearsResponseDto> yearsPage =
                yearsService.listAll(yearValue, page, size, sortDir);

        model.addAttribute("years", yearsPage.getContent());
        model.addAttribute("yearsPage", yearsPage);

        model.addAttribute("yearValue", yearValue);
        model.addAttribute("selectedSize", size);
        model.addAttribute("sortDir", sortDir);

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
    public String getYearDetails(@PathVariable Long id, Model model,
                                org.springframework.security.core.Authentication authentication) {
        YearsDetailsDto year = yearsService.findById(id);

        double totalDonations = donationService.totalDonationsAmount(id);
        double totalProjectCosts = projectService.totalProjectCostsByYear(id);

        double budgetAmount = year.getBudgetAmount() != null ? year.getBudgetAmount() : 0.0;
        double membershipIncome = memberships.total(year.getYearValue()).doubleValue();
        double projectIncome = activityFinance.yearRevenue(id).doubleValue();
        model.addAttribute("membershipIncome", membershipIncome);
        var membershipTotals = memberships.paymentTotals(year.getYearValue());
        model.addAttribute("membershipTotals", membershipTotals);
        model.addAttribute("projectIncome", projectIncome);
        model.addAttribute("donationTotals", donationService.totalsByCurrency(id));
        model.addAttribute("projectIncomeTotals", activityFinance.yearRevenueTotals(id));
        model.addAttribute("taskPaymentTotals", activityFinance.yearTaskPaymentTotals(id));
        var activityBalance = new java.util.EnumMap<emd.charitymanagementsystem.Models.Currency, java.math.BigDecimal>(emd.charitymanagementsystem.Models.Currency.class);
        var donationsByCurrency = donationService.totalsByCurrency(id);
        var incomeByCurrency = activityFinance.yearRevenueTotals(id);
        var paymentsByCurrency = activityFinance.yearTaskPaymentTotals(id);
        for (var currency : emd.charitymanagementsystem.Models.Currency.values())
            activityBalance.put(currency, donationsByCurrency.get(currency).add(incomeByCurrency.get(currency))
                    .add(membershipTotals.get(currency)).subtract(paymentsByCurrency.get(currency)));
        model.addAttribute("activityBalance", activityBalance);
        double remainingBudget = budgetAmount + totalDonations + membershipIncome + projectIncome - totalProjectCosts;
        boolean exceedsBudget = remainingBudget < 0;

        model.addAttribute("year", year);
        model.addAttribute("totalDonations", totalDonations);
        model.addAttribute("totalProjectCosts", totalProjectCosts);
        model.addAttribute("budgetAmount", budgetAmount);
        model.addAttribute("remainingBudget", remainingBudget);
        model.addAttribute("exceedsBudget", exceedsBudget);
        var donations = donationService.findByYearId(id);
        model.addAttribute("donations", donations);
        model.addAttribute("donatingMembers", memberService.findAllByIds(donations.stream()
                .filter(donation -> donation.getMemberIds() != null)
                .flatMap(donation -> donation.getMemberIds().stream()).distinct().toList()));
        model.addAttribute("yearMembers", memberService.findByYearId(id));
        model.addAttribute("budget", year.getBudgetId() == null ? null : budgetService.findById(year.getBudgetId()));
        model.addAttribute("paidMemberships", memberships.paidMembers(year.getYearValue()));
        model.addAttribute("membershipFee", memberships.fee(year.getYearValue()));
        boolean canViewProjectsAndEvents = authentication.getAuthorities().stream()
                .anyMatch(authority -> java.util.Set.of("ROLE_HEAD", "ROLE_SUBHEAD", "ROLE_MEMBER")
                        .contains(authority.getAuthority()));
        if (canViewProjectsAndEvents) {
            model.addAttribute("projects", projectService.findByYearId(id));
            model.addAttribute("events", eventService.findAllByYearId(id));
        }
        boolean canManageMemberships = authentication.getAuthorities().stream()
                .anyMatch(authority -> java.util.Set.of("ROLE_HEAD", "ROLE_SUBHEAD", "ROLE_TREASURER")
                        .contains(authority.getAuthority()));
        if (canManageMemberships) {
            model.addAttribute("membershipPayments", memberships.payments(year.getYearValue()));
        }

        return "years/details";
    }

    @PreAuthorize("hasAnyRole('HEAD')")
    @GetMapping("/edit-form/{id}")
    public String getEditForm(@PathVariable Long id, Model model) {
        model.addAttribute("year", yearsService.findFormById(id));
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
