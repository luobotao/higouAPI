package vo.shoppingOrder;

import java.util.List;


/**
 * 订单查询返回VO
 * @author luobotao
 * @Date 2015年5月11日
 */
public class ShoppingOrderResultVO {

	public int status;
	public String msg="";
	public String endflag;
	public List<ShoppingOrderItem> data;
	
	public static class ShoppingOrderItem{
		public String id;
		public String ordercode;
		public String ordertype;
		public String deposit;
		public String finalpay;
		public String finalDate;
		public String totalfee;
		public String status;
		public String refund_amount;
		public String lovelydistinct;
		public List<PackageItem> packagelist;
	}
	
	public static class PackageItem{
		public String packId;
		public String src;
		public String packagecode;
		public String packagestatus;
		public List<PackageProductItem> packagelist;
	}
	public static class PackageProductItem{
		public String pid;
		public String skucode;
		public String title;
		public String exturl;
		public String listpic;
		public String specifications;
		public String counts;
		public String price;
		public Double rmbprice;
		public String toast;
		public String paytime;
	}
}
