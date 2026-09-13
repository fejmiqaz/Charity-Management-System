package emd.charitymanagementsystem.Web;

import emd.charitymanagementsystem.Service.Implementation.MembershipService;
import emd.charitymanagementsystem.Service.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import java.math.BigDecimal;

@Controller
@RequestMapping("/memberships")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('HEAD','SUBHEAD','TREASURER')")
public class MembershipController {
    private final MembershipService memberships;
    private final MemberService members;

    @GetMapping
    public String index(@RequestParam(required = false) Integer year, @RequestParam(required = false) Long memberId, Model model) {
        int selectedYear = memberships.checkedYear(year);
        model.addAttribute("membershipYear", selectedYear);
        model.addAttribute("membershipYears", memberships.years());
        model.addAttribute("membershipFee", memberships.fee(selectedYear));
        model.addAttribute("membershipTotal", memberships.total(selectedYear));
        model.addAttribute("payments", memberId == null ? memberships.payments(selectedYear) : memberships.history(memberId));
        model.addAttribute("members", members.listAll());
        model.addAttribute("selectedMember", memberId);
        model.addAttribute("today", LocalDate.now(java.time.ZoneId.of("Europe/Skopje")));
        return "memberships/index";
    }

    @PostMapping("/record")
    public String record(@RequestParam Long memberId, @RequestParam int year,
                         @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate paidOn,
                         Authentication authentication, RedirectAttributes flash) {
        try {
            memberships.record(memberId, year, paidOn, authentication.getName());
            flash.addFlashAttribute("membershipSuccess", "Membership payment recorded.");
        } catch (IllegalArgumentException e) {
            flash.addFlashAttribute("membershipError", e.getMessage());
        } catch (org.springframework.dao.DataIntegrityViolationException e) {
            flash.addFlashAttribute("membershipError", "A payment was already recorded for this member and year. Refresh the ledger before trying again.");
        }
        return "redirect:/memberships?year=" + memberships.checkedYear(year);
    }

    @PostMapping("/fee")
    public String fee(@RequestParam int year, @RequestParam BigDecimal amount, RedirectAttributes flash) {
        try {
            memberships.setFee(year, amount);
            flash.addFlashAttribute("membershipSuccess", "Fee updated for new payments. Existing receipts are unchanged.");
        } catch (IllegalArgumentException e) {
            flash.addFlashAttribute("membershipError", e.getMessage());
        }
        return "redirect:/memberships?year=" + memberships.checkedYear(year);
    }

    @PostMapping("/{id}/void")
    public String voidPayment(@PathVariable Long id, @RequestParam int year, @RequestParam String reason, Authentication authentication, RedirectAttributes flash) {
        try {
            memberships.voidPayment(id, reason, authentication.getName());
            flash.addFlashAttribute("membershipSuccess", "Payment voided. The original receipt remains in history.");
        } catch (IllegalArgumentException e) {
            flash.addFlashAttribute("membershipError", e.getMessage());
        }
        return "redirect:/memberships?year=" + memberships.checkedYear(year);
    }
}
