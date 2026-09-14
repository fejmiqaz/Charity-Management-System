package emd.charitymanagementsystem.Repository;

import emd.charitymanagementsystem.Models.Years;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface YearsRepository
        extends JpaRepository<Years, Long>, JpaSpecificationExecutor<Years> {
    // Join only projects so spending cannot be multiplied by donations, members or events.
    @org.springframework.data.jpa.repository.Query("""
            select y.yearValue as yearValue, b.budgetAmount as budgetAmount,
                   coalesce(sum(p.projectPrice), 0.0) as spent
            from Years y left join y.budget b left join y.projects p
            group by y.id, y.yearValue, b.budgetAmount
            order by y.yearValue desc
            """)
    java.util.List<BudgetUsage> dashboardBudgetUsage();

    interface BudgetUsage {
        Integer getYearValue();
        Double getBudgetAmount();
        double getSpent();
    }

    @org.springframework.data.jpa.repository.Query("""
            select new emd.charitymanagementsystem.DTO.years.YearsResponseDto(y.id, y.yearValue)
            from Years y order by y.yearValue desc
            """)
    java.util.List<emd.charitymanagementsystem.DTO.years.YearsResponseDto> dashboardYears();
}
