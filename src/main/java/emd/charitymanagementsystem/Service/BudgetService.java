package emd.charitymanagementsystem.Service;

import emd.charitymanagementsystem.DTO.budget.BudgetFormDto;
import emd.charitymanagementsystem.DTO.budget.BudgetResponseDto;
import emd.charitymanagementsystem.DTO.donation.DonationFormDto;
import emd.charitymanagementsystem.Models.Budget;

import java.util.List;

public interface BudgetService {
    List<BudgetResponseDto> listAll();
    BudgetResponseDto findById(Long id);
    BudgetFormDto findByYearId(Long id);
    BudgetResponseDto create(BudgetFormDto budgetFormDto);
    BudgetResponseDto update(Long id, BudgetFormDto budgetFormDto);
    void delete(Long id);

    Double getTotalBudgetAmouunt();
}
