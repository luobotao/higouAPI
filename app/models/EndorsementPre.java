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

/*
 * 代言申请表
 */
@Entity
@Table(name="endorsement_pre")
public class EndorsementPre implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -5622270214552510720L;

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Long pid;	//代言申请编号
	
	@Column
	private Long userId;	//申请用户编号
	
	@Column
	@Temporal(TemporalType.TIMESTAMP)
	private Date createTime; //代言申请时间

	@Column(columnDefinition = " int(10) unsigned DEFAULT '0' ")
	private Integer estatus;	//状态 0未审核，1通过，2未通过
	public Integer getEstatus() {
		return estatus;
	}

	public void setEstatus(Integer estatus) {
		this.estatus = estatus;
	}

	public Long getPid() {
		return pid;
	}

	public void setPid(Long pid) {
		this.pid = pid;
	}

	public Long getUserId() {
		return userId;
	}

	public void setUserId(Long userId) {
		this.userId = userId;
	}

	public Date getCreateTime() {
		return createTime;
	}

	public void setCreateTime(Date createTime) {
		this.createTime = createTime;
	}
	
}
