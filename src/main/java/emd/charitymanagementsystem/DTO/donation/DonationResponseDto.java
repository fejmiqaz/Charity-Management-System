package emd.charitymanagementsystem.DTO.donation;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DonationResponseDto {
    private Long id;
    private Double donationAmount;
    private Long yearId;
    private Integer yearValue;
    private List<Long> memberIds;
    private List<String> memberNames;
}
