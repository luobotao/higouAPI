package vo.trafCustom;//

import java.util.List;//

/**
 * @author yangtao
 *
 * @data 2015年5月5日 上午10:36:37
 */
public class PaymentInfoVO  {

	public List<PAYMENT_INFO> PAYMENT_HEAD;//
	
	public static class PAYMENT_INFO {
		public String DEAL_PLAT_ID;//支付编号
		public String DEAL_ID;//支付平台代码
		public String PAYMENT_DATE;//支付时间
		public String PAYMENT_DESC;//支付描述
		public String PAYMENT_AMOUNT;//支付金额
		public String PAYER_ACCOUNT;//付款人账户
		public String PAYER_NAME;//付款人姓名
		public String PAYER_CERT_TYPE;//付款人证件类型 1身份证 2护照 3军官证
		public String PAYER_CERT_ID;//
		public String PAYEE_ACCOUNT;//收款人账户 
		public String PAYEE_NAME;//收款人姓名
		public String PAYEE_CERT_TYPE;//收款人证件类型
		public String PAYEE_CERT_ID;//
		public String EB_PLAT_ID;//电商平台代码
		public String ORDER_ID;//订单编号
		public String NOTE;//
	}
	    
}
