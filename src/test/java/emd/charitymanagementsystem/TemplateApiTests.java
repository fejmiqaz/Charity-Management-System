package emd.charitymanagementsystem;
import org.junit.jupiter.api.Test;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.Matchers.*;

class TemplateApiTests extends ApiTests {
    @Test void publicHomepageReturnsJsonInsteadOfResolvingAThymeleafTemplate() throws Exception {
        for (String accept : java.util.List.of("application/json", "text/html")) {
            mvc.perform(get("/api/public/home").header("Accept", accept))
                    .andExpect(status().isOk())
                    .andExpect(content().contentTypeCompatibleWith("application/json"))
                    .andExpect(jsonPath("$.projects").isArray())
                    .andExpect(jsonPath("$.events").isArray())
                    .andExpect(jsonPath("$.projectChart").isArray())
                    .andExpect(jsonPath("$.eventChart").isArray())
                    .andExpect(jsonPath("$.publicZone").value("Europe/Skopje"));
        }
    }
    @Test void yearOverviewContainsTemplateSectionsWithoutEntitiesOrCredentials() throws Exception {
        mvc.perform(get("/api/years/" + year.getId() + "/overview").with(user(head).roles("HEAD")))
                .andExpect(status().isOk()).andExpect(jsonPath("$.year.yearValue").value(2029))
                .andExpect(jsonPath("$.projects").isArray()).andExpect(jsonPath("$.events").isArray())
                .andExpect(jsonPath("$.donations").isArray()).andExpect(jsonPath("$.yearMembers").isArray())
                .andExpect(jsonPath("$.membershipPayments").isArray()).andExpect(jsonPath("$.activityBalance.EUR").exists())
                .andExpect(content().string(not(containsString("password"))));
        mvc.perform(get("/api/years/" + year.getId() + "/overview").with(user(head).roles("MEMBER")))
                .andExpect(status().isOk()).andExpect(jsonPath("$.membershipPayments").doesNotExist());
        mvc.perform(get("/api/years/" + year.getId() + "/overview").with(user(head).roles("TREASURER")))
                .andExpect(status().isForbidden());
        mvc.perform(get("/api/years/" + year.getId() + "/overview").with(user(head).roles("VOLUNTEER")))
                .andExpect(status().isForbidden());
    }
    @Test void ownProfileHistoryDoesNotExposeAccountCredentials() throws Exception {
        mvc.perform(get("/api/profile/overview").with(user(head).roles("MEMBER")))
                .andExpect(status().isOk()).andExpect(jsonPath("$.member.email").value(head))
                .andExpect(jsonPath("$.membershipHistory").isArray()).andExpect(jsonPath("$.membershipYear").exists())
                .andExpect(content().string(not(containsString("password"))));
        mvc.perform(get("/api/profile/overview")).andExpect(status().isUnauthorized());
    }
    @Test void membershipSummaryAndLedgerKeepExistingRoleBoundaries() throws Exception {
        mvc.perform(get("/api/memberships/status").param("year","2029").with(user(head).roles("MEMBER")))
                .andExpect(status().isOk()).andExpect(jsonPath("$.year").value(2029)).andExpect(jsonPath("$.paidMemberships").isMap());
        mvc.perform(get("/api/memberships/overview").param("year","2029").with(user(head).roles("MEMBER")))
                .andExpect(status().isForbidden());
        mvc.perform(get("/api/memberships/overview").param("year","2029").with(user(head).roles("TREASURER")))
                .andExpect(status().isOk()).andExpect(jsonPath("$.payments").isArray());
    }
}

