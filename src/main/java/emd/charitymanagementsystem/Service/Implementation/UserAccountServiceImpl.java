package emd.charitymanagementsystem.Service.Implementation;

import emd.charitymanagementsystem.DTO.auth.RegistrationDto;
import emd.charitymanagementsystem.Models.Member;
import emd.charitymanagementsystem.Models.Role;
import emd.charitymanagementsystem.Models.UserAccount;
import emd.charitymanagementsystem.Repository.MemberRepository;
import emd.charitymanagementsystem.Repository.UserAccountRepository;
import emd.charitymanagementsystem.Service.UserAccountService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserAccountServiceImpl implements UserAccountService {

    private final UserAccountRepository userAccountRepository;
    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public UserAccount register(RegistrationDto registrationDto) {

        String email = registrationDto.getEmail().trim().toLowerCase();

        if (userAccountRepository.existsByEmailIgnoreCase(email)) {
            throw new IllegalArgumentException(
                    "An account with this email already exists."
            );
        }

        if (!registrationDto.getPassword()
                .equals(registrationDto.getConfirmPassword())) {
            throw new IllegalArgumentException(
                    "Passwords do not match."
            );
        }

        String encodedPassword =
                passwordEncoder.encode(registrationDto.getPassword());

        UserAccount userAccount = UserAccount.builder()
                .name(registrationDto.getName() + " " + registrationDto.getSurname())
                .email(email)
                .password(encodedPassword)
                .role(Role.MEMBER)
                .enabled(true)
                .build();

        UserAccount savedUserAccount =
                userAccountRepository.save(userAccount);

        Member member = Member.builder()
                .name(registrationDto.getName())
                .surname(registrationDto.getSurname())
                .country(registrationDto.getCountry())
                .city(registrationDto.getCity())
                .phone(registrationDto.getPhone())
                .email(email)
                .password(encodedPassword)
                .role(Role.MEMBER)
                .userAccount(savedUserAccount)
                .build();

        Member savedMember = memberRepository.save(member);

        savedUserAccount.setMember(savedMember);

        return savedUserAccount;
    }

    @Override
    public List<UserAccount> findAll() {
        return userAccountRepository.findAll();
    }

    @Override
    public UserAccount findById(Long id) {
        return userAccountRepository.findById(id)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "User account not found with ID: " + id
                        )
                );
    }

    @Override
    @Transactional
    public void updateRole(Long userId, Role role) {
        UserAccount userAccount = findById(userId);

        userAccount.setRole(role);

        if (userAccount.getMember() != null) {
            userAccount.getMember().setRole(role);
        }

        userAccountRepository.save(userAccount);
    }
}