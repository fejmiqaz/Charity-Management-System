package emd.charitymanagementsystem.Service.Implementation;

import emd.charitymanagementsystem.DTO.budget.BudgetFormDto;
import emd.charitymanagementsystem.DTO.budget.BudgetResponseDto;
import emd.charitymanagementsystem.DTO.donation.DonationResponseDto;
import emd.charitymanagementsystem.Mapper.BudgetMapper;
import emd.charitymanagementsystem.Models.*;
import emd.charitymanagementsystem.Repository.BudgetRepository;
import emd.charitymanagementsystem.Repository.DonationRepository;
import emd.charitymanagementsystem.Repository.MemberRepository;
import emd.charitymanagementsystem.Repository.YearsRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import emd.charitymanagementsystem.Service.BudgetService;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

@Service
@AllArgsConstructor
public class BudgetServiceImpl implements BudgetService {

    private final BudgetRepository budgetRepository;
    private final DonationRepository donationRepository;
    private final MemberRepository memberRepository;
    private final YearsRepository yearsRepository;

    @Override
    public List<BudgetResponseDto> listAll() {
        return budgetRepository.findAll().stream()
                .map(BudgetMapper::toDto)
                .toList();
    }

    @Override
    public BudgetResponseDto findById(Long id) {
        Budget budget = budgetRepository.findById(id).orElseThrow();
        return BudgetMapper.toDto(budget);
    }

    @Override
    public BudgetFormDto findByYearId(Long yearId) {
        Budget budget = budgetRepository.findByYearId(yearId);
        return BudgetMapper.toFormDto(budget);
    }

    @Override
    public BudgetResponseDto create(BudgetFormDto budgetFormDto) {
        Years year = yearsRepository.findById(budgetFormDto.getYearId())
                .orElseThrow(() -> new RuntimeException("Year not found with id: " + budgetFormDto.getYearId()));

        List<Donation> donations = new LinkedList<>() {
        };
        if (budgetFormDto.getDonationIds() != null && !budgetFormDto.getDonationIds().isEmpty()) {
            donations = new LinkedList<>(donationRepository.findAllById(budgetFormDto.getDonationIds()));
        }

        List<Member> members = new LinkedList<>();
        if (budgetFormDto.getMemberIds() != null && !budgetFormDto.getMemberIds().isEmpty()) {
            members = new LinkedList<>(memberRepository.findAllById(budgetFormDto.getMemberIds()));
        }

        Budget budget = new Budget();
        budget.setBudgetAmount(budgetFormDto.getBudgetAmount());
        budget.setDescription(budgetFormDto.getDescription());
        budget.setYear(year);
        budget.setDonations(donations);
        budget.setMembers(members);

        Budget saved = budgetRepository.save(budget);
        return BudgetMapper.toDto(saved);
    }

    @Override
    public BudgetResponseDto update(Long id, BudgetFormDto budgetFormDto) {

        Budget budget = budgetRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Budget not found with id: " + id));

        Years year = yearsRepository.findById(budgetFormDto.getYearId())
                .orElseThrow(() -> new RuntimeException("Year not found with id: " + budgetFormDto.getYearId()));

        List<Donation> donations = new LinkedList<>();
        if (budgetFormDto.getDonationIds() != null && !budgetFormDto.getDonationIds().isEmpty()) {
            donations = new LinkedList<>(donationRepository.findAllById(budgetFormDto.getDonationIds()));
        }

        List<Member> members = new LinkedList<>();
        if (budgetFormDto.getMemberIds() != null && !budgetFormDto.getMemberIds().isEmpty()) {
            members = new LinkedList<>(memberRepository.findAllById(budgetFormDto.getMemberIds()));
        }

        budget.setBudgetAmount(budgetFormDto.getBudgetAmount());
        budget.setDescription(budgetFormDto.getDescription());
        budget.setYear(year);
        budget.setDonations(donations);
        budget.setMembers(members);

        Budget updated = budgetRepository.save(budget);
        return BudgetMapper.toDto(updated);
    }

    @Override
    public void delete(Long id) {
        budgetRepository.deleteById(id);
    }

    @Override
    public Double getTotalBudgetAmouunt() {
        return budgetRepository.findAll().stream()
                .map(Budget::getBudgetAmount)
                .filter(java.util.Objects::nonNull)
                .mapToDouble(Double::doubleValue)
                .sum();
    }
}
