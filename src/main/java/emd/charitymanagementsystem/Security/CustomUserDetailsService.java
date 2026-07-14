package emd.charitymanagementsystem.Security;

import emd.charitymanagementsystem.Models.UserAccount;
import emd.charitymanagementsystem.Repository.UserAccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService
        implements UserDetailsService {

    private final UserAccountRepository userAccountRepository;

    @Override
    public UserDetails loadUserByUsername(String email)
            throws UsernameNotFoundException {

        UserAccount account = userAccountRepository
                .findByEmailIgnoreCase(email)
                .orElseThrow(() ->
                        new UsernameNotFoundException(
                                "Account not found."
                        )
                );

        return new User(
                account.getEmail(),
                account.getPassword(),
                account.isEnabled(),
                true,
                true,
                true,
                List.of(
                        new SimpleGrantedAuthority(
                                "ROLE_" + account.getRole().name()
                        )
                )
        );
    }
}