package emd.charitymanagementsystem.Service;

import emd.charitymanagementsystem.DTO.donation.DonationFormDto;
import emd.charitymanagementsystem.DTO.donation.DonationResponseDto;
import emd.charitymanagementsystem.Models.Donation;
import org.jspecify.annotations.Nullable;

import java.util.List;

public interface DonationService {
    List<DonationResponseDto> listAll();
    DonationResponseDto findById(Long id);
    DonationFormDto findByIdForEdit(Long id);
    DonationResponseDto create(DonationFormDto donationFormDto);
    DonationResponseDto update(Long id, DonationFormDto donationFormDto);
    void delete(Long id);

    @Nullable List<DonationResponseDto> findByYearId(Long yearId);
    Double totalDonationsAmount(Long yearId);
    long donationsCount();
    double getTotalDonations();
}
