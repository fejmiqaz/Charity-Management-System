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
import static org.junit.jupiter.api.Assertions.assertTrue;
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
    @Autowired
    emd.charitymanagementsystem.Repository.ProjectRepository projects;
    @Autowired
    emd.charitymanagementsystem.Repository.EventRepository events;
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
        String[] paths = {"/dashboard", "/members", "/members/add", "/years", "/years/add-form",
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
                    .andExpect(content().string(containsString("auth-story")))
                    .andExpect(content().string(containsString("id=\"themeToggle\"")))
                    .andExpect(content().string(containsString("value=\"sq\"")))
                    .andExpect(content().string(containsString("value=\"fr\"")))
                    .andExpect(content().string(containsString("value=\"de\"")));
        }
    }

    @Test
    void rendersPersistentThemeAndLanguageControlsInWorkspace() throws Exception {
        String html = mvc.perform(get("/dashboard").with(user("test@example.com").roles("HEAD")))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("id=\"themeToggle\"")))
                .andExpect(content().string(containsString("id=\"appLanguage\"")))
                .andExpect(content().string(containsString("data-i18n=\"Overview\"")))
                .andReturn().getResponse().getContentAsString();
        assertTrue(html.indexOf("mobile-search-trigger") < html.indexOf("id=\"mobileActions\""));
        assertTrue(html.indexOf("mobile-profile-link") < html.indexOf("id=\"appLanguage\""));
        assertTrue(html.indexOf("id=\"themeToggle\"") < html.indexOf("mobile-signout-form"));
    }

    @Test
    void projectFiltersCombineAndKeepAnnualTotals() throws Exception {
        var year = yearsRepository.findById(yearId).orElseThrow();
        for (int i = 0; i < 3; i++) {
            var project = new emd.charitymanagementsystem.Models.Project();
            project.setName("Filter project " + i);
            project.setYear(year);
            project.setProjectType(i == 0 ? emd.charitymanagementsystem.Models.ProjectType.STANDARD
                    : emd.charitymanagementsystem.Models.ProjectType.REVENUE);
            project.setStatus(i == 2 ? emd.charitymanagementsystem.Models.ProjectStatus.FINISHED
                    : emd.charitymanagementsystem.Models.ProjectStatus.PLANNED);
            project.setProjectPrice(10.0);
            project.setMembers(java.util.Set.of());
            projects.save(project);
        }
        String path = "/years/" + yearId + "/projects";
        mvc.perform(get(path).param("type", "REVENUE").param("status", "PLANNED")
                        .with(user("test@example.com").roles("HEAD")))
                .andExpect(status().isOk())
                .andExpect(model().attribute("projects", hasSize(1)))
                .andExpect(model().attribute("totalProjectPrice", 30.0))
                .andExpect(content().string(containsString("Filter project 1")))
                .andExpect(content().string(containsString("value=\"REVENUE\" selected=\"selected\"")))
                .andExpect(content().string(containsString("value=\"PLANNED\" selected=\"selected\"")));
        mvc.perform(get(path).param("status", "PLANNED").with(user("test@example.com").roles("HEAD")))
                .andExpect(model().attribute("projects", hasSize(2)));
        mvc.perform(get(path).param("type", "STANDARD").param("status", "FINISHED")
                        .with(user("test@example.com").roles("HEAD")))
                .andExpect(content().string(containsString("No projects match the selected filters.")));
        mvc.perform(get(path).param("type", "").param("status", "").with(user("test@example.com").roles("HEAD")))
                .andExpect(model().attribute("projects", hasSize(3)));
    }

    @Test
    void eventFiltersCombineByTypeAndDate() throws Exception {
        var year = yearsRepository.findById(yearId).orElseThrow();
        for (int i = 0; i < 3; i++) {
            var event = new emd.charitymanagementsystem.Models.Event();
            event.setPurpose("Filter event " + i);
            event.setYear(year);
            event.setEventType(i == 0 ? emd.charitymanagementsystem.Models.EventType.NORMAL
                    : emd.charitymanagementsystem.Models.EventType.TASK_BASED);
            event.setDate(java.time.LocalDateTime.now().plusDays(i == 2 ? -2 : 2));
            event.setMembers(java.util.List.of());
            events.save(event);
        }
        String path = "/years/" + yearId + "/events";
        mvc.perform(get(path).param("type", "TASK_BASED").param("status", "UPCOMING")
                        .with(user("test@example.com").roles("HEAD")))
                .andExpect(status().isOk())
                .andExpect(model().attribute("events", hasSize(1)))
                .andExpect(content().string(containsString("Filter event 1")))
                .andExpect(content().string(containsString("value=\"TASK_BASED\" selected=\"selected\"")))
                .andExpect(content().string(containsString("value=\"UPCOMING\" selected=\"selected\"")));
        mvc.perform(get(path).param("status", "PAST").with(user("test@example.com").roles("HEAD")))
                .andExpect(model().attribute("events", hasSize(1)))
                .andExpect(content().string(containsString("Filter event 2")));
        mvc.perform(get(path).param("type", "NORMAL").param("status", "PAST")
                        .with(user("test@example.com").roles("HEAD")))
                .andExpect(content().string(containsString("No events match the selected filters.")));
        mvc.perform(get(path).param("type", "").param("status", "").with(user("test@example.com").roles("HEAD")))
                .andExpect(model().attribute("events", hasSize(3)));
    }

    @Test
    void eventStatusHandlesExactTimeAndMissingDate() {
        var now = java.time.LocalDateTime.of(2026, 9, 22, 12, 0);
        assertTrue(emd.charitymanagementsystem.Models.EventStatus.fromDate(now, now)
                == emd.charitymanagementsystem.Models.EventStatus.UPCOMING);
        assertTrue(emd.charitymanagementsystem.Models.EventStatus.fromDate(now.minusNanos(1), now)
                == emd.charitymanagementsystem.Models.EventStatus.PAST);
        org.junit.jupiter.api.Assertions.assertNull(emd.charitymanagementsystem.Models.EventStatus.fromDate(null, now));
    }
}
