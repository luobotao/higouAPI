package repositories;

import javax.inject.Named;
import javax.inject.Singleton;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import models.PadChannelPro;

/**
 * pad频道pro相关
 * @author luobotao
 * Date: 2015年4月17日 下午2:21:17
 */
@Named
@Singleton
public interface PadChannelProRepository extends JpaRepository<PadChannelPro, Long>,JpaSpecificationExecutor<PadChannelPro> {



}