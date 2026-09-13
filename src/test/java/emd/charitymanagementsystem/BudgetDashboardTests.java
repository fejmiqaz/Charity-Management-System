package emd.charitymanagementsystem;

import emd.charitymanagementsystem.Models.*;
import emd.charitymanagementsystem.Repository.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import java.util.HashSet;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:budget-dashboard-tests", "spring.datasource.username=sa",
        "spring.datasource.password=", "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.jpa.database-platform=org.hibernate.dialect.H2Dialect",
        "app.admin.email=test@example.com", "app.admin.password=TestOnly123!",
        "logging.file.name=target/budget-dashboard-tests.log"
})
class BudgetDashboardTests {
    @Autowired WebApplicationContext context;
    @Autowired YearsRepository years;
    @Autowired BudgetRepository budgets;
    @Autowired ProjectRepository projects;

    @Test void rendersWarningsAndUnsetBudgetsWithAmounts() throws Exception {
        for (int i = 0; i < 3; i++) {
            var year = new Years(); year.setYearValue(2024 + i); years.save(year);
            if (i < 2) {
                var budget = new Budget(); budget.setYear(year); budget.setBudgetAmount(100.0); budgets.save(budget);
            }
            var project = new Project(); project.setName("Budget fixture"); project.setYear(year);
            project.setProjectPrice(i == 0 ? 80.0 : 120.0); project.setMembers(new HashSet<>());
            projects.save(project);
        }
        MockMvcBuilders.webAppContextSetup(context).apply(springSecurity()).build()
                .perform(get("/").with(user("test@example.com").roles("HEAD")))
                .andExpect(status().isOk()).andExpect(view().name("dashboard"))
                .andExpect(content().string(containsString("80.00% used")))
                .andExpect(content().string(containsString("120.00% used")))
                .andExpect(content().string(containsString("20.00 EUR remaining")))
                .andExpect(content().string(containsString("20.00 EUR over budget")))
                .andExpect(content().string(containsString("No positive budget set")));
    }
}
