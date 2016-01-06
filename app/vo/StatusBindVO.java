package vo;

import java.util.List;

public class StatusBindVO {

	private String status;
	private String uid;
	private String phone="";
	private String ispwds="";
	private String nickname="";
	private String headIcon="";
	private String gender="";
	private String token="";
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
	public String getIspwds() {
		return ispwds;
	}
	public void setIspwds(String ispwds) {
		this.ispwds = ispwds;
	}
}
