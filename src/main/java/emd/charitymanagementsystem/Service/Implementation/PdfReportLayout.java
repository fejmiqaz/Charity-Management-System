package emd.charitymanagementsystem.Service.Implementation;

import com.lowagie.text.*;
import com.lowagie.text.pdf.*;
import java.awt.Color;
import java.io.OutputStream;
import java.time.ZonedDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import org.springframework.security.core.context.SecurityContextHolder;

/** Shared print layout for every report. No signature is implied until signed. */
final class PdfReportLayout {
    static final Color NAVY = new Color(23,47,80);
    static final Color BLUE = new Color(47,91,149);
    static final Color MUTED = new Color(100,116,139);
    static final Color LINE = new Color(219,227,239);
    static Font font(float size, boolean bold, Color color) {
        return FontFactory.getFont(bold ? FontFactory.HELVETICA_BOLD : FontFactory.HELVETICA, size, color);
    }

    static void open(Document document, OutputStream output) {
        document.setMargins(42,42,48,52);
        PdfWriter writer = PdfWriter.getInstance(document, output);
        writer.setPageEvent(new PdfPageEventHelper() {
            @Override public void onEndPage(PdfWriter writer, Document document) {
                var canvas = writer.getDirectContent();
                canvas.saveState();
                canvas.setColorStroke(LINE); canvas.setLineWidth(.6f);
                canvas.moveTo(document.left(),32); canvas.lineTo(document.right(),32); canvas.stroke();
                ColumnText.showTextAligned(canvas,Element.ALIGN_LEFT,new Phrase("CHARITY MANAGEMENT  |  Organization report",font(8,false,MUTED)),document.left(),20,0);
                ColumnText.showTextAligned(canvas,Element.ALIGN_RIGHT,new Phrase("Page " + writer.getPageNumber(),font(8,false,MUTED)),document.right(),20,0);
                if(writer.getPageNumber()>1) {
                    ColumnText.showTextAligned(canvas,Element.ALIGN_LEFT,new Phrase("CHARITY MANAGEMENT  /  REPORT CONTINUED",font(8,true,MUTED)),document.left(),document.top()+20,0);
                }
                canvas.restoreState();
            }
        });
        document.addCreator("Charity Management System");
        document.open();
    }

    static void title(Document document, String title, String subtitle) {
        document.addTitle(title);
        Paragraph brand = new Paragraph("CHARITY MANAGEMENT  /  REPORTS",font(9,true,BLUE));
        brand.setSpacingAfter(12); document.add(brand);
        Paragraph heading = new Paragraph(title,font(25,true,NAVY));
        heading.setSpacingAfter(8); document.add(heading);
        if(subtitle!=null && !subtitle.isBlank()) {
            Paragraph sub = new Paragraph(subtitle,font(12,false,MUTED));sub.setSpacingAfter(12);document.add(sub);
        }
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        String exportedBy = authentication != null && authentication.isAuthenticated() ? authentication.getName() : "System export";
        String date = ZonedDateTime.now(ZoneOffset.UTC).format(DateTimeFormatter.ofPattern("dd MMM uuuu, HH:mm 'UTC'",Locale.ENGLISH));
        PdfPTable metadata = new PdfPTable(new float[]{1,1});metadata.setWidthPercentage(100);metadata.setSpacingAfter(20);
        for(String text : new String[]{"EXPORT DATE\n"+date,"EXPORTED BY\n"+exportedBy}) {
            PdfPCell cell = new PdfPCell(new Phrase(text,font(9,false,MUTED)));cell.setLeading(0,1.5f);cell.setPadding(12);cell.setBorder(Rectangle.NO_BORDER);cell.setBackgroundColor(new Color(240,244,250));metadata.addCell(cell);
        }
        document.add(metadata);
    }

    static void finish(Document document) {
        PdfPTable signatures = new PdfPTable(new float[]{1,1});
        signatures.setWidthPercentage(100); signatures.setSpacingBefore(26); signatures.setKeepTogether(true);
        for(String title : new String[]{"PREPARED BY", "REVIEWED / APPROVED BY"}) {
            PdfPCell cell = new PdfPCell();cell.setBorder(Rectangle.TOP);cell.setBorderColor(LINE);cell.setPadding(12);
            cell.addElement(new Paragraph(title,font(9,true,BLUE)));
            Paragraph fields = new Paragraph("\nName: __________________________\n\nSignature: _______________________\n\nDate: ___________________________",font(9,false,MUTED));
            fields.setLeading(13);cell.addElement(fields);signatures.addCell(cell);
        }
        document.add(signatures);
        document.close();
    }
}
