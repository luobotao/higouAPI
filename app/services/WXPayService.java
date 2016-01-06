package services;

import java.io.UnsupportedEncodingException;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;

import javax.inject.Named;
import javax.inject.Singleton;

import org.joda.time.DateTime;

import play.Configuration;
import play.Logger;
import play.libs.Json;
import utils.wxpay.RequestHandler;
import utils.wxpay.Sha1Util;
import utils.wxpay.TenpayHttpClient;

import com.fasterxml.jackson.databind.JsonNode;


/**
 * 微信支付Service
 * @author luobotao
 *
 */
@Named
@Singleton
public class WXPayService extends Thread{
    private static final Logger.ALogger LOGGER = Logger.of(WXPayService.class);
	
	//收款方
	private static final String spname = "微信支付";                                           

	//商户号
	private static final String partner = "1234290501";

	//密钥
	private static final String partner_key = "97d12b655c585625710e31309d2ee3c4";

	//appid
	private static final String app_id="wx73bdf02facab9ca2";

	private static final String app_secret = "f17f13bbac8893946a267566c24628fb";

	//appkey
	private static final String app_key="CBTFJjMoeCoIweHgZfmuPg4nCwVrYy5QjqEWGvgghVvj44iDy9eToInNIi4DizrUYvMYSUkQnGvqqck9xKqAd1BrRPEKZeFo6fWDkQbJ93xniBbrGbkH2EC1VpGRVNec";
//	private static final String spname = "微信支付";                                           
//	
//	//商户号
//	private static final String partner = "1225432101";
//	
//	//密钥
//	private static final String partner_key = "4959df8260f776b0d427bd88b5806863";
//	
//	//appid
//	private static final String app_id="wx82db1655019828db";
//	
//	private static final String app_secret = "672d7ce5183deb6cea63c7cb512bc13d";
//	
//	//appkey
//	private static final String app_key="Nbm5sk2ago8peudS19tgXc7O9tQ9dgxhCBLv1iznEIIk1ObBIPTTcvm3ilLvZgAacwfZ3x7j2D85kxsAsSRsWLuT5iFCBNCJIHSm0uWPabUyk9wjJlaVlEuVrz1Bngle";

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
	
	private static WXPayService instance = new WXPayService();
    private Executor executor = Executors.newSingleThreadExecutor();
    private LinkedBlockingQueue<Runnable> tasks = new LinkedBlockingQueue<>();
	 /* 私有构造方法，防止被实例化 */
	private WXPayService(){
		this.start();
	}
	public void run(){
		LOGGER.info("start WXPayService service ");
		Runnable r;
		try {
			while((r = tasks.take()) != null){
				executor.execute(r);
			}
		} catch (InterruptedException e) {
			LOGGER.error("InterruptedException in WXPayService service",e);
		}
	}
	public static WXPayService getInstance(){
		return instance;
	}

	/**
	 * @param out_trade_no 订单号 商户根据自己情况调整，只要保持全局唯一就行
	 * @param total_fee 提交的商品价格
	 * @param ip 客户的IP（从浏览器传入的IP）
	 * @return
	 */
	public JsonNode getSignAndPrepayID(String out_trade_no,long price,String ip){
		String total_fee = String.valueOf(price);
		TreeMap<String, String> outParams = new TreeMap<String, String>();
		try
		{
			String rand=String.valueOf(System.currentTimeMillis());
			
			rand= rand.substring(rand.length()-3,rand.length());
			out_trade_no=out_trade_no+rand;
		}catch(Exception ex){
			
		}
		RequestHandler reqHandler = new RequestHandler();
	    //初始化 
		reqHandler.init();
		reqHandler.init(app_id, app_secret, app_key, partner, partner_key);

		//获取token值 
		String token = reqHandler.GetToken();
		if (!"".equals(token)) {
			SortedMap<String, String> packageParams = new TreeMap<String, String>();
			packageParams.put("bank_type", "WX"); //商品描述   
			packageParams.put("body", "嗨个购-"+out_trade_no); //商品描述   
			packageParams.put("notify_url", notify_url); //接收财付通通知的URL  
			packageParams.put("partner", partner); //商户号    
			packageParams.put("out_trade_no", out_trade_no); //商家订单号  
			packageParams.put("total_fee", total_fee); //商品金额,以分为单位  
			packageParams.put("spbill_create_ip", ip); //订单生成的机器IP，指用户浏览器端IP  
			packageParams.put("fee_type", "1"); //币种，1人民币   66
			packageParams.put("input_charset", "GBK"); //字符编码

			//获取package包
			String packageValue="";
			try {
				packageValue = reqHandler.genPackage(packageParams);
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}

			String noncestr = Sha1Util.getNonceStr();
			String timestamp = Sha1Util.getTimeStamp();
			String traceid = app_id;//商家对用户的唯一标识,如果用微信SSO，此处建议填写授权用户的openid

			//设置支付参数
			SortedMap<String, String> signParams = new TreeMap<String, String>();
			signParams.put("appid", app_id);
			signParams.put("appkey", app_key);
			signParams.put("noncestr", noncestr);
			signParams.put("package", packageValue);
			signParams.put("timestamp", timestamp);
			signParams.put("traceid", traceid);

			//生成支付签名，要采用URLENCODER的原始值进行SHA1算法！
			String sign="";
			try {
				sign = Sha1Util.createSHA1Sign(signParams);
			} catch (Exception e) {
				e.printStackTrace();
			}
			//增加非参与签名的额外参数
			signParams.put("app_signature", sign);
			signParams.put("sign_method", "sha1");

			//获取prepayId
			String prepayid = reqHandler.sendPrepay(signParams);

			if (null != prepayid && !"".equals(prepayid)) {
				//签名参数列表
				SortedMap<String, String> prePayParams = new TreeMap<String, String>();
				prePayParams.put("appid", app_id);
				prePayParams.put("appkey", app_key);
				prePayParams.put("noncestr", noncestr);
				prePayParams.put("package", "Sign=WXPay");
				prePayParams.put("partnerid", partner);
				prePayParams.put("prepayid", prepayid);
				prePayParams.put("timestamp", timestamp);
				//生成签名
				try {
					sign = Sha1Util.createSHA1Sign(prePayParams);
				} catch (Exception e) {
					e.printStackTrace();
				}

				//输出参数
				outParams.put("retcode", "0");
				outParams.put("retmsg", "OK");
				outParams.put("appid", app_id);
				outParams.put("partnerid", partner);
				outParams.put("noncestr", noncestr);
				outParams.put("package", "Sign=WXPay");
				outParams.put("prepayid", prepayid);
				outParams.put("timestamp", timestamp);
				outParams.put("sign", sign);
				//测试帐号多个app测试，需要判断Token是否失效，否则重新获取一次 
				if(reqHandler.getLasterrCode()=="40001"){
		         	token = reqHandler.getTokenReal();
				}
				outParams.put("token", token);
			} else {
				outParams.put("retcode", "-2");
				outParams.put("retmsg", "错误：获取prepayId失败");
			}
		} else {
			outParams.put("retcode", "-1");
			outParams.put("retmsg", "错误：获取不到Token");
		}
		return Json.toJson(outParams);
	}

}
