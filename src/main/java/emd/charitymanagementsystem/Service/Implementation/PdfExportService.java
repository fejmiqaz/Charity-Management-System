package emd.charitymanagementsystem.Service.Implementation;

import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import emd.charitymanagementsystem.DTO.budget.BudgetResponseDto;
import emd.charitymanagementsystem.DTO.donation.DonationResponseDto;
import emd.charitymanagementsystem.DTO.event.EventResponseDto;
import emd.charitymanagementsystem.DTO.project.ProjectResponseDto;
import emd.charitymanagementsystem.DTO.years.YearsResponseDto;
import emd.charitymanagementsystem.Models.Member;
import emd.charitymanagementsystem.Models.Years;
import org.springframework.stereotype.Service;

import java.awt.Color;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class PdfExportService {

    private static final Font TITLE_FONT =
            FontFactory.getFont(
                    FontFactory.HELVETICA_BOLD,
                    18
            );

    private static final Font SECTION_TITLE_FONT =
            FontFactory.getFont(
                    FontFactory.HELVETICA_BOLD,
                    14
            );

    private static final Font SUBTITLE_FONT =
            FontFactory.getFont(
                    FontFactory.HELVETICA,
                    11
            );

    private static final Font HEADER_FONT =
            FontFactory.getFont(
                    FontFactory.HELVETICA_BOLD,
                    11,
                    Color.WHITE
            );

    private static final Font CELL_FONT =
            FontFactory.getFont(
                    FontFactory.HELVETICA,
                    10
            );

    private static final Font TOTAL_FONT =
            FontFactory.getFont(
                    FontFactory.HELVETICA_BOLD,
                    12
            );

    private static final Color HEADER_COLOR =
            new Color(33, 37, 41);

    private static final Color ALTERNATIVE_ROW_COLOR =
            new Color(242, 242, 242);

    private static final DateTimeFormatter EVENT_DATE_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    /*
     * =========================================================
     * MEMBERS REPORT
     * =========================================================
     */

    public ByteArrayInputStream exportMembersPdf(
            List<Member> members
    ) {
        Document document =
                new Document(PageSize.A4.rotate());

        ByteArrayOutputStream output =
                new ByteArrayOutputStream();

        PdfWriter.getInstance(document, output);
        document.open();

        addTitle(
                document,
                "Members Report",
                null
        );

        PdfPTable table = createTable(
                new float[]{1f, 2f, 2f, 3f},
                "ID",
                "Name",
                "Surname",
                "Email"
        );

        int rowIndex = 0;

        for (Member member : members) {
            boolean alternativeRow =
                    rowIndex++ % 2 != 0;

            addCell(
                    table,
                    value(member.getId()),
                    alternativeRow
            );

            addCell(
                    table,
                    value(member.getName()),
                    alternativeRow
            );

            addCell(
                    table,
                    value(member.getSurname()),
                    alternativeRow
            );

            addCell(
                    table,
                    value(member.getEmail()),
                    alternativeRow
            );
        }

        document.add(table);

        addTotal(
                document,
                "Total members: " + members.size()
        );

        document.close();

        return new ByteArrayInputStream(
                output.toByteArray()
        );
    }

    /*
     * =========================================================
     * ALL YEARS REPORT
     * =========================================================
     */

    public ByteArrayInputStream exportYearsPdf(
            List<YearsResponseDto> years
    ) {
        Document document =
                new Document(PageSize.A4);

        ByteArrayOutputStream output =
                new ByteArrayOutputStream();

        PdfWriter.getInstance(document, output);
        document.open();

        addTitle(
                document,
                "Years Report",
                null
        );

        PdfPTable table = createTable(
                new float[]{1f, 2f},
                "ID",
                "Year"
        );

        int rowIndex = 0;

        for (YearsResponseDto year : years) {
            boolean alternativeRow =
                    rowIndex++ % 2 != 0;

            addCell(
                    table,
                    value(year.getId()),
                    alternativeRow
            );

            addCell(
                    table,
                    value(year.getYearValue()),
                    alternativeRow
            );
        }

        document.add(table);

        addTotal(
                document,
                "Total years: " + years.size()
        );

        document.close();

        return new ByteArrayInputStream(
                output.toByteArray()
        );
    }

    /*
     * =========================================================
     * COMPLETE REPORT FOR ONE YEAR
     * =========================================================
     */

    public ByteArrayInputStream exportCompleteYearPdf(
            Years year,
            List<BudgetResponseDto> budgets,
            List<DonationResponseDto> donations,
            List<EventResponseDto> events,
            List<ProjectResponseDto> projects
    ) {
        Document document =
                new Document(PageSize.A4.rotate());

        ByteArrayOutputStream output =
                new ByteArrayOutputStream();

        PdfWriter.getInstance(document, output);
        document.open();

        addTitle(
                document,
                "Complete Year Report",
                "Year: " + year.getYearValue()
        );

        addYearInformationSection(
                document,
                year
        );

        addBudgetSection(
                document,
                budgets
        );

        addDonationsSection(
                document,
                donations
        );

        addEventsSection(
                document,
                events
        );

        addProjectsSection(
                document,
                projects
        );

        document.close();

        return new ByteArrayInputStream(
                output.toByteArray()
        );
    }

    /*
     * =========================================================
     * BUDGET REPORT FOR ONE YEAR
     * =========================================================
     */

    public ByteArrayInputStream exportBudgetsPdf(
            List<BudgetResponseDto> budgets,
            Integer yearValue
    ) {
        Document document =
                new Document(PageSize.A4);

        ByteArrayOutputStream output =
                new ByteArrayOutputStream();

        PdfWriter.getInstance(document, output);
        document.open();

        addTitle(
                document,
                "Budget Report",
                yearValue != null
                        ? "Year: " + yearValue
                        : null
        );

        addBudgetSection(
                document,
                budgets
        );

        document.close();

        return new ByteArrayInputStream(
                output.toByteArray()
        );
    }

    /*
     * =========================================================
     * ALL DONATIONS FOR ONE YEAR
     * =========================================================
     */

    public ByteArrayInputStream exportDonationsPdf(
            List<DonationResponseDto> donations,
            Integer yearValue
    ) {
        Document document =
                new Document(PageSize.A4.rotate());

        ByteArrayOutputStream output =
                new ByteArrayOutputStream();

        PdfWriter.getInstance(document, output);
        document.open();

        addTitle(
                document,
                "Donations Report",
                yearValue != null
                        ? "Year: " + yearValue
                        : null
        );

        addDonationsSection(
                document,
                donations
        );

        document.close();

        return new ByteArrayInputStream(
                output.toByteArray()
        );
    }

    /*
     * =========================================================
     * ONE DONATION
     * =========================================================
     */

    public ByteArrayInputStream exportSingleDonationPdf(
            DonationResponseDto donation,
            Integer yearValue
    ) {
        Document document =
                new Document(PageSize.A4);

        ByteArrayOutputStream output =
                new ByteArrayOutputStream();

        PdfWriter.getInstance(document, output);
        document.open();

        addTitle(
                document,
                "Donation Report",
                "Year: " + yearValue
        );

        PdfPTable table = createDetailsTable();

        addDetailRow(
                table,
                "Donation ID",
                value(donation.getId())
        );

        addDetailRow(
                table,
                "Amount",
                formatMoney(
                        donation.getDonationAmount() != null
                                ? donation.getDonationAmount()
                                : 0.0
                )
        );

        addDetailRow(
                table,
                "Members",
                formatMembers(
                        donation.getMemberNames()
                )
        );

        document.add(table);
        document.close();

        return new ByteArrayInputStream(
                output.toByteArray()
        );
    }

    /*
     * =========================================================
     * ALL EVENTS FOR ONE YEAR
     * =========================================================
     */

    public ByteArrayInputStream exportEventsPdf(
            List<EventResponseDto> events,
            Integer yearValue
    ) {
        Document document =
                new Document(PageSize.A4.rotate());

        ByteArrayOutputStream output =
                new ByteArrayOutputStream();

        PdfWriter.getInstance(document, output);
        document.open();

        addTitle(
                document,
                "Events Report",
                yearValue != null
                        ? "Year: " + yearValue
                        : null
        );

        addEventsSection(
                document,
                events
        );

        document.close();

        return new ByteArrayInputStream(
                output.toByteArray()
        );
    }

    /*
     * =========================================================
     * ONE EVENT
     * =========================================================
     */

    public ByteArrayInputStream exportSingleEventPdf(
            EventResponseDto event,
            Integer yearValue
    ) {
        Document document =
                new Document(PageSize.A4);

        ByteArrayOutputStream output =
                new ByteArrayOutputStream();

        PdfWriter.getInstance(document, output);
        document.open();

        addTitle(
                document,
                "Event Report",
                "Year: " + yearValue
        );

        PdfPTable table = createDetailsTable();

        addDetailRow(
                table,
                "Event ID",
                value(event.getId())
        );

        addDetailRow(
                table,
                "Purpose",
                value(event.getPurpose())
        );

        addDetailRow(
                table,
                "Date",
                formatEventDate(event)
        );

        addDetailRow(
                table,
                "Members",
                formatMembers(
                        event.getMemberNames()
                )
        );

        document.add(table);
        document.close();

        return new ByteArrayInputStream(
                output.toByteArray()
        );
    }

    /*
     * =========================================================
     * ALL PROJECTS FOR ONE YEAR
     * =========================================================
     */

    public ByteArrayInputStream exportProjectsPdf(
            List<ProjectResponseDto> projects,
            Integer yearValue
    ) {
        Document document =
                new Document(PageSize.A4.rotate());

        ByteArrayOutputStream output =
                new ByteArrayOutputStream();

        PdfWriter.getInstance(document, output);
        document.open();

        addTitle(
                document,
                "Projects Report",
                yearValue != null
                        ? "Year: " + yearValue
                        : null
        );

        addProjectsSection(
                document,
                projects
        );

        document.close();

        return new ByteArrayInputStream(
                output.toByteArray()
        );
    }

    /*
     * =========================================================
     * ONE PROJECT
     * =========================================================
     */

    public ByteArrayInputStream exportSingleProjectPdf(
            ProjectResponseDto project,
            Integer yearValue
    ) {
        Document document =
                new Document(PageSize.A4);

        ByteArrayOutputStream output =
                new ByteArrayOutputStream();

        PdfWriter.getInstance(document, output);
        document.open();

        addTitle(
                document,
                "Project Report",
                "Year: " + yearValue
        );

        PdfPTable table = createDetailsTable();

        addDetailRow(
                table,
                "Project ID",
                value(project.getId())
        );

        addDetailRow(
                table,
                "Project Name",
                value(project.getName())
        );

        addDetailRow(
                table,
                "Members",
                formatMembers(
                        project.getMemberNames()
                )
        );

        document.add(table);
        document.close();

        return new ByteArrayInputStream(
                output.toByteArray()
        );
    }

    /*
     * =========================================================
     * COMPLETE YEAR SECTIONS
     * =========================================================
     */

    private void addYearInformationSection(
            Document document,
            Years year
    ) {
        addSectionTitle(
                document,
                "Year Information"
        );

        PdfPTable table = createDetailsTable();

        addDetailRow(
                table,
                "Year ID",
                value(year.getId())
        );

        addDetailRow(
                table,
                "Year",
                value(year.getYearValue())
        );

        document.add(table);
    }

    private void addBudgetSection(
            Document document,
            List<BudgetResponseDto> budgets
    ) {
        addSectionTitle(
                document,
                "Budget"
        );

        PdfPTable table = createTable(
                new float[]{1f, 2f, 4f},
                "ID",
                "Amount",
                "Description"
        );

        double totalBudget = 0.0;
        int rowIndex = 0;

        for (BudgetResponseDto budget : budgets) {
            boolean alternativeRow =
                    rowIndex++ % 2 != 0;

            double amount =
                    budget.getBudgetAmount() != null
                            ? budget.getBudgetAmount()
                            : 0.0;

            totalBudget += amount;

            addCell(
                    table,
                    value(budget.getId()),
                    alternativeRow
            );

            addCell(
                    table,
                    formatMoney(amount),
                    alternativeRow
            );

            addCell(
                    table,
                    value(budget.getDescription()),
                    alternativeRow
            );
        }

        if (budgets.isEmpty()) {
            addEmptyRow(
                    table,
                    3,
                    "No budget information available."
            );
        }

        document.add(table);

        addTotal(
                document,
                "Total budget: "
                        + formatMoney(totalBudget)
        );
    }

    private void addDonationsSection(
            Document document,
            List<DonationResponseDto> donations
    ) {
        addSectionTitle(
                document,
                "Donations"
        );

        PdfPTable table = createTable(
                new float[]{1f, 2f, 5f},
                "ID",
                "Amount",
                "Members"
        );

        double totalDonations = 0.0;
        int rowIndex = 0;

        for (DonationResponseDto donation : donations) {
            boolean alternativeRow =
                    rowIndex++ % 2 != 0;

            double amount =
                    donation.getDonationAmount() != null
                            ? donation.getDonationAmount()
                            : 0.0;

            totalDonations += amount;

            addCell(
                    table,
                    value(donation.getId()),
                    alternativeRow
            );

            addCell(
                    table,
                    formatMoney(amount),
                    alternativeRow
            );

            addCell(
                    table,
                    formatMembers(
                            donation.getMemberNames()
                    ),
                    alternativeRow
            );
        }

        if (donations.isEmpty()) {
            addEmptyRow(
                    table,
                    3,
                    "No donations available."
            );
        }

        document.add(table);

        addTotal(
                document,
                "Total donations: "
                        + formatMoney(totalDonations)
        );
    }

    private void addEventsSection(
            Document document,
            List<EventResponseDto> events
    ) {
        addSectionTitle(
                document,
                "Events"
        );

        PdfPTable table = createTable(
                new float[]{1f, 3f, 2f, 4f},
                "ID",
                "Purpose",
                "Date",
                "Members"
        );

        int rowIndex = 0;

        for (EventResponseDto event : events) {
            boolean alternativeRow =
                    rowIndex++ % 2 != 0;

            addCell(
                    table,
                    value(event.getId()),
                    alternativeRow
            );

            addCell(
                    table,
                    value(event.getPurpose()),
                    alternativeRow
            );

            addCell(
                    table,
                    formatEventDate(event),
                    alternativeRow
            );

            addCell(
                    table,
                    formatMembers(
                            event.getMemberNames()
                    ),
                    alternativeRow
            );
        }

        if (events.isEmpty()) {
            addEmptyRow(
                    table,
                    4,
                    "No events available."
            );
        }

        document.add(table);

        addTotal(
                document,
                "Total events: " + events.size()
        );
    }

    private void addProjectsSection(
            Document document,
            List<ProjectResponseDto> projects
    ) {
        addSectionTitle(
                document,
                "Projects"
        );

        /*
         * Your current ProjectResponseDto usage shows:
         * getId(), getName() and getMemberNames().
         *
         * Therefore, this table has three columns.
         */
        PdfPTable table = createTable(
                new float[]{1f, 3f, 5f},
                "ID",
                "Name",
                "Members"
        );

        int rowIndex = 0;

        for (ProjectResponseDto project : projects) {
            boolean alternativeRow =
                    rowIndex++ % 2 != 0;

            addCell(
                    table,
                    value(project.getId()),
                    alternativeRow
            );

            addCell(
                    table,
                    value(project.getName()),
                    alternativeRow
            );

            addCell(
                    table,
                    formatMembers(
                            project.getMemberNames()
                    ),
                    alternativeRow
            );
        }

        if (projects.isEmpty()) {
            addEmptyRow(
                    table,
                    3,
                    "No projects available."
            );
        }

        document.add(table);

        addTotal(
                document,
                "Total projects: " + projects.size()
        );
    }

    /*
     * =========================================================
     * SHARED PDF METHODS
     * =========================================================
     */

    private void addTitle(
            Document document,
            String titleText,
            String subtitleText
    ) {
        Paragraph title =
                new Paragraph(
                        titleText,
                        TITLE_FONT
                );

        title.setAlignment(
                Element.ALIGN_CENTER
        );

        title.setSpacingAfter(6f);

        document.add(title);

        if (subtitleText != null
                && !subtitleText.isBlank()) {

            Paragraph subtitle =
                    new Paragraph(
                            subtitleText,
                            SUBTITLE_FONT
                    );

            subtitle.setAlignment(
                    Element.ALIGN_CENTER
            );

            subtitle.setSpacingAfter(15f);

            document.add(subtitle);
        } else {
            document.add(
                    new Paragraph(" ")
            );
        }
    }

    private void addSectionTitle(
            Document document,
            String title
    ) {
        Paragraph sectionTitle =
                new Paragraph(
                        title,
                        SECTION_TITLE_FONT
                );

        sectionTitle.setSpacingBefore(15f);
        sectionTitle.setSpacingAfter(5f);

        document.add(sectionTitle);
    }

    private PdfPTable createTable(
            float[] widths,
            String... headers
    ) {
        PdfPTable table =
                new PdfPTable(headers.length);

        table.setWidthPercentage(100);
        table.setSpacingBefore(5f);
        table.setSpacingAfter(5f);

        try {
            table.setWidths(widths);
        } catch (DocumentException exception) {
            throw new IllegalStateException(
                    "Could not configure PDF table widths.",
                    exception
            );
        }

        for (String header : headers) {
            PdfPCell cell =
                    new PdfPCell(
                            new Phrase(
                                    header,
                                    HEADER_FONT
                            )
                    );

            cell.setBackgroundColor(
                    HEADER_COLOR
            );

            cell.setHorizontalAlignment(
                    Element.ALIGN_CENTER
            );

            cell.setVerticalAlignment(
                    Element.ALIGN_MIDDLE
            );

            cell.setPadding(8f);

            table.addCell(cell);
        }

        table.setHeaderRows(1);

        return table;
    }

    private PdfPTable createDetailsTable() {
        PdfPTable table =
                new PdfPTable(2);

        table.setWidthPercentage(100);
        table.setSpacingBefore(10f);
        table.setSpacingAfter(10f);

        try {
            table.setWidths(
                    new float[]{2f, 5f}
            );
        } catch (DocumentException exception) {
            throw new IllegalStateException(
                    "Could not configure PDF details table.",
                    exception
            );
        }

        return table;
    }

    private void addDetailRow(
            PdfPTable table,
            String label,
            String content
    ) {
        PdfPCell labelCell =
                new PdfPCell(
                        new Phrase(
                                label,
                                HEADER_FONT
                        )
                );

        labelCell.setBackgroundColor(
                HEADER_COLOR
        );

        labelCell.setPadding(8f);
        labelCell.setVerticalAlignment(
                Element.ALIGN_MIDDLE
        );

        PdfPCell contentCell =
                new PdfPCell(
                        new Phrase(
                                content,
                                CELL_FONT
                        )
                );

        contentCell.setPadding(8f);
        contentCell.setVerticalAlignment(
                Element.ALIGN_MIDDLE
        );

        table.addCell(labelCell);
        table.addCell(contentCell);
    }

    private void addCell(
            PdfPTable table,
            String text,
            boolean alternativeRow
    ) {
        PdfPCell cell =
                new PdfPCell(
                        new Phrase(
                                text,
                                CELL_FONT
                        )
                );

        cell.setPadding(7f);

        cell.setVerticalAlignment(
                Element.ALIGN_MIDDLE
        );

        if (alternativeRow) {
            cell.setBackgroundColor(
                    ALTERNATIVE_ROW_COLOR
            );
        }

        table.addCell(cell);
    }

    private void addEmptyRow(
            PdfPTable table,
            int columnCount,
            String message
    ) {
        PdfPCell cell =
                new PdfPCell(
                        new Phrase(
                                message,
                                CELL_FONT
                        )
                );

        cell.setColspan(columnCount);

        cell.setHorizontalAlignment(
                Element.ALIGN_CENTER
        );

        cell.setPadding(10f);

        table.addCell(cell);
    }

    private void addTotal(
            Document document,
            String totalText
    ) {
        Paragraph total =
                new Paragraph(
                        totalText,
                        TOTAL_FONT
                );

        total.setAlignment(
                Element.ALIGN_RIGHT
        );

        total.setSpacingBefore(5f);
        total.setSpacingAfter(10f);

        document.add(total);
    }

    private String formatMembers(
            List<String> memberNames
    ) {
        if (memberNames == null
                || memberNames.isEmpty()) {
            return "No members";
        }

        return String.join(
                ", ",
                memberNames
        );
    }

    private String formatEventDate(
            EventResponseDto event
    ) {
        if (event.getDate() == null) {
            return "-";
        }

        return event.getDate().format(
                EVENT_DATE_FORMATTER
        );
    }

    private String value(
            Object value
    ) {
        return value != null
                ? String.valueOf(value)
                : "-";
    }

    private String formatMoney(
            double amount
    ) {
        return String.format(
                "%.2f EUR",
                amount
        );
    }
}