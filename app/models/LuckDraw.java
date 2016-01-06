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
 * 抽奖－－微信授权活动日志表（UNIONID，PID，PHONE）
 */
@Entity
@Table(name = "luck_draw")
public class LuckDraw implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = -4861524772133377868L;
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Long id;	//编号
	@Column
	@Temporal(TemporalType.TIMESTAMP)
	private Date createTime; //添加时间
	@Column
	private Long pid;//产品编号
	@Column
	private String unionid;//微信unionid
	@Column
	private String phone;//登记手机号

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Date getCreateTime() {
		return createTime;
	}

	public void setCreateTime(Date createTime) {
		this.createTime = createTime;
	}

	public Long getPid() {
		return pid;
	}

	public void setPid(Long pid) {
		this.pid = pid;
	}

	public String getUnionid() {
		return unionid;
	}

	public void setUnionid(String unionid) {
		this.unionid = unionid;
	}

	public String getPhone() {
		return phone;
	}

	public void setPhone(String phone) {
		this.phone = phone;
	}
	
	
}
