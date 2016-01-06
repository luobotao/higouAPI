package repositories;

import java.util.List;

import javax.inject.Named;
import javax.inject.Singleton;

import models.Product_images;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

@Named
@Singleton
public interface ProductImageRepository extends JpaRepository<Product_images, Long>,JpaSpecificationExecutor<Product_images> {

	List<Product_images> findByPid(Long pid);
	
	
}