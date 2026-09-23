package emd.charitymanagementsystem.Web;

import emd.charitymanagementsystem.Service.Implementation.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller @RequiredArgsConstructor @RequestMapping("/notifications")
public class NotificationController {
    private final NotificationService notifications;

    @GetMapping
    public String inbox(@RequestParam(defaultValue = "0") int page, Authentication authentication, Model model) {
        model.addAttribute("notificationPage", notifications.inbox(authentication.getName(), page));
        return "notifications/list";
    }

    @PostMapping("/{id}/read")
    public String read(@PathVariable Long id, Authentication authentication) {
        notifications.markRead(authentication.getName(), id);
        return "redirect:/notifications";
    }

    @PostMapping("/read-all")
    public String readAll(Authentication authentication) {
        notifications.markAllRead(authentication.getName());
        return "redirect:/notifications";
    }

    @PostMapping("/clear-all")
    public String clearAll(Authentication authentication, org.springframework.web.servlet.mvc.support.RedirectAttributes flash) {
        notifications.clearAll(authentication.getName());
        flash.addFlashAttribute("notificationSuccess", "Your notifications have been cleared.");
        return "redirect:/notifications";
    }
}
