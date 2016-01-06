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

//抢代言日志表
@Entity
@Table(name = "endorsement_opt_log")
public class EndoresementOpLog implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = -3813873816256641877L;

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Long id;	
	
	@Column
	private Long eid;//代言编号
	
	@Column
	@Temporal(TemporalType.TIMESTAMP)
	private Date createTime; //代言申请时间
	
	@Column
	private Long userId;//操作人编号
	
	@Column
	private Long pid;//产品编号

	@Column
	@Temporal(TemporalType.TIMESTAMP)
	private Date updatetime;//变更时间
	
	public Date getUpdatetime() {
		return updatetime;
	}

	public void setUpdatetime(Date updatetime) {
		this.updatetime = updatetime;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

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

	public Long getPid() {
		return pid;
	}

	public void setPid(Long pid) {
		this.pid = pid;
	}
	
	
}
