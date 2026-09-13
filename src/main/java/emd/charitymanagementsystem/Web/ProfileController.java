package emd.charitymanagementsystem.Web;

import emd.charitymanagementsystem.DTO.profile.ProfileFormDto;
import emd.charitymanagementsystem.Service.Implementation.ProfileService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/profile")
@RequiredArgsConstructor
public class ProfileController {
    private final ProfileService profiles;
    private final emd.charitymanagementsystem.Service.Implementation.MembershipService memberships;

    @GetMapping
    public String view(Authentication authentication, Model model) {
        var account = profiles.account(authentication.getName());
        model.addAttribute("account", account);
        model.addAttribute("membershipYear", memberships.currentYear());
        model.addAttribute("membershipFee", memberships.fee(memberships.currentYear()));
        var member = account.getMember();
        model.addAttribute("membershipHistory", member == null ? java.util.List.of() : memberships.history(member.getId()));
        model.addAttribute("membershipPaid", member != null && memberships.paidMembers(memberships.currentYear()).containsKey(member.getId()));
        return "profile/details";
    }

    @GetMapping("/edit")
    public String edit(Authentication authentication, Model model) {
        model.addAttribute("profile", profiles.form(authentication.getName()));
        return "profile/form";
    }

    @PostMapping("/edit")
    public String update(@Valid @ModelAttribute("profile") ProfileFormDto form, BindingResult errors,
                         Authentication authentication, Model model,
                         HttpServletRequest request, HttpServletResponse response) {
        if (errors.hasErrors()) return "profile/form";
        boolean emailChanged;
        try {
            emailChanged = profiles.update(authentication.getName(), form);
        } catch (IllegalArgumentException exception) {
            errors.rejectValue("email", "email.unavailable", exception.getMessage());
            return "profile/form";
        } catch (org.springframework.dao.DataIntegrityViolationException exception) {
            errors.rejectValue("email", "email.unavailable", "This email address is already in use. Choose another address.");
            return "profile/form";
        }
        if (emailChanged) {
            new SecurityContextLogoutHandler().logout(request, response, authentication);
            return "redirect:/login?profileUpdated";
        }
        return "redirect:/profile?saved";
    }
}
