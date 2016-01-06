package repositories;

import java.util.List;

import javax.inject.Named;
import javax.inject.Singleton;

import models.Coupon;
import models.Parcels;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

/**
 * 优惠券相关
 * @author luobotao
 * Date: 2015年4月20日 下午2:40:53
 */
@Named
@Singleton
public interface CouponRepository extends JpaRepository<Coupon, Long>,JpaSpecificationExecutor<Coupon> {


	@Query(value="SELECT c.* FROM coupon c,coupon_user u WHERE c.id=u.couponId AND u.id=?1 and c.states=?2 and u.uid=?3",nativeQuery=true)
	List<Coupon> getCouponListByUseId(String couponUid, String state, String uid);
	
	@Query(value="SELECT c.* FROM coupon c,coupon_user u WHERE c.id=u.couponId and u.id=?1",nativeQuery=true)
	List<Coupon> getByUserId(Integer couponUid);
	
	@Query(value="SELECT * FROM coupon WHERE id=?1",nativeQuery=true)
	List<Coupon> getCouponListById(String couponId);

}