package emd.charitymanagementsystem.Web;

import emd.charitymanagementsystem.DTO.member.MemberFormDto;
import emd.charitymanagementsystem.DTO.member.MemberResponseDto;
import emd.charitymanagementsystem.Models.Role;
import emd.charitymanagementsystem.Service.MemberService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
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
        model.addAttribute("editMode", false);

        return "members/form";
    }

    @PreAuthorize("hasAnyRole('HEAD', 'SUBHEAD')")
    @PostMapping("/add")
    public String saveMember(
            @Valid @ModelAttribute("member") MemberFormDto memberFormDto,
            BindingResult bindingResult,
            Model model
    ) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("roles", Role.values());
            model.addAttribute("editMode", false);

            return "members/form";
        }

        memberService.create(memberFormDto);

        return "redirect:/members";
    }

    @PreAuthorize("hasAnyRole('HEAD', 'SUBHEAD', 'TREASURER', 'MEMBER')")
    @GetMapping("/{memberId}")
    public String details(
            @PathVariable Long memberId,
            Model model
    ) {
        model.addAttribute("member", memberService.findById(memberId));

        return "members/details";
    }

    @PreAuthorize("hasAnyRole('HEAD', 'MEMBER')")
    @GetMapping("/{memberId}/edit")
    public String showEditForm(
            @PathVariable Long memberId,
            Authentication authentication,
            Model model
    ) {
        MemberResponseDto member = memberService.findById(memberId);

        boolean isHead = authentication.getAuthorities()
                .stream()
                .anyMatch(authority ->
                        authority.getAuthority().equals("ROLE_HEAD")
                );

        boolean isOwner = member.getEmail()
                .equalsIgnoreCase(authentication.getName());

        if (!isHead && !isOwner) {
            return "redirect:/access-denied";
        }

        MemberFormDto memberFormDto = new MemberFormDto();

        memberFormDto.setId(member.getId());
        memberFormDto.setName(member.getName());
        memberFormDto.setSurname(member.getSurname());
        memberFormDto.setEmail(member.getEmail());
        memberFormDto.setPhone(member.getPhone());
        memberFormDto.setCountry(member.getCountry());
        memberFormDto.setCity(member.getCity());
        memberFormDto.setRole(member.getRole());
        memberFormDto.setYearId(member.getYearId());
        memberFormDto.setPassword(null);

        model.addAttribute("member", memberFormDto);
        model.addAttribute("memberId", memberId);
        model.addAttribute("roles", Role.values());
        model.addAttribute("editMode", true);

        return "members/form";
    }

    @PreAuthorize("hasAnyRole('HEAD', 'MEMBER')")
    @PostMapping("/{memberId}/edit")
    public String updateMember(
            @PathVariable Long memberId,
            @Valid @ModelAttribute("member") MemberFormDto memberFormDto,
            BindingResult bindingResult,
            Authentication authentication,
            Model model
    ) {
        MemberResponseDto existingMember = memberService.findById(memberId);

        boolean isHead = authentication.getAuthorities()
                .stream()
                .anyMatch(authority ->
                        authority.getAuthority().equals("ROLE_HEAD")
                );

        boolean isOwner = existingMember.getEmail()
                .equalsIgnoreCase(authentication.getName());

        if (!isHead && !isOwner) {
            return "redirect:/access-denied";
        }

        if (bindingResult.hasErrors()) {
            model.addAttribute("memberId", memberId);
            model.addAttribute("roles", Role.values());
            model.addAttribute("editMode", true);

            return "members/form";
        }

        try {
            memberService.update(
                    memberId,
                    memberFormDto,
                    authentication.getName()
            );
        } catch (AccessDeniedException exception) {
            return "redirect:/access-denied";
        } catch (IllegalArgumentException exception) {
            model.addAttribute("updateError", exception.getMessage());
            model.addAttribute("memberId", memberId);
            model.addAttribute("roles", Role.values());
            model.addAttribute("editMode", true);

            return "members/form";
        }

        return "redirect:/members";
    }

    @PreAuthorize("hasRole('HEAD')")
    @PostMapping("/{memberId}/delete")
    public String deleteMember(
            @PathVariable Long memberId
    ) {
        memberService.delete(memberId);

        return "redirect:/members";
    }
}