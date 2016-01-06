package vo.appSalesMan;

import java.util.List;

/**
 * @author luobotao
 * @Date 2015年9月22日
 */
public class AppSalesManCustomerVO {
	// 状态 0：失败 1：成功
	private String status;
	// 返回提示（不管请求状态，有值就提示）
	private String msg;
	private String endflag;
	private List<dataInfo> data;
	

	public String getEndflag() {
		return endflag;
	}
	public void setEndflag(String endflag) {
		this.endflag = endflag;
	}


	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}
	


	public List<dataInfo> getData() {
		return data;
	}
	public void setData(List<dataInfo> data) {
		this.data = data;
	}
	public String getMsg() {
		return msg;
	}
	public void setMsg(String msg) {
		this.msg = msg;
	}
	
	
	public static class dataInfo{
		private String indexNum;
		private List<customerInfo> customerList;
		
		public List<customerInfo> getCustomerList() {
			return customerList;
		}
		public void setCustomerList(List<customerInfo> customerList) {
			this.customerList = customerList;
		}
		
		public String getIndexNum() {
			return indexNum;
		}
		public void setIndexNum(String indexNum) {
			this.indexNum = indexNum;
		}
	
	
	
	}
	
	public static class customerInfo{
		public String name="";
	}
}