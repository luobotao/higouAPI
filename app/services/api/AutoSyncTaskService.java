package services.api;

import java.text.SimpleDateFormat;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.springframework.transaction.annotation.Transactional;

import models.AutoSyncTask;
import play.Logger;
import repositories.AutoSyncTaskRepository;

/**自动同步任务Service
 */
@Named
@Singleton
public class AutoSyncTaskService {

    private static final Logger.ALogger logger = Logger.of(AutoSyncTaskService.class);
	private static final SimpleDateFormat CHINESE_DATE_TIME_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    @Inject
    private AutoSyncTaskRepository autoSyncTaskRepository;
    
    /**
     * 根据条件获得定时task
     * @param string
     * @param string2
     * @return
     */
	public AutoSyncTask getTaskWithCond(String target, String operType) {
		return autoSyncTaskRepository.getTaskWithCond(target,operType);
	}

	@Transactional
	public void save(AutoSyncTask autoSyncTask) {
		autoSyncTaskRepository.save(autoSyncTask);
	}
    
    
    
    
}
