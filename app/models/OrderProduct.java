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
import javax.persistence.Transient;
import javax.validation.constraints.NotNull;

import models.admin.AdminUser;

/**
 * 订单商品实体
 * 
 * @author luobotao Date: 2015年4月16日 上午9:59:24
 */
@Entity
@Table(name = "shopping_Order_Pro")
public class OrderProduct implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 4205635990262485715L;

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Long id;

	@Column(name="`date_add`")
	@Temporal(TemporalType.TIMESTAMP)
	private Date date_add;

	@NotNull
	private Long orderId;
	
	@NotNull
	private Long pId;
	
	@Column(nullable = true, columnDefinition = "decimal(9,2) DEFAULT NULL ")
	private Double price;
	
	@Column(nullable = true, columnDefinition = "int(11) DEFAULT '1' ")
	private int counts;
	
	@Column(nullable = true, columnDefinition = "decimal(9,2) DEFAULT NULL ")
	private Double totalFee;
	
	@Column(nullable = true, columnDefinition = "varchar(2) DEFAULT '0' ")
	private String flg;
	
	@Column(nullable = true, columnDefinition = "int(11) DEFAULT '0'")
	private Long cid;
	


	
	@Transient
	private ShoppingOrder shoppingOrder;
	
	@Transient
	private Product product;
	
	@Transient
	private AdminUser adminUser;
	
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

	public AdminUser getAdminUser() {
		return adminUser;
	}

	public void setAdminUser(AdminUser adminUser) {
		this.adminUser = adminUser;
	}

	public Long getOrderId() {
		return orderId;
	}

	public void setOrderId(Long orderId) {
		this.orderId = orderId;
	}

	public Long getpId() {
		return pId;
	}

	public void setpId(Long pId) {
		this.pId = pId;
	}

	public Double getPrice() {
		return price;
	}

	public void setPrice(Double price) {
		this.price = price;
	}

	public int getCounts() {
		return counts;
	}

	public void setCounts(int counts) {
		this.counts = counts;
	}

	public Double getTotalFee() {
		return totalFee;
	}

	public void setTotalFee(Double totalFee) {
		this.totalFee = totalFee;
	}

	public String getFlg() {
		return flg;
	}

	public void setFlg(String flg) {
		this.flg = flg;
	}

	public Long getCid() {
		return cid;
	}

	public void setCid(Long cid) {
		this.cid = cid;
	}

	public ShoppingOrder getShoppingOrder() {
		return shoppingOrder;
	}

	public void setShoppingOrder(ShoppingOrder shoppingOrder) {
		this.shoppingOrder = shoppingOrder;
	}

	public Product getProduct() {
		return product;
	}

	public void setProduct(Product product) {
		this.product = product;
	}
	
	
	
}
