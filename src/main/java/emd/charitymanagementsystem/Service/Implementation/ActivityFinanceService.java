package emd.charitymanagementsystem.Service.Implementation;

import emd.charitymanagementsystem.Models.*;
import emd.charitymanagementsystem.Models.Currency;
import emd.charitymanagementsystem.Repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.*;
import java.time.*;
import java.util.*;

@Service @RequiredArgsConstructor @Transactional(readOnly = true)
public class ActivityFinanceService {
    private final EventRepository events;
    private final EventTaskRepository tasks;
    private final TaskPaymentRepository taskPayments;
    private final ProjectRepository projects;
    private final ProjectRevenueRepository revenues;
    private final MemberRepository members;

    public List<EventTask> tasks(Long eventId) { return tasks.findByEventIdOrderByIdAsc(eventId); }
    public List<ProjectRevenue> revenues(Long projectId) { return revenues.findByProjectIdOrderByRevenueMonthDescIdDesc(projectId); }
    public BigDecimal projectRevenue(Long projectId) { return revenueTotals(revenues.findByProjectIdOrderByRevenueMonthDescIdDesc(projectId)).get(Currency.EUR); }
    public BigDecimal totalRevenue() { return revenues.totalEur().setScale(2); }
    public BigDecimal yearRevenue(Long yearId) { return yearRevenueTotals(yearId).get(Currency.EUR); }
    public Map<Currency, BigDecimal> projectRevenueTotals(Long projectId) { return revenueTotals(revenues.findByProjectIdOrderByRevenueMonthDescIdDesc(projectId)); }
    public Map<Currency, BigDecimal> yearRevenueTotals(Long yearId) { return revenueTotals(revenues.findAll().stream().filter(r -> r.getProject().getYear() != null && yearId.equals(r.getProject().getYear().getId())).toList()); }
    public Map<Currency, BigDecimal> yearTaskPaymentTotals(Long yearId) {
        Map<Currency, BigDecimal> totals = new EnumMap<>(Currency.class);
        for (Currency currency : Currency.values()) totals.put(currency, BigDecimal.ZERO.setScale(2));
        for (TaskPayment payment : taskPayments.findAll()) {
            if (payment.getTask().getEvent().getYear() != null && yearId.equals(payment.getTask().getEvent().getYear().getId()))
                totals.merge(payment.getCurrency(), payment.getAmount(), BigDecimal::add);
        }
        return totals;
    }
    private Map<Currency, BigDecimal> revenueTotals(List<ProjectRevenue> entries) {
        Map<Currency, BigDecimal> totals = new EnumMap<>(Currency.class);
        for (Currency currency : Currency.values()) totals.put(currency, BigDecimal.ZERO.setScale(2));
        for (ProjectRevenue entry : entries) totals.merge(entry.getCurrency(), entry.getAmount(), BigDecimal::add);
        return totals;
    }

    @Transactional @PreAuthorize("hasAnyRole('HEAD','SUBHEAD','EVENT_MANAGER')")
    public void addTask(Long yearId, Long eventId, String title, String description, BigDecimal price, List<Long> memberIds) {
        Event event = event(yearId, eventId);
        if (event.getEventType() != EventType.TASK_BASED) throw new IllegalArgumentException("Tasks can only be added to task-based events.");
        if (title == null || title.isBlank() || title.length() > 160) throw new IllegalArgumentException("Enter a task title up to 160 characters.");
        BigDecimal checked = money(price, true);
        EventTask task = new EventTask(); task.setEvent(event); task.setTitle(title.trim());
        task.setDescription(description == null ? null : description.trim()); task.setPrice(checked);
        if (memberIds != null) task.setMembers(new LinkedHashSet<>(members.findAllById(memberIds)));
        tasks.save(task);
    }

    @Transactional @PreAuthorize("hasAnyRole('HEAD','SUBHEAD','EVENT_MANAGER')")
    public void completeTask(Long yearId, Long eventId, Long taskId, boolean completed) {
        event(yearId, eventId); EventTask task = task(eventId, taskId); task.setCompleted(completed);
    }

    @Transactional @PreAuthorize("hasAnyRole('HEAD','SUBHEAD','TREASURER','EVENT_MANAGER')")
    public void addTaskPayment(Long yearId, Long eventId, Long taskId, Long memberId, BigDecimal amount,
                               LocalDate paidOn, String note, String actor) {
        addTaskPayment(yearId, eventId, taskId, memberId, amount, Currency.EUR, paidOn, note, actor);
    }

    @Transactional @PreAuthorize("hasAnyRole('HEAD','SUBHEAD','TREASURER','EVENT_MANAGER')")
    public void addTaskPayment(Long yearId, Long eventId, Long taskId, Long memberId, BigDecimal amount,
                               Currency currency, LocalDate paidOn, String note, String actor) {
        event(yearId, eventId); EventTask task = task(eventId, taskId);
        Member member = memberId == null ? null : members.findById(memberId).orElseThrow();
        if (member != null && task.getMembers().stream().noneMatch(item -> item.getId().equals(memberId)))
            throw new IllegalArgumentException("Payments can only be assigned to a member working on this task.");
        if (paidOn == null || paidOn.isAfter(LocalDate.now())) throw new IllegalArgumentException("Choose a valid payment date.");
        TaskPayment payment = new TaskPayment(); payment.setTask(task); payment.setMember(member);
        payment.setAmount(money(amount, false)); payment.setCurrency(Objects.requireNonNull(currency)); payment.setPaidOn(paidOn); payment.setNote(note == null ? null : note.trim());
        payment.setRecordedBy(actor); payment.setRecordedAt(Instant.now()); taskPayments.save(payment); task.getPayments().add(payment);
    }

    @Transactional @PreAuthorize("hasAnyRole('HEAD','SUBHEAD','TREASURER','PROJECT_MANAGER')")
    public void addRevenue(Long yearId, Long projectId, YearMonth month, String customer, BigDecimal amount,
                           String note, String actor) {
        addRevenue(yearId, projectId, month, customer, amount, Currency.EUR, note, actor);
    }

    @Transactional @PreAuthorize("hasAnyRole('HEAD','SUBHEAD','TREASURER','PROJECT_MANAGER')")
    public void addRevenue(Long yearId, Long projectId, YearMonth month, String customer, BigDecimal amount,
                           Currency currency, String note, String actor) {
        Project project = project(yearId, projectId);
        if (project.getProjectType() != ProjectType.REVENUE) throw new IllegalArgumentException("Income can only be recorded for revenue projects.");
        if (month == null) throw new IllegalArgumentException("Choose a revenue month.");
        if (customer == null || customer.isBlank() || customer.length() > 160) throw new IllegalArgumentException("Enter a customer or payer name.");
        ProjectRevenue revenue = new ProjectRevenue(); revenue.setProject(project); revenue.setRevenueMonth(month.atDay(1));
        revenue.setCustomer(customer.trim()); revenue.setAmount(money(amount, false)); revenue.setCurrency(Objects.requireNonNull(currency)); revenue.setNote(note == null ? null : note.trim());
        revenue.setRecordedBy(actor); revenue.setRecordedAt(Instant.now()); revenues.save(revenue);
    }

    private Event event(Long yearId, Long id) { Event event = events.findById(id).orElseThrow(); if(event.getYear()==null || !event.getYear().getId().equals(yearId)) throw new IllegalArgumentException("Event does not belong to this year."); return event; }
    private EventTask task(Long eventId, Long id) { EventTask task = tasks.findById(id).orElseThrow(); if(!task.getEvent().getId().equals(eventId)) throw new IllegalArgumentException("Task does not belong to this event."); return task; }
    private Project project(Long yearId, Long id) { Project project = projects.findById(id).orElseThrow(); if(project.getYear()==null || !project.getYear().getId().equals(yearId)) throw new IllegalArgumentException("Project does not belong to this year."); return project; }
    private BigDecimal money(BigDecimal value, boolean allowZero) { if(value==null || (allowZero ? value.signum()<0 : value.signum()<=0) || value.scale()>2 || value.compareTo(new BigDecimal("9999999999.99"))>0) throw new IllegalArgumentException("Enter a valid amount with at most two decimals."); return value.setScale(2); }
}
