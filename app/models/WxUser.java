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
 *微认鉴权表 
 */
@Entity
@Table(name = "wx_user")
public class WxUser implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = -5139981429177632113L;
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Long uid;
	@Column
	@Temporal(TemporalType.TIMESTAMP)
	private Date createTime;
	@Column
	private String unionid;
	@Column
	private String nickname;
	@Column
	private String headicon;
	public Long getUid() {
		return uid;
	}
	public void setUid(Long uid) {
		this.uid = uid;
	}
	public Date getCreateTime() {
		return createTime;
	}
	public void setCreateTime(Date createTime) {
		this.createTime = createTime;
	}
	public String getUnionid() {
		return unionid;
	}
	public void setUnionid(String unionid) {
		this.unionid = unionid;
	}
	public String getNickname() {
		return nickname;
	}
	public void setNickname(String nickname) {
		this.nickname = nickname;
	}
	public String getHeadicon() {
		return headicon;
	}
	public void setHeadicon(String headicon) {
		this.headicon = headicon;
	}
	
	
}
