package repositories;

import javax.inject.Named;
import javax.inject.Singleton;

import models.OrderProduct;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

@Named
@Singleton
public interface OrderProductRepository extends JpaRepository<OrderProduct, Long>,JpaSpecificationExecutor<OrderProduct> {

}