package vo.product;


public class ProductLovelyVO {

	private int status;
	private ProductLovelyItem Data;

	
	public int getStatus() {
		return status;
	}


	public void setStatus(int status) {
		this.status = status;
	}


	public ProductLovelyItem getData() {
		return Data;
	}


	public void setData(ProductLovelyItem data) {
		Data = data;
	}


	public static class ProductLovelyItem {

		public String pid;
		public String title;
		public String lovelydistinct;
		public String rmb_price;
		public String linkurl;
		public String lovely_price;
		public String rmbprice;
		public String lovely_image;
		public String shareImg;
		public String lovely_remark;
		public String lovely_shareURL;
		public String lovelyRule;
		public String img;
		public String reffer;

	}
}
