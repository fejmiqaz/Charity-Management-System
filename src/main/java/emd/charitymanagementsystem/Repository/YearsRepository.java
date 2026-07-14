package emd.charitymanagementsystem.Repository;

import emd.charitymanagementsystem.Models.Years;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface YearsRepository
        extends JpaRepository<Years, Long>, JpaSpecificationExecutor<Years> {
}
