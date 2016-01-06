package vo.appSalesMan;

import java.util.List;

import models.Address;
import vo.shoppingOrder.ShoppingOrderCostVO.ShoppingOrderCostItem;

public class AppSalesManOrderCostlVO {
	public int status;
	public String msg;
	public ShoppingOrderCostItem data;
	
	
	public static class ShoppingOrderCostItem{
		public List<PInfo> p_list;
		public String totalfee="";              
		public Integer total_fee=0;             
		public String carriagefee="0";
		public String costfee="";
		public String tarifffee="";
		public List<String> hgPaymentType;
	}
	
	
	public static class PInfo{
		public String pid = "0";
		public String linkurl ="" ;
		public String title ="" ;
		public String chinaprice ="" ;
		public String china_price ="" ;
		public String img ="" ;
		public String limitcount="";
		public String counts ="" ;
	}
}