package emd.charitymanagementsystem.Repository;

import emd.charitymanagementsystem.DTO.budget.BudgetFormDto;
import emd.charitymanagementsystem.DTO.budget.BudgetResponseDto;
import emd.charitymanagementsystem.Models.Budget;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;


@Repository
public interface BudgetRepository extends JpaRepository<Budget, Long>, JpaSpecificationExecutor<Budget> {
    Budget findByYearId(Long id);
}
