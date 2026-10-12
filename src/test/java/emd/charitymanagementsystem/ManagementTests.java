package emd.charitymanagementsystem;

import emd.charitymanagementsystem.Models.*;
import emd.charitymanagementsystem.Repository.*;
import emd.charitymanagementsystem.Service.Implementation.ActiveSessionService;
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
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(properties={
    "spring.datasource.url=jdbc:h2:mem:management-tests", "spring.datasource.username=sa", "spring.datasource.password=",
    "spring.datasource.driver-class-name=org.h2.Driver", "spring.jpa.database-platform=org.hibernate.dialect.H2Dialect",
    "app.admin.email=test@example.com", "app.admin.password=TestOnly123!", "logging.file.name=target/management-tests.log",
    "app.notifications.scheduling-enabled=false", "app.notifications.email-enabled=false"
})
@Transactional
class ManagementTests {
    @Autowired WebApplicationContext context;
    @Autowired UserAccountRepository accounts;
    @Autowired AuditEntryRepository audit;
    @Autowired ActiveSessionService sessions;
    @Autowired PasswordEncoder passwords;
    MockMvc mvc;
    String head="management-head@example.com";
    @BeforeEach void setup() {
        mvc=MockMvcBuilders.webAppContextSetup(context).apply(springSecurity()).build();
        var account=new UserAccount(); account.setName("Management Head"); account.setEmail(head); account.setRole(Role.HEAD);
        account.setEnabled(true); account.setPassword(passwords.encode("TestOnly123!")); accounts.saveAndFlush(account);
    }
    @Test void managementIsHeadOnlyAndChecksCurrentAccountRole() throws Exception {
        mvc.perform(get("/api/management/activity")).andExpect(status().isUnauthorized());
        mvc.perform(get("/api/management/security").with(user(head).roles("MEMBER"))).andExpect(status().isForbidden());
        var account=accounts.findByEmailIgnoreCase(head).orElseThrow(); account.setRole(Role.SUBHEAD); accounts.saveAndFlush(account);
        mvc.perform(get("/api/management/activity").with(user(head).roles("HEAD"))).andExpect(status().isForbidden());
    }
    @Test void successfulChangesAreAuditedButInvalidChangesAndSecretsAreNot() throws Exception {
        String label="2037";
        mvc.perform(post("/api/years").with(user(head).roles("HEAD")).with(csrf()).contentType(MediaType.APPLICATION_JSON).content("{\"yearValue\":"+label+"}"))
            .andExpect(status().isCreated());
        assertTrue(audit.findAll().stream().anyMatch(a -> head.equals(a.getActorEmail()) && "CREATE".equals(a.getAction()) && "Year".equals(a.getResource()) && a.getRecordId()!=null));
        long before=audit.count();
        mvc.perform(post("/api/years").with(user(head).roles("HEAD")).with(csrf()).contentType(MediaType.APPLICATION_JSON).content("{}"))
            .andExpect(status().isBadRequest());
        assertEquals(before,audit.count());
        mvc.perform(post("/api/profile/password").with(user(head).roles("HEAD")).with(csrf()).contentType(MediaType.APPLICATION_JSON)
            .content("{\"currentPassword\":\"TestOnly123!\",\"password\":\"AnotherSecret123!\",\"confirmPassword\":\"AnotherSecret123!\"}"))
            .andExpect(status().isOk());
        var response=mvc.perform(get("/api/management/activity").with(user(head).roles("HEAD")).param("action","PASSWORD_CHANGED"))
            .andExpect(status().isOk()).andExpect(jsonPath("$.content[0].action").value("PASSWORD_CHANGED")).andReturn().getResponse().getContentAsString();
        assertFalse(response.contains("AnotherSecret123!")); assertFalse(response.contains("TestOnly123!"));
    }
    @Test void securityUsesSafeSessionReferencesAndRevokeInvalidatesTheSession() throws Exception {
        var current=new MockHttpSession(); var other=new MockHttpSession();
        sessions.track(current,head); sessions.track(other,"member@example.com");
        var overview=mvc.perform(get("/api/management/security").session(current).with(user(head).roles("HEAD")))
            .andExpect(status().isOk()).andExpect(jsonPath("$.accounts[0].password").doesNotExist()).andReturn().getResponse().getContentAsString();
        assertTrue(overview.contains(ActiveSessionService.reference(other.getId())));
        mvc.perform(post("/api/management/security/sessions/"+ActiveSessionService.reference(other.getId())+"/revoke")
            .session(current).with(user(head).roles("HEAD")))
            .andExpect(status().isForbidden());
        assertFalse(other.isInvalid());
        mvc.perform(post("/api/management/security/sessions/"+ActiveSessionService.reference(current.getId())+"/revoke")
            .session(current).with(user(head).roles("HEAD")).with(csrf()))
            .andExpect(status().isBadRequest());
        mvc.perform(post("/api/management/security/sessions/"+ActiveSessionService.reference(other.getId())+"/revoke")
            .session(current).with(user(head).roles("HEAD")).with(csrf()))
            .andExpect(status().isOk());
        assertTrue(other.isInvalid());
        assertTrue(audit.findAll().stream().anyMatch(a -> "SESSION_REVOKED".equals(a.getAction())));
        sessions.sessionDestroyed(new jakarta.servlet.http.HttpSessionEvent(current));
    }
    @Test void failedLoginIsRecordedWithoutSubmittedCredentials() throws Exception {
        mvc.perform(post("/api/auth/login").with(csrf()).contentType(MediaType.APPLICATION_JSON)
            .content("{\"email\":\"no-account@example.com\",\"password\":\"NeverLogThis123!\"}"))
            .andExpect(status().isUnauthorized());
        assertTrue(audit.findAll().stream().anyMatch(a -> "SIGN_IN_FAILED".equals(a.getAction()) && a.getActorEmail()==null));
        mvc.perform(get("/api/management/security").with(user(head).roles("HEAD")))
            .andExpect(status().isOk()).andExpect(jsonPath("$.failedSignIns24h").value(org.hamcrest.Matchers.greaterThanOrEqualTo(1)));
    }
}
