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
/**
 * 频道卡片商品表
 * @author luobotao
 * @Date 2015年8月6日
 */
@Entity
@Table(name = "channel_mould_pro")
public class ChannelMouldPro  implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 5352723686497145126L;
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Long id;
	@Column(name="`date_add`",nullable=false)
	@Temporal(TemporalType.TIMESTAMP)
	private Date date_add;
	@Column(nullable=false,name="cmid",columnDefinition = "int(11) ")
	private Long cmid;
	@Column(nullable=false,columnDefinition = "int(11) ")
	private Long pid;
	@Column(columnDefinition = "varchar(2) ")
	private String typ;
	@Column(columnDefinition = "varchar(256) ")
	private String linkurl;//
	@Column(columnDefinition = "varchar(256) ")
	private String imgurl;//
	@Column(columnDefinition = "int(11) DEFAULT 0 ")
	private int nsort;// 排序
	@Column(columnDefinition = "int(11) DEFAULT 0 ")
	private Long mouldId;// 
	@Column(name="beginTime")
	@Temporal(TemporalType.TIMESTAMP)
	private Date beginTime;
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
	public Long getCmid() {
		return cmid;
	}
	public void setCmid(Long cmid) {
		this.cmid = cmid;
	}
	public Long getPid() {
		return pid;
	}
	public void setPid(Long pid) {
		this.pid = pid;
	}
	public String getTyp() {
		return typ;
	}
	public void setTyp(String typ) {
		this.typ = typ;
	}
	public String getLinkurl() {
		return linkurl;
	}
	public void setLinkurl(String linkurl) {
		this.linkurl = linkurl;
	}
	public String getImgurl() {
		return imgurl;
	}
	public void setImgurl(String imgurl) {
		this.imgurl = imgurl;
	}
	public int getNsort() {
		return nsort;
	}
	public void setNsort(int nsort) {
		this.nsort = nsort;
	}
	public Long getMouldId() {
		return mouldId;
	}
	public void setMouldId(Long mouldId) {
		this.mouldId = mouldId;
	}
	public Date getBeginTime() {
		return beginTime;
	}
	public void setBeginTime(Date beginTime) {
		this.beginTime = beginTime;
	}
	
}
