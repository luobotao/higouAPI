package controllers.api;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import models.ShoppingOrder;
import net.sf.json.JSONObject;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.StringUtils;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;

import play.Configuration;
import play.Logger;
import play.libs.Json;
import play.mvc.Result;
import services.AliPayService;
import services.api.ProductService;
import services.api.ShoppingOrderService;
import services.api.UserService;
import utils.AjaxHellper;
import utils.StringUtil;
import utils.Constants.OrderStatus;
import utils.Numbers;
import utils.alipay.AlipayConfig;
import utils.alipay.AlipayNotify;
import utils.alipay.MD5;
import utils.alipay.forextrade.AlipayFTConfig;
import utils.alipay.forextrade.AlipayFTNotify;
import vo.product.ProductNewVO;
import vo.user.AlipayLoginUserVO;

import com.fasterxml.jackson.databind.node.ObjectNode;


/**
 * 阿里支付Controller
 * 
 * @author luobotao
 * 
 */
@Named
@Singleton
public class AliPayAPIController extends BaseApiController {
	private static final Logger.ALogger logger = Logger.of(AliPayAPIController.class);
	private final ProductService productService;
	private final ShoppingOrderService shoppingOrderService;
	private final UserService userService;
    @Inject
    public AliPayAPIController(final ProductService productService,final UserService userService,final ShoppingOrderService ShoppingOrderService) {
        this.productService = productService;
        this.userService = userService;
        this.shoppingOrderService = ShoppingOrderService;
    }
	//获取阿里支付的必填参数alipay_sign.php POST  快捷支付获取sign
	public static Result alipay_sign() {
		response().setContentType("application/json;charset=utf-8");
		
		// appversion=2.3.0&deviceType=0&devid=5a6e5839765241df964e6d40465b1693
		// &idfa=5618B6D3-FE7E-4A93-A087-28AE061E40A0&marketCode=1&model=iPhone 4S
		// &osversion=7.1.2&out_trade_no=1483923802&total_fee=0.01&uid=116297&wdhjy=fe858c0d2fe4e63616d04d7b5ddbfbafHG2015
        // lwdjl:6a4815a632b001ea789dde2886a220cc
		Map<String, String> pramt = new HashMap<String, String>();
		String appversion=AjaxHellper.getHttpParamOfFormUrlEncoded(request(), "appversion");
		if(appversion!=null){
			pramt.put("appversion",appversion);
		}
		String deviceType = AjaxHellper.getHttpParamOfFormUrlEncoded(request(), "deviceType");
		if(deviceType!=null){
			pramt.put("deviceType",deviceType);
		}
		String devid = AjaxHellper.getHttpParamOfFormUrlEncoded(request(),"devid");
		if(devid!=null){
			pramt.put("devid",devid);
		}
		String idfa = AjaxHellper.getHttpParamOfFormUrlEncoded(request(),"idfa");
		if(idfa!=null){
			pramt.put("idfa",idfa);
		}
		String marketCode = AjaxHellper.getHttpParamOfFormUrlEncoded(request(),"marketCode");
		if(marketCode!=null){
			pramt.put("marketCode",marketCode);
		}
		String model = AjaxHellper.getHttpParamOfFormUrlEncoded(request(),"model");
		if(model!=null){
			pramt.put("model",model);
		}
		String osversion = AjaxHellper.getHttpParamOfFormUrlEncoded(request(),"osversion");
		if(osversion!=null){
			pramt.put("osversion",osversion);
		}
		String out_trade_no = AjaxHellper.getHttpParamOfFormUrlEncoded(request(), "out_trade_no");
		if(out_trade_no!=null){
			pramt.put("out_trade_no",out_trade_no);
		}
		String total_fee = AjaxHellper.getHttpParamOfFormUrlEncoded(request(), "total_fee");
		if(total_fee!=null){
			pramt.put("total_fee",total_fee);
		}
		String uid = AjaxHellper.getHttpParamOfFormUrlEncoded(request(), "uid");
		if(uid!=null){
			pramt.put("uid",uid);
		}
		String wdhjy = AjaxHellper.getHttpParamOfFormUrlEncoded(request(),"wdhjy");
		if(wdhjy!=null){
			pramt.put("wdhjy",wdhjy);
		}
		
	
		String lwdjl=AjaxHellper.getHttpParamOfFormUrlEncoded(request(),"lwdjl")==null?"":AjaxHellper.getHttpParamOfFormUrlEncoded(request(),"lwdjl").trim();
		String mysign=StringUtil.makeSig(pramt)+"HG2015";
		
		logger.info(mysign);
		
		String md5sign="";
		try
		{
			String base64str=Base64.encodeBase64String(mysign.getBytes("UTF-8"));
			md5sign=StringUtil.getMD5(base64str);
		}catch(Exception ex){
			md5sign="";
		}	
		ObjectNode result = Json.newObject();
		if (!StringUtil.checkMd5(devid, wdhjy,appversion))
		{
			result.put("status", 0);
			result.put("msg","校验失败");
			return ok(Json.toJson(result));
		}
		
		if (!StringUtil.checksign(lwdjl,md5sign,appversion)){
			result.put("status", 0);
			result.put("msg","校验失败");
			return ok(Json.toJson(result));
		}
		if(StringUtils.isBlank(out_trade_no)||StringUtils.isBlank(total_fee)){
			return ok(Json.toJson(""));
		}
		Double price = Numbers.parseDouble(total_fee, 0D);
		String sign = AliPayService.getInstance().alipay_Wap_sign(out_trade_no,price);
		
		if(StringUtils.isBlank(sign)){
			result.put("status", 0);
			result.put("msg", "参数不全");
			return ok(Json.toJson(result));
		}else{
			result.put("status", 1);
			result.put("sign", sign);
			return ok(Json.toJson(result));
		}
	}
	
	//wap端支付 alipaywap_new.php GET
	public static Result alipaywap_new() {
		response().setContentType("application/json;charset=utf-8");
		String out_trade_no = AjaxHellper.getHttpParam(request(), "orderCode");
		String total_fee = AjaxHellper.getHttpParam(request(), "amount");
		if(StringUtils.isBlank(out_trade_no)||StringUtils.isBlank(total_fee)){
			return ok(Json.toJson(""));
		}
		Double price = Numbers.parseDouble(total_fee, 0D);
		String result = AliPayService.getInstance().sendWapAliPay(out_trade_no,price);
		return redirect(result);
//		return ok(result);
	}
	
	//wap端支付 alipaywap_new.php GET
	public Result alipaywap_new_endorse() {
		//response().setContentType("application/json;charset=utf-8");
		String out_trade_no = AjaxHellper.getHttpParam(request(), "ordercode");
		String total_fee = AjaxHellper.getHttpParam(request(), "amount");
		String callbackurl=AjaxHellper.getHttpParam(request(),"backurl");
		if(StringUtils.isBlank(out_trade_no)||StringUtils.isBlank(total_fee)){
			return ok(views.html.sheSaid.pageError.render());
		}
		ShoppingOrder order=shoppingOrderService.getShoppingOrderByOrderCode(out_trade_no);
		if(order==null){
			return ok(views.html.sheSaid.pageError.render());
		}
		String notify_wap_url_ENDORSE="http://ht2.neolix.cn:9004/sheSaid/endorsement?daiyanid=";
		boolean IsProduct = Configuration.root().getBoolean("production", false);
		if(IsProduct){
			
			notify_wap_url_ENDORSE=Configuration.root().getString("alipay.url.productH5","http://h5.higegou.com")+"/sheSaid/endorsement?daiyanid=";			
		}
		
		Double price = Numbers.parseDouble(total_fee, 0D);
		String result = AliPayService.getInstance().sendWapAliPayEndorse(out_trade_no, price, callbackurl,notify_wap_url_ENDORSE+order.getEndorsementid());
		return redirect(result);
//			return ok(result);
	}
	
	//wap端支付 alipaywap_new.php GET
		public Result alipaywap_new_H5() {
			//response().setContentType("application/json;charset=utf-8");
			String out_trade_no = AjaxHellper.getHttpParam(request(), "ordercode");
			String total_fee = AjaxHellper.getHttpParam(request(), "amount");
			String callbackurl=AjaxHellper.getHttpParam(request(),"backurl");
			String pid=AjaxHellper.getHttpParam(request(), "pid");
			String phone=AjaxHellper.getHttpParam(request(), "phone");
			
			if(StringUtils.isBlank(out_trade_no)||StringUtils.isBlank(total_fee)){
				return ok(views.html.sheSaid.pageError.render());
			}
			ShoppingOrder order=shoppingOrderService.getShoppingOrderByOrderCode(out_trade_no);
			if(order==null){
				return ok(views.html.sheSaid.pageError.render());
			}
			String notify_wap_url_ENDORSE="http://ht2.neolix.cn:9004/H5/pro?pid=";
			boolean IsProduct = Configuration.root().getBoolean("production", false);
			if(IsProduct){
				
				notify_wap_url_ENDORSE=Configuration.root().getString("alipay.url.productH5","http://h5.higegou.com")+"/H5/pro?pid=";			
			}
			
			Double price = Numbers.parseDouble(total_fee, 0D);
			String result = AliPayService.getInstance().sendWapAliPayEndorse(out_trade_no, price, callbackurl,notify_wap_url_ENDORSE+pid+"&amp;phone="+phone);
			return redirect(result);
//				return ok(result);
		}
	/**
	 * 阿里支付的回调(异步) POST 快捷支付的回调
	 * @param id
	 * @return
	 */
	public Result alipayWapNotify() {
		response().setContentType("application/json;charset=utf-8");
		String method = "21";
		String state = "20";
		ObjectNode result = Json.newObject();
		Map<String,String> params = new HashMap<String,String>();
		Map<String,String[]> all =request().body().asFormUrlEncoded();
		if(all!=null && all.keySet()!=null){
			Iterator<String> keyIt = all.keySet().iterator();
			while(keyIt.hasNext()){
				String key = keyIt.next();
				String[] values = all.get(key);
				String valueStr = "";
				for (int i = 0; i < values.length; i++) {
					valueStr = (i == values.length - 1) ? valueStr + values[i]: valueStr + values[i] + ",";
				}
				//乱码解决，这段代码在出现乱码时使用。如果mysign和sign不相等也可以使用这段代码转化
				try {
					valueStr = new String(valueStr.getBytes("ISO-8859-1"), "utf-8");
				} catch (UnsupportedEncodingException e) {
					e.printStackTrace();
				}
				params.put(key, valueStr);
			}
			logger.info(Json.toJson(params)+"===============");
			String notify_data = params.get("notify_data");
			
			String out_trade_no="";//商户订单号
			String trade_no="";//支付宝交易号
			String trade_status="";//交易状态
			try {
				Document document = DocumentHelper.parseText(notify_data);
		        Element root = document.getRootElement();  
		        out_trade_no = root.element("out_trade_no").getText();
		        trade_no = root.element("trade_no").getText();
		        trade_status = root.element("trade_status").getText();
			} catch (Exception e1) {
				try {
					out_trade_no = new String(params.get("out_trade_no")==null?"".getBytes():params.get("out_trade_no").getBytes("ISO-8859-1"),"UTF-8");
					trade_no = new String(params.get("trade_no")==null?"".getBytes():params.get("trade_no").getBytes("ISO-8859-1"),"UTF-8");
					trade_status =params.get("trade_status")==null?"": new String(params.get("trade_status").getBytes("ISO-8859-1"),"UTF-8");
				} catch (UnsupportedEncodingException e) {
					e.printStackTrace();
				}
			} 
			logger.info(out_trade_no+"==============="+trade_no+"trade_status:"+trade_status);

			boolean verify_result =true;// AlipayNotify.verifyReturn(params);
			
			if(verify_result){//验证成功
				if(trade_status.equals("TRADE_FINISHED") || trade_status.equals("TRADE_SUCCESS")){
					ShoppingOrder shoppingOrder = productService.getShoppingOrderByOrderCode(out_trade_no);
					if(shoppingOrder!=null ){
						int status = productService.checkOrderPayStat(out_trade_no,shoppingOrder.getTotalFee());
						if(status==1){
							productService.setPayStatusFast(out_trade_no,method,state,shoppingOrder.getTotalFee(),trade_no);
							result.put("status",1);
							result.put("msg","支付成功");
							return ok(Json.toJson(result));
						}else{
							result.put("status",3);
							result.put("msg","订单有误");
							return ok(Json.toJson(result));
						}
					}else{
						result.put("status",3);
						result.put("msg","订单有误");
						return ok(Json.toJson(result));
					}
				}
			}else{
				result.put("status",2);
				result.put("msg","加密串不匹配");
				return ok(Json.toJson(result));
			}
		}
		String retstr = "errorshow://orderStatus=0";
		return redirect(retstr);
	}
	/**
	 * 阿里支付的回调(异步) POST 快捷支付 点击完成后由客户端调用
	 * @param id
	 * @return
	 */
	public Result alipay_callback() {
		response().setContentType("application/json;charset=utf-8");
		String method = "20";
		String state = "10";
		/*
		   amount=0.01&appversion=2.3.0&deviceType=0&devid=1bc4d023b16441779525d92d6b8f03cf&errcode=9000
		   &idfa=A4F4EAA1-DF7C-4CCC-9421-6B9DA9B81184&marketCode=1&model=iPhone 6 Plus
		   &orderCode=1483923803&osversion=8.1&uid=116297&usewallet=0&vstr=36fa798d17a06f1da2aa4133f9e8b9db
		   &wdhjy=61150db30237c7a8f383b603ece45293HG2015
           lwdjl：a043c75201568b6efe42a828ac81d3e7
		 */
		Map<String, String> pramt = new HashMap<String, String>();
		String amount = AjaxHellper.getHttpParamOfFormUrlEncoded(request(), "amount");
		if(amount!=null){
			pramt.put("amount",amount);
		}
		String appversion=AjaxHellper.getHttpParamOfFormUrlEncoded(request(), "appversion");
		if(appversion!=null){
			pramt.put("appversion",appversion);
		}
		String deviceType = AjaxHellper.getHttpParamOfFormUrlEncoded(request(), "deviceType");
		if(deviceType!=null){
			pramt.put("deviceType",deviceType);
		}
		String devid = AjaxHellper.getHttpParamOfFormUrlEncoded(request(),"devid");
		if(devid!=null){
			pramt.put("devid",devid);
		}
		String idfa = AjaxHellper.getHttpParamOfFormUrlEncoded(request(),"idfa");
		if(idfa!=null){
			pramt.put("idfa",idfa);
		}
		String marketCode = AjaxHellper.getHttpParamOfFormUrlEncoded(request(),"marketCode");
		if(marketCode!=null){
			pramt.put("marketCode",marketCode);
		}
		String model = AjaxHellper.getHttpParamOfFormUrlEncoded(request(),"model");
		if(model!=null){
			pramt.put("model",model);
		}
		String osversion = AjaxHellper.getHttpParamOfFormUrlEncoded(request(),"osversion");
		if(osversion!=null){
			pramt.put("osversion",osversion);
		}
		String uid = AjaxHellper.getHttpParamOfFormUrlEncoded(request(), "uid");
		if(uid!=null){
			pramt.put("uid",uid);
		}
		String usewallet=AjaxHellper.getHttpParamOfFormUrlEncoded(request(), "usewallet");
		if(usewallet!=null){
			pramt.put("usewallet",usewallet);
		}
		String wdhjy = AjaxHellper.getHttpParamOfFormUrlEncoded(request(),"wdhjy");
		if(wdhjy!=null){
			pramt.put("wdhjy",wdhjy);
		}
		String orderCode = AjaxHellper.getHttpParamOfFormUrlEncoded(request(), "orderCode");
		if(orderCode!=null){
			pramt.put("orderCode",orderCode);
		}
		String vstr = AjaxHellper.getHttpParamOfFormUrlEncoded(request(), "vstr");
		if(vstr!=null){
			pramt.put("vstr",vstr);
		}
		String errcode = AjaxHellper.getHttpParamOfFormUrlEncoded(request(), "errcode");
		if(errcode!=null){
			pramt.put("errcode",errcode);
		}
		
		ObjectNode result = Json.newObject();
		
		String lwdjl=AjaxHellper.getHttpParamOfFormUrlEncoded(request(),"lwdjl")==null?"":AjaxHellper.getHttpParamOfFormUrlEncoded(request(),"lwdjl").trim();
		String sign=StringUtil.makeSig(pramt)+"HG2015";
		
		logger.info(sign);
		
		String md5sign="";
		try
		{
			String base64str=Base64.encodeBase64String(sign.getBytes("UTF-8"));
			md5sign=StringUtil.getMD5(base64str);
		}catch(Exception ex){
			md5sign="";
		}	
		
		if (!StringUtil.checkMd5(devid, wdhjy,appversion))
		{
			result.put("status", 0);
			result.put("msg", "验证失败");
			return ok(Json.toJson(result));
		}
		
		if (!StringUtil.checksign(lwdjl,md5sign,appversion)){
			result.put("status", 0);
			result.put("msg", "验证失败");
			return ok(Json.toJson(result));
		}

		if(orderCode.length()>10){
    		orderCode = orderCode.substring(0, 10);
    	}
		
		Logger.info("uid:"+uid+"orderCode:"+orderCode+"vstr:"+vstr+"amount:"+amount+"errcode:"+errcode);
		if(StringUtils.isBlank(uid)||StringUtils.isBlank(orderCode)||StringUtils.isBlank(vstr)||StringUtils.isBlank(amount)){
			result.put("status", 4);
			result.put("msg", "调用参数错误");
			return ok(Json.toJson(result));
		}
		if (!"9000".equals(errcode)){
			shoppingOrderService.order_back(orderCode);
			result.put("status", 4);
			result.put("msg", "支付错误码"+errcode);
			return ok(Json.toJson(result));
		}
		String secret_str = "DX2014";
		String prestr=orderCode+amount;
		String checkStr = MD5.sign(secret_str, prestr, AlipayConfig.input_charset);
		if(!vstr.equals(checkStr)){
			result.put("status", 2);
			result.put("msg", "加密串不匹配");
			return ok(Json.toJson(result));
		}
		/*ShoppingOrder shoppingOrder = shoppingOrderService.checkOrderAmountStat(orderCode,Numbers.parseDouble(amount, 0D));
		if(shoppingOrder==null){
			result.put("status", 3);
			result.put("msg", "订单有误");
			return ok(Json.toJson(result));
		}else{
			shoppingOrder.setPaymethod(Numbers.parseInt(method, 0));
			shoppingOrder.setPaystat(Numbers.parseInt(state, 0));
			shoppingOrderService.saveShoppingOrder(shoppingOrder);
			result.put("status", 1);
			result.put("msg", "支付成功");
			return ok(Json.toJson(result));
		}*/
//		ShoppingOrder shoppingOrder = shoppingOrderService.getShoppingOrderByOrderCode(orderCode);
		productService.setPayStatus(orderCode,method,state,Numbers.parseDouble(amount, 0D),"");
//		shoppingOrder.setPaymethod(Numbers.parseInt(method, 0));
//		shoppingOrder.setPaystat(Numbers.parseInt(state, 0));
//		shoppingOrderService.saveShoppingOrder(shoppingOrder);
		result.put("status", 1);
		result.put("msg", "支付成功");
		return ok(Json.toJson(result));
	}
	/**
	 * 阿里支付失败（用户中断操作）
	 * @return
	 */
	public Result alipayWapMerchant() {
		response().setContentType("application/json;charset=utf-8");
		
		String orderCode = AjaxHellper.getHttpParamOfFormUrlEncoded(request(), "orderCode")==null?"":AjaxHellper.getHttpParamOfFormUrlEncoded(request(), "orderCode");
		if(StringUtils.isBlank(orderCode)){
			orderCode = AjaxHellper.getHttpParam(request(), "orderCode")==null?"":AjaxHellper.getHttpParam(request(), "orderCode");
		}
		logger.info("alipayWapMerchant====="+orderCode);
		shoppingOrderService.order_back(orderCode);
		String retstr = "oshow://orderCode="+orderCode+"&orderStatus=0";
		return redirect(retstr);
	}
	/**
	 * 阿里支付的回调(同步) GET Wap
	 * @param id
	 * @return
	 */
	public Result alipayWapReturn() {
		response().setContentType("application/json;charset=utf-8");
		String method = "21";
		String state = "20";
		ObjectNode result = Json.newObject();
		Map<String,String> params = new HashMap<String,String>();
		Map<String,String[]> allQue = request().queryString();
		if(allQue!=null && allQue.keySet()!=null){
			Iterator<String> keyIt = allQue.keySet().iterator();
			while(keyIt.hasNext()){
				String key = keyIt.next();
				String[] values = allQue.get(key);
				String valueStr = "";
				for (int i = 0; i < values.length; i++) {
					valueStr = (i == values.length - 1) ? valueStr + values[i]: valueStr + values[i] + ",";
				}
				//乱码解决，这段代码在出现乱码时使用。如果mysign和sign不相等也可以使用这段代码转化
				try {
					valueStr = new String(valueStr.getBytes("ISO-8859-1"), "utf-8");
				} catch (UnsupportedEncodingException e) {
					e.printStackTrace();
				}
				params.put(key, valueStr);
			}
			String out_trade_no="";//商户订单号
			String trade_no="";//支付宝交易号
			String trade_status="";//交易状态
			try {
				out_trade_no = new String(params.get("out_trade_no").getBytes("ISO-8859-1"),"UTF-8");
				trade_no = new String(params.get("trade_no").getBytes("ISO-8859-1"),"UTF-8");
				trade_status =params.get("result")==null?"": new String(params.get("result").getBytes("ISO-8859-1"),"UTF-8");
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}

			boolean verify_result = true;//AlipayNotify.verifyReturn(params);
			
			if(verify_result){//验证成功
				
				if(out_trade_no.length()>10){
					out_trade_no = out_trade_no.substring(0, 10);
		    	}
				ShoppingOrder shoppingOrder = productService.getShoppingOrderByOrderCode(out_trade_no);
				if(shoppingOrder!=null ){
					int status = productService.checkOrderPayStat(out_trade_no,shoppingOrder.getTotalFee());
					if(status==1){
						productService.setPayStatusFast(out_trade_no,method,state,shoppingOrder.getTotalFee(),trade_no);
						String retstr = "oshow://orderCode="+out_trade_no+"&orderStatus=1";
						return redirect(retstr);
					}else{
						String retstr = "oshow://orderCode="+out_trade_no+"&orderStatus=1";
						return redirect(retstr);
					}
				}else{
					String retstr = "oshow://orderCode="+out_trade_no+"&orderStatus=0";
					return redirect(retstr);
				}
			}else{
				String retstr = "oshow://orderCode="+out_trade_no+"&orderStatus=0";
				return redirect(retstr);
			}
		}
		result.put("status",2);
		result.put("msg","加密串不匹配");
		return ok(Json.toJson(result));
	}
	
	/**
	 * 阿里支付的回调(异步) POST 快捷
	 * @param id
	 * @return
	 */
	public Result alipayFastNotify() {
		response().setContentType("application/json;charset=utf-8");
		String method = "20";
		String state = "20";
		ObjectNode result = Json.newObject();
		Map<String,String> params = new HashMap<String,String>();
		Map<String,String[]> all =request().body().asFormUrlEncoded();
		if(all!=null && all.keySet()!=null){
			Iterator<String> keyIt = all.keySet().iterator();
			while(keyIt.hasNext()){
				String key = keyIt.next();
				String[] values = all.get(key);
				String valueStr = "";
				for (int i = 0; i < values.length; i++) {
					valueStr = (i == values.length - 1) ? valueStr + values[i]: valueStr + values[i] + ",";
				}
				//乱码解决，这段代码在出现乱码时使用。如果mysign和sign不相等也可以使用这段代码转化
				try {
					valueStr = new String(valueStr.getBytes("ISO-8859-1"), "utf-8");
				} catch (UnsupportedEncodingException e) {
					e.printStackTrace();
				}
				params.put(key, valueStr);
			}
			String out_trade_no="";//商户订单号
			String trade_no="";//支付宝交易号
			String trade_status="";//交易状态
			try {
				out_trade_no = new String(params.get("out_trade_no").getBytes("ISO-8859-1"),"UTF-8");
				trade_no = new String(params.get("trade_no").getBytes("ISO-8859-1"),"UTF-8");
				trade_status = new String(params.get("trade_status").getBytes("ISO-8859-1"),"UTF-8");
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}

			boolean verify_result = AlipayNotify.verifyReturn(params);
			
			if(verify_result){//验证成功
				if(trade_status.equals("TRADE_FINISHED") || trade_status.equals("TRADE_SUCCESS")){
					ShoppingOrder shoppingOrder = productService.getShoppingOrderByOrderCode(out_trade_no);
					if(shoppingOrder!=null ){
						int status = productService.checkOrderPayStat(out_trade_no,shoppingOrder.getTotalFee());
						if(status==1){
							productService.setPayStatusFast(out_trade_no,method,state,shoppingOrder.getTotalFee(),trade_no);
							result.put("status",1);
							result.put("msg","支付成功");
							return ok(Json.toJson(result));
						}else{
							result.put("status",3);
							result.put("msg","订单有误");
							return ok(Json.toJson(result));
						}
					}else{
						result.put("status",3);
						result.put("msg","订单有误");
						return ok(Json.toJson(result));
					}
				}
			}else{
				result.put("status",2);
				result.put("msg","加密串不匹配");
				return ok(Json.toJson(result));
			}
		}
		result.put("status",2);
		result.put("msg","加密串不匹配");
		return ok(Json.toJson(result));
	}
	/**
	 * 阿里支付的回调(同步) GET Fast
	 * @param id
	 * @return
	 */
	public Result alipayFastReturn() {
		response().setContentType("application/json;charset=utf-8");
		String method = "20";
		String state = "20";
		ObjectNode result = Json.newObject();
		Map<String,String> params = new HashMap<String,String>();
		Map<String,String[]> allQue = request().queryString();
		if(allQue!=null && allQue.keySet()!=null){
			Iterator<String> keyIt = allQue.keySet().iterator();
			while(keyIt.hasNext()){
				String key = keyIt.next();
				String[] values = allQue.get(key);
				String valueStr = "";
				for (int i = 0; i < values.length; i++) {
					valueStr = (i == values.length - 1) ? valueStr + values[i]: valueStr + values[i] + ",";
				}
				//乱码解决，这段代码在出现乱码时使用。如果mysign和sign不相等也可以使用这段代码转化
				try {
					valueStr = new String(valueStr.getBytes("ISO-8859-1"), "utf-8");
				} catch (UnsupportedEncodingException e) {
					e.printStackTrace();
				}
				params.put(key, valueStr);
			}
			String out_trade_no="";//商户订单号
			String trade_no="";//支付宝交易号
			String trade_status="";//交易状态
			try {
				out_trade_no = new String(params.get("out_trade_no").getBytes("ISO-8859-1"),"UTF-8");
				trade_no = new String(params.get("trade_no").getBytes("ISO-8859-1"),"UTF-8");
				trade_status = new String(params.get("trade_status").getBytes("ISO-8859-1"),"UTF-8");
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}

			boolean verify_result = AlipayNotify.verifyReturn(params);
			
			if(verify_result){//验证成功
				if(trade_status.equals("TRADE_FINISHED") || trade_status.equals("TRADE_SUCCESS")){
					ShoppingOrder shoppingOrder = productService.getShoppingOrderByOrderCode(out_trade_no);
					if(shoppingOrder!=null && shoppingOrder.getStatus()==OrderStatus.NOPAY.getStatus()){
						int status = productService.checkOrderPayStat(out_trade_no,shoppingOrder.getTotalFee());
						if(status==1){
							productService.setPayStatusFast(out_trade_no,method,state,shoppingOrder.getTotalFee(),trade_no);
							result.put("status",1);
							result.put("msg","支付成功");
							return ok(Json.toJson(result));
						}else{
							result.put("status",3);
							result.put("msg","订单有误");
							return ok(Json.toJson(result));
						}
					}else{
						result.put("status",3);
						result.put("msg","订单有误");
						return ok(Json.toJson(result));
					}
				}
			}else{
				result.put("status",2);
				result.put("msg","加密串不匹配");
				return ok(Json.toJson(result));
			}
		}
		result.put("status",2);
		result.put("msg","加密串不匹配");
		return ok(Json.toJson(result));
	}
	/**
	 * 支付宝快捷登录
	 * @return
	 */
	public Result authorize() {
		String url = AliPayService.getInstance().buildAuthorizeUrl();
		return redirect(url);
	}
	/**
	 * 支付宝快捷登录回调
	 * @return
	 */
	public Result authorizeReturn() {
		ObjectNode result = Json.newObject();
		String is_success = AjaxHellper.getHttpParam(request(), "is_success");
		String user_id = AjaxHellper.getHttpParam(request(), "user_id");
		String real_name = AjaxHellper.getHttpParam(request(), "real_name");
		String token = AjaxHellper.getHttpParam(request(), "token");
		logger.info("is_success:"+is_success+";user_id:"+user_id+";real_name:"+real_name+";token:"+token);
		if("T".equals(is_success)){
			try {
				real_name = URLEncoder.encode(real_name,"UTF-8");
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
			AlipayLoginUserVO alipayUser = userService.checkAliPayUser(user_id);
			String url = "aliLogin://higegou?uid="+alipayUser.uid+"&phone="+alipayUser.phone+"&ispwds="+alipayUser.ispwds+"&real_name="+real_name+"&token="+token+"&usid="+user_id+"&unionid="+user_id;
			logger.info(url);
			return redirect(url);
		}else{
			response().setContentType("application/json;charset=utf-8");
			result.put("status", 0);
			result.put("msg", "快捷登录失败");
			return ok(Json.toJson(result));
		}
		
	}
	
	
	/**
	 * POST 快捷支付 点击完成后由客户端调用（使用优惠券支付金额为0）
	 * @param id
	 * @return
	 */
	public Result coupon_callback() {
		response().setContentType("application/json;charset=utf-8");
		String method = "90";
		String state = "20";
		Map<String, String> pramt = new HashMap<String, String>();
		String amount = AjaxHellper.getHttpParamOfFormUrlEncoded(request(), "amount");
		if(amount!=null){
			pramt.put("amount",amount);
		}
		String appversion=AjaxHellper.getHttpParamOfFormUrlEncoded(request(), "appversion");
		if(appversion!=null){
			pramt.put("appversion",appversion);
		}
		String deviceType = AjaxHellper.getHttpParamOfFormUrlEncoded(request(), "deviceType");
		if(deviceType!=null){
			pramt.put("deviceType",deviceType);
		}
		String devid = AjaxHellper.getHttpParamOfFormUrlEncoded(request(),"devid");
		if(devid!=null){
			pramt.put("devid",devid);
		}
		String idfa = AjaxHellper.getHttpParamOfFormUrlEncoded(request(),"idfa");
		if(idfa!=null){
			pramt.put("idfa",idfa);
		}
		String marketCode = AjaxHellper.getHttpParamOfFormUrlEncoded(request(),"marketCode");
		if(marketCode!=null){
			pramt.put("marketCode",marketCode);
		}
		String model = AjaxHellper.getHttpParamOfFormUrlEncoded(request(),"model");
		if(model!=null){
			pramt.put("model",model);
		}
		String osversion = AjaxHellper.getHttpParamOfFormUrlEncoded(request(),"osversion");
		if(osversion!=null){
			pramt.put("osversion",osversion);
		}
		String uid = AjaxHellper.getHttpParamOfFormUrlEncoded(request(), "uid");
		if(uid!=null){
			pramt.put("uid",uid);
		}
		String usewallet=AjaxHellper.getHttpParamOfFormUrlEncoded(request(), "usewallet");
		if(usewallet!=null){
			pramt.put("usewallet",usewallet);
		}
		String wdhjy = AjaxHellper.getHttpParamOfFormUrlEncoded(request(),"wdhjy");
		if(wdhjy!=null){
			pramt.put("wdhjy",wdhjy);
		}
		String orderCode = AjaxHellper.getHttpParamOfFormUrlEncoded(request(), "orderCode");
		if(orderCode!=null){
			pramt.put("orderCode",orderCode);
		}
		String vstr = AjaxHellper.getHttpParamOfFormUrlEncoded(request(), "vstr");
		if(vstr!=null){
			pramt.put("vstr",vstr);
		}
		String errcode = AjaxHellper.getHttpParamOfFormUrlEncoded(request(), "errcode");
		if(errcode!=null){
			pramt.put("errcode",errcode);
		}
		
		ObjectNode result = Json.newObject();
		
		String lwdjl=AjaxHellper.getHttpParamOfFormUrlEncoded(request(),"lwdjl")==null?"":AjaxHellper.getHttpParamOfFormUrlEncoded(request(),"lwdjl").trim();
		String sign=StringUtil.makeSig(pramt)+"HG2015";
		
		logger.info(sign);
		
		String md5sign="";
		try
		{
			String base64str=Base64.encodeBase64String(sign.getBytes("UTF-8"));
			md5sign=StringUtil.getMD5(base64str);
		}catch(Exception ex){
			md5sign="";
		}	
		
		if (!StringUtil.checkMd5(devid, wdhjy,appversion))
		{
			result.put("status", 0);
			result.put("msg", "验证失败");
			return ok(Json.toJson(result));
		}
		
		if (!StringUtil.checksign(lwdjl,md5sign,appversion)){
			result.put("status", 0);
			result.put("msg", "验证失败");
			return ok(Json.toJson(result));
		}
		
		if(StringUtils.isBlank(uid)||StringUtils.isBlank(orderCode)||StringUtils.isBlank(vstr)||StringUtils.isBlank(amount)){
			result.put("status", 4);
			result.put("msg", "调用参数错误");
			return ok(Json.toJson(result));
		}
		productService.setPayStatus(orderCode,method,state,Numbers.parseDouble(amount, 0D),"");
		result.put("status", 1);
		result.put("msg", "支付成功");
		return ok(Json.toJson(result));
	}
	
	/**
	 * POST 快捷支付 点击完成后由客户端调用（使用钱包支付金额为0）
	 * @param id
	 * @return
	 */
	public Result userBalance_callback() {
		response().setContentType("application/json;charset=utf-8");
		String method = "80";
		String state = "20";
		Map<String, String> pramt = new HashMap<String, String>();
		String amount = AjaxHellper.getHttpParamOfFormUrlEncoded(request(), "amount");
		if(amount!=null){
			pramt.put("amount",amount);
		}
		String appversion=AjaxHellper.getHttpParamOfFormUrlEncoded(request(), "appversion");
		if(appversion!=null){
			pramt.put("appversion",appversion);
		}
		String deviceType = AjaxHellper.getHttpParamOfFormUrlEncoded(request(), "deviceType");
		if(deviceType!=null){
			pramt.put("deviceType",deviceType);
		}
		String devid = AjaxHellper.getHttpParamOfFormUrlEncoded(request(),"devid");
		if(devid!=null){
			pramt.put("devid",devid);
		}
		String idfa = AjaxHellper.getHttpParamOfFormUrlEncoded(request(),"idfa");
		if(idfa!=null){
			pramt.put("idfa",idfa);
		}
		String marketCode = AjaxHellper.getHttpParamOfFormUrlEncoded(request(),"marketCode");
		if(marketCode!=null){
			pramt.put("marketCode",marketCode);
		}
		String model = AjaxHellper.getHttpParamOfFormUrlEncoded(request(),"model");
		if(model!=null){
			pramt.put("model",model);
		}
		String osversion = AjaxHellper.getHttpParamOfFormUrlEncoded(request(),"osversion");
		if(osversion!=null){
			pramt.put("osversion",osversion);
		}
		String uid = AjaxHellper.getHttpParamOfFormUrlEncoded(request(), "uid");
		if(uid!=null){
			pramt.put("uid",uid);
		}
		String usewallet=AjaxHellper.getHttpParamOfFormUrlEncoded(request(), "usewallet");
		if(usewallet!=null){
			pramt.put("usewallet",usewallet);
		}
		String wdhjy = AjaxHellper.getHttpParamOfFormUrlEncoded(request(),"wdhjy");
		if(wdhjy!=null){
			pramt.put("wdhjy",wdhjy);
		}
		String orderCode = AjaxHellper.getHttpParamOfFormUrlEncoded(request(), "orderCode");
		if(orderCode!=null){
			pramt.put("orderCode",orderCode);
		}
		String vstr = AjaxHellper.getHttpParamOfFormUrlEncoded(request(), "vstr");
		if(vstr!=null){
			pramt.put("vstr",vstr);
		}
		String errcode = AjaxHellper.getHttpParamOfFormUrlEncoded(request(), "errcode");
		if(errcode!=null){
			pramt.put("errcode",errcode);
		}
		
		ObjectNode result = Json.newObject();
		
		String lwdjl=AjaxHellper.getHttpParamOfFormUrlEncoded(request(),"lwdjl")==null?"":AjaxHellper.getHttpParamOfFormUrlEncoded(request(),"lwdjl").trim();
		String sign=StringUtil.makeSig(pramt)+"HG2015";
		
		logger.info(sign);
		
		String md5sign="";
		try
		{
			String base64str=Base64.encodeBase64String(sign.getBytes("UTF-8"));
			md5sign=StringUtil.getMD5(base64str);
		}catch(Exception ex){
			md5sign="";
		}	
		
		if (!StringUtil.checkMd5(devid, wdhjy,appversion))
		{
			result.put("status", 0);
			result.put("msg", "验证失败");
			return ok(Json.toJson(result));
		}
		
		if (!StringUtil.checksign(lwdjl,md5sign,appversion)){
			result.put("status", 0);
			result.put("msg", "验证失败");
			return ok(Json.toJson(result));
		}
		/*
		if(StringUtils.isBlank(uid)||StringUtils.isBlank(orderCode)||StringUtils.isBlank(vstr)||StringUtils.isBlank(amount)){
			result.put("status", 4);
			result.put("msg", "调用参数错误");
			return ok(Json.toJson(result));
		}*/
		productService.setPayStatus(orderCode,method,state,Numbers.parseDouble(amount, 0D),"");
		result.put("status", 1);
		result.put("msg", "支付成功");
		return ok(Json.toJson(result));
	}
	
	//获取阿里国际支付的必填参数alipayInternational_sign.php POST  阿里国际支付获取sign
	public static Result alipayInternational_sign() {
		response().setContentType("application/json;charset=utf-8");
		/*
		appversion=2.3.0&deviceType=0&devid=1bc4d023b16441779525d92d6b8f03cf&idfa=A4F4EAA1-DF7C-4CCC-9421-6B9DA9B81184
		&marketCode=1&model=iPhone 6 Plus&osversion=8.1&out_trade_no=1483923802&total_fee=0.01&uid=116297&wdhjy=61150db30237c7a8f383b603ece45293HG2015
                lwdjl：bd4c4252653c117c0c162f911231950d
        */ 
		Map<String, String> pramt = new HashMap<String, String>();
		String appversion=AjaxHellper.getHttpParamOfFormUrlEncoded(request(), "appversion");
		if(appversion!=null){
			pramt.put("appversion",appversion);
		}
		String deviceType = AjaxHellper.getHttpParamOfFormUrlEncoded(request(), "deviceType");
		if(deviceType!=null){
			pramt.put("deviceType",deviceType);
		}
		String devid = AjaxHellper.getHttpParamOfFormUrlEncoded(request(),"devid");
		if(devid!=null){
			pramt.put("devid",devid);
		}
		String idfa = AjaxHellper.getHttpParamOfFormUrlEncoded(request(),"idfa");
		if(idfa!=null){
			pramt.put("idfa",idfa);
		}
		String marketCode = AjaxHellper.getHttpParamOfFormUrlEncoded(request(),"marketCode");
		if(marketCode!=null){
			pramt.put("marketCode",marketCode);
		}
		String model = AjaxHellper.getHttpParamOfFormUrlEncoded(request(),"model");
		if(model!=null){
			pramt.put("model",model);
		}
		String osversion = AjaxHellper.getHttpParamOfFormUrlEncoded(request(),"osversion");
		if(osversion!=null){
			pramt.put("osversion",osversion);
		}
		String out_trade_no = AjaxHellper.getHttpParamOfFormUrlEncoded(request(), "out_trade_no");
		if(out_trade_no!=null){
			pramt.put("out_trade_no",out_trade_no);
		}
		String total_fee = AjaxHellper.getHttpParamOfFormUrlEncoded(request(), "total_fee");
		if(total_fee!=null){
			pramt.put("total_fee",total_fee);
		}
		String uid = AjaxHellper.getHttpParamOfFormUrlEncoded(request(), "uid");
		if(uid!=null){
			pramt.put("uid",uid);
		}
		String wdhjy = AjaxHellper.getHttpParamOfFormUrlEncoded(request(),"wdhjy");
		if(wdhjy!=null){
			pramt.put("wdhjy",wdhjy);
		}
		
	
		String lwdjl=AjaxHellper.getHttpParamOfFormUrlEncoded(request(),"lwdjl")==null?"":AjaxHellper.getHttpParamOfFormUrlEncoded(request(),"lwdjl").trim();
		String mysign=StringUtil.makeSig(pramt)+"HG2015";
		
		logger.info(mysign);
		
		String md5sign="";
		try
		{
			String base64str=Base64.encodeBase64String(mysign.getBytes("UTF-8"));
			md5sign=StringUtil.getMD5(base64str);			
		}catch(Exception ex){
			md5sign="";
		}	
		
		ObjectNode result = Json.newObject();
		if (!StringUtil.checkMd5(devid, wdhjy,appversion))
		{
			result.put("status", 0);
			result.put("msg","校验失败");
			return ok(Json.toJson(result));
		}
		
		if (!StringUtil.checksign(lwdjl,md5sign,appversion)){
			result.put("status", 0);
			result.put("msg","校验失败");
			return ok(Json.toJson(result));
		}
		if(StringUtils.isBlank(out_trade_no)||StringUtils.isBlank(total_fee)){
			return ok(Json.toJson(""));
		}
		Double price = Numbers.parseDouble(total_fee, 0D);
		String sign = AliPayService.getInstance().alipay_FT_sign(out_trade_no,price);
	
		if(StringUtils.isBlank(sign)){
			result.put("status", 0);
			result.put("msg", "参数不全");
			return ok(Json.toJson(result));
		}else{
			result.put("status", 1);
			result.put("sign", sign);
			return ok(Json.toJson(result));
		}
	}
	
	/**
	 * 阿里国际支付的回调(同步) POST 国际支付 点击完成后由客户端调用
	 * @param id
	 * @return
	 */
	public Result alipayInternational_callback() {
		response().setContentType("application/json;charset=utf-8");
		String method = "22";
		String state = "10";
		/*
		 amount=0.05&appversion=2.3.0&deviceType=0&devid=1bc4d023b16441779525d92d6b8f03cf
		 &errcode=6001&idfa=A4F4EAA1-DF7C-4CCC-9421-6B9DA9B81184
		 &marketCode=1&model=iPhone 6 Plus&orderCode=1483923802&osversion=8.1
		 &uid=116297&usewallet=0&vstr=92182357f1070002c9831845f1e0f631&wdhjy=61150db30237c7a8f383b603ece45293HG2015
         lwdjl：ef643b267cbebd9d457dcdf1761e7e0e
		*/
		Map<String, String> pramt = new HashMap<String, String>();
		String amount = AjaxHellper.getHttpParamOfFormUrlEncoded(request(), "amount");
		if(amount!=null){
			pramt.put("amount",amount);
		}
		String appversion=AjaxHellper.getHttpParamOfFormUrlEncoded(request(), "appversion");
		if(appversion!=null){
			pramt.put("appversion",appversion);
		}
		String deviceType = AjaxHellper.getHttpParamOfFormUrlEncoded(request(), "deviceType");
		if(deviceType!=null){
			pramt.put("deviceType",deviceType);
		}
		String devid = AjaxHellper.getHttpParamOfFormUrlEncoded(request(),"devid");
		if(devid!=null){
			pramt.put("devid",devid);
		}
		String idfa = AjaxHellper.getHttpParamOfFormUrlEncoded(request(),"idfa");
		if(idfa!=null){
			pramt.put("idfa",idfa);
		}
		String marketCode = AjaxHellper.getHttpParamOfFormUrlEncoded(request(),"marketCode");
		if(marketCode!=null){
			pramt.put("marketCode",marketCode);
		}
		String model = AjaxHellper.getHttpParamOfFormUrlEncoded(request(),"model");
		if(model!=null){
			pramt.put("model",model);
		}
		String osversion = AjaxHellper.getHttpParamOfFormUrlEncoded(request(),"osversion");
		if(osversion!=null){
			pramt.put("osversion",osversion);
		}
		String uid = AjaxHellper.getHttpParamOfFormUrlEncoded(request(), "uid");
		if(uid!=null){
			pramt.put("uid",uid);
		}
		String usewallet=AjaxHellper.getHttpParamOfFormUrlEncoded(request(), "usewallet");
		if(usewallet!=null){
			pramt.put("usewallet",usewallet);
		}
		String wdhjy = AjaxHellper.getHttpParamOfFormUrlEncoded(request(),"wdhjy");
		if(wdhjy!=null){
			pramt.put("wdhjy",wdhjy);
		}
		String orderCode = AjaxHellper.getHttpParamOfFormUrlEncoded(request(), "orderCode");
		if(orderCode!=null){
			pramt.put("orderCode",orderCode);
		}
		String vstr = AjaxHellper.getHttpParamOfFormUrlEncoded(request(), "vstr");
		if(vstr!=null){
			pramt.put("vstr",vstr);
		}
		String errcode = AjaxHellper.getHttpParamOfFormUrlEncoded(request(), "errcode");
		if(errcode!=null){
			pramt.put("errcode",errcode);
		}
		
		ObjectNode result = Json.newObject();
		
		String lwdjl=AjaxHellper.getHttpParamOfFormUrlEncoded(request(),"lwdjl")==null?"":AjaxHellper.getHttpParamOfFormUrlEncoded(request(),"lwdjl").trim();
		String sign=StringUtil.makeSig(pramt)+"HG2015";
		
		logger.info(sign);
		
		String md5sign="";
		try
		{
			String base64str=Base64.encodeBase64String(sign.getBytes("UTF-8"));
			md5sign=StringUtil.getMD5(base64str);
		}catch(Exception ex){
			md5sign="";
		}	
		
		if (!StringUtil.checkMd5(devid, wdhjy,appversion))
		{
			result.put("status", 0);
			result.put("msg", "验证失败");
			return ok(Json.toJson(result));
		}
		
		if (!StringUtil.checksign(lwdjl,md5sign,appversion)){
			result.put("status", 0);
			result.put("msg", "验证失败");
			return ok(Json.toJson(result));
		}
		
		if(StringUtils.isBlank(orderCode)||StringUtils.isBlank(vstr)||StringUtils.isBlank(amount)){
			result.put("status", 4);
			result.put("msg", "调用参数错误");
			return ok(Json.toJson(result));
		}

		if(orderCode.length()>10){
    		orderCode = orderCode.substring(0, 10);
    	}
		
		if (!"9000".equals(errcode)){
			shoppingOrderService.order_back(orderCode);
			result.put("status", 4);
			result.put("msg", "支付错误码"+errcode);
			return ok(Json.toJson(result));
		}
		String secret_str = "DX2014";
		String prestr=orderCode+amount;
		String checkStr = MD5.sign(secret_str, prestr, AlipayFTConfig.input_charset);
		if(!vstr.equals(checkStr)){
			result.put("status", 2);
			result.put("msg", "加密串不匹配");
			return ok(Json.toJson(result));
		}
		productService.setPayStatus(orderCode,method,state,Numbers.parseDouble(amount, 0D),"");
		result.put("status", 1);
		result.put("msg", "支付成功");
		logger.info("国际支付成功");
		return ok(Json.toJson(result));
	}
	
	/**
	 * 阿里国际支付的回调(异步) POST 国际支付的回调
	 * @param id
	 * @return
	 */
	public Result alipayFTNotify() {
		response().setContentType("application/json;charset=utf-8");
		String method = "22";		//22代表海外支付
		String state = "20";		//代表异步回调结果
		ObjectNode result = Json.newObject();
		Map<String,String> params = new HashMap<String,String>();
		Map<String,String[]> all =request().body().asFormUrlEncoded();
		if(all!=null && all.keySet()!=null){
			Iterator<String> keyIt = all.keySet().iterator();
			while(keyIt.hasNext()){
				String key = keyIt.next();
				String[] values = all.get(key);
				String valueStr = "";
				for (int i = 0; i < values.length; i++) {
					valueStr = (i == values.length - 1) ? valueStr + values[i]: valueStr + values[i] + ",";
				}
				//乱码解决，这段代码在出现乱码时使用。如果mysign和sign不相等也可以使用这段代码转化
				try {
					valueStr = new String(valueStr.getBytes("ISO-8859-1"), "utf-8");
				} catch (UnsupportedEncodingException e) {
					e.printStackTrace();
				}
				params.put(key, valueStr);
			}
			logger.info(Json.toJson(params)+"===============");
			String out_trade_no="";//商户订单号
			String trade_no="";//支付宝交易号
			String trade_status="";//交易状态
			try {
				out_trade_no = new String(params.get("out_trade_no")==null?"".getBytes():params.get("out_trade_no").getBytes("ISO-8859-1"),"UTF-8");
				trade_no = new String(params.get("trade_no")==null?"".getBytes():params.get("trade_no").getBytes("ISO-8859-1"),"UTF-8");
				trade_status =params.get("trade_status")==null?"": new String(params.get("trade_status").getBytes("ISO-8859-1"),"UTF-8");
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
			logger.info(out_trade_no+"==============="+trade_no+"trade_status:"+trade_status);

			//boolean verify_result =true;// AlipayNotify.verifyReturn(params);
			boolean verify_result = AlipayFTNotify.verify(params);
			
			if(verify_result){//验证成功
				if(trade_status.equals("TRADE_FINISHED")|| trade_status.equals("TRADE_SUCCESS")){
					ShoppingOrder shoppingOrder = productService.getShoppingOrderByOrderCode(out_trade_no);
					if(shoppingOrder!=null ){
						int status = productService.checkOrderPayStat(out_trade_no,shoppingOrder.getTotalFee());
						if(status==1){
							productService.setPayStatusFast(out_trade_no,method,state,shoppingOrder.getTotalFee(),trade_no);
							String resultStr = "success";
							/*result.put("status",1);
							result.put("msg","支付成功");*/
							logger.info("订单号："+out_trade_no+"，支付宝交易号："+trade_no+"，交易状态:"+trade_status+" 交易成功");
							return ok(resultStr);
						}else{
							result.put("status",3);
							result.put("msg","订单有误");
							return ok(Json.toJson(result));
						}
					}else{
						result.put("status",3);
						result.put("msg","订单有误");
						return ok(Json.toJson(result));
					}
				}
			}else{
				result.put("status",2);
				result.put("msg","加密串不匹配");
				return ok(Json.toJson(result));
			}
		}
		String retstr = "errorshow://orderStatus=0";
		return redirect(retstr);
	}
	
}
