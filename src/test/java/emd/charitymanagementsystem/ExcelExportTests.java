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
            assertEquals(10, sheet.getRow(0).getLastCellNum());
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
            assertTrue(sheet.getRow(1).getCell(1).getCellStyle().getDataFormatString().contains("EUR"));
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
            assertTrue(DateUtil.isCellDateFormatted(row.getCell(9)));
            assertEquals(payment.getPaidOn(), row.getCell(9).getLocalDateTimeCellValue().toLocalDate());
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
}
