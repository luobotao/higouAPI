package models;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

public class EndorsementAnswer implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 666494925735292565L;

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Long aid;
	
	@Column
	private Long tid;	//问题编号
	
	@Column
	private Long eid;	//代言编号
	
	@Column
	private String answer;	//回答时间
	
	@Column
	@Temporal(TemporalType.TIMESTAMP)
	private Date createTime; //回答时间

	public Long getAid() {
		return aid;
	}

	public void setAid(Long aid) {
		this.aid = aid;
	}

	public Long getTid() {
		return tid;
	}

	public void setTid(Long tid) {
		this.tid = tid;
	}

	public Long getEid() {
		return eid;
	}

	public void setEid(Long eid) {
		this.eid = eid;
	}

	public String getAnswer() {
		return answer;
	}

	public void setAnswer(String answer) {
		this.answer = answer;
	}

	public Date getCreateTime() {
		return createTime;
	}

	public void setCreateTime(Date createTime) {
		this.createTime = createTime;
	}
}
