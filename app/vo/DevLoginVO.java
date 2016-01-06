package vo;

import java.util.List;

import models.APPConfig;
import models.Share;
import play.Configuration;


public class DevLoginVO {
	private String loading;
	private String status;
	private String uid;
	private String phone;
	private String opencardId;
	private List<APPConfig> config;
	private List<Share> share;
	private VersionVo version;
	
	private String nickname;
	private String headIcon;
	private String gender;
	private String token;
	public String getToken() {
		return token;
	}
	public void setToken(String token) {
		this.token = token;
	}
	private List<String> authorities;
	
	public List<String> getAuthorities() {
		return authorities;
	}
	public void setAuthorities(List<String> authorities) {
		this.authorities = authorities;
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
	public String getGender() {
		return gender;
	}
	public void setGender(String gender) {
		this.gender = gender;
	}
	public String getLoading() {
		return loading;
	}
	public void setLoading(String loading) {
		boolean IsProduct = Configuration.root().getBoolean("production", false);
		String domain = Configuration.root().getString("domain.dev","http://ht2.neolix.cn");
		if(IsProduct){
			domain = Configuration.root().getString("domain.product","http://ht.neolix.cn");
		}
		this.loading = domain + Configuration.root().getString("adload","/pimgs/adload/") + loading;
	}
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}
	public String getUid() {
		return uid;
	}
	public void setUid(String uid) {
		this.uid = uid;
	}
	public String getPhone() {
		return phone;
	}
	public void setPhone(String phone) {
		this.phone = phone;
	}
	public String getOpencardId() {
		return opencardId;
	}
	public void setOpencardId(String opencardId) {
		this.opencardId = opencardId;
	}
	public List<APPConfig> getConfig() {
		return config;
	}
	public void setConfig(List<APPConfig> config) {
		this.config = config;
	}
	public List<Share> getShare() {
		return share;
	}
	public void setShare(List<Share> share) {
		this.share = share;
	}
	public VersionVo getVersion() {
		return version;
	}
	public void setVersion(VersionVo version) {
		this.version = version;
	}
}
