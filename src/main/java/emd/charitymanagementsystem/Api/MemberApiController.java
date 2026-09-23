package emd.charitymanagementsystem.Api;

import emd.charitymanagementsystem.DTO.member.*;
import emd.charitymanagementsystem.Models.Role;
import emd.charitymanagementsystem.Service.MemberService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@RestController
@RequestMapping("/api/members")
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MemberApiController {
    private final MemberService service;
    private final ApiScope scope;

    @GetMapping
    @PreAuthorize("hasAnyRole('HEAD','SUBHEAD','TREASURER','MEMBER')")
    public ApiPage<MemberResponseDto> list(@RequestParam(required = false) String search,
                                           @RequestParam(required = false) String country, @RequestParam(required = false) String city,
                                           @RequestParam(required = false) Role role, @RequestParam(defaultValue = "0") int page,
                                           @RequestParam(defaultValue = "20") int size) {
        if (page < 0 || page > 1000000 || size < 1 || size > 100)
            throw new IllegalArgumentException("Invalid page or size (1–100).");
        return ApiPage.of(service.findPage(search, country, city, role, page + 1, size));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('HEAD','SUBHEAD','TREASURER','MEMBER')")
    public MemberResponseDto get(@PathVariable Long id) {
        scope.member(id);
        return service.findById(id);
    }

    @PostMapping
    @Transactional
    @PreAuthorize("hasAnyRole('HEAD','SUBHEAD')")
    public ResponseEntity<MemberResponseDto> create(@Valid @RequestBody MemberFormDto form) {
        if (form.getYearId() != null) scope.year(form.getYearId());
        form.setId(null);
        var result = service.create(form);
        return ResponseEntity.created(URI.create("/api/members/" + result.getId())).body(result);
    }

    @PutMapping("/{id}")
    @Transactional
    @PreAuthorize("hasRole('HEAD')")
    public MemberResponseDto update(@PathVariable Long id, @Valid @RequestBody MemberFormDto form, Authentication auth) {
        scope.member(id);
        if (form.getYearId() != null) scope.year(form.getYearId());
        form.setId(id);
        return service.update(id, form, auth.getName());
    }

    @DeleteMapping("/{id}")
    @Transactional
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('HEAD')")
    public void delete(@PathVariable Long id) {
        scope.member(id);
        service.delete(id);
    }
}
