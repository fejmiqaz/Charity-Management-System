package emd.charitymanagementsystem;

import emd.charitymanagementsystem.DTO.auth.RegistrationDto;
import emd.charitymanagementsystem.Repository.UserAccountRepository;
import emd.charitymanagementsystem.Service.UserAccountService;
import emd.charitymanagementsystem.Security.GoogleAccountService;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.authentication.*;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.oidc.OidcIdToken;
import org.springframework.security.oauth2.core.oidc.user.*;
import java.time.Instant;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(properties = {
    "spring.datasource.url=jdbc:h2:mem:google-revised-tests", "spring.datasource.username=sa", "spring.datasource.password=",
    "spring.datasource.driver-class-name=org.h2.Driver", "spring.jpa.database-platform=org.hibernate.dialect.H2Dialect",
    "app.admin.email=admin@example.com", "app.admin.password=TestOnly123!", "logging.file.name=target/google-tests.log",
    "app.notifications.scheduling-enabled=false", "app.auth.google-enabled=true",
    "app.auth.google-client-id=test-client", "app.auth.google-client-secret=test-secret"
})
@Transactional
class GoogleAuthenticationTests {
    @Autowired UserAccountService registration;
    @Autowired UserAccountRepository accounts;
    @Autowired AuthenticationManager apiAuthenticationManager;
    @Autowired GoogleAccountService google;
    @Autowired emd.charitymanagementsystem.Service.Implementation.UsernameService usernames;
    @Autowired org.springframework.jdbc.core.JdbcTemplate jdbc;
    @Autowired org.springframework.transaction.PlatformTransactionManager transactions;
    @Autowired org.springframework.web.context.WebApplicationContext webContext;

    org.springframework.test.web.servlet.MockMvc mvc() {
        return org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup(webContext)
            .apply(org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity()).build();
    }

    @Test void googleOnlyUserCreatesPasswordAndCanLoginUsingEmail() throws Exception {
        google.authenticate(identity("password@gmail.com"));
        var account = accounts.findByEmailIgnoreCase("password@gmail.com").orElseThrow();
        mvc().perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post("/api/profile/password")
            .with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user(account.getEmail()).roles("MEMBER"))
            .with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf())
            .contentType("application/json").content("{\"password\":\"NewPassword123!\",\"confirmPassword\":\"NewPassword123!\"}"))
            .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.status().isOk());
        accounts.flush();
        assertEquals(account.getPassword(), account.getMember().getPassword());
        assertEquals(account.getEmail(), apiAuthenticationManager.authenticate(
            UsernamePasswordAuthenticationToken.unauthenticated(account.getEmail(), "NewPassword123!")).getName());
        mvc().perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post("/api/profile/password")
            .with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user(account.getEmail()).roles("MEMBER"))
            .with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf())
            .contentType("application/json").content("{\"password\":\"OtherPassword123!\",\"confirmPassword\":\"OtherPassword123!\"}"))
            .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.status().isBadRequest());
    }

    @Test void headChangesOtherUsersRoleAndMembersCannotChangeRoles() throws Exception {
        var account = registration.register(form("roles@gmail.com"));
        String path = "/api/accounts/" + account.getId() + "/role";
        var mvc = mvc();
        mvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put(path)
            .with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user(account.getEmail()).roles("MEMBER"))
            .with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf())
            .contentType("application/json").content("{\"role\":\"HEAD\"}"))
            .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.status().isForbidden());
        mvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put(path)
            .with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user("admin@example.com").roles("HEAD"))
            .with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf())
            .contentType("application/json").content("{\"role\":\"TREASURER\"}"))
            .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.status().isOk());
        assertEquals(emd.charitymanagementsystem.Models.Role.TREASURER, account.getRole());
        assertEquals(account.getRole(), account.getMember().getRole());
    }

    @Test void staleHeadSessionCannotKeepHeadPermissionsAfterDemotion() throws Exception {
        var account = registration.register(form("demoted@gmail.com"));
        var session = new org.springframework.mock.web.MockHttpSession();
        session.setAttribute("accountAuthenticated", true);
        mvc().perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get("/api/accounts").session(session)
            .with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user(account.getEmail()).roles("HEAD")))
            .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.status().isForbidden());
    }

    @AfterEach void clear() { SecurityContextHolder.clearContext(); }

    RegistrationDto form(String email) {
        var dto = new RegistrationDto(); dto.setName("Fejmi"); dto.setSurname("Qazimi"); dto.setEmail(email);
        dto.setPhone("+38970123456"); dto.setCountry("MK"); dto.setCity("Skopje");
        dto.setPassword("TestPassword123!"); dto.setConfirmPassword("TestPassword123!"); return dto;
    }

    @Test void registrationNeedsNoEmailSenderAndPasswordLoginWorksImmediately() {
        var account = registration.register(form("first@example.com"));
        assertEquals("fejmi.qazimi", account.getUsername());
        for (String identifier : List.of(account.getUsername(), account.getEmail())) {
            assertEquals(account.getEmail(), apiAuthenticationManager.authenticate(
                UsernamePasswordAuthenticationToken.unauthenticated(identifier, "TestPassword123!")).getName());
        }
    }

    @Test void sameNamesShareUsernameHaveDistinctIdsAndLoginByEmail() {
        var first = registration.register(form("first@example.com"));
        var second = registration.register(form("second@example.com")); accounts.flush();
        assertNotEquals(first.getId(), second.getId());
        assertEquals(first.getUsername(), second.getUsername());
        assertThrows(org.springframework.security.core.AuthenticationException.class, () -> apiAuthenticationManager.authenticate(
            UsernamePasswordAuthenticationToken.unauthenticated("fejmi.qazimi", "TestPassword123!")));
        for (var account : List.of(first, second)) assertEquals(account.getEmail(), apiAuthenticationManager.authenticate(
            UsernamePasswordAuthenticationToken.unauthenticated(account.getEmail(), "TestPassword123!")).getName());
    }

    OidcUser identity(String email) {
        return new DefaultOidcUser(List.of(), new OidcIdToken("test", Instant.now(), Instant.now().plusSeconds(300),
            Map.of("sub", "google-123", "email", email, "email_verified", true)));
    }

    @Test
    @Transactional(propagation = org.springframework.transaction.annotation.Propagation.NOT_SUPPORTED)
    void migratesPreviouslyUniqueNumberedUsernames() {
        var first = accounts.saveAndFlush(emd.charitymanagementsystem.Models.UserAccount.builder()
            .name("Migration User").email("migration-first@example.com").username("migration.user")
            .password("encoded").role(emd.charitymanagementsystem.Models.Role.MEMBER).enabled(true).build());
        var second = accounts.saveAndFlush(emd.charitymanagementsystem.Models.UserAccount.builder()
            .name("Migration User").email("migration-second@example.com").username("migration.user.1")
            .password("encoded").role(emd.charitymanagementsystem.Models.Role.MEMBER).enabled(true).build());
        try {
            jdbc.execute("ALTER TABLE user_accounts ADD CONSTRAINT previous_username_unique UNIQUE (username)");
            usernames.migrate();
            assertEquals("migration.user", accounts.findById(second.getId()).orElseThrow().getUsername());
            assertEquals(2, accounts.findAllByUsernameIgnoreCase("migration.user").size());
        } finally {
            jdbc.execute("ALTER TABLE user_accounts DROP CONSTRAINT IF EXISTS previous_username_unique");
            accounts.deleteAllById(List.of(first.getId(), second.getId()));
        }
    }

    @Test void googleRequiresExplicitMatchingLinkThenSignsIntoSameDatabaseAccount() {
        var account = registration.register(form("first@example.com"));
        var identity = identity(account.getEmail());
        assertThrows(OAuth2AuthenticationException.class, () -> google.authenticate(identity));
        SecurityContextHolder.getContext().setAuthentication(
            UsernamePasswordAuthenticationToken.authenticated(account.getEmail(), "ignored", List.of()));
        assertThrows(OAuth2AuthenticationException.class, () -> google.authenticate(identity("different@example.com")));
        var principal = google.authenticate(identity);
        assertEquals(account.getEmail(), principal.getName());
        assertEquals("ROLE_MEMBER", principal.getAuthorities().iterator().next().getAuthority());
        SecurityContextHolder.clearContext();
        assertEquals(account.getEmail(), google.authenticate(identity).getName());
        account.setEnabled(false);
        assertThrows(OAuth2AuthenticationException.class, () -> google.authenticate(identity));
    }

    @Test void gmailAutomaticallyLinksExistingAccountWithoutCreatingAnotherUser() {
        var account = registration.register(form("first@gmail.com"));
        long before = accounts.count();
        assertEquals(account.getEmail(), google.authenticate(identity("first@gmail.com")).getName());
        assertEquals("google-123", account.getGoogleSubject());
        assertEquals(before, accounts.count());
    }

    @Test void newGoogleUserGetsMemberAccountWithoutLocalPassword() {
        google.authenticate(identity("new@gmail.com"));
        var account = accounts.findByEmailIgnoreCase("new@gmail.com").orElseThrow();
        assertEquals("google-123", account.getGoogleSubject());
        assertEquals("!", account.getPassword());
        assertNotNull(account.getMember());
        assertEquals(emd.charitymanagementsystem.Models.Role.MEMBER, account.getRole());
    }

    @Test void twentyFifthAccountIsAllowedThenBothRegistrationMethodsRejectNewAccounts() {
        while (accounts.count() < 24) {
            accounts.saveAndFlush(emd.charitymanagementsystem.Models.UserAccount.builder()
                .name("Existing User").email("existing" + accounts.count() + "@gmail.com").password("!")
                .role(emd.charitymanagementsystem.Models.Role.MEMBER).enabled(true).build());
        }
        registration.register(form("last@gmail.com"));
        accounts.flush();
        assertEquals(25, accounts.count());
        assertThrows(IllegalArgumentException.class, () -> registration.register(form("overflow@gmail.com")));
        assertThrows(OAuth2AuthenticationException.class, () -> google.authenticate(identity("overflow@gmail.com")));
        // Existing users can still link and log in at capacity.
        assertEquals("last@gmail.com", google.authenticate(identity("last@gmail.com")).getName());
        assertEquals(25, accounts.count());
    }

    @Test
    @Transactional(propagation = org.springframework.transaction.annotation.Propagation.NOT_SUPPORTED)
    void simultaneousRegistrationsCannotExceedTwentyFive() throws Exception {
        String prefix = "quota" + System.nanoTime();
        var pool = java.util.concurrent.Executors.newFixedThreadPool(2);
        try {
            while (accounts.count() < 24) accounts.saveAndFlush(emd.charitymanagementsystem.Models.UserAccount.builder()
                .name("Existing User").email(prefix + accounts.count() + "@gmail.com").password("!")
                .role(emd.charitymanagementsystem.Models.Role.MEMBER).enabled(true).build());
            var first = pool.submit(() -> registration.register(form(prefix + "first@gmail.com")));
            var second = pool.submit(() -> registration.register(form(prefix + "second@gmail.com")));
            int succeeded = 0;
            for (var future : List.of(first, second)) {
                try { future.get(15, java.util.concurrent.TimeUnit.SECONDS); succeeded++; }
                catch (java.util.concurrent.ExecutionException ex) { assertInstanceOf(IllegalArgumentException.class, ex.getCause()); }
            }
            assertEquals(1, succeeded);
            assertEquals(25, accounts.count());
        } finally {
            pool.shutdownNow();
            accounts.deleteAllById(accounts.findAll().stream().filter(a -> a.getEmail().startsWith(prefix)).map(a -> a.getId()).toList());
        }
    }
}
