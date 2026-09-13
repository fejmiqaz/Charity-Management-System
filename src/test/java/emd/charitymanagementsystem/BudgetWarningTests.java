package emd.charitymanagementsystem;

import emd.charitymanagementsystem.Service.Implementation.BudgetWarningService;
import emd.charitymanagementsystem.Service.*;
import emd.charitymanagementsystem.DTO.years.*;
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

    @Test void reusesExistingYearAndSpendingCalculations() {
        YearsService years = mock(YearsService.class); ProjectService projects = mock(ProjectService.class);
        when(years.listAll()).thenReturn(List.of(new YearsResponseDto(1L, 2025), new YearsResponseDto(2L, 2026)));
        var details = new YearsDetailsDto(); details.setBudgetAmount(100.0);
        when(years.findById(1L)).thenReturn(details); when(years.findById(2L)).thenReturn(details);
        when(projects.totalProjectCostsByYear(1L)).thenReturn(80.0);
        when(projects.totalProjectCostsByYear(2L)).thenReturn(20.0);
        var warnings = new BudgetWarningService(years, projects).warnings();
        assertEquals(1, warnings.size()); assertEquals(2025, warnings.get(0).year());
        verify(projects).totalProjectCostsByYear(1L); verify(projects).totalProjectCostsByYear(2L);
    }
}
