package vo;

/**
 * @author yangtao
 *
 * @data 2015年5月5日 上午10:36:37
 */
public class UserHuanXinVO  {

	private String status;
    private String msg;
    private String emLoginID="";
    private String emLoginPwd="";
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
	public String getEmLoginID() {
		return emLoginID;
	}
	public void setEmLoginID(String emLoginID) {
		this.emLoginID = emLoginID;
	}
	public String getEmLoginPwd() {
		return emLoginPwd;
	}
	public void setEmLoginPwd(String emLoginPwd) {
		this.emLoginPwd = emLoginPwd;
	}
 

	
}
