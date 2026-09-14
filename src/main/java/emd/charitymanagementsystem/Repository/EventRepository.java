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
    @org.springframework.data.jpa.repository.Query("""
            select new emd.charitymanagementsystem.DTO.event.PublicEventDto(e.purpose, e.date)
            from Event e where e.publicVisible = true and e.date >= :now
            order by e.date asc, e.id asc
            """)
    List<emd.charitymanagementsystem.DTO.event.PublicEventDto> findPublicUpcoming(
            @org.springframework.data.repository.query.Param("now") java.time.LocalDateTime now);

    List<Event> findAllByYearId(Long yearId);
    List<Event> findByMembers_Id(Long memberId);
}
