package vo.product;

import java.util.List;

public class ProductSearchMouldDetailVO {

	private String status;
	private String totalcount="0";
	private List<ProductSearchMouldDetailItem> Data;
	
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}
	public List<ProductSearchMouldDetailItem> getData() {
		return Data;
	}

	public void setData(List<ProductSearchMouldDetailItem> data) {
		Data = data;
	}
	

	public String getTotalcount() {
		return totalcount;
	}
	public void setTotalcount(String totalcount) {
		this.totalcount = totalcount;
	}


	public static class ProductSearchMouldDetailItem{
		public String pid;           
		public String pcode;  
		public String cardMask;
		public String title;         
		public String subtitle;      
		public String rate;          
		public String symbol;        
		public String adstr1;        
		public String fromsite;      
		public String fromsiteimg;   
		public String ptyp;          
		public String nstock;        
		public Long seconds;       
		public String chinaprice;    
		public String price;         
		public String list_price;    
		public String rmb_price;     
		public String rmb_price_no_symbol;     
		public String discount;      
		public String nlikes;        
		public String is_like;       
		public String deposit;       
		public String mancnt;   
		public String ptypeImg;
		public String typimg;
		public String islovely;
		public String typ;  
		public String nationalFlagImg;
	}
}
