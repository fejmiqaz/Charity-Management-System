package emd.charitymanagementsystem.Service;


import emd.charitymanagementsystem.DTO.member.MemberFormDto;
import emd.charitymanagementsystem.DTO.member.MemberResponseDto;
import emd.charitymanagementsystem.Models.Member;
import org.jspecify.annotations.Nullable;

import java.util.List;

public interface MemberService {
    List<MemberResponseDto> listAll();
    MemberResponseDto findById(Long id);
    MemberFormDto findByIdForEdit(Long id);
    MemberResponseDto create(MemberFormDto memberFormDto);
    MemberResponseDto update(Long id, MemberFormDto memberFormDto);
    void delete(Long id);

    long membersCount();
    Member getEntityById(Long id);

    @Nullable List<MemberResponseDto> findAllByIds(List<Long> memberIds);
}
