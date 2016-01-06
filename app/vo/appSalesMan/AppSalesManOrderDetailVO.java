package vo.appSalesMan;

import java.util.List;

public class AppSalesManOrderDetailVO {
	public int status;
	public ShoppingOrderDetail data;
	
	
	public int getStatus() {
		return status;
	}

	public void setStatus(int status) {
		this.status = status;
	}

	public ShoppingOrderDetail getData() {
		return data;
	}

	public void setData(ShoppingOrderDetail data) {
		this.data = data;
	}

	public static class ShoppingOrderDetail{
		public String id;            
	    public String orderCode;
	    public String orderTime;
	    public String paystatus;
	    public String needpay;
	    public String carriagefee;    
	    public String tarifffee;    
	    public String totalfee;
	    public String total_fee;
	    public String costfee;
	    public String orderstatus;
	    
	    public AddressData addressdata;
	    public List<PInfo> p_list;
	    public List<String> hgPaymentType;
	}
	
	public static class AddressData{
		public String addressId = "0";
		public String city ="" ;
		public String address ="" ;
		public String name ="" ;
		public String phone ="" ;
		public String cardId ="" ;
	
	}
	
	public static class PInfo{
		public String pid = "0";
		public String linkurl ="" ;
		public String title ="" ;
		public String chinaprice ="" ;
		public String china_price ="" ;
		public String img ="" ;
		public String limitcount ="" ;
		public String counts ="" ;
	}
}