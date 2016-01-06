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
@Table(name="user_balance_log")
public class UserBalanceLog implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 2441775336214145563L;

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Long bid;
	
	@Column
	private Long userId;	//代言用户编号
	
	@Column(columnDefinition = " decimal(9,2) DEFAULT '0.00' ")
	private BigDecimal beforBalance;	//变更前余额
	
	@Column(columnDefinition = " decimal(9,2) DEFAULT '0.00' ")
	private BigDecimal curentBalance;	//变更后余额
	
	@Column(columnDefinition = " decimal(9,2) DEFAULT '0.00' ")
	private BigDecimal balance;	//发生金额
	
	@Column
	@Temporal(TemporalType.TIMESTAMP)
	private Date createTime; //创建时间

	@Column(columnDefinition = "int(2) DEFAULT '0' ")
	private int flg;	//变更类型1收入，2支出
	@Column(columnDefinition = " varchar(50) DEFAULT '' ")
	private String remark;	//支出、收入明细说明
	
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

	public String getRemark() {
		return remark;
	}

	public void setRemark(String remark) {
		this.remark = remark;
	}

	
	
	public int getFlg() {
		return flg;
	}

	public void setFlg(int flg) {
		this.flg = flg;
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

	public BigDecimal getBeforBalance() {
		return beforBalance;
	}

	public void setBeforBalance(BigDecimal beforBalance) {
		this.beforBalance = beforBalance;
	}

	public BigDecimal getCurentBalance() {
		return curentBalance;
	}

	public void setCurentBalance(BigDecimal curentBalance) {
		this.curentBalance = curentBalance;
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
