package repositories;

import java.util.List;

import javax.inject.Named;
import javax.inject.Singleton;

import models.ChannelMouldPro;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

/**
 * 频道内容相关
 * @author luobotao
 * Date: 2015年4月17日 下午2:21:17
 */
@Named
@Singleton
public interface ChannelMouldProRepository extends JpaRepository<ChannelMouldPro, Long>,JpaSpecificationExecutor<ChannelMouldPro> {

	@Query(value="SELECT id FROM channel_mould_pro where cmid=?1 order by nsort desc",nativeQuery=true)
	List<Integer> findIdsByCmid(Long cmid);
	
	/**
	 * 根据商品ID获取所有的pro列表
	 * @param pid
	 * @return
	 */
	@Query(value="SELECT * FROM channel_mould_pro where pid=?1",nativeQuery=true)
	List<ChannelMouldPro> findByPid(Long pid);



}