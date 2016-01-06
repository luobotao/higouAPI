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
 * 用户实体
 * @author luobotao
 * @Date 2015年5月5日
 */
@Entity
@Table(name = "user")
public class User implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -4911407432201286423L;

	@Id
	@GeneratedValue
	private Long uid;
	
	private String nickname;
	
	@Column(columnDefinition = "tinyint(4) NOT NULL DEFAULT '0' COMMENT '0 new;1 wx;2 weibo'")
	private int cologin;
	
	private String token;
	
	@Column(columnDefinition = "tinyint(1) unsigned NOT NULL DEFAULT '1' ")
	private int active;
	@Column(name="`date_add`")
	@Temporal(TemporalType.TIMESTAMP)
	private Date date_add;
	@Column
	@Temporal(TemporalType.TIMESTAMP)
	private Date date_upd;
	private String usid;
	@Column(columnDefinition = "varchar(512) ")
	private String headIcon;
	@Column(columnDefinition = "varchar(64) ")
	private String catstr;
	@Column(columnDefinition = "varchar(16) DEFAULT '' ")
	private String phone;
	@Column(columnDefinition = "varchar(32) DEFAULT '' ")
	private String appversion;
	@Column(columnDefinition = "varchar(32) DEFAULT '' ")
	private String marketCode;
	@Column(columnDefinition = "varchar(128) DEFAULT '' ")
	private String openId;
	@Column(columnDefinition = "varchar(128) DEFAULT '' ")
	private String unionid;
	
	@Column(columnDefinition = "int(11) DEFAULT '1' ")
	private int platform;
	@Column(columnDefinition = "varchar(10) DEFAULT '' ")
	private String passwords;
	@Column(columnDefinition = "varchar(20) DEFAULT '' ")
	private String postmanid;//快递员编号
	/*
	 * 是否代言 0未代言，1已代言
	 */
	@Column(columnDefinition = "int(11) DEFAULT '0' ")
	private int isEndorsement;
	
	/*
	 * 代言暗号
	 */
	@Column(columnDefinition = "varchar(100) DEFAULT '' ")
	private String endorsementCode;
	/*
	 * 性别
	 */
	@Column(columnDefinition = "varchar(10) unsigned NOT NULL DEFAULT '' ")
	private String sex;
	/*
	 * 绑定帐户
	 */
	@Column(columnDefinition="varchar(100) DEFAULT ''")
	private String cardNO;
	/*
	 * 邦定帐户类型1银行卡,2支付宝,3微信
	 */
	@Column(columnDefinition="int(4) DEFAULT 1")
	private int cardType;
	
	/*
	 * 客户端是否修改过头像0未修改，1修改
	 */
	@Column(columnDefinition="int(1) DEFAULT 0")
	public int isHeadimgEdit;
	
	/*
	 * 用户分享类型  1 普通 2 大图 3 待二维码
	 */
	@Column(columnDefinition="varchar(2) DEFAULT '1'")
	public String shareType;
	
	@Column(columnDefinition="varchar(20) DEFAULT ''")
	private String mcode;//推广来源码
	
	@Transient
	private Long pingid;//评论编号
	
	@Transient
	private Integer editor;//是否是小编　０非，１是

	@Column(columnDefinition="int(11) DEFAULT '1'")
	 private Long gid;//用户组ID 1普通用户组2新用户组3旧用户组4商户组(为商户数据做此字段)
	 @Column(columnDefinition="int(11) DEFAULT '0'")
	 private Integer province;//省份ID 值应去bbtaddress表里进行查询(为商户数据做此字段)
	 @Column(columnDefinition="int(11) DEFAULT '0'")
	 private Integer city;//城市ID 值应去bbtaddress表里进行查询(为商户数据做此字段)
	 @Column(columnDefinition="varchar(200) DEFAULT ''")
	 private String address;//地址(为商户数据做此字段)
	 @Column(columnDefinition="varchar(20) DEFAULT ''")
	 private String contactPerson;//联系人(为商户数据做此字段)
	 @Column(name="contactPhone",columnDefinition="varchar(20) DEFAULT ''")
	 private String contactPhone;//联系电话(为商户数据做此字段)
	 @Column(name="`state`",columnDefinition = "tinyint(1) unsigned NOT NULL DEFAULT '1' ")
	 private int state;//1启用 0停用
	public Integer getEditor() {
		return editor;
	}

	public void setEditor(Integer editor) {
		this.editor = editor;
	}

	public Long getPingid() {
		return pingid;
	}

	public void setPingid(Long pingid) {
		this.pingid = pingid;
	}

	public String getMcode() {
		return mcode;
	}

	public void setMcode(String mcode) {
		this.mcode = mcode;
	}

	public String getShareType() {
		return shareType;
	}

	public void setShareType(String shareType) {
		this.shareType = shareType;
	}

	public int getIsHeadimgEdit() {
		return isHeadimgEdit;
	}

	public void setIsHeadimgEdit(int isHeadimgEdit) {
		this.isHeadimgEdit = isHeadimgEdit;
	}

	/*
	 * 微信返回的access_token
	 */
	@Transient
	private String accessToken;
	
	public String getAccessToken() {
		return accessToken;
	}

	public void setAccessToken(String accessToken) {
		this.accessToken = accessToken;
	}

	public String getCardNO() {
		return cardNO;
	}

	public void setCardNO(String cardNO) {
		this.cardNO = cardNO;
	}

	public int getCardType() {
		return cardType;
	}

	public void setCardType(int cardType) {
		this.cardType = cardType;
	}

	public String getSex() {
		return sex;
	}

	public void setSex(String sex) {
		this.sex = sex;
	}

	
	
	public String getEndorsementCode() {
		return endorsementCode;
	}

	public void setEndorsementCode(String endorsementCode) {
		this.endorsementCode = endorsementCode;
	}

	public int getIsEndorsement() {
		return isEndorsement;
	}

	public void setIsEndorsement(int isEndorsement) {
		this.isEndorsement = isEndorsement;
	}

	public Long getUid() {
		return uid;
	}

	public void setUid(Long uid) {
		this.uid = uid;
	}

	public String getNickname() {
		return nickname;
	}

	public void setNickname(String nickname) {
		this.nickname = nickname;
	}

	public String getHeadIcon() {
		return headIcon;
	}

	public void setHeadIcon(String headIcon) {
		this.headIcon = headIcon;
	}

	public String getPhone() {
		return phone;
	}

	public void setPhone(String phone) {
		this.phone = phone;
	}

	public String getAppversion() {
		return appversion;
	}

	public void setAppversion(String appversion) {
		this.appversion = appversion;
	}

	public String getMarketCode() {
		return marketCode;
	}

	public void setMarketCode(String marketCode) {
		this.marketCode = marketCode;
	}

	public String getOpenId() {
		return openId;
	}

	public void setOpenId(String openId) {
		this.openId = openId;
	}

	public String getUnionid() {
		return unionid;
	}

	public void setUnionid(String unionid) {
		this.unionid = unionid;
	}

	public String getPasswords() {
		return passwords;
	}

	public void setPasswords(String passwords) {
		this.passwords = passwords;
	}

	public int getCologin() {
		return cologin;
	}

	public void setCologin(int cologin) {
		this.cologin = cologin;
	}

	public String getToken() {
		return token;
	}

	public void setToken(String token) {
		this.token = token;
	}

	public int getActive() {
		return active;
	}

	public void setActive(int active) {
		this.active = active;
	}

	public Date getDate_add() {
		return date_add;
	}

	public void setDate_add(Date date_add) {
		this.date_add = date_add;
	}

	public Date getDate_upd() {
		return date_upd;
	}

	public void setDate_upd(Date date_upd) {
		this.date_upd = date_upd;
	}

	public String getUsid() {
		return usid;
	}

	public void setUsid(String usid) {
		this.usid = usid;
	}

	public String getCatstr() {
		return catstr;
	}

	public void setCatstr(String catstr) {
		this.catstr = catstr;
	}

	public int getPlatform() {
		return platform;
	}

	public void setPlatform(int platform) {
		this.platform = platform;
	}

	public Long getGid() {
		return gid;
	}

	public void setGid(Long gid) {
		this.gid = gid;
	}

	public Integer getProvince() {
		return province;
	}

	public void setProvince(Integer province) {
		this.province = province;
	}

	public Integer getCity() {
		return city;
	}

	public void setCity(Integer city) {
		this.city = city;
	}

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public String getContactPerson() {
		return contactPerson;
	}

	public void setContactPerson(String contactPerson) {
		this.contactPerson = contactPerson;
	}

	public String getContactPhone() {
		return contactPhone;
	}

	public void setContactPhone(String contactPhone) {
		this.contactPhone = contactPhone;
	}

	public int getState() {
		return state;
	}

	public void setState(int state) {
		this.state = state;
	}

	public String getPostmanid() {
		return postmanid;
	}

	public void setPostmanid(String postmanid) {
		this.postmanid = postmanid;
	}
	
}
