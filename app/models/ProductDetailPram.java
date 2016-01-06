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

@Entity
@Table(name = "product_detail_info")
public class ProductDetailPram implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 9198584131218545711L;
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Long id;
	@Column
	private Long pdid;	//商品详情编号
	@Column(name="`date_add`")
	@Temporal(TemporalType.TIMESTAMP)
	private Date date_add;	//添加日期
	@Column(columnDefinition = " int(2) unsigned DEFAULT '0' ")
	private Integer nsort;
	@Column
	private String key;//参数名称
	@Column
	private String val;//参数值
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public Long getPdid() {
		return pdid;
	}
	public void setPdid(Long pdid) {
		this.pdid = pdid;
	}
	public Date getDate_add() {
		return date_add;
	}
	public void setDate_add(Date date_add) {
		this.date_add = date_add;
	}
	public Integer getNsort() {
		return nsort;
	}
	public void setNsort(Integer nsort) {
		this.nsort = nsort;
	}
	public String getKey() {
		return key;
	}
	public void setKey(String key) {
		this.key = key;
	}
	public String getVal() {
		return val;
	}
	public void setVal(String val) {
		this.val = val;
	}
	
}
