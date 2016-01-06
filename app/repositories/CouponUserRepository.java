package repositories;

import java.util.List;

import javax.inject.Named;
import javax.inject.Singleton;

import models.Coupon_user;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

/**
 * 优惠券与用户关联相关
 * 
 * @author luobotao Date: 2015年4月20日 下午2:40:53
 */
@Named
@Singleton
public interface CouponUserRepository extends JpaRepository<Coupon_user, Long>,
		JpaSpecificationExecutor<Coupon_user> {

	public List<Coupon_user> findByUid(Long uid);

}