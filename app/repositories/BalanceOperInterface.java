package repositories;

import models.BalanceOperLog;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface BalanceOperInterface extends JpaRepository<BalanceOperLog,Long>,JpaSpecificationExecutor<BalanceOperLog>{
	
}
