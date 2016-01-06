package services;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;

import javax.inject.Named;
import javax.inject.Singleton;

import play.Configuration;
import play.Logger;
import utils.alipay.AlipayConfig;
import utils.alipay.AlipaySubmit;
import utils.alipay.SignUtils;
import utils.alipay.UtilDate;
import utils.alipay.forextrade.AlipayFTConfig;

/**
 * 阿里支付Service
 * 
 * @author luobotao
 * 
 */
@Named
@Singleton
public class AliPayService extends Thread {
	private static final Logger.ALogger logger = Logger.of(AliPayService.class);
	// 服务器异步通知页面路径
	private static String notify_wap_url = "http://123.56.105.53:9002/api/alipayWapNotify";			//wap 异步回调
	private static String call_wap_back_url = "http://123.56.105.53:9002/api/alipayWapReturn";		//wap 同步回调
	private static String call_wap_back_url_endorse="http://h5.higegou.com/sheSaid/alipayreturn";	//代言 同步回调
	private static String notify_web_url = "http://123.56.105.53:9002/api/alipayFastNotify";
	private static String call_web_back_url = "http://123.56.105.53:9002/api/alipayFastReturn";
	private static String merchant_url = "http://123.56.105.53:9002/api/alipayWapNotify";// 用户付款中途退出返回商户的地址。需http://格式的完整路径，不允许加?id=123这类自定义参数
	private static String return_url = "http://123.56.105.53:9002/api/AliPay/authorizeReturn";
	//海外支付回调地址
	private static String notify_ft_url = "http://123.56.105.53:9002/api/alipayFTNotify";

	private static final String format = "xml";
	private static final String v = "2.0";// 请求号
	
	private static AliPayService instance = new AliPayService();
	private Executor executor = Executors.newSingleThreadExecutor();
	private LinkedBlockingQueue<Runnable> tasks = new LinkedBlockingQueue<>();

	static{
		boolean IsProduct = Configuration.root().getBoolean("production", false);
		String domain = Configuration.root().getString("alipay.url.dev","http://182.92.227.140:9004");
		if(IsProduct){
			domain = Configuration.root().getString("alipay.url.product","http://123.56.105.53:9002");
		}
		notify_wap_url = domain+"/api/alipayWapNotify";
		call_wap_back_url = domain+"/api/alipayWapReturn";
		notify_web_url = domain+"/api/alipayFastNotify";
		call_web_back_url = domain+"/api/alipayFastReturn";
		merchant_url = domain+"/api/alipayWapMerchant";
		return_url = domain+"/api/AliPay/authorizeReturn";
		notify_ft_url = domain+"/api/alipayFTNotify";
		Logger.debug("notify_wap_url="+notify_wap_url);
		Logger.debug("call_wap_back_url="+call_wap_back_url);
		Logger.debug("notify_web_url="+notify_web_url);
		Logger.debug("call_web_back_url="+call_web_back_url);
		Logger.debug("merchant_url="+merchant_url);
		Logger.debug("return_url="+return_url);
		Logger.debug("notify_ft_url="+notify_ft_url);
	}
	
	/* 私有构造方法，防止被实例化 */
	private AliPayService() {
		this.start();
	}

	public void run() {
		logger.info("start AliPayService service ");
		Runnable r;
		try {
			while ((r = tasks.take()) != null) {
				executor.execute(r);
			}
		} catch (InterruptedException e) {
			logger.error("InterruptedException in AliPayService service", e);
		}
	}

	public static AliPayService getInstance() {
		return instance;
	}

	/**
	 * 获取sign
	 * @param out_trade_no
	 * @param price
	 * @return
	 */
	public String alipay_Wap_sign(String out_trade_no, Double price) {
		String total_fee = String.valueOf(price);// 必填
		String orderInfo = getOrderInfo(out_trade_no, total_fee);
		String sign = SignUtils.sign(orderInfo, AlipayConfig.private_key);
		try {
			// 仅需对sign 做URL编码
			sign = URLEncoder.encode(sign, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		 String payInfo = orderInfo + "&sign=\"" + sign + "\"&sign_type=\"RSA\"";
		return payInfo;
	}
	
	
	
	/**
	 * create the order info. 创建订单信息
	 * 
	 */
	public String getOrderInfo(String out_trade_no,  String price) {
		// 合作者身份ID
		String orderInfo = "partner=" + "\"" + AlipayConfig.partner + "\"";

		// 卖家支付宝账号
		orderInfo += "&seller_id=" + "\"" + AlipayConfig.seller_email + "\"";

		// 商户网站唯一订单号
		orderInfo += "&out_trade_no=" + "\"" + out_trade_no + "\"";
		
		String subject = "嗨个购-订单号:"+out_trade_no;// 必填
		String body = "嗨个购";// 订单描述
		// 商品名称
		orderInfo += "&subject=" + "\"" + subject + "\"";

		// 商品详情
		orderInfo += "&body=" + "\"" + body + "\"";

		// 商品金额
		orderInfo += "&total_fee=" + "\"" + price + "\"";

		// 服务器异步通知页面路径
		orderInfo += "&notify_url=" + "\"" + notify_wap_url+ "\"";

		// 接口名称， 固定值
		orderInfo += "&service=\"mobile.securitypay.pay\"";

		// 支付类型， 固定值
		orderInfo += "&payment_type=\"1\"";

		// 参数编码， 固定值
		orderInfo += "&_input_charset=\"utf-8\"";

		// 设置未付款交易的超时时间
		// 默认30分钟，一旦超时，该笔交易就会自动被关闭。
		// 取值范围：1m～15d。
		// m-分钟，h-小时，d-天，1c-当天（无论交易何时创建，都在0点关闭）。
		// 该参数数值不接受小数点，如1.5h，可转换为90m。
		orderInfo += "&it_b_pay=\"30m\"";

		// 支付宝处理完请求后，当前页面跳转到商户指定页面的路径，可空
		orderInfo += "&return_url=\"m.alipay.com\"";

		// 调用银行卡支付，需配置此参数，参与签名， 固定值
		// orderInfo += "&paymethod=\"expressGateway\"";

		return orderInfo;
	}
	

	/**
	 * wap 生成支付的URL
	 * @param out_trade_no 订单号
	 * @param price 待付款金额
	 * @return
	 */
	public String sendWapAliPay(String out_trade_no, Double price) {
		String merchant_url_result = merchant_url+"?orderCode="+out_trade_no;
		logger.info(merchant_url+"----------------merchant_url");
		// 支付宝网关地址
		String ALIPAY_GATEWAY_NEW = "http://wappaygw.alipay.com/service/rest.htm?";
		// //////////////////////////////////调用授权接口alipay.wap.trade.create.direct获取授权码token//////////////////////////////////////
		// 返回格式
		String req_id = UtilDate.getOrderNum();// 必填，须保证每次请求都是唯一
		
		// 订单名称
		String subject = "嗨个购-"+out_trade_no;// 必填
		String body = "嗨个购";// 订单描述
		String total_fee = String.valueOf(price);// 必填
		// 请求业务参数详细
		String req_dataToken = "<direct_trade_create_req><notify_url>"
				+ notify_wap_url + "</notify_url><call_back_url>" + call_wap_back_url
				+ "</call_back_url><seller_account_name>"
				+ AlipayConfig.seller_email
				+ "</seller_account_name><body>"+body+"</body><out_trade_no>" + out_trade_no
				+ "</out_trade_no><subject>" + subject
				+ "</subject><total_fee>" + total_fee
				+ "</total_fee><merchant_url>" + merchant_url_result
				+ "</merchant_url></direct_trade_create_req>";
		logger.info(req_dataToken);
		// 把请求参数打包成数组
		Map<String, String> sParaTempToken = new HashMap<String, String>();
		sParaTempToken.put("service", "alipay.wap.trade.create.direct");
		sParaTempToken.put("partner", AlipayConfig.partner);
		sParaTempToken.put("_input_charset", AlipayConfig.input_charset);
		sParaTempToken.put("sec_id", AlipayConfig.sign_type);
		sParaTempToken.put("format", format);
		sParaTempToken.put("v", v);
		sParaTempToken.put("req_id", req_id);
		sParaTempToken.put("req_data", req_dataToken);
		// 建立请求
		String sHtmlTextToken = "";
		String request_token = "";
		try {
			sHtmlTextToken = AlipaySubmit.buildRequest(ALIPAY_GATEWAY_NEW, "","", sParaTempToken);
			// URLDECODE返回的信息
			sHtmlTextToken = URLDecoder.decode(sHtmlTextToken,AlipayConfig.input_charset);
			// 获取token
			request_token = AlipaySubmit.getRequestToken(sHtmlTextToken);
		} catch (Exception e) {
			e.printStackTrace();
		}
		// 根据授权码token调用交易接口alipay.wap.auth.authAndExecute
		String req_data = "<auth_and_execute_req><request_token>"
				+ request_token + "</request_token></auth_and_execute_req>";
		// 把请求参数打包成数组
		Map<String, String> sParaTemp = new HashMap<String, String>();
		sParaTemp.put("service", "alipay.wap.auth.authAndExecute");
		sParaTemp.put("partner", AlipayConfig.partner);
		sParaTemp.put("_input_charset", AlipayConfig.input_charset);
		sParaTemp.put("sec_id", AlipayConfig.sign_type);
		sParaTemp.put("format", format);
		sParaTemp.put("v", v);
		sParaTemp.put("req_data", req_data);
//		sParaTemp = AlipaySubmit.buildRequestPara(sParaTemp);
		
		return buildUrl(ALIPAY_GATEWAY_NEW, sParaTemp);
	}
	
	
	/**
	 * wap 生成支付的URL,支持传入返回URL
	 * @param out_trade_no 订单号
	 * @param price 待付款金额
	 * @return
	 */
	public String sendWapAliPayEndorse(String out_trade_no, Double price,String callbackurl,String notify_wap_url_ENDORSE) {
		// 支付宝网关地址
		callbackurl=call_wap_back_url_endorse;
		boolean IsProduct = Configuration.root().getBoolean("production", false);
		if(IsProduct){
			
			call_wap_back_url_endorse=Configuration.root().getString("alipay.url.productH5","http://h5.higegou.com")+"/sheSaid/alipayreturn";			
		}
		
		String ALIPAY_GATEWAY_NEW = "http://wappaygw.alipay.com/service/rest.htm?";
		// //////////////////////////////////调用授权接口alipay.wap.trade.create.direct获取授权码token//////////////////////////////////////
		// 返回格式
		String req_id = UtilDate.getOrderNum();// 必填，须保证每次请求都是唯一
		
		// 订单名称
		String subject = "嗨个购-"+out_trade_no;// 必填
		String body = "嗨个购";// 订单描述
		String total_fee = String.valueOf(price);// 必填
		// 请求业务参数详细
		String req_dataToken = "<direct_trade_create_req><notify_url>"
				+ notify_wap_url + "</notify_url><call_back_url>" + callbackurl
				+ "</call_back_url><seller_account_name>"
				+ AlipayConfig.seller_email
				+ "</seller_account_name><body>"+body+"</body><out_trade_no>" + out_trade_no
				+ "</out_trade_no><subject>" + subject
				+ "</subject><total_fee>" + total_fee
				+ "</total_fee><merchant_url>" + notify_wap_url_ENDORSE
				+ "</merchant_url></direct_trade_create_req>";
		// 把请求参数打包成数组
		Map<String, String> sParaTempToken = new HashMap<String, String>();
		sParaTempToken.put("service", "alipay.wap.trade.create.direct");
		sParaTempToken.put("partner", AlipayConfig.partner);
		sParaTempToken.put("_input_charset", AlipayConfig.input_charset);
		sParaTempToken.put("sec_id", AlipayConfig.sign_type);
		sParaTempToken.put("format", format);
		sParaTempToken.put("v", v);
		sParaTempToken.put("req_id", req_id);
		sParaTempToken.put("req_data", req_dataToken);
		// 建立请求
		String sHtmlTextToken = "";
		String request_token = "";
		try {
			sHtmlTextToken = AlipaySubmit.buildRequest(ALIPAY_GATEWAY_NEW, "","", sParaTempToken);
			// URLDECODE返回的信息
			sHtmlTextToken = URLDecoder.decode(sHtmlTextToken,AlipayConfig.input_charset);
			// 获取token
			request_token = AlipaySubmit.getRequestToken(sHtmlTextToken);
		} catch (Exception e) {
			e.printStackTrace();
		}
		// 根据授权码token调用交易接口alipay.wap.auth.authAndExecute
		String req_data = "<auth_and_execute_req><request_token>"
				+ request_token + "</request_token></auth_and_execute_req>";
		// 把请求参数打包成数组
		Map<String, String> sParaTemp = new HashMap<String, String>();
		sParaTemp.put("service", "alipay.wap.auth.authAndExecute");
		sParaTemp.put("partner", AlipayConfig.partner);
		sParaTemp.put("_input_charset", AlipayConfig.input_charset);
		sParaTemp.put("sec_id", AlipayConfig.sign_type);
		sParaTemp.put("format", format);
		sParaTemp.put("v", v);
		sParaTemp.put("req_data", req_data);
//		sParaTemp = AlipaySubmit.buildRequestPara(sParaTemp);
		
		return buildUrl(ALIPAY_GATEWAY_NEW, sParaTemp);
	}
	
	public String sendWebAliPay(String out_trade_no, Double price) {
		// 支付类型
		String payment_type = "1";// 必填，不能修改
		String subject = "myorder";// 订单名称 必填;
		// 付款金额
		String total_fee = String.valueOf(price);// 必填;
		String body = "myorder";// 订单描述
		
		String show_url = "";// 商品展示地址需以http://开头的完整路径，例如：http://www.商户网址.com/myorder.html
		
		String anti_phishing_key = "";// 防钓鱼时间戳
		try {
			anti_phishing_key = AlipaySubmit.query_timestamp();
		} catch (Exception e) {
			e.printStackTrace();
		}
		// 客户端的IP地址
		String exter_invoke_ip = "";// 非局域网的外网IP地址，如：221.0.0.1
		// 把请求参数打包成数组
		Map<String, String> sParaTemp = new HashMap<String, String>();
		sParaTemp.put("service", "create_direct_pay_by_user");
		sParaTemp.put("partner", AlipayConfig.partner);
		sParaTemp.put("seller_email", AlipayConfig.seller_email);
		sParaTemp.put("_input_charset", AlipayConfig.input_charset);
		sParaTemp.put("payment_type", payment_type);
		sParaTemp.put("notify_url", notify_web_url);
		sParaTemp.put("return_url", call_web_back_url);
		sParaTemp.put("out_trade_no", out_trade_no);
		sParaTemp.put("subject", subject);
		sParaTemp.put("total_fee", total_fee);
		sParaTemp.put("body", body);
		sParaTemp.put("show_url", show_url);
		sParaTemp.put("anti_phishing_key", anti_phishing_key);
		sParaTemp.put("exter_invoke_ip", exter_invoke_ip);
		// 建立请求
		return buildUrl("https://mapi.alipay.com/gateway.do?", sParaTemp);
	}

	private String buildUrl(String ALIPAY_GATEWAY_NEW,
			Map<String, String> sParaTemp) {
		// 待请求参数数组
		Map<String, String> sPara = AlipaySubmit.buildRequestPara(sParaTemp);
		List<String> keys = new ArrayList<String>(sPara.keySet());

		StringBuffer sbHtml = new StringBuffer();
		sbHtml.append(ALIPAY_GATEWAY_NEW + "_input_charset="+ AlipayConfig.input_charset);
		for (int i = 0; i < keys.size(); i++) {
			String name = (String) keys.get(i);
			String value = (String) sPara.get(name);
			sbHtml.append("&" + name + "=" + value);
		}
		return sbHtml.toString();
	}

	public static void main(String[] args) {
		String url = AliPayService.getInstance().sendWapAliPay("1212121", 1D);
		System.out.println(url);
	}

	public String buildAuthorizeUrl() {
		// 目标服务地址
		String target_service = "user.auth.quick.login";
		// 必填，页面跳转同步通知页面路径
//		String return_url = "http://123.56.105.53:9002/api/AliPay/authorizeReturn";
		// 防钓鱼时间戳
		String anti_phishing_key = "";
		// 若要使用请调用类文件submit中的query_timestamp函数
		// 客户端的IP地址
		String exter_invoke_ip = "";// 非局域网的外网IP地址，如：221.0.0.1
		// 把请求参数打包成数组
		Map<String, String> sParaTemp = new HashMap<String, String>();
		sParaTemp.put("service", "alipay.auth.authorize");
		sParaTemp.put("partner", AlipayConfig.partner);
		sParaTemp.put("_input_charset", AlipayConfig.input_charset);
		sParaTemp.put("target_service", target_service);
		sParaTemp.put("return_url", return_url);
		sParaTemp.put("anti_phishing_key", anti_phishing_key);
		sParaTemp.put("exter_invoke_ip", exter_invoke_ip);
		// 建立请求
		return buildUrl("https://mapi.alipay.com/gateway.do?", sParaTemp);
	}

	/**
	 * 
	 * <p>Title: alipay_FT_sign</p> 
	 * <p>Description: 阿里国际支付获取sign</p> 
	 * @param out_trade_no
	 * @param price
	 * @return
	 */
	public String alipay_FT_sign(String out_trade_no, Double price) {
		String total_fee = String.valueOf(price);// 必填
		String orderInfo = getFTOrderInfo(out_trade_no, total_fee);
		String sign = SignUtils.sign(orderInfo, AlipayFTConfig.private_key);
		try {
			// 仅需对sign 做URL编码
			sign = URLEncoder.encode(sign, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		String payInfo = orderInfo + "&sign=\"" + sign + "\"&sign_type=\"RSA\"";
		logger.info("payInfo:"+payInfo);
		return payInfo;
	}
	
	/**
	 * create the order info. 创建订单信息
	 * 用于获取阿里国际支付的sign
	 */
	public String getFTOrderInfo(String out_trade_no,  String price) {
		// 接口名称， 固定值
		String orderInfo = "service=\"mobile.securitypay.pay\"";
		// 合作者身份ID
		orderInfo += "&partner=" + "\"" + AlipayFTConfig.partner + "\"";
		// 参数编码， 固定值
		orderInfo += "&_input_charset=\"utf-8\"";
		// 服务器异步通知页面路径
		orderInfo += "&notify_url=" + "\"" + notify_ft_url+ "\"";
		// 商户网站唯一订单号
		orderInfo += "&out_trade_no=" + "\"" + out_trade_no + "\"";
		
		String subject = "嗨个购-订单号:"+out_trade_no;// 必填
		String body = "嗨个购";// 订单描述
		// 商品名称
		orderInfo += "&subject=" + "\"" + subject + "\"";
		// 支付类型， 固定值
		orderInfo += "&payment_type=\"1\"";
		// 卖家支付宝账号
		orderInfo += "&seller_id=" + "\"" + AlipayFTConfig.partner + "\"";
		// 商品金额
		orderInfo += "&rmb_fee=" + "\"" + price + "\"";
		// forex_biz,目前只能填FP 支付宝规定
		orderInfo += "&forex_biz=\"FP\"";
		// 币种 目前咱们使用的都是usd美元
		orderInfo += "&currency=" + "\"USD\"";
		// 商品详情
		orderInfo += "&body=" + "\"" + body + "\"";

		// 设置未付款交易的超时时间
		// 默认30分钟，一旦超时，该笔交易就会自动被关闭。
		// 取值范围：1m～15d。
		// m-分钟，h-小时，d-天，1c-当天（无论交易何时创建，都在0点关闭）。
		// 该参数数值不接受小数点，如1.5h，可转换为90m。
		orderInfo += "&it_b_pay=\"30m\"";
		// 支付宝处理完请求后，当前页面跳转到商户指定页面的路径，可空
		orderInfo += "&return_url=\"m.alipay.com\"";
		return orderInfo;
	}
}
