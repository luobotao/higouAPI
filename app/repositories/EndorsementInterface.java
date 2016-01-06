package repositories;

import java.util.List;

import models.Endorsement;
import models.Product;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

public interface EndorsementInterface extends JpaRepository<Endorsement,Long>,JpaSpecificationExecutor<Endorsement>{

	@Modifying
	@Query(value="update endorsementduct set remark=?1,picnums=?3 where eid=?2",nativeQuery=true)
	public void updateEndorse(String content,Long dyid,int picnums);
	
	@Modifying
	@Query(value="update endorsementduct set status=?3 where userId=?1 and eid=?2",nativeQuery=true)
	public void updateEndoseStatus(Long userId,Long eid,int status);
	
	//根据产品编号,用户编号获取代言信息
//	@Query(value="select * from endorsementduct where userId=?1 and productId=?2",nativeQuery=true)
//	public Endorsement getEndorsementInfo(Long userid, Long productid);
	

	@Modifying
	@Query(value="update endorsementduct set status=?2 where eid=?1",nativeQuery=true)
	public void updateEndoseStatus(Long eid,int status);
}
