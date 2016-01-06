package vo.user;

import java.util.List;

public class UserGuessLikeMouldVO {
	public int status;
	public String totalnum;
	public String date_txt;
	public List<ProductGuessLikeItem> userlike;
	
	
	
public static class ProductGuessLikeItem{
		public String reffer;
	    public String pid;                     
	    public String pcode;                   
	    public String title;                   
	    public String subtitle;                
	    public String exturl;                  
	    public String rate;                    
	    public String chinaprice;              
	    public String price;                   
	    public String list_price;              
	    public String rmb_price;               
	    public String rmb_price_no_symbol;               
	    public String fromsite;                
	    public String fromsiteimg;             
	    public String discount;                
	    public String img_src;                
	    public String linkurl;                
	    public String adstr1;                
	    public String nlikes;                
	    public String nstock;                
	    public String is_like;                
	    public String date_txt;                
	                     

	    
	}

}