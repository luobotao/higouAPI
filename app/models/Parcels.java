package models;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;

/**
 * 包裹实体
 * 
 * @author luobotao Date: 2015年4月20日 下午2:13:08
 */
@Entity
@Table(name = "pardels")
public class Parcels implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 690049631158189023L;

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Long id;

	@Column(name="`date_add`")
	@Temporal(TemporalType.TIMESTAMP)
	private Date date_add;

	@Column(nullable = false)
	private Long adminId;

	@Column(nullable = false, columnDefinition = "varchar(32) " ,name="pardelCode")
	private String parcelCode;
	private String src;

	@Column(nullable = true, columnDefinition = "decimal(9,2) DEFAULT NULL ")
	private Double totalFee;
	@Column(nullable = true, columnDefinition = "decimal(9,2) DEFAULT NULL ")
	private Double goodsFee;
	@Column(nullable = true, columnDefinition = "decimal(9,2) DEFAULT NULL ")
	private Double freight;
	@Column(name="`name`")
	private String name;
	private String phone;
	private String province;
	private String address;
	private Integer status;
	private String OrderCode;
	private Long orderId;
	private String cardId;
	private String creditcard;
	private String foreignorder;
	private String currency;
	private String account;
	private String traffic;

	@Column(columnDefinition = "DEFAULT '' ")
	private String traffic_mark;

	@Column(nullable = false, columnDefinition = "DEFAULT '0' ")
	private Long bbtid;
	@Column(nullable = false, columnDefinition = "DEFAULT '' ")
	private String mailnum;
	@Column(nullable = false, columnDefinition = "DEFAULT '0' ")
	private Long order_print;
	@Column(nullable = false, columnDefinition = "DEFAULT '0' ")
	private Long bbt_print;
	@Column(columnDefinition = "DEFAULT '' ")
	private String remark;

	@Transient
	private List<Product> productList;
	

	public List<Product> getProductList() {
		return productList;
	}

	public void setProductList(List<Product> productList) {
		this.productList = productList;
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

	public Long getAdminId() {
		return adminId;
	}

	public void setAdminId(Long adminId) {
		this.adminId = adminId;
	}


	public String getParcelCode() {
		return parcelCode;
	}

	public void setParcelCode(String parcelCode) {
		this.parcelCode = parcelCode;
	}

	public String getSrc() {
		return src;
	}

	public void setSrc(String src) {
		this.src = src;
	}

	public Double getTotalFee() {
		return totalFee;
	}

	public void setTotalFee(Double totalFee) {
		this.totalFee = totalFee;
	}

	public Double getGoodsFee() {
		return goodsFee;
	}

	public void setGoodsFee(Double goodsFee) {
		this.goodsFee = goodsFee;
	}

	public Double getFreight() {
		return freight;
	}

	public void setFreight(Double freight) {
		this.freight = freight;
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

	public Integer getStatus() {
		return status;
	}

	public void setStatus(Integer status) {
		this.status = status;
	}

	public String getOrderCode() {
		return OrderCode;
	}

	public void setOrderCode(String orderCode) {
		OrderCode = orderCode;
	}

	public Long getOrderId() {
		return orderId;
	}

	public void setOrderId(Long orderId) {
		this.orderId = orderId;
	}

	public String getCardId() {
		return cardId;
	}

	public void setCardId(String cardId) {
		this.cardId = cardId;
	}

	public String getCreditcard() {
		return creditcard;
	}

	public void setCreditcard(String creditcard) {
		this.creditcard = creditcard;
	}

	public String getForeignorder() {
		return foreignorder;
	}

	public void setForeignorder(String foreignorder) {
		this.foreignorder = foreignorder;
	}

	public String getCurrency() {
		return currency;
	}

	public void setCurrency(String currency) {
		this.currency = currency;
	}

	public String getAccount() {
		return account;
	}

	public void setAccount(String account) {
		this.account = account;
	}

	public String getTraffic() {
		return traffic;
	}

	public void setTraffic(String traffic) {
		this.traffic = traffic;
	}

	public String getTraffic_mark() {
		return traffic_mark;
	}

	public void setTraffic_mark(String traffic_mark) {
		this.traffic_mark = traffic_mark;
	}

	public Long getBbtid() {
		return bbtid;
	}

	public void setBbtid(Long bbtid) {
		this.bbtid = bbtid;
	}

	public String getMailnum() {
		return mailnum;
	}

	public void setMailnum(String mailnum) {
		this.mailnum = mailnum;
	}

	public Long getOrder_print() {
		return order_print;
	}

	public void setOrder_print(Long order_print) {
		this.order_print = order_print;
	}

	public Long getBbt_print() {
		return bbt_print;
	}

	public void setBbt_print(Long bbt_print) {
		this.bbt_print = bbt_print;
	}

	public String getRemark() {
		return remark;
	}

	public void setRemark(String remark) {
		this.remark = remark;
	}

}
