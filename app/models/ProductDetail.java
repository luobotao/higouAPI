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

@Entity
@Table(name = "product_detail")
public class ProductDetail implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 2913375510624628500L;
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Long id;
	@Column
	private Long pid;	//商品编号
	@Column
	private String detail;
	@Column(name="`date_add`")
	@Temporal(TemporalType.TIMESTAMP)
	private Date date_add;	//添加日期
	@Column
	private String chname;	//中文名称
	@Column
	private String enname;	//英文名称
	@Column(columnDefinition = " int(2) unsigned DEFAULT '0' ")
	private Integer nsort;
	@Transient
	private Integer typ;	//1代下单，2自营
	
	@Transient
	private List<ProductDetailPram> pramlist;
	
	
	public List<ProductDetailPram> getPramlist() {
		return pramlist;
	}
	public void setPramlist(List<ProductDetailPram> pramlist) {
		this.pramlist = pramlist;
	}
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
	public String getDetail() {
		return detail;
	}
	public void setDetail(String detail) {
		this.detail = detail;
	}
	public Date getDate_add() {
		return date_add;
	}
	public void setDate_add(Date date_add) {
		this.date_add = date_add;
	}
	public String getChname() {
		return chname;
	}
	public void setChname(String chname) {
		this.chname = chname;
	}
	public String getEnname() {
		return enname;
	}
	public void setEnname(String enname) {
		this.enname = enname;
	}
	public Integer getNsort() {
		return nsort;
	}
	public void setNsort(Integer nsort) {
		this.nsort = nsort;
	}
	public Integer getTyp() {
		return typ;
	}
	public void setTyp(Integer typ) {
		this.typ = typ;
	}
	
	
}
