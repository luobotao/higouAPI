package models;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;

import org.apache.commons.lang3.StringUtils;

import play.Configuration;

/**
 * 优惠券实体
 * @author luobotao
 * @Date 2015年5月5日
 */
@Entity
@Table(name = "coupon")
public class Coupon implements Serializable {

	private static final long serialVersionUID = -9098546955443672350L;

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Long id;
	@Column(name="`date_add`")
	@Temporal(TemporalType.TIMESTAMP)
	private Date date_add;

	@Column(columnDefinition = "int(10) DEFAULT 1 ")
	private int typ;// 状态 1新增 2审核通过
	private String title;//
	@Lob
	private String remark;//
	@Column(columnDefinition = "decimal(5,0) DEFAULT '0'")
	private Double tprice;//
	@Column(columnDefinition = "decimal(5,0) DEFAULT '0'")
	private Double couponprice;

	private String couponpic;
	@Column(columnDefinition = " DEFAULT 0 ")
	private Long pid;

	@Column(columnDefinition = "varchar(32)")
	private String btim;
	@Column(columnDefinition = "varchar(32)")
	private String etim;

	@Column(columnDefinition = "int(11) DEFAULT '0' ")
	private int counts;//
	@Column(columnDefinition = "varchar(2) DEFAULT '0'")
	private String states;
	@Column(columnDefinition = "varchar(10) ")
	private String market;
	@Column(columnDefinition = "varchar(1) ")
	private String durable;
	
	@Transient
	private String tag;
	@Transient
	private User user;
	
	public User getUser() {
		return user;
	}
	public void setUser(User user) {
		this.user = user;
	}
	public String getTag() {
		return tag;
	}
	public void setTag(String tag) {
		this.tag = tag;
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
	public int getTyp() {
		return typ;
	}
	public void setTyp(int typ) {
		this.typ = typ;
	}
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public String getRemark() {
		return remark;
	}
	public void setRemark(String remark) {
		this.remark = remark;
	}
	public Double getTprice() {
		return tprice;
	}
	public void setTprice(Double tprice) {
		this.tprice = tprice;
	}
	public Double getCouponprice() {
		return couponprice;
	}
	public void setCouponprice(Double couponprice) {
		this.couponprice = couponprice;
	}
	public String getCouponpic() {
		if(StringUtils.isBlank(couponpic)){
			return "";
		}
		boolean IsProduct = Configuration.root().getBoolean("production", false);
		String domain = Configuration.root().getString("domain.dev","http://ht2.neolix.cn");
		if(IsProduct){
			domain = Configuration.root().getString("domain.product","http://ht.neolix.cn");
		}
		couponpic = domain+Configuration.root().getString("adload","/pimgs/adload/")+"coupon/"+couponpic;
		return couponpic;
	}
	public void setCouponpic(String couponpic) {
		this.couponpic = couponpic;
	}
	public Long getPid() {
		return pid;
	}
	public void setPid(Long pid) {
		this.pid = pid;
	}
	public String getBtim() {
		return btim;
	}
	public void setBtim(String btim) {
		this.btim = btim;
	}
	public String getEtim() {
		return etim;
	}
	public void setEtim(String etim) {
		this.etim = etim;
	}
	public int getCounts() {
		return counts;
	}
	public void setCounts(int counts) {
		this.counts = counts;
	}
	public String getStates() {
		return states;
	}
	public void setStates(String states) {
		this.states = states;
	}
	public String getMarket() {
		return market;
	}
	public void setMarket(String market) {
		this.market = market;
	}
	public String getDurable() {
		return durable;
	}
	public void setDurable(String durable) {
		this.durable = durable;
	}
	
}
