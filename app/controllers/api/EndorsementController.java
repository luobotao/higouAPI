package controllers.api;

import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.net.URLDecoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.imageio.ImageIO;
import javax.imageio.stream.MemoryCacheImageInputStream;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import models.BalanceOperLog;
import models.EndoresementOpLog;
import models.Endorsement;
import models.EndorsementContent;
import models.EndorsementImg;
import models.EndorsementPraise;
import models.EndorsementReport;
import models.Product;
import models.Reffer;
import models.User;
import models.UserBalance;
import models.UserBalanceLog;
import net.sf.json.JSONObject;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.springframework.data.domain.Page;

import play.Configuration;
import play.Logger;
import play.data.Form;
import play.libs.Json;
import play.mvc.Http.MultipartFormData;
import play.mvc.Http.MultipartFormData.FilePart;
import play.mvc.Result;
import services.api.AddressService;
import services.api.EndorsementService;
import services.api.ProductService;
import services.api.RefferService;
import services.api.UserService;
import utils.AjaxHellper;
import utils.Constants;
import utils.Numbers;
import utils.OSSUtils;
import utils.StringUtil;
import vo.UserRegisterVO;
import vo.endorsment.EndorsePaylogVO;
import vo.endorsment.EndorsementProductVO;
import vo.endorsment.EndorsmentVO;
import vo.endorsment.EndorsmentVO.EnorsmentVOItem;
import vo.endorsment.EndorsmentVO.userLikeImg;
import vo.endorsment.EnorsmentDetailVO;
import vo.endorsment.EnorsmentDetailVO.EnorsmentDetailVOItem;
import vo.endorsment.UserBalanceLogVO;
import vo.endorsment.UserBalanceLogVO.UserBalanceLogItem;
import assets.CdnAssets;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;


@Named
@Singleton
public class EndorsementController extends BaseApiController {
	private static final SimpleDateFormat CHINESE_DATE_TIME_FORMAT = new SimpleDateFormat(
			"yyyy-MM-dd HH:mm:ss");
	private static final SimpleDateFormat CHINESE_DATE_MONTH = new SimpleDateFormat("yyyyMM");
	private static final Logger.ALogger logger = Logger
			.of(EndorsementController.class);

	//测试环境上传地址
	private static String path_test = File.separator+"data"+File.separator+"www"+File.separator+"higouAPI"+File.separator+"public";
	
	//生产环境上传地址
	private static String path_online = File.separator+"data"+File.separator+"higouapi"+File.separator+"higouAPI"+File.separator+"public";
	
	
	private final EndorsementService endorsementService;
	private final UserService userService;
	private final ProductService productService;
	private final AddressService addressService;
	private final RefferService refferService;
	@Inject
	public EndorsementController(final EndorsementService enmserv,
			UserService userService, ProductService productService,AddressService addressService,final RefferService refferService) {
		this.endorsementService = enmserv;
		this.userService = userService;
		this.productService = productService;
		this.addressService=addressService;
		this.refferService=refferService;
	}

	// 代言列表接口
	public Result getEndorsementlist() {
		response().setContentType("application/json;charset=utf-8");
		String uid = AjaxHellper.getHttpParam(request(), "uid");
		String devid = AjaxHellper.getHttpParam(request(), "devid");
		String wdhjy = AjaxHellper.getHttpParam(request(),"wdhjy");
		String appversion = AjaxHellper.getHttpParam(request(), "appversion");
		String reffer=AjaxHellper.getHttpParam(request(), "ref");
		reffer=StringUtils.isBlank(reffer)?"":reffer;
		String toUid = uid;
		final Integer isall = Numbers.parseInt(AjaxHellper.getHttpParam(request(), "isall"),0);
		final Integer status = 1;

		String pg = AjaxHellper.getHttpParam(request(), "page");
		EndorsmentVO result = new EndorsmentVO();
		if (!StringUtil.checkMd5(devid, wdhjy,appversion))
		{
			result.status = "0";
			return ok(Json.toJson(result));
		}
		
		if (StringUtils.isBlank(uid)) {
			result.status = "0";
			return ok(Json.toJson(result));
		}

		
		if (StringUtils.isBlank(pg)) {
			pg = "0";
		}
		result.reffer="Typ="+Constants.MAIDIAN_DAIYAN;
		
		Integer page=Numbers.parseInt(pg, 0);
//		if(page>=1)
//			page=page-1;
		/*
		 * 埋点
		 */
		Reffer ref=new Reffer();
		ref.setIp(request().remoteAddress());
		ref.setRefer(reffer);
		ref.setTyp(Constants.MAIDIAN_DAIYAN);
		ref.setTid(0L);
		refferService.addReffer(ref, "reffer"+CHINESE_DATE_MONTH.format(new Date()));

		String domains=StringUtil.getOSSUrl();
		Page<Endorsement> emlist = endorsementService.getEndorsmentlist(
				Long.valueOf(toUid), status, page,isall);
		
		String domainimg=CdnAssets.CDN_API_PUBLIC_URL;

		result.status = "1";
		Endorsement systemendorse=null;
		if(page==0 && isall==1){
			//取出系统宣传代言记录
				List<Endorsement> systemendorslist=endorsementService.getEndorseByStatus(99);		
				if(systemendorslist!=null && !systemendorslist.isEmpty())
					systemendorse=systemendorslist.get(0);
			}
		
		if (emlist != null && emlist.getContent() != null) {
			List<Endorsement> tmplist = emlist.getContent();
			List<Endorsement> tempenodrselist=new ArrayList<Endorsement>();
			
			if(systemendorse!=null ){
				int i=0;
				for (Endorsement et : tmplist) {
					if(i==1)
						tempenodrselist.add(systemendorse);
					tempenodrselist.add(et);
					i++;
				}
			}
			if(tempenodrselist==null || tempenodrselist.isEmpty())
				tempenodrselist=tmplist;
			
			List<EnorsmentVOItem> itemlist = new ArrayList<EnorsmentVOItem>();
			for (Endorsement et : tempenodrselist) {
				EnorsmentVOItem itm = new EnorsmentVOItem();
				itm.eid = ""+et.getEid();
				itm.count = StringUtil.formatnum(Long.valueOf(et.getCount()));
				
				itm.createTime =StringUtil.getfomatdate(et.getCreateTime(),new Date());
				
				itm.remark = et.getRemark()==null?"":et.getRemark();
				itm.userId = ""+et.getUserId();
				if(et.getStatus()==99){
					itm.linkURL=StringUtil.getDomainH5()+"/sheSaid/en_notes";
					itm.endorBadgeUrl="";
					itm.endorTagImgUrl=domainimg+"images/sheSaidImages/bordnote.jpg";
					itm.userNickName="嗨个购小嗨";
					itm.userHeadIcon=domainimg+"images/sheSaidImages/bordnoteicon.jpg";
					itm.preImgPath=domainimg+"images/sheSaidImages/bordnotepre.jpg";
				}
				else{
					String preimg = et.getPreImgPath()==null?"":et.getPreImgPath();
					if (!preimg.isEmpty()){
						itm.preImgPath = domains+preimg;
					}else{
						itm.preImgPath = ""; 	
					}
					itm.linkURL = "endorsementDetail://eid="+et.getEid();
					itm.endorBadgeUrl=StringUtils.isBlank(et.getBannerimg())?domainimg+StringUtil.getSheSaidIcon():domainimg+et.getBannerimg();
					itm.endorTagImgUrl=StringUtil.getSheSaidTagImg();
					User uInfo = userService.getUserByUid(et.getUserId());
					itm.userNickName = uInfo.getNickname()==null?"":uInfo.getNickname();
					itm.userHeadIcon = uInfo.getHeadIcon()==null?"":uInfo.getHeadIcon();
					if(itm.userHeadIcon==null || itm.userHeadIcon.equals("")){
						if(StringUtils.isBlank(uInfo.getSex()) || uInfo.getSex().equals("0"))
							itm.userHeadIcon=domainimg+"images/sheSaidImages/default_headicon_girl.png";
						else
							itm.userHeadIcon=domainimg+"images/sheSaidImages/default_headicon_boy.png";
					}
					itm.endorTagImgUrl=StringUtil.getSheSaidTagImg();
				}
				
				itm.isLiked=endorsementService.getendorsmentIsPraise(et.getEid(), Numbers.parseLong(uid, 0L))?"1":"0";
				
				List<EndorsementPraise> praiseList = endorsementService
						.getPraiselist(et.getEid(),11);
				if (praiseList != null && !praiseList.isEmpty()){
					itm.headImglist = new ArrayList<EndorsmentVO.userLikeImg>();
					for (EndorsementPraise p : praiseList) {
						EndorsmentVO.userLikeImg uImg = new userLikeImg();
						uImg.userId = p.getUserId().toString();
						if(!StringUtils.isBlank(p.getImgPath()))
							uImg.headIcon = p.getImgPath();
						else
							uImg.headIcon="";
						if(uImg.headIcon.equals("")){
							if(StringUtils.isBlank(p.getSex()))
								uImg.headIcon=domainimg+"images/sheSaidImages/default_headicon_girl.png";
							else{
								if(p.getSex().equals("0"))
									uImg.headIcon=domainimg+"images/sheSaidImages/default_headicon_girl.png";
								else
									uImg.headIcon=domainimg+"images/sheSaidImages/default_headicon_boy.png";
							}
						}
						itm.headImglist.add(uImg);
					}
				}
				else
				{
					itm.headImglist=new ArrayList<userLikeImg>();
				}
				itemlist.add(itm);
			}
			result.data = itemlist;

			return ok(Json.toJson(result));
		} else {
			result.status = "0";
			result.data = new ArrayList<EnorsmentVOItem>();
		}
		// }
		return ok(Json.toJson(result));
	}

	// 代言点赞接口
	public Result endorsementPraise() {
		response().setContentType("application/json;charset=utf-8");
		String status = "1";
		Long emid = Numbers.parseLong(
				AjaxHellper.getHttpParam(request(), "daiyanId"),
				Long.valueOf(0));
		Long userid = Numbers.parseLong(
				AjaxHellper.getHttpParam(request(), "uid"), Long.valueOf(0));
		String devid = AjaxHellper.getHttpParam(request(), "devid");
		String wdhjy = AjaxHellper.getHttpParam(request(),"wdhjy");
		String appversion = AjaxHellper.getHttpParam(request(), "appversion");
		ObjectNode result = Json.newObject();
		
		if (!StringUtil.checkMd5(devid, wdhjy,appversion))
		{
			result.put("status", "0");
			return ok(Json.toJson(result));
		}
		
		Boolean sucs = endorsementService.endorsmentPraise(emid, userid);
		try {
			Thread.sleep(300);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		String isLiked="1";
		Endorsement emInfo= endorsementService.getEndorseDetail(emid,11);
		//status = sucs == true ? "1" : "0";
		
		result.put("status", status);
		result.put("isLiked",isLiked);
		result.put("count",emInfo==null?"0":""+emInfo.getCount());
		List<EnorsmentDetailVO.userLikeImg> headImglist = new ArrayList<EnorsmentDetailVO.userLikeImg>();
		if(emInfo.getEndorsPraiseList()!=null && !emInfo.getEndorsPraiseList().isEmpty()){
			for (EndorsementPraise p : emInfo.getEndorsPraiseList()) {
					EnorsmentDetailVO.userLikeImg uImg = new EnorsmentDetailVO.userLikeImg();
					uImg.userId = p.getUserId().toString();
					if(!StringUtils.isBlank(p.getImgPath()) &&  p.getImgPath()!= null)
						uImg.headIcon = p.getImgPath();
					else
						uImg.headIcon="";
					headImglist.add(uImg);
			}
		}
		else
			headImglist=new ArrayList<EnorsmentDetailVO.userLikeImg>();
		JsonNode objimg=Json.toJson(headImglist);
		
		result.putPOJO("headImglist", objimg);
		
		return ok(Json.toJson(result));
	}

	// 取消代言点赞
	public Result unendorsPraise() {
		response().setContentType("application/json;charset=utf-8");
		String devid = AjaxHellper.getHttpParam(request(), "devid");
		String wdhjy = AjaxHellper.getHttpParam(request(),"wdhjy");
		String appversion = AjaxHellper.getHttpParam(request(), "appversion");
		String status = "1";
		Long emid = Numbers.parseLong(
				AjaxHellper.getHttpParam(request(), "daiyanId"),
				Long.valueOf(0));
		Long userid = Numbers.parseLong(
				AjaxHellper.getHttpParam(request(), "uid"), Long.valueOf(0));
		ObjectNode result = Json.newObject();
		
		if (!StringUtil.checkMd5(devid, wdhjy,appversion))
		{
			result.put("status", "0");
			return ok(Json.toJson(result));
		}
		endorsementService.unendorspraise(emid, userid);
		try {
			Thread.sleep(300);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		String isLiked="0";
		Endorsement emInfo= endorsementService.getEndorseDetail(emid,11);
		
		
		result.put("status", status);
		result.put("isLiked",isLiked);
		result.put("count",""+emInfo.getCount());
		List<EnorsmentDetailVO.userLikeImg> headImglist = new ArrayList<EnorsmentDetailVO.userLikeImg>();
		if(emInfo.getEndorsPraiseList()!=null && !emInfo.getEndorsPraiseList().isEmpty()){
			for (EndorsementPraise p : emInfo.getEndorsPraiseList()) {
					EnorsmentDetailVO.userLikeImg uImg = new EnorsmentDetailVO.userLikeImg();
					uImg.userId = p.getUserId().toString();
					if(!StringUtils.isBlank(p.getImgPath()) &&  p.getImgPath()!= null)
						uImg.headIcon = p.getImgPath();
					else
						uImg.headIcon="";
					headImglist.add(uImg);
			}
		}
		else
			headImglist=new ArrayList<EnorsmentDetailVO.userLikeImg>();
		JsonNode objimg=Json.toJson(headImglist);
		
		result.putPOJO("headImglist", objimg);
		return ok(Json.toJson(result));
	}

	// 获取用户是否代言接口
	public Result isEndorsementUser() {
		response().setContentType("application/json;charset=utf-8");
		Long userid = Numbers.parseLong(
				AjaxHellper.getHttpParam(request(), "uid"), Long.valueOf(0));
		String devid = AjaxHellper.getHttpParam(request(), "devid");
		String wdhjy = AjaxHellper.getHttpParam(request(),"wdhjy");
		String appversion = AjaxHellper.getHttpParam(request(), "appversion");
		ObjectNode result = Json.newObject();
		if (!StringUtil.checkMd5(devid, wdhjy,appversion))
		{
			result.put("status", "0");
			return ok(Json.toJson(result));
		}
		
		User us = userService.getUserByUid(userid);
	
		if (us == null) {
			result.put("status", "0");
			result.put("daiyanURL", Constants.ENDORSEMENT_URL);
			result.put("applyURL", Constants.ENDORSEMENT_QUEST_URL);
		} else {
			if (us.getIsEndorsement() == 0) {
				result.put("status", "0");
				result.put("daiyanURL", Constants.ENDORSEMENT_URL);
				result.put("applyURL", Constants.ENDORSEMENT_QUEST_URL);
			} else {
				result.put("status", "1");
				result.put("daiyanURL", "");
				result.put("applyURL", "");
			}

		}
		return ok(Json.toJson(result));
	}

	// 代言暗号检测接口
	public Result checkEndorsementCode() {
		ObjectNode result= Json.newObject();
		String status = "1";
		List<String> authorities=new ArrayList<String>();
		authorities.add("0");
		
		response().setContentType("application/json;charset=utf-8");
		Long userid = Numbers.parseLong(
				AjaxHellper.getHttpParam(request(), "uid"), Long.valueOf(0));
		String DaiyanCode = AjaxHellper.getHttpParam(request(), "daiyanCode");
		String devid = AjaxHellper.getHttpParam(request(), "devid");
		String wdhjy = AjaxHellper.getHttpParam(request(),"wdhjy");
		String appversion = AjaxHellper.getHttpParam(request(), "appversion");
		if (!StringUtil.checkMd5(devid, wdhjy,appversion))
		{
			result.put("status", "0");
			return ok(Json.toJson(result));
		}
		
		if (userid.equals(0) || StringUtils.isBlank(DaiyanCode)) {
			status = "0";
		} else {
			// 验证暗号表，修改用户表--
			boolean iscode = endorsementService.isEndorementcode(DaiyanCode);
			if (!iscode)
				status = "0";
			else {
				// 修改用户代言
				userService.EditEndorsementCode(userid, DaiyanCode);
				//修改暗号表被占用
				userService.EditEndorseCode(userid, DaiyanCode);
				status = "1";
				authorities.add("1");
				result.putPOJO("authorities", authorities);
			}
		}
		result.put("status", status);
		
		return ok(Json.toJson(result));
	}

	// 代言抢商品接口（检查是否可以代言）
	public Result checkEndorseproduct() {
		response().setContentType("application/json;charset=utf-8");
		Long pid = Numbers.parseLong(
				AjaxHellper.getHttpParam(request(), "pid"), Long.valueOf(0));
		Long uid = Numbers.parseLong(
				AjaxHellper.getHttpParam(request(), "uid"), Long.valueOf(0));
		String devid = AjaxHellper.getHttpParam(request(), "devid");
		String wdhjy = AjaxHellper.getHttpParam(request(),"wdhjy");
		String appversion = AjaxHellper.getHttpParam(request(), "appversion");
		EndorsementProductVO result = new EndorsementProductVO();
		if (!StringUtil.checkMd5(devid, wdhjy,appversion))
		{
			result.status = "0";
			result.DaiyanCnt = "";
			result.msg="校验失败";
			return ok(Json.toJson(result));
		}
		
		String resContent="";
		EndorsementContent content = endorsementService.getRanContent();
		if (content != null) {
			resContent=content.getRemark();
		}
		
		if (pid == 0) {
			result.status = "0";
			result.DaiyanCnt = "";
			result.msg="参数错误";
			return ok(Json.toJson(result));
		}
		Product pro = productService.getProductById(pid);

		User user=userService.getUserByUid(uid);
		if(user==null || user.getIsEndorsement()!=1){
			result.status="0";
			result.IsFull="0";
			result.DaiyanCnt="0";
			result.remark= resContent;
			result.shareType= user.getShareType();
			result.msg="非法操作";
			return ok(Json.toJson(result));
		}
		
		boolean hasendorse=false;
		List<Endorsement> endorse=endorsementService.getEndorsementInfo(uid, pid);
		if(endorse!=null){
			for(Endorsement en:endorse){
				if(en.getStatus()==0 || en.getStatus()==1)
					hasendorse=true;
			}
		}
		if(hasendorse){
			result.status="0";
			result.IsFull="0";
			result.DaiyanCnt="0";
			result.remark= resContent;
			result.shareType= user.getShareType();
			result.msg="已代言该商品，不能重复代言";
			return ok(Json.toJson(result));
		}
		result.status = "0";
		if (pro == null) {
			result.status = "0";
			result.msg="商品不存在";
		} else {
			if (pro.getIsEndorsement() == 1) {
				if (pro.getMaxEndorsementCount() <= pro.getEndorsementCount()) {
					result.status = "0";
					result.IsFull = "1";
					result.DaiyanCnt = "";
					result.remark= resContent;
					result.shareType= user.getShareType();
					result.msg="代言数量已到上限";
					return ok(Json.toJson(result));
				}
				
				// 代言商品代言数量加1，暂时锁定
				pro.setEndorsementCount(pro.getEndorsementCount() + 1);
				pro = productService.saveProduct(pro);
				//添加抢日志
				EndoresementOpLog optlog=new EndoresementOpLog();
				optlog.setCreateTime(new Date());
				optlog.setEid(0L);
				optlog.setPid(pid);
				optlog.setUserId(uid);
				endorsementService.addEndorseOptLog(optlog);
				
				result.status = "1";
				result.IsFull = "0";
				result.DaiyanCnt = String.valueOf(pro
						.getEndorsementCount());
				result.remark= resContent;
				result.shareType= user.getShareType();
				result.msg="";
			}
		}
		return ok(Json.toJson(result));
	}

	// 获取随机文案接口
	public Result getRoundContent() {
		response().setContentType("application/json;charset=utf-8");
		String devid = AjaxHellper.getHttpParam(request(), "devid");
		String wdhjy = AjaxHellper.getHttpParam(request(),"wdhjy");
		String appversion = AjaxHellper.getHttpParam(request(), "appversion");
		ObjectNode result = Json.newObject();
		if (!StringUtil.checkMd5(devid, wdhjy,appversion))
		{
			result.put("status", "0");
			result.put("toast", "校验失败");
			return ok(Json.toJson(result));
		}
		
		EndorsementContent content = endorsementService.getRanContent();
		if (content != null || !StringUtils.isBlank(content.getRemark())) {
			result.put("status", "1");
			result.put("toast", content.getRemark());
			return ok(Json.toJson(result));
		}
		result.put("status", "0");
		result.put("toast", "");
		return ok(Json.toJson(result));
	}

	// 发表代言接口（POST方式)-待定
	public Result uploadfile() throws UnsupportedEncodingException {
		response().setContentType("application/json;charset=utf-8");
		ObjectNode result = Json.newObject();
		Long pid = Numbers.parseLong(
				Form.form().bindFromRequest().get("pid"), Long.valueOf(0));
		Long uid = Numbers.parseLong(
				Form.form().bindFromRequest().get("uid"), Long.valueOf(0));
		Integer picnums = Numbers.parseInt(
				Form.form().bindFromRequest().get("nums"), 0);
		String content = Form.form().bindFromRequest().get("content");
		String devid = Form.form().bindFromRequest().get("devid");
		String wdhjy = Form.form().bindFromRequest().get("wdhjy");
		String appversion = Form.form().bindFromRequest().get("appversion");
		if (!StringUtil.checkMd5(devid, wdhjy,appversion))
		{
			result.put("status", "0");
			result.put("msg", "校验失败");
			return ok(Json.toJson(result));
		}
		
//		try{
//			if(!StringUtils.isBlank(content))
//				content=new String(content.getBytes("utf-8") , "gbk");
//		}
//		catch(Exception e){}
		if (pid == 0 || uid == 0) {
			result.put("status", "0");
			result.put("daiyanId", "0");
			result.put("daiyanURL", "");
			result.put("msg", "参数不正确");
			return ok(Json.toJson(result));
		}

		Product pro=productService.getProductById(pid);
		if(pro==null){
			result.put("status", "0");
			result.put("daiyanId", "0");
			result.put("daiyanURL", "");
			result.put("msg", "产品不存在");
			return ok(Json.toJson(result));
		}
		if (pro.getIsEndorsement() != 1) {
			result.put("status", "0");
			result.put("daiyanId", "0");
			result.put("daiyanURL", "");
			result.put("msg", "该产品不是代言产品");
			return ok(Json.toJson(result));
		}
			if (pro.getMaxEndorsementCount() <= pro.getEndorsementCount()) {
				result.put("status", "0");
				result.put("daiyanId", "0");
				result.put("daiyanURL", "");
				result.put("msg", "产品代言数量已满");
				return ok(Json.toJson(result));
			}

//		// 代言商品代言数量加1；
//		pro.setEndorsementCount(pro.getEndorsementCount() + 1);
//		pro = productService.saveProduct(pro);
		//发表代言，向代言表里加数据
		Endorsement endorsment = new Endorsement();
		endorsment.setCount(0);
		endorsment.setCreateTime(new Date());
		endorsment.setPreImgPath("");
		endorsment.setProductId(pro.getPid());
		endorsment.setUserId(uid);
		endorsment.setRemark(content);
		endorsment.setStatus(0);
		endorsment = endorsementService
				.saveEndorsement(endorsment);
		if (endorsment != null){
			endorsementService.upodateEndorse(content, endorsment.getEid(), picnums);
			result.put("daiyanId", endorsment.getEid());
			result.put("daiyanURL", Constants.ENDORSEMENT_URL+endorsment.getEid());
			result.put("msg", "发布成功");
		}
		else{
			result.put("status", "0");
			result.put("daiyanId", "0");
			result.put("daiyanURL", "");
			result.put("msg", "发布失败");
		}
		
		return ok(Json.toJson(result));
	}

	// 代言详情接口
	public Result endorseDetail() {
		response().setContentType("application/json;charset=utf-8");
		Long eid = Numbers.parseLong(
				AjaxHellper.getHttpParam(request(), "daiyanId"),
				Long.valueOf(0));
		Long uid = Numbers.parseLong(
				AjaxHellper.getHttpParam(request(), "uid"),
				Long.valueOf(0));
		String devid = AjaxHellper.getHttpParam(request(), "devid")==null?"":AjaxHellper.getHttpParam(request(), "devid");
		String wdhjy = AjaxHellper.getHttpParam(request(),"wdhjy");
		String appversion = AjaxHellper.getHttpParam(request(), "appversion");
		String reffer=AjaxHellper.getHttpParam(request(), "ref");
		reffer=StringUtils.isBlank(reffer)?"":reffer;
		EnorsmentDetailVO result = new EnorsmentDetailVO();
		result.status = "0";
		if (!StringUtil.checkMd5(devid, wdhjy,appversion))
		{
			result.status = "0";
			result.data = new EnorsmentDetailVOItem();
			return ok(Json.toJson(result));
		}
		
		
		if (eid == 0) {
			result.data = new EnorsmentDetailVOItem();
			return ok(Json.toJson(result));
		}
//		User usr=userService.getUserByUid(uid);
//		if(usr==null){
//			result.data=new EnorsmentDetailVOItem();
//			return ok(Json.toJson(result));
//		}
		/*
		 * 埋点
		 */
		Reffer ref=new Reffer();
		ref.setIp(request().remoteAddress());
		ref.setRefer(reffer);
		ref.setTyp(Constants.MAIDIAN_DAIYAN);
		ref.setTid(eid);
		refferService.addReffer(ref, "reffer"+CHINESE_DATE_MONTH.format(new Date()));

		String domains=StringUtil.getOSSUrl();
		Endorsement endors = endorsementService.getEndorseDetail(eid,11);
		if (endors != null) {
			User uInfo = userService.getUserByUid(endors.getUserId());
			User uInfo1 = userService.getUserByUid(uid);
			result.status = "1";
			Date da=endors.getCreateTime();
			if(da==null)
				da=new Date();
			result.data.createTime =StringUtil.getfomatdate(da, new Date());//
			result.data.eid = eid;
			result.data.imglist = endors.getEndorsImgList();
			result.data.productId = endors.getProductId();
			result.data.userId = endors.getUserId();
			result.data.remark = endors.getRemark();
			result.data.status = endors.getStatus();
			result.data.shareDescription=StringUtil.getSheSaidremark();
			result.data.shareTitle=StringUtil.getShesaidTitle(uInfo.getNickname());
			if(!endors.getPreImgPath().isEmpty())
			{
				result.data.preImgPath = domains+endors.getPreImgPath();
			}else{
				result.data.preImgPath ="";
			}	
			result.data.picnums = endors.getPicnums();
			result.data.reffer="Typ="+Constants.MAIDIAN_DAIYAN+"&eid="+eid;
			//result.data.H5pushURL = Constants.ENDORSEMENT_URL+eid.toString();
			//转换短链接
			String daiyanURL=Constants.ENDORSEMENT_URL+endors.getEid()+"&shareType="+uInfo1.getShareType();
			if(!StringUtils.isBlank(uInfo1.getShareType()) && !uInfo1.getShareType().equals("1")){
				try{
					String sortturnurl="http://api.t.sina.com.cn/short_url/shorten.json?source="+Constants.APP_KEY+"&url_long="+daiyanURL+"&callback=jsonpshorturl123";
					// 发送请求，返回json
					HttpClient clientc = new DefaultHttpClient();
					HttpGet get = new HttpGet(sortturnurl);
					
					String resContenti = "";
					HttpResponse resc = clientc.execute(get);
					if (resc.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
						HttpEntity entity = resc.getEntity();
	
						resContenti = EntityUtils.toString(resc.getEntity(),"UTF-8");
						// System.out.println(strResult);
						Logger.info("调用新浪转换短链接接口返回报文内容:" + resContenti);	
						if(!StringUtils.isBlank(resContenti)){
							resContenti=resContenti.replace("[", "").replace("]", "");
						}
						JSONObject jsons = JSONObject.fromObject(resContenti);
						if(!StringUtils.isBlank(jsons.getString("url_short")))
							daiyanURL=jsons.getString("url_short");
					}
				}
				catch(Exception e){
					Logger.error("转换短链接失败："+e.toString());
				}
			}
			result.data.H5pushURL=daiyanURL;
			
			result.data.userNickName = uInfo.getNickname();
			if(StringUtils.isBlank(uInfo.getHeadIcon())){
				result.data.userHeadIcon=CdnAssets.CDN_API_PUBLIC_URL+"images/sheSaidImages/default_headicon_boy.png";
				if(StringUtils.isBlank(uInfo.getSex()) || uInfo.getSex().equals("0"))
					result.data.userHeadIcon=CdnAssets.CDN_API_PUBLIC_URL+"images/sheSaidImages/default_headicon_girl.png";
			}
			else
				result.data.userHeadIcon = uInfo.getHeadIcon();
			
			String deviceType = AjaxHellper.getHttpParam(request(), "deviceType")==null?"0":AjaxHellper.getHttpParam(request(), "deviceType");
			String resolution = UserService.getResolution(String.valueOf(uid),devid);
			result.data.endorBadgeUrl =StringUtil.getCoverPic(deviceType, resolution);
			result.data.shareType = uInfo1.getShareType();
			
			String isLiked="0";
			if(uid>0L){
				isLiked=endorsementService.getendorsmentIsPraise(eid, uid)?"1":"0";
			}
			result.data.isLiked  = isLiked;
			if(StringUtils.isBlank(uInfo.getHeadIcon())){
				result.data.badgeHeadUrl=CdnAssets.CDN_API_PUBLIC_URL+"images/sheSaidImages/default_headicon_boy.png";
				if(StringUtils.isBlank(uInfo.getSex()) || uInfo.getSex().equals("0"))
					result.data.badgeHeadUrl=CdnAssets.CDN_API_PUBLIC_URL+"images/sheSaidImages/default_headicon_girl.png";
			}
			else
				result.data.badgeHeadUrl = uInfo.getHeadIcon();
			
			result.data.badgeUserName = uInfo.getNickname();
			List<EndorsementPraise> pimglist=endorsementService.getPraiselist(eid,10000);
			
//			result.data.praiseNums = pimglist == null
//					|| pimglist.isEmpty() ? 0 : pimglist.size();
			result.data.praiseNums=endors.getCount();
			if (endors.getEndorsPraiseList() != null
					&& !endors.getEndorsPraiseList().isEmpty()) {
				
			}
			result.data.headImglist = new ArrayList<EnorsmentDetailVO.userLikeImg>();
			if(endors.getEndorsPraiseList()!=null && !endors.getEndorsPraiseList().isEmpty()){
				for (EndorsementPraise p : endors.getEndorsPraiseList()) {
						EnorsmentDetailVO.userLikeImg uImg = new EnorsmentDetailVO.userLikeImg();
						uImg.userId = p.getUserId().toString();
						if(!StringUtils.isBlank(p.getImgPath()) &&  p.getImgPath()!= null)
							uImg.headIcon = p.getImgPath();
						else
							uImg.headIcon="";
						result.data.headImglist.add(uImg);
				}
			}
			else
				result.data.headImglist=new ArrayList<EnorsmentDetailVO.userLikeImg>();
			result.data.pinfo=productService.getProductEndorsementInfo(endors.getProductId());
			if(result.data.pinfo!=null)
				result.data.pinfo.linkUrl=result.data.pinfo.linkUrl+"&eid="+eid;
		}
		return ok(Json.toJson(result));
	}

	// 修改个人信息接口
	public Result editUserInfo() {
		response().setContentType("application/json;charset=utf-8");
		MultipartFormData body = request().body().asMultipartFormData();
		FilePart sfFile = body.getFile("headimg");
		
		Long uid = Numbers.parseLong(Form.form().bindFromRequest().get("uid"), Long.valueOf(0));
		
		String Headimg = "";
		String Nickname = Form.form().bindFromRequest().get("nickname");
		String sex = Form.form().bindFromRequest().get("sex");
		String devid = Form.form().bindFromRequest().get("devid");
		String wdhjy = Form.form().bindFromRequest().get("wdhjy");
		String appversion = Form.form().bindFromRequest().get("appversion");
		UserRegisterVO result = new UserRegisterVO();
		if (!StringUtil.checkMd5(devid, wdhjy,appversion))
		{
			result.setStatus("0");
			return ok(Json.toJson(result));
		}
		
		
		if (uid == 0) {
			result.setStatus("0");
			return ok(Json.toJson(result));
		}

		String endfilestr="";
		
		String path=Configuration.root().getString("oss.upload.endorsement", "upload/endorsement/");//上传路径
		String BUCKET_NAME=Configuration.root().getString("oss.bucket.name.higouAPIDev", "higou-api");
		boolean IsProduct = Configuration.root().getBoolean("production", false);
		if(IsProduct){
			BUCKET_NAME=Configuration.root().getString("oss.bucket.name.higouAPIProduct", "higou-api");
		}
		String domains=OSSUtils.PROTOCOL + "://" + BUCKET_NAME + "." + OSSUtils.OSS_ENDPOINT;
		if (sfFile != null && sfFile.getFile() != null) {
			String fileName = sfFile.getFilename();
			File file = sfFile.getFile();//获取到该文件
			int p = fileName.lastIndexOf('.');
			String type = fileName.substring(p, fileName.length()).toLowerCase();
			
			if (".jpg".equals(type)||".gif".equals(type)||".png".equals(type)||".jpeg".equals(type)||".bmp".equals(type)) {
				// 检查文件后缀格式
				String fileNameLast = "headicon"+UUID.randomUUID().toString()+type;//最终的文件名称
				endfilestr = OSSUtils.uploadFile(file,path,fileNameLast, type,BUCKET_NAME);		
			}
		}
		if(!StringUtils.isBlank(endfilestr))
			Headimg=domains+endfilestr;
		userService.EditUserInfo(uid, Headimg, Nickname, sex,1);
		User user=userService.getUserByUid(uid);
		List<String> authList = new ArrayList<String>();
		authList.add("0");
		if ("1".equals(String.valueOf(user.getIsEndorsement()))){
			authList.add("1");
		}
		if ("4".equals(String.valueOf(user.getGid()))){
			authList.add("2");
		}
		result.setUid(Integer.valueOf(uid.toString()));
		result.setNickname(Nickname==null?"":Nickname);
		result.setHeadIcon(user==null?"":user.getHeadIcon().replace("\\", "/"));
		result.setPhone(user==null?"":user.getPhone());
		result.setGender(sex);
		result.setStatus("1");
		result.setMsg("修改成功");
		result.setAuthorities(authList);
		String domainimg=CdnAssets.CDN_API_PUBLIC_URL;
		if(StringUtils.isBlank(user.getHeadIcon())){
			if(StringUtils.isBlank(user.getSex()) || user.getSex().equals("0"))
				result.setHeadIcon(domainimg+"images/sheSaidImages/default_headicon_girl.png");
			else
				result.setHeadIcon(domainimg+"images/sheSaidImages/default_headicon_boy.png");
		}
		
		return ok(Json.toJson(result));
	}

	// 代言收入流水信息接口
	public Result getendorsPaylist() {
		response().setContentType("application/json;charset=utf-8");
		Long uid = Numbers.parseLong(
				AjaxHellper.getHttpParam(request(), "uid"), Long.valueOf(0));
		Integer pagesize = 3;
		Integer page = Numbers
				.parseInt(AjaxHellper.getHttpParam(request(), "page"),
						Integer.valueOf(0));

		String devid = AjaxHellper.getHttpParam(request(), "devid");
		String wdhjy = AjaxHellper.getHttpParam(request(),"wdhjy");
		String appversion = AjaxHellper.getHttpParam(request(), "appversion");
		EndorsePaylogVO result = new EndorsePaylogVO();
		if (!StringUtil.checkMd5(devid, wdhjy,appversion))
		{
			result.status="0";
			return ok(Json.toJson(result));
		}
		
//		if(page>=1)
//			page=page-1;
		UserBalance userb=endorsementService.getUserBalance(uid);
		if(userb==null){
			userb=new UserBalance();
			userb.setUserId(uid);
			userb.setCreateTime(new Date());
			userb.setEnbalance(new BigDecimal(0));
			userb.setBalance(new BigDecimal(0));
		}
		userb.setRedFlag("0");
		//消红点
		userService.updateUserRedFlag(uid, "EndorseBalanceFlag", 0);
		endorsementService.saveUserBalance(userb);
		result = endorsementService.getEndorsepayloglist(uid,
				pagesize, page);
		return ok(Json.toJson(result));
	}

	// 钱包流水信息接口
	public Result getbalanceLoglist() {
		response().setContentType("application/json;charset=utf-8");
		Long uid = Numbers.parseLong(
				AjaxHellper.getHttpParam(request(), "uid"), Long.valueOf(0));
		Integer pagesize = 3;
		Integer page = Numbers
				.parseInt(AjaxHellper.getHttpParam(request(), "page"),
						Integer.valueOf(0));
		
		String devid = AjaxHellper.getHttpParam(request(), "devid");
		String wdhjy = AjaxHellper.getHttpParam(request(),"wdhjy");
		String appversion = AjaxHellper.getHttpParam(request(), "appversion");
		UserBalanceLogVO result = new UserBalanceLogVO();
		if (!StringUtil.checkMd5(devid, wdhjy,appversion))
		{
			result.status="0";
			return ok(Json.toJson(result));
		}
		
		result.status = "1";
		result.endflag=1;
		result.noticeMsg=StringUtil.getSystemConfigValue("wallet_noticeMsg");
		result.rulesurl=StringUtil.getSystemConfigValue("wallet_rulesurl");//StringUtil.getDomain()+"/H5/balancerule";
		result.limitmoney=StringUtil.getSystemConfigValue("wallet_moneylimit");
		result.data.balancelist=new ArrayList<UserBalanceLogItem>();
		result.data.balance="0";
		
		UserBalance userb=endorsementService.getUserBalance(uid);
		if(userb==null)
		{
			userb=new UserBalance();
			userb.setBalance(new BigDecimal(0));
			userb.setCreateTime(new Date());
			userb.setEnbalance(new BigDecimal(0));
			userb.setUpdateTime(new Date());
			userb.setRedFlag("0");
			userb.setUserId(uid);
		}
		
		result.data.balance=String.valueOf(userb.getBalance());
		userb.setRedFlag("0");//将金额变化的flag改为0
		endorsementService.saveUserBalance(userb);
		Page<UserBalanceLog> ublog = endorsementService.userbalanceloglist(uid,
				10, page);
		if (userb!=null && ublog != null && ublog.getContent() != null
				&& !ublog.getContent().isEmpty()) {
			//result.endflag=1;
			result.data.balance=userb.getBalance().toString();
			if((page+1)<ublog.getTotalPages())
				result.endflag=0;
			
			for (UserBalanceLog ul : ublog.getContent()) {
				UserBalanceLogItem ui = new UserBalanceLogItem();
				ui.balance = ul.getBalance()==null?"0.00":ul.getBalance().toString();
				ui.beforBalance = ul.getBalance();
				ui.createTime =ul.getCreateTime()==null?"":CHINESE_DATE_TIME_FORMAT.format(ul
						.getCreateTime());
				ui.curentBalance = ul.getCurentBalance();
				ui.flg = ul.getFlg();
				if(ui.flg==2)
					ui.balance="- "+ui.balance;
				else
					ui.balance="+ "+ui.balance;
				
				ui.id = ul.getBid();
				ui.remark = ul.getRemark();
				result.data.balancelist.add(ui);
			}

		}

		return ok(Json.toJson(result));
	}
	
	
	//用户红点接口
	public Result getRedFlag() {
		response().setContentType("application/json;charset=utf-8");
		Long uid = Numbers.parseLong(
				AjaxHellper.getHttpParam(request(), "uid"), Long.valueOf(0));
		String devid = AjaxHellper.getHttpParam(request(), "devid");
		String wdhjy = AjaxHellper.getHttpParam(request(),"wdhjy");
		String appversion = AjaxHellper.getHttpParam(request(), "appversion");
		
		ObjectNode result = Json.newObject();
		if (!StringUtil.checkMd5(devid, wdhjy,appversion))
		{
			result.put("status", "0");
			return ok(Json.toJson(result));
		}
		
		if(uid==0){
			result.put("status", "0");
		}else{
			result.put("status", "1");
			ObjectNode data = Json.newObject();
			UserBalance userb=endorsementService.getUserBalance(uid);
			if (userb !=null)
			{
				data.put("userBalanceRedFlag", userb.getRedFlag());
			}else{
				data.put("userBalanceRedFlag", "0");
			}
			result.set("data", data);
		}
		

		return ok(Json.toJson(result));
	}
	
	
	// 提现说明H5信息接口
	public Result getPayMoney() {
		response().setContentType("application/json;charset=utf-8");
		String devid = AjaxHellper.getHttpParam(request(), "devid");
		String wdhjy = AjaxHellper.getHttpParam(request(),"wdhjy");
		String appversion = AjaxHellper.getHttpParam(request(), "appversion");
		
		ObjectNode result = Json.newObject();
		if (!StringUtil.checkMd5(devid, wdhjy,appversion))
		{
			result.put("status", "0");
			return ok(Json.toJson(result));
		}
		result.put("status", "1");
		result.put("H5url", Constants.ENDORSEMENT_MONEY_URL);
		return ok(Json.toJson(result));
	}

	// 用户提现接口
	public Result OperMoney() {
		response().setContentType("application/json;charset=utf-8");
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
		
		String uname=AjaxHellper.getHttpParam(request(), "userName");
		if(StringUtils.isBlank(uname)){
			uname = AjaxHellper.getHttpParamOfFormUrlEncoded(request(), "userName");
		}
		if(uname!=null){
			try{
				pramt.put("userName",uname);
				uname=URLDecoder.decode(uname);
			}catch(Exception ex){
				
			}
		}
		String fee = AjaxHellper.getHttpParam(request(), "fee");
		if(StringUtils.isBlank(fee)){
			fee = AjaxHellper.getHttpParamOfFormUrlEncoded(request(), "fee");
		}
		if(fee!=null){
			pramt.put("fee",fee);
		}
		String paycardno=AjaxHellper.getHttpParam(request(), "paycardno");
		if(StringUtils.isBlank(paycardno)){
			paycardno = AjaxHellper.getHttpParamOfFormUrlEncoded(request(), "paycardno");
		}
		if(paycardno!=null){
			pramt.put("paycardno",paycardno);
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
		Long userid = Numbers.parseLong(uid,0L);
		if (userid == 0) {
			result.put("status", "0");
			return ok(Json.toJson(result));
		}
	
		if (!StringUtil.checkMd5(devid, wdhjy,appversion))
		{
			result.put("status", "0");
			result.put("msg", "校验失败");
			return ok(Json.toJson(result));
		}
		if (!StringUtil.checksign(lwdjl,md5sign,appversion)){
			result.put("status", "0");
			result.put("msg", "校验失败");
			return ok(Json.toJson(result));
		}
		
		if(StringUtils.isBlank(uname)){
			result.put("status", "0");
			result.put("msg", "姓名不能为空");
			return ok(Json.toJson(result));
		}
		else if(uname.length()>50){
			result.put("status", "0");
			result.put("msg", "姓名长度不能超过50字符");
			return ok(Json.toJson(result));
		}
		BigDecimal balance = null;
		try {
			balance = new BigDecimal(fee);
			Integer txmoney=Numbers.parseInt(StringUtil.getSystemConfigValue("wallet_moneylimit"), 0);
			
			if(balance.compareTo(new BigDecimal(txmoney))<0){
				result.put("status", "0");
				result.put("msg", "金额小于"+txmoney.toString()+"不能提现");
				return ok(Json.toJson(result));
			}
		} catch (Exception e) {
			logger.error("fee传入非法!");
			result.put("status", "0");
			result.put("msg", "非法操作");
			return ok(Json.toJson(result));
		}		
		if(StringUtils.isBlank(paycardno)){
			result.put("status", "0");
			result.put("msg", "支付宝帐号不能为空!");
			return ok(Json.toJson(result));
		}else{
			if(paycardno.length()>50){
				result.put("status", "0");
				result.put("msg", "支付宝帐号过长!");
				return ok(Json.toJson(result));
			}
		}
		// 判断余额够否
		User us = userService.getUserByUid(userid);
		UserBalance ub = endorsementService.getUserBalance(userid);	

		if (us == null || ub == null || !us.getUid().equals(ub.getUserId())
				|| ub.getBalance().compareTo(balance) < 0) {
			result.put("status", "0");
			result.put("msg", "余额不足");
			return ok(Json.toJson(result));
		}
		//增加校验，devid必须与uid一致，否则提现失败
		String uuid = userService.getUserId(devid);
		if (StringUtils.isBlank(uuid)){
			result.put("status", "0");
			result.put("msg", "数据非法");
			return ok(Json.toJson(result));
		}
		if (!uuid.equals(uid))
		{
			result.put("status", "0");
			result.put("msg", "数据非法");
			return ok(Json.toJson(result));
		}
		// 做余额变动日志
		UserBalanceLog ubg = new UserBalanceLog();
		ubg.setBalance(balance);
		ubg.setBeforBalance(ub.getBalance());
		ubg.setCurentBalance(balance.add(ub.getBalance()));
		ubg.setCreateTime(new Date());
		ubg.setFlg(2);
		ubg.setPaycardno(paycardno);
		ubg.setUserId(userid);
		ubg.setUserName(uname);
		ubg.setRemark("提现"
				+ balance.setScale(2, BigDecimal.ROUND_HALF_UP).toString() + "元");
		ubg=endorsementService.addbalanceLog(ubg);

		
		BalanceOperLog oper = new BalanceOperLog();
		BalanceOperLog opert=null;
		if(ubg!=null && ubg.getBid()>0){
			oper.setBalance(balance);
			oper.setCreateTime(new Date());
			oper.setFlag(0);
			oper.setUserId(userid);
			oper.setPaycardno(paycardno);
			oper.setUserName(uname);
			opert = endorsementService.addOperLog(oper);
			if(opert!=null && opert.getBid()>0){
				endorsementService.updatebalance(userid, balance);
			}
		}
		
		if (opert == null){
			result.put("status", "0");
			result.put("msg", "清加操作日志失败");
		}
		else{
			result.put("status", "1");
		}

		return ok(Json.toJson(result));
	}

	// 用户绑定提现账号接口
	public Result edituserCard() {
		response().setContentType("application/json;charset=utf-8");
		ObjectNode result = Json.newObject();
		Long uid = Numbers.parseLong(
				AjaxHellper.getHttpParam(request(), "uid"), Long.valueOf(0));
		String cardNo = AjaxHellper.getHttpParam(request(), "account");
		int cardtype = Numbers.parseInt(
				AjaxHellper.getHttpParam(request(), "accountTyp"), 1);
		String devid = AjaxHellper.getHttpParam(request(), "devid");
		String wdhjy = AjaxHellper.getHttpParam(request(),"wdhjy");
		String appversion = AjaxHellper.getHttpParam(request(), "appversion");
		if (!StringUtil.checkMd5(devid, wdhjy,appversion))
		{
			result.put("status", "0");
			result.put("msg", "校验失败");
			return ok(Json.toJson(result));
		}
		
		if (uid == 0 || StringUtils.isBlank(cardNo)) {
			result.put("status", "0");
			return ok(Json.toJson(result));
		}
		userService.updateuserCard(uid, cardNo, cardtype);

		result.put("status", "1");

		return ok(Json.toJson(result));
	}

	
	public Result report(){
		response().setContentType("application/json;charset=utf-8");
		String uid = AjaxHellper.getHttpParamOfFormUrlEncoded(request(),"uid");
		String eid = AjaxHellper.getHttpParamOfFormUrlEncoded(request(),"eid");
		String devid = AjaxHellper.getHttpParamOfFormUrlEncoded(request(), "devid");
		String wdhjy = AjaxHellper.getHttpParamOfFormUrlEncoded(request(),"wdhjy");
		String appversion = AjaxHellper.getHttpParamOfFormUrlEncoded(request(), "appversion");
		ObjectNode result=Json.newObject();
		if (!StringUtil.checkMd5(devid, wdhjy,appversion))
		{
			result.put("status", "0");
			result.put("msg", "校验失败");
			return ok(Json.toJson(result));
		}
		
		EndorsementReport endorsementReport = new EndorsementReport();
		endorsementReport.setEid(Numbers.parseLong(eid, 0L));
		endorsementReport.setUserId(Numbers.parseLong(uid, 0L));
		endorsementReport.setRemark("");
		endorsementReport.setCreateTime(new Date());
		endorsementService.saveEndorsementReport(endorsementReport);
		result.put("status", "1");
		return ok(Json.toJson(result));
	}
	
	// 发表代言接口
	public Result addendorse() {
		
		response().setContentType("application/json;charset=utf-8");
		Long pid = Numbers.parseLong(
				Form.form().bindFromRequest().get("pid"), Long.valueOf(0));
		Long uid = Numbers.parseLong(
				Form.form().bindFromRequest().get("uid"), Long.valueOf(0));
		String content=Form.form().bindFromRequest().get("content");
		
		String devid = Form.form().bindFromRequest().get("devid");
		String wdhjy =  Form.form().bindFromRequest().get("wdhjy");
		String appversion = Form.form().bindFromRequest().get("appversion");
		
		ObjectNode result=Json.newObject();
		if (!StringUtil.checkMd5(devid, wdhjy,appversion))
		{
			result.put("status", "0");
			result.put("msg", "校验失败");
			return ok(Json.toJson(result));
		}
//		if(!StringUtils.isBlank(content)){
//			try{
//				content=new String(content.getBytes("utf-8"),"gbk");
//			}catch(Exception e){
//			}
//		}
		//处理图片
		MultipartFormData body = request().body().asMultipartFormData();
		Integer nums=0;
		if(body!=null && body.getFiles()!=null && body.getFiles().size()>0){
			nums=body.getFiles().size();
		}
		String endfilestr="";
		String domains = "http://ht2.neolix.cn:9004";//Configuration.root().getString("domain.dev","http://ht2.neolix.cn:9004");
		boolean IsProduct = Configuration.root().getBoolean("production", false);
		if(IsProduct){
			domains = Configuration.root().getString("domain.productH5","http://ht.neolix.cn");
		}
		if (pid == 0) {
			result.put("status", "0");
			result.put("msg", "产品编号错误");
			return ok(Json.toJson(result));
		}
		Product pro = productService.getProductById(pid);

		result.put("status", "0");
		if (pro == null) {
			result.put("status", "0");
			result.put("msg", "产品不存在");
			return ok(Json.toJson(result));
		}

		if(pro.getIsEndorsement()!=1){
			result.put("status", "0");
			result.put("msg", "非代言产品");
			return ok(Json.toJson(result));
		}

		if (pro.getMaxEndorsementCount() <= pro.getEndorsementCount()) {
			result.put("status", "0");
			result.put("msg", "代言已达到上限");
			return ok(Json.toJson(result));
		}

		// 代言商品代言数量加1；
//		pro.setEndorsementCount(pro.getEndorsementCount() + 1);
//		pro = productService.saveProduct(pro);


		//检查是否抢过
		boolean inhas=endorsementService.checkEndorsement(uid, pid, new Date());
		if(!inhas){
			result.put("status", "0");
			result.put("msg", "抢超时请重新抢");
			return ok(Json.toJson(result));
		}

		User user=userService.getUserByUid(uid);
		if(user==null){
			result.put("status", "0");
			result.put("msg", "用户不存在");
			return ok(Json.toJson(result));
		}
		List<String> piclist=this.uploadimglist(body);
		
		if(piclist!=null && !piclist.isEmpty()){
				Endorsement endorsment = new Endorsement();
				endorsment.setCount(0);
				endorsment.setCreateTime(new Date());
				endorsment.setPreImgPath("");
				endorsment.setProductId(pro.getPid());
				endorsment.setUserId(uid);
				endorsment.setRemark(content);
				endorsment.setPicnums(nums);
				endorsment.setStatus(0);
				endorsment.setBannerimg(StringUtil.getSheSaidIcon());
				endorsment.setSort(0);
				try{
					endorsment = endorsementService.saveEndorsement(endorsment);
				}
				catch(Exception ee){
					result.put("status", "0");
					result.put("msg", "请检查代言内容是否有表情等非法符号");
					return ok(Json.toJson(result));
				}
				for(int i=0;i<piclist.size();i++){
					String[] p=piclist.get(i).split("~");
					// 写入图片表
					EndorsementImg eminfo = new EndorsementImg();
					eminfo.setCreateTime(new Date());
					eminfo.setEid(endorsment.getEid());
					eminfo.setImgName(p[0]);
					eminfo.setImgPath(p[1]);
					eminfo.setPicNO(i);
					try{
						eminfo.setHeight(Long.valueOf(p[2]));
						eminfo.setWidth(Long.valueOf(p[3]));
					}
					catch(Exception e){}
					EndorsementImg eimg = endorsementService.saveEimg(eminfo);
					// 添加首图
					if (i == 0) {
						endorsementService.updatePreimg(p[1], endorsment.getEid());
						result.put("preImgUrl",StringUtil.getOSSUrl()+p[1]);
					}
				}
				
				//回填操作日志
				endorsementService.editEndorseOptLog(uid, pid, endorsment.getEid());
				
				result.put("status", "1");
				result.put("daiyanId", endorsment.getEid());
				//User user=userService.getUserByUid(uid);
				String shareType=user.getShareType();
				result.put("shareType", shareType);
				//转换短链接
				String daiyanURL=Constants.ENDORSEMENT_URL+endorsment.getEid()+"&shareType="+shareType;
				if(!StringUtils.isBlank(shareType) && !shareType.equals("1")){
					try{
						String sortturnurl="http://api.t.sina.com.cn/short_url/shorten.json?source="+Constants.APP_KEY+"&url_long="+daiyanURL+"&callback=jsonpshorturl123";
						// 发送请求，返回json
						HttpClient clientc = new DefaultHttpClient();
						HttpGet get = new HttpGet(sortturnurl);
						
						String resContenti = "";
						HttpResponse resc = clientc.execute(get);
						if (resc.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
							HttpEntity entity = resc.getEntity();
	
							resContenti = EntityUtils.toString(resc.getEntity(),"UTF-8");
							// System.out.println(strResult);
							Logger.info("调用新浪转换短链接接口返回报文内容:" + resContenti);	
							if(!StringUtils.isBlank(resContenti)){
								resContenti=resContenti.replace("[", "").replace("]", "");
							}
							JSONObject jsons = JSONObject.fromObject(resContenti);
							if(!StringUtils.isBlank(jsons.getString("url_short")))
								daiyanURL=jsons.getString("url_short");
						}
					}
					catch(Exception e){
						Logger.error("转换短链接失败："+e.toString());
					}
				}
				result.put("daiyanURL",daiyanURL);
				result.put("shareTitle", StringUtil.getShesaidTitle(user.getNickname()));
				result.put("shareDescription", StringUtil.getSheSaidremark());
				result.put("productTitle", pro.getTitle());
			}			
		return ok(Json.toJson(result));
	}
	
	
	// 修改代言接口
		public Result editendorse() {
			
			response().setContentType("application/json;charset=utf-8");
			Long dyid = Numbers.parseLong(
					Form.form().bindFromRequest().get("daiyanid"), Long.valueOf(0));
//			Long uid = Numbers.parseLong(
//					Form.form().bindFromRequest().get("uid"), Long.valueOf(0));
			String content=Form.form().bindFromRequest().get("content");
			String devid = Form.form().bindFromRequest().get("devid");
			String wdhjy = Form.form().bindFromRequest().get("wdhjy");
			String appversion = Form.form().bindFromRequest().get("appversion");
			ObjectNode result=Json.newObject();
			if (!StringUtil.checkMd5(devid, wdhjy,appversion))
			{
				result.put("status", "0");
				result.put("msg", "校验失败");
				return ok(Json.toJson(result));
			}
			//处理图片
			MultipartFormData body = request().body().asMultipartFormData();
			Integer nums=0;
			if(body!=null && body.getFiles()!=null && body.getFiles().size()>0){
				nums=body.getFiles().size();
			}
			String endfilestr="";
			String domains = "http://ht2.neolix.cn:9004";//Configuration.root().getString("domain.dev","http://ht2.neolix.cn:9004");
			boolean IsProduct = Configuration.root().getBoolean("production", false);
			if(IsProduct){
				domains = Configuration.root().getString("domain.productH5","http://ht.neolix.cn");
			}
			result.put("status", "0");
			if(dyid==0L){
				result.put("status", "0");
				result.put("msg", "参数错误");
				return ok(Json.toJson(result));
			}
			

			Endorsement endorse=endorsementService.getEndorseById(dyid);
			if(endorse==null){
				result.put("status", "0");
				result.put("msg", "代言不存在");
				return ok(Json.toJson(result));
			}
//			User user=userService.getUserByUid(uid);
//			if(user==null){
//				result.put("status", "0");
//				result.put("msg", "用户不存在");
//				return ok(Json.toJson(result));
//			}
//			if(endorse.getUserId()!=user.getUid()){
//				result.put("status", "0");
//				result.put("msg", "非法操作");
//				return ok(Json.toJson(result));
//			}
			
			List<String> piclist=this.uploadimglist(body);
			
			if(piclist!=null && !piclist.isEmpty()){
				endorse.setRemark(content);
				endorse.setPicnums(nums);
				
				endorse = endorsementService.saveEndorsement(endorse);
				//删除图片
				endorsementService.delEmdorseImg(endorse.getEid());
				
					for(int i=0;i<piclist.size();i++){
						String[] p=piclist.get(i).split("~");
						// 写入图片表
						EndorsementImg eminfo = new EndorsementImg();
						eminfo.setCreateTime(new Date());
						eminfo.setEid(endorse.getEid());
						eminfo.setImgName(p[0]);
						eminfo.setImgPath(p[1]);
						eminfo.setPicNO(i);
						try{
							eminfo.setHeight(Long.valueOf(p[2]));
							eminfo.setWidth(Long.valueOf(p[3]));
						}
						catch(Exception e){}
						EndorsementImg eimg = endorsementService.saveEimg(eminfo);
						// 添加首图
						if (i == 0) {
							endorsementService.updatePreimg(p[1], endorse.getEid());
						}
					}
					
					
					result.put("status", "1");
					return redirect("http://ht.neolix.cn/admin/endorsmentlist.php");
				}			
			return ok(Json.toJson(result));
		}
	
	
	// 上传身份证
	public Result uploadCard() {
		response().setContentType("application/json;charset=utf-8");
		Long aid=Numbers.parseLong(Form.form().bindFromRequest().get("addressId"), Long.valueOf(0));
		Long uid = Numbers.parseLong(Form.form().bindFromRequest().get("uid"), Long.valueOf(0));
		String devid = Form.form().bindFromRequest().get("devid");
		String wdhjy = Form.form().bindFromRequest().get("wdhjy");
		String appversion = Form.form().bindFromRequest().get("appversion");
		ObjectNode result=Json.newObject();
		if (!StringUtil.checkMd5(devid, wdhjy,appversion))
		{
			result.put("status", "0");
			result.put("msg", "校验失败");
			return ok(Json.toJson(result));
		}
		if (aid == 0) {
			result.put("status", "0");
			return ok(Json.toJson(result));
		}
		
		MultipartFormData body = request().body().asMultipartFormData();
		String path=Configuration.root().getString("oss.upload.address", "upload/address/");//上传路径
		String BUCKET_NAME=Configuration.root().getString("oss.bucket.name.higouAPIDev", "higou-api");
		boolean IsProduct = Configuration.root().getBoolean("production", false);
		if(IsProduct){
			BUCKET_NAME=Configuration.root().getString("oss.bucket.name.higouAPIProduct", "higou-api");
		}
		
		String endfilestr="";
		
		if(body!=null && body.getFiles()!=null && body.getFiles().size()>0){

			List<String> tfname=new ArrayList<String>();
			Map<String,String> tfnamemap=new HashMap<String,String>();
			
			for(int i=0;i<body.getFiles().size();i++){
				if(body.getFiles().get(i)!=null && body.getFiles().get(i).getFile()!=null){
					tfnamemap.put(body.getFiles().get(i).getKey(), body.getFiles().get(i).getKey());
				}
			}
			Object[] keys = tfnamemap.keySet().toArray();
			Arrays.sort(keys);//对file按传入名称进行排序
			for (int i = 0; i < keys.length; i++) {
				String mapkey = (String) keys[i];
				tfname.add(mapkey);
			}
			for(int i=0;i<tfname.size();i++){
				FilePart sfFile=body.getFile(tfname.get(i));
				if (sfFile != null && sfFile.getFile() != null) {		
					File file = sfFile.getFile();//获取到该文件
					String fileName = sfFile.getFilename();
					int p = fileName.lastIndexOf('.');
					String type = fileName.substring(p, fileName.length()).toLowerCase();
					
					if (".jpg".equals(type)||".gif".equals(type)||".png".equals(type)||".jpeg".equals(type)||".bmp".equals(type)) {
						String fileNameLast = UUID.randomUUID().toString()+type;//最终的文件名称
						String url = OSSUtils.uploadFile(file,path,fileNameLast, type,BUCKET_NAME);	
						if(StringUtils.isBlank(endfilestr)){
							endfilestr = url;
						}else{
							endfilestr = endfilestr+","+url;
						}
					} else {
						result.put("status", 0);
						return ok(Json.toJson(result));
					}								
				} else
					result.put("status", "0");
			}
		}
		Integer hasimg=0;
		if(endfilestr.length()>1)
			hasimg=1;
		addressService.EditaddressImg(aid, hasimg, endfilestr);
		result.put("status", "1");
		
		return ok(Json.toJson(result));
	}

	/*
	 * 图片压缩
	 */
	private BufferedImage getNewImage(MultipartFormData oldImage, double width,
			double height) throws IOException {
		/* srcURl 原图地址；deskURL 缩略图地址；comBase 压缩基数；scale 压缩限制(宽/高)比例 */

		FilePart sfFile = oldImage.getFiles().get(0);
		File pfile = sfFile.getFile();
		FileInputStream fis = new FileInputStream(pfile);
		BufferedInputStream bis = new BufferedInputStream(fis);

		ByteArrayOutputStream baos = new ByteArrayOutputStream();

		int c = bis.read();// 读取bis流中的下一个字节

		while (c != -1) {

			baos.write(c);

			c = bis.read();

		}

		bis.close();

		ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
		MemoryCacheImageInputStream mciis = new MemoryCacheImageInputStream(
				bais);
		Image src = ImageIO.read(mciis);
		double srcHeight = src.getHeight(null);
		double srcWidth = src.getWidth(null);
		double deskHeight = 0;// 缩略图高
		double deskWidth = 0;// 缩略图宽
		if (srcWidth > srcHeight) {

			if (srcWidth > width) {
				if (width / height > srcWidth / srcHeight) {
					deskHeight = height;
					deskWidth = srcWidth / (srcHeight / height);
				} else {
					deskHeight = width / (srcWidth / srcHeight);
					deskWidth = width;
				}
			} else {

				if (srcHeight > height) {
					deskHeight = height;
					deskWidth = srcWidth / (srcHeight / height);
				} else {
					deskHeight = srcHeight;
					deskWidth = srcWidth;
				}

			}

		} else if (srcHeight > srcWidth) {
			if (srcHeight > (height)) {
				if ((height) / width > srcHeight / srcWidth) {
					deskHeight = srcHeight / (srcWidth / width);
					deskWidth = width;
				} else {
					deskHeight = height;
					deskWidth = (height) / (srcHeight / srcWidth);
				}
			} else {
				if (srcWidth > width) {
					deskHeight = srcHeight / (srcWidth / width);
					deskWidth = width;
				} else {
					deskHeight = srcHeight;
					deskWidth = srcWidth;
				}

			}

		}else if (srcWidth == srcHeight) {
			if (width >= (height) && srcHeight > (height)) {
				deskWidth = (height);
				deskHeight = (height);
			} else if (width <= (height) && srcWidth > width) {
				deskWidth = width;
				deskHeight = width;
			} else if (width == (height) && srcWidth < width) {
				deskWidth = srcWidth;
				deskHeight = srcHeight;
			} else {
				deskHeight = srcHeight;
				deskWidth = srcWidth;
			}
		}
		
		BufferedImage tag = new BufferedImage((int) deskWidth,
				(int) deskHeight, BufferedImage.TYPE_3BYTE_BGR);
		tag.getGraphics().drawImage(src, 0, 0, (int) deskWidth,
				(int) deskHeight, null); // 绘制缩小后的图
		return tag;
	}
	
	//php后台使用发表代言
	public Result addendosephp(){
		response().setContentType("application/json;charset=utf-8");
		Long pid = Numbers.parseLong(
				Form.form().bindFromRequest().get("pid"), Long.valueOf(0));
		Long uid = Numbers.parseLong(
				Form.form().bindFromRequest().get("uid"), Long.valueOf(0));
		String content=Form.form().bindFromRequest().get("content");
		//处理图片
		MultipartFormData body = request().body().asMultipartFormData();
		Integer nums=0;
		if(body!=null && body.getFiles()!=null && body.getFiles().size()>0){
			nums=body.getFiles().size();
		}
		String endfilestr="";
		
		ObjectNode result=Json.newObject();
		if (pid == 0) {
			result.put("status", "0");
			return ok(Json.toJson(result));
		}
		Product pro = productService.getProductById(pid);

		result.put("status", "0");
		if (pro == null) {
			result.put("status", "0");
			return ok(Json.toJson(result));
		}

		// 代言商品代言数量加1；
		pro.setEndorsementCount(pro.getEndorsementCount() + 1);
		pro = productService.saveProduct(pro);
		if(pro==null)
		{
			result.put("status", "0");
			return ok(Json.toJson(result));
		}
	
		List<String> piclist=this.uploadimglist(body);
		
		if(piclist!=null && !piclist.isEmpty()){
				Endorsement endorsment = new Endorsement();
				endorsment.setCount(0);
				endorsment.setCreateTime(new Date());
				endorsment.setPreImgPath("");
				endorsment.setProductId(pro.getPid());
				endorsment.setUserId(uid);
				endorsment.setRemark(content);
				endorsment.setPicnums(nums);
				endorsment = endorsementService.saveEndorsement(endorsment);
				for(int i=0;i<piclist.size();i++){
					String[] p=piclist.get(i).split("~");
					// 写入图片表
					EndorsementImg eminfo = new EndorsementImg();
					eminfo.setCreateTime(new Date());
					eminfo.setEid(endorsment.getEid());
					eminfo.setImgName(p[0]);
					eminfo.setImgPath(p[1]);
					eminfo.setPicNO(i);
					eminfo.setHeight(Long.valueOf(p[2]));
					eminfo.setWidth(Long.valueOf(p[3]));
					endorsementService.saveEimg(eminfo);
					// 添加首图
					if (i == 0) {
							endorsementService.updatePreimg(p[1], endorsment.getEid());
					}
				}
			}
		return redirect("http://ht.neolix.cn/admin/endorsmentlist.php");
	}

	//图片上传私有方法,返回String 规则：文件名称_数据库路径_图片高度_图片宽度_图片保存路径
	private List<String> uploadimglist(MultipartFormData body){
		List<String> piclist=new ArrayList<String>();
		
		if(body!=null && body.getFiles()!=null && body.getFiles().size()>0){
			List<String> tfname=new ArrayList<String>();
			Map<String,String> tfnamemap=new HashMap<String,String>();
			for(int i=0;i<body.getFiles().size();i++){
				if(body.getFiles().get(i)!=null && body.getFiles().get(i).getFile()!=null){
					tfnamemap.put(body.getFiles().get(i).getKey(), body.getFiles().get(i).getKey());
				}
			}
			Object[] keys = tfnamemap.keySet().toArray();
			Arrays.sort(keys);
			
			//对file按传入名称进行排序
			for (int i = 0; i < keys.length; i++) {
				String mapkey = (String) keys[i];
				tfname.add(mapkey);
			}
			
			String path=Configuration.root().getString("oss.upload.endorsement", "upload/endorsement/");//上传路径
			String BUCKET_NAME=Configuration.root().getString("oss.bucket.name.higouAPIDev", "higou-api");
			boolean IsProduct = Configuration.root().getBoolean("production", false);
			if(IsProduct){
				BUCKET_NAME=Configuration.root().getString("oss.bucket.name.higouAPIProduct", "higou-api");
			}
			
			
			for(int i=0;i<tfname.size();i++){									
				FilePart sfFile=body.getFile(tfname.get(i));//body.getFiles().get(i);
				if (sfFile != null && sfFile.getFile() != null) {
					File file = sfFile.getFile();//获取到该文件
					String fileName = sfFile.getFilename();
					int p = fileName.lastIndexOf('.');
					String type = fileName.substring(p, fileName.length()).toLowerCase();
					
					if (".jpg".equals(type)||".gif".equals(type)||".png".equals(type)||".jpeg".equals(type)||".bmp".equals(type)) {
						// 检查文件后缀格式
						String fileEnd = fileName.substring(fileName.lastIndexOf(".") + 1).toLowerCase();
						String fileNameLast = UUID.randomUUID().toString()+type;//最终的文件名称
						String url = OSSUtils.uploadFile(file,path,fileNameLast, type,BUCKET_NAME);		
						
						Long srcHeight =0L;
						Long srcWidth=0L;
						try {
							FileInputStream fiss = new FileInputStream(sfFile.getFile());
							BufferedInputStream bis = new BufferedInputStream(fiss);
							ByteArrayOutputStream baos = new ByteArrayOutputStream();
							int c = bis.read();// 璇诲彇bis娴佷腑鐨勪笅涓�釜瀛楄妭
							while (c != -1) {
								baos.write(c);
								c = bis.read();
							}
							bis.close();
							ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
							MemoryCacheImageInputStream mciis = new MemoryCacheImageInputStream(bais);
							Image src = ImageIO.read(mciis);
							/*
							 * 获取宽度和高度
							 */
							srcHeight = Long.valueOf(src.getWidth(null));
							srcWidth = Long.valueOf(src.getHeight(null));
						
							piclist.add(fileNameLast+"~"+url+"~"+srcHeight+"~"+srcWidth);
						} catch (FileNotFoundException e) {
							logger.error("上传文件不存在,上传代言文件失败" + e.toString());
						} catch (IOException e) {
							logger.error("写文件失败,上传代言文件失败" + e.toString());
						}
					}
				}
			}
		}
		return piclist;
	}
	
	//删除代言
	public Result delendoresement(){
		ObjectNode result=Json.newObject();
		response().setContentType("application/json;charset=utf-8");
		Long eid = Numbers.parseLong(
				AjaxHellper.getHttpParam(request(),"daiyanid"), Long.valueOf(0));
		String devid = AjaxHellper.getHttpParam(request(), "devid");
		String wdhjy = AjaxHellper.getHttpParam(request(),"wdhjy");
		String appversion = AjaxHellper.getHttpParam(request(), "appversion");
		if (!StringUtil.checkMd5(devid, wdhjy,appversion))
		{
			result.put("status", "0");
			result.put("msg", "校验失败");
			return ok(Json.toJson(result));
		}
//		Long pid=Numbers.parseLong(AjaxHellper.getHttpParam(request(),"pid"),0L);
		if(eid==0L){
			result.put("status", "0");
			result.put("msg", "参数错误");
			return ok(Json.toJson(result));
		}
		//
		Endorsement endorse=endorsementService.getEndorseById(eid);
		if(endorse==null){
			result.put("status", "0");
			result.put("msg", "代言不存在");
			return ok(Json.toJson(result));
		}else
		{
			if(endorse.getProductId()==null || endorse.getProductId()<1)
			{
				result.put("status", "0");
				result.put("msg", "代言不存在");
				return ok(Json.toJson(result));
			}
		}
		Product pro=productService.getProductById(endorse.getProductId());
		if(pro==null){
			result.put("status", "0");
			result.put("msg", "产品不存在");
			return ok(Json.toJson(result));
		}
		
		//改变代言状态为3
		endorsementService.updateEndorseSta(eid, 3);
		//翻译产品代言数量-1
		pro.setEndorsementCount(pro.getEndorsementCount() - 1);
		pro = productService.saveProduct(pro);
		result.put("status", "1");
		result.put("msg", "发布成功");
		return ok(Json.toJson(result));
	}
	
	//根据产品编号获取代言列表
	public Result DetailAllEndorsements(){
		response().setContentType("application/json;charset=utf-8");
		Long pid=Numbers.parseLong(AjaxHellper.getHttpParam(request(), "pid"), 0L);
		Integer page=Numbers.parseInt(AjaxHellper.getHttpParam(request(),"page"),0);
		Long uid=Numbers.parseLong(AjaxHellper.getHttpParam(request(), "uid"), 0L);
		
		ObjectNode result=Json.newObject();
		String devid = AjaxHellper.getHttpParam(request(), "devid");
		String wdhjy = AjaxHellper.getHttpParam(request(),"wdhjy");
		String appversion = AjaxHellper.getHttpParam(request(), "appversion");
		if (!StringUtil.checkMd5(devid, wdhjy,appversion))
		{
			result.put("status", "0");
			result.put("msg", "校验失败");
			result.put("data", "");
			return ok(Json.toJson(result));
		}
		
		ObjectNode dataresult=Json.newObject();
		
		result.put("status", "0");
		result.put("msg", "没有代言数据");
		result.put("endFlag", "1");
		result.put("data", "");
		Product pro=productService.getProductById(pid);
		if(pro==null){
			return ok(result);
		}
		dataresult.put("pid", pid);
		dataresult.put("productImg", pro.getListpic());
		dataresult.put("productName", pro.getTitle());
		dataresult.put("productPrice", pro.getPrice());
		///???是否使用新人价格
		result.put("status", "1");
		result.put("msg", "");
		Page<Endorsement> emlist=endorsementService.getEndorsmentsByPid(pid, 1, page);
		String domainimg=CdnAssets.CDN_API_PUBLIC_URL;
		String domains=StringUtil.getOSSUrl();
//		Integer totalPage=emlist.getTotalPages();
//		if(totalPage==page){
//			result.put("endFlag", "0");
//		}else{
//			result.put("endFlag", "1");
//		}
		if (emlist != null && emlist.getContent() != null) {
			List<Endorsement> tmplist = emlist.getContent();
			List<EnorsmentVOItem> itemlist = new ArrayList<EnorsmentVOItem>();
			for (Endorsement et : tmplist) {
				EnorsmentVOItem itm = new EnorsmentVOItem();
				itm.eid = ""+et.getEid();
				itm.count = StringUtil.formatnum(Long.valueOf(et.getCount()));
				String preimg = et.getPreImgPath()==null?"":et.getPreImgPath();
				if (!preimg.isEmpty()){
					itm.preImgPath = domains+preimg;
				}else{
					itm.preImgPath = "";
				}
				itm.createTime =StringUtil.getfomatdate(et.getCreateTime(),new Date());
				
				itm.remark = et.getRemark()==null?"":et.getRemark();
				itm.userId = ""+et.getUserId();
				itm.linkURL = "endorsementDetail://eid="+et.getEid();
				itm.endorBadgeUrl=StringUtils.isBlank(et.getBannerimg())?domainimg+StringUtil.getSheSaidIcon():domainimg+et.getBannerimg();
				itm.endorTagImgUrl=StringUtil.getSheSaidTagImg();
				
				User uInfo = userService.getUserByUid(et.getUserId());
				itm.userNickName = uInfo.getNickname()==null?"":uInfo.getNickname();
				itm.userHeadIcon = uInfo.getHeadIcon()==null?"":uInfo.getHeadIcon();
				if(itm.userHeadIcon.equals("")){
					if(uInfo.getSex().equals("0") ||StringUtils.isBlank(uInfo.getSex()))
						itm.userHeadIcon=domainimg+"images/sheSaidImages/default_headicon_girl.png";
					else
						itm.userHeadIcon=domainimg+"images/sheSaidImages/default_headicon_boy.png";
				}

				itm.isLiked=endorsementService.getendorsmentIsPraise(et.getEid(),uid)?"1":"0";
				
				List<EndorsementPraise> praiseList = endorsementService
						.getPraiselist(et.getEid(),11);
				if (praiseList != null && !praiseList.isEmpty()){
					itm.headImglist = new ArrayList<EndorsmentVO.userLikeImg>();
					for (EndorsementPraise p : praiseList) {
						EndorsmentVO.userLikeImg uImg = new userLikeImg();
						uImg.userId = p.getUserId().toString();
						if(!StringUtils.isBlank(p.getImgPath()))
							uImg.headIcon = p.getImgPath();
						else
							uImg.headIcon="";
						if(uImg.headIcon.equals("")){
							if(StringUtils.isBlank(p.getSex()))
								uImg.headIcon=domainimg+"images/sheSaidImages/default_headicon_girl.png";
							else{
								if(p.getSex().equals("0"))
									uImg.headIcon=domainimg+"images/sheSaidImages/default_headicon_girl.png";
								else
									uImg.headIcon=domainimg+"images/sheSaidImages/default_headicon_boy.png";
							}
						}
						itm.headImglist.add(uImg);
					}
				}
				else
				{
					itm.headImglist=new ArrayList<userLikeImg>();
				}
				itemlist.add(itm);
			}
			dataresult.putPOJO("endorsementList", itemlist);

			result.putPOJO("data", dataresult);
		}
		return ok(result);
	}
}
