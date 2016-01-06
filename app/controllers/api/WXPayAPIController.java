package controllers.api;

import java.math.BigDecimal;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import models.ShoppingOrder;
import net.sf.json.JSONObject;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;

import play.Logger;
import play.data.Form;
import play.libs.Json;
import play.mvc.Result;
import services.WXPayService;
import services.api.ProductService;
import services.api.ShoppingOrderService;
import utils.AjaxHellper;
import utils.Constants;
import utils.Numbers;
import utils.StringUtil;
import utils.wxpay.TenpayHttpClient;
import vo.product.ProductNewVO;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * 微信支付Controller
 * @author luobotao
 *
 */
@Named
@Singleton
public class WXPayAPIController extends BaseApiController {
	private static final Logger.ALogger logger = Logger.of(WXPayAPIController.class);
	private final ShoppingOrderService shoppingOrderService;
	private final ProductService productService;
    @Inject
    public WXPayAPIController(final ProductService productService,final ShoppingOrderService ShoppingOrderService) {
        this.productService = productService;
        this.shoppingOrderService = ShoppingOrderService;
    }
	/**
	 * {
    "retcode": "-2",
    "retmsg": "错误：获取prepayId失败"
}
	 * @return
	 */
	public static Result getSignAndPrepayID() {
		response().setContentType("application/json;charset=utf-8");
		JsonNode reslut = WXPayService.getInstance().getSignAndPrepayID("234234234",  1,"127.0.0.1");
		return ok(Json.toJson(reslut));
	}
	
	//wxprepay.php
	public static Result wxprepay() {
		response().setContentType("application/json;charset=utf-8");
		ObjectNode result = Json.newObject();
		/*
		 * amount=0.05&appversion=2.3.0&deviceType=0&devid=5a6e5839765241df964e6d40465b1693
		 * &idfa=5618B6D3-FE7E-4A93-A087-28AE061E40A0&marketCode=1&model=iPhone 4S&orderCode=1483923804&osversion=7.1.2
		 * &uid=116297&vstr=a39fd5439c1dfc92a724947e4cf819d9&wdhjy=fe858c0d2fe4e63616d04d7b5ddbfbafHG2015
		 *  
		 */
		Map<String, String> pramt = new HashMap<String, String>();
		String appversion = AjaxHellper.getHttpParam(request(), "appversion");
		if(StringUtils.isBlank(appversion)){
			appversion = AjaxHellper.getHttpParamOfFormUrlEncoded(request(), "appversion");
//			appversion = Form.form().bindFromRequest().get("appversion");
		}
		if(appversion!=null){
			pramt.put("appversion",appversion);
		}
		
		String wdhjy = AjaxHellper.getHttpParam(request(), "wdhjy");
		if(StringUtils.isBlank(wdhjy)){
			wdhjy = AjaxHellper.getHttpParamOfFormUrlEncoded(request(), "wdhjy");
		}
		if(wdhjy!=null){
			pramt.put("wdhjy",wdhjy);
		}
		
		String osversion = AjaxHellper.getHttpParam(request(), "osversion");
		if(StringUtils.isBlank(osversion)){
			osversion = AjaxHellper.getHttpParamOfFormUrlEncoded(request(), "osversion");
		}
		if(osversion!=null){
			pramt.put("osversion",osversion);
		}
		
		String model = AjaxHellper.getHttpParam(request(), "model");
		if(StringUtils.isBlank(model)){
			model = AjaxHellper.getHttpParamOfFormUrlEncoded(request(), "model");
		}
		if(model!=null){
			pramt.put("model",model);
		}
		
		String usewallet = AjaxHellper.getHttpParam(request(), "usewallet");
		if(StringUtils.isBlank(usewallet)){
			usewallet = AjaxHellper.getHttpParamOfFormUrlEncoded(request(), "usewallet");
		}
		if(usewallet!=null){
			pramt.put("usewallet",usewallet);
		}
		
		
		String deviceType = AjaxHellper.getHttpParam(request(), "deviceType");
		if(StringUtils.isBlank(deviceType)){
			deviceType = AjaxHellper.getHttpParamOfFormUrlEncoded(request(), "deviceType");
		}
		if(deviceType!=null){
			pramt.put("deviceType",deviceType);
		}
		
		String devid = AjaxHellper.getHttpParam(request(), "devid");
		if(StringUtils.isBlank(devid)){
			devid = AjaxHellper.getHttpParamOfFormUrlEncoded(request(), "devid");
			//devid = AjaxHellper.getHttpParamOfFormUrlEncoded(request(), "devid");
		}
		if(devid!=null){
			pramt.put("devid",devid);
		}
		
		String idfa = AjaxHellper.getHttpParam(request(), "idfa");
		if(StringUtils.isBlank(idfa)){
			idfa = AjaxHellper.getHttpParamOfFormUrlEncoded(request(), "idfa");
		}
		if(idfa!=null){
			pramt.put("idfa",idfa);
		}
		
		String marketCode = AjaxHellper.getHttpParam(request(), "marketCode");
		if(StringUtils.isBlank(marketCode)){
			marketCode = AjaxHellper.getHttpParamOfFormUrlEncoded(request(), "marketCode");
		}
		if(marketCode!=null){
			pramt.put("marketCode",marketCode);
		}
		
		String uid = AjaxHellper.getHttpParam(request(), "uid");
		if(StringUtils.isBlank(uid)){
			uid = AjaxHellper.getHttpParamOfFormUrlEncoded(request(), "uid");
		}
		if(uid!=null){
			pramt.put("uid",uid);
		}
		String orderCode = AjaxHellper.getHttpParam(request(), "orderCode");
		if(StringUtils.isBlank(orderCode)){
			orderCode = AjaxHellper.getHttpParamOfFormUrlEncoded(request(), "orderCode")==null?"":AjaxHellper.getHttpParamOfFormUrlEncoded(request(), "orderCode");
		}
		if(orderCode!=null){
			pramt.put("orderCode",orderCode);
		}
		String vstr = AjaxHellper.getHttpParam(request(), "vstr");
		if(StringUtils.isBlank(vstr)){
			vstr = AjaxHellper.getHttpParamOfFormUrlEncoded(request(), "vstr");
		}
		if(vstr!=null){
			pramt.put("vstr",vstr);
		}
		String amount = AjaxHellper.getHttpParam(request(), "amount");
		if(StringUtils.isBlank(amount)){
			amount = AjaxHellper.getHttpParamOfFormUrlEncoded(request(), "amount");
		}
		if(amount!=null){
			pramt.put("amount",amount);
		}
		
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
			result.put("msg","校验失败");
			return ok(Json.toJson(result));
		}
		
		if (!StringUtil.checksign(lwdjl,md5sign,appversion)){
			result.put("status", 0);
			result.put("msg","校验失败");
			return ok(Json.toJson(result));
		}
	
		if(StringUtils.isBlank(uid)||StringUtils.isBlank(orderCode)||StringUtils.isBlank(amount)||StringUtils.isBlank(vstr)){
			result.put("status", 0);
			return ok(Json.toJson(result));
		}
		String ip=request().remoteAddress();
		BigDecimal price = new BigDecimal(amount).multiply(new BigDecimal(100)).setScale(0, BigDecimal.ROUND_CEILING);
		JsonNode reslut = WXPayService.getInstance().getSignAndPrepayID(orderCode,  price.longValue(),ip);
		if(reslut==null){
			result.put("status", 0);
			return ok(Json.toJson(result));
		}else{
			result.put("status", 1);
			result.put("info", reslut);
			return ok(Json.toJson(result));
		}
	}

	/**
	 * 微信支付的回调(异步) POST WAP
	 * @param id
	 * @return
	 */
	public Result wxpayWapNotify() {
		response().setContentType("application/json;charset=utf-8");
		String method = "10";
		String state = "20";
		ObjectNode result = Json.newObject();
		
		String orderCode = AjaxHellper.getHttpParam(request(), "out_trade_no");
		if(StringUtils.isBlank(orderCode)){
			orderCode = AjaxHellper.getHttpParamOfFormUrlEncoded(request(), "out_trade_no")==null?"":AjaxHellper.getHttpParamOfFormUrlEncoded(request(), "out_trade_no");
		}
		String transaction_id = AjaxHellper.getHttpParam(request(), "transaction_id");
		if(StringUtils.isBlank(transaction_id)){
			transaction_id = AjaxHellper.getHttpParamOfFormUrlEncoded(request(), "transaction_id")==null?"":AjaxHellper.getHttpParamOfFormUrlEncoded(request(), "transaction_id");
		}
		if(orderCode.length()>10){
    		orderCode = orderCode.substring(0, 10);
    	}
		logger.info("orderCode:"+orderCode);
		ShoppingOrder shoppingOrder = productService.getShoppingOrderByOrderCode(orderCode);
		if(shoppingOrder!=null ){
			int status = productService.checkOrderPayStat(orderCode,shoppingOrder.getTotalFee());
			if(status==1){
				productService.setPayStatusFast(orderCode,method,state,shoppingOrder.getTotalFee(),transaction_id);
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
	/**
	 * 微信支付的回调(异步) 供客户端调用
	 * @param id
	 * @return
	 */
	public Result wxcallback() {
		response().setContentType("application/json;charset=utf-8");
		String method = "10";
		String state = "10";
		ObjectNode result = Json.newObject();
		Map<String, String> pramt = new HashMap<String, String>();
		
		// amount=0.01&appversion=2.3.0&deviceType=0&devid=5a6e5839765241df964e6d40465b1693&errcode=-2
		// &idfa=5618B6D3-FE7E-4A93-A087-28AE061E40A0&marketCode=1&model=iPhone 4S&orderCode=1483923801
		// &osversion=7.1.2&uid=116297&usewallet=0&vstr=13181043c7c6d686669dc602dedfda6a&wdhjy=fe858c0d2fe4e63616d04d7b5ddbfbafHG2015
		
        // lwdjl :c97711c45648e46c547bc67e57894956
		
		String appversion = AjaxHellper.getHttpParam(request(), "appversion");
		if(StringUtils.isBlank(appversion)){
			appversion = AjaxHellper.getHttpParamOfFormUrlEncoded(request(), "appversion");
		}
		if(appversion!=null){
			pramt.put("appversion",appversion);
		}
		
		String idfa = AjaxHellper.getHttpParam(request(), "idfa");
		if(StringUtils.isBlank(idfa)){
			idfa = AjaxHellper.getHttpParamOfFormUrlEncoded(request(), "idfa");
		}
		if(idfa!=null){
			pramt.put("idfa",idfa);
		}
		
		String marketCode = AjaxHellper.getHttpParam(request(), "marketCode");
		if(StringUtils.isBlank(marketCode)){
			marketCode = AjaxHellper.getHttpParamOfFormUrlEncoded(request(), "marketCode");
		}
		if(marketCode!=null){
			pramt.put("marketCode",marketCode);
		}
		
		String model = AjaxHellper.getHttpParam(request(), "model");
		if(StringUtils.isBlank(model)){
			model = AjaxHellper.getHttpParamOfFormUrlEncoded(request(), "model");
		}
		if(model!=null){
			pramt.put("model",model);
		}
		
		String wdhjy = AjaxHellper.getHttpParam(request(), "wdhjy");
		if(StringUtils.isBlank(wdhjy)){
			wdhjy = AjaxHellper.getHttpParamOfFormUrlEncoded(request(), "wdhjy");
		}
		if(wdhjy!=null){
			pramt.put("wdhjy",wdhjy);
		}
		
		String osversion = AjaxHellper.getHttpParam(request(), "osversion");
		if(StringUtils.isBlank(osversion)){
			osversion = AjaxHellper.getHttpParamOfFormUrlEncoded(request(), "osversion");
		}
		if(osversion!=null){
			pramt.put("osversion",osversion);
		}
		
		String usewallet = AjaxHellper.getHttpParam(request(), "usewallet");
		if(StringUtils.isBlank(usewallet)){
			usewallet = AjaxHellper.getHttpParamOfFormUrlEncoded(request(), "usewallet");
		}
		if(usewallet!=null){
			pramt.put("usewallet",usewallet);
		}
		
		
		String deviceType = AjaxHellper.getHttpParam(request(), "deviceType");
		if(StringUtils.isBlank(deviceType)){
			deviceType = AjaxHellper.getHttpParamOfFormUrlEncoded(request(), "deviceType");
		}
		if(deviceType!=null){
			pramt.put("deviceType",deviceType);
		}
		
		String devid = AjaxHellper.getHttpParam(request(), "devid");
		if(StringUtils.isBlank(devid)){
			devid = AjaxHellper.getHttpParamOfFormUrlEncoded(request(), "devid");
		}
		if(devid!=null){
			pramt.put("devid",devid);
		}
		
		String uid = AjaxHellper.getHttpParam(request(), "uid");
		if(StringUtils.isBlank(uid)){
			uid = AjaxHellper.getHttpParamOfFormUrlEncoded(request(), "uid");
		}
		if(uid!=null){
			pramt.put("uid",uid);
		}
		String orderCode = AjaxHellper.getHttpParam(request(), "orderCode");
		if(StringUtils.isBlank(orderCode)){
			orderCode = AjaxHellper.getHttpParamOfFormUrlEncoded(request(), "orderCode")==null?"":AjaxHellper.getHttpParamOfFormUrlEncoded(request(), "orderCode");
		}
		if(orderCode!=null){
			pramt.put("orderCode",orderCode);
		}
		String vstr = AjaxHellper.getHttpParam(request(), "vstr");
		if(StringUtils.isBlank(vstr)){
			vstr = AjaxHellper.getHttpParamOfFormUrlEncoded(request(), "vstr");
		}
		if(vstr!=null){
			pramt.put("vstr",vstr);
		}
		String amount = AjaxHellper.getHttpParam(request(), "amount");
		if(StringUtils.isBlank(amount)){
			amount = AjaxHellper.getHttpParamOfFormUrlEncoded(request(), "amount");
		}
		if(amount!=null){
			pramt.put("amount",amount);
		}
		String errcode = AjaxHellper.getHttpParam(request(), "errcode");
		if(StringUtils.isBlank(errcode)){
			errcode = AjaxHellper.getHttpParamOfFormUrlEncoded(request(), "errcode")==null?"100":AjaxHellper.getHttpParamOfFormUrlEncoded(request(), "errcode");
		}
		if(errcode!=null){
			pramt.put("errcode",errcode);
		}
		
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
			result.put("msg","校验失败");
			return ok(Json.toJson(result));
		}
		
		if (!StringUtil.checksign(lwdjl,md5sign,appversion)){
			result.put("status", 0);
			result.put("msg","校验失败");
			return ok(Json.toJson(result));
		}
		
		if(orderCode.length()>10){
    		orderCode = orderCode.substring(0, 10);
    	}
		logger.info("uid:"+uid+"orderCode:"+orderCode+"vstr:"+vstr+"amount:"+amount+"errcode:"+errcode);
		if(Numbers.parseInt(errcode, 100)!=0){
			shoppingOrderService.order_back(orderCode);
			result.put("status", 4);
			result.put("msg","支付错误码"+errcode);
			return ok(Json.toJson(result));
		}
		ShoppingOrder shoppingOrder = productService.getShoppingOrderByOrderCode(orderCode);
		if(shoppingOrder!=null ){
			int status = productService.checkOrderPayStat(orderCode,shoppingOrder.getTotalFee());
			if(status==1){
				productService.setPayStatus(orderCode,method,state,shoppingOrder.getTotalFee(),"");
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
	
	//微信回调JSAPI回调
	public Result WXpayreturnJSAPI(){

		String bodystr=request().body().toString();
		logger.info("微信回调contentType:"+bodystr);
		
		String return_code="";
		String result_code="";
		String out_trade_no="";
		String transaction_id="";
		String mch_id="";
		String appid="";
		
		String method = "11";
		String state = "20";
		StringBuilder result=new StringBuilder();
		
		//解析body
	    if(bodystr.indexOf("<xml>")>=0){
	    	bodystr=bodystr.substring(bodystr.indexOf("<xml>"),bodystr.indexOf("</xml>"))+"</xml>";
	    	logger.info("bodystr:"+bodystr);
	    }

	    if(!StringUtils.isBlank(bodystr)){
	    	try{
				org.dom4j.Document xmltmp=org.dom4j.DocumentHelper.parseText(bodystr.replace("\n", ""));
				if(xmltmp!=null){
					org.dom4j.Element root =xmltmp.getRootElement();
					appid=root.elementText("appid");
					mch_id=root.elementText("mch_id");
					out_trade_no=root.elementText("out_trade_no");
					return_code=root.elementText("return_code");
					transaction_id=root.elementText("transaction_id");
				}
	    	}
	    	catch(Exception e){
	    		logger.info("转换ＸＭＬ失败："+e.toString());
	    	}
		}
		if(!StringUtils.isBlank(return_code) && return_code.equals("SUCCESS")&&!StringUtils.isBlank(out_trade_no) && !StringUtils.isBlank(appid) && !StringUtils.isBlank(transaction_id) && !StringUtils.isBlank(mch_id)){
			if(out_trade_no.length()>10){
				out_trade_no = out_trade_no.substring(0, 10);
	    	}
			if(appid.equals(Constants.WXappID) && mch_id.equals(Constants.WXMCID)){
				ShoppingOrder shoppingOrder = productService.getShoppingOrderByOrderCode(out_trade_no);
				if(shoppingOrder!=null ){
					int status = productService.checkOrderPayStat(out_trade_no,shoppingOrder.getTotalFee());
					if(status==1){
						productService.setPayStatusFast(out_trade_no,method,state,shoppingOrder.getTotalFee(),transaction_id);
						result.append("<xml><return_code><![CDATA[SUCCESS]]></return_code>");
						result.append("<return_msg><![CDATA[OK]]></return_msg></xml>");
						return ok(result.toString());
					}
				}
			}
		}
		result.append("<xml><return_code><![CDATA[FAIL]]></return_code>");
		result.append("<return_msg><![CDATA[OK]]></return_msg></xml>");
		return ok(result.toString());
	}
	
	
	/*
	 * 微信退款接口
	 */
	public Result WXpayBackMoney(){
		response().setContentType("application/json;charset=utf-8");
		String transactionid=AjaxHellper.getHttpParamOfFormUrlEncoded(request(), "transactionid");
		String ordercode=AjaxHellper.getHttpParamOfFormUrlEncoded(request(), "ordercode");
		ObjectNode re=Json.newObject();
		re.put("status", "0");
		re.put("msg", "退款失败");
		ShoppingOrder order=shoppingOrderService.getShoppingOrderByOrderCode(ordercode);
		String nonce_str=RandomStringUtils.randomAlphanumeric(32);
		if(StringUtils.isBlank(transactionid) || StringUtils.isBlank(ordercode))
			return ok(re);
		
		Map<String,String> signmap=new HashMap<String,String>();
		signmap.put("appid",Constants.WXappID);
		signmap.put("mch_id",Constants.WXMCID);
		signmap.put("nonce_str", nonce_str);
		signmap.put("transaction_id", transactionid);
		signmap.put("out_trade_no",ordercode);
		signmap.put("out_refund_no", ordercode);
		signmap.put("total_fee", String.valueOf(100*order.getTotalFee()));
		signmap.put("refund_fee", String.valueOf(order.getFinalpay()));
		signmap.put("refund_fee_type", "CNY");
		signmap.put("op_user_id", Constants.WXMCID);
		signmap.put("sign", StringUtil.getSign(signmap));
		
		String postData = "<xml>";
		Set es = signmap.entrySet();
		Iterator it = es.iterator();
		while (it.hasNext()) {
			Map.Entry entry = (Map.Entry) it.next();
			String k = (String) entry.getKey();
			String v = (String) entry.getValue();
			if (k != "appkey") {
				postData += "<" + k + ">" + v + "</"+k+">";
			}
		}
		postData += "</xml>";
		String wxurl="https://api.mch.weixin.qq.com/secapi/pay/refund";

		String resContent = "";
		HttpClient client = new DefaultHttpClient();
		HttpPost post = new HttpPost(wxurl);
		post.setHeader("Content-Type", "text/xml; charset=UTF-8");
		try {
			post.setEntity(new StringEntity(postData,"UTF-8"));
			HttpResponse res = client.execute(post);
			String strResult = EntityUtils.toString(res.getEntity(), "UTF-8");

			Logger.info("weixin退款返回:" + strResult);
			if (strResult != null && strResult.length() > 0) {
				Document doc = null;
				doc = DocumentHelper.parseText(strResult);
				Element rootElt = doc.getRootElement();
				// returnstr=strResult;
				String return_code=rootElt.elementTextTrim("return_code");
				String return_msg=rootElt.elementTextTrim("return_code");
				if(!StringUtils.isBlank(return_code) && return_code.equals("")){
					//微信退款单号
					String refund_id=rootElt.elementText("refund_id");
					//微信退款渠道
					String refund_channel=rootElt.elementText("refund_channel");
					
					//记录退款日志表，待续............
					re.put("status", "1");
				}else{
					re.put("msg",StringUtils.isBlank(return_msg)?"调用退款失败":return_msg);
				}
			}
		}catch(Exception e){}
		
		return ok(re);
	}
	/**  
     * 初始化一个DocumentBuilder  
     *  
     * @return a DocumentBuilder  
     * @throws ParserConfigurationException  
     */  
    public static DocumentBuilder newDocumentBuilder()  
            throws ParserConfigurationException {  
        return newDocumentBuilderFactory().newDocumentBuilder();  
    }  
    /**  
     * 初始化一个DocumentBuilderFactory  
     *  
     * @return a DocumentBuilderFactory  
     */  
    public static DocumentBuilderFactory newDocumentBuilderFactory() {  
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();  
        dbf.setNamespaceAware(true);  
        return dbf;  
    }  
}
