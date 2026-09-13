package emd.charitymanagementsystem.Web;

import emd.charitymanagementsystem.DTO.auth.RegistrationDto;
import emd.charitymanagementsystem.Service.UserAccountService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
@RequiredArgsConstructor
public class AuthController {

    private final UserAccountService userAccountService;


    @GetMapping("/login")
    public String login() {
        return "auth/login";
    }

    @GetMapping("/register")
    public String showRegistrationForm(Model model) {
        model.addAttribute("registrationDto", new RegistrationDto());

        return "auth/register";
    }

    @PostMapping("/register")
    public String register(
            @Valid
            @ModelAttribute("registrationDto")
            RegistrationDto registrationDto,
            BindingResult bindingResult,
            Model model
    ) {

        if (bindingResult.hasErrors()) {
            return "auth/register";
        }

        try {
            userAccountService.register(registrationDto);
        } catch (IllegalArgumentException exception) {
            model.addAttribute(
                    "registrationError",
                    exception.getMessage()
            );

            return "auth/register";
        }

        return "redirect:/login?registered";
    }
}