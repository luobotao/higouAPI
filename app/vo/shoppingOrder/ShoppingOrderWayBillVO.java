package vo.shoppingOrder;

import java.util.List;


/**
 * 订单物流信息查询返回VO
 * @author luobotao
 * @Date 2015年5月11日
 */
public class ShoppingOrderWayBillVO {
	
	public int status;
	public String orderType;
	public String nowflag;
	public String packagestatus;
	public String src;
	public List<ShoppingOrderWayBillItem> data;
	
	public static class ShoppingOrderWayBillItem{
		public String waybillCode;
		public String transport;
		public String date_txt;
		public String remark;
	}
}
