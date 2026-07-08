package emd.charitymanagementsystem.Service;

import emd.charitymanagementsystem.DTO.project.ProjectFormDto;
import emd.charitymanagementsystem.DTO.project.ProjectResponseDto;
import emd.charitymanagementsystem.Models.Project;
import org.jspecify.annotations.Nullable;

import java.util.List;

public interface ProjectService {
    List<ProjectResponseDto> listAll();
    ProjectResponseDto findById(Long id);
    ProjectResponseDto create(ProjectFormDto projectFormDto);
    ProjectResponseDto update(Long id, ProjectFormDto projectFormDto);
    void delete(Long id);

    @Nullable List<ProjectResponseDto> findByYearId(Long yearId);
    Double getTotalPriceForYear(Long yearId);
    long projectsCount();

    double getTotalProjectCost();

    double totalProjectCostsByYear(Long id);
}
