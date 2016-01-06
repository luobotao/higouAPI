package repositories;

import java.util.List;

import javax.inject.Named;
import javax.inject.Singleton;

import models.Parcels;
import models.ParcelsPro;
import models.Product;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

/**
 * 包裹商品相关
 * @author luobotao
 * Date: 2015年4月20日 下午2:40:53
 */
@Named
@Singleton
public interface ParcelsProRepository extends JpaRepository<ParcelsPro, Long>,JpaSpecificationExecutor<ParcelsPro> {

	@Query(value="SELECT * FROM pardels_Pro WHERE pardelsId=?1",nativeQuery=true)
	List<ParcelsPro> queryParcelsProListByParcelsId(Long parcelsId);

}