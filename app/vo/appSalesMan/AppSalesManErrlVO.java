package vo.appSalesMan;

import java.util.List;

import com.fasterxml.jackson.databind.node.ObjectNode;

import models.Address;
import vo.shoppingOrder.ShoppingOrderCostVO.ShoppingOrderCostItem;

public class AppSalesManErrlVO {
	public String status;
	public String msg;
	public ObjectNode data;
}