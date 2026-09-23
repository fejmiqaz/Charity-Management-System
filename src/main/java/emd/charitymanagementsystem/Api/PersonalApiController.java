package emd.charitymanagementsystem.Api;

import emd.charitymanagementsystem.DTO.profile.ProfileFormDto;
import emd.charitymanagementsystem.Service.Implementation.*;
import jakarta.servlet.http.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class PersonalApiController {
    private final NotificationService notifications;
    private final ProfileService profiles;

    @GetMapping("/profile")
    public ProfileFormDto profile(Authentication auth) {
        return profiles.form(auth.getName());
    }

    @PutMapping("/profile")
    public Map<String, Boolean> updateProfile(@Valid @RequestBody ProfileFormDto form,
                                              Authentication auth, HttpServletRequest request, HttpServletResponse response) {
        boolean emailChanged = profiles.update(auth.getName(), form);
        if (emailChanged) new SecurityContextLogoutHandler().logout(request, response, auth);
        return Map.of("loginRequired", emailChanged);
    }

    @GetMapping("/notifications")
    public ApiPage<NotificationService.Item> notifications(Authentication auth, @RequestParam(defaultValue = "0") int page) {
        return ApiPage.of(notifications.inbox(auth.getName(), page));
    }

    @GetMapping("/notifications/summary")
    public NotificationService.Header summary(Authentication auth) {
        return notifications.header(auth.getName());
    }

    @PostMapping("/notifications/{id}/read")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void read(Authentication auth, @PathVariable Long id) {
        notifications.markRead(auth.getName(), id);
    }

    @PostMapping("/notifications/read-all")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void readAll(Authentication auth) {
        notifications.markAllRead(auth.getName());
    }

    @DeleteMapping("/notifications")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void clear(Authentication auth) {
        notifications.clearAll(auth.getName());
    }
}
