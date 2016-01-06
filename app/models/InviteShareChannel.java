package models;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
/*
 * 邀请方案
 */
@Entity
@Table(name = "invite_share_channel")
public class InviteShareChannel implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 3057293741657024527L;
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Long id;
	@Column
	private String title;//方案标题
	@Column
	private String remark;	//描述
	@Column
	private String linkurl;//分享链接
	
	@Column
	private String icon;//图标
	
	@Column
	private Integer islogin;//0不需要登录显示，1需要登录显示
	@Column
	private String version;//版本
	
	public String getIcon() {
		return icon;
	}
	public void setIcon(String icon) {
		this.icon = icon;
	}
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
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
	public String getLinkurl() {
		return linkurl;
	}
	public void setLinkurl(String linkurl) {
		this.linkurl = linkurl;
	}
	public Integer getIslogin() {
		return islogin;
	}
	public void setIslogin(Integer islogin) {
		this.islogin = islogin;
	}
	public String getVersion() {
		return version;
	}
	public void setVersion(String version) {
		this.version = version;
	}
	
	
}
