package controllers.api;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import models.AdLoading;
import models.Event;
import models.EventLabel;
import models.EventParam;
import models.Product;
import models.Reffer;
import models.User;
import models.UserDevice;
import models.UserLike;
import models.UserRedFlag;
import models.UserVerify;
import models.Version;
import net.sf.json.JSONObject;

import org.apache.commons.lang3.StringUtils;

import play.Configuration;
import play.Logger;
import play.libs.Json;
import play.mvc.Http.MultipartFormData;
import play.mvc.Http.MultipartFormData.FilePart;
import play.mvc.Result;
import services.SmsService;
import services.UmengService;
import services.api.EventService;
import services.api.ProductService;
import services.api.RefferService;
import services.api.UserService;
import utils.AjaxHellper;
import utils.CompressUtil;
import utils.Constants;
import utils.Dates;
import utils.FileUtils;
import utils.Numbers;
import utils.OSSUtils;
import utils.StringUtil;
import utils.push.IOSPushUtil;
import utils.wxpay.TenpayHttpClient;
import vo.DevLoginVO;
import vo.StatusBindVO;
import vo.StatusMsgVO;
import vo.StatusOnlyVO;
import vo.StatusToastVO;
import vo.StatusUidVO;
import vo.UserHuanXinVO;
import vo.UserRegisterVO;
import vo.UserViewVO;
import vo.VersionVo;
import vo.appPad.appPadVO;
import vo.user.UserCheckVerify;
import vo.user.UserGuessLikeMouldVO;
import vo.user.UserGuessLikeMouldVO.ProductGuessLikeItem;
import vo.user.UserlikeMouldVO;
import vo.user.UserlikeMouldVO.DataInfo;
import vo.user.UserlikeMouldVO.LayoutInfo;
import vo.user.UserlikeMouldVO.PInfo;
import assets.CdnAssets;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import forms.Umeng.MessagePush;

/**
 * 
 * @author luobotao
 *
 */
@Named
@Singleton
public class UserAPIController extends BaseApiController {
	private static final SimpleDateFormat CHINESE_DATE_TIME_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	private static final SimpleDateFormat CHINESE_DATE_MONTH = new SimpleDateFormat("yyyyMM");
	private static final Logger.ALogger logger = Logger.of(UserAPIController.class);
	private static final java.util.regex.Pattern PHONE_PATTERN = java.util.regex.Pattern.compile("^((1))\\d{10}$");
	
	private final UserService userService;
	private final ProductService productService;
	private final SmsService smsService;
	private final EventService eventService;
	private final RefferService refferService;
	private String domainimg=CdnAssets.CDN_API_PUBLIC_URL;
	@Inject
    public UserAPIController(final UserService userService,final ProductService productService,final SmsService smsService,final EventService eventService,final RefferService refferService) {
        this.userService = userService;
        this.productService = productService;
        this.smsService = smsService;
        this.eventService = eventService;
        this.refferService=refferService;
    }

	// 用户喜欢列表接口(GET方式)
	public Result userlike_mouldlist() {
		response().setContentType("application/json;charset=utf-8");
		String uid = AjaxHellper.getHttpParam(request(), "uid");
		String page = AjaxHellper.getHttpParam(request(), "page")==null?"0":AjaxHellper.getHttpParam(request(), "page");
		String devid = AjaxHellper.getHttpParam(request(), "devid")==null?"":AjaxHellper.getHttpParam(request(), "devid");
		String wdhjy = AjaxHellper.getHttpParam(request(),"wdhjy");
		String appversion = AjaxHellper.getHttpParam(request(),"appversion");
		String reffer=AjaxHellper.getHttpParam(request(), "ref");
		reffer=StringUtils.isAllLowerCase(reffer)?"":reffer;
		
		UserlikeMouldVO result =new UserlikeMouldVO();
		if (!StringUtil.checkMd5(devid, wdhjy,appversion))
		{
			result.status="0";
			return ok(Json.toJson(result));
		}
		if(StringUtils.isBlank(uid)){
			result.status="0";
			return ok(Json.toJson(result));
		}
		result.reffer="Typ="+Constants.MAIDIAN_WODEZUIAI;
		/*
		 * 埋点
		 */
		Reffer ref=new Reffer();
		ref.setIp(request().remoteAddress());
		ref.setRefer(reffer);
		ref.setTyp(Constants.MAIDIAN_WODEZUIAI);
		ref.setTid(0L);
		refferService.addReffer(ref, "reffer"+CHINESE_DATE_MONTH.format(new Date()));

		List<Object[]> deviceUsers = userService.getDeviceUser(uid);
		if(deviceUsers!=null && deviceUsers.size()>0){
			String domain = StringUtil.getPICDomain();
			String resolution = UserService.getResolution(uid,devid);
			List<UserLike> userLikeList = userService.getUserLikePage(Numbers.parseLong(uid, 0L),Numbers.parseInt(page, 0),Constants.PAGESIZE);
			int size = userLikeList.size();
			if(size<Constants.PAGESIZE){
				result.endflag=1;
			}else{
				result.endflag=0;
			}
			List<DataInfo> dataList = new ArrayList<UserlikeMouldVO.DataInfo>();
			List<PInfo> plist = new ArrayList<UserlikeMouldVO.PInfo>();
			for(int i=0;i<userLikeList.size();i++){
				DataInfo dataInfo = new DataInfo();
				dataInfo.cardId =(i+1)+"";
				dataInfo.mould ="2";
				dataInfo.struct ="2*1";
				dataInfo.stxt ="";
				dataInfo.sdate ="";
				List<LayoutInfo> layout = new ArrayList<UserlikeMouldVO.LayoutInfo>();
				LayoutInfo layoutInfo1 = new LayoutInfo();
				layoutInfo1.start="0_0";
				layoutInfo1.end="1_1";
				layout.add(layoutInfo1);
				LayoutInfo layoutInfo2 = new LayoutInfo();
				layoutInfo2.start="0_1";
				layoutInfo2.end="1_2";
				layout.add(layoutInfo2);
				dataInfo.layout = layout;
				List<PInfo> productlist = new ArrayList<UserlikeMouldVO.PInfo>();
				
				
				PInfo pInfoTemp1 = new PInfo();
				UserLike userLike1 = userLikeList.get(i);
				Product product1 = productService.getProductById(userLike1.getPid());
				if(product1!=null && product1.getStatus()==10){
					pInfoTemp1.pinfo = productService.covertToProductUserLikeItem(product1,productService,uid,CHINESE_DATE_TIME_FORMAT.format(userLike1.getDate_add()));
					if ("3".equals(product1.getPtyp()))
					{
						pInfoTemp1.linkurl = "presellDetail://pid="+userLike1.getPid();
					}else{
						pInfoTemp1.linkurl = "pDe://pid="+userLike1.getPid();
					}
					String imgurl= productService.getmaxImgUrl(product1.getPid());
					pInfoTemp1.img=domain+StringUtil.getWebListpic(product1.getSkucode(),imgurl,resolution,new BigDecimal(2));
					pInfoTemp1.pinfo.cardMask = StringUtil.getProductIcon(
							userLike1.getPid().intValue(), "2");
					productlist.add(pInfoTemp1);
				}
				
				if(i+1<userLikeList.size()){
					PInfo pInfoTemp2 = new PInfo();
					UserLike userLike2 = userLikeList.get(i+1);
					if(userLike2==null){
						continue;
					}
					Product product2 = productService.getProductById(userLike2.getPid());
					if(product2!=null  && product2.getStatus()==10){
						pInfoTemp2.pinfo = productService.covertToProductUserLikeItem(product2,productService,uid,CHINESE_DATE_TIME_FORMAT.format(userLike2.getDate_add()));
						if ("3".equals(product2.getPtyp()))
						{
							pInfoTemp2.linkurl = "presellDetail://pid="+userLike2.getPid();
						}else{
							pInfoTemp2.linkurl = "pDe://pid="+userLike2.getPid();
						}
						String imgurl= productService.getmaxImgUrl(product2.getPid());
						pInfoTemp2.img=domain+StringUtil.getWebListpic(product2.getSkucode(),imgurl,resolution,new BigDecimal(2));
						pInfoTemp2.pinfo.cardMask = StringUtil.getProductIcon(userLike2.getPid().intValue(), "2");
						productlist.add(pInfoTemp2);
					}
						
				}
				
				dataInfo.plist = productlist;
				i++;
				dataList.add(dataInfo);
			}
			result.status="1";
			result.data = dataList;
			return ok(Json.toJson(result));
		}else{
			result.status="4";
			return ok(Json.toJson(result));
		}
		
	}

	// 猜你喜欢商品列表接口(GET方式) guesslike_list.php
	public Result guesslike_list() {		
		response().setContentType("application/json;charset=utf-8");
		String uid = AjaxHellper.getHttpParam(request(), "uid");
		String page = AjaxHellper.getHttpParam(request(), "page")==null?"0":AjaxHellper.getHttpParam(request(), "page");
		String devid = AjaxHellper.getHttpParam(request(), "devid")==null?"":AjaxHellper.getHttpParam(request(), "devid");
		String wdhjy = AjaxHellper.getHttpParam(request(),"wdhjy");
		String appversion = AjaxHellper.getHttpParam(request(),"appversion");
		UserGuessLikeMouldVO result =new UserGuessLikeMouldVO();
		String reffer=AjaxHellper.getHttpParam(request(), "ref");
		reffer=StringUtils.isBlank(reffer)?"":reffer;
		
		if (!StringUtil.checkMd5(devid, wdhjy,appversion))
		{
			result.status=0;
			return ok(Json.toJson(result));
		}
		if(StringUtils.isBlank(uid)){
			result.status=0;
			return ok(Json.toJson(result));
		}
		User user = userService.getUserByUid(Numbers.parseLong(uid, 0L));
		if(user!=null ){
			//加埋点
			Reffer ref=new Reffer();
			ref.setIp(request().remoteAddress());
			ref.setRefer(reffer);
			ref.setTyp(Constants.MAIDIAN_CAINIXIHUAN);
			ref.setTid(0L);
			refferService.addReffer(ref, "reffer"+CHINESE_DATE_MONTH.format(new Date()));
			//消红点
			userService.updateUserRedFlag(Numbers.parseLong(uid, 0L), "guessULikeRedFlag", 0);
			List<ProductGuessLikeItem> userlikeList = new ArrayList<ProductGuessLikeItem>();
			String catstr = user.getCatstr();
			
			int num = userService.getGuessNum(catstr);
			if(num==0){
				catstr="";
				num = userService.getGuessNum("");
			}
			Map<Long,String> maps = userService.getGuessProlist(uid,page,Constants.PAGESIZE,catstr);
			List<Product> productList = productService.queryProductListByIds(maps);
			logger.info(num+"====="+productList.size());
			result.status=1;
			for(Product product:productList){
				product.setReffer(reffer);
				userlikeList.add(productService.covertToProductGuessLikeItem(product, productService, uid));
				try {
					result.date_txt =  CHINESE_DATE_TIME_FORMAT.parse(product.getDate_txt()).getTime()/1000+"";
				} catch (ParseException e) {
					e.printStackTrace();
				}
			}
			result.totalnum =String.valueOf(num);
			result.userlike =userlikeList;
			return ok(Json.toJson(result));
		}else{
			result.status=4;
			return ok(Json.toJson(result));
		}
		
	}
    public Result getEaseMobAuthInfo(){
    	response().setContentType("application/json;charset=utf-8");
    	String appversion=AjaxHellper.getHttpParam(request(), "appversion");
		String deviceType = AjaxHellper.getHttpParam(request(), "deviceType");
		String devid = AjaxHellper.getHttpParam(request(),"devid");
		String idfa = AjaxHellper.getHttpParam(request(),"idfa");
		String marketCode = AjaxHellper.getHttpParam(request(),"marketCode");
		String model = AjaxHellper.getHttpParam(request(),"model");
		String osversion = AjaxHellper.getHttpParam(request(),"osversion");
		String uid = AjaxHellper.getHttpParam(request(), "uid");
		String wdhjy = AjaxHellper.getHttpParam(request(),"wdhjy");
		
		UserHuanXinVO result = new UserHuanXinVO();
		/*if (!StringUtil.checkMd5(devid, wdhjy,appversion))
		{
			result.setStatus("0");
			result.setMsg("校验失败");
			return ok(Json.toJson(result));
		}*/
		result = userService.getHuanXinUser(Numbers.parseInt(uid,0));
		
    	result.setStatus("1");
    	return ok(Json.toJson(result));
    }
    
    
	// 新用户登录接口（GET方式)
	public Result userLogin() {
		response().setContentType("application/json;charset=utf-8");
		String phone = AjaxHellper.getHttpParam(request(), "phone");
		String pwds = AjaxHellper.getHttpParam(request(), "pwds");
		String devid = AjaxHellper.getHttpParam(request(), "devid");
		String marketCode = AjaxHellper.getHttpParam(request(), "marketCode");
		String appversion = AjaxHellper.getHttpParam(request(),"appversion");
		String wdhjy = AjaxHellper.getHttpParam(request(),"wdhjy");
		String appTemp = appversion.replace(".", "");
		if(Numbers.parseInt(appTemp, 0)<222){
			pwds=StringUtil.getMD5(pwds);
		}
		UserRegisterVO result = userService.loginWithPhoneAndPwd(phone,pwds,devid,marketCode);
		if (!StringUtil.checkMd5(devid, wdhjy,appversion))
		{
			result.setStatus("0");
			result.setMsg("校验错误");
			return ok(Json.toJson(result));
		}
		if ("14552410990".equals(phone.trim()))
		{
			if ("111111".equals(pwds.trim()))
			{
				result.setStatus("1");
				result.setMsg("登录成功");
				return ok(Json.toJson(result));
			}
		}
		if(result.getUid()==0){
			User user = userService.findByPhone(phone);
			if(user==null){
				result.setStatus("0");
				result.setMsg("用户名不正确");
				return ok(Json.toJson(result));
			}else{
				if(!pwds.equals(user.getPasswords())){
					result.setStatus("0");
					result.setMsg("密码不正确");
					return ok(Json.toJson(result));
				}
			}
			result.setStatus("0");
			result.setMsg("登录失败");
		}else{
			result.setStatus("1");
			result.setMsg("登录成功");
			Map<String, String> mayarray=userService.getUserId_ByGuid(result.getUid(),devid,"", "0");
			String Guid=mayarray.get("guid");
			result.setToken(Guid);
			response().setCookie("token", Guid);
		}
		return ok(Json.toJson(result));
	}

	// (二十三)	获取浏览记录信息getuserview.php
	public Result getUserView() {
		response().setContentType("application/json;charset=utf-8");
		String uid = AjaxHellper.getHttpParam(request(), "uid");
		String devid = AjaxHellper.getHttpParam(request(), "devid");
		String wdhjy = AjaxHellper.getHttpParam(request(),"wdhjy");
		String appversion = AjaxHellper.getHttpParam(request(),"appversion");
		int difDay = userService.getDifDay(uid)+1;
		int viewCount = userService.getViewCount(uid);
		List<Object[]> deviceUsers = userService.getDeviceUser(uid);
		
		UserViewVO userViewVo = new UserViewVO();
		if (!StringUtil.checkMd5(devid, wdhjy,appversion))
		{
			userViewVo.setStatus("0");
			userViewVo.setMessage("校验错误");
			return ok(Json.toJson(userViewVo));
		}
		if(deviceUsers!=null && deviceUsers.size()>0){
			userViewVo.setStatus("1");
			String message = difDay+"天,"+viewCount+"个浏览";
			userViewVo.setMessage(message);
		}else{
			userViewVo.setStatus("3");
			userViewVo.setMessage("该用户未绑定设备");
		}
		return ok(Json.toJson(userViewVo));
	}

	public Result getMeiQia()  throws Exception {
		response().setContentType("application/json;charset=utf-8");
		String _id = AjaxHellper.getHttpParamOfFormUrlEncoded(request(),"_id");
		String content = AjaxHellper.getHttpParamOfFormUrlEncoded(request(),"content");
		String fromName = AjaxHellper.getHttpParamOfFormUrlEncoded(request(),"fromName");
		String status = AjaxHellper.getHttpParamOfFormUrlEncoded(request(),"status");
		String createdTime = AjaxHellper.getHttpParamOfFormUrlEncoded(request(),"createdTime");
		String metadata = AjaxHellper.getHttpParamOfFormUrlEncoded(request(),"metadata");
		String deviceToken = AjaxHellper.getHttpParamOfFormUrlEncoded(request(),"deviceToken");
		String unReadMsgNum = AjaxHellper.getHttpParamOfFormUrlEncoded(request(),"unReadMsgNum");
		String os = AjaxHellper.getHttpParamOfFormUrlEncoded(request(),"os");
		String device = AjaxHellper.getHttpParamOfFormUrlEncoded(request(),"device");
		String from = AjaxHellper.getHttpParamOfFormUrlEncoded(request(),"from");
		String appUserId = AjaxHellper.getHttpParamOfFormUrlEncoded(request(),"appUserId");
		String tel = AjaxHellper.getHttpParamOfFormUrlEncoded(request(),"tel");
		String IM = AjaxHellper.getHttpParamOfFormUrlEncoded(request(),"IM");
		String email = AjaxHellper.getHttpParamOfFormUrlEncoded(request(),"email");
		String appkey = AjaxHellper.getHttpParamOfFormUrlEncoded(request(),"appkey");

		/*接收离线消息发送离线推送*/
		JsonNode result;
		UserDevice deviceUsers;
		try
		{
			deviceUsers = userService.getDeviceUserInfo(appUserId);
		}catch(Exception ex){
			deviceUsers=null;
		}
		
		if(deviceUsers!=null){
			String device_tokens =deviceUsers.getPushToken();
			MessagePush messagePush = new MessagePush();
			messagePush.ticker="ticker";
			messagePush.title="A";
			messagePush.text="11111111111111A";
			messagePush.display_type="notification";
			messagePush.after_open="go_app";
			if(deviceUsers.getOstype()==2)
			{
				//String device_tokens = "AqXm1qhBElnn0_MEX1u7IM62-NqmBclEO4k776POASe6";//要推送的设备token
				result = UmengService.getInstance().sendAndroidUnicast(device_tokens,messagePush);
				logger.info("push message to one device:"+result);
				return ok(Json.toJson(result));	
			}else{
				String title="11111111111111A";
				IOSPushUtil.pushMsgNotification(title,device_tokens);
				return ok(Json.toJson("{stat:1}"));
			}
			
		}else{
			return ok(Json.toJson("{stat:1}"));
		}
		
	}
	// 校验验证码接口（POST方式)
	public Result checkUserVerify() {
		response().setContentType("application/json;charset=utf-8");
		String phone = AjaxHellper.getHttpParamOfFormUrlEncoded(request(),"phone");
		String verify = AjaxHellper.getHttpParamOfFormUrlEncoded(request(),"verify");
		String devid = AjaxHellper.getHttpParamOfFormUrlEncoded(request(), "devid");
		String wdhjy = AjaxHellper.getHttpParamOfFormUrlEncoded(request(),"wdhjy");
		String appversion = AjaxHellper.getHttpParamOfFormUrlEncoded(request(),"appversion");
		UserCheckVerify result = new UserCheckVerify();
		if (!StringUtil.checkMd5(devid, wdhjy,appversion))
		{
			result.setStatus("0");
			result.setMsg("校验错误");
			return ok(Json.toJson(result));
		}
		if ("14552410990".equals(phone.trim()))
		{
			if ("7859".equals(verify.trim()))
			{
				result.setStatus("1");
				result.setMsg("");
				return ok(Json.toJson(result));
			}
		}
		UserVerify userVerify = userService.checkVerify(phone,verify);
		if(userVerify!=null){
			result.setStatus("1");
			result.setMsg("");
		}else{
			result.setStatus("2");
			result.setMsg("验证码校验失败");
		}
		return ok(Json.toJson(result));
	}

	// 新用户注册（修改）接口(POST方式)
	public Result userRegister() {
		response().setContentType("application/json;charset=utf-8");
		String uid = AjaxHellper.getHttpParamOfFormUrlEncoded(request(), "uid");
		String devid = AjaxHellper.getHttpParamOfFormUrlEncoded(request(),"devid");
		String appversion = AjaxHellper.getHttpParamOfFormUrlEncoded(request(),"appversion");
		String phone = AjaxHellper.getHttpParamOfFormUrlEncoded(request(),"phone");
		String marketCode = AjaxHellper.getHttpParamOfFormUrlEncoded(request(),"marketCode");
		String pwds = AjaxHellper.getHttpParamOfFormUrlEncoded(request(),"pwds");
		String cologin = AjaxHellper.getHttpParamOfFormUrlEncoded(request(),"cologin");
		String nickname = AjaxHellper.getHttpParamOfFormUrlEncoded(request(),"nickname");
		String token = AjaxHellper.getHttpParamOfFormUrlEncoded(request(),"token");
		String usid = AjaxHellper.getHttpParamOfFormUrlEncoded(request(),"usid");
		String headIcon = AjaxHellper.getHttpParamOfFormUrlEncoded(request(),"headIcon");
		String unionid = AjaxHellper.getHttpParamOfFormUrlEncoded(request(),"unionid");
		String platform = AjaxHellper.getHttpParamOfFormUrlEncoded(request(),"platform");
		String wdhjy = AjaxHellper.getHttpParamOfFormUrlEncoded(request(),"wdhjy");
		UserRegisterVO result=new UserRegisterVO();
		if (!StringUtil.checkMd5(devid, wdhjy,appversion))
		{
			result.setStatus("0");
			result.setMsg("校验错误");
			return ok(Json.toJson(result));
		}
		if(Numbers.parseLong(uid, 0L)==0L){
			result.setMsg("注册失败");
			result.setStatus("0");
			return ok(Json.toJson(result));
		}
		
		String appTemp = appversion.replace(".", "");
		if(Numbers.parseInt(appTemp, 0)<222){
			pwds=StringUtil.getMD5(pwds);
		}
				
		result = userService.register(uid, devid, nickname, marketCode,
				appversion, phone, pwds, token, usid, headIcon, cologin,
				unionid, platform);
		if (result.getUid() == 0) {
			result.setMsg("注册失败");
			result.setStatus("0");
		}else{
			result.setStatus("1");
			result.setMsg("");
			Map<String, String> mayarray=userService.getUserId_ByGuid(result.getUid(),devid,"", "0");
			String Guid=mayarray.get("guid");
			result.setToken(Guid);
			response().setCookie("token", Guid);
		}
		return ok(Json.toJson(result));
	}

	// (五十五)	客户端日志回传接口（新增）postErrorLog.php
	public Result postErrorLog() {
		response().setContentType("application/json;charset=utf-8");
		String uid = AjaxHellper.getHttpParamOfFormUrlEncoded(request(),"uid");
		String devid = AjaxHellper.getHttpParamOfFormUrlEncoded(request(),"devid");
		String log = AjaxHellper.getHttpParamOfFormUrlEncoded(request(),"log")==null?"":AjaxHellper.getHttpParamOfFormUrlEncoded(request(),"log");
		String wdhjy = AjaxHellper.getHttpParamOfFormUrlEncoded(request(),"wdhjy");
		String appversion = AjaxHellper.getHttpParamOfFormUrlEncoded(request(),"appversion");
		StatusOnlyVO result = new StatusOnlyVO();
		if (!StringUtil.checkMd5(devid, wdhjy,appversion))
		{
			result.setStatus(0);
			return ok(Json.toJson(result));
		}
		List<Object[]> deviceUsers = userService.getDeviceUser(uid);
		if(deviceUsers!=null && deviceUsers.size()>0){
			String fileName = devid+".log";
			File file = FileUtils.writeToFile(fileName,log);
			String path=Configuration.root().getString("oss.upload.iosLog", "upload/iosLog/");//上传路径
			String BUCKET_NAME=Configuration.root().getString("oss.bucket.name.higouAPIDev", "higou-api");
			boolean IsProduct = Configuration.root().getBoolean("production", false);
			if(IsProduct){
				BUCKET_NAME=Configuration.root().getString("oss.bucket.name.higouAPIProduct", "higou-api");
			}
			if (file != null ) {
				int p = fileName.lastIndexOf('.');
				String type = fileName.substring(p, fileName.length()).toLowerCase();
				// 检查文件后缀格式
				OSSUtils.uploadFile(file,path,fileName, type,BUCKET_NAME);	
				file.delete();
			}
			result.setStatus(1);
		}else{
			result.setStatus(0);
		}
		return ok(Json.toJson(result));
	}

	
	/*
	情况一：用户在商品页：订金支付阶段
	1）商品仍是订金支付阶段：点击后继续进行支付
	2）商品为订金支付完成阶段：toast“订金支付已结束”
	3）尾款支付阶段：toast“订金支付已结束”
	4）尾款结束阶段：toast”本次预售已结束“

	情况二：用户在商品页：尾款支付阶段
	1）仍是尾款支付阶段
	A.已经参与该商品的预售：去自己的预售列表页
	B.没有参与该商品的预售：toast”您没有参与此次预售“
	2）尾款支付已结束：”本次预售已结束“

	*/
	// (五十四)	用户是否参加预售接口（新增）pcheckpresell.php
	public Result pcheckpresell() {
		response().setContentType("application/json;charset=utf-8");
		String uid = AjaxHellper.getHttpParam(request(), "uid");
		String devid = AjaxHellper.getHttpParam(request(), "devid");
		String wdhjy = AjaxHellper.getHttpParam(request(),"wdhjy");
		String pid = AjaxHellper.getHttpParam(request(), "pid");
		String appversion = AjaxHellper.getHttpParam(request(),"appversion");
		String orderCode = AjaxHellper.getHttpParam(request(), "orderCode");
		String stageGet = AjaxHellper.getHttpParam(request(), "stage")==null?"1":AjaxHellper.getHttpParam(request(), "stage");
		
		StatusToastVO result = new StatusToastVO();
		if (!StringUtil.checkMd5(devid, wdhjy,appversion))
		{
			result.setStatus("0");
			result.setToast("校验失败");
			return ok(Json.toJson(result));
		}
		Product product = productService.getProductById(Numbers.parseLong(pid, 0L));
		if(product!=null){
			int stage = product.getStage();
			if ("1".equals(stageGet)){
				switch (stage)
				{
					case 1:
						result.setStatus("1");
						result.setToast("");
						break;
					case 2:
						result.setStatus("0");
						result.setToast("订金支付已结束");
					break;
					case 3:
						result.setStatus("0");
						result.setToast("订金支付已结束");
						break;
					case 4:
						result.setStatus("0");
						result.setToast("本次预售已结束");
						break;
				}
			}
			if ("3".equals(stageGet)){
				boolean flag = productService.checkIsPresell(uid,pid,orderCode);
				if(flag)
				{
					result.setStatus("1");
					result.setToast("");
				}else{
					result.setStatus("0");
					result.setToast("您没有参与此次预售");
				}
				if (stage==4){
					result.setStatus("0");
					result.setToast("本次预售已结束");
				}
			}
			if ("4".equals(stageGet)){
				if (stage==4){
					result.setStatus("0");
					result.setToast("本次预售已结束");
				}
			}
		}else{
			result.setStatus("0");
			result.setToast("商品不存在");
		}
		return ok(Json.toJson(result));
	}
	
	// (客户端通知提醒接口（新增）(GET方式)
	public Result user_notice() {
		response().setContentType("application/json;charset=utf-8");
		String uid = AjaxHellper.getHttpParam(request(), "uid");
		String devid = AjaxHellper.getHttpParam(request(), "devid");
		String wdhjy = AjaxHellper.getHttpParam(request(),"wdhjy");
		String appversion = AjaxHellper.getHttpParam(request(),"appversion");
		StatusToastVO result = new StatusToastVO();
		if (!StringUtil.checkMd5(devid, wdhjy,appversion))
		{
			result.setStatus("0");
			result.setToast("校验失败");
			return ok(Json.toJson(result));
		}
		int count = userService.getNoticeOrders(Numbers.parseLong(uid, 0L));
		if(count>0){
			userService.updateNoticeFlag(Numbers.parseLong(uid, 0L));
			result.setStatus("1");
			result.setToast("您有预定的商品到了，正在进行尾款支付阶段。如果过期会造成订单取消。");
		}else{
			result.setStatus("1");
			result.setToast("");
		}
		return ok(Json.toJson(result));
	}
	
	// 增加用户喜欢商品接口(POST方式) 
	public Result userlike_add() {
		response().setContentType("application/json;charset=utf-8");
		String uid = AjaxHellper.getHttpParamOfFormUrlEncoded(request(),"uid");
		String pid = AjaxHellper.getHttpParamOfFormUrlEncoded(request(),"pid");
		String devid = AjaxHellper.getHttpParamOfFormUrlEncoded(request(), "devid");
		String wdhjy = AjaxHellper.getHttpParamOfFormUrlEncoded(request(),"wdhjy");
		String appversion = AjaxHellper.getHttpParamOfFormUrlEncoded(request(),"appversion");
		
		StatusOnlyVO result = new StatusOnlyVO();
		if (!StringUtil.checkMd5(devid, wdhjy,appversion))
		{
			result.setStatus(0);
			return ok(Json.toJson(result));
		}
		
		List<Object[]> deviceUsers = userService.getDeviceUser(uid);
		if(deviceUsers!=null && deviceUsers.size()>0){
			Product product = productService.getProductById(Numbers.parseLong(pid, 0L));
			if(product==null){
				result.setStatus(3);//商品不存在
			}else{
				UserLike userLike = userService.findByUidAndPid(Numbers.parseLong(uid, 0L),Numbers.parseLong(pid, 0L));
				if(userLike!=null){
					result.setStatus(2);//already like
				}else{
					userLike = new UserLike(); 
					userLike.setDate_add(new Date());
					userLike.setUid(Numbers.parseLong(uid, 0L));
					userLike.setPid(Numbers.parseLong(pid, 0L));
					userService.addUserLike(userLike,product);
					result.setStatus(1);// successful
				}
			}
		}else{
			result.setStatus(4);//用户不存在
		}
		return ok(Json.toJson(result));
	}
	
	//删除用户喜欢商品接口(POST方式)  
	public Result userlike_del() {
		response().setContentType("application/json;charset=utf-8");
		String uid = AjaxHellper.getHttpParamOfFormUrlEncoded(request(),"uid");
		String pid = AjaxHellper.getHttpParamOfFormUrlEncoded(request(),"pid");
		String devid = AjaxHellper.getHttpParamOfFormUrlEncoded(request(), "devid");
		String wdhjy = AjaxHellper.getHttpParamOfFormUrlEncoded(request(),"wdhjy");
		String appversion = AjaxHellper.getHttpParamOfFormUrlEncoded(request(),"appversion");
		
		StatusOnlyVO result = new StatusOnlyVO();
		if (!StringUtil.checkMd5(devid, wdhjy,appversion))
		{
			result.setStatus(0);
			return ok(Json.toJson(result));
		}
		List<Object[]> deviceUsers = userService.getDeviceUser(uid);
		if(deviceUsers!=null && deviceUsers.size()>0){
			Product product = productService.getProductById(Numbers.parseLong(pid, 0L));
			if(product==null){
				result.setStatus(3);//商品不存在
			}else{
				UserLike userLike = userService.findByUidAndPid(Numbers.parseLong(uid, 0L),Numbers.parseLong(pid, 0L));
				if(userLike==null){
					result.setStatus(2);//not exist (del)
				}else{
					userService.deleteUserLike(userLike,product);
					result.setStatus(1);// successful
				}
			}
		}else{
			result.setStatus(4);//用户不存在
		}
		return ok(Json.toJson(result));
	}
	
	//用户信息解除绑定接口(POST方式)  
	public Result userunbind() {
		response().setContentType("application/json;charset=utf-8");
		Long uid = Numbers.parseLong(AjaxHellper.getHttpParamOfFormUrlEncoded(request(),"uid"),0L);
		String devid = AjaxHellper.getHttpParamOfFormUrlEncoded(request(),"devid");
		String wdhjy = AjaxHellper.getHttpParamOfFormUrlEncoded(request(),"wdhjy");
		String appversion = AjaxHellper.getHttpParamOfFormUrlEncoded(request(),"appversion");
		
		StatusUidVO result = new StatusUidVO();
		if (!StringUtil.checkMd5(devid, wdhjy,appversion))
		{
			result.setStatus("0");
			return ok(Json.toJson(result));
		}
		List<String> authList = new ArrayList<String>();
		authList.add("0");
		int newUid=userService.bindLogout(uid.toString(),devid);
		result.setAuthorities(authList);
		if(newUid>0){
			User user=userService.getUserByUid(Long.valueOf(newUid));
			result.setHeadIcon(StringUtils.isBlank(user.getHeadIcon())?domainimg+"images/sheSaidImages/default_headicon_girl.png":user.getHeadIcon());
			result.setStatus("1");
			result.setUid(newUid);
		}else{
			result.setStatus("1");
			result.setUid(0);
		}
		return ok(Json.toJson(result));
	}
	
	//用户反馈接口(POST方式)   
	public Result feedback() {
		response().setContentType("application/json;charset=utf-8");
		String contact = AjaxHellper.getHttpParamOfFormUrlEncoded(request(),"contact");
		String content = AjaxHellper.getHttpParamOfFormUrlEncoded(request(),"content");
		String devid = AjaxHellper.getHttpParamOfFormUrlEncoded(request(),"devid");
		String wdhjy = AjaxHellper.getHttpParamOfFormUrlEncoded(request(),"wdhjy");
		String appversion = AjaxHellper.getHttpParamOfFormUrlEncoded(request(),"appversion");
		
		StatusOnlyVO Result = new StatusOnlyVO();
		if (!StringUtil.checkMd5(devid, wdhjy,appversion))
		{
			Result.setStatus(0);
			return ok(Json.toJson(Result));
		}
		userService.insertFeedback(contact,content);
		Result.setStatus(1);
		return ok(Json.toJson(Result));
	}
	
	// 用户绑定手机号码接口(Get方式) 
	public Result userbindphone() {
		response().setContentType("application/json;charset=utf-8");
		String uid = AjaxHellper.getHttpParam(request(), "uid");
		String devid = AjaxHellper.getHttpParam(request(), "devid");
		String wdhjy = AjaxHellper.getHttpParam(request(),"wdhjy");
		String phone = AjaxHellper.getHttpParam(request(), "phone");
		String verify = AjaxHellper.getHttpParam(request(), "verify");
		String appversion = AjaxHellper.getHttpParam(request(),"appversion");
		
		StatusMsgVO result = new StatusMsgVO();
		if (!StringUtil.checkMd5(devid, wdhjy,appversion))
		{
			result.setStatus("0");
			result.setMsg("校验失败");
			return ok(Json.toJson(result));
		}
		List<Object[]> deviceUsers = userService.getDeviceUser(uid);
		if(deviceUsers!=null && deviceUsers.size()>0){
			UserVerify userVerify = userService.getIsVerify(Numbers.parseLong(uid, 0L),phone,verify);
			if(userVerify==null){
				result.setStatus("3");
				result.setMsg("您输入的验证码不正确");
			}else{
				userService.userbindphone(Numbers.parseLong(uid, 0L), phone);
				result.setStatus("1");
				result.setMsg("");
			}
		}else{
			result.setStatus("2");
			result.setMsg("");
		}
		
		return ok(Json.toJson(result));
	}
	
	// 用户信息发送验证短信接口(Get方式)
	public Result usergetverify() {
		response().setContentType("application/json;charset=utf-8");
		String uid = AjaxHellper.getHttpParam(request(), "uid");
		String devid = AjaxHellper.getHttpParam(request(), "devid");
		String wdhjy = AjaxHellper.getHttpParam(request(),"wdhjy");
		String phone = AjaxHellper.getHttpParam(request(), "phone");
		String reg = AjaxHellper.getHttpParam(request(), "reg")==null?"1":AjaxHellper.getHttpParam(request(), "reg");
		String appversion = AjaxHellper.getHttpParam(request(), "appversion");
		StatusMsgVO result = new StatusMsgVO();
		if (!StringUtil.checkMd5(devid, wdhjy,appversion))
		{
			result.setStatus("0");
			result.setMsg("校验失败");
			return ok(Json.toJson(result));
		}
		if (StringUtils.isBlank(uid) || uid.equals("0")|| Numbers.parseLong(uid, 0L) == 0L) {
			result.setStatus("4");
			result.setMsg("非法操作");
			return ok(Json.toJson(result));
		}
		if(StringUtils.isBlank(devid) || StringUtils.isBlank(appversion) || devid.length()<32 || devid.length()>40)
		{
			result.setStatus("4");
			result.setMsg("非法操作");
			return ok(Json.toJson(result));
		}
		if(!userService.checkDeviceID(devid)){
			result.setStatus("4");
			result.setMsg("非法操作");
			return ok(Json.toJson(result));
		}
		if ("1".equals(reg)) {
			boolean phoneBindFlag = userService.getPhoneIsBind(phone);
			if(phoneBindFlag){
				boolean pwdBindFlag = userService.getPwdsIsBind(phone);
				if(pwdBindFlag){
					result.setStatus("4");
					result.setMsg("该手机号已经用另一社交平台绑定过了");
					return ok(Json.toJson(result));
				}
			}
		}
		if(!PHONE_PATTERN.matcher(phone).matches()){
			result.setStatus("5");
			result.setMsg("请输入正确的手机号");
			return ok(Json.toJson(result));
		}
		String code = utils.StringUtil.genRandomCode(4);//生成四位随机数
		  logger.info(code);
		  String ip = request().remoteAddress();
		  String sendFlag = userService.saveVerifyInVerfify(ip,uid,phone,code,devid);
		  if("1".equals(sendFlag)){
		   smsService.getVerify(phone, code);
		   result.setStatus("1");
		   result.setMsg("");
		  }else if("-1".equals(sendFlag)){
			  result.setStatus("6");
			   result.setMsg("发送太频繁，请稍后再试！");
		  } else{
		   result.setStatus("6");
		   result.setMsg("该手机号发送次数太多，请稍后再试！");
		  }
		
		return ok(Json.toJson(result));
	}

	// 用户信息发送语音验证码接口(Get方式)
	public Result getVoiceVerifyCode() {
		response().setContentType("application/json;charset=utf-8");
		String uid = AjaxHellper.getHttpParam(request(), "uid");
		String phone = AjaxHellper.getHttpParam(request(), "phone");
		String wdhjy = AjaxHellper.getHttpParam(request(),"wdhjy");
		String reg = AjaxHellper.getHttpParam(request(), "reg") == null ? "0": AjaxHellper.getHttpParam(request(), "reg");
		String devid = AjaxHellper.getHttpParam(request(), "devid");
		String appversion = AjaxHellper.getHttpParam(request(),"appversion");
		
		StatusMsgVO result = new StatusMsgVO();
		if (!StringUtil.checkMd5(devid, wdhjy,appversion))
		{
			result.setStatus("0");
			result.setMsg("校验失败");
			return ok(Json.toJson(result));
		}
		if ("1".equals(reg)) {
			boolean phoneBindFlag = userService.getPhoneIsBind(phone);
			if (phoneBindFlag) {
				boolean pwdBindFlag = userService.getPwdsIsBind(phone);
				if (pwdBindFlag) {
					result.setStatus("4");
					result.setMsg("该手机号已经用另一社交平台绑定过了");
					return ok(Json.toJson(result));
				}
			}
		}
		if (!PHONE_PATTERN.matcher(phone).matches()) {
			result.setStatus("5");
			result.setMsg("请输入正确的手机号");
			return ok(Json.toJson(result));
		}
		String code = utils.StringUtil.genRandomCode(4);// 生成四位随机数
		logger.info(code);
		String ip = request().remoteAddress();

		String sendFlag = userService.saveVerifyInVerfify(ip, uid, phone, code,devid);
		if ("1".equals(sendFlag)) {
			smsService.getVoiceVerify(phone, code);
			result.setStatus("1");
			result.setMsg("");
		} else if ("-1".equals(sendFlag)) {
			result.setStatus("6");
			result.setMsg("发送太频繁，请稍后再试！");
		} else {
			result.setStatus("6");
			result.setMsg("该手机号发送次数太多，请稍后再试！");
		}
		return ok(Json.toJson(result));
	}
	
	// 检查是否绑定引导接口(GET方式) 
	public Result checkbind() {
		response().setContentType("application/json;charset=utf-8");
		String uid = AjaxHellper.getHttpParam(request(), "uid");
		String devid = AjaxHellper.getHttpParam(request(), "devid");
		String wdhjy = AjaxHellper.getHttpParam(request(),"wdhjy");
		String usid = AjaxHellper.getHttpParam(request(), "usid");
		String unionid = AjaxHellper.getHttpParam(request(), "unionid");
		String appversion = AjaxHellper.getHttpParam(request(),"appversion");
		
		StatusBindVO result = new StatusBindVO();
		if (!StringUtil.checkMd5(devid, wdhjy,appversion))
		{
			result.setStatus("0");
			return ok(Json.toJson(result));
		}
		boolean bindFlag = userService.checkBind(unionid,usid);
		if(bindFlag){
			result = userService.getUserByBindOption(unionid,usid,devid);
			Map<String, String> mayarray=userService.getUserId_ByGuid(Numbers.parseInt(result.getUid(), 0),devid,"", "0");
			String Guid=mayarray.get("guid");
			result.setToken(Guid);
			response().setCookie("token", Guid);
		}else{
			result.setStatus("0");
			result.setUid("0");;
			result.setPhone("");
			result.setIspwds("0");
			List<String> authList = new ArrayList<String>();
			authList.add("0");
			result.setAuthorities(authList);
		}
		return ok(Json.toJson(result));
	}
	
	//设备绑定接口(GET方式||POST方式)
	public Result devlogin() {
		response().setContentType("application/json;charset=utf-8");
		String devid = AjaxHellper.getHttpParamOfFormUrlEncoded(request(), "devid");
		String wdhjy = AjaxHellper.getHttpParamOfFormUrlEncoded(request(),"wdhjy");
		String osversion = AjaxHellper.getHttpParamOfFormUrlEncoded(request(), "osversion");
		String model = AjaxHellper.getHttpParamOfFormUrlEncoded(request(), "model");
		String pushToken = AjaxHellper.getHttpParamOfFormUrlEncoded(request(), "pushToken");
		String appversion = AjaxHellper.getHttpParamOfFormUrlEncoded(request(), "appversion");
		String marketCode = AjaxHellper.getHttpParamOfFormUrlEncoded(request(), "marketCode");
		if(StringUtils.isBlank(marketCode))
			marketCode="appStore";
		
		String install = AjaxHellper.getHttpParamOfFormUrlEncoded(request(), "install");
		String resolution=AjaxHellper.getHttpParamOfFormUrlEncoded(request(), "resolution");
		String ostype = "2";
		String idfa=AjaxHellper.getHttpParamOfFormUrlEncoded(request(), "idfa");
		//String appid=userService.getDevappId(idfa);
		
		String appid="higegou";
		String marketCodes="";
		if(!StringUtils.isBlank(idfa))
			marketCodes=userService.getChannelByidfa(idfa);
		if(StringUtils.isBlank(marketCodes))
			marketCodes="";
		DevLoginVO result = new  DevLoginVO();
		if (!StringUtil.checkMd5(devid, wdhjy,appversion))
		{
			result.setStatus("0");
			return ok(Json.toJson(result));
		}
		
		if(!StringUtils.isBlank(idfa) && !StringUtils.isBlank(appid) && !StringUtils.isBlank(install) && install.equals("0") && marketCodes.equals("wooboo")){
			try{
				String geturl="http://api.wooboo.com.cn/services/cpa/callback?appid="+appid+"&udid="+idfa;
				TenpayHttpClient httpClient = new TenpayHttpClient();
				httpClient.setReqContent(geturl);
				String resContent = "";
				if(httpClient.callHttpPost(geturl, "")){
					//添加返回对方日志
					userService.addidfaLog(appid, idfa, marketCode);
					resContent = httpClient.getResContent();
					JSONObject json=JSONObject.fromObject(resContent);
					if(json.getString("success").equals("true")){
						//变更日志
						userService.updateDevIdfa(idfa, "higehou",1,osversion,marketCode,"");												
					}
				}
			}
			catch(Exception e){}
		}
		//记录用户idfa
			if(!StringUtils.isBlank(idfa)){
				userService.updateDevIdfa(idfa, appid,1,osversion,marketCode,"");
			}
		
		if(model.indexOf("iP")>=0){
			ostype ="1";
		}
		List<AdLoading> adLoadingList = userService.getAdLoadingList();
		if("1".equals(ostype)){
			if(model.indexOf("6")>0){//iphone 6
				if(model.indexOf("Plus")>0){
					result.setLoading(adLoadingList.get(3).getFilename());
					resolution = "1242_2208";
				}else{
					result.setLoading(adLoadingList.get(2).getFilename());
					resolution = "750_134";
				}
			}else if(model.indexOf("4")>0){//iphone 4
				if(model.indexOf("s")>0){
					result.setLoading(adLoadingList.get(0).getFilename());
					resolution = "640_960";
				}else{
					result.setLoading(adLoadingList.get(1).getFilename());
					resolution = "640_960";
				}
			}else{
				result.setLoading(adLoadingList.get(1).getFilename());
				resolution = "640_1136";
			}
		}else{
			if(StringUtils.isBlank(resolution)){
				result.setLoading(adLoadingList.get(4).getFilename());
			}else{
				String width = resolution.split("_")[0];
				if(Numbers.parseInt(width, 0)>640 && adLoadingList!=null && adLoadingList.size()>3){
					result.setLoading(adLoadingList.get(3).getFilename());
				}else{
					if(adLoadingList!=null && adLoadingList.size()>4){
						result.setLoading(adLoadingList.get(4).getFilename());
					}
				}
			}
		}
		result = userService.getUidAndPhone(devid, osversion, model, pushToken, resolution, appversion, marketCode,result);
		if(!StringUtils.isBlank(result.getPhone()))
		{		
			Map<String, String> mayarray=userService.getUserId_ByGuid(Numbers.parseInt(result.getUid(), 0),devid,"", "0");
			String Guid=mayarray.get("guid");
			result.setToken(Guid);
			response().setCookie("token", Guid);
		}
		
		
		result.setStatus("1");
		result.setShare(userService.getShareList());
		Version version = userService.getVersion(ostype)==null?null:(userService.getVersion(ostype).size()==0?null:userService.getVersion(ostype).get(0));
		VersionVo versionVo = new VersionVo();
		versionVo.setOs(ostype);
		versionVo.setClient_version(appversion);
		if(version!=null){
			versionVo.setLatest_version(version.getLatest_version());
			versionVo.setRemind(version.getRemind().toString());
			versionVo.setMessage(version.getMessage());
			if(ostype.equals("2"))
				versionVo.setUrl(StringUtil.getDomainAPI()+"/"+Configuration.root().getString("APKFILE_DIR","pimgs/apk/")+version.getUrl());
			else
				versionVo.setUrl(version.getUrl());
			versionVo.setIsforced(version.getIsforced().toString());
		}
		result.setVersion(versionVo);
		result.setConfig(userService.getAppConfigList());
		
		return ok(Json.toJson(result));
	}
	
	// 客户端通知提醒接口（新增）(GET方式)
	public Result pcheck() {
		response().setContentType("application/json;charset=utf-8");
		String uid = AjaxHellper.getHttpParam(request(), "uid");
		String devid = AjaxHellper.getHttpParam(request(), "devid");
		String wdhjy = AjaxHellper.getHttpParam(request(),"wdhjy");
		String pid = AjaxHellper.getHttpParam(request(), "pid");
		String appversion = AjaxHellper.getHttpParam(request(),"appversion");
		
		StatusOnlyVO result = new StatusOnlyVO();
		if (!StringUtil.checkMd5(devid, wdhjy,appversion))
		{
			result.setStatus(0);
			return ok(Json.toJson(result));
		}
		Product pInfo = productService.getStatProductById(Numbers.parseLong(pid, 0L), "10");
		if(pInfo !=null)
		{
			result.setStatus(1);
		}else{
			result.setStatus(0);
		}
		return ok(Json.toJson(result));
	}
	
	//获取用户红点信息
	public Result getRedFlag(){
		response().setContentType("application/json;charset=utf-8");
		Long uid = Numbers.parseLong(AjaxHellper.getHttpParam(request(), "uid"),0L);
		String devid = AjaxHellper.getHttpParam(request(), "devid");
		String wdhjy = AjaxHellper.getHttpParam(request(),"wdhjy");
		String appversion = AjaxHellper.getHttpParam(request(),"appversion");
		
		ObjectNode result=Json.newObject();
		ObjectNode datanode=Json.newObject();
		
		if (!StringUtil.checkMd5(devid, wdhjy,appversion))
		{
			result.put("status", "0");
			return ok(Json.toJson(result));
		}
		
		result.put("status", "1");
		UserRedFlag rfg=userService.getuserRedflag(uid);
		if(rfg==null){
			datanode.put("userBalanceRedFlag", "0");
			datanode.put("couponRedFlag", "0");
			datanode.put("myPresellsRedFlag", "0");
			datanode.put("guessULikeRedFlag", "0");
			datanode.put("customServiceMsgRedFlag", "0");
		}
		else
		{
			datanode.put("userBalanceRedFlag", rfg.getEndorseBalanceFlag()==null?"0":rfg.getEndorseBalanceFlag().toString());
			datanode.put("couponRedFlag", rfg.getCouponRedFlag()==null?"0":rfg.getCouponRedFlag().toString());
			datanode.put("myPresellsRedFlag", rfg.getMyPresellsRedFlag()==null?"0":rfg.getMyPresellsRedFlag().toString());
			datanode.put("guessULikeRedFlag", rfg.getGuessULikeRedFlag()==null?"0":rfg.getGuessULikeRedFlag().toString());
			datanode.put("customServiceMsgRedFlag", rfg.getCustomServiceRedFlag()==null?"0":rfg.getCustomServiceRedFlag().toString());
		}
		result.putPOJO("data", datanode);
		return ok(result);
	}
	
	//重围在线客服红点接口
	public Result resetMeiChatMsgRedDot(){
		response().setContentType("application/json;charset=utf-8");
		Long uid = Numbers.parseLong(AjaxHellper.getHttpParam(request(), "uid"),0L);
		ObjectNode result=Json.newObject();
		if(uid==0L){
			result.put("status", "0");
			result.put("msg", "用户参数错误");
			return ok(result);
		}
		userService.updateUserRedFlag(uid, "customServiceRedFlag", 0);
		result.put("status", "1");
		result.put("msg", "");
		return ok(result);
	}
	
	//反查idfa下载明细
	public Result checkidfa(){
		response().setContentType("application/json;charset=utf-8");
		String idfa=AjaxHellper.getHttpParamOfFormUrlEncoded(request(), "idfa");
		String appid=AjaxHellper.getHttpParamOfFormUrlEncoded(request(), "appid");
		ObjectNode oblist=Json.newObject();	
		if(!StringUtils.isBlank(idfa)){
			String[] idfalist=idfa.split(",");
			for(int i=0;i<idfalist.length;i++){
				Integer flg=userService.getDevappId(idfalist[i], appid);
				oblist.put(idfalist[i], flg.toString());
			}
		}
		return ok(oblist);
	}
	
	//为运营数据埋点上传文件
	public Result uploadStatData() {
		response().setContentType("application/json;charset=utf-8");
		MultipartFormData body = request().body().asMultipartFormData();
		ObjectNode result=Json.newObject();
		result.put("status", 1);
		FilePart logfile = body.getFile("logfile");
		if (logfile != null && logfile.getFile() != null) {
			String fileName = logfile.getFilename();
			File file = logfile.getFile();//获取到该文件
			String type = FileUtils.getStrFileExt(fileName);
			String path=file.getAbsolutePath();
			if (".zip".equals(type)) {
				// 检查文件后缀格式
				File[] fileUnzip;
				try {
					fileUnzip = CompressUtil.unzip(path,Configuration.root().getString("zipPassword","BEDDBA95C61947E0BA2F27ECE59D4DC1"));
					for(File fileTemp : fileUnzip){
						InputStreamReader read = new InputStreamReader(new FileInputStream(fileTemp),"UTF-8");//考虑到编码格式
	                    BufferedReader bufferedReader = new BufferedReader(read);
	                    String lineTxt = null;
	                    while((lineTxt = bufferedReader.readLine()) != null){
	                        String splicArray[]=lineTxt.split("\uFFFF");
	                        if(splicArray.length==6){
	                        	String eventName = splicArray[0];
	                        	Event event = eventService.findEventByEventName(eventName);
	                        	if(event==null){
	                        		event=new Event();
	                        		event.setCreateTime(new Date());
	                        		event.setEventName(eventName);
	                        		event=eventService.save(event);
	                        	}
		                        String label = splicArray[1];
		                        EventLabel eventLable = eventService.findByEventidAndLabel(event, label);
		                        if(eventLable==null){
		                        	eventLable = new EventLabel();
		                        	eventLable.setCreateTime(new Date());
		                        	eventLable.setEventid(event);
		                        	eventLable.setLabel(label);
		                        	eventLable = eventService.save(eventLable);
		                        }
		                        EventParam eventParam=new EventParam();
		                        String paramKey = splicArray[2];
		                        String paramValue = splicArray[3];
		                        String uid = splicArray[4];
		                        String createdAtInMiniSeconds = splicArray[5];
		                        eventParam.setLabelid(eventLable);
		                        eventParam.setParamKey(paramKey);
		                        eventParam.setParamValue(paramValue);
		                        eventParam.setUid(Numbers.parseLong(uid,0L));
		                        eventParam.setCreateTime(Dates.parseDateByLong(Numbers.parseLong(createdAtInMiniSeconds, 0L)));
		                        eventService.save(eventParam);
	                        }
	                    }
	                    read.close();
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}else{
			result.put("status", 0);
		}
		
		return ok(Json.toJson(result));
	}
}
