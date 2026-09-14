package emd.charitymanagementsystem;

import emd.charitymanagementsystem.Service.Implementation.BudgetWarningService;
import emd.charitymanagementsystem.Repository.YearsRepository;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class BudgetWarningTests {
    @Test void thresholdsAndRemainingAmounts() {
        assertEquals("normal", BudgetWarningService.usage(2026, 100.0, 79.99).level());
        assertEquals("warning", BudgetWarningService.usage(2026, 100.0, 80).level());
        assertEquals("warning", BudgetWarningService.usage(2026, 100.0, 99.99).level());
        var atLimit = BudgetWarningService.usage(2026, 100.0, 100);
        assertEquals("danger", atLimit.level());
        assertEquals(new BigDecimal("100.00"), atLimit.percentage());
        assertEquals(0, atLimit.remaining().signum());
        var over = BudgetWarningService.usage(2026, 100.0, 125);
        assertEquals("danger", over.level()); assertTrue(over.exceeded());
        assertEquals(0, new BigDecimal("25").compareTo(over.amount()));
    }

    @Test void missingZeroAndNegativeBudgetsDoNotDivide() {
        for (Double budget : new Double[]{null, 0.0, -1.0}) {
            assertNull(BudgetWarningService.usage(2026, budget, 10).percentage());
            assertEquals("unset", BudgetWarningService.usage(2026, budget, 10).level());
            assertEquals("normal", BudgetWarningService.usage(2026, budget, 0).level());
        }
    }

    @Test void filtersGroupedYearTotalsWithoutLoadingDetails() {
        YearsRepository years = mock(YearsRepository.class);
        var high = mock(YearsRepository.BudgetUsage.class);
        when(high.getYearValue()).thenReturn(2025); when(high.getBudgetAmount()).thenReturn(100.0);
        when(high.getSpent()).thenReturn(80.0);
        var low = mock(YearsRepository.BudgetUsage.class);
        when(low.getYearValue()).thenReturn(2026); when(low.getBudgetAmount()).thenReturn(100.0);
        when(low.getSpent()).thenReturn(20.0);
        when(years.dashboardBudgetUsage()).thenReturn(List.of(high, low));
        var warnings = new BudgetWarningService(years).warnings();
        assertEquals(1, warnings.size()); assertEquals(2025, warnings.get(0).year());
        verify(years).dashboardBudgetUsage(); verifyNoMoreInteractions(years);
    }
}
