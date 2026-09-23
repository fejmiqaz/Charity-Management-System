package emd.charitymanagementsystem.Api;

import emd.charitymanagementsystem.DTO.years.*;
import emd.charitymanagementsystem.Service.YearsService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@RestController
@RequestMapping("/api/years")
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class YearsApiController {
    private final YearsService service;
    private final ApiScope scope;

    @GetMapping
    @PreAuthorize("hasAnyRole('HEAD','SUBHEAD','MEMBER')")
    public ApiPage<YearsResponseDto> list(@RequestParam(required = false) Integer yearValue,
                                          @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int size,
                                          @RequestParam(defaultValue = "desc") String sortDir) {
        return ApiPage.of(service.listAll(yearValue, page, size, sortDir));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('HEAD','SUBHEAD','MEMBER')")
    public YearsDetailsDto get(@PathVariable Long id) {
        scope.year(id);
        return service.findById(id);
    }

    @PostMapping
    @Transactional
    @PreAuthorize("hasRole('HEAD')")
    public ResponseEntity<YearsResponseDto> create(@Valid @RequestBody YearsFormDto form) {
        form.setId(null);
        var result = service.create(form);
        return ResponseEntity.created(URI.create("/api/years/" + result.getId())).body(result);
    }

    @PutMapping("/{id}")
    @Transactional
    @PreAuthorize("hasRole('HEAD')")
    public YearsResponseDto update(@PathVariable Long id, @Valid @RequestBody YearsFormDto form) {
        scope.year(id);
        form.setId(id);
        return service.update(id, form);
    }

    @DeleteMapping("/{id}")
    @Transactional
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('HEAD')")
    public void delete(@PathVariable Long id) {
        scope.year(id);
        service.delete(id);
    }
}
