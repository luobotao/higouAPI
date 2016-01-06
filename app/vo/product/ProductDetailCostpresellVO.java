package vo.product;

import java.util.List;

public class ProductDetailCostpresellVO {

	private String status;
	private String totalcount="0";
	private List<ProductDetailCostpresellItem> Data;
	
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}
	public List<ProductDetailCostpresellItem> getData() {
		return Data;
	}

	public void setData(List<ProductDetailCostpresellItem> data) {
		Data = data;
	}
	

	public String getTotalcount() {
		return totalcount;
	}
	public void setTotalcount(String totalcount) {
		this.totalcount = totalcount;
	}


	public static class ProductDetailCostpresellItem{
		
		public String fromsite;           
		public String fromsiteimg;        
		public String pid;                
		public String pcode;              
		public String title;              
		public String subtitle;           
		public String rate;               
		public String symbol;             
		public String adstr1;             
		public String linkurl;            
		public String chinaprice;         
		public String price;              
		public String list_price;         
		public String logisticsFee;       
		public String rmb_price;          
		public String rmb_price_no_symbol;          
		public String rmbprice;           
		public String discount_price;     
		public String discount;           
		public String img;                
		public String counts;             
		public String iscoupon;           
		public String pay_fee;            
		public String specifications;     
		public String deposit;            
		public String finalpay;           
		public String final_pay;          
		public String paytim;             
		public String rtitle;             
		public String remark;             
   
	    
	}
}
