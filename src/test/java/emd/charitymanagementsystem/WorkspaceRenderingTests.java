package emd.charitymanagementsystem;

import emd.charitymanagementsystem.Models.Years;
import emd.charitymanagementsystem.Repository.YearsRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.hamcrest.Matchers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:workspace-tests", "spring.datasource.username=sa",
        "spring.datasource.password=", "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.jpa.database-platform=org.hibernate.dialect.H2Dialect",
        "app.admin.email=test@example.com", "app.admin.password=TestOnly123!",
        "logging.file.name=target/workspace-tests.log"
})
class WorkspaceRenderingTests {
    @Autowired
    WebApplicationContext context;
    @Autowired
    YearsRepository yearsRepository;
    MockMvc mvc;
    Long yearId;

    @BeforeEach
    void setup() {
        mvc = MockMvcBuilders.webAppContextSetup(context).apply(springSecurity()).build();
        Years year = new Years();
        year.setYearValue(2026);
        yearId = yearsRepository.save(year).getId();
    }

    @Test
    void rendersDashboardAndEverySectionAndForm() throws Exception {
        String base = "/years/" + yearId;
        String[] paths = {"/", "/members", "/members/add", "/years", "/years/add-form",
                base, base + "/projects", base + "/projects/add", base + "/donations",
                base + "/donations/add", base + "/events", base + "/events/add-form",
                base + "/budget", base + "/budget/add-form"};
        for (String path : paths) {
            mvc.perform(get(path).with(user("test@example.com").roles("HEAD")))
                    .andExpect(status().isOk()).andExpect(content().string(containsString("appSidebar")))
                    .andExpect(content().string(not(containsString("An unexpected error"))));
        }
    }

    @Test
    void keepsRequestedYearAndHidesInaccessibleSections() throws Exception {
        mvc.perform(get("/years/" + yearId + "/donations")
                        .with(user("treasurer@example.com").roles("TREASURER")))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("/years/" + yearId + "/budget")))
                .andExpect(content().string(not(containsString("data-nav=\"projects\""))))
                .andExpect(content().string(containsString("data-nav=\"members\"")));
    }

    @Test
    void rendersPublicAuthenticationPages() throws Exception {
        for (String path : new String[]{"/login", "/register"}) {
            mvc.perform(get(path)).andExpect(status().isOk())
                    .andExpect(content().string(containsString("auth-story")));
        }
    }
}
