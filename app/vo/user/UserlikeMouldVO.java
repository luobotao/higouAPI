package vo.user;

import java.util.List;

import utils.StringUtil;

public class UserlikeMouldVO {
	public String status;
	public int endflag;
	public String reffer;
	public List<DataInfo> data;

	public static class DataInfo {
		public String cardId;
		public String mould;
		public String struct;
		public String stxt;
		public String sdate;
		public List<LayoutInfo> layout;
		public List<PInfo> plist;
	}

	public static class LayoutInfo {
		public String start;
		public String end;
	}

	public static class PInfo {
		public String linkurl;
		public String img;
		public ProductUserLikeItem pinfo;
	}
	
	
	public static class ProductUserLikeItem{
		public String pid;                  
		public String skucode;              
		public String title;  
		public String cardMask;
		public String subtitle;             
		public String category;             
		public String price;                
		public String list_price;           
		public String discount;             
		public String imgnums;              
		public String exturl;               
		public String salesrank;            
		public String status;               
		public String date_add;             
		public String date_upd;             
		public String nlikes;               
		public String ishot;                
		public String version;              
		public String extcode;              
		public String fromsite;             
		public String currency;             
		public String imgstr;               
		public String listpic;              
		public String adstr1;               
		public String adstr3;               
		public String detail;               
		public String sort;                 
		public String chinaprice;           
		public String nstock;               
		public String nstock_autoupd;       
		public String islovely;             
		public String typ;                  
		public String weight;               
		public String freight;              
		public String wayremark;            
		public String wishcount;            
		public String activityname;         
		public String activityimage;        
		public String PromiseURL;           
		public String limitcount;           
		public String lovelydistinct;       
		public String rmbprice;             
		public String islockprice;          
		public String distinctimg;          
		public String sendmailflg;          
		public String backnstock;           
		public String specifications;       
		public String ppid;                 
		public String stitle;               
		public String isopenid;             
		public String specpic;              
		public String btim;                 
		public String etim;                 
		public String ptyp;                 
		public String deposit;              
		public String mancnt;               
		public String stage;                
		public String preselltoast;         
		public String rtitle;               
		public String num_iid;              
		public String wx_upd;               
		public String wx_flg;               
		public String stock;                
		public String paytim;               
		public String jpntitle;             
		public String jpncode;              
		public String fromsitename;         
		public String fromsiteimg;          
		public String symbol;               
		public String rate;                 
		public String DATE_ADD;             
		public String rmb_price;            
		public String rmb_price_no_symbol;            
		public Long seconds;              
		public String img_src; 
		public String typimg;
		public String nationalFlagImg;
		public String ptypeImg;
	}
}