package emd.charitymanagementsystem.Web;

import emd.charitymanagementsystem.Service.Implementation.ImpactService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Controller
@RequiredArgsConstructor
public class FrontPageController {
    private final ImpactService impact;
    @Value("${app.public-zone:Europe/Skopje}")
    private String publicZone;

    @GetMapping("/")
    public String index(Model model) {
        var now = LocalDateTime.now(ZoneId.of(publicZone));
        var projects = impact.publicProjects();
        var events = impact.upcomingEvents(now);
        Map<String, Long> projectYears = new TreeMap<>();
        projects.forEach(project -> {
            if (project.yearValue() != null) projectYears.merge(project.yearValue().toString(), 1L, Long::sum);
        });
        Map<String, Long> eventMonths = new LinkedHashMap<>();
        YearMonth first = YearMonth.from(now);
        var monthLabel = DateTimeFormatter.ofPattern("MMM yyyy", Locale.ENGLISH);
        for (int i = 0; i < 12; i++) {
            YearMonth month = first.plusMonths(i);
            eventMonths.put(month.format(monthLabel), events.stream()
                    .filter(event -> YearMonth.from(event.date()).equals(month)).count());
        }
        model.addAttribute("projects", projects);
        model.addAttribute("events", events);
        model.addAttribute("completedProjects", projects.size());
        model.addAttribute("impactYears", (long) projectYears.size());
        model.addAttribute("projectChart", bars(projectYears));
        model.addAttribute("eventChart", bars(eventMonths));
        model.addAttribute("publicZone", publicZone);
        model.addAttribute("currentYear", now.getYear());
        return "index";
    }

    @GetMapping("/api/public/home")
    @org.springframework.web.bind.annotation.ResponseBody
    public org.springframework.http.ResponseEntity<Map<String, Object>> publicHome() {
        var model = new org.springframework.ui.ExtendedModelMap();
        index(model);
        return org.springframework.http.ResponseEntity.ok().contentType(org.springframework.http.MediaType.APPLICATION_JSON).body(new LinkedHashMap<>(model.asMap()));
    }

    private List<Bar> bars(Map<String, Long> counts) {
        long max = counts.values().stream().mapToLong(Long::longValue).max().orElse(0);
        return counts.entrySet().stream().map(entry -> new Bar(entry.getKey(), entry.getValue(),
                max == 0 ? 0 : 100.0 * entry.getValue() / max)).toList();
    }

    public record Bar(String label, long count, double width) {}
}



