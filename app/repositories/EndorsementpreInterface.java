package repositories;

import models.Endorsement;
import models.EndorsementPre;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

public interface EndorsementpreInterface extends JpaRepository<EndorsementPre,Long>,JpaSpecificationExecutor<EndorsementPre>{

}
