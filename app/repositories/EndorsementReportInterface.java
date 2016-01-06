package repositories;

import models.EndorsementReport;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface EndorsementReportInterface extends JpaRepository<EndorsementReport,Long>,JpaSpecificationExecutor<EndorsementReport>{


}
