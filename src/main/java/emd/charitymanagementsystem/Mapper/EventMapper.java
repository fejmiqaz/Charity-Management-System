package emd.charitymanagementsystem.Mapper;

import emd.charitymanagementsystem.DTO.event.EventFormDto;
import emd.charitymanagementsystem.DTO.event.EventResponseDto;
import emd.charitymanagementsystem.Models.Event;
import emd.charitymanagementsystem.Models.Member;

import java.util.List;

public class EventMapper {
    public static EventResponseDto toDto(Event event){
        return new EventResponseDto(
                event.getId(),
                event.getPurpose(),
                event.getDate(),
                event.getMembers().isEmpty() ? List.<Long>of() : event.getMembers().stream().map(Member::getId).toList(),
                event.getMembers().isEmpty() ? List.<String>of() : event.getMembers().stream().map(Member::getName).toList(),
                event.getYear() != null ? event.getYear().getId() : null,
                event.getYear() != null ? event.getYear().getYearValue() : null
        );
    }

    public static EventFormDto toFormDto(Event event){
        return new EventFormDto(
                event.getId(),
                event.getPurpose(),
                event.getDate(),
                event.getMembers().isEmpty() ? List.<Long>of() : event.getMembers().stream().map(Member::getId).toList(),
                event.getYear() != null ? event.getYear().getId() : null
        );
    }
}
