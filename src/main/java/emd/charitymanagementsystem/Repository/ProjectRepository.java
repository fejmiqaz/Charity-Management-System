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
    List<Project> findByYearId(Long yearId);
    List<Project> findByMembers_Id(Long memberId);

    Long year(Years year);
}
