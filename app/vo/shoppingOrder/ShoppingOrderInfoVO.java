package vo.shoppingOrder;

import java.util.List;


/**
 * 订单info查询返回VO
 * @author luobotao
 * @Date 2015年5月11日
 */
public class ShoppingOrderInfoVO {
	
	public static class FromSiteItem{
		public String fromsite;
		public String fromsiteimg;
		public String typ;
		public String wayremark;
	}
	
	public static class ProductItem{
		public String fromsite;          
		public String fromsiteimg;       
		public String typ;               
		public String pid;               
		public String pcode;             
		public String title;
		public String linkurl;           
		public String iscoupon;          
		public String rmbprice;          
		public String rmb_price;         
		public String img;               
		public String stage;             
		public String specifications;    
		public String counts;
		public String rtitle;                    
		public String remark;   

	}
	
	public static class WeightItem{
		public String typ;
		public String freight;
	}
}
