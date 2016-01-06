package services.api;

import java.text.SimpleDateFormat;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.springframework.transaction.annotation.Transactional;

import models.AutoSyncTaskLog;
import play.Logger;
import repositories.AutoSyncTaskLogRepository;

@Named
@Singleton
public class AutoSyncTaskLogService {

    private static final Logger.ALogger logger = Logger.of(AutoSyncTaskLogService.class);
	private static final SimpleDateFormat CHINESE_DATE_TIME_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    @Inject
    private AutoSyncTaskLogRepository autoSyncTaskLogRepository;
    
    @Transactional
	public void save(AutoSyncTaskLog autoSyncTaskLog) {
		autoSyncTaskLogRepository.save(autoSyncTaskLog);
	}

    /**
     * 
     * <p>Title: queryWithRecord</p> 
     * <p>Description: 根据record查询相应的记录</p> 
     * @param record
     * @param operType
     * @param target
     * @return
     */
	public AutoSyncTaskLog queryWithRecord(String record,String operType,String target) {
		return autoSyncTaskLogRepository.queryTarget(record,operType,target);
	}

	public List<AutoSyncTaskLog> getAllWithOperType(String operType,String target) {
		return autoSyncTaskLogRepository.getAllWithOperType(operType,target);
	}
	/**
	 * 
	 * <p>Title: del</p> 
	 * <p>Description: 删除成功的记录</p> 
	 * @param record
	 * @param string
	 * @param string2
	 */
	public void del(AutoSyncTaskLog autoSyncTaskLog) {
		autoSyncTaskLogRepository.delete(autoSyncTaskLog);;
	}
    
    
    
    
}
