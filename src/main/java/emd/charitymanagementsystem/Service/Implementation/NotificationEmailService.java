package emd.charitymanagementsystem.Service.Implementation;

import com.resend.Resend;
import com.resend.core.exception.ResendException;
import com.resend.services.emails.model.CreateEmailOptions;
import emd.charitymanagementsystem.Models.*;
import emd.charitymanagementsystem.Repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.*;
import java.util.Objects;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationEmailService {

    private final EmailDeliveryRepository emails;
    private final UserAccountRepository accounts;
    private final MemberRepository members;
    private final EventRepository events;
    private final EventTaskRepository tasks;
    private final Clock clock;

    @Value("${app.notifications.email-enabled:false}")
    private boolean enabled;

    @Value("${app.notifications.from:}")
    private String from;

    @Value("${app.notifications.base-url:}")
    private String baseUrl;

    @Value("${app.notifications.resend-api-key:}")
    private String resendApiKey;

    @Value("${app.public-zone:Europe/Skopje}")
    private String timeZone;

    @Transactional
    public void deliver(Long id) {
        if (!enabled) {
            return;
        }

        if (resendApiKey.isBlank()
                || from.isBlank()
                || !baseUrl.matches("https?://[^\\s]+")) {

            log.warn(
                    "Notification email is enabled but Resend API key, sender, "
                            + "or application URL is missing; messages remain queued."
            );
            return;
        }

        // Serialize workers on this row to prevent concurrent duplicates.
        EmailDelivery delivery = emails.findLockedById(id).orElse(null);

        if (delivery == null
                || delivery.getStatus() != EmailDelivery.Status.PENDING
                || delivery.getNextAttemptAt().isAfter(clock.instant())) {
            return;
        }

        String recipient = recipient(delivery);

        if (recipient == null || !stillRelevant(delivery)) {
            delivery.setStatus(EmailDelivery.Status.CANCELLED);
            return;
        }

        String subject;
        String body;

        // Render current details when sending because a queued retry
        // might happen later.
        if (delivery.getTaskId() != null) {

            EventTask task = tasks.findById(delivery.getTaskId())
                    .orElseThrow();

            subject = "Task assigned: " + task.getTitle();

            body = NotificationMessages.assignment(
                    task,
                    timeZone
            );

        } else {

            Event event = events.findById(delivery.getEventId())
                    .orElseThrow();

            long days = java.time.temporal.ChronoUnit.DAYS.between(
                    LocalDate.now(
                            clock.withZone(ZoneId.of(timeZone))
                    ),
                    event.getDate().toLocalDate()
            );

            subject = "Upcoming event: " + event.getPurpose();

            body = NotificationMessages.reminder(
                    event,
                    days,
                    timeZone
            );
        }

        String finalBody =
                body
                        + "\n\nSign in to view your notifications:\n"
                        + baseUrl.replaceAll("/+$", "")
                        + "/notifications"
                        + "\n\nCharity Management";

        delivery.setAttempts(delivery.getAttempts() + 1);

        try {

            Resend resend = new Resend(resendApiKey);

            CreateEmailOptions params =
                    CreateEmailOptions.builder()
                            .from(from)
                            .to(recipient)
                            .subject(subject)
                            .text(finalBody)
                            .build();

            resend.emails().send(params);

            delivery.setStatus(EmailDelivery.Status.SENT);
            delivery.setSentAt(clock.instant());

            log.info(
                    "Notification email {} sent successfully on attempt {}",
                    id,
                    delivery.getAttempts()
            );

        } catch (ResendException failure) {

            if (delivery.getAttempts() >= 8) {

                delivery.setStatus(
                        EmailDelivery.Status.FAILED
                );

            } else {

                delivery.setNextAttemptAt(
                        clock.instant().plusSeconds(
                                Math.min(
                                        21600,
                                        60L << delivery.getAttempts()
                                )
                        )
                );
            }

            // Do not log recipient addresses, message contents,
            // or API credentials.
            log.error(
                    "Notification email {} failed on attempt {}: {}",
                    id,
                    delivery.getAttempts(),
                    failure.getMessage(),
                    failure
            );
        }
    }

    private String recipient(EmailDelivery delivery) {

        if (delivery.getAccountId() != null) {

            return accounts
                    .findById(delivery.getAccountId())
                    .filter(UserAccount::isEnabled)
                    .map(UserAccount::getEmail)
                    .orElse(null);
        }

        return delivery.getMemberId() == null
                ? null
                : members
                .findById(delivery.getMemberId())
                .filter(m ->
                        m.getUserAccount() == null
                                || m.getUserAccount().isEnabled()
                )
                .map(Member::getEmail)
                .orElse(null);
    }

    private boolean stillRelevant(EmailDelivery delivery) {

        Event event =
                events.findById(delivery.getEventId())
                        .orElse(null);

        if (event == null) {
            return false;
        }

        LocalDateTime now =
                LocalDateTime.now(
                        clock.withZone(
                                ZoneId.of(timeZone)
                        )
                );

        if (event.getDate() != null
                && !event.getDate().isAfter(now)) {
            return false;
        }

        if (delivery.getTaskId() == null) {

            if (!Objects.equals(
                    event.getDate(),
                    delivery.getEventDate())) {
                return false;
            }

            long days =
                    java.time.temporal.ChronoUnit.DAYS.between(
                            now.toLocalDate(),
                            event.getDate().toLocalDate()
                    );

            int currentMilestone =
                    days == 1
                            ? 1
                            : days >= 2 && days <= 7
                            ? 7
                            : days >= 8 && days <= 21
                            ? 21
                            : 0;

            return Objects.equals(
                    delivery.getReminderDays(),
                    currentMilestone
            );
        }

        return tasks
                .findById(delivery.getTaskId())
                .filter(t ->
                        !t.isCompleted()
                                && t.getEvent()
                                .getId()
                                .equals(event.getId())
                )
                .map(t ->
                        t.getMembers()
                                .stream()
                                .anyMatch(m ->
                                        m.getId()
                                                .equals(
                                                        delivery.getMemberId()
                                                )
                                )
                )
                .orElse(false);
    }
}