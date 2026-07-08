package emd.charitymanagementsystem.Service;

import emd.charitymanagementsystem.DTO.donation.DonationFormDto;
import emd.charitymanagementsystem.DTO.event.EventFormDto;
import emd.charitymanagementsystem.DTO.event.EventResponseDto;
import emd.charitymanagementsystem.Models.Event;
import emd.charitymanagementsystem.Models.Project;
import emd.charitymanagementsystem.Repository.EventRepository;

import java.util.List;

public interface EventService {
    List<EventResponseDto> listAll();
    EventResponseDto findById(Long id);
    EventFormDto findByIdForEdit(Long id);

    EventResponseDto create(EventFormDto eventFormDto);
    EventResponseDto update(Long id, EventFormDto eventFormDto);
    void delete(Long id);

    List<EventResponseDto> findAllByYearId(Long yearId);
}
