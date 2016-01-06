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
 * 自动同步信息到ERP日志记录
 * Title:AutoSyncTaskLog
 * Description:
 * @author ctt
 * @date{date}
 */
@Entity
@Table(name = "auto_sync_task_log")
public class AutoSyncTaskLog implements Serializable {

	private static final long serialVersionUID = 6661109889987075701L;

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Long id;

	private String target;	
	private String operType;
	@Column(name="date_add")
	@Temporal(TemporalType.TIMESTAMP)
	private Date dateAdd;
	private String record;
	private String memo;
	@Column(name="date_upd")
	@Temporal(TemporalType.TIMESTAMP)
	private Date dateUpd;
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
	public String getMemo() {
		return memo;
	}
	public void setMemo(String memo) {
		this.memo = memo;
	}
	public String getRecord() {
		return record;
	}
	public void setRecord(String record) {
		this.record = record;
	}
	public Date getDateAdd() {
		return dateAdd;
	}
	public void setDateAdd(Date dateAdd) {
		this.dateAdd = dateAdd;
	}
	public Date getDateUpd() {
		return dateUpd;
	}
	public void setDateUpd(Date dateUpd) {
		this.dateUpd = dateUpd;
	}
	
	
}
