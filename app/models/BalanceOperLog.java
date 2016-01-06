package models;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

@Entity(name="banlance_oper_log")
public class BalanceOperLog implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 5580931505010585348L;

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Long bid;
	
	@Column(columnDefinition = " int(4) unsigned DEFAULT '0' ")
	private int flag;	//  0为处理,1处理成功,2处理失败	
	@Column
	@Temporal(TemporalType.TIMESTAMP)
	private Date createTime;	//添加时间
	
	@Column
	@Temporal(TemporalType.TIMESTAMP)
	private Date submitTime;	//处理时间
	
	@Column(columnDefinition = " decimal(9,2) DEFAULT '0.00' ")
	private BigDecimal balance; //提现金额

	@Column
	private Long userId;	//用户编号
	
	@Column(columnDefinition = " varchar(50) DEFAULT '' ")
	private String paycardno;//支付宝提现帐号
	
	@Column(columnDefinition =" varchar(50) DEFAULT '' ")
	private String userName;//提现人姓名
	
	
	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public String getPaycardno() {
		return paycardno;
	}

	public void setPaycardno(String paycardno) {
		this.paycardno = paycardno;
	}

	public Long getUserId() {
		return userId;
	}

	public void setUserId(Long userId) {
		this.userId = userId;
	}

	public Long getBid() {
		return bid;
	}

	public void setBid(Long bid) {
		this.bid = bid;
	}

	public int getFlag() {
		return flag;
	}

	public void setFlag(int flag) {
		this.flag = flag;
	}

	public Date getCreateTime() {
		return createTime;
	}

	public void setCreateTime(Date createTime) {
		this.createTime = createTime;
	}

	public Date getSubmitTime() {
		return submitTime;
	}

	public void setSubmitTime(Date submitTime) {
		this.submitTime = submitTime;
	}

	public BigDecimal getBalance() {
		return balance;
	}

	public void setBalance(BigDecimal balance) {
		this.balance = balance;
	}
	
	
}
