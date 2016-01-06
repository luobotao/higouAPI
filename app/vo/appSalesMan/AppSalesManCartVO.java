package vo.appSalesMan;

import java.util.List;

/**
 * @author luobotao
 * @Date 2015年9月22日
 */
public class AppSalesManCartVO {
	// 状态 0：失败 1：成功
	private String status;
	private String exemptionimg;
	private String exemptiontxt; 
	private String descriptionimg;
	private List<dataInfo> data;	
	
	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getExemptionimg() {
		return exemptionimg;
	}

	public void setExemptionimg(String exemptionimg) {
		this.exemptionimg = exemptionimg;
	}

	public String getExemptiontxt() {
		return exemptiontxt;
	}

	public void setExemptiontxt(String exemptiontxt) {
		this.exemptiontxt = exemptiontxt;
	}

	public String getDescriptionimg() {
		return descriptionimg;
	}

	public void setDescriptionimg(String descriptionimg) {
		this.descriptionimg = descriptionimg;
	}

	public List<dataInfo> getData() {
		return data;
	}

	public void setData(List<dataInfo> data) {
		this.data = data;
	}
	
	public static class dataInfo{
		public String pid; 
		public String linkurl; 
		public String title; 
		public String chinaprice; 
		public String china_price; 
        public String img; 
        public String limitcount; 
        public String counts;
	}
}