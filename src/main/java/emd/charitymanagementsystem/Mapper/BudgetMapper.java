package emd.charitymanagementsystem.Mapper;

import emd.charitymanagementsystem.DTO.budget.BudgetFormDto;
import emd.charitymanagementsystem.DTO.budget.BudgetResponseDto;
import emd.charitymanagementsystem.Models.Budget;
import emd.charitymanagementsystem.Models.Donation;
import emd.charitymanagementsystem.Models.Member;

import java.util.List;

public class BudgetMapper {
    public static BudgetResponseDto toDto(Budget budget){
        return new BudgetResponseDto(
                budget.getId(),
                budget.getBudgetAmount(),
                budget.getDescription(),
                budget.getDonations().isEmpty() ? List.of() : budget.getDonations().stream().map(Donation::getId).toList(),
                budget.getDonations().isEmpty() ? List.of() : budget.getDonations().stream().map(donation ->
                        donation.getMembers().isEmpty() ? List.<Long>of() : donation.getMembers().stream().map(Member::getId).toList()).toList(),
                budget.getDonations().isEmpty() ? List.of() : budget.getDonations().stream().map(donation ->
                        donation.getMembers().isEmpty() ? List.<String>of() : donation.getMembers().stream().map(Member::getName).toList()).toList(),
                budget.getYear() != null ? budget.getYear().getId() : null,
                budget.getYear() != null ? budget.getYear().getYearValue() : null,
                budget.getMembers().isEmpty() ? null : budget.getMembers().stream().map(Member::getId).toList(),
                budget.getMembers().isEmpty() ? null : budget.getMembers().stream().map(Member::getName).toList()
        );
    }

    public static BudgetFormDto toFormDto(Budget budget){
        return new BudgetFormDto(
                budget.getId(),
                budget.getBudgetAmount(),
                budget.getDescription(),
                budget.getDonations().isEmpty() ? null : budget.getDonations().stream().map(Donation::getId).toList(),
                budget.getYear() != null ? budget.getYear().getId() : null,
                budget.getMembers().isEmpty() ? null : budget.getMembers().stream().map(Member::getId).toList()
        );
    }
}
