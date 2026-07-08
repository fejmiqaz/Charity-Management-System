package emd.charitymanagementsystem.DTO.donation;

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
public class DonationFormDto {
    private Long id;
    @NotNull(message = "Donation amount is required")
    @DecimalMin(value = "0.01", message = "Donation amount must be greater than 0")
    private Double donationAmount;

    private Long yearId;

    @NotNull(message = "At least one member must be selected")
    @Size(min = 1, message = "Select at least one member for the donation")
    private List<Long> memberIds;
}
