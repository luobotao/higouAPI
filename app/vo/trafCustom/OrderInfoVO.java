package vo.trafCustom;//

import java.util.List;//

public class OrderInfoVO {
	public List<ORDER_INFO> ORDER_HEAD;//
	public List<GOODS_INFO> GOODS_LIST;//	
	public List<WAYBILL_INFO> WAYBILL_LIST;//
		
public static class ORDER_INFO {
	public String ORDER_ID;//订单号
	public String IE_FLAG;//进出口类型 I进口
	public String TRADE_MODE;//贸易方式 0 一般贸易 1保税贸易
	public String EB_CODE;// 商户代码 10位海关编码
	public String EB_NAME;//商户名称
	public String TOTAL_PAYMENT;//商品竞价
	public String CURR_CODE;//币制代码 142 是人民币
	public String BUYER_NAME;//真实姓名
	public String BUYER_CERT_TYPE;//1身份证 2护照 3军官证
	public String BUYER_CERT_ID;//证件号
	public String BUYER_COUNTRY;//110 中国
	public String BUYER_TEL;//买方电话
	public String DELIVERY_ADDR;//收货地址
	public String EB_PLAT_ID;//电商平台参数表
	public String NOTE;//备注
}

public static class GOODS_INFO {
	public String G_NO;//序号 从 1开始顺序递增
	public String CODE_TS;// 商品编码  根据商品名录编码
	public String G_NAME;//商品名称 实际商品名称
	public String G_DESC;//商品描述 与商品编码对应的描述
	public String G_MODEL;//规格型号
	public String G_NUM;//数量
	public String G_UNIT;//计量单位
	public String PRICE;//单价
	public String CURR_CODE;//币制 142 人民币
	public String NOTE;//备注
	public String ORDER_ID;//订单编号
	public String EB_PLAT_ID;//电商平台代码
}

public static class WAYBILL_INFO {
	public String WAYBILL_ID;//运单号
	public String LOGI_ENTE_CODE;//物流公司代码
	public String ORDER_ID;//订单编号
	public String G_DESC;//
	public String EB_PLAT_ID;//电商平台代码
}
}