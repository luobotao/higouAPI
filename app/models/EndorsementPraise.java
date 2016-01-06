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

/*
 * 代言点赞表
 */
@Entity
@Table(name="endorsement_praise")
public class EndorsementPraise implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 8497215726369617171L;

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Long rid;	//编号
	
	@Column
	private Long eid;//代言编号
	
	@Column
	private Long userId;//点赞用户编号
	
	@Column
	private String ImgPath;	//点赞用户头像地址
	
	@Column
	@Temporal(TemporalType.TIMESTAMP)
	private Date createTime; //点赞时间

	@Column(columnDefinition = " varchar(100) DEFAULT '' ")
	private String openId;	//微信的OPENID
	@Column(columnDefinition = " varchar(100) DEFAULT '' ")
	private String unionId;//微信的UNIONID
	@Column(columnDefinition = " varchar(100) DEFAULT '' ")
	private String nickName;//微信的妮称
	
	@Transient
	private String sex;
	
	public String getSex() {
		return sex;
	}

	public void setSex(String sex) {
		this.sex = sex;
	}

	public String getOpenId() {
		return openId;
	}

	public void setOpenId(String openId) {
		this.openId = openId;
	}

	public String getUnionId() {
		return unionId;
	}

	public void setUnionId(String unionId) {
		this.unionId = unionId;
	}

	public String getNickName() {
		return nickName;
	}

	public void setNickName(String nickName) {
		this.nickName = nickName;
	}

	public Long getRid() {
		return rid;
	}

	public void setRid(Long rid) {
		this.rid = rid;
	}

	public Long getEid() {
		return eid;
	}

	public void setEid(Long eid) {
		this.eid = eid;
	}

	public Long getUserId() {
		return userId;
	}

	public void setUserId(Long userId) {
		this.userId = userId;
	}

	public String getImgPath() {
		return ImgPath;
	}

	public void setImgPath(String imgPath) {
		ImgPath = imgPath;
	}

	public Date getCreateTime() {
		return createTime;
	}

	public void setCreateTime(Date createTime) {
		this.createTime = createTime;
	}
}
