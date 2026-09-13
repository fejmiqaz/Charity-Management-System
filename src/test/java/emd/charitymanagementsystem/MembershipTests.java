package emd.charitymanagementsystem;

import emd.charitymanagementsystem.Models.*;
import emd.charitymanagementsystem.Repository.*;
import emd.charitymanagementsystem.Service.Implementation.MembershipService;
import emd.charitymanagementsystem.Service.MemberService;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import java.math.BigDecimal;
import java.time.LocalDate;
import static org.junit.jupiter.api.Assertions.*;
import static org.hamcrest.Matchers.*;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(properties={"spring.datasource.url=jdbc:h2:mem:membership-tests", "spring.datasource.username=sa", "spring.datasource.password=", "spring.datasource.driver-class-name=org.h2.Driver", "spring.jpa.database-platform=org.hibernate.dialect.H2Dialect", "app.admin.email=admin@example.com", "app.admin.password=TestOnly123!", "logging.file.name=target/membership-tests.log"})
@WithMockUser(username="admin@example.com",roles="HEAD")
class MembershipTests {
    @Autowired MembershipService service;
    @Autowired MembershipPaymentRepository payments;
    @Autowired MembershipFeeRepository fees;
    @Autowired MemberRepository members;
    @Autowired MemberService memberService;
    @Autowired UserAccountRepository accounts;
    @Autowired YearsRepository years;
    @Autowired WebApplicationContext context;
    Long memberId;
    MockMvc mvc;
    @BeforeEach void setup() {
        payments.deleteAll();fees.deleteAll();
        var member = Member.builder().name("Membership").surname("Test").email("dues"+System.nanoTime()+"@example.com").password("encoded").role(Role.MEMBER).build();
        memberId=members.save(member).getId();
        mvc=MockMvcBuilders.webAppContextSetup(context).apply(springSecurity()).build();
    }
    @Test void defaultsToTenAndKeepsYearlyPaymentsIndependent() {
        assertEquals(0,new BigDecimal("10.00").compareTo(service.fee(2025)));
        service.record(memberId,2025,LocalDate.of(2025,3,1),"admin@example.com");
        assertTrue(service.paidMembers(2025).containsKey(memberId));assertFalse(service.paidMembers(2026).containsKey(memberId));
        service.record(memberId,2026,LocalDate.of(2026,1,1),"admin@example.com");
        assertEquals(2,service.history(memberId).size());assertEquals(0,new BigDecimal("20.00").compareTo(service.total()));
        assertEquals(Role.MEMBER,members.findById(memberId).orElseThrow().getRole());
    }
    @Test void feeChangesApplyOnlyToNewReceiptsAndRejectDuplicatePayments() {
        var paid=service.record(memberId,2026,LocalDate.of(2026,1,1),"admin@example.com");
        service.setFee(2026,new BigDecimal("15.00"));
        assertEquals(0,new BigDecimal("10.00").compareTo(payments.findById(paid.getId()).orElseThrow().getAmount()));
        assertThrows(IllegalArgumentException.class,()->service.record(memberId,2026,LocalDate.of(2026,1,1),"admin@example.com"));
        assertEquals(1,payments.count());assertEquals(0,new BigDecimal("10.00").compareTo(service.fee(2025)));
    }
    @Test void voidPreservesHistoryAndAllowsCorrectedPayment() {
        var paid=service.record(memberId,2026,LocalDate.of(2026,1,1),"admin@example.com");
        service.voidPayment(paid.getId(),"Wrong date entered","admin@example.com");
        assertFalse(service.paidMembers(2026).containsKey(memberId));assertEquals(0,service.total().compareTo(BigDecimal.ZERO));
        service.record(memberId,2026,LocalDate.of(2026,2,1),"admin@example.com");
        assertEquals(2,service.history(memberId).size());assertEquals(0,new BigDecimal("10.00").compareTo(service.total()));
        assertEquals("Wrong date entered",payments.findById(paid.getId()).orElseThrow().getVoidReason());
    }
    @Test void receiptsSurviveMemberDeletion() {
        service.record(memberId,2025,LocalDate.of(2025,1,1),"admin@example.com");
        memberService.delete(memberId);
        assertEquals(1,service.history(memberId).size());assertEquals("Membership Test",service.history(memberId).get(0).getMemberName());
        assertEquals(0,new BigDecimal("10.00").compareTo(service.total()));
    }
    @Test @WithMockUser(roles="MEMBER") void regularMembersCannotRecordPaymentsOrChangeFees() {
        assertThrows(AccessDeniedException.class,()->service.record(memberId,2026,LocalDate.of(2026,1,1),"member"));
        assertThrows(AccessDeniedException.class,()->service.setFee(2026,new BigDecimal("1")));
        assertEquals(0,payments.count());
    }
    @Test void validatesFeesYearsDatesAndCorrectionReasons() {
        assertThrows(IllegalArgumentException.class,()->service.setFee(2026,new BigDecimal("-1")));
        assertThrows(IllegalArgumentException.class,()->service.setFee(2026,new BigDecimal("1.001")));
        assertThrows(IllegalArgumentException.class,()->service.setFee(9999,BigDecimal.TEN));
        assertThrows(IllegalArgumentException.class,()->service.record(memberId,2026,LocalDate.now().plusYears(1),"admin"));
        assertThrows(IllegalArgumentException.class,()->service.voidPayment(1L," ","admin"));
    }
    @Test void profileShowsOnlyItsOwnMembershipHistory() throws Exception {
        var account=accounts.save(UserAccount.builder().name("Own Profile").email("membership-profile@example.com").password("encoded").role(Role.MEMBER).enabled(true).build());
        var member=members.findById(memberId).orElseThrow();member.setUserAccount(account);members.save(member);
        service.record(memberId,2025,LocalDate.of(2025,1,1),"admin");
        mvc.perform(get("/profile").with(user(account.getEmail()).roles("MEMBER")))
            .andExpect(status().isOk()).andExpect(content().string(containsString("My membership")))
            .andExpect(content().string(containsString("2025-01-01")));
        mvc.perform(get("/memberships").with(user(account.getEmail()).roles("MEMBER")))
            .andExpect(content().string(not(containsString("Payment ledger"))));
    }
    @Test void completeYearReportIncludesMembershipIncome() throws Exception {
        var year=new Years();year.setYearValue(2030);years.save(year);
        service.record(memberId,2030,LocalDate.of(2026,1,1),"admin");
        byte[] pdf=mvc.perform(get("/pdf/years/"+year.getId())).andExpect(status().isOk()).andReturn().getResponse().getContentAsByteArray();
        try(var reader=new com.lowagie.text.pdf.PdfReader(pdf)) {
            String text=new com.lowagie.text.pdf.parser.PdfTextExtractor(reader).getTextFromPage(1);
            assertTrue(text.contains("Membership income"));assertTrue(text.contains("10.00 EUR"));
        }
    }
    @Test void concurrentSubmissionsCreateOnlyOneReceipt() throws Exception {
        var pool=java.util.concurrent.Executors.newFixedThreadPool(2);
        var ready=new java.util.concurrent.CountDownLatch(2);
        java.util.concurrent.Callable<Boolean> request=()->{
            var context=org.springframework.security.core.context.SecurityContextHolder.createEmptyContext();
            context.setAuthentication(new org.springframework.security.authentication.UsernamePasswordAuthenticationToken("admin","",java.util.List.of(new org.springframework.security.core.authority.SimpleGrantedAuthority("ROLE_HEAD"))));
            org.springframework.security.core.context.SecurityContextHolder.setContext(context);
            try {
                ready.countDown();ready.await(5,java.util.concurrent.TimeUnit.SECONDS);
                service.record(memberId,2026,LocalDate.of(2026,1,1),"admin");return true;
            } catch(IllegalArgumentException | org.springframework.dao.DataIntegrityViolationException expected) {return false;}
            finally {org.springframework.security.core.context.SecurityContextHolder.clearContext();}
        };
        try {
            var first=pool.submit(request);var second=pool.submit(request);
            assertNotEquals(first.get(10,java.util.concurrent.TimeUnit.SECONDS),second.get(10,java.util.concurrent.TimeUnit.SECONDS));
            assertEquals(1,payments.count());
        } finally {pool.shutdownNow();}
    }
    @Test void rendersLedgerMemberYearStatusesAndAcceptsTreasurerPayment() throws Exception {
        mvc.perform(get("/memberships").param("year","2025")).andExpect(status().isOk()).andExpect(content().string(containsString("Record a received payment")));
        mvc.perform(post("/memberships/record").with(user("treasurer").roles("TREASURER")).with(csrf()).param("year","2025").param("memberId",memberId.toString()).param("paidOn","2025-01-01"))
            .andExpect(redirectedUrl("/memberships?year=2025"));
        mvc.perform(get("/members").param("membershipYear","2025").param("search",members.findById(memberId).orElseThrow().getEmail()).with(user("treasurer").roles("TREASURER")))
            .andExpect(status().isOk()).andExpect(content().string(containsString("Paid membership")));
        mvc.perform(get("/memberships").param("memberId",memberId.toString())).andExpect(status().isOk()).andExpect(content().string(containsString("2025")));
        mvc.perform(get("/members").param("membershipYear","2026")).andExpect(status().isOk()).andExpect(content().string(not(containsString("Paid membership</span>"))));
    }
}
