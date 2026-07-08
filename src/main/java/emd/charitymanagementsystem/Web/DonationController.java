package emd.charitymanagementsystem.Web;

import emd.charitymanagementsystem.DTO.donation.DonationFormDto;
import emd.charitymanagementsystem.Models.Years;
import emd.charitymanagementsystem.Service.DonationService;
import emd.charitymanagementsystem.Service.MemberService;
import emd.charitymanagementsystem.Service.YearsService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;

@Controller
@RequestMapping("/years/{yearId}/donations")
@AllArgsConstructor
public class DonationController {

    private final DonationService donationService;
    private final YearsService yearsService;
    private final MemberService memberService;

    @PreAuthorize("hasAnyRole('HEAD', 'SUBHEAD', 'TREASURER', 'MEMBER')")
    @GetMapping
    public String listDonations(@PathVariable Long yearId, Model model) {
        Years year = yearsService.findEntityById(yearId);
        model.addAttribute("year", year);
        model.addAttribute("donations", donationService.findByYearId(yearId));
        model.addAttribute("totalDonationsAmount", donationService.totalDonationsAmount(yearId));
        return "donations/list";
    }

    @PreAuthorize("hasAnyRole('HEAD', 'SUBHEAD', 'TREASURER')")
    @GetMapping("/add")
    public String showAddForm(@PathVariable Long yearId, Model model) {
        Years year = yearsService.findEntityById(yearId);
        DonationFormDto donationFormDto = new DonationFormDto();
        donationFormDto.setYearId(yearId);

        model.addAttribute("year", year);
        model.addAttribute("donation", donationFormDto);
        model.addAttribute("members", memberService.listAll());
        return "donations/form";
    }

    @PreAuthorize("hasAnyRole('HEAD', 'SUBHEAD', 'TREASURER')")
    @PostMapping("/add")
    public String saveDonation(@PathVariable Long yearId,
                               @Valid @ModelAttribute("donation") DonationFormDto donationFormDto,
                               BindingResult bindingResult,
                               Model model) {
        Years year = yearsService.findEntityById(yearId);
        donationFormDto.setYearId(yearId);

        if (bindingResult.hasErrors()) {
            model.addAttribute("year", year);
            model.addAttribute("members", memberService.listAll());
            return "donations/form";
        }

        donationService.create(donationFormDto);
        return "redirect:/years/" + yearId + "/donations";
    }

    @PreAuthorize("hasAnyRole('HEAD', 'SUBHEAD', 'TREASURER', 'MEMBER')")
    @GetMapping("/{donationId}")
    public String details(@PathVariable Long yearId,
                          @PathVariable Long donationId,
                          Model model) {
        model.addAttribute("year", yearsService.findById(yearId));
        model.addAttribute("donation", donationService.findById(donationId));
        model.addAttribute("members", memberService.findAllByIds(donationService.findById(donationId).getMemberIds()));
        return "donations/details";
    }

    @PreAuthorize("hasAnyRole('HEAD', 'SUBHEAD', 'TREASURER')")
    @GetMapping("/{donationId}/edit")
    public String showEditForm(@PathVariable Long yearId,
                               @PathVariable Long donationId,
                               Model model) {
        Years year = yearsService.findEntityById(yearId);
        var donationResponse = donationService.findById(donationId);

        DonationFormDto formDto = new DonationFormDto();
        formDto.setId(donationResponse.getId());
        formDto.setDonationAmount(donationResponse.getDonationAmount());
        formDto.setYearId(yearId);
        formDto.setMemberIds(donationResponse.getMemberIds());

        model.addAttribute("year", year);
        model.addAttribute("donation", formDto);
        model.addAttribute("members", memberService.listAll());

        return "donations/form";
    }

    @PreAuthorize("hasAnyRole('HEAD', 'SUBHEAD', 'TREASURER')")
    @PostMapping("/{donationId}/edit")
    public String updateDonation(@PathVariable Long yearId,
                                 @PathVariable Long donationId,
                                 @Valid @ModelAttribute("donation") DonationFormDto donationFormDto,
                                 BindingResult bindingResult,
                                 Model model) {
        Years year = yearsService.findEntityById(yearId);
        donationFormDto.setId(donationId);
        donationFormDto.setYearId(yearId);

        if (bindingResult.hasErrors()) {
            model.addAttribute("year", year);
            model.addAttribute("members", memberService.listAll());
            return "donations/form";
        }

        donationService.update(donationId, donationFormDto);
        return "redirect:/years/" + yearId + "/donations";
    }

    @PreAuthorize("hasAnyRole('HEAD', 'SUBHEAD', 'TREASURER')")
    @PostMapping("/{donationId}/delete")
    public String deleteDonation(@PathVariable Long yearId,
                                 @PathVariable Long donationId) {
        donationService.delete(donationId);
        return "redirect:/years/" + yearId + "/donations";
    }
}