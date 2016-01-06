package vo.appSalesMan;

import java.util.List;

/**
 * @author luobotao
 * @Date 2015年9月22日
 */
public class AppSalesManOrderNewVO {
	private String status;
	private String msg;
	private dInfo data;
	
	
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


	public dInfo getData() {
		return data;
	}


	public void setData(dInfo data) {
		this.data = data;
	}


	public static class dInfo{
		public String orderCode="";
		public String payCode="";
		public String orderId="";
		public String thirdPartyPaySign="";
	}
	
	
}