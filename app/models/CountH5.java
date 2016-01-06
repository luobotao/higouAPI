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
@Table(name = "count_h5")
public class CountH5 implements Serializable {
	private static final long serialVersionUID = 2483896093444400505L;

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Long id;
	
	@Temporal(TemporalType.TIMESTAMP)
	@Column
	private Date createTime;
	
	@Column
	private String ip;	//ip来源
	
	@Column
	private String url;//被访问URL
	@Column(columnDefinition = "varchar(2) DEFAULT ''")
	private String shareType;//分享来源1微信，2微信短图，3微信长图
	@Column(columnDefinition = "varchar(2) DEFAULT '0'")
	private String iswx;//是否微信访问0其它，1微信
	@Column
	private String channel;//频道
	@Column
	private Long userId;//访问用户编号
	
	@Column
	private String unionid;
	
	@Column
	private Long daiyanid;//代言编号
	
	
	public Long getDaiyanid() {
		return daiyanid;
	}
	public void setDaiyanid(Long daiyanid) {
		this.daiyanid = daiyanid;
	}
	public String getUnionid() {
		return unionid;
	}
	public void setUnionid(String unionid) {
		this.unionid = unionid;
	}
	public Long getUserId() {
		return userId;
	}
	public void setUserId(Long userId) {
		this.userId = userId;
	}
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public Date getCreateTime() {
		return createTime;
	}
	public void setCreateTime(Date createTime) {
		this.createTime = createTime;
	}
	public String getIp() {
		return ip;
	}
	public void setIp(String ip) {
		this.ip = ip;
	}
	public String getUrl() {
		return url;
	}
	public void setUrl(String url) {
		this.url = url;
	}
	public String getShareType() {
		return shareType;
	}
	public void setShareType(String shareType) {
		this.shareType = shareType;
	}
	public String getIswx() {
		return iswx;
	}
	public void setIswx(String iswx) {
		this.iswx = iswx;
	}
	public String getChannel() {
		return channel;
	}
	public void setChannel(String channel) {
		this.channel = channel;
	}

	
}
