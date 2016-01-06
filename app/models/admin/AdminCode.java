package models.admin;

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
 * 店员暗号表
 * @author luobotao
 * @Date 2015年10月22日
 */
@Entity
@Table(name = "admin_code")
public class AdminCode implements Serializable {

	
	private static final long serialVersionUID = 6909367553776745861L;

	@Id
	@GeneratedValue
	private Long id;

	@Column(name = "`date_add`")
	@Temporal(TemporalType.TIMESTAMP)
	private Date dateAdd;

	@Column(name = "adminid")
	private Long adminid;

	@Column(name = "admin_code", columnDefinition = " varchar(256) DEFAULT '0' ")
	private String adminCode;

	@Column(columnDefinition = " int(11) DEFAULT 0 ")
	private Long uid;

	@Column(name = "sta", columnDefinition = " varchar(2) DEFAULT '0' ")
	private String sta;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Date getDateAdd() {
		return dateAdd;
	}

	public void setDateAdd(Date dateAdd) {
		this.dateAdd = dateAdd;
	}

	public Long getAdminid() {
		return adminid;
	}

	public void setAdminid(Long adminid) {
		this.adminid = adminid;
	}

	public String getAdminCode() {
		return adminCode;
	}

	public void setAdminCode(String adminCode) {
		this.adminCode = adminCode;
	}

	public Long getUid() {
		return uid;
	}

	public void setUid(Long uid) {
		this.uid = uid;
	}

	public String getSta() {
		return sta;
	}

	public void setSta(String sta) {
		this.sta = sta;
	}

}
