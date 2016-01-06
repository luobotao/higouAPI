package repositories;


import java.util.List;

import javax.inject.Named;
import javax.inject.Singleton;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import models.ChinaAddress;
import models.ErpAddress;

/**
 * Provides CRUD functionality for accessing people. Spring Data auto-magically
 * takes care of many standard operations here.
 */
@Named
@Singleton
public interface ChinaAddressRepository extends JpaRepository<ChinaAddress, Integer>,JpaSpecificationExecutor<ChinaAddress> {

	@Query(value="SELECT * FROM chinaAddress WHERE ?1 LIKE CONCAT(NAME,'%') ORDER BY tier,id ASC  LIMIT 100",nativeQuery=true)
	List<ErpAddress> getErpAddressWithProvince(String province);

	
}