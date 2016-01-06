package repositories;

import models.UserBalanceLog;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface UserbalanceLogInterface extends JpaRepository<UserBalanceLog,Long>,JpaSpecificationExecutor<UserBalanceLog>{

	
}
