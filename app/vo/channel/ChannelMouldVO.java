package vo.channel;

import java.util.List;

import vo.product.ProductDetailVO.ProductDetailItem;

public class ChannelMouldVO {
	public String status;
	public String refreshnum;
	public String retag;
	public int endflag;
	public String channelId;
	public List<ChannelDataInfo> data;

	public static class ChannelDataInfo {
		public String reffer;
		public String cardId;
		public String mould;
		public String struct;
		public String sectionPic;
		public String stxt;
		public String sdate;
		public List<LayoutInfo> layout;
		public List<ChannelPInfo> plist;
	}

	public static class LayoutInfo {
		public String start;
		public String end;
	}

	public static class ChannelPInfo {
		public String linkurl;
		public String img;
		public String countDownSeconds;
		public String countDownTitle;
		public ProductChannelItem pinfo;
	}
	
	public static class ProductChannelItem{
		public String pid;               
		public String pcode;             
		public String title;
		public String cardMask;
		public String subtitle;          
		public String fromsite;          
		public String fromsiteimg;       
		public Double rate;              
		public String symbol;            
		public String adstr1;            
		public String adstr3;            
		public String ptyp;
		public String nstock;
		public Long seconds;           
		public String chinaprice;        
		public String price;             
		public String list_price;        
		public String discount_price;    
		public String discount;          
		public String rmb_price;         
		public String rmb_price_no_symbol;         
		public String nlikes;            
		public String is_like;           
		public String islovely;
		public String lovelydistinct;    
		public String typ; 
		public String typimg;
		public String weight;            
		public String logisticsFee;      
		public String logisticsDesc;     
		public String deposit;           
		public String mancnt;   
		public String nationalFlagImg;
		public String ptypeImg;
	}
}