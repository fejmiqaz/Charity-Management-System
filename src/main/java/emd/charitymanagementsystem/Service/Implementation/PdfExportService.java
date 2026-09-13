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
            new Color(23, 47, 80);

    private static final Color ALTERNATIVE_ROW_COLOR =
            new Color(243, 246, 251);

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

        PdfReportLayout.open(document, output);

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

        if (members.isEmpty()) addEmptyRow(table, 4, "No members available.");

        document.add(table);

        addTotal(
                document,
                "Total members: " + members.size()
        );

        PdfReportLayout.finish(document);

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

        PdfReportLayout.open(document, output);

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

        if (years.isEmpty()) addEmptyRow(table, 2, "No yearly records available.");

        document.add(table);

        addTotal(
                document,
                "Total years: " + years.size()
        );

        PdfReportLayout.finish(document);

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
        return exportCompleteYearPdf(year, budgets, donations, events, projects, java.math.BigDecimal.ZERO);
    }

    public ByteArrayInputStream exportCompleteYearPdf(
            Years year,
            List<BudgetResponseDto> budgets,
            List<DonationResponseDto> donations,
            List<EventResponseDto> events,
            List<ProjectResponseDto> projects,
            java.math.BigDecimal membershipIncome
    ) {
        Document document =
                new Document(PageSize.A4);

        ByteArrayOutputStream output =
                new ByteArrayOutputStream();

        PdfReportLayout.open(document, output);

        addTitle(
                document,
                "Complete Year Report",
                "Year: " + year.getYearValue()
        );

        addSectionTitle(document, "Financial summary");
        double allocated = budgets.stream().mapToDouble(b -> b.getBudgetAmount() == null ? 0 : b.getBudgetAmount()).sum();
        double contributed = donations.stream().mapToDouble(d -> d.getDonationAmount() == null ? 0 : d.getDonationAmount()).sum();
        double spent = projects.stream().mapToDouble(p -> p.getProjectPrice() == null ? 0 : p.getProjectPrice()).sum();
        PdfPTable summary = createDetailsTable();
        addDetailRow(summary, "Allocated budget", formatMoney(allocated));
        addDetailRow(summary, "Donations", formatMoney(contributed));
        addDetailRow(summary, "Project costs", formatMoney(spent));
        addDetailRow(summary, "Membership income", formatMoney(membershipIncome.doubleValue()));
        addDetailRow(summary, "Remaining budget", formatMoney(allocated + contributed + membershipIncome.doubleValue() - spent));
        document.add(summary);

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

        PdfReportLayout.finish(document);

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

        PdfReportLayout.open(document, output);

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

        PdfReportLayout.finish(document);

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

        PdfReportLayout.open(document, output);

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

        PdfReportLayout.finish(document);

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

        PdfReportLayout.open(document, output);

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
        PdfReportLayout.finish(document);

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

        PdfReportLayout.open(document, output);

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

        PdfReportLayout.finish(document);

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

        PdfReportLayout.open(document, output);

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
        PdfReportLayout.finish(document);

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

        PdfReportLayout.open(document, output);

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

        PdfReportLayout.finish(document);

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

        PdfReportLayout.open(document, output);

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

        addDetailRow(table, "Status", value(project.getStatus()).replace('_', ' '));
        addDetailRow(table, "Created", value(project.getDateCreated()));
        addDetailRow(table, "Cost", formatMoney(project.getProjectPrice() == null ? 0 : project.getProjectPrice()));
        addDetailRow(table, "Description", value(project.getDescription()));

        document.add(table);
        PdfReportLayout.finish(document);

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

        PdfPTable table = createTable(
                new float[]{.7f, 2.5f, 1.4f, 1.5f, 3f},
                "ID", "Project", "Status", "Cost (EUR)", "Members"
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

            addCell(table, value(project.getStatus()).replace('_', ' '), alternativeRow);
            addCell(table, formatMoney(project.getProjectPrice() == null ? 0 : project.getProjectPrice()), alternativeRow);

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
                    5,
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
        PdfReportLayout.title(document, titleText, subtitleText);
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
        sectionTitle.setSpacingAfter(9f);
        sectionTitle.setKeepTogether(true);

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

            cell.setPadding(10f);
            cell.setBorderColor(PdfReportLayout.LINE);

            table.addCell(cell);
        }

        table.setHeaderRows(1);
        table.setSplitLate(false);

        return table;
    }

    private PdfPTable createDetailsTable() {
        PdfPTable table =
                new PdfPTable(2);

        table.setWidthPercentage(100);
        table.setKeepTogether(true);
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

        labelCell.setPadding(10f);
        labelCell.setBorderColor(PdfReportLayout.LINE);
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

        contentCell.setPadding(10f);
        contentCell.setBorderColor(PdfReportLayout.LINE);
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

        cell.setPadding(9f);
        cell.setBorderColor(PdfReportLayout.LINE);
        cell.setBorderWidth(.4f);
        if (text.endsWith(" EUR")) cell.setHorizontalAlignment(Element.ALIGN_RIGHT);

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
                java.util.Locale.ENGLISH,
                "%,.2f EUR",
                amount
        );
    }
}