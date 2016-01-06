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
 * 用户红点表
 */
@Entity
@Table(name = "user_red_flag")
public class UserRedFlag implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = -9115773602293630634L;
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Long id;
	@Column
	@Temporal(TemporalType.TIMESTAMP)
	private Date createTime;//添加时间
	@Column
	@Temporal(TemporalType.TIMESTAMP)
	private Date updateTime;//变更时间
	@Column
	private Integer EndorseBalanceFlag;//用户代言余额变更表，1表示要显示红点，0表示不显示红点
	@Column
	private Integer couponRedFlag;//优惠券变化表，1表示要显示红点，0表示不显示红点
	@Column
	private Integer myPresellsRedFlag;//我的预售，1表示要显示红点，0表示不显示红点
	@Column
	private Integer guessULikeRedFlag;//猜你喜欢，1表示要显示红点，0表示不显示红点
	@Column
	private Integer customServiceRedFlag;//在线客服，1表示要显示红点，0表示不显示红点
	@Column
	private Long userId;//用户编号（唯一）
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
	public Date getUpdateTime() {
		return updateTime;
	}
	public void setUpdateTime(Date updateTime) {
		this.updateTime = updateTime;
	}
	public Integer getEndorseBalanceFlag() {
		return EndorseBalanceFlag;
	}
	public void setEndorseBalanceFlag(Integer endorseBalanceFlag) {
		EndorseBalanceFlag = endorseBalanceFlag;
	}
	public Integer getCouponRedFlag() {
		return couponRedFlag;
	}
	public void setCouponRedFlag(Integer couponRedFlag) {
		this.couponRedFlag = couponRedFlag;
	}
	public Integer getMyPresellsRedFlag() {
		return myPresellsRedFlag;
	}
	public void setMyPresellsRedFlag(Integer myPresellsRedFlag) {
		this.myPresellsRedFlag = myPresellsRedFlag;
	}
	public Integer getGuessULikeRedFlag() {
		return guessULikeRedFlag;
	}
	public void setGuessULikeRedFlag(Integer guessULikeRedFlag) {
		this.guessULikeRedFlag = guessULikeRedFlag;
	}
	public Integer getCustomServiceRedFlag() {
		return customServiceRedFlag;
	}
	public void setCustomServiceRedFlag(Integer customServiceRedFlag) {
		this.customServiceRedFlag = customServiceRedFlag;
	}
	public Long getUserId() {
		return userId;
	}
	public void setUserId(Long userId) {
		this.userId = userId;
	}
	
}
