package models;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;

/**
 * 代言购物车实体
 */
@Entity
@Table(name = "shopping_Cart_endorse")
public class ShoppingCartEndorse implements Serializable {

	private static final long serialVersionUID = -5773577118399520996L;

	@Id
	@GeneratedValue
	private Long id;
	@Column
	private Long uId;
	@Column
	private Long pId;
	
	@Column(columnDefinition = " int(11) DEFAULT '0' ")
	private int counts;
	@Column(name="`date_add`")
	@Temporal(TemporalType.TIMESTAMP)
	private Date date_add;
	@Column(columnDefinition = "varchar(500)")
	private String reffer;
	
	@Column
	private Long eid;//代言编号

	@Column(columnDefinition = "varchar(50) DEFAULT '' ")
	private String openid;//微信标识
	
	@Transient
	private Product proinfo;//产品信息
	
	@Transient
	private Fromsite fromsite;//物流信息
	@Transient
	private Currency currency;//汇率信息
		
	@Transient
	private Endorsement endorse;
	public Currency getCurrency() {
		return currency;
	}

	public void setCurrency(Currency currency) {
		this.currency = currency;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Long getuId() {
		return uId;
	}

	public void setuId(Long uId) {
		this.uId = uId;
	}

	public Long getpId() {
		return pId;
	}

	public void setpId(Long pId) {
		this.pId = pId;
	}

	public int getCounts() {
		return counts;
	}

	public void setCounts(int counts) {
		this.counts = counts;
	}

	public Date getDate_add() {
		return date_add;
	}

	public void setDate_add(Date date_add) {
		this.date_add = date_add;
	}

	public String getReffer() {
		return reffer;
	}

	public void setReffer(String reffer) {
		this.reffer = reffer;
	}

	public Long getEid() {
		return eid;
	}

	public void setEid(Long eid) {
		this.eid = eid;
	}

	public Product getProinfo() {
		return proinfo;
	}

	public void setProinfo(Product proinfo) {
		this.proinfo = proinfo;
	}

	public Fromsite getFromsite() {
		return fromsite;
	}

	public void setFromsite(Fromsite fromsite) {
		this.fromsite = fromsite;
	}

	public Endorsement getEndorse() {
		return endorse;
	}

	public void setEndorse(Endorsement endorse) {
		this.endorse = endorse;
	}

	public String getOpenid() {
		return openid;
	}

	public void setOpenid(String openid) {
		this.openid = openid;
	}
	
}
