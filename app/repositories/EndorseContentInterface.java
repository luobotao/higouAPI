package repositories;

import models.EndorsementContent;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

public interface EndorseContentInterface extends JpaRepository<EndorsementContent,Integer>,JpaSpecificationExecutor<EndorsementContent>{

	//获取随机方案内容
	@Query(value="SELECT * FROM endorsement_content ORDER BY RAND() LIMIT 1",nativeQuery=true)
	public EndorsementContent getRanContent();
}
