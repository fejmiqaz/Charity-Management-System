package emd.charitymanagementsystem.Web;

import emd.charitymanagementsystem.DTO.project.ProjectFormDto;
import emd.charitymanagementsystem.DTO.project.ProjectResponseDto;
import emd.charitymanagementsystem.Models.ProjectStatus;
import emd.charitymanagementsystem.Models.Years;
import emd.charitymanagementsystem.Service.MemberService;
import emd.charitymanagementsystem.Service.ProjectService;
import emd.charitymanagementsystem.Service.YearsService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Objects;
import java.math.BigDecimal;
import java.time.YearMonth;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.Authentication;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import emd.charitymanagementsystem.Models.ProjectType;

@Controller
@RequestMapping("/years/{yearId}/projects")
@AllArgsConstructor
public class ProjectController {

    private final ProjectService projectService;
    private final YearsService yearService;
    private final MemberService memberService;
    private final emd.charitymanagementsystem.Service.Implementation.ImpactService impact;
    private final emd.charitymanagementsystem.Service.Implementation.ActivityFinanceService activityFinance;

    @PreAuthorize("hasRole('HEAD')")
    @PostMapping("/{projectId}/publication")
    public String publication(@PathVariable Long yearId, @PathVariable Long projectId,
                              @RequestParam(defaultValue = "false") boolean published) {
        impact.setPublished(yearId, projectId, published);
        return "redirect:/years/" + yearId + "/projects/" + projectId;
    }

    @PreAuthorize("hasAnyRole('HEAD', 'TREASURER', 'SUBHEAD', 'PROJECT_MANAGER', 'VOLUNTEER', 'MEMBER')")
    @GetMapping
    public String listProjects(@PathVariable Long yearId,
                               @RequestParam(required = false) ProjectType type,
                               @RequestParam(required = false) ProjectStatus status, Model model) {
        Years year = yearService.findEntityById(yearId);

        List<ProjectResponseDto> projects = projectService.findByYearId(yearId);

        double totalProjectPrice = projects.stream()
                .map(ProjectResponseDto::getProjectPrice)
                .filter(Objects::nonNull)
                .mapToDouble(Double::doubleValue)
                .sum();

        double yearBudget = 0.0;
        if (year.getBudget() != null && year.getBudget().getBudgetAmount() != null) {
            yearBudget = year.getBudget().getBudgetAmount();
        }

        boolean withinBudget = totalProjectPrice <= yearBudget;

        model.addAttribute("year", year);
        model.addAttribute("projects", projects.stream()
                .filter(project -> type == null || (project.getProjectType() == null ? ProjectType.STANDARD : project.getProjectType()) == type)
                .filter(project -> status == null || project.getStatus() == status)
                .toList());
        model.addAttribute("types", ProjectType.values());
        model.addAttribute("statuses", ProjectStatus.values());
        model.addAttribute("selectedType", type);
        model.addAttribute("selectedStatus", status);
        model.addAttribute("filtersActive", type != null || status != null);
        model.addAttribute("totalProjectPrice", totalProjectPrice);
        model.addAttribute("yearBudget", yearBudget);
        model.addAttribute("withinBudget", withinBudget);

        return "projects/list";
    }

    @PreAuthorize("hasAnyRole('HEAD', 'SUBHEAD', 'PROJECT_MANAGER')")
    @GetMapping("/add")
    public String showAddForm(@PathVariable Long yearId, Model model) {
        ProjectFormDto projectFormDto = new ProjectFormDto();
        projectFormDto.setYearId(yearId);

        model.addAttribute("year", yearService.findById(yearId));
        model.addAttribute("project", projectFormDto);
        model.addAttribute("statuses", ProjectStatus.values());
        model.addAttribute("projectTypes", ProjectType.values());
        model.addAttribute("members", memberService.listAll());

        return "projects/form";
    }

    @PreAuthorize("hasAnyRole('HEAD', 'SUBHEAD', 'PROJECT_MANAGER')")
    @PostMapping("/add")
    public String saveProject(@PathVariable Long yearId,
                              @Valid @ModelAttribute("project") ProjectFormDto projectFormDto,
                              BindingResult bindingResult,
                              Model model) {
        projectFormDto.setYearId(yearId);

        if (bindingResult.hasErrors()) {
            model.addAttribute("year", yearService.findById(yearId));
            model.addAttribute("statuses", ProjectStatus.values());
            model.addAttribute("projectTypes", ProjectType.values());
            model.addAttribute("members", memberService.listAll());
            return "projects/form";
        }

        projectService.create(projectFormDto);
        return "redirect:/years/" + yearId + "/projects";
    }

    @PreAuthorize("hasAnyRole('HEAD', 'SUBHEAD', 'TREASURER', 'MEMBER')")
    @GetMapping("/{projectId}")
    public String details(@PathVariable Long yearId,
                          @PathVariable Long projectId,
                          Model model) {
        model.addAttribute("year", yearService.findById(yearId));
        ProjectResponseDto project = projectService.findById(projectId);
        model.addAttribute("project", project);
        model.addAttribute("revenues", activityFinance.revenues(projectId));
        model.addAttribute("revenueTotal", activityFinance.projectRevenue(projectId));
        model.addAttribute("revenueTotals", activityFinance.projectRevenueTotals(projectId));
        model.addAttribute("currentMonth", YearMonth.now());
        return "projects/details";
    }

    @PreAuthorize("hasAnyRole('HEAD', 'SUBHEAD', 'TREASURER')")
    @GetMapping("/{projectId}/edit")
    public String showEditForm(@PathVariable Long yearId,
                               @PathVariable Long projectId,
                               Model model) {
        ProjectResponseDto project = projectService.findById(projectId);

        ProjectFormDto projectFormDto = new ProjectFormDto();
        projectFormDto.setId(project.getId());
        projectFormDto.setProjectType(project.getProjectType());
        projectFormDto.setName(project.getName());
        projectFormDto.setDescription(project.getDescription());
        projectFormDto.setStatus(project.getStatus());
        projectFormDto.setYearId(project.getYearId());
        projectFormDto.setProjectPrice(project.getProjectPrice());
        projectFormDto.setMemberIds(project.getMemberIds());

        model.addAttribute("year", yearService.findById(yearId));
        model.addAttribute("project", projectFormDto);
        model.addAttribute("statuses", ProjectStatus.values());
        model.addAttribute("projectTypes", ProjectType.values());
        model.addAttribute("members", memberService.listAll());

        return "projects/form";
    }

    @PreAuthorize("hasAnyRole('HEAD', 'SUBHEAD', 'PROJECT_MANAGER')")
    @PostMapping("/{projectId}/edit")
    public String updateProject(@PathVariable Long yearId,
                                @PathVariable Long projectId,
                                @Valid @ModelAttribute("project") ProjectFormDto projectFormDto,
                                BindingResult bindingResult,
                                Model model) {
        projectFormDto.setId(projectId);
        projectFormDto.setYearId(yearId);

        if (bindingResult.hasErrors()) {
            model.addAttribute("year", yearService.findById(yearId));
            model.addAttribute("statuses", ProjectStatus.values());
            model.addAttribute("projectTypes", ProjectType.values());
            model.addAttribute("members", memberService.listAll());
            return "projects/form";
        }

        projectService.update(projectId, projectFormDto);
        return "redirect:/years/" + yearId + "/projects";
    }

    @PreAuthorize("hasAnyRole('HEAD', 'SUBHEAD', 'PROJECT_MANAGER')")
    @PostMapping("/{projectId}/delete")
    public String deleteProject(@PathVariable Long yearId,
                                @PathVariable Long projectId) {
        projectService.delete(projectId);
        return "redirect:/years/" + yearId + "/projects";
    }

    @PostMapping("/{projectId}/revenues")
    public String addRevenue(@PathVariable Long yearId,@PathVariable Long projectId,
                             @RequestParam @DateTimeFormat(pattern="yyyy-MM") YearMonth month,
                             @RequestParam String customer,@RequestParam BigDecimal amount,
                             @RequestParam emd.charitymanagementsystem.Models.Currency currency,
                             @RequestParam(required=false) String note, Authentication authentication, RedirectAttributes flash) {
        try { activityFinance.addRevenue(yearId,projectId,month,customer,amount,currency,note,authentication.getName()); flash.addFlashAttribute("activitySuccess","Monthly income recorded."); }
        catch (IllegalArgumentException e) { flash.addFlashAttribute("activityError",e.getMessage()); }
        return "redirect:/years/"+yearId+"/projects/"+projectId;
    }
}
