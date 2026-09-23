package emd.charitymanagementsystem.Service.Implementation;

import emd.charitymanagementsystem.Models.*;
import emd.charitymanagementsystem.Repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.*;
import java.time.temporal.ChronoUnit;
import java.util.*;

@Service @RequiredArgsConstructor @Transactional(readOnly = true)
public class NotificationService {
    private final NotificationRepository notifications;
    private final EmailDeliveryRepository emails;
    private final UserAccountRepository accounts;
    private final EventRepository events;
    private final Clock clock;
    @Value("${app.public-zone:Europe/Skopje}") private String timeZone;

    public record Item(Long id, String title, String message, Instant createdAt, boolean unread, String eventPath) {}
    public record Header(long unread, List<Item> items) {}

    public Header header(String email) {
        return new Header(notifications.countByRecipientEmailIgnoreCaseAndRecipientEnabledTrueAndReadAtIsNullAndClearedAtIsNull(email),
                notifications.findTop5ByRecipientEmailIgnoreCaseAndRecipientEnabledTrueAndClearedAtIsNullOrderByCreatedAtDescIdDesc(email).stream()
                        .map(n -> new Item(n.getId(), n.getTitle(), n.getMessage(), n.getCreatedAt(), n.getReadAt() == null, null)).toList());
    }

    public Page<Item> inbox(String email, int page) {
        var pageable = PageRequest.of(Math.max(0, page), 20);
        return accounts.findByEmailIgnoreCase(email).filter(UserAccount::isEnabled)
                .map(account -> notifications.findByRecipientIdAndClearedAtIsNullOrderByCreatedAtDescIdDesc(account.getId(), pageable)
                        .map(n -> item(n, account))).orElse(Page.empty(pageable));
    }

    private Item item(Notification n, UserAccount account) {
        String path = null;
        if (n.getEventId() != null && EnumSet.of(Role.HEAD, Role.SUBHEAD, Role.TREASURER,
                Role.EVENT_MANAGER, Role.VOLUNTEER, Role.MEMBER).contains(account.getRole())) {
            path = events.findById(n.getEventId()).filter(e -> e.getYear() != null)
                    .map(e -> "/years/" + e.getYear().getId() + "/events/" + e.getId()).orElse(null);
        }
        return new Item(n.getId(), n.getTitle(), n.getMessage(), n.getCreatedAt(), n.getReadAt() == null, path);
    }

    @Transactional
    public void markRead(String email, Long id) {
        accounts.findByEmailIgnoreCase(email).filter(UserAccount::isEnabled)
                .ifPresent(a -> notifications.markRead(id, a.getId(), clock.instant()));
    }

    @Transactional
    public void markAllRead(String email) {
        accounts.findByEmailIgnoreCase(email).filter(UserAccount::isEnabled)
                .ifPresent(a -> notifications.markAllRead(a.getId(), clock.instant()));
    }

    @Transactional
    public void clearAll(String email) {
        // Retain deduplication keys so the scheduler cannot recreate dismissed reminders.
        accounts.findByEmailIgnoreCase(email).filter(UserAccount::isEnabled)
                .ifPresent(a -> notifications.clearAll(a.getId(), clock.instant()));
    }

    @Transactional
    public void cancelTaskEmails(Long taskId) {
        emails.cancelTaskEmails(taskId, EmailDelivery.Status.PENDING, EmailDelivery.Status.CANCELLED);
    }

    @Transactional
    public void taskAssigned(EventTask task) {
        Event event = task.getEvent();
        String title = "Task assigned: " + task.getTitle();
        String message = NotificationMessages.assignment(task, timeZone);
        for (Member member : task.getMembers()) {
            UserAccount account = member.getUserAccount();
            if (account != null && !account.isEnabled()) continue;
            String key = "task:" + task.getId() + ":member:" + member.getId();
            if (account != null) notify(account, key, title, message, event.getId());
            queueEmail(key, account == null ? null : account.getId(), member.getId(), event, task.getId(), null, title, message);
        }
    }

    /** One reminder per current milestone; a restart never floods users with all missed milestones. */
    @Transactional
    public void remindEvent(Long eventId) {
        Event event = events.findLockedById(eventId).orElse(null);
        if (event == null || event.getDate() == null) return;
        LocalDate today = LocalDate.now(clock.withZone(ZoneId.of(timeZone)));
        long days = ChronoUnit.DAYS.between(today, event.getDate().toLocalDate());
        if (days < 1 || days > 21) return;
        int milestone = days == 1 ? 1 : days <= 7 ? 7 : 21;
        String title = "Upcoming event: " + event.getPurpose();
        String message = NotificationMessages.reminder(event, days, timeZone);
        for (UserAccount account : accounts.findByEnabledTrue()) {
            String key = "event:" + eventId + ":" + event.getDate() + ":" + milestone + ":account:" + account.getId();
            notify(account, key, title, message, eventId);
            queueEmail(key, account.getId(), null, event, null, milestone, title, message);
        }
    }

    private void notify(UserAccount account, String key, String title, String message, Long eventId) {
        if (notifications.existsByDeduplicationKey(key)) return;
        Notification n = new Notification();
        n.setRecipient(account); n.setDeduplicationKey(key); n.setTitle(title); n.setMessage(message);
        n.setEventId(eventId); n.setCreatedAt(clock.instant()); notifications.save(n);
    }

    private void queueEmail(String key, Long accountId, Long memberId, Event event, Long taskId, Integer reminderDays, String title, String message) {
        if (emails.existsByDeduplicationKey(key)) return;
        EmailDelivery delivery = new EmailDelivery();
        delivery.setDeduplicationKey(key); delivery.setAccountId(accountId); delivery.setMemberId(memberId);
        delivery.setEventId(event.getId()); delivery.setEventDate(event.getDate()); delivery.setTaskId(taskId);
        delivery.setReminderDays(reminderDays);
        delivery.setSubject(title); delivery.setBody(message); delivery.setNextAttemptAt(clock.instant());
        emails.save(delivery);
    }
}
