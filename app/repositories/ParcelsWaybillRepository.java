package repositories;

import javax.inject.Named;
import javax.inject.Singleton;

import models.ParcelsWaybill;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

/**
 * 包裹运单相关
 * @author luobotao
 * Date: 2015年4月20日 下午2:40:53
 */
@Named
@Singleton
public interface ParcelsWaybillRepository extends JpaRepository<ParcelsWaybill, Long>,JpaSpecificationExecutor<ParcelsWaybill> {

	@Query(value="SELECT * FROM pardels_Waybill WHERE waybillCode=?1 limit 1",nativeQuery=true)
	ParcelsWaybill findByWaybillCode(String mail_no);


}