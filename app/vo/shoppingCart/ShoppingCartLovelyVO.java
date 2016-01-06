package vo.shoppingCart;

import java.util.List;

public class ShoppingCartLovelyVO {

	public String status;
	public String lovelytxt;
	public String lovelydistinct;
	public String lovelyimg;
	public String shareImg;
	public String lovelyurl;
	public String lovelyRule	;
	public String domestic_fee;
	public String foreignfee;
	public String tariff_fee;
	public String cost_fee;
	public String totalfee;
	public String goods_fee;
	public String money;
	public List<ShoppingCartLovelyItem> data;
	
	public static class ShoppingCartLovelyItem{
		public String pid;
		public String title;
		public String count;
		public String linkurl;
		public Double price;
		public String rmb_price;
		public String rmbprice;
		public String img;
	}
	
}
