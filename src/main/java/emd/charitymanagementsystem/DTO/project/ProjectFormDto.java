package emd.charitymanagementsystem.DTO.project;

import emd.charitymanagementsystem.Models.ProjectStatus;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProjectFormDto {

    private Long id;

    @NotBlank(message = "Project name is required")
    @Size(min = 3, max = 150, message = "Project name must be between 3 and 150 characters")
    private String name;

    @NotBlank(message = "Description is required")
    @Size(min = 10, max = 1000, message = "Description must be between 10 and 1000 characters")
    private String description;

    @NotNull(message = "Project status must be selected")
    private ProjectStatus status;

    @NotNull(message = "Project price is required")
    @Positive(message = "Project price must be greater than 0")
    @DecimalMin(value = "0.01", message = "Project price must be at least 0.01")
    private Double projectPrice;

    private Long yearId;

    @NotNull(message = "Members must be selected")
    @Size(min = 1, message = "At least one member must be assigned")
    private List<Long> memberIds;
}