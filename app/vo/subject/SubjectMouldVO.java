package vo.subject;

import java.util.List;

import vo.product.ProductDetailCostpresellVO.ProductDetailCostpresellItem;
import vo.product.ProductDetailVO.ProductDetailItem;

/**
 * @author yangtao
 *
 * @data 2015年5月9日 上午11:22:33
 */
public class SubjectMouldVO {
	public String status;
	public String refreshnum;
	public String retag;
	public int endflag;
	public String subjectId;
	public String subjectName;
	public String reffer;
	public List<DataInfo> data;

	public static class DataInfo {
		public String cardId;
		public String mould;
		public String struct;
		public String sectionPic;
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
		public ProductSubjectItem pinfo;
	}
	
	public static class ProductSubjectItem{
		public String pid;               
		public String pcode;  
		public String cardMask;
		public String title;             
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
		public String ptypeImg;
		public String nationalFlagImg;
	}
}