package emd.charitymanagementsystem.Mapper;

import emd.charitymanagementsystem.DTO.donation.DonationFormDto;
import emd.charitymanagementsystem.DTO.donation.DonationResponseDto;
import emd.charitymanagementsystem.Models.Donation;
import emd.charitymanagementsystem.Models.Member;

public class DonationMapper {
    public static DonationResponseDto toDto(Donation donation){
        return new DonationResponseDto(
                donation.getId(),
                donation.getDonationAmount(),
                donation.getYear() != null ? donation.getYear().getId() : null,
                donation.getYear() != null ? donation.getYear().getYearValue() : null,
                donation.getMembers().stream()
                        .map(Member::getId)
                        .toList(),
                donation.getMembers().stream()
                        .map(member -> member.getName() + " " + member.getSurname())
                        .toList()
        );
    }

    public static DonationFormDto toFormDto(Donation donation){
        return new DonationFormDto(
                donation.getId(),
                donation.getDonationAmount(),
                donation.getYear() != null ? donation.getYear().getId() : null,
                donation.getMembers().stream()
                        .map(Member::getId)
                        .toList()
        );
    }
}
