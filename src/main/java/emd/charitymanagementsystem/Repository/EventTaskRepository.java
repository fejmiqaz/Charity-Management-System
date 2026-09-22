package emd.charitymanagementsystem.Repository;

import emd.charitymanagementsystem.Models.EventTask;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface EventTaskRepository extends JpaRepository<EventTask, Long> {
    List<EventTask> findByEventIdOrderByIdAsc(Long eventId);
    List<EventTask> findByMembers_Id(Long memberId);
}
