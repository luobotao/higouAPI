package repositories;

import javax.inject.Named;
import javax.inject.Singleton;

import models.UserLike;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

/**
 * Provides CRUD functionality for accessing people. Spring Data auto-magically
 * takes care of many standard operations here.
 */
@Named
@Singleton
public interface UserLikeRepository extends JpaRepository<UserLike, Long>,JpaSpecificationExecutor<UserLike> {

	public UserLike findByUidAndPid(Long uid, Long pid);
	
	
}