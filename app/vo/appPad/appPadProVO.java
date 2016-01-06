package vo.appPad;

import java.util.List;

public class appPadProVO {
	// 状态 0：失败 1：成功
	private String status;
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

	public List<productInfo> getProductlist() {
		return productlist;
	}

	public void setProductlist(List<productInfo> productlist) {
		this.productlist = productlist;
	}

	// 返回提示（不管请求状态，有值就提示）
	private String msg;
	
	private List<productInfo> productlist;

	public static class productInfo{
		public String pid;
		public String linkurl;
		public String img;
		public String title;
		public String subtitle;
		public String price;
		public String listprice;
		public String discount;
		public String qrcodeimg;
		public String html5url;
		public String nationalflagimg;
		public String fromsite;
		public String fromsiteimg;
		public String logisticsfee;
		public String logisticsdesc;
		public String nstock;
		public String soldoutimg;
		public String weight;
		public String nationalflag;
		public String discountdesc;
	}
	
}