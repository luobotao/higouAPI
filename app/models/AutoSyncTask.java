package models;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;

import net.logstash.logback.encoder.org.apache.commons.lang.StringUtils;
import play.Configuration;

/**
 * 自动同步信息到ERP
 * Title:AutoSyncErpTask
 * Description:
 * @author ctt
 * @date{date}
 */
@Entity
@Table(name = "auto_sync_task")
public class AutoSyncTask implements Serializable {

	private static final long serialVersionUID = 6661109889987075701L;

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Long id;

	private String target;	
	private String operType;
	@Column
	@Temporal(TemporalType.TIMESTAMP)
	private Date lasttime;
	private String cronExpression;
	private String memo;
	@Temporal(TemporalType.TIMESTAMP)
	private Date updatetime;
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public String getTarget() {
		return target;
	}
	public void setTarget(String target) {
		this.target = target;
	}
	public String getOperType() {
		return operType;
	}
	public void setOperType(String operType) {
		this.operType = operType;
	}
	public Date getLasttime() {
		return lasttime;
	}
	public void setLasttime(Date lasttime) {
		this.lasttime = lasttime;
	}
	public String getMemo() {
		return memo;
	}
	public void setMemo(String memo) {
		this.memo = memo;
	}
	public Date getUpdatetime() {
		return updatetime;
	}
	public void setUpdatetime(Date updatetime) {
		this.updatetime = updatetime;
	}
	public String getCronExpression() {
		return cronExpression;
	}
	public void setCronExpression(String cronExpression) {
		this.cronExpression = cronExpression;
	}
	
}
