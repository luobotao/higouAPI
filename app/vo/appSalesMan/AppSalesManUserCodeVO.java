package vo.appSalesMan;

import java.util.List;

/**
 * @author luobotao
 * @Date 2015年9月22日
 */
public class AppSalesManUserCodeVO {
	
	 private String status; // 状态码 //1表示成功，非1表示失败
     private String msg ; //消息...
     private String storeName;
     private String storeID;
     private String registCode;
     
	public String getRegistCode() {
		return registCode;
	}
	public void setRegistCode(String registCode) {
		this.registCode = registCode;
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
	public String getStoreName() {
		return storeName;
	}
	public void setStoreName(String storeName) {
		this.storeName = storeName;
	}
	public String getStoreID() {
		return storeID;
	}
	public void setStoreID(String storeID) {
		this.storeID = storeID;
	}
     
}