package emd.charitymanagementsystem.Service.Implementation;

import emd.charitymanagementsystem.Models.EmailDelivery;
import emd.charitymanagementsystem.Repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import java.time.*;

@Component @RequiredArgsConstructor @Slf4j
@ConditionalOnProperty(name = "app.notifications.scheduling-enabled", havingValue = "true", matchIfMissing = true)
public class NotificationScheduler {
    private final NotificationService notifications;
    private final NotificationEmailService sender;
    private final EventRepository events;
    private final EmailDeliveryRepository emails;
    private final Clock clock;
    @Value("${app.public-zone:Europe/Skopje}") private String timeZone;
    @Value("${app.notifications.email-enabled:false}") private boolean emailEnabled;

    @Scheduled(initialDelayString = "${app.notifications.initial-delay-ms:60000}",
            fixedDelayString = "${app.notifications.reminder-delay-ms:900000}")
    public void reminders() {
        LocalDate today = LocalDate.now(clock.withZone(ZoneId.of(timeZone)));
        for (Long id : events.findReminderCandidates(today.plusDays(1).atStartOfDay(), today.plusDays(22).atStartOfDay())) {
            try { notifications.remindEvent(id); }
            catch (RuntimeException failure) { log.error("Could not generate reminders for event {}", id, failure); }
        }
    }

    @Scheduled(initialDelayString = "${app.notifications.initial-delay-ms:60000}",
            fixedDelayString = "${app.notifications.email-delay-ms:30000}")
    public void emails() {
        if (!emailEnabled) return;
        for (Long id : emails.findDue(EmailDelivery.Status.PENDING, clock.instant(), PageRequest.of(0, 50))) {
            try { sender.deliver(id); }
            catch (RuntimeException failure) { log.error("Could not process notification email {} ({})", id, failure.getClass().getSimpleName()); }
        }
    }
}
