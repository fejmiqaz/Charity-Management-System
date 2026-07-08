package emd.charitymanagementsystem.Web;

import emd.charitymanagementsystem.DTO.event.EventFormDto;
import emd.charitymanagementsystem.DTO.event.EventResponseDto;
import emd.charitymanagementsystem.Models.Years;
import emd.charitymanagementsystem.Service.EventService;
import emd.charitymanagementsystem.Service.MemberService;
import emd.charitymanagementsystem.Service.YearsService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/years/{yearId}/events")
@AllArgsConstructor
public class EventController {

    private final EventService eventService;
    private final YearsService yearsService;
    private final MemberService memberService;

    @GetMapping
    public String listEvents(@PathVariable Long yearId, Model model) {
        Years year = yearsService.findEntityById(yearId);
        List<EventResponseDto> events = eventService.findAllByYearId(yearId);

        model.addAttribute("year", year);
        model.addAttribute("events", events);
        return "events/list";
    }

    @GetMapping("/{id}")
    public String details(@PathVariable Long yearId,
                          @PathVariable Long id,
                          Model model) {
        Years year = yearsService.findEntityById(yearId);
        EventResponseDto event = eventService.findById(id);

        model.addAttribute("year", year);
        model.addAttribute("event", event);
        return "events/details";
    }

    @GetMapping("/add-form")
    public String addForm(@PathVariable Long yearId, Model model) {
        Years year = yearsService.findEntityById(yearId);

        EventFormDto eventFormDto = new EventFormDto();
        eventFormDto.setYearId(yearId);

        model.addAttribute("year", year);
        model.addAttribute("event", eventFormDto);
        model.addAttribute("members", memberService.listAll());

        return "events/form";
    }

    @GetMapping("/{id}/edit-form")
    public String editForm(@PathVariable Long yearId,
                           @PathVariable Long id,
                           Model model) {
        Years year = yearsService.findEntityById(yearId);
        EventFormDto eventFormDto = eventService.findByIdForEdit(id);

        model.addAttribute("year", year);
        model.addAttribute("event", eventFormDto);
        model.addAttribute("members", memberService.listAll());

        return "events/form";
    }

    @PostMapping("/add")
    public String saveEvent(@PathVariable Long yearId,
                            @Valid @ModelAttribute("event") EventFormDto eventFormDto,
                            BindingResult bindingResult,
                            Model model) {
        Years year = yearsService.findEntityById(yearId);
        eventFormDto.setYearId(yearId);

        if (bindingResult.hasErrors()) {
            model.addAttribute("year", year);
            model.addAttribute("members", memberService.listAll());
            return "events/form";
        }

        if (eventFormDto.getId() == null) {
            eventService.create(eventFormDto);
        } else {
            eventService.update(eventFormDto.getId(), eventFormDto);
        }

        return "redirect:/years/" + yearId + "/events";
    }

    @PostMapping("/{id}/delete")
    public String deleteEvent(@PathVariable Long yearId,
                              @PathVariable Long id) {
        eventService.delete(id);
        return "redirect:/years/" + yearId + "/events";
    }
}