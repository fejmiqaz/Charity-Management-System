package emd.charitymanagementsystem.Service.Implementation;

import emd.charitymanagementsystem.DTO.member.MemberFormDto;
import emd.charitymanagementsystem.DTO.member.MemberResponseDto;
import emd.charitymanagementsystem.Mapper.MemberMapper;
import emd.charitymanagementsystem.Models.*;
import emd.charitymanagementsystem.Repository.*;
import emd.charitymanagementsystem.Service.MemberService;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.jspecify.annotations.Nullable;
import org.springframework.security.crypto.password.*;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@AllArgsConstructor
public class MemberServiceImpl implements MemberService {
    private final MemberRepository memberRepository;
    private final YearsRepository yearsRepository;
    private final PasswordEncoder passwordEncoder;
    private final DonationRepository donationRepository;
    private final EventRepository eventRepository;
    private final ProjectRepository projectRepository;

    @Override
    public List<MemberResponseDto> listAll() {
        return memberRepository.findAll()
                .stream()
                .map(MemberMapper::toDto)
                .toList();
    }

    @Override
    public MemberResponseDto findById(Long id) {
        Member member = memberRepository.findById(id).orElseThrow();
        return MemberMapper.toDto(member);
    }

    @Override
    public MemberFormDto findByIdForEdit(Long id) {
        Member member = memberRepository.findById(id).orElseThrow();
        return MemberMapper.toFormDto(member);
    }

    @Override
    public MemberResponseDto create(MemberFormDto memberFormDto) {
        validateMemberFormDto(memberFormDto);

        Years year = null;
        if (memberFormDto.getYearId() != null) {
            year = yearsRepository.findById(memberFormDto.getYearId()).orElseThrow();
        }

        Member member = new Member();
        member.setName(memberFormDto.getName());
        member.setSurname(memberFormDto.getSurname());
        member.setEmail(memberFormDto.getEmail());
        member.setPassword(passwordEncoder.encode(memberFormDto.getPassword()));
        member.setPhone(memberFormDto.getPhone());
        member.setCountry(memberFormDto.getCountry());
        member.setCity(memberFormDto.getCity());
        member.setRole(memberFormDto.getRole());
        member.setYear(year);

        Member saved = memberRepository.save(member);
        return MemberMapper.toDto(saved);
    }

    @Override
    public MemberResponseDto update(Long id, MemberFormDto memberFormDto) {
        validateMemberFormDtoForUpdate(memberFormDto);

        Member memberToUpdate = memberRepository.findById(id).orElseThrow();

        Years year = null;
        if (memberFormDto.getYearId() != null) {
            year = yearsRepository.findById(memberFormDto.getYearId()).orElseThrow();
        }

        memberToUpdate.setName(memberFormDto.getName());
        memberToUpdate.setSurname(memberFormDto.getSurname());
        memberToUpdate.setEmail(memberFormDto.getEmail());
        memberToUpdate.setPhone(memberFormDto.getPhone());
        memberToUpdate.setCountry(memberFormDto.getCountry());
        memberToUpdate.setCity(memberFormDto.getCity());
        memberToUpdate.setRole(memberFormDto.getRole());
        memberToUpdate.setYear(year);

        if (memberFormDto.getPassword() != null && !memberFormDto.getPassword().isBlank()) {
            memberToUpdate.setPassword(passwordEncoder.encode(memberFormDto.getPassword()));
        }

        Member updated = memberRepository.save(memberToUpdate);
        return MemberMapper.toDto(updated);
    }

    @Transactional
    public void delete(Long id) {
        Member member = memberRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Member not found"));

        List<Donation> donations = donationRepository.findByMembers_Id(id);

        for (Donation donation : donations) {
            donation.getMembers().removeIf(m -> m.getId().equals(id));
            donationRepository.save(donation);
        }

        List<Event> events = eventRepository.findByMembers_Id(id);

        for (Event event : events) {
            event.getMembers().removeIf(m -> m.getId().equals(id));
            eventRepository.save(event);
        }

        List<Project> projects = projectRepository.findByMembers_Id(id);

        for (Project project : projects) {
            project.getMembers().removeIf(m -> m.getId().equals(id));
            projectRepository.save(project);
        }

        memberRepository.delete(member);
    }

    @Override
    public long membersCount() {
        return memberRepository.count();
    }

    @Override
    public Member getEntityById(Long id) {
        return memberRepository.findById(id).orElseThrow();
    }

    @Override
    public @Nullable List<MemberResponseDto> findAllByIds(List<Long> memberIds) {
        return memberRepository.findAllById(memberIds).stream()
                .map(MemberMapper::toDto)
                .toList();
    }

    private void validateMemberFormDto(MemberFormDto dto) {
        if (dto.getName() == null || dto.getName().isBlank() ||
                dto.getSurname() == null || dto.getSurname().isBlank() ||
                dto.getEmail() == null || dto.getEmail().isBlank() ||
                dto.getPassword() == null || dto.getPassword().isBlank() ||
                dto.getPhone() == null || dto.getPhone().isBlank() ||
                dto.getCountry() == null || dto.getCountry().isBlank() ||
                dto.getCity() == null || dto.getCity().isBlank() ||
                dto.getRole() == null) {
            throw new IllegalArgumentException("All required fields must be filled");
        }
    }

    private void validateMemberFormDtoForUpdate(MemberFormDto dto) {
        if (dto.getName() == null || dto.getName().isBlank() ||
                dto.getSurname() == null || dto.getSurname().isBlank() ||
                dto.getEmail() == null || dto.getEmail().isBlank() ||
                dto.getPhone() == null || dto.getPhone().isBlank() ||
                dto.getCountry() == null || dto.getCountry().isBlank() ||
                dto.getCity() == null || dto.getCity().isBlank() ||
                dto.getRole() == null) {
            throw new IllegalArgumentException("All required fields must be filled");
        }
    }
}