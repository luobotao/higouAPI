package repositories;

import javax.inject.Named;
import javax.inject.Singleton;

import models.UserVerify;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

/**
 * Provides CRUD functionality for accessing people. Spring Data auto-magically
 * takes care of many standard operations here.
 */
@Named
@Singleton
public interface UserVerifyRepository extends JpaRepository<UserVerify, Long>,JpaSpecificationExecutor<UserVerify> {

	public UserVerify findByUidAndPhoneAndVerifyAndFlg(Long uid, String phone,String verify,String flg);

	public UserVerify findByPhoneAndVerifyAndFlg(String phone, String verify,
			String flg);
	
	
}