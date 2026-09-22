package emd.charitymanagementsystem.DTO.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import emd.charitymanagementsystem.Models.EventType;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class EventResponseDto {
    private Long id;
    private EventType eventType;
    private String purpose;
    private LocalDateTime date;
    private List<Long> memberIds;
    private List<String> memberNames;
    private Long yearId;
    private Integer yearValue;
    private boolean publicVisible;
}
