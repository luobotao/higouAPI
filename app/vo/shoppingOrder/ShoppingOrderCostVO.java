package vo.shoppingOrder;

import java.util.List;

import models.Address;


/**
 * 订单结算返回VO
 * @author luobotao
 * @Date 2015年5月11日
 */
public class ShoppingOrderCostVO {

	public int status;
	public ShoppingOrderCostItem data;
	
	
	public static class ShoppingOrderCostItem{
		public List<Object> p_list;
		public Integer domestic_fee =0;          
		public String foreignfee="0";            
		public Integer tariff_fee=0;            
		public Integer cost_fee=0;              
		public String totalfee="";              
		public Integer total_fee=0;             
		public String goods_fee="";             
		public Integer money=0;                 
		public String coupon="";                
		public String coupontyp="0";             
		public String couponfee="0";             
		public String fname="";             
		public String wayremark="";             
		public Integer couponcount=0;
		public String balance="";
		public Address addressdata;
		public List<String> hgPaymentType;
	}
	
	public static class ShoppingOrderCostItemWithTotalfee{
		public List<Object> p_list;
		public Double domestic_fee;          
		public String foreignfee;            
		public Double tariff_fee;            
		public Double cost_fee;              
		public String totalfee;              
		public Double total_fee;             
		public String goods_fee;             
		public Double money;                 
		public String coupon;                
		public String coupontyp;             
		public String couponfee;             
		public Integer couponcount;          
		public List<Address> addressdata;
	}

}
