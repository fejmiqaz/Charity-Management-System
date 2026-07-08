package emd.charitymanagementsystem.Repository;

import emd.charitymanagementsystem.DTO.event.EventResponseDto;
import emd.charitymanagementsystem.Models.Donation;
import emd.charitymanagementsystem.Models.Event;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EventRepository extends JpaRepository<Event, Long>, JpaSpecificationExecutor<Event> {

    List<Event> findAllByYearId(Long yearId);
    List<Event> findByMembers_Id(Long memberId);
}
