package emd.charitymanagementsystem.Mapper;

import emd.charitymanagementsystem.DTO.years.YearsDetailsDto;
import emd.charitymanagementsystem.DTO.years.YearsFormDto;
import emd.charitymanagementsystem.DTO.years.YearsResponseDto;
import emd.charitymanagementsystem.Models.Years;

public class YearsMapper {

    public static YearsResponseDto toResponseDto(Years year) {
        return new YearsResponseDto(
                year.getId(),
                year.getYearValue()
        );
    }

    public static YearsFormDto toFormDto(Years year) {
        return new YearsFormDto(
                year.getId(),
                year.getYearValue()
        );
    }

    public static YearsDetailsDto toDetailsDto(Years year) {
        double totalDonationsAmount = year.getDonations() != null
                ? year.getDonations().stream()
                .mapToDouble(d -> d.getDonationAmount() != null ? d.getDonationAmount() : 0.0)
                .sum()
                : 0.0;

        double totalProjectsAmount = year.getProjects() != null
                ? year.getProjects().stream()
                .mapToDouble(p -> p.getProjectPrice() != null ? p.getProjectPrice() : 0.0)
                .sum()
                : 0.0;

        return new YearsDetailsDto(
                year.getId(),
                year.getYearValue(),
                year.getBudget() != null ? year.getBudget().getId() : null,
                year.getBudget() != null ? year.getBudget().getBudgetAmount() : null,
                year.getProjects() != null ? year.getProjects().size() : 0,
                year.getDonations() != null ? year.getDonations().size() : 0,
                year.getMembers() != null ? year.getMembers().size() : 0,
                year.getEvents() != null ? year.getEvents().size() : 0,
                totalDonationsAmount,
                totalProjectsAmount
        );
    }
}
