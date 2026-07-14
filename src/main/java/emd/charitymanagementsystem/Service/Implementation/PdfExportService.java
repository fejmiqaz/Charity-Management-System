package emd.charitymanagementsystem.Service.Implementation;

import com.lowagie.text.*;
import com.lowagie.text.pdf.*;
import emd.charitymanagementsystem.Models.Member;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.List;

@Service
public class PdfExportService {

    public ByteArrayInputStream exportMembersPdf(List<Member> members) {
        Document document = new Document();
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        PdfWriter.getInstance(document, out);
        document.open();

        Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18);
        Paragraph title = new Paragraph("Members Report", titleFont);
        title.setAlignment(Element.ALIGN_CENTER);
        document.add(title);
        document.add(new Paragraph(" "));

        PdfPTable table = new PdfPTable(3);
        table.setWidthPercentage(100);

        table.addCell("ID");
        table.addCell("Name");
        table.addCell("Email");

        for (Member member : members) {
            table.addCell(String.valueOf(member.getId()));
            table.addCell(member.getName());
            table.addCell(member.getEmail());
        }

        document.add(table);
        document.close();

        return new ByteArrayInputStream(out.toByteArray());
    }
}