package emd.charitymanagementsystem.DTO.project;

import emd.charitymanagementsystem.Models.ProjectStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProjectResponseDto {
    private Long id;
    private String name;
    private String description;
    private ProjectStatus status;
    private LocalDate dateCreated;
    private Long yearId;
    private Integer yearValue;
    private Double projectPrice;
    private List<Long> memberIds;
    private List<String> memberNames;
}
