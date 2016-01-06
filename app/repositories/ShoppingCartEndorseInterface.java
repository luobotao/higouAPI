package repositories;

import java.util.List;

import models.ShoppingCartEndorse;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface ShoppingCartEndorseInterface extends JpaRepository<ShoppingCartEndorse, Long>,JpaSpecificationExecutor<ShoppingCartEndorse> {

	@Query(value="SELECT sum(counts) FROM shopping_Cart_endorse where openid= ?1 AND pid =?2 AND eid=?3 ",nativeQuery=true)
	public Integer getTotalNumByopenIdAndPId(String openid, Long pId,Long eid);
	
	@Query(value="SELECT sum(counts) FROM shopping_Cart_endorse where openid= ?1 AND pid =?2 ",nativeQuery=true)
	public Integer getTotalNumByopenIdAndPId(String openid, Long pId);
	
	@Query(value="SELECT sum(counts) FROM shopping_Cart_endorse where openid= ?1 ",nativeQuery=true)
	public Integer getTotalNumByopenId(String openid);
	
	/**
	 * 根据用户ID获取该用户下的某商品购物车列表
	 * @param uId
	 * @return
	 */
	public ShoppingCartEndorse findByOpenidAndPId(String openid, Long pId);
	
	/**
	 * 根据用户ID获取该用户下的购物车列表
	 * @param uId
	 * @return
	 */
	public List<ShoppingCartEndorse> findByOpenid(String openid);
	
	@Modifying
	@Query(value="update shopping_Cart_endorse set counts=?3 where openid=?1 and pid=?2",nativeQuery=true)
	public void updateShoppingCart(String openid, String pid, Long nstock);

}
