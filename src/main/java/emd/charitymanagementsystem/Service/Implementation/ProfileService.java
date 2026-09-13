package emd.charitymanagementsystem.Service.Implementation;

import emd.charitymanagementsystem.DTO.profile.ProfileFormDto;
import emd.charitymanagementsystem.Models.UserAccount;
import emd.charitymanagementsystem.Repository.UserAccountRepository;
import emd.charitymanagementsystem.Repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class ProfileService {
    private final UserAccountRepository accounts;
    private final MemberRepository members;

    @Transactional(readOnly=true)
    public UserAccount account(String authenticatedEmail) {
        return accounts.findByEmailIgnoreCase(authenticatedEmail)
                .orElseThrow(() -> new AccessDeniedException("Your account could not be found."));
    }

    @Transactional(readOnly=true)
    public ProfileFormDto form(String authenticatedEmail) {
        var member = account(authenticatedEmail).getMember();
        if (member == null) throw new IllegalArgumentException("This account does not have a linked member profile. Contact your organization head.");
        var form = new ProfileFormDto();
        form.setName(member.getName()); form.setSurname(member.getSurname());
        form.setEmail(member.getEmail()); form.setPhone(member.getPhone());
        form.setCountry(member.getCountry()); form.setCity(member.getCity());
        return form;
    }

    @Transactional
    public boolean update(String authenticatedEmail, ProfileFormDto form) {
        var account = account(authenticatedEmail);
        var member = account.getMember();
        if (member == null) throw new IllegalArgumentException("This account does not have a linked member profile.");
        String email = form.getEmail().toLowerCase(Locale.ROOT);
        if (accounts.existsByEmailIgnoreCaseAndIdNot(email, account.getId()) ||
                members.existsByEmailIgnoreCaseAndIdNot(email, member.getId())) {
            throw new IllegalArgumentException("This email address is already in use. Choose another address.");
        }
        boolean emailChanged = !account.getEmail().equalsIgnoreCase(email);
        member.setName(form.getName().trim()); member.setSurname(form.getSurname().trim());
        member.setEmail(email); member.setPhone(form.getPhone().trim());
        member.setCountry(form.getCountry().trim()); member.setCity(form.getCity().trim());
        account.setName(member.getName() + " " + member.getSurname()); account.setEmail(email);
        // Identity, permissions, yearly assignment, and credentials are never bound from this form.
        accounts.flush();
        return emailChanged;
    }
}
