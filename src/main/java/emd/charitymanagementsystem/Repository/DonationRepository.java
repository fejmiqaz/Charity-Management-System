package emd.charitymanagementsystem.Repository;

import emd.charitymanagementsystem.Models.Donation;
import org.jspecify.annotations.Nullable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DonationRepository extends JpaRepository<Donation, Long>, JpaSpecificationExecutor<Donation> {
    @Nullable
    List<Donation> findByYearId(Long yearId);
    List<Donation> findByMembers_Id(Long memberId);
}
