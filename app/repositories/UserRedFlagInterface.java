package repositories;

import models.UserRedFlag;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

public interface UserRedFlagInterface  extends
JpaRepository<UserRedFlag,Long>,JpaSpecificationExecutor<UserRedFlag>{

	@Query(value="select * from user_red_flag where userId=?1 order by 1 desc limit 1",nativeQuery=true)
	public UserRedFlag getUserFlag(Long userId);
}
