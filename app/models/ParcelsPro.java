package models;

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
 * 包裹与商品关联实体
 * 
 * @author luobotao Date: 2015年4月20日 下午2:13:08
 */
@Entity
@Table(name = "pardels_Pro")
public class ParcelsPro {

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Long id;

	@Column(name="`date_add`")
	@Temporal(TemporalType.TIMESTAMP)
	private Date date_add;

	@Column(nullable = false, name = "pardelsId")
	private Long parcelsId;
	@Column(nullable = false)
	private Long shopProId;
	@Column(nullable = false)
	private Long pId;

	@Column(nullable = true, columnDefinition = "decimal(9,2) DEFAULT NULL ")
	private Double price;

	private Long counts;

	@Column(nullable = true, columnDefinition = "decimal(9,2) DEFAULT NULL ")
	private Double totalFee;

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

	public Long getParcelsId() {
		return parcelsId;
	}

	public void setParcelsId(Long parcelsId) {
		this.parcelsId = parcelsId;
	}

	public Long getShopProId() {
		return shopProId;
	}

	public void setShopProId(Long shopProId) {
		this.shopProId = shopProId;
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

	public Long getCounts() {
		return counts;
	}

	public void setCounts(Long counts) {
		this.counts = counts;
	}

	public Double getTotalFee() {
		return totalFee;
	}

	public void setTotalFee(Double totalFee) {
		this.totalFee = totalFee;
	}

	
}
