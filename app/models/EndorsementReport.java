package models;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
/*
 * 代言表
 */
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;
@Entity
@Table(name = "endorsement_Report")
public class EndorsementReport implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 3223451174247783412L;

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Long eid;	//代言编号
	
	@Column
	@Temporal(TemporalType.TIMESTAMP)
	private Date createTime; //代言举报时间
	
	@Column
	private Long userId;	//代言人编号

	@Column
	private String remark;	//代言描述

	public Long getEid() {
		return eid;
	}

	public void setEid(Long eid) {
		this.eid = eid;
	}

	public Date getCreateTime() {
		return createTime;
	}

	public void setCreateTime(Date createTime) {
		this.createTime = createTime;
	}

	public Long getUserId() {
		return userId;
	}

	public void setUserId(Long userId) {
		this.userId = userId;
	}

	public String getRemark() {
		return remark;
	}

	public void setRemark(String remark) {
		this.remark = remark;
	}
	

}
