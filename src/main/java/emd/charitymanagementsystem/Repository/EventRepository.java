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
    @org.springframework.data.jpa.repository.Query("select e.id from Event e where e.date >= :start and e.date < :end")
    List<Long> findReminderCandidates(java.time.LocalDateTime start, java.time.LocalDateTime end);

    @org.springframework.data.jpa.repository.Lock(jakarta.persistence.LockModeType.PESSIMISTIC_WRITE)
    @org.springframework.data.jpa.repository.Query("select e from Event e where e.id = :id")
    java.util.Optional<Event> findLockedById(Long id);
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
