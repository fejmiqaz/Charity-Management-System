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

    @PreAuthorize("hasAnyRole('HEAD', 'SUBHEAD', 'TREASURER', 'MEMBER')")
    @GetMapping
    public String listMembers(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String country,
            @RequestParam(required = false) String city,
            @RequestParam(required = false) Role role,
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize,
            Model model
    ) {
        if (pageNum < 1) {
            pageNum = 1;
        }

        if (pageSize < 1) {
            pageSize = 10;
        }

        var memberPage = memberService.findPage(
                search,
                country,
                city,
                role,
                pageNum,
                pageSize
        );

        model.addAttribute("members", memberPage.getContent());
        model.addAttribute("roles", Role.values());

        model.addAttribute("currentPage", pageNum);
        model.addAttribute("totalPages", memberPage.getTotalPages());
        model.addAttribute("totalItems", memberPage.getTotalElements());
        model.addAttribute("pageSize", pageSize);

        model.addAttribute("search", search);
        model.addAttribute("country", country);
        model.addAttribute("city", city);
        model.addAttribute("selectedRole", role);

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

    @PreAuthorize("hasAnyRole('HEAD', 'SUBHEAD', 'TREASURER', 'MEMBER')")
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