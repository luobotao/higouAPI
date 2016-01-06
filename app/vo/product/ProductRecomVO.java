package vo.product;

import java.util.List;

public class ProductRecomVO {

	public int status=0;
	public int totalnum=0;
	public String reffer;
	public List<ProductRecomItem> userlike;
	

	public static class ProductRecomItem{
	    public String pid;                     
	    public String pcode;                   
	    public String title;                   
	    public String subtitle;                
	    public String price;                   
	    public String list_price;              
	    public String exturl;                  
	    public String date_upd;                     
	    public String fromsite;                
	    public String fromsiteimg;             
	    public String symbol;                  
	    public String rate;                    
	    public String listpic;                  
	    public String rmb_price;                 
	    public String rmb_price_no_symbol;                 
	    public String discount;                 
	    public String img_src;              
	    public String linkurl;       
	    public String reffer;
	}
	
	
}
