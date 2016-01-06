package vo.trafCustom;

import java.util.List;

public class WaybillInfoVO {

	public List<WAYBILL_INFO> WAYBILL_HEAD;//
	public List<GOODS_INFO> GOODS_LIST;//
	public List<ORDER_INFO> ORDER_LIST;//
	
	
	public static class WAYBILL_INFO {
		public String WAYBILL_ID;//运单号
		public String TRAF_MODE;//
		public String DECL_PORT;//
		public String IE_PORT;//
		public String TRAF_NAME;//
		public String VOYAGE_NO;//
		public String BILL_NO;//
		public String PACK_ID;//
		public String LOGI_ENTE_CODE;//
		public String LOGI_ENTE_NAME;//
		public String TOTAL_FREIGHT;//
		public String CURR_CODE;//
		public String GROSS_WEIGHT;//
		public String PACK_NUM;//
		public String CONSIGNEE_NAME;//
		public String CONSIGNEE_ADDR;//
		public String CONSIGNEE_TEL;//
		public String CONSIGNEE_COUN;//
		public String CONSIGNER_NAME;//
		public String CONSIGNER_ADDR;//
		public String CONSIGNER_COUN;//
		public String NOTE;//
	}
	
	public static class GOODS_INFO{
		public String WAYBILL_ID;//运单号
		public String LOGI_ENTE_CODE;//
		public String G_NO;//
		public String CODE_TS;//
		public String G_NAME;//
		public String G_DESC;//
		public String G_MODEL;//
		public String G_NUM;//
		public String G_UNIT;//
		public String PRICE;//
		public String CURR_CODE;//
		public String FREIGHT;//
		public String F_CURR_CODE;//
		public String ORDER_ID;//
		public String EB_PLAT_ID;//
		public String NOTE;//
	}
	
	public static class ORDER_INFO{
		public String WAYBILL_ID;//运单号
		public String LOGI_ENTE_CODE;//
		public String ORDER_ID;//
		public String EB_PLAT_ID;//
	}
}