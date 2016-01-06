package repositories;

import java.util.Date;
import java.util.List;

import javax.inject.Named;
import javax.inject.Singleton;

import models.Product;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

/**
 * 商品相关
 * @author luobotao
 * Date: 2015年4月17日 下午2:21:17
 */
@Named
@Singleton
public interface ProductRepository extends JpaRepository<Product, Long>,JpaSpecificationExecutor<Product> {

	@Query(value="SELECT * FROM product WHERE pid IN(SELECT pid FROM pardels_Pro WHERE pardelsId=?1)",nativeQuery=true)
	List<Product> queryProductListByParcelsId(Long parcelsId);
	
	@Query(value="SELECT * FROM product WHERE pid IN(SELECT pid FROM shopping_Order_Pro WHERE orderId = (SELECT id FROM shopping_Order WHERE OrderCode=?1))",nativeQuery=true)
	List<Product> getproductListByOrderCode(String orderCode);
	
	@Query(value="SELECT counts FROM pardels_Pro WHERE pardelsId = ?1 AND pid =?2",nativeQuery=true)
	public int queryProductCountsInparcel(Long parcelsId, Long pid);
	@Query(value="SELECT price FROM pardels_Pro WHERE pardelsId = ?1 AND pid =?2",nativeQuery=true)
	public Double queryProductPriceInparcel(Long parcelsId, Long pid);
	
	@Query(value="SELECT * FROM product WHERE status=?2 and pid =?1",nativeQuery=true)
	public Product queryProductInfoById(Long pid,String stat);

	@Query(value="SELECT * FROM product WHERE (ppid IN(?1) or pid in(?1)) and status=?2",nativeQuery=true)
	List<Product> findByPpid(Long ppid,String status);
	
	@Query(value="SELECT * FROM product WHERE ppid IN(?1)",nativeQuery=true)
	List<Product> findByPpid(Long ppid);
	
	@Query(value="SELECT * FROM product WHERE pid IN(?1)",nativeQuery=true)
	List<Product> queryProductListByIds(List<Long> ids);

	@Query(value="SELECT remark FROM lovelyRemark WHERE typ=?1 ORDER BY RAND() LIMIT 1",nativeQuery=true)
	String getLovelyRemark(String type);

	@Query(value="SELECT  p.* FROM `shopping_Order_Pro` sp,product p,`shopping_Order` s,currency c,fromsite f WHERE s.id = sp.orderId AND p.currency=c.id AND p.fromsite=f.id AND sp.pid = p.pid AND sp.flg<>'1' AND sp.flg<>'3' AND s.OrderCode=?1",nativeQuery=true)
	List<Product> getOutPardelsProduct_ByOrderCode(String orderCode);
	
	@Query(value="SELECT  p.*,c.rate,f.name as fromsite,sp.price,sp.counts FROM `shopping_Order_Pro` sp,product p,`shopping_Order` s,currency c,fromsite f WHERE s.id = sp.orderId and p.currency=c.id and p.fromsite=f.id and sp.pid = p.pid AND sp.flg='3' AND s.OrderCode=?1",nativeQuery=true)
	List<Product> getRefundProduct_ByOrderCode(String orderCode);
	
	@Query(value="SELECT counts FROM shopping_Order_Pro WHERE pid=?2 AND orderId = (SELECT id FROM shopping_Order WHERE OrderCode=?1)",nativeQuery=true)
	public Integer queryOrderProductCounts(String orderCode, Long pid);
	@Query(value="SELECT price FROM shopping_Order_Pro WHERE pid=?2 AND orderId = (SELECT id FROM shopping_Order WHERE OrderCode=?1)",nativeQuery=true)
	public Double queryOrderProductPrice(String orderCode, Long pid);

	@Query(value="select * from product where date_upd >= ?1",nativeQuery=true)
	List<Product> findProductsWithDateUpd(Date lasttime);

	@Query(value="SELECT * FROM product WHERE newSku =?1",nativeQuery=true)
	List<Product> getProductByNewSku(String newSku);

	@Query(value="select * from product where date_add >= ?1",nativeQuery=true)
	List<Product> findProductsWithDateAdd(Date lasttime);

	@Query(value="select * from product where typ=2 and newSku <>'' and nstock between ?1 and 10000 order by nstock desc",nativeQuery=true)
	List<Product> getOverStockInfo(Integer overLine);
	
}