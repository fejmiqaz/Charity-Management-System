package emd.charitymanagementsystem.Api;

import emd.charitymanagementsystem.DTO.budget.*;
import emd.charitymanagementsystem.Service.BudgetService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.net.URI;

@RestController
@RequestMapping("/api/years/{yearId}/budget")
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BudgetApiController {
    private final BudgetService service;
    private final ApiScope scope;

    @GetMapping
    @PreAuthorize("hasAnyRole('HEAD','SUBHEAD','TREASURER','MEMBER')")
    public BudgetResponseDto get(@PathVariable Long yearId) {
        return service.findById(budgetId(yearId));
    }

    @PostMapping
    @Transactional
    @PreAuthorize("hasAnyRole('HEAD','TREASURER')")
    public ResponseEntity<BudgetResponseDto> create(@PathVariable Long yearId, @Valid @RequestBody BudgetFormDto form) {
        if (scope.year(yearId).getBudget() != null)
            throw new ResponseStatusException(HttpStatus.CONFLICT, "This year already has a budget.");
        prepare(yearId, form);
        form.setId(null);
        return ResponseEntity.created(URI.create("/api/years/" + yearId + "/budget")).body(service.create(form));
    }

    @PutMapping
    @Transactional
    @PreAuthorize("hasAnyRole('HEAD','TREASURER')")
    public BudgetResponseDto update(@PathVariable Long yearId, @Valid @RequestBody BudgetFormDto form) {
        Long id = budgetId(yearId);
        prepare(yearId, form);
        form.setId(id);
        return service.update(id, form);
    }

    @DeleteMapping
    @Transactional
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasAnyRole('HEAD','TREASURER')")
    public void delete(@PathVariable Long yearId) {
        Long id = budgetId(yearId);
        scope.year(yearId).setBudget(null);
        service.delete(id);
    }

    private Long budgetId(Long yearId) {
        var budget = scope.year(yearId).getBudget();
        if (budget == null) throw ApiScope.missing();
        return budget.getId();
    }

    private void prepare(Long yearId, BudgetFormDto form) {
        scope.members(form.getMemberIds());
        if (form.getDonationIds() != null) for (Long id : form.getDonationIds()) {
            if (id == null) throw new IllegalArgumentException("Donation IDs cannot be null.");
            scope.donation(yearId, id);
        }
        form.setYearId(yearId);
    }
}
