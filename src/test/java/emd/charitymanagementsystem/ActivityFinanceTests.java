package emd.charitymanagementsystem;

import emd.charitymanagementsystem.Models.*;
import emd.charitymanagementsystem.Models.Currency;
import emd.charitymanagementsystem.Repository.*;
import emd.charitymanagementsystem.Service.Implementation.ActivityFinanceService;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.*;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import org.springframework.test.web.servlet.*;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.Matchers.containsString;

@SpringBootTest(properties={"spring.datasource.url=jdbc:h2:mem:activity-finance-tests","spring.datasource.username=sa",
        "spring.datasource.password=","spring.datasource.driver-class-name=org.h2.Driver",
        "spring.jpa.database-platform=org.hibernate.dialect.H2Dialect","app.admin.email=test@example.com",
        "app.admin.password=TestOnly123!","logging.file.name=target/activity-finance-tests.log"})
@Transactional @WithMockUser(username="head@example.com",roles="HEAD")
class ActivityFinanceTests {
    @Autowired ActivityFinanceService service; @Autowired YearsRepository years; @Autowired EventRepository events;
    @Autowired ProjectRepository projects; @Autowired MemberRepository members;
    @Autowired WebApplicationContext context; MockMvc mvc; Years year; Member member;

    @BeforeEach void setup(){ mvc=MockMvcBuilders.webAppContextSetup(context).apply(springSecurity()).build(); year=new Years();year.setYearValue(2028);years.saveAndFlush(year);
        member=new Member();member.setName("Worker");member.setSurname("Member");member.setEmail("worker@example.com");
        member.setPassword("encoded");member.setRole(Role.MEMBER);members.saveAndFlush(member); }

    @Test void taskEventTracksAssignmentsCostAndPayments(){
        Event event=new Event();event.setPurpose("Fundraiser");event.setDate(LocalDateTime.now().plusDays(2));
        event.setEventType(EventType.TASK_BASED);event.setYear(year);event.setMembers(new ArrayList<>());events.saveAndFlush(event);
        service.addTask(year.getId(),event.getId(),"Build stand","Materials",new BigDecimal("125.50"),List.of(member.getId()));
        EventTask task=service.tasks(event.getId()).get(0);assertEquals(new BigDecimal("125.50"),task.getPrice());
        assertEquals(member.getId(),task.getMembers().iterator().next().getId());
        service.addTaskPayment(year.getId(),event.getId(),task.getId(),member.getId(),new BigDecimal("40.00"),LocalDate.now(),"Advance","head");
        assertEquals(new BigDecimal("40.00"),service.tasks(event.getId()).get(0).getTotalPaid());
    }

    @Test void revenueProjectAggregatesMonthlyIncome(){
        Project project=new Project();project.setName("LED advertising");project.setDescription("Monthly advertising");
        project.setProjectType(ProjectType.REVENUE);project.setStatus(ProjectStatus.ONGOING);project.setYear(year);
        project.setProjectPrice(1000.0);project.setMembers(new HashSet<>());projects.saveAndFlush(project);
        service.addRevenue(year.getId(),project.getId(),YearMonth.of(2028,2),"Example Company",new BigDecimal("250.00"),"February ad","head");
        service.addRevenue(year.getId(),project.getId(),YearMonth.of(2028,3),"Second Company",new BigDecimal("175.50"),null,"head");
        assertEquals(new BigDecimal("425.50"),service.projectRevenue(project.getId()));
        assertEquals(2,service.revenues(project.getId()).size());
    }

    @Test void currencyTotalsKeepIndependentPaymentsAndIncome() {
        Event event = new Event(); event.setPurpose("Fundraiser"); event.setDate(LocalDateTime.now().plusDays(2));
        event.setEventType(EventType.TASK_BASED); event.setYear(year); event.setMembers(new ArrayList<>()); events.saveAndFlush(event);
        service.addTask(year.getId(), event.getId(), "Build stand", null, BigDecimal.ZERO, List.of(member.getId()));
        EventTask task = service.tasks(event.getId()).get(0);
        service.addTaskPayment(year.getId(), event.getId(), task.getId(), member.getId(), new BigDecimal("1000"), Currency.MKD, LocalDate.now(), null, "head");
        service.addTaskPayment(year.getId(), event.getId(), task.getId(), member.getId(), new BigDecimal("500"), Currency.EUR, LocalDate.now(), null, "head");
        assertEquals(new BigDecimal("1000.00"), task.getPaymentTotals().get(Currency.MKD));
        assertEquals(new BigDecimal("500.00"), task.getPaymentTotals().get(Currency.EUR));

        Project project = new Project(); project.setName("Income"); project.setProjectType(ProjectType.REVENUE);
        project.setYear(year); project.setStatus(ProjectStatus.ONGOING); project.setMembers(new HashSet<>()); projects.saveAndFlush(project);
        service.addRevenue(year.getId(), project.getId(), YearMonth.of(2028, 2), "Customer", new BigDecimal("80"), Currency.CHF, null, "head");
        assertEquals(new BigDecimal("80.00"), service.yearRevenueTotals(year.getId()).get(Currency.CHF));
        assertEquals(new BigDecimal("0.00"), service.yearRevenueTotals(year.getId()).get(Currency.EUR));
    }

    @Test void rejectsTasksOnNormalEventsAndRevenueOnStandardProjects(){
        Event event=new Event();event.setPurpose("Meeting");event.setDate(LocalDateTime.now().plusDays(1));event.setYear(year);
        event.setEventType(EventType.NORMAL);event.setMembers(new ArrayList<>());events.saveAndFlush(event);
        assertThrows(IllegalArgumentException.class,()->service.addTask(year.getId(),event.getId(),"Not allowed",null,BigDecimal.ZERO,List.of()));
        Project project=new Project();project.setName("House");project.setDescription("House project");project.setProjectType(ProjectType.STANDARD);
        project.setStatus(ProjectStatus.ONGOING);project.setYear(year);project.setProjectPrice(1.0);project.setMembers(new HashSet<>());projects.saveAndFlush(project);
        assertThrows(IllegalArgumentException.class,()->service.addRevenue(year.getId(),project.getId(),YearMonth.now(),"Payer",BigDecimal.ONE,null,"head"));
    }

    @Test void rendersTaskAndRevenueWorkspaces() throws Exception {
        Event event=new Event();event.setPurpose("Task event");event.setDate(LocalDateTime.now().plusDays(1));event.setYear(year);
        event.setEventType(EventType.TASK_BASED);event.setMembers(new ArrayList<>());events.saveAndFlush(event);
        Project project=new Project();project.setName("Income project");project.setDescription("Revenue description");project.setProjectType(ProjectType.REVENUE);
        project.setStatus(ProjectStatus.ONGOING);project.setYear(year);project.setProjectPrice(1.0);project.setMembers(new HashSet<>());projects.saveAndFlush(project);
        mvc.perform(get("/years/"+year.getId()+"/events/"+event.getId()).with(user("head@example.com").roles("HEAD")))
                .andExpect(status().isOk()).andExpect(content().string(containsString("Event tasks")));
        mvc.perform(get("/years/"+year.getId()+"/projects/"+project.getId()).with(user("head@example.com").roles("HEAD")))
                .andExpect(status().isOk()).andExpect(content().string(containsString("Monthly project income")));
    }
}
