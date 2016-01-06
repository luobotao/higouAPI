package vo.product;

import java.util.List;

public class ProductDetailVO {

	private String status;
	private String totalcount="0";
	private List<ProductDetailItem> Data;
	
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}
	public List<ProductDetailItem> getData() {
		return Data;
	}

	public void setData(List<ProductDetailItem> data) {
		Data = data;
	}
	

	public String getTotalcount() {
		return totalcount;
	}
	public void setTotalcount(String totalcount) {
		this.totalcount = totalcount;
	}


	public static class ProductDetailItem{
		public String reffer;
		public String pid;                       
		public String pcode;                     
		public String title;                     
		public String subtitle;                  
		public String fromsite;                  
		public String fromsiteimg;               
		public String rate;                      
		public String symbol;                    
		public String adstr3;                    
		public String linkurl;                   
		public String ratemsg;                   
		public String chinaprice;                
		public String price;                     
		public String list_price;                
		public String discount_price;            
		public String discount;                  
		public String nstock;                    
		public String limitcount;                
		public String showStockCount;            
		public String sta;                       
		public String showlogistics;             
		public String exturl;                    
		public String Html5url;                  
		public String ShareURL;                  
		public String PromisePic;                
		public String PromiseURL;                
		public String nlikes;                    
		public String is_like;                   
		public String islovely;                  
		public String typ;                       
		public String ptyp;                      
		public String weight;                    
		public String logisticsFee;              
		public String rmb_price;                 
		public String rmb_price_no_symbol;                 
		public String tariff_fee;                
		public String cost_fee;                  
		public String toast;                     
		public String logisticsDesc;             
		public String specifications;            
		public String specpic;                   
		public String ppid;                      
		public String deposit;                   
		public String pay_fee;                   
		public String finalpay;                  
		public String mancnt;                    
		public String stage;                     
		public String rtitle;                    
		public String remark;                    
		public String btm;                       
		public String etm;                       
		public String logisticsIntroURL;         
		public Long seconds;  
		public String nationalFlagImg;
		public String[] img;                       
    
	    
	}
}
