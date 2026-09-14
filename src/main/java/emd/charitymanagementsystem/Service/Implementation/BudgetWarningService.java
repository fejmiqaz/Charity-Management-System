package emd.charitymanagementsystem.Service.Implementation;

import emd.charitymanagementsystem.Repository.YearsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BudgetWarningService {
    private final YearsRepository years;

    public List<Warning> warnings() {
        return years.dashboardBudgetUsage().stream().map(year -> usage(year.getYearValue(),
                        year.getBudgetAmount(), year.getSpent()))
                .filter(warning -> !warning.level().equals("normal")).toList();
    }

    public static Warning usage(Integer year, Double budget, double spent) {
        BigDecimal spending = BigDecimal.valueOf(spent);
        if (budget == null || budget <= 0) {
            return new Warning(year, spent > 0 ? "unset" : "normal", null, spending);
        }
        BigDecimal allocated = BigDecimal.valueOf(budget);
        String level = spending.compareTo(allocated) >= 0 ? "danger"
                : spending.compareTo(allocated.multiply(new BigDecimal("0.80"))) >= 0 ? "warning" : "normal";
        BigDecimal percentage = spending.multiply(BigDecimal.valueOf(100)).divide(allocated, 2, RoundingMode.HALF_UP);
        return new Warning(year, level, percentage, allocated.subtract(spending));
    }

    public record Warning(Integer year, String level, BigDecimal percentage, BigDecimal remaining) {
        public BigDecimal amount() { return remaining.abs(); }
        public boolean exceeded() { return remaining.signum() < 0; }
    }
}
