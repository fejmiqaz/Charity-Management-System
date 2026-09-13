package emd.charitymanagementsystem;

import emd.charitymanagementsystem.Models.*;
import emd.charitymanagementsystem.Repository.*;
import emd.charitymanagementsystem.Service.Implementation.ImpactService;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;
import java.util.HashSet;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:impact-tests", "spring.datasource.username=sa",
        "spring.datasource.password=", "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.jpa.database-platform=org.hibernate.dialect.H2Dialect",
        "app.admin.email=test@example.com", "app.admin.password=TestOnly123!",
        "logging.file.name=target/impact-tests.log"
})
@Transactional
class ImpactPageTests {
    @Autowired WebApplicationContext context;
    @Autowired YearsRepository years;
    @Autowired ProjectRepository projects;
    @Autowired MemberRepository members;
    @Autowired ImpactService impact;
    MockMvc mvc;
    Years year;

    @BeforeEach void setup() {
        mvc = MockMvcBuilders.webAppContextSetup(context).apply(springSecurity()).build();
        year = new Years(); year.setYearValue(2026); years.save(year);
    }

    Project project(String name, ProjectStatus status, boolean published) {
        var project = new Project(); project.setName(name); project.setYear(year);
        project.setStatus(status); project.setPublicImpact(published);
        project.setDescription("PRIVATE-DESCRIPTION"); project.setProjectPrice(987654.32);
        project.setMembers(new HashSet<>());
        return projects.saveAndFlush(project);
    }

    String publication(Project project) {
        return "/years/" + year.getId() + "/projects/" + project.getId() + "/publication";
    }

    Member member() {
        var member = new Member(); member.setName("PRIVATE-MEMBER");
        member.setEmail("private-member@example.com"); member.setPassword("PRIVATE-PASSWORD");
        member.setRole(Role.MEMBER);
        return members.saveAndFlush(member);
    }

    @Test void anonymousPageContainsOnlyExplicitlyPublishedFinishedProjectFields() throws Exception {
        var approved = project("Approved community garden", ProjectStatus.FINISHED, true);
        approved.getMembers().add(member()); projects.saveAndFlush(approved);
        project("PRIVATE-TITLE", ProjectStatus.FINISHED, false);
        for (ProjectStatus status : ProjectStatus.values()) {
            if (status != ProjectStatus.FINISHED) project("HIDDEN-" + status, status, true);
        }
        var result = mvc.perform(get("/impact")).andExpect(status().isOk()).andExpect(view().name("impact"))
                .andExpect(model().attribute("completedProjects", 1))
                .andExpect(model().attribute("impactYears", 1L))
                .andExpect(content().string(containsString("Approved community garden")))
                .andExpect(content().string(containsString("/login")))
                .andExpect(content().string(not(containsString("PRIVATE-"))))
                .andExpect(content().string(not(containsString("HIDDEN-"))))
                .andExpect(content().string(not(containsString("987654"))))
                .andExpect(content().string(not(containsString("appSidebar"))))
                .andReturn();
        assertEquals(2, impact.publicProjects().get(0).getClass().getRecordComponents().length);
        assertFalse(result.getResponse().getContentAsString().contains("/years/"));
        mvc.perform(get("/impact").with(user("member@example.com").roles("MEMBER")))
                .andExpect(status().isOk()).andExpect(content().string(not(containsString("PRIVATE-"))));
    }

    @Test void handlesEmptyDataAndEscapesApprovedTitles() throws Exception {
        mvc.perform(get("/impact")).andExpect(status().isOk())
                .andExpect(content().string(containsString("More to share soon")))
                .andExpect(model().attribute("completedProjects", 0));
        project("<script>alert(1)</script>", ProjectStatus.FINISHED, true);
        mvc.perform(get("/impact")).andExpect(content().string(not(containsString("<script>"))))
                .andExpect(content().string(containsString("&lt;script&gt;")));
    }

    @Test void headCanPublishAndUnpublishAndFormIsHeadOnly() throws Exception {
        var project = project("Ready to share", ProjectStatus.FINISHED, false);
        assertFalse(new Project().isPublicImpact());
        mvc.perform(post(publication(project)).with(user("head@example.com").roles("HEAD"))
                        .with(csrf()).param("published", "true"))
                .andExpect(status().is3xxRedirection());
        assertTrue(projects.findById(project.getId()).orElseThrow().isPublicImpact());
        String details = "/years/" + year.getId() + "/projects/" + project.getId();
        mvc.perform(get(details).with(user("head@example.com").roles("HEAD")))
                .andExpect(view().name("projects/details"))
                .andExpect(content().string(containsString("Save publication setting")));
        mvc.perform(get(details).with(user("member@example.com").roles("MEMBER")))
                .andExpect(content().string(not(containsString("Save publication setting"))));
        mvc.perform(post(publication(project)).with(user("head@example.com").roles("HEAD"))
                .with(csrf())).andExpect(status().is3xxRedirection());
        assertFalse(projects.findById(project.getId()).orElseThrow().isPublicImpact());
    }

    @Test void allNonHeadRolesAndMissingCsrfCannotPublish() throws Exception {
        var project = project("Private", ProjectStatus.FINISHED, false);
        for (Role role : Role.values()) {
            if (role == Role.HEAD) continue;
            mvc.perform(post(publication(project)).with(user("other@example.com").roles(role.name()))
                    .with(csrf()).param("published", "true"));
            assertFalse(projects.findById(project.getId()).orElseThrow().isPublicImpact(), role.name());
        }
        mvc.perform(post(publication(project)).with(user("head@example.com").roles("HEAD"))
                .param("published", "true")).andExpect(redirectedUrl("/access-denied"));
        assertFalse(projects.findById(project.getId()).orElseThrow().isPublicImpact());
    }

    @Test void rejectsUnfinishedAndWrongYearPublication() throws Exception {
        var project = project("Not finished", ProjectStatus.ONGOING, false);
        mvc.perform(post(publication(project)).with(user("head@example.com").roles("HEAD"))
                .with(csrf()).param("published", "true"));
        assertFalse(projects.findById(project.getId()).orElseThrow().isPublicImpact());
        project.setStatus(ProjectStatus.FINISHED); projects.saveAndFlush(project);
        mvc.perform(post("/years/999999/projects/" + project.getId() + "/publication")
                .with(user("head@example.com").roles("HEAD")).with(csrf()).param("published", "true"));
        assertFalse(projects.findById(project.getId()).orElseThrow().isPublicImpact());
    }

    @Test void editingPublicTitleRequiresApprovalAgainAndCannotBindPublicationThroughRegularForm() throws Exception {
        var project = project("Approved title", ProjectStatus.FINISHED, true);
        var member = member();
        mvc.perform(post("/years/" + year.getId() + "/projects/" + project.getId() + "/edit")
                .with(user("subhead@example.com").roles("SUBHEAD")).with(csrf())
                .param("name", "Unreviewed title").param("description", "Private description")
                .param("status", "FINISHED").param("projectPrice", "10")
                .param("memberIds", member.getId().toString())
                .param("publicImpact", "true")).andExpect(status().is3xxRedirection());
        assertEquals("Unreviewed title", projects.findById(project.getId()).orElseThrow().getName());
        assertFalse(projects.findById(project.getId()).orElseThrow().isPublicImpact());
        assertTrue(impact.publicProjects().isEmpty());
    }

    @Test void existingProtectedRoutesRemainPrivate() throws Exception {
        for (String path : new String[]{"/", "/home", "/members", "/memberships", "/profile", "/years",
                "/years/1/projects", "/years/1/donations", "/years/1/budget", "/impact/private"}) {
            mvc.perform(get(path)).andExpect(status().is3xxRedirection());
        }
    }
}
