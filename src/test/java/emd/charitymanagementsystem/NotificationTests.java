package emd.charitymanagementsystem;

import emd.charitymanagementsystem.Models.*;
import emd.charitymanagementsystem.Repository.*;
import emd.charitymanagementsystem.Service.Implementation.*;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mail.MailSendException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.transaction.TestTransaction;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;
import java.math.BigDecimal;
import java.time.*;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:notification-tests", "spring.datasource.username=sa", "spring.datasource.password=",
        "spring.datasource.driver-class-name=org.h2.Driver", "spring.jpa.database-platform=org.hibernate.dialect.H2Dialect",
        "app.admin.email=test@example.com", "app.admin.password=TestOnly123!", "logging.file.name=target/notification-tests.log",
        "app.notifications.scheduling-enabled=false", "app.notifications.email-enabled=true",
        "app.notifications.from=charity@example.com", "app.notifications.base-url=https://charity.example.com",
        "spring.mail.host=smtp.example.com"
})
@Transactional @WithMockUser(username = "test@example.com", roles = "HEAD")
class NotificationTests {
    @Autowired NotificationService service;
    @Autowired NotificationEmailService sender;
    @Autowired ActivityFinanceService activity;
    @Autowired NotificationRepository notifications;
    @Autowired EmailDeliveryRepository emails;
    @Autowired UserAccountRepository accounts;
    @Autowired MemberRepository members;
    @Autowired EventRepository events;
    @Autowired EventTaskRepository tasks;
    @Autowired YearsRepository years;
    @Autowired WebApplicationContext context;
    @MockitoBean Clock clock;
    @MockitoBean JavaMailSender mail;
    Instant now;
    Event event;
    Member worker;
    UserAccount account;
    MockMvc mvc;

    @BeforeEach void setup() {
        now = Instant.parse("2027-03-10T09:00:00Z");
        when(clock.instant()).thenAnswer(invocation -> now);
        when(clock.withZone(any())).thenAnswer(invocation -> Clock.fixed(now, invocation.getArgument(0)));
        mvc = MockMvcBuilders.webAppContextSetup(context).apply(springSecurity()).build();
        account = account("worker@example.com", Role.MEMBER, true);
        worker = new Member(); worker.setName("Worker"); worker.setSurname("Member"); worker.setEmail(account.getEmail());
        worker.setPassword("encoded"); worker.setRole(Role.MEMBER); worker.setUserAccount(account); members.saveAndFlush(worker);
        account.setMember(worker);
        Years year = new Years(); year.setYearValue(2027); years.saveAndFlush(year);
        event = new Event(); event.setPurpose("Community fundraiser"); event.setDate(LocalDateTime.of(2027, 3, 31, 18, 0));
        event.setYear(year); event.setEventType(EventType.TASK_BASED); event.setMembers(new ArrayList<>()); events.saveAndFlush(event);
    }

    private UserAccount account(String email, Role role, boolean enabled) {
        UserAccount a = new UserAccount(); a.setName("Test user"); a.setEmail(email); a.setPassword("encoded");
        a.setRole(role); a.setEnabled(enabled); return accounts.saveAndFlush(a);
    }

    private EmailDelivery assigned() {
        activity.addTask(event.getYear().getId(), event.getId(), "Prepare tables", "Set up before opening", BigDecimal.ZERO,
                List.of(worker.getId(), worker.getId()));
        return emails.findAll().stream().filter(e -> e.getTaskId() != null).findFirst().orElseThrow();
    }

    @Test void assignmentsPersistAnUnreadAlertAndOneQueuedEmailWithoutSendingDuringSave() {
        EmailDelivery delivery = assigned();
        assertEquals(1, service.header(account.getEmail()).unread());
        assertEquals(1, emails.count());
        assertEquals(EmailDelivery.Status.PENDING, delivery.getStatus());
        service.taskAssigned(tasks.findById(delivery.getTaskId()).orElseThrow());
        assertEquals(1, notifications.count()); assertEquals(1, emails.count());
        verifyNoInteractions(mail);
    }

    @Test void rollbackOfAssignmentAlsoRollsBackBothNotificationAndEmail() {
        EmailDelivery delivery = assigned();
        Long id = delivery.getId(); Long taskId = delivery.getTaskId();
        TestTransaction.flagForRollback(); TestTransaction.end();
        assertFalse(emails.existsById(id)); assertFalse(tasks.existsById(taskId));
        assertEquals(0, notifications.count()); verifyNoInteractions(mail);
    }

    @Test void sendsAllThreeMilestonesOnceToEveryEnabledAccount() {
        account("donor@example.com", Role.DONOR, true);
        UserAccount disabled = account("disabled@example.com", Role.MEMBER, false);
        long recipients = accounts.findByEnabledTrue().size();
        event.setDate(event.getDate().plusDays(1)); service.remindEvent(event.getId()); assertEquals(0, notifications.count());
        event.setDate(event.getDate().minusDays(1));
        for (int daysElapsed : new int[]{0, 14, 20}) {
            now = Instant.parse("2027-03-10T09:00:00Z").plus(Duration.ofDays(daysElapsed));
            service.remindEvent(event.getId()); service.remindEvent(event.getId());
        }
        assertEquals(recipients * 3, notifications.count()); assertEquals(recipients * 3, emails.count());
        assertEquals(0, notifications.countByRecipientIdAndReadAtIsNull(disabled.getId()));
        assertEquals(3, service.inbox("donor@example.com", 0).getTotalElements());
        assertTrue(service.inbox("donor@example.com", 0).stream().allMatch(n -> n.eventPath() == null));
    }

    @Test void catchesUpOnlyTheCurrentMilestoneAndUsesLocalCalendarDates() {
        now = Instant.parse("2027-03-24T23:30:00Z"); // March 25 in Skopje: six days before event.
        service.remindEvent(event.getId());
        assertEquals(1, service.header(account.getEmail()).unread());
        assertTrue(emails.findAll().stream().allMatch(e -> e.getReminderDays() == 7));
        now = Instant.parse("2027-03-30T23:30:00Z"); // Event day locally: do not create a day-before alert.
        service.remindEvent(event.getId());
        assertEquals(1, service.header(account.getEmail()).unread());
    }

    @Test void reschedulingCreatesFreshRemindersAndCancelsStaleQueuedEmail() {
        service.remindEvent(event.getId());
        List<EmailDelivery> old = emails.findAll();
        event.setDate(event.getDate().minusDays(20)); events.saveAndFlush(event);
        service.remindEvent(event.getId());
        assertEquals(2, service.header(account.getEmail()).unread());
        for (EmailDelivery delivery : old) {
            sender.deliver(delivery.getId()); assertEquals(EmailDelivery.Status.CANCELLED, delivery.getStatus());
        }
        verifyNoInteractions(mail);
    }

    @Test void retriesSmtpFailuresWithoutLosingNotificationAndDoesNotResendSentMail() {
        EmailDelivery delivery = assigned();
        doThrow(new MailSendException("Unavailable")).doNothing().when(mail).send(any(SimpleMailMessage.class));
        sender.deliver(delivery.getId());
        assertEquals(EmailDelivery.Status.PENDING, delivery.getStatus()); assertEquals(1, delivery.getAttempts());
        assertEquals(1, service.header(account.getEmail()).unread());
        sender.deliver(delivery.getId()); verify(mail, times(1)).send(any(SimpleMailMessage.class));
        now = now.plusSeconds(121); sender.deliver(delivery.getId()); sender.deliver(delivery.getId());
        assertEquals(EmailDelivery.Status.SENT, delivery.getStatus());
        var capture = org.mockito.ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mail, times(2)).send(capture.capture());
        assertArrayEquals(new String[]{account.getEmail()}, capture.getValue().getTo());
        assertTrue(capture.getValue().getText().contains("https://charity.example.com/notifications"));
    }

    @Test void disabledRecipientsAndRemovedAssignmentsAreNotEmailed() {
        EmailDelivery delivery = assigned(); account.setEnabled(false); sender.deliver(delivery.getId());
        assertEquals(EmailDelivery.Status.CANCELLED, delivery.getStatus());
        account.setEnabled(true);
        activity.addTask(event.getYear().getId(), event.getId(), "Another task", null, BigDecimal.ZERO, List.of(worker.getId()));
        EmailDelivery other = emails.findAll().stream().filter(e -> !e.getId().equals(delivery.getId())).findFirst().orElseThrow();
        tasks.findById(other.getTaskId()).orElseThrow().getMembers().clear(); sender.deliver(other.getId());
        assertEquals(EmailDelivery.Status.CANCELLED, other.getStatus()); verifyNoInteractions(mail);
    }

    @Test void inboxAndReadActionsArePrivateAndRequireCsrf() throws Exception {
        assigned(); Long id = notifications.findAll().get(0).getId();
        String preview = mvc.perform(get("/notifications").with(user(account.getEmail()).roles("MEMBER")))
                .andExpect(status().isOk()).andExpect(view().name("notifications/list"))
                .andExpect(content().string(containsString("Prepare tables")))
                .andExpect(content().string(containsString("Notifications, 1 unread")))
                .andReturn().getResponse().getContentAsString();
        java.nio.file.Files.writeString(java.nio.file.Path.of("target/notifications-preview.html"), preview);
        mvc.perform(get("/notifications").with(user("test@example.com").roles("HEAD")))
                .andExpect(content().string(not(containsString("Prepare tables"))));
        mvc.perform(post("/notifications/" + id + "/read").with(user("test@example.com").roles("HEAD")).with(csrf()))
                .andExpect(redirectedUrl("/notifications"));
        assertEquals(1, service.header(account.getEmail()).unread());
        mvc.perform(post("/notifications/" + id + "/read").with(user(account.getEmail()).roles("MEMBER")))
                .andExpect(redirectedUrl("/access-denied"));
        mvc.perform(post("/notifications/" + id + "/read").with(user(account.getEmail()).roles("MEMBER")).with(csrf()))
                .andExpect(redirectedUrl("/notifications"));
        assertEquals(0, service.header(account.getEmail()).unread());
        mvc.perform(get("/notifications").with(anonymous())).andExpect(status().is3xxRedirection());
    }

    @Test void markAllReadAffectsOnlyTheCurrentUser() throws Exception {
        service.remindEvent(event.getId());
        mvc.perform(post("/notifications/read-all").with(user(account.getEmail()).roles("MEMBER")).with(csrf()))
                .andExpect(redirectedUrl("/notifications"));
        assertEquals(0, service.header(account.getEmail()).unread());
        assertEquals(1, service.header("test@example.com").unread());
    }

    @Test void schedulerFindsOnlyUpcomingEventsAndQueuesReminders() {
        NotificationScheduler scheduler = new NotificationScheduler(service, sender, events, emails, clock);
        org.springframework.test.util.ReflectionTestUtils.setField(scheduler, "timeZone", "Europe/Skopje");
        scheduler.reminders(); scheduler.reminders();
        assertEquals(accounts.findByEnabledTrue().size(), emails.count());
        verifyNoInteractions(mail);
    }

    @Test void emailRetriesStopAfterEightFailures() {
        EmailDelivery delivery = assigned();
        doThrow(new MailSendException("Unavailable")).when(mail).send(any(SimpleMailMessage.class));
        for (int i = 0; i < 8; i++) {
            sender.deliver(delivery.getId()); now = now.plusSeconds(21601);
        }
        assertEquals(EmailDelivery.Status.FAILED, delivery.getStatus());
        sender.deliver(delivery.getId()); verify(mail, times(8)).send(any(SimpleMailMessage.class));
    }

    @Test void missingSmtpConfigurationKeepsMailQueuedWithoutConsumingRetries() {
        EmailDelivery delivery = assigned();
        org.springframework.test.util.ReflectionTestUtils.setField(sender, "mailHost", "");
        try {
            sender.deliver(delivery.getId());
            assertEquals(EmailDelivery.Status.PENDING, delivery.getStatus());
            assertEquals(0, delivery.getAttempts()); verifyNoInteractions(mail);
        } finally {
            org.springframework.test.util.ReflectionTestUtils.setField(sender, "mailHost", "smtp.example.com");
        }
    }

    @Test void deletedEventsAndExpiredMilestonesCancelQueuedReminders() {
        service.remindEvent(event.getId());
        EmailDelivery old = emails.findAll().get(0);
        now = now.plus(Duration.ofDays(14)); sender.deliver(old.getId());
        assertEquals(EmailDelivery.Status.CANCELLED, old.getStatus());
        service.remindEvent(event.getId());
        events.delete(event); events.flush();
        for (EmailDelivery delivery : emails.findAll()) sender.deliver(delivery.getId());
        assertTrue(emails.findAll().stream().allMatch(e -> e.getStatus() == EmailDelivery.Status.CANCELLED));
        assertTrue(service.inbox(account.getEmail(), 0).stream().allMatch(n -> n.eventPath() == null));
        verifyNoInteractions(mail);
    }

    @Test void legacyMemberWithoutAccountStillGetsAssignmentEmail() {
        worker.setUserAccount(null); account.setMember(null); members.saveAndFlush(worker);
        EmailDelivery delivery = assigned(); sender.deliver(delivery.getId());
        assertEquals(0, notifications.count()); assertEquals(EmailDelivery.Status.SENT, delivery.getStatus());
        verify(mail).send(any(SimpleMailMessage.class));
    }
}
