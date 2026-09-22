package emd.charitymanagementsystem.Web;

import emd.charitymanagementsystem.Service.Implementation.MembershipPdfReportService;
import emd.charitymanagementsystem.Service.Implementation.MembershipService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('HEAD','SUBHEAD','TREASURER')")
public class MembershipReportController {
    private final MembershipService memberships;
    private final MembershipPdfReportService pdf;

    @GetMapping("/memberships/export.pdf")
    public ResponseEntity<byte[]> report(@RequestParam(required = false) Integer year) {
        int selectedYear = memberships.checkedYear(year);
        return ResponseEntity.ok().contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.attachment()
                        .filename("memberships-" + selectedYear + ".pdf").build().toString())
                .cacheControl(CacheControl.noStore()).body(pdf.report(selectedYear));
    }
}
