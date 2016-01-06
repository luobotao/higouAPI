package repositories;

import javax.inject.Named;
import javax.inject.Singleton;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import models.UserVerify;
import models.admin.AdminCode;

/**
 * Provides CRUD functionality for accessing people. Spring Data auto-magically
 * takes care of many standard operations here.
 */
@Named
@Singleton
public interface AdminCodeRepository extends JpaRepository<AdminCode, Long>,JpaSpecificationExecutor<AdminCode> {

	AdminCode findByUid(Long uid);

	
	
}