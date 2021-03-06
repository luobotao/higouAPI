package vo.shoppingOrder;

import java.util.List;


/**
 * 预售订单详情返回VO
 * @author luobotao
 * @Date 2015年5月11日
 */
public class ShoppingOrderPresellDetailResultVO {

	public int status;
	public ShoppingOrderPresellDetail data;
	
	public static class ShoppingOrderPresellDetail{
	    public String id;            
	    public String orderCode;     
	    public String ordertype;     
	    public String orderCode_Pay; 
	    public String lovely_peolpe; 
	    public String lovely_speck;  
	    public String original_fee;  
	    public String totalfee;      
	    public String total_fee;     
	    public String paymethod;     
	    public String paystat;       
	    public String stage;         
	    public String status;        
	    public String orderToast;    
	    public String goods_fee;     
	    public String domestic_fee;  
	    public String foreignfee;    
	    public String tariff_fee;    
	    public String cost_fee;      
	    public String payfee;      
	    public String pay_fee;      
	    public String deposit;      
	    public String finalpay;      
	    public String final_pay;      
	    public String coupon;        
	    public String coupontyp;     
	    public String dateAddTime;   
	    public String isHavePack;    
	    public Integer packCount;     
	    public AddressPreselData addressdata;
	    
	    public String toast;                
	    public String depositPayTime;       
	    public String finalPayTime;
	    public String balance;
	    public List<Object> p_list;
	    public List<String> hgPaymentType;
	}
	
	public static class AddressPreselData{
		public String addressId = "0";
		public String province ="" ;
		public String address ="" ;
		public String name ="" ;
		public String phone ="" ;
		public String postcode ="" ;
		public String cardId ="" ;
	}
	
}
