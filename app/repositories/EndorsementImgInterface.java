package repositories;

import java.util.List;

import javax.inject.Named;
import javax.inject.Singleton;

import models.EndorsementImg;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

@Named
@Singleton
public interface EndorsementImgInterface extends
		JpaRepository<EndorsementImg, Long>,
		JpaSpecificationExecutor<EndorsementImg> {

	@Query(value = "select x from EndorsementImg x where x.eid=?1")
	List<EndorsementImg> querybyEid(Long eid);
}
