package emd.charitymanagementsystem.Repository;

import emd.charitymanagementsystem.Models.RegistrationQuota;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.*;

public interface RegistrationQuotaRepository extends JpaRepository<RegistrationQuota, Long> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select q from RegistrationQuota q where q.id = 1")
    RegistrationQuota lockQuota();
}
