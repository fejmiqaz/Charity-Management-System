package emd.charitymanagementsystem.Mapper;

import emd.charitymanagementsystem.DTO.member.MemberFormDto;
import emd.charitymanagementsystem.DTO.member.MemberResponseDto;
import emd.charitymanagementsystem.Models.Member;

public class MemberMapper {
    public static MemberResponseDto toDto(Member member){
        return new MemberResponseDto(
                member.getId(),
                member.getName(),
                member.getSurname(),
                member.getEmail(),
                member.getPhone(),
                member.getCountry(),
                member.getCity(),
                member.getRole(),
                member.getYear() != null ? member.getYear().getId() : null,
                member.getYear() != null ? member.getYear().getYearValue() : null
        );
    }

    public static MemberFormDto toFormDto(Member member){
        return new MemberFormDto(
                member.getId(),
                member.getName(),
                member.getSurname(),
                member.getEmail(),
                "",
                member.getPhone(),
                member.getCountry(),
                member.getCity(),
                member.getRole(),
                member.getYear() != null ? member.getYear().getId() : null
        );
    }
}