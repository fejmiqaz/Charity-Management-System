package emd.charitymanagementsystem.Web;

import emd.charitymanagementsystem.Service.Implementation.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice @RequiredArgsConstructor
public class NotificationAdvice {
    private final NotificationService notifications;
    @ModelAttribute
    public void notifications(Model model, Authentication authentication) {
        if (authentication != null && authentication.isAuthenticated()
                && !(authentication instanceof AnonymousAuthenticationToken)) {
            model.addAttribute("notificationHeader", notifications.header(authentication.getName()));
        }
    }
}
