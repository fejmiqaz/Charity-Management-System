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
}
