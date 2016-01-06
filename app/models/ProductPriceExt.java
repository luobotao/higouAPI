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
 * 产品价格表扩展，新老人价格使用
 */
@Entity
@Table(name="product_price")
public class ProductPriceExt implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1797129680907419687L;
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Long id;
	@Column(columnDefinition = " decimal(12,4) ")
	private Double saleprice;//销售价格
	@Column
	private Long pid;//产品编号
	@Column(columnDefinition = " varchar(10) ")
	private String manType;//销售类型（新人价，老人价）
	@Column
	@Temporal(TemporalType.TIMESTAMP)
	private Date createTime; //创建时间
	@Column
	@Temporal(TemporalType.TIMESTAMP)
	private Date updateTime; //更新时间
	@Column(columnDefinition = " varchar(30) DEFAULT 'price' ")
	private String pricetype;//价格类型；rmb_price,china_price,list_price,endorsement_price,price
	
	public String getPricetype() {
		return pricetype;
	}
	public void setPricetype(String pricetype) {
		this.pricetype = pricetype;
	}
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public Double getSaleprice() {
		return saleprice;
	}
	public void setSaleprice(Double saleprice) {
		this.saleprice = saleprice;
	}
	public Long getPid() {
		return pid;
	}
	public void setPid(Long pid) {
		this.pid = pid;
	}
	public String getManType() {
		return manType;
	}
	public void setManType(String manType) {
		this.manType = manType;
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
