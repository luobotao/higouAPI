package repositories;

import java.util.List;

import javax.inject.Named;
import javax.inject.Singleton;

import models.Category;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

/**
 * 商品品类相关
 * @author luobotao
 * Date: 2015年4月17日 下午2:21:17
 */
@Named
@Singleton
public interface CategoryRepository extends JpaRepository<Category, Long>,JpaSpecificationExecutor<Category> {

}