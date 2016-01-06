package models;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
/**
 * 用户设备实体
 * @author yangtao
 * @Date 2015年5月5日
 */
@Entity
@Table(name = "user_device")
public class UserDevice implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 708056533715893061L;

	@Id
	@GeneratedValue
	
	@Column(columnDefinition = "varchar(64) DEFAULT '' ")
	private String device_id;
	
	private Long inituid;
	private Long uid;
	@Column(columnDefinition = "tinyint(4) NOT NULL DEFAULT '0' COMMENT '0 iOS;2 android;'")
	private Long ostype;
	@Column(columnDefinition = "varchar(32) DEFAULT ''")
	private String osversion;
	@Column(columnDefinition = "varchar(32) DEFAULT '' ")
	private String model;
	@Column(columnDefinition = "varchar(200) DEFAULT '' ")
	private String pushToken;
	@Column(columnDefinition = "varchar(32) DEFAULT '' ")
	private String solution;
	@Column(columnDefinition = "varchar(32) DEFAULT '' ")
	private String appversion;
	@Column(columnDefinition = "varchar(10) DEFAULT '' ")
	private String marketCode;
	@Column(name="`date_add`")
	@Temporal(TemporalType.TIMESTAMP)
	private Date date_add;
	
	public Date getDate_add() {
		return date_add;
	}
	public void setDate_add(Date date_add) {
		this.date_add = date_add;
	}
	public Long getInituid() {
		return inituid;
	}
	public void setInituid(Long inituid) {
		this.inituid = inituid;
	}
	public Long getUid() {
		return uid;
	}
	public void setUid(Long uid) {
		this.uid = uid;
	}
	
	public Long getOstype() {
		return ostype;
	}
	public void setOstype(Long ostype) {
		this.ostype = ostype;
	}
	public String getModel() {
		return model;
	}
	public void setModel(String model) {
		this.model = model;
	}
	public String getPushToken() {
		return pushToken;
	}
	public void setPushToken(String pushToken) {
		this.pushToken = pushToken;
	}
	public String getSolution() {
		return solution;
	}
	public void setSolution(String solution) {
		this.solution = solution;
	}
	public String getAppversion() {
		return appversion;
	}
	public void setAppversion(String appversion) {
		this.appversion = appversion;
	}
	public String getDevice_id() {
		return device_id;
	}
	public void setDevice_id(String device_id) {
		this.device_id = device_id;
	}
	public String getOsversion() {
		return osversion;
	}
	public void setOsversion(String osversion) {
		this.osversion = osversion;
	}
	public String getMarketCode() {
		return marketCode;
	}
	public void setMarketCode(String marketCode) {
		this.marketCode = marketCode;
	}
	
}
