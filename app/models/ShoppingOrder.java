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

/**
 * 订单实体
 * 
 * @author luobotao Date: 2015年4月16日 上午9:59:24
 */
@Entity
@Table(name = "shopping_Order")
public class ShoppingOrder implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 330843425903288877L;

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Long id;

	@Column(name="`date_add`")
	@Temporal(TemporalType.TIMESTAMP)
	private Date date_add;

	private Long uId;
	@Column(nullable = false, columnDefinition = "int(11) DEFAULT 0 ")
	private int cId;
	private String orderCode;
	private String ordertype;// 1代购 2撒娇
	private Double totalFee;
	@Column(nullable = false, columnDefinition = "tinyint(4) unsigned NOT NULL DEFAULT '0'")
	private int paymethod;// 
	@Column(nullable = false, columnDefinition = "tinyint(4) unsigned NOT NULL DEFAULT '0'")
	private int paystat;// 20服务端返回成功
	private String paytime;
	@Column(name="`name`")
	private String name;
	private String phone;
	private String province;
	private String address;
	@Column(nullable = false, columnDefinition = "DEFAULT 0 ")
	private int status;
	private Double goods_fee;
	private Double domestic_fee;
	private Double foreignfee;
	private Double tariff_fee;
	private Double cost_fee;
	private String endtime;
	private String cardId;// 身份证
	private String lovely;// 撒娇说的话
	@Column(nullable = false, columnDefinition = "tinyint(4) unsigned NOT NULL DEFAULT '0'")
	private int nrefund_total;
	@Column(nullable = false, columnDefinition = "tinyint(4) unsigned NOT NULL DEFAULT '0'")
	private int nrefund_succ;
	@Column(nullable = false, columnDefinition = "decimal(9,2) unsigned NOT NULL DEFAULT '0.00'")
	private Double refund_amount;

	@Column(nullable = true, columnDefinition = "varchar(8) DEFAULT '' ")
	private String postcode;// 邮编
	@Column(nullable = true, columnDefinition = "varchar(32) DEFAULT '' ")
	private String tradeno;// 交易号
	@Column(nullable = true, columnDefinition = "int(11) DEFAULT '0' ")
	private int couponUserId;// 优惠券ID
	@Column(nullable = true, columnDefinition = "varchar(256) DEFAULT '' ")
	private String coupon;// 优惠券描述
	@Column(nullable = true, columnDefinition = "decimal(9,2) DEFAULT '0.00' ")
	private Double coupon_price;
	@Column(nullable = true, columnDefinition = "decimal(9,2) DEFAULT NULL")
	private Double lovelydistinct;// 折扣
	
	@Column(nullable = false, columnDefinition = "DEFAULT 0 ")
	private int stage;
	@Column(nullable = true, columnDefinition = "decimal(9,2) DEFAULT 0.00")
	private Double deposit;
	@Column(nullable = true, columnDefinition = "decimal(9,2) DEFAULT 0.00")
	private Double finalpay;
	
	@Column(nullable = true, columnDefinition = "int(11) DEFAULT '0'")
	private Long taskId;
	
	@Column(nullable = true, columnDefinition = "varchar(2) DEFAULT '0' ")
	private String noticeflg;
	@Column(nullable = true, columnDefinition = "varchar(32) DEFAULT '' ")
	private String sfcode;
	@Column(nullable = true, columnDefinition = "decimal(9,2) DEFAULT 0.00")
	private Double original_fee;
	@Column
	private Long endorsementid;//代言编号
	
	@Column(columnDefinition="varchar(20) DEFAULT ''")
	private String mcode;//推广来源码
	
	@Column(columnDefinition="varchar(5) DEFAULT '0'")
	private String wx;//订单来源，1微信，0微博
	
	@Column(columnDefinition="varchar(50) DEFAULT ''")
	private String shareType;//分享长短图模式 1微信，2微信短图，3微信长图
	
	
	public String getShareType() {
		return shareType;
	}
	public void setShareType(String shareType) {
		this.shareType = shareType;
	}
	public String getWx() {
		return wx;
	}
	public void setWx(String wx) {
		this.wx = wx;
	}
	public String getMcode() {
		return mcode;
	}
	public void setMcode(String mcode) {
		this.mcode = mcode;
	}
	public Long getEndorsementid() {
		return endorsementid;
	}
	public void setEndorsementid(Long endorsementid) {
		this.endorsementid = endorsementid;
	}
	public String getNoticeflg() {
		return noticeflg;
	}
	public void setNoticeflg(String noticeflg) {
		this.noticeflg = noticeflg;
	}
	public String getSfcode() {
		return sfcode;
	}
	public void setSfcode(String sfcode) {
		this.sfcode = sfcode;
	}
	public Double getOriginal_fee() {
		return original_fee;
	}
	public void setOriginal_fee(Double original_fee) {
		this.original_fee = original_fee;
	}
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public Date getDate_add() {
		return date_add;
	}
	public void setDate_add(Date date_add) {
		this.date_add = date_add;
	}

	public int getcId() {
		return cId;
	}
	public Long getuId() {
		return uId;
	}
	public void setuId(Long uId) {
		this.uId = uId;
	}
	public void setcId(int cId) {
		this.cId = cId;
	}
	public String getOrderCode() {
		return orderCode;
	}
	public void setOrderCode(String orderCode) {
		this.orderCode = orderCode;
	}
	public String getOrdertype() {
		return ordertype;
	}
	public void setOrdertype(String ordertype) {
		this.ordertype = ordertype;
	}
	public Double getTotalFee() {
		return totalFee;
	}
	public void setTotalFee(Double totalFee) {
		this.totalFee = totalFee;
	}
	public int getPaymethod() {
		return paymethod;
	}
	public void setPaymethod(int paymethod) {
		this.paymethod = paymethod;
	}
	public int getPaystat() {
		return paystat;
	}
	public void setPaystat(int paystat) {
		this.paystat = paystat;
	}
	public String getPaytime() {
		return paytime;
	}
	public void setPaytime(String paytime) {
		this.paytime = paytime;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getPhone() {
		return phone;
	}
	public void setPhone(String phone) {
		this.phone = phone;
	}
	public String getProvince() {
		return province;
	}
	public void setProvince(String province) {
		this.province = province;
	}
	public String getAddress() {
		return address;
	}
	public void setAddress(String address) {
		this.address = address;
	}
	public int getStatus() {
		return status;
	}
	public void setStatus(int status) {
		this.status = status;
	}
	public Double getGoods_fee() {
		return goods_fee;
	}
	public void setGoods_fee(Double goods_fee) {
		this.goods_fee = goods_fee;
	}
	public Double getDomestic_fee() {
		return domestic_fee;
	}
	public void setDomestic_fee(Double domestic_fee) {
		this.domestic_fee = domestic_fee;
	}
	public Double getForeignfee() {
		return foreignfee;
	}
	public void setForeignfee(Double foreignfee) {
		this.foreignfee = foreignfee;
	}
	public Double getTariff_fee() {
		return tariff_fee;
	}
	public void setTariff_fee(Double tariff_fee) {
		this.tariff_fee = tariff_fee;
	}
	public Double getCost_fee() {
		return cost_fee;
	}
	public void setCost_fee(Double cost_fee) {
		this.cost_fee = cost_fee;
	}
	public String getEndtime() {
		return endtime;
	}
	public void setEndtime(String endtime) {
		this.endtime = endtime;
	}
	public String getCardId() {
		return cardId;
	}
	public void setCardId(String cardId) {
		this.cardId = cardId;
	}
	public String getLovely() {
		return lovely;
	}
	public void setLovely(String lovely) {
		this.lovely = lovely;
	}
	public int getNrefund_total() {
		return nrefund_total;
	}
	public void setNrefund_total(int nrefund_total) {
		this.nrefund_total = nrefund_total;
	}
	public int getNrefund_succ() {
		return nrefund_succ;
	}
	public void setNrefund_succ(int nrefund_succ) {
		this.nrefund_succ = nrefund_succ;
	}
	public Double getRefund_amount() {
		return refund_amount;
	}
	public void setRefund_amount(Double refund_amount) {
		this.refund_amount = refund_amount;
	}
	public String getPostcode() {
		return postcode;
	}
	public void setPostcode(String postcode) {
		this.postcode = postcode;
	}
	public String getTradeno() {
		return tradeno;
	}
	public void setTradeno(String tradeno) {
		this.tradeno = tradeno;
	}
	public int getCouponUserId() {
		return couponUserId;
	}
	public void setCouponUserId(int couponUserId) {
		this.couponUserId = couponUserId;
	}
	public String getCoupon() {
		return coupon;
	}
	public void setCoupon(String coupon) {
		this.coupon = coupon;
	}
	public Double getCoupon_price() {
		return coupon_price;
	}
	public void setCoupon_price(Double coupon_price) {
		this.coupon_price = coupon_price;
	}
	public Double getLovelydistinct() {
		return lovelydistinct;
	}
	public void setLovelydistinct(Double lovelydistinct) {
		this.lovelydistinct = lovelydistinct;
	}
	public int getStage() {
		return stage;
	}
	public void setStage(int stage) {
		this.stage = stage;
	}
	public Double getDeposit() {
		return deposit;
	}
	public void setDeposit(Double deposit) {
		this.deposit = deposit;
	}
	public Double getFinalpay() {
		return finalpay;
	}
	public void setFinalpay(Double finalpay) {
		this.finalpay = finalpay;
	}
	public Long getTaskId() {
		return taskId;
	}
	public void setTaskId(Long taskId) {
		this.taskId = taskId;
	}
	
	
	
}
