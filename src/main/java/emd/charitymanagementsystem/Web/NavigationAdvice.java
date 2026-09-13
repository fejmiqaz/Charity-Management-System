package emd.charitymanagementsystem.Web;

import emd.charitymanagementsystem.Repository.YearsRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
@RequiredArgsConstructor
public class NavigationAdvice {
    private final YearsRepository yearsRepository;

    public record NavigationYear(Long id, Integer yearValue) {}

    @ModelAttribute
    public void navigation(Model model, HttpServletRequest request, Authentication authentication) {
        if (authentication == null || authentication instanceof AnonymousAuthenticationToken) return;
        var years = yearsRepository.findAll(Sort.by(Sort.Direction.DESC, "yearValue"))
                .stream().map(year -> new NavigationYear(year.getId(), year.getYearValue())).toList();
        String path = request.getRequestURI().substring(request.getContextPath().length());
        var match = java.util.regex.Pattern.compile("^/years/(\\d+)(?:/|$)").matcher(path);
        Long requestedId = match.find() ? Long.valueOf(match.group(1)) : null;
        var selected = years.stream().filter(year -> year.id().equals(requestedId)).findFirst()
                .orElse(years.isEmpty() ? null : years.get(0));
        model.addAttribute("navigationYears", years);
        model.addAttribute("navigationYear", selected);
        model.addAttribute("navigationPath", path);
    }
}
