package emd.charitymanagementsystem;

import emd.charitymanagementsystem.Models.*;
import emd.charitymanagementsystem.Repository.*;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;
import java.math.BigDecimal;
import java.time.*;
import java.util.*;
import static org.hamcrest.Matchers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:year-details-tests", "spring.datasource.username=sa",
        "spring.datasource.password=", "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.jpa.database-platform=org.hibernate.dialect.H2Dialect",
        "app.admin.email=test@example.com", "app.admin.password=TestOnly123!",
        "logging.file.name=target/year-details-tests.log"
})
@Transactional
class YearDetailsTests {
    @Autowired WebApplicationContext context;
    @Autowired YearsRepository years;
    @Autowired MemberRepository members;
    @Autowired ProjectRepository projects;
    @Autowired EventRepository events;
    @Autowired DonationRepository donations;
    @Autowired BudgetRepository budgets;
    @Autowired MembershipPaymentRepository payments;
    MockMvc mvc;
    Years year;

    @BeforeEach void setup() {
        mvc = MockMvcBuilders.webAppContextSetup(context).apply(springSecurity()).build();
        year = new Years(); year.setYearValue(2026); years.saveAndFlush(year);
    }

    void populate(Years selected, String prefix) {
        var member = new Member(); member.setName(prefix + "Member"); member.setSurname("Person");
        member.setEmail(prefix + "@example.com"); member.setPassword("NEVER-EXPOSE-PASSWORD");
        member.setCountry("Macedonia"); member.setCity("Skopje"); member.setPhone("123456");
        member.setRole(Role.MEMBER); member.setYear(selected); members.saveAndFlush(member);
        var project = new Project(); project.setName(prefix + "Project"); project.setDescription(prefix + "Description");
        project.setYear(selected); project.setStatus(ProjectStatus.ONGOING); project.setProjectPrice(50.0);
        project.setDateCreated(LocalDate.of(2026, 2, 1)); project.setMembers(new HashSet<>(Set.of(member)));
        projects.saveAndFlush(project);
        var event = new Event(); event.setPurpose(prefix + "Event"); event.setYear(selected);
        event.setDate(LocalDateTime.of(2026, 3, 2, 14, 30)); event.setMembers(new ArrayList<>(List.of(member)));
        events.saveAndFlush(event);
        var donation = new Donation(); donation.setYear(selected); donation.setDonationAmount(20.0);
        donation.setMembers(new HashSet<>(Set.of(member))); donations.saveAndFlush(donation);
        var budget = new Budget(); budget.setYear(selected); budget.setDescription(prefix + "Budget notes");
        budget.setBudgetAmount(100.0); budget.setDonations(new ArrayList<>(List.of(donation)));
        budget.setMembers(new ArrayList<>(List.of(member))); budgets.saveAndFlush(budget); selected.setBudget(budget);
        var payment = new MembershipPayment(); payment.setMemberId(member.getId());
        payment.setMemberName(prefix + "Receipt holder"); payment.setMembershipYear(selected.getYearValue());
        payment.setAmount(new BigDecimal("10.00")); payment.setPaidOn(LocalDate.of(2026, 1, 10));
        payment.setRecordedAt(Instant.now()); payment.setRecordedBy("INTERNAL-RECORDER");
        payment.setActiveKey(member.getId() + ":" + selected.getYearValue()); payments.saveAndFlush(payment);
    }

    @Test void showsAllSelectedYearRecordsAndPreservesFinancialCalculation() throws Exception {
        populate(year, "Selected");
        var other = new Years(); other.setYearValue(2025); years.saveAndFlush(other); populate(other, "OtherSecret");
        mvc.perform(get("/years/" + year.getId()).with(user("head@example.com").roles("HEAD")))
                .andExpect(status().isOk()).andExpect(view().name("years/details"))
                .andExpect(content().string(containsString("SelectedProject")))
                .andExpect(content().string(containsString("SelectedDescription")))
                .andExpect(content().string(containsString("SelectedEvent")))
                .andExpect(content().string(containsString("SelectedBudget notes")))
                .andExpect(content().string(containsString("SelectedMember")))
                .andExpect(content().string(containsString("SelectedReceipt holder")))
                .andExpect(content().string(containsString("02 Mar 2026, 14:30")))
                .andExpect(content().string(not(containsString("OtherSecret"))))
                .andExpect(content().string(not(containsString("NEVER-EXPOSE-PASSWORD"))))
                .andExpect(content().string(not(containsString("INTERNAL-RECORDER"))))
                .andExpect(model().attribute("remainingBudget", 80.0))
                .andExpect(model().attribute("yearMembers", hasSize(1)))
                .andExpect(model().attribute("donatingMembers", hasSize(1)));
    }

    @Test void memberSeesRecordsButNotManagerReceiptHistory() throws Exception {
        populate(year, "Selected");
        mvc.perform(get("/years/" + year.getId()).with(user("member@example.com").roles("MEMBER")))
                .andExpect(view().name("years/details"))
                .andExpect(content().string(containsString("SelectedProject")))
                .andExpect(content().string(not(containsString("SelectedReceipt holder"))))
                .andExpect(model().attributeDoesNotExist("membershipPayments"));
        mvc.perform(get("/years/" + year.getId())).andExpect(status().is3xxRedirection());
        mvc.perform(get("/years/" + year.getId()).with(user("donor@example.com").roles("DONOR")))
                .andExpect(redirectedUrl("/access-denied"));
    }

    @Test void emptyYearRendersUsefulEmptyStates() throws Exception {
        mvc.perform(get("/years/" + year.getId()).with(user("head@example.com").roles("HEAD")))
                .andExpect(view().name("years/details"))
                .andExpect(content().string(containsString("There are no projects for this year.")))
                .andExpect(content().string(containsString("There are no events for this year.")))
                .andExpect(content().string(containsString("No members are assigned to this year.")))
                .andExpect(content().string(matchesPattern("(?s).*No membership payments recorded for this\\s+year\\..*")));
    }
}
