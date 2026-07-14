package emd.charitymanagementsystem.Web;

import emd.charitymanagementsystem.DTO.budget.BudgetResponseDto;
import emd.charitymanagementsystem.DTO.donation.DonationResponseDto;
import emd.charitymanagementsystem.DTO.event.EventResponseDto;
import emd.charitymanagementsystem.DTO.project.ProjectResponseDto;
import emd.charitymanagementsystem.Models.Years;
import emd.charitymanagementsystem.Service.BudgetService;
import emd.charitymanagementsystem.Service.DonationService;
import emd.charitymanagementsystem.Service.EventService;
import emd.charitymanagementsystem.Service.ProjectService;
import emd.charitymanagementsystem.Service.YearsService;
import emd.charitymanagementsystem.Service.Implementation.PdfExportService;
import lombok.AllArgsConstructor;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.ByteArrayInputStream;
import java.util.List;

@RestController
@RequestMapping("/pdf")
@AllArgsConstructor
@PreAuthorize("hasAnyRole('HEAD', 'SUBHEAD')")
public class PdfExportController {

    private final PdfExportService pdfExportService;
    private final YearsService yearsService;
    private final BudgetService budgetService;
    private final DonationService donationService;
    private final EventService eventService;
    private final ProjectService projectService;

    /*
     * =========================================================
     * COMPLETE REPORT FOR ONE YEAR
     * Includes:
     * - year information
     * - budget
     * - donations
     * - events
     * - projects
     * =========================================================
     */

    @GetMapping("/years/{yearId}")
    public ResponseEntity<InputStreamResource> exportCompleteYearReport(
            @PathVariable Long yearId
    ) {
        Years year = yearsService.findEntityById(yearId);

        List<BudgetResponseDto> budgets = budgetService.listAll()
                .stream()
                .filter(budget -> yearId.equals(budget.getYearId()))
                .toList();

        List<DonationResponseDto> donations = donationService.listAll()
                .stream()
                .filter(donation -> yearId.equals(donation.getYearId()))
                .toList();

        List<EventResponseDto> events = eventService.listAll()
                .stream()
                .filter(event -> yearId.equals(event.getYearId()))
                .toList();

        List<ProjectResponseDto> projects = projectService.listAll()
                .stream()
                .filter(project -> yearId.equals(project.getYearId()))
                .toList();

        ByteArrayInputStream pdf =
                pdfExportService.exportCompleteYearPdf(
                        year,
                        budgets,
                        donations,
                        events,
                        projects
                );

        return createPdfResponse(
                pdf,
                "complete-year-" + year.getYearValue() + ".pdf"
        );
    }

    /*
     * =========================================================
     * COMPLETE BUDGET FOR ONE YEAR
     * =========================================================
     */

    @GetMapping("/years/{yearId}/budgets")
    public ResponseEntity<InputStreamResource> exportBudgetsByYear(
            @PathVariable Long yearId
    ) {
        Years year = yearsService.findEntityById(yearId);

        List<BudgetResponseDto> budgets = budgetService.listAll()
                .stream()
                .filter(budget -> yearId.equals(budget.getYearId()))
                .toList();

        ByteArrayInputStream pdf =
                pdfExportService.exportBudgetsPdf(
                        budgets,
                        year.getYearValue()
                );

        return createPdfResponse(
                pdf,
                "budget-" + year.getYearValue() + ".pdf"
        );
    }

    /*
     * =========================================================
     * ALL DONATIONS FOR ONE YEAR
     * =========================================================
     */

    @GetMapping("/years/{yearId}/donations")
    public ResponseEntity<InputStreamResource> exportDonationsByYear(
            @PathVariable Long yearId
    ) {
        Years year = yearsService.findEntityById(yearId);

        List<DonationResponseDto> donations = donationService.listAll()
                .stream()
                .filter(donation -> yearId.equals(donation.getYearId()))
                .toList();

        ByteArrayInputStream pdf =
                pdfExportService.exportDonationsPdf(
                        donations,
                        year.getYearValue()
                );

        return createPdfResponse(
                pdf,
                "donations-" + year.getYearValue() + ".pdf"
        );
    }

    /*
     * =========================================================
     * ONE DONATION FROM ONE YEAR
     * =========================================================
     */

    @GetMapping("/years/{yearId}/donations/{donationId}")
    public ResponseEntity<InputStreamResource> exportSingleDonation(
            @PathVariable Long yearId,
            @PathVariable Long donationId
    ) {
        Years year = yearsService.findEntityById(yearId);

        DonationResponseDto donation =
                donationService.findById(donationId);

        validateDonationYear(donation, yearId);

        ByteArrayInputStream pdf =
                pdfExportService.exportSingleDonationPdf(
                        donation,
                        year.getYearValue()
                );

        return createPdfResponse(
                pdf,
                "donation-" + donationId
                        + "-" + year.getYearValue()
                        + ".pdf"
        );
    }

    /*
     * =========================================================
     * ALL EVENTS FOR ONE YEAR
     * =========================================================
     */

    @GetMapping("/years/{yearId}/events")
    public ResponseEntity<InputStreamResource> exportEventsByYear(
            @PathVariable Long yearId
    ) {
        Years year = yearsService.findEntityById(yearId);

        List<EventResponseDto> events = eventService.listAll()
                .stream()
                .filter(event -> yearId.equals(event.getYearId()))
                .toList();

        ByteArrayInputStream pdf =
                pdfExportService.exportEventsPdf(
                        events,
                        year.getYearValue()
                );

        return createPdfResponse(
                pdf,
                "events-" + year.getYearValue() + ".pdf"
        );
    }

    /*
     * =========================================================
     * ONE EVENT FROM ONE YEAR
     * =========================================================
     */

    @GetMapping("/years/{yearId}/events/{eventId}")
    public ResponseEntity<InputStreamResource> exportSingleEvent(
            @PathVariable Long yearId,
            @PathVariable Long eventId
    ) {
        Years year = yearsService.findEntityById(yearId);

        EventResponseDto event =
                eventService.findById(eventId);

        validateEventYear(event, yearId);

        ByteArrayInputStream pdf =
                pdfExportService.exportSingleEventPdf(
                        event,
                        year.getYearValue()
                );

        return createPdfResponse(
                pdf,
                "event-" + eventId
                        + "-" + year.getYearValue()
                        + ".pdf"
        );
    }

    /*
     * =========================================================
     * ALL PROJECTS FOR ONE YEAR
     * =========================================================
     */

    @GetMapping("/years/{yearId}/projects")
    public ResponseEntity<InputStreamResource> exportProjectsByYear(
            @PathVariable Long yearId
    ) {
        Years year = yearsService.findEntityById(yearId);

        List<ProjectResponseDto> projects = projectService.listAll()
                .stream()
                .filter(project -> yearId.equals(project.getYearId()))
                .toList();

        ByteArrayInputStream pdf =
                pdfExportService.exportProjectsPdf(
                        projects,
                        year.getYearValue()
                );

        return createPdfResponse(
                pdf,
                "projects-" + year.getYearValue() + ".pdf"
        );
    }

    /*
     * =========================================================
     * ONE PROJECT FROM ONE YEAR
     * =========================================================
     */

    @GetMapping("/years/{yearId}/projects/{projectId}")
    public ResponseEntity<InputStreamResource> exportSingleProject(
            @PathVariable Long yearId,
            @PathVariable Long projectId
    ) {
        Years year = yearsService.findEntityById(yearId);

        ProjectResponseDto project =
                projectService.findById(projectId);

        validateProjectYear(project, yearId);

        ByteArrayInputStream pdf =
                pdfExportService.exportSingleProjectPdf(
                        project,
                        year.getYearValue()
                );

        return createPdfResponse(
                pdf,
                "project-" + projectId
                        + "-" + year.getYearValue()
                        + ".pdf"
        );
    }

    /*
     * =========================================================
     * VALIDATION
     * Prevents exporting a donation, event or project using
     * the wrong year in the URL.
     * =========================================================
     */

    private void validateDonationYear(
            DonationResponseDto donation,
            Long yearId
    ) {
        if (donation.getYearId() == null
                || !yearId.equals(donation.getYearId())) {
            throw new IllegalArgumentException(
                    "Donation does not belong to the selected year."
            );
        }
    }

    private void validateEventYear(
            EventResponseDto event,
            Long yearId
    ) {
        if (event.getYearId() == null
                || !yearId.equals(event.getYearId())) {
            throw new IllegalArgumentException(
                    "Event does not belong to the selected year."
            );
        }
    }

    private void validateProjectYear(
            ProjectResponseDto project,
            Long yearId
    ) {
        if (project.getYearId() == null
                || !yearId.equals(project.getYearId())) {
            throw new IllegalArgumentException(
                    "Project does not belong to the selected year."
            );
        }
    }

    /*
     * =========================================================
     * PDF RESPONSE
     * =========================================================
     */

    private ResponseEntity<InputStreamResource> createPdfResponse(
            ByteArrayInputStream pdf,
            String filename
    ) {
        HttpHeaders headers = new HttpHeaders();

        headers.add(
                HttpHeaders.CONTENT_DISPOSITION,
                "attachment; filename=\"" + filename + "\""
        );

        return ResponseEntity.ok()
                .headers(headers)
                .contentType(MediaType.APPLICATION_PDF)
                .body(new InputStreamResource(pdf));
    }
}