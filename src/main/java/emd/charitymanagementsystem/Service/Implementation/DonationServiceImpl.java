package emd.charitymanagementsystem.Service.Implementation;

import emd.charitymanagementsystem.DTO.donation.DonationFormDto;
import emd.charitymanagementsystem.DTO.donation.DonationResponseDto;
import emd.charitymanagementsystem.DTO.member.MemberResponseDto;
import emd.charitymanagementsystem.Mapper.DonationMapper;
import emd.charitymanagementsystem.Models.Budget;
import emd.charitymanagementsystem.Models.Donation;
import emd.charitymanagementsystem.Models.Member;
import emd.charitymanagementsystem.Models.Years;
import emd.charitymanagementsystem.Repository.BudgetRepository;
import emd.charitymanagementsystem.Repository.DonationRepository;
import emd.charitymanagementsystem.Repository.MemberRepository;
import emd.charitymanagementsystem.Repository.YearsRepository;
import emd.charitymanagementsystem.Service.DonationService;
import lombok.AllArgsConstructor;
import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@AllArgsConstructor
public class DonationServiceImpl implements DonationService {

    private final DonationRepository donationRepository;
    private final YearsRepository yearsRepository;
    private final BudgetRepository budgetRepository;
    private final MemberRepository memberRepository;

    @Override
    public List<DonationResponseDto> listAll() {
        return donationRepository.findAll()
                .stream()
                .map(DonationMapper::toDto)
                .toList();
    }

    @Override
    public DonationResponseDto findById(Long id) {
        Donation donation = donationRepository.findById(id).orElseThrow();
        return DonationMapper.toDto(donation);
    }

    @Override
    public DonationFormDto findByIdForEdit(Long id) {
        Donation donation = donationRepository.findById(id).orElseThrow();

        return DonationMapper.toFormDto(donation);
    }

    @Override
    public DonationResponseDto create(DonationFormDto donationFormDto) {
        validateDonationFormDto(donationFormDto);

        Years year = yearsRepository.findById(donationFormDto.getYearId())
                .orElseThrow(() -> new RuntimeException("Year not found"));

        Set<Member> selectedMembers = getMembersFromIds(donationFormDto.getMemberIds());

        Donation donation = new Donation();
        donation.setDonationAmount(donationFormDto.getDonationAmount());
        donation.setYear(year);
        donation.setMembers(selectedMembers);

        Donation savedDonation = donationRepository.save(donation);

        Budget budget = year.getBudget();
        if (budget != null) {
            Double currentAmount = budget.getBudgetAmount() != null ? budget.getBudgetAmount() : 0.0;
            Double donationAmount = savedDonation.getDonationAmount() != null ? savedDonation.getDonationAmount() : 0.0;

            budget.setBudgetAmount(currentAmount + donationAmount);
            budgetRepository.save(budget);
        }

        return DonationMapper.toDto(savedDonation);
    }

    @Override
    public DonationResponseDto update(Long id, DonationFormDto donationFormDto) {
        validateDonationFormDto(donationFormDto);

        Donation existingDonation = donationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Donation not found"));

        Years newYear = yearsRepository.findById(donationFormDto.getYearId())
                .orElseThrow(() -> new RuntimeException("Year not found"));

        Set<Member> selectedMembers = getMembersFromIds(donationFormDto.getMemberIds());

        Double oldAmount = existingDonation.getDonationAmount() != null ? existingDonation.getDonationAmount() : 0.0;
        Double newAmount = donationFormDto.getDonationAmount() != null ? donationFormDto.getDonationAmount() : 0.0;

        Years oldYear = existingDonation.getYear();

        // If year changed, subtract old donation from old year's budget first
        if (oldYear != null && !oldYear.getId().equals(newYear.getId())) {
            Budget oldBudget = oldYear.getBudget();
            if (oldBudget != null) {
                Double currentOldBudget = oldBudget.getBudgetAmount() != null ? oldBudget.getBudgetAmount() : 0.0;
                oldBudget.setBudgetAmount(Math.max(0.0, currentOldBudget - oldAmount));
                budgetRepository.save(oldBudget);
            }

            Budget newBudget = newYear.getBudget();
            if (newBudget != null) {
                Double currentNewBudget = newBudget.getBudgetAmount() != null ? newBudget.getBudgetAmount() : 0.0;
                newBudget.setBudgetAmount(currentNewBudget + newAmount);
                budgetRepository.save(newBudget);
            }
        } else {
            // Same year: apply only the difference
            Double difference = newAmount - oldAmount;
            Budget budget = newYear.getBudget();
            if (budget != null) {
                Double currentAmount = budget.getBudgetAmount() != null ? budget.getBudgetAmount() : 0.0;
                budget.setBudgetAmount(Math.max(0.0, currentAmount + difference));
                budgetRepository.save(budget);
            }
        }

        existingDonation.setDonationAmount(newAmount);
        existingDonation.setYear(newYear);
        existingDonation.setMembers(selectedMembers);

        Donation updatedDonation = donationRepository.save(existingDonation);
        return DonationMapper.toDto(updatedDonation);
    }

    @Override
    public void delete(Long id) {
        Donation donation = donationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Donation not found"));

        Years year = donation.getYear();
        if (year != null) {
            Budget budget = year.getBudget();

            if (budget != null) {
                Double currentAmount = budget.getBudgetAmount() != null ? budget.getBudgetAmount() : 0.0;
                Double donationAmount = donation.getDonationAmount() != null ? donation.getDonationAmount() : 0.0;

                double newAmount = currentAmount - donationAmount;
                budget.setBudgetAmount(Math.max(0.0, newAmount));
                budgetRepository.save(budget);
            }
        }

        donationRepository.deleteById(id);
    }

    @Override
    public List<DonationResponseDto> findByYearId(Long yearId) {
        return donationRepository.findByYearId(yearId)
                .stream()
                .map(DonationMapper::toDto)
                .toList();
    }

    @Override
    public Double totalDonationsAmount(Long yearId) {
        return donationRepository.findByYearId(yearId).stream()
                .map(Donation::getDonationAmount)
                .filter(java.util.Objects::nonNull)
                .mapToDouble(Double::doubleValue)
                .sum();
    }

    @Override
    public long donationsCount() {
        return donationRepository.count();
    }

    @Override
    public double getTotalDonations() {
        return donationRepository.findAll().stream()
                .map(Donation::getDonationAmount)
                .filter(java.util.Objects::nonNull)
                .mapToDouble(Double::doubleValue)
                .sum();
    }

    private Set<Member> getMembersFromIds(List<Long> memberIds) {
        Set<Member> selectedMembers = new HashSet<>();

        if (memberIds != null && !memberIds.isEmpty()) {
            for (Long memberId : memberIds) {
                Member member = memberRepository.findById(memberId)
                        .orElseThrow(() -> new RuntimeException("Member not found"));
                selectedMembers.add(member);
            }
        }

        return selectedMembers;
    }

    private void validateDonationFormDto(DonationFormDto donationFormDto) {
        if (donationFormDto.getDonationAmount() == null ||
                donationFormDto.getDonationAmount() <= 0 ||
                donationFormDto.getYearId() == null ||
                donationFormDto.getMemberIds() == null ||
                donationFormDto.getMemberIds().isEmpty()) {
            throw new IllegalArgumentException("Donation amount, year, and at least one member are required");
        }
    }
}