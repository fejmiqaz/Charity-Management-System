package emd.charitymanagementsystem.Mapper;

import emd.charitymanagementsystem.DTO.project.ProjectFormDto;
import emd.charitymanagementsystem.DTO.project.ProjectResponseDto;
import emd.charitymanagementsystem.Models.Member;
import emd.charitymanagementsystem.Models.Project;

public class ProjectMapper {
    public static ProjectResponseDto toDto(Project project){
        return new ProjectResponseDto(
                project.getId(),
                project.getName(),
                project.getDescription(),
                project.getStatus(),
                project.getDateCreated(),
                project.getYear() != null ? project.getYear().getId() : null,
                project.getYear() != null ? project.getYear().getYearValue() : null,
                project.getProjectPrice(),
                project.getMembers().isEmpty() ? null : project.getMembers().stream().map(Member::getId).toList() ,
                project.getMembers().isEmpty() ? null : project.getMembers().stream().map(Member::getName).toList()
        );
    }

    public static ProjectFormDto toFormDto(Project project){
        return new ProjectFormDto(
                project.getId(),
                project.getName(),
                project.getDescription(),
                project.getStatus(),
                project.getProjectPrice(),
                project.getYear() != null ? project.getYear().getId() : null,
                project.getMembers().isEmpty() ? null : project.getMembers().stream().map(Member::getId).toList()
        );
    }
}
