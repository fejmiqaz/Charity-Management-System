package emd.charitymanagementsystem;

import emd.charitymanagementsystem.Models.*;
import emd.charitymanagementsystem.Repository.*;
import emd.charitymanagementsystem.Service.*;
import emd.charitymanagementsystem.Service.Implementation.BudgetWarningService;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import org.hibernate.SessionFactory;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:dashboard-query-tests", "spring.datasource.username=sa",
        "spring.datasource.password=", "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.jpa.database-platform=org.hibernate.dialect.H2Dialect",
        "app.admin.email=test@example.com", "app.admin.password=TestOnly123!",
        "logging.file.name=target/dashboard-query-tests.log",
        "spring.jpa.properties.hibernate.generate_statistics=true",
        "logging.level.org.hibernate.stat=OFF",
        "logging.level.org.hibernate.engine.internal.StatisticalLoggingSessionEventListener=OFF"
})
@Transactional
class DashboardQueryTests {
    @Autowired WebApplicationContext context;
    @Autowired EntityManager em;
    @Autowired EntityManagerFactory emf;
    @Autowired YearsRepository years;
    @Autowired DonationService donations;
    @Autowired ProjectService projects;
    @Autowired BudgetService budgets;
    @Autowired BudgetWarningService warnings;
    MockMvc mvc;

    @BeforeEach void setup() {
        mvc = MockMvcBuilders.webAppContextSetup(context).apply(springSecurity()).build();
    }

    void seedYear(int value) {
        var year = new Years(); year.setYearValue(value); em.persist(year);
        var budget = new Budget(); budget.setYear(year); budget.setBudgetAmount(100.0); em.persist(budget);
        // Multiple donations must not multiply the project sum in the yearly aggregate.
        for (Double amount : new Double[]{10.0, null}) {
            var donation = new Donation(); donation.setYear(year); donation.setDonationAmount(amount); em.persist(donation);
        }
        for (Double amount : new Double[]{30.0, 50.0, null}) {
            var project = new Project(); project.setYear(year); project.setProjectPrice(amount);
            project.setStatus(ProjectStatus.CANCELLED); em.persist(project);
        }
    }

    @Test void emptyTotalsAndWarningsStayZero() throws Exception {
        assertEquals(0.0, donations.getTotalDonations());
        assertEquals(0.0, projects.getTotalProjectCost());
        assertEquals(0.0, budgets.getTotalBudgetAmouunt());
        assertTrue(warnings.warnings().isEmpty());
        mvc.perform(get("/dashboard").with(user("test@example.com").roles("HEAD")))
                .andExpect(view().name("dashboard")).andExpect(model().attribute("remainingBudget", 0.0));
        mvc.perform(get("/dashboard")).andExpect(status().is3xxRedirection());
    }

    @Test void warningGroupsPreserveYearsNullsAndMissingBudgets() {
        seedYear(2025); seedYear(2026);
        var noBudget = new Years(); noBudget.setYearValue(2027); em.persist(noBudget);
        var project = new Project(); project.setYear(noBudget); project.setProjectPrice(12.0); em.persist(project);
        var nullBudget = new Budget(); nullBudget.setBudgetAmount(null); em.persist(nullBudget);
        em.flush(); em.clear();
        assertEquals(20.0, donations.getTotalDonations());
        assertEquals(172.0, projects.getTotalProjectCost());
        assertEquals(200.0, budgets.getTotalBudgetAmouunt());
        var usage = years.dashboardBudgetUsage();
        assertEquals(3, usage.size()); assertEquals(2027, usage.get(0).getYearValue());
        assertNull(usage.get(0).getBudgetAmount()); assertEquals(12.0, usage.get(0).getSpent());
        assertEquals(80.0, usage.get(1).getSpent()); assertEquals(80.0, usage.get(2).getSpent());
        var alerts = warnings.warnings();
        assertEquals("unset", alerts.get(0).level()); assertEquals("warning", alerts.get(1).level());
    }

    @Test void fullDashboardQueryCountDoesNotGrowWithYearsAndLoadsNoEntities() throws Exception {
        seedYear(2025); em.flush(); em.clear();
        var stats = emf.unwrap(SessionFactory.class).getStatistics();
        stats.clear();
        mvc.perform(get("/dashboard").with(user("test@example.com").roles("HEAD")))
                .andExpect(view().name("dashboard")).andExpect(model().attribute("remainingBudget", 30.0));
        long oneYearQueries = stats.getPrepareStatementCount();
        assertEquals(0, stats.getEntityLoadCount()); assertEquals(0, stats.getCollectionLoadCount());
        assertEquals(10, oneYearQueries);
        for (int year = 2026; year <= 2032; year++) seedYear(year);
        em.flush(); em.clear(); stats.clear();
        mvc.perform(get("/home").with(user("test@example.com").roles("HEAD")))
                .andExpect(view().name("dashboard")).andExpect(model().attribute("remainingBudget", 240.0))
                .andExpect(model().attribute("totalYears", 8L));
        assertEquals(oneYearQueries, stats.getPrepareStatementCount());
        assertEquals(0, stats.getEntityLoadCount()); assertEquals(0, stats.getCollectionLoadCount());
    }
}
