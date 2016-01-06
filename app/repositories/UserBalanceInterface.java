package repositories;

import java.math.BigDecimal;

import models.UserBalance;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface UserBalanceInterface extends JpaRepository<UserBalance,Long>,JpaSpecificationExecutor<UserBalance>{

	//取出用户当前余额
	@Query(value="select * from user_balance where userId=?1",nativeQuery=true)
	public UserBalance getbalance(Long uid);
	
	//修改用户余额
	@Modifying
	@Query(value="update user_balance set balance=balance-?2 where userId=?1",nativeQuery=true)
	public void updateBalance(Long userId,BigDecimal balance);
}
