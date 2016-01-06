package repositories;

import java.util.List;

import javax.inject.Named;
import javax.inject.Singleton;

import models.Address;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

/**
 * Provides CRUD functionality for accessing people. Spring Data auto-magically
 * takes care of many standard operations here.
 */
@Named
@Singleton
public interface AddressRepository extends JpaRepository<Address, Long>,JpaSpecificationExecutor<Address> {

	public List<Address> findByUId(Long uId);

	public List<Address> findByUIdAndFlg(Long uId, String flg);

	@Modifying
	@Query(value="update address set flg='0' where uid=?1",nativeQuery=true)
	public void setNotDefaultByUId(Long uId);
	
	@Modifying
	@Query(value="update address set flg='1' where id=?2 and uid=?1",nativeQuery=true)
	public void setdefault(Long uId,Long addressId);
	
	@Modifying
	@Query(value="update address set firstPY = firstPinyin(`name`) where id=?2 and uid=?1",nativeQuery=true)
	public void setfirstPY_CH(Long uId,Long addressId);
	
	@Modifying
	@Query(value="update address set firstPY = UPPER(LEFT(`name`,1)) where id=?2 and uid=?1 and firstPY is null",nativeQuery=true)
	public void setfirstPY_EN(Long uId,Long addressId);
	

	public Address findByUIdAndAddressId(Long uId, Long addressId);
	
	public List<Address> findByUIdAndNameAndPhoneAndProvinceAndAddress(Long uId,String name,String phone,String province,String address);

	public Address findByAddressId(Long addressid);

	public void deleteByUIdAndAddressId(Long uId, Long addressId);

	@Modifying
	@Query(value="update address set cardImg=?2,imgpath=?3 where id=?1",nativeQuery=true)
	public void EditaddressImg(Long addressId,Integer cardImg,String imgpath);
	
}