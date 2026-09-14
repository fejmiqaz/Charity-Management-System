package emd.charitymanagementsystem.Repository;

import emd.charitymanagementsystem.Models.Event;
import emd.charitymanagementsystem.Models.Project;
import emd.charitymanagementsystem.Models.Years;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProjectRepository extends JpaRepository<Project, Long>, JpaSpecificationExecutor<Project> {
    @org.springframework.data.jpa.repository.Query("select coalesce(sum(p.projectPrice), 0.0) from Project p")
    double totalCost();
    @org.springframework.data.jpa.repository.Query("""
            select new emd.charitymanagementsystem.DTO.project.PublicProjectDto(p.name, y.yearValue)
            from Project p left join p.year y
            where p.publicImpact = true and p.status = :status
            order by y.yearValue desc, p.name asc
            """)
    List<emd.charitymanagementsystem.DTO.project.PublicProjectDto> findPublicImpactProjects(
            @org.springframework.data.repository.query.Param("status") emd.charitymanagementsystem.Models.ProjectStatus status);
    List<Project> findByYearId(Long yearId);
    List<Project> findByMembers_Id(Long memberId);

    Long year(Years year);
}
