package emd.charitymanagementsystem.Web;

import emd.charitymanagementsystem.DTO.member.MemberFormDto;
import emd.charitymanagementsystem.Models.Role;
import emd.charitymanagementsystem.Service.MemberService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/members")
@AllArgsConstructor
public class MemberController {

    private final MemberService memberService;

    @PreAuthorize("hasAnyRole('HEAD', 'SUBHEAD')")
    @GetMapping
    public String listMembers(Model model) {
        model.addAttribute("members", memberService.listAll());
        return "members/list";
    }

    @PreAuthorize("hasAnyRole('HEAD', 'SUBHEAD')")
    @GetMapping("/add")
    public String showAddForm(Model model) {
        model.addAttribute("member", new MemberFormDto());
        model.addAttribute("roles", Role.values());
        return "members/form";
    }

    @PreAuthorize("hasAnyRole('HEAD', 'SUBHEAD')")
    @PostMapping("/add")
    public String saveMember(@Valid @ModelAttribute("member") MemberFormDto memberFormDto,
                             BindingResult bindingResult,
                             Model model) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("roles", Role.values());
            return "members/form";
        }

        memberService.create(memberFormDto);
        return "redirect:/members";
    }

    @PreAuthorize("hasAnyRole('HEAD', 'SUBHEAD')")
    @GetMapping("/{memberId}")
    public String details(@PathVariable Long memberId, Model model) {
        model.addAttribute("member", memberService.findById(memberId));
        return "members/details";
    }

    @PreAuthorize("hasAnyRole('HEAD')")
    @GetMapping("/{memberId}/edit")
    public String showEditForm(@PathVariable Long memberId, Model model) {
        model.addAttribute("member", memberService.findByIdForEdit(memberId));
        model.addAttribute("roles", Role.values());
        return "members/form";
    }

    @PreAuthorize("hasAnyRole('HEAD')")
    @PostMapping("/{memberId}/edit")
    public String updateMember(@PathVariable Long memberId,
                               @Valid @ModelAttribute("member") MemberFormDto memberFormDto,
                               BindingResult bindingResult,
                               Model model) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("roles", Role.values());
            return "members/form";
        }

        memberService.update(memberId, memberFormDto);
        return "redirect:/members";
    }

    @PreAuthorize("hasAnyRole('HEAD')")
    @PostMapping("/{memberId}/delete")
    public String deleteMember(@PathVariable Long memberId) {
        memberService.delete(memberId);
        return "redirect:/members";
    }
}