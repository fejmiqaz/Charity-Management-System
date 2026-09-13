package emd.charitymanagementsystem.Service.Implementation;

import emd.charitymanagementsystem.DTO.project.PublicProjectDto;
import emd.charitymanagementsystem.Models.ProjectStatus;
import emd.charitymanagementsystem.Repository.ProjectRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class ImpactService {
    private final ProjectRepository projects;

    @Transactional(readOnly = true)
    public List<PublicProjectDto> publicProjects() {
        return projects.findPublicImpactProjects(ProjectStatus.FINISHED);
    }

    @Transactional
    @PreAuthorize("hasRole('HEAD')")
    public void setPublished(Long yearId, Long projectId, boolean published) {
        var project = projects.findById(projectId).orElseThrow();
        if (project.getYear() == null || !Objects.equals(project.getYear().getId(), yearId)) {
            throw new IllegalArgumentException("Project does not belong to this year.");
        }
        if (published && project.getStatus() != ProjectStatus.FINISHED) {
            throw new IllegalArgumentException("Only finished projects can be published.");
        }
        project.setPublicImpact(published);
    }
}
