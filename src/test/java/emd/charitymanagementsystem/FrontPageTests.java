package emd.charitymanagementsystem;

import emd.charitymanagementsystem.Models.*;
import emd.charitymanagementsystem.Repository.*;
import emd.charitymanagementsystem.DTO.event.EventFormDto;
import emd.charitymanagementsystem.Service.EventService;
import emd.charitymanagementsystem.Service.Implementation.ImpactService;
import emd.charitymanagementsystem.Web.FrontPageController;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;
import java.time.*;
import java.util.*;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestBuilders.formLogin;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:front-page-tests", "spring.datasource.username=sa",
        "spring.datasource.password=", "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.jpa.database-platform=org.hibernate.dialect.H2Dialect",
        "app.admin.email=test@example.com", "app.admin.password=TestOnly123!",
        "logging.file.name=target/front-page-tests.log", "app.public-zone=Europe/Skopje"
})
@Transactional
class FrontPageTests {
    @Autowired WebApplicationContext context;
    @Autowired YearsRepository years;
    @Autowired EventRepository events;
    @Autowired ProjectRepository projects;
    @Autowired EventService eventService;
    @Autowired ImpactService impact;
    MockMvc mvc;
    Years year;
    LocalDateTime now;

    @BeforeEach void setup() {
        mvc = MockMvcBuilders.webAppContextSetup(context).apply(springSecurity()).build();
        year = new Years(); year.setYearValue(2026); years.saveAndFlush(year);
        now = LocalDateTime.now(ZoneId.of("Europe/Skopje")).withNano(0);
    }

    Event event(String title, LocalDateTime date, boolean published) {
        var event = new Event(); event.setPurpose(title); event.setDate(date); event.setYear(year);
        event.setPublicVisible(published); event.setMembers(new ArrayList<>());
        return events.saveAndFlush(event);
    }

    void project(String title, boolean published) {
        var project = new Project(); project.setName(title); project.setDescription("PRIVATE-DESCRIPTION");
        project.setProjectPrice(998877.0); project.setStatus(ProjectStatus.FINISHED);
        project.setPublicImpact(published); project.setYear(year); project.setMembers(new HashSet<>());
        projects.saveAndFlush(project);
    }

    String publication(Event event) { return "/years/" + year.getId() + "/events/" + event.getId() + "/publication"; }

    @Test void showsOnlyApprovedUpcomingEventsInNearestOrderAndPublicCharts() throws Exception {
        event("Later community day", now.plusDays(7), true);
        event("Nearest community day", now.plusDays(1), true);
        event("PRIVATE-EVENT", now.plusHours(1), false);
        event("PAST-EVENT", now.minusHours(1), true);
        event("UNDATED-EVENT", null, true);
        project("Community garden completed", true); project("PRIVATE-PROJECT", false);
        var result = mvc.perform(get("/")).andExpect(status().isOk()).andExpect(view().name("index"))
                .andExpect(model().attribute("completedProjects", 1))
                .andExpect(model().attribute("events", hasSize(2)))
                .andExpect(content().string(not(containsString("PRIVATE-"))))
                .andExpect(content().string(not(containsString("PAST-EVENT"))))
                .andExpect(content().string(not(containsString("UNDATED-EVENT"))))
                .andExpect(content().string(not(containsString("998877"))))
                .andExpect(model().attributeDoesNotExist("donations", "membershipPayments", "budget", "navigationYears"))
                .andReturn();
        var publicEvents = impact.upcomingEvents(now);
        assertEquals("Nearest community day", publicEvents.get(0).purpose());
        assertEquals("Later community day", publicEvents.get(1).purpose());
        var chart = (List<FrontPageController.Bar>) result.getModelAndView().getModel().get("projectChart");
        assertEquals(List.of(new FrontPageController.Bar("2026", 1, 100)), chart);
        var eventChart = (List<FrontPageController.Bar>) result.getModelAndView().getModel().get("eventChart");
        assertEquals(12, eventChart.size());
        assertEquals(2, eventChart.stream().mapToLong(FrontPageController.Bar::count).sum());
        // Synthetic rendered fixture for visual review; no live data is written here.
        java.nio.file.Files.writeString(java.nio.file.Path.of("target/front-page-preview.html"), result.getResponse().getContentAsString());
    }

    @Test void exactEventBoundaryIsIncludedButPastAndPrivateAreNot() {
        event("At boundary", now, true); event("Before boundary", now.minusSeconds(1), true);
        event("Hidden boundary", now, false);
        assertEquals(List.of("At boundary"), impact.upcomingEvents(now).stream().map(e -> e.purpose()).toList());
    }

    @Test void headCanPublishAndUnpublishWhileOthersCannot() throws Exception {
        var event = event("Reviewed event", now.plusDays(1), false);
        for (Role role : Role.values()) {
            if (role == Role.HEAD) continue;
            mvc.perform(post(publication(event)).with(user("person@example.com").roles(role.name()))
                    .with(csrf()).param("published", "true"));
            assertFalse(events.findById(event.getId()).orElseThrow().isPublicVisible());
        }
        mvc.perform(post(publication(event)).with(user("head@example.com").roles("HEAD"))
                .param("published", "true")).andExpect(redirectedUrl("/access-denied"));
        assertFalse(event.isPublicVisible());
        mvc.perform(post(publication(event)).with(user("head@example.com").roles("HEAD"))
                .with(csrf()).param("published", "true")).andExpect(status().is3xxRedirection());
        assertTrue(events.findById(event.getId()).orElseThrow().isPublicVisible());
        mvc.perform(get("/years/" + year.getId() + "/events/" + event.getId())
                .with(user("head@example.com").roles("HEAD"))).andExpect(view().name("events/details"))
                .andExpect(content().string(containsString("Save publication setting")));
        mvc.perform(post(publication(event)).with(user("head@example.com").roles("HEAD"))
                .with(csrf())).andExpect(status().is3xxRedirection());
        assertFalse(events.findById(event.getId()).orElseThrow().isPublicVisible());
    }

    @Test void wrongYearCannotPublishAndNullDateCannotPublish() throws Exception {
        var event = event("Unscheduled", null, false);
        mvc.perform(post(publication(event)).with(user("head@example.com").roles("HEAD"))
                .with(csrf()).param("published", "true"));
        assertFalse(event.isPublicVisible());
        event.setDate(now.plusDays(1)); events.saveAndFlush(event);
        mvc.perform(post("/years/999999/events/" + event.getId() + "/publication")
                .with(user("head@example.com").roles("HEAD")).with(csrf()).param("published", "true"));
        assertFalse(event.isPublicVisible());
    }

    @Test void scheduleIsSavedAndPublicChangesRequireReapproval() {
        var form = new EventFormDto(); form.setPurpose("New event"); form.setDate(now.plusDays(2));
        form.setYearId(year.getId()); form.setMemberIds(List.of());
        var created = eventService.create(form);
        assertEquals(form.getDate(), created.getDate()); assertFalse(created.isPublicVisible());
        var event = events.findById(created.getId()).orElseThrow(); event.setPublicVisible(true); events.saveAndFlush(event);
        form.setDate(now.plusDays(3));
        var updated = eventService.update(event.getId(), form);
        assertEquals(form.getDate(), updated.getDate()); assertFalse(updated.isPublicVisible());
        event.setPublicVisible(true); events.saveAndFlush(event);
        form.setPurpose("Changed public purpose");
        assertFalse(eventService.update(event.getId(), form).isPublicVisible());
    }

    @Test void emptyPageAndZeroChartsRenderAndTitlesAreEscaped() throws Exception {
        mvc.perform(get("/")).andExpect(status().isOk()).andExpect(view().name("index"))
                .andExpect(content().string(containsString("No upcoming public events")))
                .andExpect(content().string(not(containsString("NaN"))))
                .andExpect(content().string(not(containsString("Infinity"))));
        event("<script>alert('unsafe')</script>", now.plusDays(1), true);
        mvc.perform(get("/")).andExpect(content().string(containsString("&lt;script&gt;")))
                .andExpect(content().string(not(containsString("<script>alert"))));
    }

    @Test void publicAndPrivateRoutesAndLoginDestination() throws Exception {
        mvc.perform(get("/")).andExpect(status().isOk());
        mvc.perform(get("/dashboard")).andExpect(status().is3xxRedirection());
        mvc.perform(get("/dashboard").with(user("person@example.com").roles("MEMBER")))
                .andExpect(view().name("dashboard"));
        mvc.perform(get("/").with(user("person@example.com").roles("MEMBER")))
                .andExpect(view().name("index"))
                .andExpect(model().attributeDoesNotExist("navigationYears", "navigationYear"));
        mvc.perform(formLogin("/login").user("username", "test@example.com").password("TestOnly123!"))
                .andExpect(redirectedUrl("/dashboard"));
        mvc.perform(get("/impact").with(user("person@example.com").roles("MEMBER")))
                .andExpect(result -> assertNotEquals("impact", result.getModelAndView() == null ? null : result.getModelAndView().getViewName()));
    }
}
