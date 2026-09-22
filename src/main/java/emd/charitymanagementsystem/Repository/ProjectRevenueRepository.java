package emd.charitymanagementsystem.Repository;

import emd.charitymanagementsystem.Models.ProjectRevenue;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.math.BigDecimal;
import java.util.List;

public interface ProjectRevenueRepository extends JpaRepository<ProjectRevenue, Long> {
    List<ProjectRevenue> findByProjectIdOrderByRevenueMonthDescIdDesc(Long projectId);
    @Query("select coalesce(sum(r.amount), 0) from ProjectRevenue r where r.project.id = :projectId")
    BigDecimal totalForProject(@Param("projectId") Long projectId);
    @Query("select coalesce(sum(r.amount), 0) from ProjectRevenue r") BigDecimal total();
    @Query("select coalesce(sum(r.amount), 0) from ProjectRevenue r where r.currency = emd.charitymanagementsystem.Models.Currency.EUR") BigDecimal totalEur();
    @Query("select coalesce(sum(r.amount), 0) from ProjectRevenue r where r.project.year.id = :yearId")
    BigDecimal totalForYear(@Param("yearId") Long yearId);
}
