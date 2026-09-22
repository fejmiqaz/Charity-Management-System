package emd.charitymanagementsystem.Repository;

import emd.charitymanagementsystem.Models.TaskPayment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TaskPaymentRepository extends JpaRepository<TaskPayment, Long> {
    java.util.List<TaskPayment> findByMemberId(Long memberId);
}
