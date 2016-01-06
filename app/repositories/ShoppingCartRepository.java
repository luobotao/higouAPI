package repositories;

import java.util.List;

import javax.inject.Named;
import javax.inject.Singleton;

import models.ShoppingCart;

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
public interface ShoppingCartRepository extends JpaRepository<ShoppingCart, Long>,JpaSpecificationExecutor<ShoppingCart> {

	@Query(value="SELECT sum(counts) FROM shopping_Cart where uid= ?1 AND pid =?2",nativeQuery=true)
	public Integer getTotalNumByUIdAndPId(Long uId, Long pId);
	
	@Query(value="SELECT sum(counts) FROM shopping_Cart where uid= ?1 ",nativeQuery=true)
	public Integer getTotalNumByUId(Long uId);

	public ShoppingCart findByUIdAndPId(Long uId, Long pId);
	
	/**
	 * 根据用户ID获取该用户下的购物车列表
	 * @param uId
	 * @return
	 */
	public List<ShoppingCart> findByUId(Long uId);

	@Modifying
	@Query(value="DELETE FROM shopping_Cart WHERE uId=?1 AND pId IN ?2 ",nativeQuery=true)
	public void deleteShoppingCartByPIds(Long uId,List<Long> ids);
	@Modifying
	@Query(value="update shopping_Cart set counts=?3 where uid=?1 and pid=?2",nativeQuery=true)
	public void updateShoppingCart(String uid, String pid, Long nstock);

	@Query(value="SELECT COUNT(*) FROM product WHERE newMantype=1 AND pid IN (SELECT pid FROM shopping_Cart WHERE uid=?1) and pid<>?2",nativeQuery=true)
	public Integer checkShoppingCart_newMan(Long uId,Long pId);
	
	@Query(value="SELECT COUNT(*) FROM product WHERE newMantype=3 AND pid IN (SELECT pid FROM shopping_Cart WHERE uid=?1) AND pid<>?2",nativeQuery=true)
	public Integer checkShoppingCart_newManZero(Long uId,Long pId);
	
	
}