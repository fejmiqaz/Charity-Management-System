package emd.charitymanagementsystem.Web;

import emd.charitymanagementsystem.DTO.event.EventFormDto;
import emd.charitymanagementsystem.DTO.event.EventResponseDto;
import emd.charitymanagementsystem.Models.Years;
import emd.charitymanagementsystem.Service.EventService;
import emd.charitymanagementsystem.Service.MemberService;
import emd.charitymanagementsystem.Service.YearsService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.math.BigDecimal;
import java.time.LocalDate;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.Authentication;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import emd.charitymanagementsystem.Models.EventType;

@Controller
@RequestMapping("/years/{yearId}/events")
@AllArgsConstructor
public class EventController {

    private final EventService eventService;
    private final YearsService yearsService;
    private final MemberService memberService;
    private final emd.charitymanagementsystem.Service.Implementation.ImpactService impact;
    private final emd.charitymanagementsystem.Service.Implementation.ActivityFinanceService activityFinance;

    @PreAuthorize("hasRole('HEAD')")
    @PostMapping("/{id}/publication")
    public String publication(@PathVariable Long yearId, @PathVariable Long id,
                              @RequestParam(defaultValue = "false") boolean published) {
        impact.setEventPublished(yearId, id, published);
        return "redirect:/years/" + yearId + "/events/" + id;
    }

    @GetMapping
    public String listEvents(@PathVariable Long yearId, Model model) {
        Years year = yearsService.findEntityById(yearId);
        List<EventResponseDto> events = eventService.findAllByYearId(yearId);

        model.addAttribute("year", year);
        model.addAttribute("events", events);
        return "events/list";
    }

    @PreAuthorize("hasAnyRole('HEAD', 'SUBHEAD', 'TREASURER', 'MEMBER')")
    @GetMapping("/{id}")
    public String details(@PathVariable Long yearId,
                          @PathVariable Long id,
                          Model model) {
        Years year = yearsService.findEntityById(yearId);
        EventResponseDto event = eventService.findById(id);

        model.addAttribute("year", year);
        model.addAttribute("event", event);
        model.addAttribute("tasks", activityFinance.tasks(id));
        model.addAttribute("members", memberService.listAll());
        model.addAttribute("today", LocalDate.now());
        return "events/details";
    }

    @PreAuthorize("hasAnyRole('HEAD', 'SUBHEAD', 'MEMBER')")
    @GetMapping("/add-form")
    public String addForm(@PathVariable Long yearId, Model model) {
        Years year = yearsService.findEntityById(yearId);

        EventFormDto eventFormDto = new EventFormDto();
        eventFormDto.setYearId(yearId);

        model.addAttribute("year", year);
        model.addAttribute("event", eventFormDto);
        model.addAttribute("members", memberService.listAll());
        model.addAttribute("eventTypes", EventType.values());

        return "events/form";
    }

    @PreAuthorize("hasAnyRole('HEAD', 'SUBHEAD', 'TREASURER')")
    @GetMapping("/{id}/edit-form")
    public String editForm(@PathVariable Long yearId,
                           @PathVariable Long id,
                           Model model) {
        Years year = yearsService.findEntityById(yearId);
        EventFormDto eventFormDto = eventService.findByIdForEdit(id);

        model.addAttribute("year", year);
        model.addAttribute("event", eventFormDto);
        model.addAttribute("members", memberService.listAll());
        model.addAttribute("eventTypes", EventType.values());

        return "events/form";
    }

    @PreAuthorize("hasAnyRole('HEAD', 'SUBHEAD', 'TREASURER')")
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
            model.addAttribute("eventTypes", EventType.values());
            return "events/form";
        }

        if (eventFormDto.getId() == null) {
            eventService.create(eventFormDto);
        } else {
            eventService.update(eventFormDto.getId(), eventFormDto);
        }

        return "redirect:/years/" + yearId + "/events";
    }

    @PreAuthorize("hasAnyRole('HEAD', 'SUBHEAD', 'TREASURER')")
    @PostMapping("/{id}/delete")
    public String deleteEvent(@PathVariable Long yearId,
                              @PathVariable Long id) {
        eventService.delete(id);
        return "redirect:/years/" + yearId + "/events";
    }

    @PostMapping("/{eventId}/tasks")
    public String addTask(@PathVariable Long yearId, @PathVariable Long eventId,
                          @RequestParam String title, @RequestParam(required=false) String description,
                          @RequestParam BigDecimal price, @RequestParam(required=false) List<Long> memberIds,
                          RedirectAttributes flash) {
        try { activityFinance.addTask(yearId,eventId,title,description,price,memberIds); flash.addFlashAttribute("activitySuccess","Task added."); }
        catch (IllegalArgumentException e) { flash.addFlashAttribute("activityError",e.getMessage()); }
        return "redirect:/years/"+yearId+"/events/"+eventId;
    }

    @PostMapping("/{eventId}/tasks/{taskId}/status")
    public String taskStatus(@PathVariable Long yearId,@PathVariable Long eventId,@PathVariable Long taskId,
                             @RequestParam(defaultValue="false") boolean completed) {
        activityFinance.completeTask(yearId,eventId,taskId,completed);
        return "redirect:/years/"+yearId+"/events/"+eventId;
    }

    @PostMapping("/{eventId}/tasks/{taskId}/payments")
    public String taskPayment(@PathVariable Long yearId,@PathVariable Long eventId,@PathVariable Long taskId,
                              @RequestParam(required=false) Long memberId,@RequestParam BigDecimal amount,
                              @RequestParam emd.charitymanagementsystem.Models.Currency currency,
                              @RequestParam @DateTimeFormat(iso=DateTimeFormat.ISO.DATE) LocalDate paidOn,
                              @RequestParam(required=false) String note, Authentication authentication, RedirectAttributes flash) {
        try { activityFinance.addTaskPayment(yearId,eventId,taskId,memberId,amount,currency,paidOn,note,authentication.getName()); flash.addFlashAttribute("activitySuccess","Payment recorded."); }
        catch (IllegalArgumentException e) { flash.addFlashAttribute("activityError",e.getMessage()); }
        return "redirect:/years/"+yearId+"/events/"+eventId;
    }
}
