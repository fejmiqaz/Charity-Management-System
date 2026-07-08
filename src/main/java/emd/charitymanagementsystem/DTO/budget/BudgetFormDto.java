package emd.charitymanagementsystem.DTO.budget;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BudgetFormDto {
    private Long id;
    @NotNull(message = "Budget amount is required")
    @DecimalMin(value = "0.0", inclusive = true, message = "Budget amount cannot be negative")
    private Double budgetAmount;

    @Size(max = 255, message = "Description cannot be longer than 255 characters")
    private String description;

    private List<Long> donationIds;

    private Long yearId;

    private List<Long> memberIds;
}
