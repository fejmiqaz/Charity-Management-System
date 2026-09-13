package emd.charitymanagementsystem.Web;

import emd.charitymanagementsystem.Service.Implementation.ImpactService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@RequiredArgsConstructor
public class ImpactController {
    private final ImpactService impact;

    @GetMapping("/impact")
    public String impact(Model model) {
        var projects = impact.publicProjects();
        model.addAttribute("projects", projects);
        model.addAttribute("completedProjects", projects.size());
        model.addAttribute("impactYears", projects.stream().map(p -> p.yearValue())
                .filter(java.util.Objects::nonNull).distinct().count());
        return "impact";
    }
}
