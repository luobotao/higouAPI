package vo.appSalesMan;

import java.util.List;

/**
 * @author luobotao
 * @Date 2015年9月22日
 */
public class AppSalesManHomePageVO {
	// 状态 0：失败 1：成功
	private String status;
	private List<dataInfo> data;
	


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
	
	public static class dataInfo{
		public String leftmoduleTitle=""; 
		public String leftmoduleBackImage="";
		public String leftmoduleLink="";
		public String leftmoduleSubNum="";
		public String rightmoduleTitle=""; 
		public String rightmoduleBackImage="";
		public String rightmoduleLink="";
		public String rightmoduleSubNum="";
	}
}