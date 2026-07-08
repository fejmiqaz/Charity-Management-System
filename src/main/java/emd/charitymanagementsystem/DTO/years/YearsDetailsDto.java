package emd.charitymanagementsystem.DTO.years;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class YearsDetailsDto {
    private Long id;
    private Integer yearValue;

    private Long budgetId;
    private Double budgetAmount;

    private Integer totalProjects;
    private Integer totalDonations;
    private Integer totalMembers;
    private Integer totalEvents;

    private Double totalDonationsAmount;
    private Double totalProjectsAmount;
}