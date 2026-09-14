package emd.charitymanagementsystem.Repository;

import emd.charitymanagementsystem.Models.Donation;
import org.jspecify.annotations.Nullable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DonationRepository extends JpaRepository<Donation, Long>, JpaSpecificationExecutor<Donation> {
    @org.springframework.data.jpa.repository.Query("select coalesce(sum(d.donationAmount), 0.0) from Donation d")
    double totalAmount();
    @Nullable
    List<Donation> findByYearId(Long yearId);
    List<Donation> findByMembers_Id(Long memberId);
}
