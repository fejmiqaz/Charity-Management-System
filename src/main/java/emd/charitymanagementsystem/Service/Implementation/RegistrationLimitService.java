package emd.charitymanagementsystem.Service.Implementation;

import emd.charitymanagementsystem.Repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.*;

@Service
@RequiredArgsConstructor
public class RegistrationLimitService {
    private final RegistrationQuotaRepository quota;
    private final UserAccountRepository accounts;
    public static final int MAX_ACCOUNTS = 25;

    @Transactional(propagation = Propagation.MANDATORY)
    public void check() {
        quota.lockQuota();
        if (accounts.count() >= MAX_ACCOUNTS)
            throw new IllegalArgumentException("Registration is closed. The organization has reached its limit of 25 accounts.");
    }
}
