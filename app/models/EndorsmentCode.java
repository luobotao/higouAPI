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
 * 暗号表
 */
@Entity
@Table(name="endorsment_code")
public class EndorsmentCode implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 573018232277130089L;

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Long cid;
	
	@Column
	private String code;	//暗号
	
	@Column
	private Integer status;	//状态
	
	@Column 
	private Long uid;

	@Column
	@Temporal(TemporalType.TIMESTAMP)
	private Date createTime; //时间

	public Long getCid() {
		return cid;
	}

	public void setCid(Long cid) {
		this.cid = cid;
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public Integer getStatus() {
		return status;
	}

	public void setStatus(Integer status) {
		this.status = status;
	}

	public Date getCreateTime() {
		return createTime;
	}

	public void setCreateTime(Date createTime) {
		this.createTime = createTime;
	}

	public Long getUid() {
		return uid;
	}

	public void setUid(Long uid) {
		this.uid = uid;
	}
}
