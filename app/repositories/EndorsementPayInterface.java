package repositories;

import java.util.List;

import models.EndorsementPayLog;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

public interface EndorsementPayInterface extends JpaRepository<EndorsementPayLog,Long>,JpaSpecificationExecutor<EndorsementPayLog>{
	
	//根据日期选择代言收入列表
	@Query(value="select * from endorsement_pay_log where substr(createTime,1,10)=?1",nativeQuery=true)
	public List<EndorsementPayLog> findAllBydate(String datetime);
	
	
}
