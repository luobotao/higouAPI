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
 * 代言用户余额变更表
 */
@Entity
@Table(name="endorsement_pay_log")
public class EndorsementPayLog implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 8866187111628611994L;

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Long bid;
	
	@Column
	private Long userId;	//代言用户编号
	
	
	@Column(columnDefinition = " decimal(9,2) DEFAULT '0.00' ")
	private BigDecimal balance;	//奖励金额
	
	@Column
	@Temporal(TemporalType.TIMESTAMP)
	private Date createTime; //创建时间

	@Column(columnDefinition ="int(2) DEFAULT '0'")
	private int status;	//状态 0兑现,1冻结,2:退款
	
	@Column
	private Long eid;	//代言编号

	@Column
	private Long orderId;	//订单编号
	
	@Column
	private Long pid;	//产品编号
	
	@Column(columnDefinition="VARCHAR(200)")
	private String protitle;	//产品名称
	
	public Long getPid() {
		return pid;
	}

	public void setPid(Long pid) {
		this.pid = pid;
	}

	public String getProtitle() {
		return protitle;
	}

	public void setProtitle(String protitle) {
		this.protitle = protitle;
	}

	public int getStatus() {
		return status;
	}

	public void setStatus(int status) {
		this.status = status;
	}

	public Long getOrderId() {
		return orderId;
	}

	public void setOrderId(Long orderId) {
		this.orderId = orderId;
	}

	public Long getEid() {
		return eid;
	}

	public void setEid(Long eid) {
		this.eid = eid;
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
	
}
