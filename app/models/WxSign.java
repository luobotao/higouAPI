package models;

import java.io.Serializable;

public class WxSign implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String appId;//公众号ID
	private String nostr;//系统随机数
	private String timstr; //系统时间串
	private String sign;//加密签名
	private String sharetitle;//分享标题
	private String shareimg;//分享图标
	private String shareurl;//分享URL
	private String sharecontent;//分享内容
	
	private String code;
	private String state;
	private String access_token;
	private String openid;
	private String unionid;
	
	
	public String getCode() {
		return code;
	}
	public void setCode(String code) {
		this.code = code;
	}
	public String getState() {
		return state;
	}
	public void setState(String state) {
		this.state = state;
	}
	public String getAccess_token() {
		return access_token;
	}
	public void setAccess_token(String access_token) {
		this.access_token = access_token;
	}
	public String getOpenid() {
		return openid;
	}
	public void setOpenid(String openid) {
		this.openid = openid;
	}
	public String getUnionid() {
		return unionid;
	}
	public void setUnionid(String unionid) {
		this.unionid = unionid;
	}
	public String getSharecontent() {
		return sharecontent;
	}
	public void setSharecontent(String sharecontent) {
		this.sharecontent = sharecontent;
	}
	public String getAppId() {
		return appId;
	}
	public void setAppId(String appId) {
		this.appId = appId;
	}
	public String getNostr() {
		return nostr;
	}
	public void setNostr(String nostr) {
		this.nostr = nostr;
	}
	public String getTimstr() {
		return timstr;
	}
	public void setTimstr(String timstr) {
		this.timstr = timstr;
	}
	public String getSign() {
		return sign;
	}
	public void setSign(String sign) {
		this.sign = sign;
	}
	public String getSharetitle() {
		return sharetitle;
	}
	public void setSharetitle(String sharetitle) {
		this.sharetitle = sharetitle;
	}
	public String getShareimg() {
		return shareimg;
	}
	public void setShareimg(String shareimg) {
		this.shareimg = shareimg;
	}
	public String getShareurl() {
		return shareurl;
	}
	public void setShareurl(String shareurl) {
		this.shareurl = shareurl;
	}
	
}
