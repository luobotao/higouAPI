package controllers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import models.AdBanner;
import models.AdLoading;

import org.apache.commons.lang3.StringUtils;

import play.Configuration;
import play.Logger;
import play.libs.F.Function;
import play.libs.F.Promise;
import play.libs.F;
import play.libs.Json;
import play.libs.ws.WS;
import play.libs.ws.WSResponse;
import play.mvc.Action;
import play.mvc.Result;
import play.mvc.Http.Context;
import services.ApplicationService;
import services.ICacheService;
import services.KuaidiService;
import services.ServiceFactory;
import services.UmengService;
import services.api.CertificationService;
import services.api.UserService;
import utils.AjaxHellper;
import utils.Constants;
import utils.ImageGenerator;
import utils.WSUtils;
import utils.bbt.BBTUtils;
import utils.certificate.CertificateUtils;
import utils.kuaidi100.JacksonHelper;
import utils.kuaidi100.KuaidiResult;
import utils.kuaidi100.NoticeRequest;
import utils.kuaidi100.NoticeResponse;
import utils.kuaidi100.ResultItem;
import vo.AdBannerVO;
import vo.loading.AppVersionVO;
import vo.loading.AppVersionVO.AppversionItem;
import vo.loading.LoadingVO;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import forms.Umeng.MessagePush;

@Named
@Singleton
public class Application extends BaseController {
	private static final Logger.ALogger logger = Logger.of(Application.class);
	
	private final ApplicationService applicationService;
	private final UserService userService;
	private final CertificationService certificationService;
	@Inject
	public Application(final ApplicationService applicationService,final UserService userService,final CertificationService certificationService) {
		this.applicationService = applicationService;
		this.userService = userService;
		this.certificationService = certificationService;
	}

	public static Result index() {
		response().setContentType("application/json;charset=utf-8");
		ICacheService cache = ServiceFactory.getCacheService();
		cache.setWithOutTime("foo2", "1",200);
		Map<String, String> m = new HashMap<String, String>();
		m.put("test", "value");
		cache.setObject("foo", m, 22);
		String r = cache.get("foo2") + " - foo2:" + cache.getObject("foo");
		logger.info(r);
		return ok();
	}
	public Result testFive() {
		response().setContentType("application/json;charset=utf-8");
		logger.info("5 minite is running");
		return ok();
	}
	public static Result wx(String param) {
    	return redirect("http://h5.higegou.com"+request().uri());
	}
	public static Result getWyAccessToken(String code) {
		response().setContentType("application/json;charset=utf-8");
		String app_id = "wx99199cff15133f37";
		String app_secret = "a017774f117bf0100a2f7939ef56c89a";
		String url = "https://api.weixin.qq.com/sns/oauth2/access_token?appid="+app_id+"&secret="+app_secret+"&code="+code+"&grant_type=authorization_code";
		logger.info("url:"+url);
		JsonNode result = WSUtils.getResponseAsJson(url);
		logger.info(result+"=============");
		return ok(Json.toJson(result));
	}
	
	
	public Result testRabbitMQ() {
		ObjectNode result = Json.newObject();
		String name = "陈建伟5",cardid= "130183198208290011";
		int status=0;
		String toast="";
		int flag = certificationService.checkNameWithCard("24", name, cardid);
		if (flag == 0) {
			status = 1;
		} else {
			status = 2;
			switch (flag) {
			case -1:
				toast = "请输入真实姓名，身份证信息。";
				break;
			case 1:
				toast = "请登录。";
				break;
			case 2:
				toast = "请输入真实收货人姓名。";
				break;
			case 3:
				toast = "请输入真实身份证号。";
				break;
			case 4:
				toast = "验证次数太多，请明天再试。";
				break;
			default:
				toast = "您购买的商品中有海外直邮商品，由海关统一清关入境，需要您完善真实的收货人身份信息。";
			}
		}
		result.put("status", status);
		result.put("toast",toast);
		return ok(Json.toJson(result));
	}
	
	// 图形验证码
	public static Result imageValidate() {
		ImageGenerator igt = ImageGenerator.make();
		// set Image code to session
		session(Constants.Session_Validate_Image, igt.imgCode);
		// write out image;
		response().setHeader("Pragma", "No-cache");
		response().setHeader("Cache-Control", "no-cache");
		response().setHeader("Expires", "0");

		return ok(igt.getImgBytes());
	}
		
	//向单个设备进行消息推送
	public static Result sendAndroidUnicast() {
		response().setContentType("application/json;charset=utf-8");
		MessagePush messagePush = new MessagePush();
		messagePush.ticker="ticker";
		messagePush.title="A";
		messagePush.text="11111111111111A";
		messagePush.display_type="notification";
		messagePush.after_open="go_app";
		String device_tokens = "AqXm1qhBElnn0_MEX1u7IM62-NqmBclEO4k776POASe6";//要推送的设备token
		
		JsonNode result = UmengService.getInstance().sendAndroidUnicast(device_tokens,messagePush);
		logger.info("push message to one device:"+result);
		return ok(Json.toJson(result));
	}
	
	//向所有设备进行消息推送
	public static Result sendAndroidBroadcast() {
		response().setContentType("application/json;charset=utf-8");
		MessagePush messagePush = new MessagePush();
		messagePush.ticker="ticker";
		messagePush.title="A";
		messagePush.text="11111111111111A";
		messagePush.display_type="notification";
		messagePush.after_open="go_app";
		
		JsonNode result = UmengService.getInstance().sendAndroidBroadcast(messagePush);
		logger.info("push message to All devices:"+result);
		return ok(Json.toJson(result));
	}
	
	//获取首页banner图接口(GET方式) 
	public Result adbanner() {
		response().setContentType("application/json;charset=utf-8");
		List<AdBanner> adBannerList = applicationService.getAdBanner();
		List<AdBannerVO> adBannerVOList = new ArrayList<>();
		for(AdBanner adBanner:adBannerList){
			adBannerVOList.add(AdBannerVO.create(adBanner));
		}
		return ok(Json.toJson(adBannerVOList));
	}
	//获取支付宝登录URL接口(GET方式) 
	public Result getAlipayLoginUrl() {
		response().setContentType("application/json;charset=utf-8");
		boolean IsProduct = Configuration.root().getBoolean("production", false);
		String domain = Configuration.root().getString("alipay.url.dev","http://182.92.227.140:9004");
		if(IsProduct){
			domain = Configuration.root().getString("alipay.url.product","http://api.higegou.com");
		}
		String url=domain+"/api/aliPaylogin";
		ObjectNode result = Json.newObject();
		result.put("status", 1);
		result.put("loginUrl", url);
		return ok(Json.toJson(result));
	}
	//获取支付宝登录URL接口(GET方式) 
	public Result aliPaylogin() {
		return ok(views.html.alipayLogin.render());
	}
	
	//(五)	获取加载图接口(GET方式) loading.php
	public Result loading() {
		response().setContentType("application/json;charset=utf-8");
		List<AdLoading> adLoadingList = userService.getAdLoadingList();
		LoadingVO loadingVO = new LoadingVO();
		boolean IsProduct = Configuration.root().getBoolean("production", false);
		String domain = Configuration.root().getString("domain.dev","http://ht2.neolix.cn");
		if(IsProduct){
			domain = Configuration.root().getString("domain.product","http://ht.neolix.cn");
		}
		domain =domain+Configuration.root().getString("adload","/pimgs/adload/");
		
		loadingVO.setImg1(domain+adLoadingList.get(0).getFilename());
		loadingVO.setImg2(domain+adLoadingList.get(1).getFilename());
		loadingVO.setImg3(domain+adLoadingList.get(2).getFilename());
		loadingVO.setImg4(domain+adLoadingList.get(3).getFilename());
		loadingVO.setStatus("1");
		return ok(Json.toJson(loadingVO));
	}
	//获取版本升级信息接口(GET方式) appversion.php
	public Result appversion() {
		response().setContentType("application/json;charset=utf-8");
		String os = AjaxHellper.getHttpParam(request(), "os");
		String version = AjaxHellper.getHttpParam(request(), "version");
		AppVersionVO result = new AppVersionVO();
		if(StringUtils.isBlank(os)||StringUtils.isBlank(version)){
			result.status=0;
			return ok(Json.toJson(result));
		}else{
			int ostype = 1;
			os = os.toLowerCase();
			if ("android".equals(os))
				ostype = 2;
			else {
				if ("ios".equals(os))
					ostype = 1;
				else {
					result.status = 2;
					return ok(Json.toJson(result));
				}
			}
			AppversionItem appversionItem = userService.getLatest(ostype,version);
			appversionItem.os = os;
			result.version = appversionItem;
			result.status=1;
		}
		return ok(Json.toJson(result));
	}
	//新浪微博用户获取信息并绑定接口(GET方式) getSinaData.php
	public Result getSinaData() {
		response().setContentType("application/json;charset=utf-8");
		String uid = AjaxHellper.getHttpParam(request(), "uid");
		String access_token = AjaxHellper.getHttpParam(request(), "access_token");
		String usid = AjaxHellper.getHttpParam(request(), "usid");
		String devid = AjaxHellper.getHttpParam(request(), "devid");
		String marketCode = AjaxHellper.getHttpParam(request(), "marketCode")==null?"":AjaxHellper.getHttpParam(request(), "marketCode");
		String appversion = AjaxHellper.getHttpParam(request(), "appversion")==null?"":AjaxHellper.getHttpParam(request(), "appversion");
		if(StringUtils.isBlank(uid)||StringUtils.isBlank(access_token)||StringUtils.isBlank(usid)||StringUtils.isBlank(devid)){
			return ok(Json.toJson(""));
		}else{
			String url="https://api.weibo.com/2/users/show.json?source=3543275707&access_token="+access_token+"&uid="+usid;
			long startTime = System.currentTimeMillis();
			Promise<JsonNode> jsonPromise = WS.url(url).get().map(
			        new Function<WSResponse, JsonNode>() {
			            public JsonNode apply(WSResponse response) {
			                JsonNode json = response.asJson();
			                return json;
			            }
			        }
			);
	    	long costTime = System.currentTimeMillis() - startTime;
	    	logger.info("-------------------------call " + url + " -  costs " +costTime + "ms");
	    	JsonNode weiboResult = jsonPromise.get(100000);
	    	if(weiboResult!=null){
	    		String nickname = weiboResult.get("screen_name")==null?"": weiboResult.get("screen_name").asText();
	    		String headIcon = weiboResult.get("profile_image_url")==null?"": weiboResult.get("profile_image_url").asText();
	    		logger.info(nickname);
	    		logger.info(headIcon);
	    		String newUid = userService.bindLogin(devid,uid,access_token,nickname,usid,headIcon,"2",marketCode,appversion);
	    		Boolean flag = userService.getIsBindPhone(newUid);
	    		ObjectNode result = Json.newObject();
	    		result.put("status", 1);
	    		result.put("uid", newUid);
	    		if(flag){
	    			result.put("isbindphone", 1);
	    		}else{
	    			result.put("isbindphone", 0);
	    		}
	    		result.put("weiboInfo", (ObjectNode)weiboResult);
	    		return ok(result);
	    	}
	    	return ok();
		}
//		return ok(Json.toJson(result));
	}
	
	
	//快递100查询订单状态
	public static Result queryOrderDetail() {
		response().setContentType("application/json;charset=utf-8");
		String company="shunfeng";
		String show ="3";	//返回类型： 0：返回json字符串， 1：返回xml对象， 2：返回html对象， 3：返回text文本。 如果不填，默认返回json字符串。
		String orderNumber="782001058634";
		String muti ="1";//返回信息数量：1:返回多行完整的信息， 0:只返回一行信息。 不填默认返回多行。
		String order ="desc";//排序：desc：按时间由新到旧排列，asc：按时间由旧到新排列。不填默认返回倒序（大小写不敏感） 
		
		JsonNode result = KuaidiService.getInstance().queryOrderDetailAsJson(company, orderNumber, muti, order);
		return ok(Json.toJson(result));
	}
	//快递100订阅一个订单
	public static Result subscribe() {
		response().setContentType("application/json;charset=utf-8");
		String company="shunfeng";
		String from="武汉";
		String to="北京";
		String orderNumber="782001092212";
		
		JsonNode result = KuaidiService.getInstance().subscribe(company, from, to, orderNumber);
		return ok(Json.toJson(result));
	}
	//快递100回调
	public static Result kuaidi100_callback() {
		response().setContentType("application/json;charset=utf-8");
		NoticeRequest req = new NoticeRequest();
		req.setBillstatus("polling");
		req.setMessage("到达");
		req.setStatus("check");
		req.getLastResult().setCom("yauntong");
		req.getLastResult().setCondition("F00");
		req.getLastResult().setIscheck("0");
		req.getLastResult().setNu("V030344422");
		req.getLastResult().setState("0");
		req.getLastResult().setStatus("200");
		req.getLastResult().setMessage("ok");
		ResultItem item = new ResultItem();
		item.setContext("上海分拨中心/装件入车扫描 ");
		item.setFtime("2012-08-28 16:33:19");
		item.setTime("2012-08-28 16:33:19");
		req.getLastResult().getData().add(item);
		item = new ResultItem();
		item.setContext("上海分拨中心/下车扫描");
		item.setFtime("2012-08-27 23:22:42");
		item.setTime("2012-08-27 23:22:42");
		req.getLastResult().getData().add(item);
		JsonNode testParam = Json.toJson(req);
		
		NoticeResponse resp = new NoticeResponse();
		resp.setResult(false);
		resp.setReturnCode("500");
		resp.setMessage("保存失败");
		
		JsonNode param = request().getQueryString("param")==null?testParam:Json.toJson(request().getQueryString("param"));
		
		NoticeRequest nReq = JacksonHelper.fromJSON(param+"",NoticeRequest.class);

		KuaidiResult result = nReq.getLastResult();
		JsonNode testParam2 = Json.toJson(result);
		System.out.println(testParam2);
		// 处理快递结果
		
		resp.setResult(true);
		resp.setReturnCode("200");
		
		return ok(Json.toJson(resp));
	}
	public static Result test(){
		BBTUtils.bbtOrder("", "", "");
		return ok();
	}
}
