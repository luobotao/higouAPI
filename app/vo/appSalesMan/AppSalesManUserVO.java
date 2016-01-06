package vo.appSalesMan;

/**
 * @author luobotao
 * @Date 2015年9月22日
 */
public class AppSalesManUserVO {

	private String status; // 状态码 //1表示成功，非1表示失败
	private String msg; // 消息...
	private UserInfo userInfo;

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

	public UserInfo getUserInfo() {
		return userInfo;
	}

	public void setUserInfo(UserInfo userInfo) {
		this.userInfo = userInfo;
	}

	public static class UserInfo {
		private String uid;
		private String gender;
		private String nickname;
		private String phone;
		private String headIcon;
		private String storeID;
		private String storeName;
		public String getUid() {
			return uid;
		}
		public void setUid(String uid) {
			this.uid = uid;
		}
		public String getGender() {
			return gender;
		}
		public void setGender(String gender) {
			this.gender = gender;
		}
		public String getNickname() {
			return nickname;
		}
		public void setNickname(String nickname) {
			this.nickname = nickname;
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
		public String getStoreID() {
			return storeID;
		}
		public void setStoreID(String storeID) {
			this.storeID = storeID;
		}
		public String getStoreName() {
			return storeName;
		}
		public void setStoreName(String storeName) {
			this.storeName = storeName;
		}
		
	}
}