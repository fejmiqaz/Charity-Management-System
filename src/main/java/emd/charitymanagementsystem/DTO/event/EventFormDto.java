package emd.charitymanagementsystem.DTO.event;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
@Data
@NoArgsConstructor
@AllArgsConstructor
public class EventFormDto {
    private Long id;
    @NotBlank(message = "Purpose is required")
    @Size(min = 3, max = 255, message = "Purpose must be between 3 and 255 characters")
    private String purpose;

    @NotNull(message = "Date is required")
    @Future(message = "Event date must be in the future")
    private LocalDateTime date;

    @NotNull(message = "Members must be selected")
    @Size(min = 1, message = "At least one member must participate")
    private List<Long> memberIds;

    private Long yearId;
}
