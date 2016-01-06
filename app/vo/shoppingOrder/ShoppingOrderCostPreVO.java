package vo.shoppingOrder;

import java.util.List;
import models.Address;


/**
 * 订单结算返回VO
 * @author luobotao
 * @Date 2015年5月11日
 */
public class ShoppingOrderCostPreVO {

	public int status;
	public ShoppingOrderCostPreItem data;
	
	
	public static class ShoppingOrderCostPreItem {
		public List<Object> p_list;
		public String ptyp="";
		public String domestic_fee="";
		public String foreignfee="";
		public String tariff_fee="";
		public String cost_fee="";
		public String totalfee="";
		public Integer total_fee;
		public String payfee="";
		public Integer pay_fee;
		public String goods_fee="";
		public Integer money;
		public String balance="";

		public Address addressdata;
		
		public List<String> hgPaymentType;
	}
}
