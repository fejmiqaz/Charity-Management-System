package emd.charitymanagementsystem;

import emd.charitymanagementsystem.Models.*;
import emd.charitymanagementsystem.Repository.*;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.*;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;
import tools.jackson.databind.ObjectMapper;
import java.time.*;
import java.util.*;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:api-tests", "spring.datasource.username=sa", "spring.datasource.password=",
        "spring.datasource.driver-class-name=org.h2.Driver", "spring.jpa.database-platform=org.hibernate.dialect.H2Dialect",
        "app.admin.email=test@example.com", "app.admin.password=TestOnly123!", "logging.file.name=target/api-tests.log",
        "app.notifications.scheduling-enabled=false", "app.notifications.email-enabled=false",
        "app.api.allowed-origins=http://localhost:5173"
})
@Transactional
class ApiTests {
    @Autowired WebApplicationContext context;
    @Autowired ObjectMapper json;
    @Autowired YearsRepository years;
    @Autowired MemberRepository members;
    @Autowired UserAccountRepository accounts;
    @Autowired EventRepository events;
    @Autowired EventTaskRepository tasks;
    @Autowired NotificationRepository notifications;
    @Autowired EmailDeliveryRepository emails;
    @Autowired PasswordEncoder passwords;
    @Autowired jakarta.persistence.EntityManager entities;
    MockMvc mvc;
    Years year;
    Member member;
    String head = "api-head@example.com";

    @BeforeEach void setup() {
        mvc = MockMvcBuilders.webAppContextSetup(context).apply(springSecurity()).build();
        UserAccount account = new UserAccount(); account.setEmail(head); account.setName("API Head"); account.setRole(Role.HEAD);
        account.setPassword(passwords.encode("ApiPassword123!")); account.setEnabled(true); accounts.saveAndFlush(account);
        member = new Member(); member.setEmail(head); member.setPassword(account.getPassword()); member.setName("API");
        member.setSurname("Head"); member.setRole(Role.HEAD); member.setPhone("+38970123456"); member.setCountry("MK"); member.setCity("Skopje");
        member.setUserAccount(account); members.saveAndFlush(member); account.setMember(member);
        year = new Years(); year.setYearValue(2029); years.saveAndFlush(year);
    }
    private String body(Object value) { return json.writeValueAsString(value); }
    private long id(MvcResult result) throws Exception { return json.readTree(result.getResponse().getContentAsString()).get("id").asLong(); }
    private String base(String section) { return "/api/years/" + year.getId() + "/" + section; }

    @Test void unauthenticatedAndForbiddenRequestsReturnJsonWhileWebLoginStillRenders() throws Exception {
        mvc.perform(get("/api/years")).andExpect(status().isUnauthorized()).andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(401)).andExpect(header().doesNotExist("Location"));
        mvc.perform(post(base("projects")).with(user(head).roles("HEAD")).contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isForbidden()).andExpect(jsonPath("$.status").value(403));
        mvc.perform(post(base("projects")).with(user("member").roles("MEMBER")).with(csrf()).contentType(MediaType.APPLICATION_JSON)
                        .content(body(project("Forbidden"))))
                .andExpect(status().isForbidden()).andExpect(jsonPath("$.status").value(403));
        mvc.perform(get("/login")).andExpect(status().isOk()).andExpect(view().name("auth/login"));
    }

    private Map<String, Object> project(String name) {
        return Map.of("id", 9999, "yearId", 9999, "name", name, "description", "A valid project description", "status", "PLANNED",
                "projectType", "STANDARD", "projectPrice", 12.50, "memberIds", List.of(member.getId()));
    }
    @Test void projectCrudUsesPathIdentityAndReturnsOnlyDtos() throws Exception {
        var created = mvc.perform(post(base("projects")).with(user(head).roles("HEAD")).with(csrf())
                        .contentType(MediaType.APPLICATION_JSON).content(body(project("API project"))))
                .andExpect(status().isCreated()).andExpect(jsonPath("$.yearId").value(year.getId()))
                .andExpect(jsonPath("$.password").doesNotExist()).andReturn();
        long id = id(created); assertNotEquals(9999, id);
        String path = base("projects") + "/" + id;
        assertEquals(path, created.getResponse().getHeader("Location"));
        mvc.perform(get(path).with(user(head).roles("HEAD"))).andExpect(status().isOk()).andExpect(jsonPath("$.name").value("API project"));
        mvc.perform(put(path).with(user(head).roles("HEAD")).with(csrf()).contentType(MediaType.APPLICATION_JSON).content(body(project("Updated project"))))
                .andExpect(status().isOk()).andExpect(jsonPath("$.id").value(id)).andExpect(jsonPath("$.name").value("Updated project"));
        mvc.perform(get(base("projects")).param("status", "PLANNED").with(user(head).roles("HEAD")))
                .andExpect(jsonPath("$", hasSize(1)));
        mvc.perform(delete("/api/years/999999/projects/" + id).with(user(head).roles("HEAD")).with(csrf()))
                .andExpect(status().isNotFound()).andExpect(jsonPath("$.status").value(404));
        mvc.perform(delete(path).with(user(head).roles("HEAD")).with(csrf())).andExpect(status().isNoContent());
        entities.flush(); entities.clear();
        mvc.perform(get(path).with(user(head).roles("HEAD"))).andExpect(status().isNotFound());
    }

    @Test void invalidPayloadsAndMissingReferencesHaveJsonErrors() throws Exception {
        mvc.perform(post(base("projects")).with(user(head).roles("HEAD")).with(csrf()).contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.fields.name").exists()).andExpect(jsonPath("$.fields.projectPrice").exists());
        mvc.perform(post(base("projects")).with(user(head).roles("HEAD")).with(csrf()).contentType(MediaType.APPLICATION_JSON).content("{invalid"))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.message").exists());
        var invalid = new HashMap<>(project("Missing member")); invalid.put("memberIds", List.of(999999));
        mvc.perform(post(base("projects")).with(user(head).roles("HEAD")).with(csrf()).contentType(MediaType.APPLICATION_JSON).content(body(invalid)))
                .andExpect(status().isBadRequest());
        mvc.perform(get("/api/years/999999").with(user(head).roles("HEAD"))).andExpect(status().isNotFound());
        mvc.perform(get(base("events")).param("type", "INVALID").with(user(head).roles("HEAD"))).andExpect(status().isBadRequest());
    }

    @Test void taskApiPreservesAssignmentsPaymentsAndDeletionNotifications() throws Exception {
        Event event = new Event(); event.setPurpose("Task event"); event.setDate(LocalDateTime.now().plusDays(30));
        event.setYear(year); event.setEventType(EventType.TASK_BASED); event.setMembers(new ArrayList<>()); events.saveAndFlush(event);
        String path = base("events") + "/" + event.getId() + "/tasks";
        long taskId = id(mvc.perform(post(path).with(user(head).roles("EVENT_MANAGER")).with(csrf())
                        .contentType(MediaType.APPLICATION_JSON).content(body(Map.of("title", "Prepare tables", "price", 10, "memberIds", List.of(member.getId())))))
                .andExpect(status().isCreated()).andExpect(jsonPath("$.members[0].id").value(member.getId()))
                .andExpect(content().string(not(containsString("password")))).andReturn());
        assertEquals(1, notifications.count()); assertEquals(1, emails.count());
        mvc.perform(post(path + "/" + taskId + "/payments").with(user(head).roles("HEAD")).with(csrf())
                        .contentType(MediaType.APPLICATION_JSON).content(body(Map.of("memberId", member.getId(), "amount", 5, "currency", "EUR", "paidOn", LocalDate.now().toString()))))
                .andExpect(status().isCreated()).andExpect(jsonPath("$.amount").value(5));
        mvc.perform(patch(path + "/" + taskId + "/status").with(user(head).roles("EVENT_MANAGER")).with(csrf())
                        .contentType(MediaType.APPLICATION_JSON).content("{\"completed\":true}"))
                .andExpect(status().isNoContent());
        mvc.perform(get(path + "/" + taskId).with(user(head).roles("MEMBER"))).andExpect(status().isOk())
                .andExpect(jsonPath("$.completed").value(true)).andExpect(jsonPath("$.payments", hasSize(1)));
        mvc.perform(delete(path + "/" + taskId).with(user(head).roles("MEMBER")).with(csrf())).andExpect(status().isForbidden());
        mvc.perform(delete(path + "/" + taskId).with(user(head).roles("EVENT_MANAGER")).with(csrf())).andExpect(status().isNoContent());
        entities.flush(); entities.clear();
        assertFalse(tasks.existsById(taskId)); assertEquals(EmailDelivery.Status.CANCELLED, emails.findAll().get(0).getStatus());
    }

    @Test void loginRotatesSessionAndCsrfAndLogoutEndsAuthentication() throws Exception {
        var start = mvc.perform(get("/api/auth/csrf")).andExpect(status().isOk()).andReturn();
        MockHttpSession session = (MockHttpSession) start.getRequest().getSession(false);
        String oldId = session.getId(); String token = json.readTree(start.getResponse().getContentAsString()).get("token").asText();
        mvc.perform(post("/api/auth/login").session(session).header("X-CSRF-TOKEN", token).contentType(MediaType.APPLICATION_JSON)
                        .content(body(Map.of("email", head, "password", "wrong-password"))))
                .andExpect(status().isUnauthorized()).andExpect(jsonPath("$.status").value(401));
        mvc.perform(post("/api/auth/login").session(session).header("X-CSRF-TOKEN", token).contentType(MediaType.APPLICATION_JSON)
                        .content(body(Map.of("email", head, "password", "ApiPassword123!"))))
                .andExpect(status().isOk()).andExpect(jsonPath("$.email").value(head)).andExpect(jsonPath("$.password").doesNotExist());
        assertNotEquals(oldId, session.getId());
        mvc.perform(get("/api/auth/me").session(session)).andExpect(status().isOk()).andExpect(jsonPath("$.role").value("HEAD"));
        mvc.perform(post("/api/auth/logout").session(session).header("X-CSRF-TOKEN", token)).andExpect(status().isForbidden());
        var fresh = mvc.perform(get("/api/auth/csrf").session(session)).andReturn();
        String newToken = json.readTree(fresh.getResponse().getContentAsString()).get("token").asText();
        mvc.perform(post("/api/auth/logout").session(session).header("X-CSRF-TOKEN", newToken)).andExpect(status().isNoContent());
        assertTrue(session.isInvalid());
        mvc.perform(get("/api/auth/me")).andExpect(status().isUnauthorized());
    }

    @Test void corsAllowsOnlyConfiguredOriginsAndKeepsCredentials() throws Exception {
        mvc.perform(options("/api/years").header("Origin", "http://localhost:5173").header("Access-Control-Request-Method", "GET"))
                .andExpect(status().isOk()).andExpect(header().string("Access-Control-Allow-Origin", "http://localhost:5173"))
                .andExpect(header().string("Access-Control-Allow-Credentials", "true"));
        mvc.perform(options("/api/years").header("Origin", "https://untrusted.example").header("Access-Control-Request-Method", "GET"))
                .andExpect(status().isForbidden()).andExpect(header().doesNotExist("Access-Control-Allow-Origin"));
    }

    @Test void membersProfileAndOptionsNeverSerializePasswordsOrEntities() throws Exception {
        for (String path : List.of("/api/members", "/api/profile", "/api/options/members", "/api/options/years", "/api/options/enums", "/api/dashboard")) {
            mvc.perform(get(path).with(user(head).roles("HEAD"))).andExpect(status().isOk())
                    .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                    .andExpect(content().string(not(containsString("password")))).andExpect(content().string(not(containsString("userAccount"))));
        }
        mvc.perform(get("/api/members").with(user(head).roles("HEAD"))).andExpect(jsonPath("$.page").value(0)).andExpect(jsonPath("$.content").isArray());
        mvc.perform(get("/api/members").with(user("donor").roles("DONOR"))).andExpect(status().isForbidden());
    }

    @Test void notificationsRemainPrivateAndCanBeClearedUsingTheApi() throws Exception {
        Notification n = new Notification(); n.setRecipient(accounts.findByEmailIgnoreCase(head).orElseThrow());
        n.setDeduplicationKey("api-notification"); n.setTitle("Private task"); n.setMessage("Only mine"); n.setCreatedAt(Instant.now()); notifications.saveAndFlush(n);
        mvc.perform(get("/api/notifications").with(user("test@example.com").roles("HEAD"))).andExpect(jsonPath("$.totalElements").value(0));
        mvc.perform(post("/api/notifications/" + n.getId() + "/read").with(user("test@example.com").roles("HEAD")).with(csrf())).andExpect(status().isNoContent());
        mvc.perform(get("/api/notifications/summary").with(user(head).roles("HEAD"))).andExpect(jsonPath("$.unread").value(1));
        mvc.perform(delete("/api/notifications").with(user(head).roles("HEAD")).with(csrf())).andExpect(status().isNoContent());
        mvc.perform(get("/api/notifications/summary").with(user(head).roles("HEAD"))).andExpect(jsonPath("$.unread").value(0));
    }

    @Test void donationEventAndBudgetCrudReturnJsonAndEnforceYearScoping() throws Exception {
        String donationBody = body(Map.of("donationAmount", 50, "currency", "EUR", "memberIds", List.of(member.getId())));
        long donationId = id(mvc.perform(post(base("donations")).with(user(head).roles("HEAD")).with(csrf())
                        .contentType(MediaType.APPLICATION_JSON).content(donationBody)).andExpect(status().isCreated()).andReturn());
        mvc.perform(put(base("donations") + "/" + donationId).with(user(head).roles("HEAD")).with(csrf())
                        .contentType(MediaType.APPLICATION_JSON).content(donationBody)).andExpect(status().isOk());
        mvc.perform(get(base("donations") + "/" + donationId).with(user(head).roles("MEMBER")))
                .andExpect(status().isOk()).andExpect(jsonPath("$.currency").value("EUR"));
        String eventBody = body(Map.of("purpose", "API event", "eventType", "TASK_BASED", "date", LocalDateTime.now().plusDays(30).toString(), "memberIds", List.of(member.getId())));
        long eventId = id(mvc.perform(post(base("events")).with(user(head).roles("HEAD")).with(csrf())
                        .contentType(MediaType.APPLICATION_JSON).content(eventBody)).andExpect(status().isCreated()).andReturn());
        mvc.perform(put(base("events") + "/" + eventId).with(user(head).roles("HEAD")).with(csrf())
                        .contentType(MediaType.APPLICATION_JSON).content(eventBody)).andExpect(status().isOk());
        mvc.perform(get(base("events") + "/" + eventId).with(user(head).roles("EVENT_MANAGER")))
                .andExpect(status().isOk()).andExpect(jsonPath("$.purpose").value("API event"));
        mvc.perform(get("/api/years/999999/events/" + eventId).with(user(head).roles("HEAD"))).andExpect(status().isNotFound());
        mvc.perform(post(base("budget")).with(user(head).roles("HEAD")).with(csrf()).contentType(MediaType.APPLICATION_JSON)
                        .content(body(Map.of("budgetAmount", 100, "donationIds", List.of(donationId)))))
                .andExpect(status().isCreated());
        entities.flush(); entities.clear();
        mvc.perform(post(base("budget")).with(user(head).roles("HEAD")).with(csrf()).contentType(MediaType.APPLICATION_JSON).content("{\"budgetAmount\":10}"))
                .andExpect(status().isConflict());
        mvc.perform(put(base("budget")).with(user(head).roles("HEAD")).with(csrf()).contentType(MediaType.APPLICATION_JSON).content("{\"budgetAmount\":200}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.budgetAmount").value(200));
        mvc.perform(get(base("budget")).with(user(head).roles("MEMBER"))).andExpect(status().isOk());
        mvc.perform(delete(base("budget")).with(user(head).roles("HEAD")).with(csrf())).andExpect(status().isNoContent());
        mvc.perform(delete(base("donations") + "/" + donationId).with(user(head).roles("HEAD")).with(csrf())).andExpect(status().isNoContent());
        mvc.perform(delete(base("events") + "/" + eventId).with(user(head).roles("HEAD")).with(csrf())).andExpect(status().isNoContent());
        entities.flush(); entities.clear();
        mvc.perform(get(base("budget")).with(user(head).roles("HEAD"))).andExpect(status().isNotFound());
    }

    @Test void yearMembershipAndRevenueOperationsWorkThroughTheApi() throws Exception {
        long yearId = id(mvc.perform(post("/api/years").with(user(head).roles("HEAD")).with(csrf()).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"yearValue\":2030}")).andExpect(status().isCreated()).andReturn());
        mvc.perform(put("/api/years/" + yearId).with(user(head).roles("HEAD")).with(csrf()).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"yearValue\":2031}")).andExpect(status().isOk()).andExpect(jsonPath("$.yearValue").value(2031));
        mvc.perform(delete("/api/years/" + yearId).with(user(head).roles("HEAD")).with(csrf())).andExpect(status().isNoContent());
        var revenueProject = new HashMap<>(project("Income project")); revenueProject.put("projectType", "REVENUE");
        long projectId = id(mvc.perform(post(base("projects")).with(user(head).roles("HEAD")).with(csrf()).contentType(MediaType.APPLICATION_JSON)
                        .content(body(revenueProject))).andExpect(status().isCreated()).andReturn());
        String revenues = base("projects") + "/" + projectId + "/revenues";
        mvc.perform(post(revenues).with(user(head).roles("PROJECT_MANAGER")).with(csrf()).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"month\":\"2029-01\",\"customer\":\"Example customer\",\"amount\":10,\"currency\":\"EUR\"}"))
                .andExpect(status().isCreated()).andExpect(jsonPath("$.customer").value("Example customer"));
        mvc.perform(get(revenues).with(user(head).roles("HEAD"))).andExpect(status().isOk()).andExpect(jsonPath("$", hasSize(1)));
        mvc.perform(put("/api/memberships/2029/fee").with(user(head).roles("TREASURER")).with(csrf()).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"amount\":20}")).andExpect(status().isNoContent());
        long receipt = id(mvc.perform(post("/api/memberships/payments").with(user(head).roles("TREASURER")).with(csrf()).contentType(MediaType.APPLICATION_JSON)
                        .content(body(Map.of("memberId", member.getId(), "year", 2029, "currency", "EUR", "paidOn", LocalDate.now().toString()))))
                .andExpect(status().isCreated()).andExpect(jsonPath("$.amount").value(20)).andReturn());
        mvc.perform(get("/api/memberships").param("year", "2029").with(user(head).roles("TREASURER")))
                .andExpect(status().isOk()).andExpect(jsonPath("$.payments[0].paid").value(true));
        mvc.perform(post("/api/memberships/payments/" + receipt + "/void").with(user(head).roles("TREASURER")).with(csrf()).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"reason\":\"Correction\"}")).andExpect(status().isNoContent());
        mvc.perform(get("/api/memberships").with(user(head).roles("MEMBER"))).andExpect(status().isForbidden());
    }

    @Test void registrationCannotChooseItsOwnRoleAndProfileEmailChangeEndsSession() throws Exception {
        Map<String, Object> form = new HashMap<>(Map.of("name", "New", "surname", "Member", "country", "MK", "city", "Skopje",
                "phone", "+38970123456", "email", "new-api@example.com", "password", "NewPassword123!", "confirmPassword", "NewPassword123!"));
        form.put("role", "HEAD");
        mvc.perform(post("/api/auth/register").with(csrf()).contentType(MediaType.APPLICATION_JSON).content(body(form)))
                .andExpect(status().isCreated()).andExpect(jsonPath("$.role").value("MEMBER")).andExpect(jsonPath("$.password").doesNotExist());
        MockHttpSession session = new MockHttpSession();
        mvc.perform(put("/api/profile").session(session).with(user(head).roles("HEAD")).with(csrf()).contentType(MediaType.APPLICATION_JSON)
                        .content(body(Map.of("name", "API", "surname", "Head", "country", "MK", "city", "Skopje", "phone", "+38970123456", "email", "changed-api@example.com"))))
                .andExpect(status().isOk()).andExpect(jsonPath("$.loginRequired").value(true));
        assertTrue(session.isInvalid());
        assertTrue(accounts.findByEmailIgnoreCase("changed-api@example.com").isPresent());
    }
}
