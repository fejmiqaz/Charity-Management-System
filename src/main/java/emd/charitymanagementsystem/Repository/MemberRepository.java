package emd.charitymanagementsystem.Repository;

import emd.charitymanagementsystem.Models.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MemberRepository extends JpaRepository<Member, Long>, JpaSpecificationExecutor<Member> {
    java.util.List<Member> findByYearIdOrderBySurnameAscNameAsc(Long yearId);
    Optional<Member> findByEmail(String email);
    boolean existsByEmailIgnoreCaseAndIdNot(String email, Long id);
}
