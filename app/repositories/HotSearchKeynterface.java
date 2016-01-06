package repositories;

import java.util.List;

import models.HotSearchKey;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

public interface HotSearchKeynterface  extends
JpaRepository<HotSearchKey,Long>,
JpaSpecificationExecutor<HotSearchKey>{
	
	@Query(value="select * from hot_search_key where flg='1' order by sort desc",nativeQuery=true)
	public List<HotSearchKey> getkeywordlist();
}
