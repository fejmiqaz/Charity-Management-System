package emd.charitymanagementsystem.Repository;

import emd.charitymanagementsystem.DTO.budget.BudgetFormDto;
import emd.charitymanagementsystem.DTO.budget.BudgetResponseDto;
import emd.charitymanagementsystem.Models.Budget;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;


@Repository
public interface BudgetRepository extends JpaRepository<Budget, Long>, JpaSpecificationExecutor<Budget> {
    @org.springframework.data.jpa.repository.Query("select coalesce(sum(b.budgetAmount), 0.0) from Budget b")
    double totalAmount();
    Budget findByYearId(Long id);
    java.util.List<Budget> findByMembers_Id(Long memberId);
}
