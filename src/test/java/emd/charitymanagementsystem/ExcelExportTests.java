package emd.charitymanagementsystem;

import emd.charitymanagementsystem.Models.*;
import emd.charitymanagementsystem.Repository.*;
import emd.charitymanagementsystem.Service.Implementation.ExcelExportService;
import emd.charitymanagementsystem.DTO.member.MemberResponseDto;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import java.io.ByteArrayInputStream;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:excel-tests", "spring.datasource.username=sa",
        "spring.datasource.password=", "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.jpa.database-platform=org.hibernate.dialect.H2Dialect",
        "app.admin.email=test@example.com", "app.admin.password=TestOnly123!",
        "logging.file.name=target/excel-tests.log"
})
class ExcelExportTests {
    @Autowired WebApplicationContext context;
    @Autowired MemberRepository members;
    @Autowired YearsRepository years;
    @Autowired DonationRepository donations;
    @Autowired ExcelExportService excel;
    @Autowired ProjectRepository projects;
    @Autowired EventRepository events;
    @Autowired EventTaskRepository tasks;
    @Autowired TaskPaymentRepository taskPayments;
    @Autowired ProjectRevenueRepository revenues;
    @Autowired MembershipPaymentRepository membershipPayments;
    MockMvc mvc;

    @BeforeEach void setup() {
        mvc = MockMvcBuilders.webAppContextSetup(context).apply(springSecurity()).build();
    }

    @Test void exportsAllMatchingMembersBeyondPaginationWithoutAccountFields() throws Exception {
        String marker = "Export" + System.nanoTime();
        for (int i = 0; i < 14; i++) {
            Member member = new Member();
            member.setName(marker); member.setSurname("Person");
            member.setEmail(marker + i + "@example.com"); member.setPassword("SECRET-PASSWORD");
            member.setCountry(i == 13 ? "Other" : "Macedonia"); member.setCity("Skopje");
            member.setRole(Role.MEMBER); members.save(member);
        }
        byte[] bytes = mvc.perform(get("/members/export.xlsx").param("search", marker)
                        .param("country", "mac").param("city", "skop").param("role", "MEMBER")
                        .param("membershipYear", "2026").param("pageSize", "2").param("pageNum", "2")
                        .with(user("member@example.com").roles("MEMBER")))
                .andExpect(status().isOk()).andExpect(header().string("Cache-Control", "no-store"))
                .andExpect(content().contentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .andReturn().getResponse().getContentAsByteArray();
        try (var book = new XSSFWorkbook(new ByteArrayInputStream(bytes))) {
            var sheet = book.getSheetAt(0);
            assertEquals(13, sheet.getLastRowNum());
            assertEquals(11, sheet.getRow(0).getLastCellNum());
            for (Row row : sheet) for (Cell cell : row) {
                assertFalse(cell.toString().contains("SECRET-PASSWORD"));
                assertFalse(cell.toString().toLowerCase().contains("account"));
            }
            assertEquals(2026, sheet.getRow(1).getCell(6).getNumericCellValue());
            assertEquals("Unpaid", sheet.getRow(1).getCell(7).getStringCellValue());
        }
    }

    @Test void exportsOnlySelectedDonationYearWithNumericAmounts() throws Exception {
        Years year = new Years(); year.setYearValue(2025); years.save(year);
        Years other = new Years(); other.setYearValue(2024); years.save(other);
        for (Years value : List.of(year, other)) {
            Donation d = new Donation(); d.setYear(value); d.setDonationAmount(1234.56); donations.save(d);
        }
        var result = mvc.perform(get("/years/" + year.getId() + "/donations/export.xlsx")
                .with(user("treasurer@example.com").roles("TREASURER"))).andExpect(status().isOk()).andReturn();
        try (var book = new XSSFWorkbook(new ByteArrayInputStream(result.getResponse().getContentAsByteArray()))) {
            var sheet = book.getSheetAt(0);
            assertEquals(1, sheet.getLastRowNum());
            assertEquals(2025, sheet.getRow(1).getCell(0).getNumericCellValue());
            assertEquals(1234.56, sheet.getRow(1).getCell(1).getNumericCellValue());
            assertEquals("EUR", sheet.getRow(1).getCell(2).getStringCellValue());
        }
    }

    @Test void writesLiteralTextAndTypedPaymentDatesAndHandlesEmptyExports() throws Exception {
        var member = new MemberResponseDto(); member.setId(1L); member.setName("=1+1");
        var payment = new MembershipPayment(); payment.setAmount(new BigDecimal("10.00"));
        payment.setPaidOn(LocalDate.of(2026, 2, 3));
        try (var book = new XSSFWorkbook(new ByteArrayInputStream(excel.members(List.of(member), 2026, Map.of(1L, payment))))) {
            var row = book.getSheetAt(0).getRow(1);
            assertEquals(CellType.STRING, row.getCell(0).getCellType());
            assertEquals("=1+1", row.getCell(0).getStringCellValue());
            assertTrue(DateUtil.isCellDateFormatted(row.getCell(10)));
            assertEquals(payment.getPaidOn(), row.getCell(10).getLocalDateTimeCellValue().toLocalDate());
        }
        try (var book = new XSSFWorkbook(new ByteArrayInputStream(excel.members(List.of(), 2026, Map.of())))) {
            assertEquals(0, book.getSheetAt(0).getLastRowNum());
        }
    }

    @Test void exportPermissionsMatchListPermissions() throws Exception {
        for (String path : List.of("/members/export.xlsx", "/years/1/donations/export.xlsx")) {
            mvc.perform(get(path)).andExpect(status().is3xxRedirection());
            var response = mvc.perform(get(path).with(user("donor@example.com").roles("DONOR")))
                    .andReturn().getResponse();
            assertNotEquals("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", response.getContentType());
            assertTrue(response.getStatus() == 403 || response.getRedirectedUrl() != null
                    || response.getContentAsString().contains("permission to perform this action"));
        }
    }

    private Years reportYear(int value) {
        var year = new Years();
        year.setYearValue(value);
        return years.save(year);
    }

    private XSSFWorkbook report(String path) throws Exception {
        var result = mvc.perform(get(path).with(user("test@example.com").roles("HEAD")))
                .andExpect(status().isOk())
                .andExpect(header().string("Cache-Control", "no-store"))
                .andExpect(content().contentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .andReturn();
        return new XSSFWorkbook(new ByteArrayInputStream(result.getResponse().getContentAsByteArray()));
    }

    @Test void projectAndYearReportsRespectYearFiltersAndKeepRevenueCurrencies() throws Exception {
        var year = reportYear(2031);
        var other = reportYear(2032);
        for (int i = 0; i < 3; i++) {
            var p = new Project();
            p.setYear(i == 2 ? other : year);
            p.setName("Project " + i);
            p.setProjectType(i == 0 ? ProjectType.REVENUE : ProjectType.STANDARD);
            p.setStatus(i == 0 ? ProjectStatus.ONGOING : ProjectStatus.FINISHED);
            p.setProjectPrice(123.45);
            p.setMembers(Set.of());
            projects.save(p);
            var r = new ProjectRevenue();
            r.setProject(p); r.setCustomer("=Untrusted customer");
            r.setRevenueMonth(LocalDate.of(2031, 1, 1)); r.setAmount(new BigDecimal("25.50"));
            r.setCurrency(emd.charitymanagementsystem.Models.Currency.CHF);
            r.setRecordedBy("test"); r.setRecordedAt(java.time.Instant.now());
            revenues.save(r);
        }
        String base = "/years/" + year.getId();
        try (var book = report(base + "/projects/export.xlsx?type=REVENUE&status=ONGOING")) {
            assertEquals(1, book.getSheet("Projects").getLastRowNum());
            assertEquals("Project 0", book.getSheet("Projects").getRow(1).getCell(1).getStringCellValue());
            assertEquals(123.45, book.getSheet("Projects").getRow(1).getCell(5).getNumericCellValue());
            var revenue = book.getSheet("Project revenue").getRow(1);
            assertEquals("CHF", revenue.getCell(5).getStringCellValue());
            assertEquals(CellType.STRING, revenue.getCell(3).getCellType());
        }
        try (var book = report(base + "/projects/export.xlsx?type=REVENUE&status=FINISHED")) {
            assertEquals(0, book.getSheet("Projects").getLastRowNum());
            assertEquals(0, book.getSheet("Project revenue").getLastRowNum());
        }
        try (var book = report(base + "/export.xlsx")) {
            assertEquals(5, book.getNumberOfSheets());
            assertNotNull(book.getSheet("Budget"));
            assertNotNull(book.getSheet("Donations"));
            assertNotNull(book.getSheet("Events"));
            assertEquals(2, book.getSheet("Projects").getLastRowNum());
            assertEquals(2, book.getSheet("Project revenue").getLastRowNum());
            assertEquals("No budget set", book.getSheet("Budget").getRow(1).getCell(2).getStringCellValue());
        }
    }

    @Test void eventReportsIncludeFilteredDatesTasksPaymentsAndRejectWrongYear() throws Exception {
        var year = reportYear(2033);
        var other = reportYear(2034);
        Long selectedId = null;
        for (int i = 0; i < 3; i++) {
            var event = new Event();
            event.setYear(i == 2 ? other : year); event.setPurpose("Event " + i);
            event.setMembers(List.of()); event.setEventType(EventType.TASK_BASED);
            event.setDate(java.time.LocalDateTime.now().plusDays(i == 1 ? -2 : 2));
            events.save(event);
            if (i == 0) {
                selectedId = event.getId();
                var task = new EventTask(); task.setEvent(event); task.setTitle("Prepare venue");
                task.setPrice(new BigDecimal("20.00")); task.setCompleted(true); tasks.save(task);
                var payment = new TaskPayment(); payment.setTask(task); payment.setAmount(new BigDecimal("100.00"));
                payment.setCurrency(emd.charitymanagementsystem.Models.Currency.MKD);
                payment.setPaidOn(LocalDate.now()); payment.setRecordedAt(java.time.Instant.now());
                payment.setRecordedBy("test"); taskPayments.save(payment);
            }
        }
        String base = "/years/" + year.getId() + "/events";
        try (var book = report(base + "/export.xlsx?type=TASK_BASED&status=UPCOMING")) {
            assertEquals(1, book.getSheet("Events").getLastRowNum());
            assertEquals("Event 0", book.getSheet("Events").getRow(1).getCell(1).getStringCellValue());
            assertTrue(DateUtil.isCellDateFormatted(book.getSheet("Events").getRow(1).getCell(3)));
        }
        try (var book = report(base + "/" + selectedId + "/export.xlsx")) {
            assertEquals(3, book.getNumberOfSheets());
            assertEquals("Completed", book.getSheet("Tasks").getRow(1).getCell(3).getStringCellValue());
            assertEquals(20, book.getSheet("Tasks").getRow(1).getCell(4).getNumericCellValue());
            assertEquals("MKD", book.getSheet("Payments").getRow(1).getCell(4).getStringCellValue());
            assertEquals(100, book.getSheet("Payments").getRow(1).getCell(3).getNumericCellValue());
        }
        var response = mvc.perform(get("/years/" + other.getId() + "/events/" + selectedId + "/export.xlsx")
                .with(user("test@example.com").roles("HEAD"))).andReturn().getResponse();
        assertNotEquals("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", response.getContentType());
    }

    @Test void membershipReportUsesSelectedYearAndPreservesVoidedReceipts() throws Exception {
        var member = new Member(); member.setName("Report member"); member.setSurname("Test");
        member.setEmail("report-" + System.nanoTime() + "@example.com"); member.setPassword("secret");
        member.setRole(Role.MEMBER); members.save(member);
        for (int year : List.of(2035, 2036)) {
            var payment = new MembershipPayment(); payment.setMemberId(member.getId());
            payment.setMemberName("Report member Test"); payment.setMembershipYear(year);
            payment.setAmount(new BigDecimal("15.00")); payment.setPaidOn(LocalDate.now());
            payment.setCurrency(emd.charitymanagementsystem.Models.Currency.CHF);
            payment.setRecordedAt(java.time.Instant.now()); payment.setRecordedBy("test");
            if (year == 2035) { payment.setVoidedAt(java.time.Instant.now()); payment.setVoidReason("Correction"); }
            membershipPayments.save(payment);
        }
        try (var book = report("/memberships/export.xlsx?year=2035")) {
            assertEquals(1, book.getSheet("Payment ledger").getLastRowNum());
            assertEquals("Voided", book.getSheet("Payment ledger").getRow(1).getCell(5).getStringCellValue());
            Row row = null;
            for (Row candidate : book.getSheet("Memberships")) {
                if (candidate.getRowNum() > 0 && candidate.getCell(0).getNumericCellValue() == member.getId()) row = candidate;
            }
            assertNotNull(row);
            assertEquals("Unpaid", row.getCell(6).getStringCellValue());
            assertEquals(10, row.getCell(10).getNumericCellValue());
        }
        try (var book = report("/memberships/export.xlsx?year=2036")) {
            Row row = null;
            for (Row candidate : book.getSheet("Memberships")) {
                if (candidate.getRowNum() > 0 && candidate.getCell(0).getNumericCellValue() == member.getId()) row = candidate;
            }
            assertNotNull(row);
            assertEquals("Paid", row.getCell(6).getStringCellValue());
            assertEquals("CHF", row.getCell(8).getStringCellValue());
            assertEquals(0, row.getCell(10).getNumericCellValue());
        }
        var pdfResult = mvc.perform(get("/memberships/export.pdf?year=2035")
                        .with(user("test@example.com").roles("TREASURER")))
                .andExpect(status().isOk()).andExpect(content().contentType("application/pdf"))
                .andExpect(header().string("Cache-Control", "no-store")).andReturn();
        try (var reader = new com.lowagie.text.pdf.PdfReader(pdfResult.getResponse().getContentAsByteArray())) {
            var extractor = new com.lowagie.text.pdf.parser.PdfTextExtractor(reader);
            var text = new StringBuilder();
            for (int page = 1; page <= reader.getNumberOfPages(); page++) text.append(extractor.getTextFromPage(page));
            assertTrue(text.toString().contains("Membership year 2035"));
            assertTrue(text.toString().contains("Voided"));
            assertTrue(text.toString().contains("Correction"));
            assertFalse(text.toString().contains("2036"));
        }
        String html = mvc.perform(get("/memberships?year=2035")
                        .with(user("test@example.com").roles("HEAD"))).andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        int selector = html.indexOf("id=\"year\"");
        int formEnd = html.indexOf("</form>", selector);
        assertTrue(html.indexOf("/memberships/export.pdf?year=2035", selector) < formEnd);
        assertTrue(html.indexOf("/memberships/export.xlsx?year=2035", selector) < formEnd);
        for (String path : List.of("/memberships/export.xlsx?year=2035", "/memberships/export.pdf?year=2035", "/years/1/export.xlsx")) {
            var response = mvc.perform(get(path).with(user("member@example.com").roles("MEMBER"))).andReturn().getResponse();
            assertNotEquals("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", response.getContentType());
            assertNotEquals("application/pdf", response.getContentType());
        }
    }
}
