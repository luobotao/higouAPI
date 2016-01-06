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

/**
 * pad频道表
 * @author luobotao
 * @Date 2015年8月31日
 */
@Entity
@Table(name = "padchannel")
public class PadChannel  implements Serializable{
	private static final long serialVersionUID = -2471038365434721177L;
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Long id;
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name="`date_add`")
	private Date date_add;
	@Column(name="`cname`",columnDefinition = "varchar(256) ")
	private String cname;//频道名称
	@Column(name="`nsort`",columnDefinition = "int(11) DEFAULT 0 ")
	private int nsort;// 排序
	@Column(name="`typ`",columnDefinition = "varchar(2) ")
	private String typ;
	@Column(name="`sta`",columnDefinition = "varchar(2) ")
	private String sta;
	@Column(name="`category`",columnDefinition = "varchar(256) ")
	private String category;
	@Column(name="`tag`",columnDefinition = "varchar(32) DEFAULT '2014-01-01 00:00:00' ")
	private String tag;//
	@Column(name="`userid`",columnDefinition = "int(11) DEFAULT 0 ")
	private Long userid;//商户ID
	
	@Transient
	private User user;
	
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
	public String getCname() {
		return cname;
	}
	public void setCname(String cname) {
		this.cname = cname;
	}
	public int getNsort() {
		return nsort;
	}
	public void setNsort(int nsort) {
		this.nsort = nsort;
	}
	public String getTyp() {
		return typ;
	}
	public void setTyp(String typ) {
		this.typ = typ;
	}
	public String getSta() {
		return sta;
	}
	public void setSta(String sta) {
		this.sta = sta;
	}
	public String getCategory() {
		return category;
	}
	public void setCategory(String category) {
		this.category = category;
	}
	public String getTag() {
		return tag;
	}
	public void setTag(String tag) {
		this.tag = tag;
	}
	public Long getUserid() {
		return userid;
	}
	public void setUserid(Long userid) {
		this.userid = userid;
	}
	public User getUser() {
		return user;
	}
	public void setUser(User user) {
		this.user = user;
	}
	
}
