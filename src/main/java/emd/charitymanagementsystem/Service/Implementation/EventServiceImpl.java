package emd.charitymanagementsystem.Service.Implementation;

import emd.charitymanagementsystem.DTO.event.EventFormDto;
import emd.charitymanagementsystem.DTO.event.EventResponseDto;
import emd.charitymanagementsystem.Mapper.EventMapper;
import emd.charitymanagementsystem.Models.Event;
import emd.charitymanagementsystem.Models.Member;
import emd.charitymanagementsystem.Models.Project;
import emd.charitymanagementsystem.Models.Years;
import emd.charitymanagementsystem.Repository.EventRepository;
import emd.charitymanagementsystem.Repository.MemberRepository;
import emd.charitymanagementsystem.Repository.YearsRepository;
import emd.charitymanagementsystem.Service.EventService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@AllArgsConstructor
public class EventServiceImpl implements EventService {

    private final EventRepository eventRepository;
    private final MemberRepository memberRepository;
    private final YearsRepository yearsRepository;

    @Override
    public List<EventResponseDto> listAll() {
        return eventRepository.findAll().stream()
                .map(EventMapper::toDto)
                .toList();
    }

    @Override
    public EventResponseDto findById(Long id) {
        Event event = eventRepository.findById(id).orElseThrow();
        return EventMapper.toDto(event);
    }

    @Override
    public EventFormDto findByIdForEdit(Long id) {
        Event event = eventRepository.findById(id).orElseThrow();
        return EventMapper.toFormDto(event);
    }

    @Override
    public EventResponseDto create(EventFormDto eventFormDto) {
        Years year = yearsRepository.findById(eventFormDto.getYearId())
                .orElseThrow(() -> new RuntimeException("Year not found"));

        List<Member> members = eventFormDto.getMemberIds() == null
                ? List.of()
                : memberRepository.findAllById(eventFormDto.getMemberIds());

        Event event = new Event();
        event.setPurpose(eventFormDto.getPurpose());
        event.setMembers(members);
        event.setDate(LocalDateTime.now());
        event.setYear(year);

        Event saved = eventRepository.save(event);
        return EventMapper.toDto(saved);
    }

    @Override
    public EventResponseDto update(Long id, EventFormDto eventFormDto) {
        Event event = eventRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Event not found"));

        Years year = yearsRepository.findById(eventFormDto.getYearId())
                .orElseThrow(() -> new RuntimeException("Year not found"));

        List<Member> members = eventFormDto.getMemberIds() == null
                ? List.of()
                : memberRepository.findAllById(eventFormDto.getMemberIds());

        event.setPurpose(eventFormDto.getPurpose());
        event.setMembers(members);
        event.setDate(LocalDateTime.now());
        event.setYear(year);

        Event updated = eventRepository.save(event);
        return EventMapper.toDto(updated);
    }

    @Override
    public void delete(Long id) {
        eventRepository.deleteById(id);
    }

    @Override
    public List<EventResponseDto> findAllByYearId(Long yearId) {
        return eventRepository.findAllByYearId(yearId).stream()
                .map(EventMapper::toDto)
                .toList();
    }
}
