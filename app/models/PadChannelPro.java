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

import org.apache.commons.lang3.StringUtils;

import utils.StringUtil;
/**
 * 频道商品表(针对pad)
 * @author luobotao
 * @Date 2015年8月31日
 */
@Entity
@Table(name = "padChannelPro")
public class PadChannelPro  implements Serializable{

	private static final long serialVersionUID = -4803173229855501453L;
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Long id;
	@Column(name="`date_add`",nullable=false)
	@Temporal(TemporalType.TIMESTAMP)
	private Date date_add;
	@Column(nullable=false,name="cid",columnDefinition = "int(11) ")
	private Long cid;//频道ID，此处于普通channelMouldPro不同，此表无卡片概念
	@Column(name="`pid`",nullable=false,columnDefinition = "int(11) ")
	private Long pid;
	@Column(name="`typ`",columnDefinition = "varchar(2) ")
	private String typ;//0不显示 1显示
	@Column(name="`linkurl`",columnDefinition = "varchar(256) ")
	private String linkurl;//
	@Column(name="`imgurl`",columnDefinition = "varchar(256) ")
	private String imgurl;//
	@Column(name="`nsort`",columnDefinition = "int(11) DEFAULT 0 ")
	private int nsort;// 排序
	@Column(name="`eid`",nullable=false,columnDefinition = "int(11) DEFAULT 0 ")
	private Long eid;//代言ID，当删除一个代言商品时，将对应的此条记录删除
	
	@Column(name="`typFlag`",columnDefinition = "varchar(2) DEFAULT '0' ")
	private String typFlag;//0代言商品 1代言Banner
	
	@Transient
	private Endorsement endorsement;//代言商品表
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
		if(StringUtils.isBlank(imgurl)){
			return "";
		}else{
			return StringUtil.getPICDomain()+imgurl;
		}
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
	public Long getCid() {
		return cid;
	}
	public void setCid(Long cid) {
		this.cid = cid;
	}
	public Long getEid() {
		return eid;
	}
	public void setEid(Long eid) {
		this.eid = eid;
	}
	public Endorsement getEndorsement() {
		return endorsement;
	}
	public void setEndorsement(Endorsement endorsement) {
		this.endorsement = endorsement;
	}
	public String getTypFlag() {
		return typFlag;
	}
	public void setTypFlag(String typFlag) {
		this.typFlag = typFlag;
	}
	
	
}
