package emd.charitymanagementsystem.Config;

import emd.charitymanagementsystem.Models.Role;
import emd.charitymanagementsystem.Models.UserAccount;
import emd.charitymanagementsystem.Repository.UserAccountRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DataInitializer {
    private final UserAccountRepository userAccountRepository;
    private final PasswordEncoder passwordEncoder;
    @Value("${app.admin.name}")
    private String adminName;
    @Value("${app.admin.email}")
    private String adminEmail;
    @Value("${app.admin.password}")
    private String adminPassword;

    @PostConstruct
    public void init() {
        createInitialAdminAccount();
    }

    private void createInitialAdminAccount() {
        String normalizedEmail = adminEmail.trim().toLowerCase();
        if (userAccountRepository.existsByEmailIgnoreCase(normalizedEmail)) {
            return;
        }
        UserAccount admin = new UserAccount();
        admin.setName(adminName.trim());
        admin.setEmail(normalizedEmail);
        admin.setPassword(passwordEncoder.encode(adminPassword));
        admin.setRole(Role.HEAD);
        admin.setEnabled(true);
        userAccountRepository.save(admin);
    }
}