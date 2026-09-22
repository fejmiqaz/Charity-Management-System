package emd.charitymanagementsystem.Web;

import emd.charitymanagementsystem.Models.Role;
import emd.charitymanagementsystem.Service.*;
import emd.charitymanagementsystem.Service.Implementation.ExcelExportService;
import emd.charitymanagementsystem.Service.Implementation.MembershipService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.io.IOException;
import java.time.LocalDate;

@RestController
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('HEAD','SUBHEAD','TREASURER','MEMBER')")
public class ExcelExportController {
    private final MemberService members;
    private final DonationService donations;
    private final YearsService years;
    private final MembershipService memberships;
    private final ExcelExportService excel;
    private final emd.charitymanagementsystem.Service.Implementation.ActivityExcelReportService reports;

    @GetMapping("/years/{yearId}/export.xlsx")
    @PreAuthorize("hasAnyRole('HEAD','SUBHEAD')")
    public ResponseEntity<byte[]> yearReport(@PathVariable Long yearId) throws IOException {
        return download("year-" + years.findEntityById(yearId).getYearValue(), reports.year(yearId));
    }

    @GetMapping("/years/{yearId}/projects/export.xlsx")
    @PreAuthorize("hasAnyRole('HEAD','SUBHEAD','PROJECT_MANAGER','VOLUNTEER','MEMBER')")
    public ResponseEntity<byte[]> projectReport(@PathVariable Long yearId,
            @RequestParam(required = false) emd.charitymanagementsystem.Models.ProjectType type,
            @RequestParam(required = false) emd.charitymanagementsystem.Models.ProjectStatus status) throws IOException {
        return download("projects-" + years.findEntityById(yearId).getYearValue(), reports.projects(yearId, type, status));
    }

    @GetMapping("/years/{yearId}/events/export.xlsx")
    @PreAuthorize("hasAnyRole('HEAD','SUBHEAD','EVENT_MANAGER','VOLUNTEER','MEMBER')")
    public ResponseEntity<byte[]> eventReport(@PathVariable Long yearId,
            @RequestParam(required = false) emd.charitymanagementsystem.Models.EventType type,
            @RequestParam(required = false) emd.charitymanagementsystem.Models.EventStatus status) throws IOException {
        return download("events-" + years.findEntityById(yearId).getYearValue(), reports.events(yearId, type, status));
    }

    @GetMapping("/years/{yearId}/events/{eventId}/export.xlsx")
    public ResponseEntity<byte[]> eventDetailsReport(@PathVariable Long yearId, @PathVariable Long eventId) throws IOException {
        return download("event-" + eventId + "-" + years.findEntityById(yearId).getYearValue(), reports.event(yearId, eventId));
    }

    @GetMapping("/memberships/export.xlsx")
    @PreAuthorize("hasAnyRole('HEAD','SUBHEAD','TREASURER')")
    public ResponseEntity<byte[]> membershipReport(@RequestParam(required = false) Integer year) throws IOException {
        int selectedYear = memberships.checkedYear(year);
        return download("memberships-" + selectedYear, reports.memberships(selectedYear));
    }

    @GetMapping("/members/export.xlsx")
    public ResponseEntity<byte[]> members(@RequestParam(required = false) String search,
            @RequestParam(required = false) String country, @RequestParam(required = false) String city,
            @RequestParam(required = false) Role role, @RequestParam(required = false) Integer membershipYear)
            throws IOException {
        int year = memberships.checkedYear(membershipYear);
        return download("members-" + year, excel.members(members.findAllMatching(search, country, city, role),
                year, memberships.paidMembers(year)));
    }

    @GetMapping("/years/{yearId}/donations/export.xlsx")
    public ResponseEntity<byte[]> donations(@PathVariable Long yearId) throws IOException {
        var year = years.findEntityById(yearId);
        return download("donations-" + year.getYearValue(), excel.donations(donations.findByYearId(yearId)));
    }

    private ResponseEntity<byte[]> download(String name, byte[] content) {
        return ResponseEntity.ok().contentType(MediaType.parseMediaType(
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.attachment()
                        .filename(name + "-" + LocalDate.now() + ".xlsx").build().toString())
                .cacheControl(CacheControl.noStore()).body(content);
    }
}
