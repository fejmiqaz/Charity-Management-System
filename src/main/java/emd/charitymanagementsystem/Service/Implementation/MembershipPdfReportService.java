package emd.charitymanagementsystem.Service.Implementation;

import com.lowagie.text.*;
import com.lowagie.text.pdf.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.io.ByteArrayOutputStream;
import java.awt.Color;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class MembershipPdfReportService {
    private final ActivityExcelReportService reports;

    public byte[] report(int year) {
        var sheets = reports.membershipSheets(year);
        var output = new ByteArrayOutputStream();
        var document = new Document(PageSize.A4.rotate());
        PdfReportLayout.open(document, output);
        PdfReportLayout.title(document, "Memberships Report", "Membership year " + year);
        var note = new Paragraph("Paid receipts settle membership for the selected year at their recorded amount. "
                + "Unpaid fees are in EUR. Previous years are not automatically debts. Voided receipts are excluded from paid status.",
                PdfReportLayout.font(9, false, PdfReportLayout.MUTED));
        note.setSpacingAfter(14);
        document.add(note);
        // Keep the printed member table readable; names are combined and repeated year/ID columns omitted.
        var table = table(new String[]{"Member", "Email", "Fee EUR", "Status", "Paid", "Currency", "Paid on", "Unpaid EUR"},
                new float[]{1.5f, 2.2f, .8f, .8f, .8f, .7f, 1.1f, .9f});
        for (var row : sheets.get(0).rows()) {
            Object[] values = {row[1] + " " + row[2], row[3], row[5], row[6], row[7], row[8], row[9], row[10]};
            for (int i = 0; i < values.length; i++) cell(table, values[i], i == 2 || i == 4 || i == 7);
        }
        if (sheets.get(0).rows().isEmpty()) document.add(new Paragraph("No members found."));
        else document.add(table);
        document.newPage();
        PdfReportLayout.title(document, "Payment ledger", "Membership year " + year + " - includes voided receipts");
        var ledger = table(sheets.get(1).headers(), new float[]{1.6f, .6f, .8f, .7f, 1f, .8f, 2f});
        for (var row : sheets.get(1).rows())
            for (int i = 0; i < row.length; i++) cell(ledger, row[i], i == 2);
        if (sheets.get(1).rows().isEmpty()) document.add(new Paragraph("No payments recorded for this year."));
        else document.add(ledger);
        PdfReportLayout.finish(document);
        return output.toByteArray();
    }

    private PdfPTable table(String[] headers, float[] widths) {
        var table = new PdfPTable(widths);
        table.setWidthPercentage(100);
        table.setHeaderRows(1);
        table.setSplitLate(false);
        for (String header : headers) {
            var cell = new PdfPCell(new Phrase(header, PdfReportLayout.font(9, true, Color.WHITE)));
            cell.setBackgroundColor(PdfReportLayout.NAVY);
            cell.setPadding(7);
            table.addCell(cell);
        }
        return table;
    }

    private void cell(PdfPTable table, Object value, boolean money) {
        String text = value == null ? "-" : money && value instanceof Number number
                ? String.format(Locale.ENGLISH, "%.2f", number.doubleValue()) : value.toString();
        var cell = new PdfPCell(new Phrase(text, PdfReportLayout.font(9, false, PdfReportLayout.NAVY)));
        cell.setPadding(6);
        cell.setBorderColor(PdfReportLayout.LINE);
        if (money) cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        table.addCell(cell);
    }
}
