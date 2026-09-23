package emd.charitymanagementsystem.Api;

import emd.charitymanagementsystem.Models.*;
import emd.charitymanagementsystem.Models.Currency;
import emd.charitymanagementsystem.Service.Implementation.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.net.URI;
import java.time.*;
import java.util.*;

@RestController
@RequestMapping("/api/years/{yearId}")
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ActivityApiController {
    private final ActivityFinanceService service;
    private final ImpactService impact;
    private final ApiScope scope;

    public record MemberRef(Long id, String name, String surname) {
        public static MemberRef of(Member m) {
            return new MemberRef(m.getId(), m.getName(), m.getSurname());
        }
    }

    public record Payment(Long id, Long memberId, BigDecimal amount, Currency currency, LocalDate paidOn, String note) {
        static Payment of(TaskPayment p) {
            return new Payment(p.getId(), p.getMember() == null ? null : p.getMember().getId(), p.getAmount(), p.getCurrency(), p.getPaidOn(), p.getNote());
        }
    }

    public record Task(Long id, String title, String description, BigDecimal price, boolean completed,
                       List<MemberRef> members,
                       List<Payment> payments, Map<Currency, BigDecimal> paymentTotals) {
        static Task of(EventTask t) {
            return new Task(t.getId(), t.getTitle(), t.getDescription(), t.getPrice(), t.isCompleted(),
                    t.getMembers().stream().map(MemberRef::of).toList(), t.getPayments().stream().map(Payment::of).toList(), t.getPaymentTotals());
        }
    }

    public record TaskForm(@NotBlank @Size(max = 160) String title, @Size(max = 800) String description,
                           @NotNull @DecimalMin("0") @Digits(integer = 10, fraction = 2) BigDecimal price,
                           List<Long> memberIds) {
    }

    public record TaskStatus(@NotNull Boolean completed) {
    }

    public record PaymentForm(Long memberId, @NotNull @Positive @Digits(integer = 10, fraction = 2) BigDecimal amount,
                              @NotNull Currency currency, @NotNull @PastOrPresent LocalDate paidOn,
                              @Size(max = 400) String note) {
    }

    public record Revenue(Long id, LocalDate revenueMonth, String customer, BigDecimal amount, Currency currency,
                          String note) {
        static Revenue of(ProjectRevenue r) {
            return new Revenue(r.getId(), r.getRevenueMonth(), r.getCustomer(), r.getAmount(), r.getCurrency(), r.getNote());
        }
    }

    public record RevenueForm(@NotNull YearMonth month, @NotBlank @Size(max = 160) String customer,
                              @NotNull @Positive @Digits(integer = 10, fraction = 2) BigDecimal amount,
                              @NotNull Currency currency, @Size(max = 400) String note) {
    }

    public record Publication(@NotNull Boolean published) {
    }

    @GetMapping("/events/{eventId}/tasks")
    @PreAuthorize("hasAnyRole('HEAD','SUBHEAD','EVENT_MANAGER','MEMBER')")
    public List<Task> tasks(@PathVariable Long yearId, @PathVariable Long eventId) {
        scope.event(yearId, eventId);
        return service.tasks(eventId).stream().map(Task::of).toList();
    }

    @GetMapping("/events/{eventId}/tasks/{taskId}")
    @PreAuthorize("hasAnyRole('HEAD','SUBHEAD','EVENT_MANAGER','MEMBER')")
    public Task task(@PathVariable Long yearId, @PathVariable Long eventId, @PathVariable Long taskId) {
        return Task.of(scope.task(yearId, eventId, taskId));
    }

    @PostMapping("/events/{eventId}/tasks")
    @Transactional
    @PreAuthorize("hasAnyRole('HEAD','SUBHEAD','EVENT_MANAGER')")
    public ResponseEntity<Task> createTask(@PathVariable Long yearId, @PathVariable Long eventId, @Valid @RequestBody TaskForm form) {
        scope.event(yearId, eventId);
        scope.members(form.memberIds());
        Task result = Task.of(service.addTask(yearId, eventId, form.title(), form.description(), form.price(), form.memberIds()));
        return ResponseEntity.created(URI.create("/api/years/" + yearId + "/events/" + eventId + "/tasks/" + result.id())).body(result);
    }

    @PatchMapping("/events/{eventId}/tasks/{taskId}/status")
    @Transactional
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasAnyRole('HEAD','SUBHEAD','EVENT_MANAGER')")
    public void status(@PathVariable Long yearId, @PathVariable Long eventId, @PathVariable Long taskId, @Valid @RequestBody TaskStatus form) {
        scope.task(yearId, eventId, taskId);
        service.completeTask(yearId, eventId, taskId, form.completed());
    }

    @DeleteMapping("/events/{eventId}/tasks/{taskId}")
    @Transactional
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasAnyRole('HEAD','SUBHEAD','EVENT_MANAGER')")
    public void deleteTask(@PathVariable Long yearId, @PathVariable Long eventId, @PathVariable Long taskId) {
        scope.task(yearId, eventId, taskId);
        service.deleteTask(yearId, eventId, taskId);
    }

    @PostMapping("/events/{eventId}/tasks/{taskId}/payments")
    @Transactional
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('HEAD','SUBHEAD','TREASURER','EVENT_MANAGER')")
    public Payment payment(@PathVariable Long yearId, @PathVariable Long eventId, @PathVariable Long taskId,
                           @Valid @RequestBody PaymentForm form, Authentication auth) {
        scope.task(yearId, eventId, taskId);
        if (form.memberId() != null) scope.member(form.memberId());
        return Payment.of(service.addTaskPayment(yearId, eventId, taskId, form.memberId(), form.amount(), form.currency(), form.paidOn(), form.note(), auth.getName()));
    }

    @GetMapping("/projects/{projectId}/revenues")
    @PreAuthorize("hasAnyRole('HEAD','SUBHEAD','MEMBER')")
    public List<Revenue> revenues(@PathVariable Long yearId, @PathVariable Long projectId) {
        scope.project(yearId, projectId);
        return service.revenues(projectId).stream().map(Revenue::of).toList();
    }

    @PostMapping("/projects/{projectId}/revenues")
    @Transactional
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('HEAD','SUBHEAD','TREASURER','PROJECT_MANAGER')")
    public Revenue revenue(@PathVariable Long yearId, @PathVariable Long projectId, @Valid @RequestBody RevenueForm form, Authentication auth) {
        scope.project(yearId, projectId);
        return Revenue.of(service.addRevenue(yearId, projectId, form.month(), form.customer(), form.amount(), form.currency(), form.note(), auth.getName()));
    }

    @PutMapping("/projects/{projectId}/publication")
    @Transactional
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('HEAD')")
    public void projectPublication(@PathVariable Long yearId, @PathVariable Long projectId, @Valid @RequestBody Publication form) {
        scope.project(yearId, projectId);
        impact.setPublished(yearId, projectId, form.published());
    }

    @PutMapping("/events/{eventId}/publication")
    @Transactional
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('HEAD')")
    public void eventPublication(@PathVariable Long yearId, @PathVariable Long eventId, @Valid @RequestBody Publication form) {
        scope.event(yearId, eventId);
        impact.setEventPublished(yearId, eventId, form.published());
    }
}
