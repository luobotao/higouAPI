package repositories;

import javax.inject.Named;
import javax.inject.Singleton;

import models.AutoSyncTask;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

@Named
@Singleton
public interface AutoSyncTaskRepository extends JpaRepository<AutoSyncTask, Long>,JpaSpecificationExecutor<AutoSyncTask> {

	@Query(value="select * from auto_sync_task where target=?1 and operType=?2",nativeQuery=true)
	AutoSyncTask getTaskWithCond(String target, String operType);

}