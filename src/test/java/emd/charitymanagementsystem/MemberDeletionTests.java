package emd.charitymanagementsystem;

import emd.charitymanagementsystem.Models.*;
import emd.charitymanagementsystem.Repository.*;
import emd.charitymanagementsystem.Service.MemberService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.transaction.PlatformTransactionManager;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(properties={"spring.datasource.url=jdbc:h2:mem:delete-tests", "spring.datasource.username=sa", "spring.datasource.password=", "spring.datasource.driver-class-name=org.h2.Driver", "spring.jpa.database-platform=org.hibernate.dialect.H2Dialect", "app.admin.email=test@example.com", "app.admin.password=TestOnly123!", "logging.file.name=target/delete-tests.log"})
class MemberDeletionTests {
 @Autowired MemberService service;
 @Autowired MemberRepository members;
 @Autowired UserAccountRepository accounts;
 @Autowired YearsRepository years;
 @Autowired BudgetRepository budgets;
 @Autowired DonationRepository donations;
 @Autowired EventRepository events;
 @Autowired ProjectRepository projects;
 @Autowired PlatformTransactionManager transactions;
 @Autowired org.springframework.web.context.WebApplicationContext context;

 @Test void onlyHeadCanDeleteThroughMemberEndpoint() throws Exception {
  Member member = members.save(Member.builder().name("Endpoint").surname("Test").email("endpoint@example.com").password("encoded").role(Role.MEMBER).build());
  var mvc = org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup(context)
      .apply(org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity()).build();
  String path = "/members/" + member.getId() + "/delete";
  mvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post(path)
      .with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user("member@example.com").roles("MEMBER"))
      .with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf()));
  assertTrue(members.existsById(member.getId()));
  mvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post(path)
      .with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user("test@example.com").roles("HEAD"))
      .with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf()))
      .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl("/members"));
  assertFalse(members.existsById(member.getId()));
 }

 @Test void deletesMemberLinkedToAccountAndYearAfterCommit() {
  Long id = new TransactionTemplate(transactions).execute(status -> {
   Years year = new Years(); year.setYearValue(2026);years.save(year);
   UserAccount account = accounts.save(UserAccount.builder().name("Delete Test").email("delete@example.com").password("encoded").role(Role.MEMBER).enabled(true).build());
   Member member = Member.builder().name("Delete").surname("Test").email(account.getEmail()).password("encoded").role(Role.MEMBER).year(year).userAccount(account).build();
   members.save(member);account.setMember(member);year.getMembers().add(member);
   return member.getId();
  });
  service.delete(id);
  assertFalse(members.existsById(id), "Member must remain deleted after transaction commits");
 }

 @Test void removesAssociationsWithoutDeletingOtherRecords() {
  Long id = new TransactionTemplate(transactions).execute(status -> {
   Member member = members.save(Member.builder().name("Linked").surname("Member").email("linked@example.com").password("encoded").role(Role.MEMBER).build());
   Budget budget = new Budget();budget.setMembers(new ArrayList<>(List.of(member)));budgets.save(budget);
   Donation donation = new Donation();donation.setMembers(new HashSet<>(Set.of(member)));donations.save(donation);
   Event event = new Event();event.setMembers(new ArrayList<>(List.of(member)));events.save(event);
   Project project = new Project();project.setMembers(new HashSet<>(Set.of(member)));projects.save(project);
   return member.getId();
  });
  long budgetCount=budgets.count(), donationCount=donations.count(), eventCount=events.count(), projectCount=projects.count();
  service.delete(id);
  assertFalse(members.existsById(id));
  assertEquals(budgetCount,budgets.count());assertEquals(donationCount,donations.count());assertEquals(eventCount,events.count());assertEquals(projectCount,projects.count());
 }
}
