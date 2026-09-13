package emd.charitymanagementsystem;

import emd.charitymanagementsystem.Models.*;
import emd.charitymanagementsystem.Repository.*;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(properties = {"spring.datasource.url=jdbc:h2:mem:profile-tests", "spring.datasource.username=sa", "spring.datasource.password=", "spring.datasource.driver-class-name=org.h2.Driver", "spring.jpa.database-platform=org.hibernate.dialect.H2Dialect", "app.admin.email=admin@example.com", "app.admin.password=TestOnly123!", "logging.file.name=target/profile-tests.log"})
class ProfileTests {
    @Autowired
    WebApplicationContext context;
    @Autowired
    UserAccountRepository accounts;
    @Autowired
    MemberRepository members;
    MockMvc mvc;
    String email;
    Long memberId, accountId;

    @BeforeEach
    void setup() {
        mvc = MockMvcBuilders.webAppContextSetup(context).apply(springSecurity()).build();
        email = "profile" + System.nanoTime() + "@example.com";
        var account = accounts.save(UserAccount.builder().name("Test Member").email(email).password("encoded").role(Role.MEMBER).enabled(true).build());
        var member = members.save(Member.builder().name("Test").surname("Member").email(email).password("encoded").role(Role.MEMBER).phone("+38970123456").country("North Macedonia").city("Skopje").userAccount(account).build());
        memberId = member.getId();
        accountId = account.getId();
    }

    MockHttpServletRequestBuilder change(String address) {
        return post("/profile/edit").with(user(email).roles("MEMBER")).with(csrf())
                .param("name", "Updated").param("surname", "Member").param("email", address)
                .param("phone", "+38970123456").param("country", "North Macedonia").param("city", "Ohrid");
    }

    @Test
    void showsAndUpdatesOwnProfileWithoutChangingPermissions() throws Exception {
        mvc.perform(get("/profile").with(user(email).roles("MEMBER"))).andExpect(status().isOk()).andExpect(content().string(containsString(email)));
        mvc.perform(get("/profile/edit").with(user(email).roles("MEMBER"))).andExpect(status().isOk()).andExpect(content().string(containsString("Save changes")));
        mvc.perform(change(email).param("id", "9999").param("role", "HEAD").param("yearId", "9999").param("password", "attacker-password"))
                .andExpect(redirectedUrl("/profile?saved"));
        var member = members.findById(memberId).orElseThrow();
        assertEquals("Updated", member.getName());
        assertEquals("Ohrid", member.getCity());
        assertEquals(Role.MEMBER, member.getRole());
        assertNull(member.getYear());
        var account = accounts.findById(accountId).orElseThrow();
        assertEquals(Role.MEMBER, account.getRole());
        assertEquals("encoded", account.getPassword());
    }

    @Test
    void changedEmailUpdatesBothRecordsAndEndsSession() throws Exception {
        String updated = "new" + email;
        mvc.perform(change(updated)).andExpect(redirectedUrl("/login?profileUpdated"));
        assertEquals(updated, accounts.findById(accountId).orElseThrow().getEmail());
        assertEquals(updated, members.findById(memberId).orElseThrow().getEmail());
    }

    @Test
    void rejectsInvalidAndDuplicateEmail() throws Exception {
        mvc.perform(change("invalid@domain")).andExpect(view().name("profile/form")).andExpect(model().attributeHasFieldErrors("profile", "email"));
        mvc.perform(change("admin@example.com")).andExpect(view().name("profile/form")).andExpect(model().attributeHasFieldErrors("profile", "email"));
        assertEquals(email, accounts.findById(accountId).orElseThrow().getEmail());
    }

    @Test
    void requiresAuthenticationAndCsrf() throws Exception {
        mvc.perform(get("/profile")).andExpect(status().is3xxRedirection());
        mvc.perform(post("/profile/edit").with(user(email).roles("MEMBER"))).andExpect(status().is3xxRedirection());
        assertEquals("Test", members.findById(memberId).orElseThrow().getName());
    }

    @Test
    void accountWithoutMemberShowsHelpfulMessage() throws Exception {
        mvc.perform(get("/profile").with(user("admin@example.com").roles("HEAD")))
                .andExpect(status().isOk()).andExpect(content().string(containsString("no linked member record")));
    }
}
