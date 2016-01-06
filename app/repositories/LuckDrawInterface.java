package repositories;

import java.math.BigDecimal;
import java.util.List;

import models.LuckDraw;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface LuckDrawInterface extends JpaRepository<LuckDraw,Long>,JpaSpecificationExecutor<LuckDraw>{
	
	//根据手机号，产品编号，UNIONID查询抽奖记录
	@Query(value="select * from luck_draw where pid=?1 and unionid=?2 and phone=?3",nativeQuery=true)
	public List<LuckDraw> getLuckDrawbyall(Long pid,String unionid,String phone);

	//修改手机号
	//修改用户余额
	@Modifying
	@Query(value="update luck_draw set phone=?2 where id=?1",nativeQuery=true)
	public void updateLuckDraw(Long id,String phone);

}
