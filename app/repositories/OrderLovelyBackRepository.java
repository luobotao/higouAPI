package repositories;

import java.util.List;

import javax.inject.Named;
import javax.inject.Singleton;

import models.OrderLoveLyBack;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

@Named
@Singleton
public interface OrderLovelyBackRepository extends JpaRepository<OrderLoveLyBack, Long>,JpaSpecificationExecutor<OrderLoveLyBack> {

	List<OrderLoveLyBack> findByOrderCode(String orderCode);	
	
}