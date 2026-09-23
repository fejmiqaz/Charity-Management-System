package emd.charitymanagementsystem.Api;

import emd.charitymanagementsystem.Models.MembershipPayment;
import emd.charitymanagementsystem.Models.Currency;
import emd.charitymanagementsystem.Service.Implementation.MembershipService;
import emd.charitymanagementsystem.Repository.MembershipPaymentRepository;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.*;
import java.util.*;

@RestController
@RequestMapping("/api/memberships")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('HEAD','SUBHEAD','TREASURER')")
public class MembershipApiController {
    private final MembershipService service;
    private final MembershipPaymentRepository payments;
    private final ApiScope scope;

    public record Receipt(Long id, Long memberId, String memberName, Integer membershipYear, BigDecimal amount,
                          Currency currency, LocalDate paidOn, boolean paid, Instant voidedAt, String voidReason) {
        static Receipt of(MembershipPayment p) {
            return new Receipt(p.getId(), p.getMemberId(), p.getMemberName(), p.getMembershipYear(),
                    p.getAmount(), p.getCurrency(), p.getPaidOn(), p.isPaid(), p.getVoidedAt(), p.getVoidReason());
        }
    }

    public record Ledger(int year, BigDecimal fee, Map<Currency, BigDecimal> totals, List<Receipt> payments) {
    }

    public record PaymentForm(@NotNull Long memberId, @NotNull @Min(1900) @Max(2200) Integer year,
                              @Positive @Digits(integer = 10, fraction = 2) BigDecimal amount,
                              @NotNull Currency currency, @NotNull @PastOrPresent LocalDate paidOn) {
    }

    public record FeeForm(@NotNull @Positive @Digits(integer = 10, fraction = 2) BigDecimal amount) {
    }

    public record VoidForm(@NotBlank @Size(max = 500) String reason) {
    }

    @GetMapping
    public Ledger list(@RequestParam(required = false) Integer year) {
        int selected = service.checkedYear(year);
        return new Ledger(selected, service.fee(selected), service.paymentTotals(selected), service.payments(selected).stream().map(Receipt::of).toList());
    }

    @GetMapping("/members/{memberId}")
    public List<Receipt> history(@PathVariable Long memberId) {
        return service.history(memberId).stream().map(Receipt::of).toList();
    }

    @PostMapping("/payments")
    @ResponseStatus(HttpStatus.CREATED)
    public Receipt record(@Valid @RequestBody PaymentForm form, Authentication auth) {
        scope.member(form.memberId());
        return Receipt.of(service.record(form.memberId(), form.year(), form.amount() == null ? service.fee(form.year()) : form.amount(), form.currency(), form.paidOn(), auth.getName()));
    }

    @PutMapping("/{year}/fee")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void fee(@PathVariable int year, @Valid @RequestBody FeeForm form) {
        service.setFee(year, form.amount());
    }

    @PostMapping("/payments/{id}/void")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void voidPayment(@PathVariable Long id, @Valid @RequestBody VoidForm form, Authentication auth) {
        if (!payments.existsById(id)) throw ApiScope.missing();
        service.voidPayment(id, form.reason(), auth.getName());
    }
}
