package services;

import java.util.Arrays;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;

import javax.inject.Named;
import javax.inject.Singleton;

import org.apache.commons.lang3.StringUtils;
import org.w3c.dom.Document;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import play.Configuration;
import play.Logger;
import play.libs.Json;
import play.libs.XPath;
import utils.WSUtils;
import utils.wxpay.MD5Util;
import utils.wxpay.Sha1Util;
import utils.wxpay.TenpayHttpClient;


/**
 * 微信支付Service
 * @author luobotao
 *
 */
@Named
@Singleton
public class WXPayServiceSalesMan extends Thread{
    private static final Logger.ALogger LOGGER = Logger.of(WXPayServiceSalesMan.class);
	//商户号
	private static final String mch_id = "1280503601";

	//appid
	private static final String app_id="wx64df13fdc3d36ee8";

	private static final String app_secret = "f17f13bbac8893946a267566c2ddrw59";//

	//支付完成后的回调处理页面
	private static String notify_url ="http://123.56.105.53:9002/api/wxpayWapNotify";
	static{
		boolean IsProduct = Configuration.root().getBoolean("production", false);
		String domain = Configuration.root().getString("alipay.url.dev","http://182.92.227.140:9004");
		if(IsProduct){
			domain = Configuration.root().getString("alipay.url.product","http://123.56.105.53:9002");
		}
		notify_url = domain+"/api/wxpayWapNotify";
	}
	
	private static WXPayServiceSalesMan instance = new WXPayServiceSalesMan();
    private Executor executor = Executors.newSingleThreadExecutor();
    private LinkedBlockingQueue<Runnable> tasks = new LinkedBlockingQueue<>();
	 /* 私有构造方法，防止被实例化 */
	private WXPayServiceSalesMan(){
		this.start();
	}
	public void run(){
		LOGGER.info("start WXPayServiceSalesMan service ");
		Runnable r;
		try {
			while((r = tasks.take()) != null){
				executor.execute(r);
			}
		} catch (InterruptedException e) {
			LOGGER.error("InterruptedException in WXPayService service",e);
		}
	}
	public static WXPayServiceSalesMan getInstance(){
		return instance;
	}

	/**
	 * @param out_trade_no 订单号 商户根据自己情况调整，只要保持全局唯一就行
	 * @param total_fee 提交的商品价格
	 * @param ip 客户的IP（从浏览器传入的IP）
	 * @return
	 */
	public JsonNode getSignAndPrepayID(String out_trade_no,long price,String spbill_create_ip,String trade_type){
		if ("0:0:0:0:0:0:0:1".equals(spbill_create_ip)){
			spbill_create_ip="127.0.0.1";
		}
		String total_fee = String.valueOf(price);
		
		String noncestr = Sha1Util.getNonceStr();
		String timestamp = Sha1Util.getTimeStamp();
		//设置支付参数
		SortedMap<String, String> signParams = new TreeMap<String, String>();
		signParams.put("appid", app_id);
		signParams.put("mch_id", mch_id);
		signParams.put("nonce_str", noncestr);
		signParams.put("body", "嗨个购-"+out_trade_no);
		signParams.put("out_trade_no", out_trade_no); //商家订单号  
		signParams.put("total_fee", total_fee); 
		signParams.put("spbill_create_ip", spbill_create_ip);
		signParams.put("notify_url", notify_url);
		signParams.put("trade_type", trade_type);
		//生成支付签名，要采用URLENCODER的原始值进行SHA1算法！
		String sign=makeSig(signParams);

		StringBuilder postXml = new StringBuilder("<xml>");
		postXml.append("<appid>");
		postXml.append(app_id);
		postXml.append("</appid>");
		postXml.append("<mch_id>");
		postXml.append(mch_id);
		postXml.append("</mch_id>");
		postXml.append("<nonce_str>");
		postXml.append(noncestr);
		postXml.append("</nonce_str>");
		postXml.append("<sign>");
		postXml.append(sign);
		postXml.append("</sign>");
		postXml.append("<body>");
		postXml.append("嗨个购-"+out_trade_no);
		postXml.append("</body>");
		postXml.append("<out_trade_no>");
		postXml.append(out_trade_no);
		postXml.append("</out_trade_no>");
		postXml.append("<total_fee>");
		postXml.append(total_fee);
		postXml.append("</total_fee>");
		postXml.append("<spbill_create_ip>");
		postXml.append(spbill_create_ip);
		postXml.append("</spbill_create_ip>");
		postXml.append("<notify_url>");
		postXml.append(notify_url);
		postXml.append("</notify_url>");
		postXml.append("<trade_type>");
		postXml.append(trade_type);//取值如下：JSAPI，NATIVE，APP
		postXml.append("</trade_type>");
		postXml.append("</xml>");
		LOGGER.info(postXml.toString()); 
		Document resultXml = WSUtils.postByXML("https://api.mch.weixin.qq.com/pay/unifiedorder", postXml.toString());
		Logger.info(Json.toJson(resultXml)+"");
		TreeMap<String, String> outParams = new TreeMap<String, String>();
		String prepay_id = "";
		if(resultXml!=null){
			prepay_id = XPath.selectText("//prepay_id", resultXml);
		    if(StringUtils.isBlank(prepay_id)) {
		    	outParams.put("retcode", "-2");
				outParams.put("retmsg", "错误：获取prepayId失败");
		    } else {
		    	//输出参数
				
				outParams.put("appid", app_id);
				outParams.put("partnerid", mch_id);
				outParams.put("noncestr", noncestr);
				outParams.put("package", "Sign=WXPay");
				outParams.put("prepayid", prepay_id);
				outParams.put("timestamp", timestamp);
				outParams.put("sign", makeSig(outParams));
				outParams.put("token", "");
				outParams.put("retcode", "0");
				outParams.put("retmsg", "OK");
		    }
		}
		return Json.toJson(outParams);
	}	//创建签名时的字符串
	public static String makeSig(Map<String, String> sortMap) {
		StringBuilder sb = new StringBuilder();
		Object[] keys = sortMap.keySet().toArray();
		Arrays.sort(keys);
		for (int i = 0; i < keys.length; i++) {
			String mapkey = (String) keys[i];
			if (i == keys.length - 1) {// 拼接时，不包括最后一个&字符
				sb.append(mapkey).append("=").append(sortMap.get(mapkey));// QSTRING_EQUAL为=,QSTRING_SPLIT为&
			} else {
				sb.append(mapkey).append("=").append(sortMap.get(mapkey))
						.append("&");
			}
		}
		sb.append("&key=");
		sb.append(app_secret);
		String data = sb.toString();// 参数拼好的字符串			
		
		LOGGER.info("加密参数为：" + data);
		data = MD5Util.MD5Encode(data, "utf-8");
		return data.toUpperCase();
	}
	/**
	 * 获取TOKEN，一天最多获取200次，需要所有用户共享值
	 */
	public String getToken() {
		String wxpay_access_token = ServiceFactory.getCacheService().get("wxpay_access_token_new");
		if(StringUtils.isBlank(wxpay_access_token)){
			wxpay_access_token = getTokenReal();
		}
		return wxpay_access_token;

	}

	/**
	 * 实时获取token，并更新到cache中
	 */
	public String getTokenReal() {
		String token = "";
		String url = "https://api.weixin.qq.com/cgi-bin/token?grant_type=client_credential&appid="+app_id+"&secret="+app_secret;
		JsonNode result = WSUtils.getResponseAsJson(url);
		if(result!=null){
			token = result.get("access_token")==null?"":result.get("access_token").asText();
			ServiceFactory.getCacheService().set("wxpay_access_token_new",token);
		}
		return token;
	}
}
