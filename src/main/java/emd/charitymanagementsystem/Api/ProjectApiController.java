package emd.charitymanagementsystem.Api;

import emd.charitymanagementsystem.DTO.project.*;
import emd.charitymanagementsystem.Models.*;
import emd.charitymanagementsystem.Service.ProjectService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/years/{yearId}/projects")
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProjectApiController {
    private final ProjectService service;
    private final ApiScope scope;

    @GetMapping
    @PreAuthorize("hasAnyRole('HEAD','SUBHEAD','PROJECT_MANAGER','VOLUNTEER','MEMBER')")
    public List<ProjectResponseDto> list(@PathVariable Long yearId, @RequestParam(required = false) ProjectType type, @RequestParam(required = false) ProjectStatus status) {
        scope.year(yearId);
        return service.findByYearId(yearId).stream().filter(p -> type == null || (p.getProjectType() == null ? ProjectType.STANDARD : p.getProjectType()) == type)
                .filter(p -> status == null || p.getStatus() == status).toList();
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('HEAD','SUBHEAD','MEMBER')")
    public ProjectResponseDto get(@PathVariable Long yearId, @PathVariable Long id) {
        scope.project(yearId, id);
        return service.findById(id);
    }

    @PostMapping
    @Transactional
    @PreAuthorize("hasAnyRole('HEAD','SUBHEAD','PROJECT_MANAGER')")
    public ResponseEntity<ProjectResponseDto> create(@PathVariable Long yearId, @Valid @RequestBody ProjectFormDto form) {
        scope.year(yearId);
        scope.members(form.getMemberIds());
        form.setId(null);
        form.setYearId(yearId);
        var result = service.create(form);
        return ResponseEntity.created(URI.create("/api/years/" + yearId + "/projects/" + result.getId())).body(result);
    }

    @PutMapping("/{id}")
    @Transactional
    @PreAuthorize("hasAnyRole('HEAD','SUBHEAD','PROJECT_MANAGER')")
    public ProjectResponseDto update(@PathVariable Long yearId, @PathVariable Long id, @Valid @RequestBody ProjectFormDto form) {
        scope.project(yearId, id);
        scope.members(form.getMemberIds());
        form.setId(id);
        form.setYearId(yearId);
        return service.update(id, form);
    }

    @DeleteMapping("/{id}")
    @Transactional
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasAnyRole('HEAD','SUBHEAD','PROJECT_MANAGER')")
    public void delete(@PathVariable Long yearId, @PathVariable Long id) {
        scope.project(yearId, id);
        service.delete(id);
    }
}
