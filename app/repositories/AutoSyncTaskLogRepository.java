package repositories;

import java.util.List;

import javax.inject.Named;
import javax.inject.Singleton;

import models.AutoSyncTaskLog;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

@Named
@Singleton
public interface AutoSyncTaskLogRepository extends JpaRepository<AutoSyncTaskLog, Long>,JpaSpecificationExecutor<AutoSyncTaskLog> {

	@Query(value="select * from auto_sync_task_log where record=?1 and operType=?2 and target=?3",nativeQuery=true)
	AutoSyncTaskLog queryTarget(String record, String operType, String target);

	@Query(value="select * from auto_sync_task_log where operType=?1 and target=?2",nativeQuery=true)
	List<AutoSyncTaskLog> getAllWithOperType(String operType, String target);

}