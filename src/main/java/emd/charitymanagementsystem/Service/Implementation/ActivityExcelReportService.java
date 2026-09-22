package emd.charitymanagementsystem.Service.Implementation;

import emd.charitymanagementsystem.DTO.project.ProjectResponseDto;
import emd.charitymanagementsystem.DTO.event.EventResponseDto;
import emd.charitymanagementsystem.Models.*;
import emd.charitymanagementsystem.Service.*;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.*;
import java.util.*;
import java.util.stream.Collectors;
import static emd.charitymanagementsystem.Service.Implementation.ExcelExportService.ReportSheet;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ActivityExcelReportService {
    private final ExcelExportService excel;
    private final ProjectService projects;
    private final EventService events;
    private final YearsService years;
    private final DonationService donations;
    private final MemberService members;
    private final MembershipService memberships;
    private final ActivityFinanceService finance;
    @Value("${app.public-zone:Europe/Skopje}")
    private String timeZone;

    private ReportSheet sheet(String name, String[] headers, List<Object[]> rows, Integer... money) {
        return new ReportSheet(name, headers, rows, Set.of(money));
    }

    private String names(List<String> names) {
        return names == null ? "" : String.join(", ", names);
    }

    private List<ReportSheet> projectSheets(List<ProjectResponseDto> selected) {
        var rows = new ArrayList<Object[]>();
        var revenueRows = new ArrayList<Object[]>();
        for (var p : selected) {
            rows.add(new Object[]{p.getId(), p.getName(), p.getProjectType() == null ? ProjectType.STANDARD : p.getProjectType(),
                    p.getStatus(), p.getDateCreated(), p.getProjectPrice(), names(p.getMemberNames()), p.getDescription()});
            for (var r : finance.revenues(p.getId())) {
                revenueRows.add(new Object[]{p.getId(), p.getName(), r.getRevenueMonth(), r.getCustomer(),
                        r.getAmount(), r.getCurrency(), r.getNote()});
            }
        }
        return List.of(sheet("Projects", new String[]{"ID", "Project", "Type", "Status", "Created", "Cost (EUR)", "Members", "Description"}, rows, 5),
                sheet("Project revenue", new String[]{"Project ID", "Project", "Revenue month", "Customer", "Amount", "Currency", "Note"}, revenueRows, 4));
    }

    private ReportSheet eventSheet(List<EventResponseDto> selected, LocalDateTime now) {
        return sheet("Events", new String[]{"ID", "Purpose", "Type", "Date and time (" + timeZone + ")", "Status", "Participants"},
                selected.stream().map(e -> new Object[]{e.getId(), e.getPurpose(),
                        e.getEventType() == null ? EventType.NORMAL : e.getEventType(), e.getDate(),
                        e.getDate() == null ? "Unscheduled" : EventStatus.fromDate(e.getDate(), now), names(e.getMemberNames())}).toList());
    }

    public byte[] projects(Long yearId, ProjectType type, ProjectStatus status) throws IOException {
        years.findEntityById(yearId);
        var selected = projects.findByYearId(yearId).stream()
                .filter(p -> type == null || (p.getProjectType() == null ? ProjectType.STANDARD : p.getProjectType()) == type)
                .filter(p -> status == null || p.getStatus() == status).toList();
        return excel.workbook(projectSheets(selected));
    }

    public byte[] events(Long yearId, EventType type, EventStatus status) throws IOException {
        years.findEntityById(yearId);
        var now = LocalDateTime.now(ZoneId.of(timeZone));
        var selected = events.findAllByYearId(yearId).stream()
                .filter(e -> type == null || (e.getEventType() == null ? EventType.NORMAL : e.getEventType()) == type)
                .filter(e -> status == null || EventStatus.fromDate(e.getDate(), now) == status).toList();
        return excel.workbook(List.of(eventSheet(selected, now)));
    }

    public byte[] event(Long yearId, Long eventId) throws IOException {
        years.findEntityById(yearId);
        var event = events.findById(eventId);
        if (!Objects.equals(yearId, event.getYearId()))
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Event does not belong to this year.");
        var tasks = new ArrayList<Object[]>();
        var payments = new ArrayList<Object[]>();
        for (var task : finance.tasks(eventId)) {
            tasks.add(new Object[]{task.getId(), task.getTitle(), task.getDescription(),
                    task.isCompleted() ? "Completed" : "Pending", task.getPrice(),
                    task.getMembers().stream().map(m -> m.getName() + " " + m.getSurname()).collect(Collectors.joining(", "))});
            for (var payment : task.getPayments()) {
                var member = payment.getMember();
                payments.add(new Object[]{task.getId(), task.getTitle(),
                        member == null ? "" : member.getName() + " " + member.getSurname(),
                        payment.getAmount(), payment.getCurrency(), payment.getPaidOn(), payment.getNote()});
            }
        }
        return excel.workbook(List.of(eventSheet(List.of(event), LocalDateTime.now(ZoneId.of(timeZone))),
                sheet("Tasks", new String[]{"ID", "Task", "Description", "Status", "Cost (EUR)", "Assigned members"}, tasks, 4),
                sheet("Payments", new String[]{"Task ID", "Task", "Member", "Amount", "Currency", "Paid on", "Note"}, payments, 3)));
    }

    public byte[] year(Long yearId) throws IOException {
        var year = years.findEntityById(yearId);
        var projectList = projects.findByYearId(yearId);
        var budget = year.getBudget();
        var sheets = new ArrayList<ReportSheet>();
        var budgetRows = new ArrayList<Object[]>();
        budgetRows.add(new Object[]{year.getYearValue(), budget == null ? null : budget.getBudgetAmount(),
                budget == null ? "No budget set" : budget.getDescription()});
        sheets.add(sheet("Budget", new String[]{"Year", "Budget (EUR)", "Description"}, budgetRows, 1));
        sheets.add(sheet("Donations", new String[]{"Year", "Amount", "Currency", "Donating members"},
                donations.findByYearId(yearId).stream().map(d -> new Object[]{d.getYearValue(), d.getDonationAmount(),
                        d.getCurrency(), names(d.getMemberNames())}).toList(), 1));
        sheets.addAll(projectSheets(projectList));
        sheets.add(eventSheet(events.findAllByYearId(yearId), LocalDateTime.now(ZoneId.of(timeZone))));
        return excel.workbook(sheets);
    }

    public byte[] memberships(int year) throws IOException {
        return excel.workbook(membershipSheets(year));
    }

    public List<ReportSheet> membershipSheets(int year) {
        memberships.checkedYear(year);
        var paid = memberships.paidMembers(year);
        var fee = memberships.fee(year);
        var rows = members.listAll().stream().map(m -> {
            var payment = paid.get(m.getId());
            return new Object[]{m.getId(), m.getName(), m.getSurname(), m.getEmail(), year, fee,
                    payment == null ? "Unpaid" : "Paid", payment == null ? BigDecimal.ZERO : payment.getAmount(),
                    payment == null ? null : payment.getCurrency(), payment == null ? null : payment.getPaidOn(),
                    payment == null ? fee : BigDecimal.ZERO};
        }).toList();
        var ledger = memberships.payments(year).stream().map(p -> new Object[]{p.getMemberName(),
                p.getMembershipYear(), p.getAmount(), p.getCurrency(), p.getPaidOn(), p.isPaid() ? "Paid" : "Voided",
                p.getVoidReason()}).toList();
        return List.of(sheet("Memberships", new String[]{"Member ID", "First name", "Last name", "Email", "Year",
                "Annual fee (EUR)", "Status", "Paid amount", "Currency", "Payment date", "Unpaid fee (EUR)"}, rows, 5, 7, 10),
                sheet("Payment ledger", new String[]{"Member", "Year", "Amount", "Currency", "Paid on", "Status", "Void reason"}, ledger, 2));
    }
}
