package emd.charitymanagementsystem.Api;

import emd.charitymanagementsystem.DTO.event.*;
import emd.charitymanagementsystem.Models.*;
import emd.charitymanagementsystem.Service.EventService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/years/{yearId}/events")
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EventApiController {
    private final EventService service;
    private final ApiScope scope;
    @org.springframework.beans.factory.annotation.Value("${app.public-zone:Europe/Skopje}")
    private String timeZone;

    @GetMapping
    @PreAuthorize("hasAnyRole('HEAD','SUBHEAD','EVENT_MANAGER','VOLUNTEER','MEMBER')")
    public List<EventResponseDto> list(@PathVariable Long yearId, @RequestParam(required = false) EventType type, @RequestParam(required = false) EventStatus status) {
        scope.year(yearId);
        return service.findAllByYearId(yearId).stream().filter(e -> type == null || (e.getEventType() == null ? EventType.NORMAL : e.getEventType()) == type)
                .filter(e -> status == null || EventStatus.fromDate(e.getDate(), java.time.LocalDateTime.now(java.time.ZoneId.of(timeZone))) == status).toList();
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('HEAD','SUBHEAD','EVENT_MANAGER','MEMBER')")
    public EventResponseDto get(@PathVariable Long yearId, @PathVariable Long id) {
        scope.event(yearId, id);
        return service.findById(id);
    }

    @PostMapping
    @Transactional
    @PreAuthorize("hasAnyRole('HEAD','SUBHEAD','TREASURER')")
    public ResponseEntity<EventResponseDto> create(@PathVariable Long yearId, @Valid @RequestBody EventFormDto form) {
        scope.year(yearId);
        scope.members(form.getMemberIds());
        form.setId(null);
        form.setYearId(yearId);
        var result = service.create(form);
        return ResponseEntity.created(URI.create("/api/years/" + yearId + "/events/" + result.getId())).body(result);
    }

    @PutMapping("/{id}")
    @Transactional
    @PreAuthorize("hasAnyRole('HEAD','SUBHEAD','TREASURER')")
    public EventResponseDto update(@PathVariable Long yearId, @PathVariable Long id, @Valid @RequestBody EventFormDto form) {
        scope.event(yearId, id);
        scope.members(form.getMemberIds());
        form.setId(id);
        form.setYearId(yearId);
        return service.update(id, form);
    }

    @DeleteMapping("/{id}")
    @Transactional
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasAnyRole('HEAD','SUBHEAD','TREASURER')")
    public void delete(@PathVariable Long yearId, @PathVariable Long id) {
        scope.event(yearId, id);
        service.delete(id);
    }
}
