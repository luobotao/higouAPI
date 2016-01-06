package repositories;

import javax.inject.Named;
import javax.inject.Singleton;

import models.ShoppingOrder;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

@Named
@Singleton
public interface ShoppingOrderRepository extends JpaRepository<ShoppingOrder, Long>,
		JpaSpecificationExecutor<ShoppingOrder> {

	ShoppingOrder findByOrderCode(String orderCode);

	@Query(value="select o.* from shopping_Order o,pardels p where o.id = p.orderId and p.id=?1",nativeQuery=true)
	ShoppingOrder queryShoppingOrderByParcelId(String packId);

	ShoppingOrder findByOrderCodeAndTotalFee(String orderCode, Double amount);
	
}