package models;

import java.io.Serializable;
import java.math.BigDecimal;
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
 * 代言用户余额表
 */
@Entity
@Table(name="user_balance")
public class UserBalance implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 363650763232551512L;

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Long bid;
	
	@Column
	private Long userId;	//代言用户编号
	
	@Column(columnDefinition = " decimal(9,2) DEFAULT '0.00' ")
	private BigDecimal balance;	//余额
	
	@Column
	@Temporal(TemporalType.TIMESTAMP)
	private Date createTime; //创建时间
	
	@Column
	@Temporal(TemporalType.TIMESTAMP)
	private Date updateTime; //更新时间

	@Column(columnDefinition = " decimal(9,2) DEFAULT '0.00' ")
	private BigDecimal enbalance;		//冻结金额
	
	@Column(columnDefinition="varchar(2) DEFAULT '0'")
	private String redFlag;	//是否发生了金额变化 0未变化 1已变化
	
	public String getRedFlag() {
		return redFlag;
	}

	public void setRedFlag(String redFlag) {
		this.redFlag = redFlag;
	}

	public BigDecimal getEnbalance() {
		return enbalance;
	}

	public void setEnbalance(BigDecimal enbalance) {
		this.enbalance = enbalance;
	}

	public Long getBid() {
		return bid;
	}

	public void setBid(Long bid) {
		this.bid = bid;
	}

	public Long getUserId() {
		return userId;
	}

	public void setUserId(Long userId) {
		this.userId = userId;
	}

	public BigDecimal getBalance() {
		return balance;
	}

	public void setBalance(BigDecimal balance) {
		this.balance = balance;
	}

	public Date getCreateTime() {
		return createTime;
	}

	public void setCreateTime(Date createTime) {
		this.createTime = createTime;
	}

	public Date getUpdateTime() {
		return updateTime;
	}

	public void setUpdateTime(Date updateTime) {
		this.updateTime = updateTime;
	}
}
