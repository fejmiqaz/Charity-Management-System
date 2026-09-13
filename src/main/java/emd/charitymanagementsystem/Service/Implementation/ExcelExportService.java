package emd.charitymanagementsystem.Service.Implementation;

import emd.charitymanagementsystem.DTO.member.MemberResponseDto;
import emd.charitymanagementsystem.DTO.donation.DonationResponseDto;
import emd.charitymanagementsystem.Models.MembershipPayment;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.util.*;

@Service
public class ExcelExportService {
    public byte[] members(List<MemberResponseDto> members, int year, Map<Long, MembershipPayment> payments)
            throws IOException {
        List<Object[]> rows = new ArrayList<>();
        for (var member : members) {
            var payment = payments.get(member.getId());
            rows.add(new Object[]{member.getName(), member.getSurname(), member.getEmail(), member.getPhone(),
                    member.getCountry(), member.getCity(), year, payment == null ? "Unpaid" : "Paid",
                    payment == null ? null : payment.getAmount(), payment == null ? null : payment.getPaidOn()});
        }
        return workbook("Members", new String[]{"First name", "Last name", "Email", "Phone", "Country", "City",
                "Membership year", "Membership status", "Paid amount (EUR)", "Payment date"}, rows, Set.of(8));
    }

    public byte[] donations(List<DonationResponseDto> donations) throws IOException {
        List<Object[]> rows = donations.stream().map(d -> new Object[]{d.getYearValue(), d.getDonationAmount(),
                d.getMemberNames() == null ? "" : String.join(", ", d.getMemberNames())}).toList();
        return workbook("Donations", new String[]{"Year", "Amount (EUR)", "Donating members"}, rows, Set.of(1));
    }

    private byte[] workbook(String name, String[] headers, List<Object[]> rows, Set<Integer> moneyColumns)
            throws IOException {
        try (var book = new XSSFWorkbook(); var output = new ByteArrayOutputStream()) {
            var sheet = book.createSheet(name);
            var headerStyle = book.createCellStyle();
            headerStyle.setFillForegroundColor(IndexedColors.DARK_BLUE.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            var font = book.createFont();
            font.setBold(true);
            font.setColor(IndexedColors.WHITE.getIndex());
            headerStyle.setFont(font);
            var money = book.createCellStyle();
            money.setDataFormat(book.createDataFormat().getFormat("#,##0.00 \"EUR\""));
            var date = book.createCellStyle();
            date.setDataFormat(book.createDataFormat().getFormat("dd mmm yyyy"));
            var header = sheet.createRow(0);
            for (int c = 0; c < headers.length; c++) {
                header.createCell(c).setCellValue(headers[c]);
                header.getCell(c).setCellStyle(headerStyle);
            }
            for (Object[] values : rows) {
                var row = sheet.createRow(sheet.getLastRowNum() + 1);
                for (int c = 0; c < values.length; c++) {
                    var cell = row.createCell(c);
                    Object value = values[c];
                    if (value instanceof Number number) {
                        cell.setCellValue(number.doubleValue());
                        if (moneyColumns.contains(c)) cell.setCellStyle(money);
                    } else if (value instanceof LocalDate day) {
                        cell.setCellValue(day);
                        cell.setCellStyle(date);
                    } else if (value != null) {
                        // Always write user text as a string, never as an Excel formula.
                        cell.setCellValue(value.toString());
                    }
                }
            }
            sheet.createFreezePane(0, 1);
            sheet.setAutoFilter(new CellRangeAddress(0, sheet.getLastRowNum(), 0, headers.length - 1));
            for (int c = 0; c < headers.length; c++) {
                sheet.autoSizeColumn(c);
                sheet.setColumnWidth(c, Math.min(60 * 256, Math.max(18 * 256, sheet.getColumnWidth(c) + 512)));
            }
            book.write(output);
            return output.toByteArray();
        }
    }
}
