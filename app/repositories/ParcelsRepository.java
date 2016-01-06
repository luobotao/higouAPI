package repositories;

import java.util.Date;
import java.util.List;

import javax.inject.Named;
import javax.inject.Singleton;

import models.Parcels;
import models.Product;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

/**
 * 包裹相关
 * @author luobotao
 * Date: 2015年4月20日 下午2:40:53
 */
@Named
@Singleton
public interface ParcelsRepository extends JpaRepository<Parcels, Long>,JpaSpecificationExecutor<Parcels> {

	List<Parcels> findByOrderId(Long orderId);

	Parcels getParcelsByParcelCode(String pardelCode);

	@Query(value="select * from pardels where date_add >= ?1 and mailnum=''",nativeQuery=true)
	List<Parcels> findParcelsWithDateAdd(Date lasttime);

}