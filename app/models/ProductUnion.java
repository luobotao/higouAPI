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
import javax.validation.constraints.NotNull;

/**
 * 商品表的分表
 * @author luobotao
 * @Date 2015年10月9日
 */
@Entity
@Table(name = "product_union")
public class ProductUnion implements Serializable{


	private static final long serialVersionUID = 4037215920516423348L;

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Long id;
	
	@NotNull
	private Long pid;//商品ID

	@Column(name="`buyNowFlag`",columnDefinition = " varchar(2) DEFAULT '0' ")
	private String buyNowFlag;//是否是立即购买商品 0否 1 是
	
	@Column(columnDefinition = " decimal(3,1) DEFAULT '0.0' ")
	private Double taxRate;//税率
	
	@Column(name="`date_add`")
	@Temporal(TemporalType.TIMESTAMP)
	private Date dateAdd;
	
	@Column(name="`date_upd`")
	@Temporal(TemporalType.TIMESTAMP)
	private Date dateUpd;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Long getPid() {
		return pid;
	}

	public void setPid(Long pid) {
		this.pid = pid;
	}

	public Date getDateAdd() {
		return dateAdd;
	}

	public void setDateAdd(Date dateAdd) {
		this.dateAdd = dateAdd;
	}

	public Date getDateUpd() {
		return dateUpd;
	}

	public void setDateUpd(Date dateUpd) {
		this.dateUpd = dateUpd;
	}

	public String getBuyNowFlag() {
		return buyNowFlag;
	}

	public void setBuyNowFlag(String buyNowFlag) {
		this.buyNowFlag = buyNowFlag;
	}

	public Double getTaxRate() {
		return taxRate;
	}

	public void setTaxRate(Double taxRate) {
		this.taxRate = taxRate;
	}
	
	
}
