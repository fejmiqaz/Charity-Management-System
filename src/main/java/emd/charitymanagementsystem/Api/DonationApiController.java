package emd.charitymanagementsystem.Api;

import emd.charitymanagementsystem.DTO.donation.*;
import emd.charitymanagementsystem.Models.*;
import emd.charitymanagementsystem.Service.DonationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/years/{yearId}/donations")
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DonationApiController {
    private final DonationService service;
    private final ApiScope scope;

    @GetMapping
    @PreAuthorize("hasAnyRole('HEAD','SUBHEAD','TREASURER','MEMBER')")
    public List<DonationResponseDto> list(@PathVariable Long yearId) {
        scope.year(yearId);
        return service.findByYearId(yearId);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('HEAD','SUBHEAD','TREASURER','MEMBER')")
    public DonationResponseDto get(@PathVariable Long yearId, @PathVariable Long id) {
        scope.donation(yearId, id);
        return service.findById(id);
    }

    @PostMapping
    @Transactional
    @PreAuthorize("hasAnyRole('HEAD','SUBHEAD','TREASURER')")
    public ResponseEntity<DonationResponseDto> create(@PathVariable Long yearId, @Valid @RequestBody DonationFormDto form) {
        scope.year(yearId);
        scope.members(form.getMemberIds());
        form.setId(null);
        form.setYearId(yearId);
        var result = service.create(form);
        return ResponseEntity.created(URI.create("/api/years/" + yearId + "/donations/" + result.getId())).body(result);
    }

    @PutMapping("/{id}")
    @Transactional
    @PreAuthorize("hasAnyRole('HEAD','SUBHEAD','TREASURER')")
    public DonationResponseDto update(@PathVariable Long yearId, @PathVariable Long id, @Valid @RequestBody DonationFormDto form) {
        scope.donation(yearId, id);
        scope.members(form.getMemberIds());
        form.setId(id);
        form.setYearId(yearId);
        return service.update(id, form);
    }

    @DeleteMapping("/{id}")
    @Transactional
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasAnyRole('HEAD','SUBHEAD','TREASURER')")
    public void delete(@PathVariable Long yearId, @PathVariable Long id) {
        scope.donation(yearId, id);
        service.delete(id);
    }
}
