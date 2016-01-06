package vo;

import java.util.List;

/**
 * @author yangtao
 *
 * @data 2015年5月5日 上午10:36:37
 */
public class UserRegisterVO  {

	private String status;
    private String msg;
    private int uid;
    private String phone="";
    private String headIcon="";
    private String nickname="";
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

	public String getMsg() {
		return msg;
	}

	public void setMsg(String msg) {
		this.msg = msg;
	}

	public int getUid() {
		return uid;
	}

	public void setUid(int uid) {
		this.uid = uid;
	}

	public String getPhone() {
		return phone;
	}

	public void setPhone(String phone) {
		this.phone = phone;
	}

	public String getHeadIcon() {
		return headIcon;
	}

	public void setHeadIcon(String headIcon) {
		this.headIcon = headIcon;
	}

	public String getNickname() {
		return nickname;
	}

	public void setNickname(String nickname) {
		this.nickname = nickname;
	}

	
}
