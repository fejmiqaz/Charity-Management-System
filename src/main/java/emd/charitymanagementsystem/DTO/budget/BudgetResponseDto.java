package emd.charitymanagementsystem.DTO.budget;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BudgetResponseDto {
    private Long id;
    private Double budgetAmount;
    private String description;
    private List<Long> donationIds;
    private List<List<Long>> donationMemberIds;
    private List<List<String>> donationMemberNames;
    private Long yearId;
    private Integer yearValue;
    private List<Long> memberIds;
    private List<String> memberNames;
}
