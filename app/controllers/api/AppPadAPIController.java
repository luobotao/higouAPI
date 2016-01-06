package controllers.api;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import models.Fromsite;
import models.PadChannelPro;
import models.Product;
import models.Product_images;

import org.apache.commons.lang3.StringUtils;

import play.Logger;
import play.libs.Json;
import play.mvc.Result;
import services.api.AppPadService;
import services.api.EndorsementService;
import services.api.ProductService;
import services.api.UserService;
import utils.AjaxHellper;
import utils.Numbers;
import utils.StringUtil;
import vo.appPad.AppPadChannelProVO;
import vo.appPad.appPadChannelVO;
import vo.appPad.appPadProVO;
import vo.appPad.appPadVO;

/**
 * 
 * @author luobotao
 *
 */
@Named
@Singleton
public class AppPadAPIController extends BaseApiController {
	private static final SimpleDateFormat CHINESE_DATE_TIME_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	private static final Logger.ALogger logger = Logger.of(UserAPIController.class);
	private static final java.util.regex.Pattern PHONE_PATTERN = java.util.regex.Pattern.compile("^((13[0-9])|(15[^4,\\D])|(18[0,5-9]))\\d{8}$");
	
	private final ProductService productService;
	private final UserService userService;
	private final EndorsementService endorsementService;
	private final AppPadService appPadService;
	@Inject
	public AppPadAPIController(final ProductService productService,final AppPadService appPadService,final UserService userService,final EndorsementService endorsementService){
		this.productService = productService;
		this.userService = userService;
		this.endorsementService=endorsementService;
		this.appPadService = appPadService;
	}
	
	// 新用户登录接口（POST方式)
		public Result padLogin() {
			response().setContentType("application/json;charset=utf-8");
			String uid = AjaxHellper.getHttpParamOfFormUrlEncoded(request(), "uid");
			String appversion = AjaxHellper.getHttpParamOfFormUrlEncoded(request(),"appversion");
			String marketCode = AjaxHellper.getHttpParamOfFormUrlEncoded(request(),"marketCode");
			String username = AjaxHellper.getHttpParamOfFormUrlEncoded(request(),"username");
			String password = AjaxHellper.getHttpParamOfFormUrlEncoded(request(),"password");
			String devid = AjaxHellper.getHttpParamOfFormUrlEncoded(request(),"devid");
			String wdhjy = AjaxHellper.getHttpParamOfFormUrlEncoded(request(),"wdhjy");
			appPadVO result = new appPadVO();
			if (!StringUtil.checkMd5(devid, wdhjy,appversion))
			{
				result.setStatus("0");
				result.setMsg("校验失败");
				result.setUid("0");
				return ok(Json.toJson(result));
			}
			result= userService.padlogin(username, password, devid, marketCode);
			if(result==null || StringUtils.isBlank(result.status))
			{
				result.setStatus("0");
				result.setMsg("用户名或者密码不正确");
				result.setUid("0");
				result.setUshopicon("");
				result.setUshopname("");
			}
			return ok(Json.toJson(result));
		}
		
		// 登出接口(GET)pad logout
		public Result padLogout() {
			response().setContentType("application/json;charset=utf-8");
			String uid = AjaxHellper.getHttpParam(request(), "uid");
			String devid = AjaxHellper.getHttpParam(request(),"devid");
			String wdhjy = AjaxHellper.getHttpParam(request(),"wdhjy");
			String appversion = AjaxHellper.getHttpParam(request(),"appversion");
			String marketCode = AjaxHellper.getHttpParam(request(),"marketCode");
			appPadVO result = new appPadVO();
			if (!StringUtil.checkMd5(devid, wdhjy,appversion))
			{
				result.setStatus("0");
				result.setMsg("校验失败");
				result.setUid("0");
				return ok(Json.toJson(result));
			}
			result.setStatus("1");
			result.setMsg("退出成功");
			result.setUid("0");
			/*
			int newUid=userService.bindLogout(uid.toString(),devid);
			if(newUid>0){
				result.setStatus("1");
				result.setMsg("");
				result.setUid(newUid+"");
			}else{
				result.setStatus("0");
				result.setMsg("登出失败");
				result.setUid(uid+"");
			}*/
			return ok(Json.toJson(result));
		}
		
		public Result pad_channels(){
			response().setContentType("application/json;charset=utf-8");
			String uid = AjaxHellper.getHttpParam(request(), "uid");
			String devid = AjaxHellper.getHttpParam(request(),"devid");
			String wdhjy = AjaxHellper.getHttpParam(request(),"wdhjy");
			String appversion = AjaxHellper.getHttpParam(request(),"appversion");
			String marketCode = AjaxHellper.getHttpParam(request(),"marketCode");
			appPadChannelVO result = new appPadChannelVO();
			if (!StringUtil.checkMd5(devid, wdhjy,appversion))
			{
				result.setStatus("0");
				result.setMsg("校验失败");
				return ok(Json.toJson(result));
			}
		    result = appPadService.getPadChannelList(uid);
			return ok(Json.toJson(result));
		}
		
		public Result pad_channel_list(){
			// 双图
			response().setContentType("application/json;charset=utf-8");
			String uid = AjaxHellper.getHttpParam(request(), "uid");
			String devid = AjaxHellper.getHttpParam(request(),"devid");
			String wdhjy = AjaxHellper.getHttpParam(request(),"wdhjy");
			String appversion = AjaxHellper.getHttpParam(request(),"appversion");
			String marketCode = AjaxHellper.getHttpParam(request(),"marketCode");
			String cid = AjaxHellper.getHttpParam(request(), "cid");
			String key = AjaxHellper.getHttpParam(request(), "key")==null?"": AjaxHellper.getHttpParam(request(), "key");
			String page = AjaxHellper.getHttpParam(request(), "page");
			AppPadChannelProVO result = new AppPadChannelProVO();
			if (!StringUtil.checkMd5(devid, wdhjy,appversion))
			{
				result.setStatus("0");
				result.setMsg("校验失败");
				return ok(Json.toJson(result));
			}
			List<AppPadChannelProVO.channelInfo> cList =new ArrayList<AppPadChannelProVO.channelInfo>();
			result.setStatus("1");
			result.setMsg("");
			result.setEndflag("0");
			if(Numbers.parseInt(page, 0)==0){
				List<PadChannelPro> bannerList = appPadService.getPadChannelBannerList(cid);
				if(!bannerList.isEmpty()){
					AppPadChannelProVO.channelInfo bannerInfo = new AppPadChannelProVO.channelInfo();
					bannerInfo.setMid("1");
					bannerInfo.setMtype("0");
					List<AppPadChannelProVO.productInfo> bannerInApiList = new ArrayList<AppPadChannelProVO.productInfo>(); 
					for(PadChannelPro padChannelPro : bannerList){
						AppPadChannelProVO.productInfo banner = new AppPadChannelProVO.productInfo();
						banner.img = padChannelPro.getImgurl();
						banner.linkurl = padChannelPro.getLinkurl();
						bannerInApiList.add(banner);
					}
					bannerInfo.setProductlist(bannerInApiList);
					cList.add(bannerInfo);
				}
			}
			
			Integer i=0;
			for(i=0;i<10;i++){
				AppPadChannelProVO.channelInfo cInfo = new AppPadChannelProVO.channelInfo();
				int dataPage = Numbers.parseInt(page, 0)*10+i;
				cInfo = appPadService.getPadChannelProList(uid, cid, key,String.valueOf(dataPage),"2");
				if (cInfo.getProductlist().size()<2){
					if(cInfo.getProductlist().size()>0)
					{
						cList.add(cInfo);
					}
					result.setEndflag("1");
					break;
				}else{
					cList.add(cInfo);
				}
			}
			result.setChannellist(cList);
			return ok(Json.toJson(result));
		}
		
		public Result pad_search_list(){
			response().setContentType("application/json;charset=utf-8");
			String uid = AjaxHellper.getHttpParam(request(), "uid");
			String devid = AjaxHellper.getHttpParam(request(),"devid");
			String wdhjy = AjaxHellper.getHttpParam(request(),"wdhjy");
			String appversion = AjaxHellper.getHttpParam(request(),"appversion");
			String marketCode = AjaxHellper.getHttpParam(request(),"marketCode");
			//String cid = AjaxHellper.getHttpParam(request(), "cid");
			String key = AjaxHellper.getHttpParam(request(), "key")==null?"": AjaxHellper.getHttpParam(request(), "key");
			String page = AjaxHellper.getHttpParam(request(), "page");
			AppPadChannelProVO result = new AppPadChannelProVO();
			if (!StringUtil.checkMd5(devid, wdhjy,appversion))
			{
				result.setStatus("0");
				result.setMsg("校验失败");
				return ok(Json.toJson(result));
			}
			List<AppPadChannelProVO.channelInfo> cList =new ArrayList<AppPadChannelProVO.channelInfo>();
			result.setStatus("1");
			result.setMsg("");
			result.setEndflag("0");
			Integer i=0;
			for(i=0;i<10;i++){
				AppPadChannelProVO.channelInfo cInfo = new AppPadChannelProVO.channelInfo();
				int dataPage = Numbers.parseInt(page, 0)*10+i;
				cInfo = appPadService.getPadSearchProList(uid, "0", key,String.valueOf(dataPage),"3");
				if (cInfo.getProductlist().size()<3){
					if(cInfo.getProductlist().size()>0)
					{
						cList.add(cInfo);
					}
					result.setEndflag("1");
					break;
				}else{
					cList.add(cInfo);
				}
			}
			result.setChannellist(cList);
			return ok(Json.toJson(result));
		}
		
		public Result pad_product_detail(){
			response().setContentType("application/json;charset=utf-8");
			String uid = AjaxHellper.getHttpParam(request(), "uid");
			String devid = AjaxHellper.getHttpParam(request(),"devid");
			String wdhjy = AjaxHellper.getHttpParam(request(),"wdhjy");
			String appversion = AjaxHellper.getHttpParam(request(),"appversion");
			String marketCode = AjaxHellper.getHttpParam(request(),"marketCode");
			String pid = AjaxHellper.getHttpParam(request(), "pid");
			String domain = StringUtil.getPICDomain();
			appPadProVO result = new appPadProVO();
			if (!StringUtil.checkMd5(devid, wdhjy,appversion))
			{
				result.setStatus("0");
				result.setMsg("校验失败");
				return ok(Json.toJson(result));
			}
			result.setStatus("1");
			result.setMsg("");
			List<String> strList = new ArrayList<String>();
			strList=appPadService.getPadDimensionalimg(uid, pid);
			String qrcodeimg="";
			String rmbpricestr="";
			if (strList.size()>0)
			{
				qrcodeimg=strList.get(0);
				rmbpricestr=strList.get(1);
			}
			List<appPadProVO.productInfo> proList= new ArrayList<appPadProVO.productInfo>();
			Product product = productService.getProductById(Numbers.parseLong(pid, 0L));
			if(product == null ){
				result.setStatus("2");
				return ok(Json.toJson(result));
			}
			List<Product> productList = productService.queryProductListByPpId(product.getPpid());
			for(Product productTemp:productList){
				appPadProVO.productInfo pInfo = new appPadProVO.productInfo();
				pInfo.title = productTemp.getTitle();
				pInfo.subtitle = productTemp.getSubtitle();
				pInfo.price = rmbpricestr;
				pInfo.listprice = "专柜价¥"+productTemp.getChinaprice().intValue()+"";
				double price = Numbers.parseDouble(rmbpricestr,0L);
				BigDecimal b= new BigDecimal(price/productTemp.getChinaprice()).setScale(2,RoundingMode.CEILING);
				if(b.doubleValue()>0)
				{
					String distinct=b.multiply(new BigDecimal(10)).setScale(1).toString();
					if(distinct.endsWith(".0")){
						distinct = distinct.replace(".0","");
					}
					pInfo.discount =distinct+"折";
				}else{
					pInfo.discount ="";
				}
				pInfo.weight =  productTemp.getWeight()+"Kg";
			
			    Fromsite fromsite = productService.queryFnamyByFromSite(productTemp.getFromsite());
			    String fname =  fromsite.getName();
			    String fromsitemsg =  "";
				
				if ("嗨个购".equals(fname))
				{
					fromsitemsg=fname+"国内仓发货（5天内到货）";
				}
				else{
					if ("日本亚马逊".equals(fname)){
						fromsitemsg=fname+"发货（20天左右到货）";
					}else{
						fromsitemsg=fname+"发货（15天左右到货）";
					}
				}
				pInfo.fromsite=fromsitemsg;                
				pInfo.fromsiteimg=fromsite.getImg(); 
				
				if (!StringUtils.isBlank(productTemp.getNationalFlag()))
				{
					pInfo.nationalflagimg = domain+"/pimgs/site/"+productTemp.getNationalFlag().replace(".png", "-app.png");
				}else{
					pInfo.nationalflagimg = "";
				}
				
				switch(productTemp.getNationalFlag()){
				case "usa.png":
					pInfo.nationalflag="美国直供";
					break;
				case "jpn.png":
					pInfo.nationalflag="日本直供";
					break;
				case "kor.png":
					pInfo.nationalflag="韩国直供";
					break;
				case "aus.png":
					pInfo.nationalflag="澳大利亚直供";
					break;
				case "ger.png":
					pInfo.nationalflag="德国直供";
					break;
				case "phi.png":
					pInfo.nationalflag="菲律宾直供";
					break;
				case "mys.png":
					pInfo.nationalflag="马来西亚直供";
					break;
				case "bga.png":
					pInfo.nationalflag="保加利亚直供";
					break;
				default:
					pInfo.nationalflag="";
					break;
				}
				pInfo.discountdesc="劲爆价";
				pInfo.linkurl = "pDe://pid="+productTemp.getPid();
				pInfo.html5url="http://ht.neolix.cn/www/productDetails.php?pid="+productTemp.getSkucode();
				pInfo.pid=productTemp.getPid()+"";
				String nstockStr="";
				pInfo.soldoutimg="";
				if (productTemp.getNstock()<=0){
					nstockStr="0";
					pInfo.soldoutimg=domain+"/pimgs/site/sellout-app.png";
				}else{
					nstockStr=productTemp.getNstock()+"";
				}
				pInfo.nstock=nstockStr;
				pInfo.qrcodeimg=qrcodeimg;
				String wayremark = productTemp.getWayremark();
			    String[] wayremarkArray = wayremark.split("_");
			    if(wayremarkArray!=null && wayremarkArray.length>0){
			    	
			    	pInfo.logisticsdesc="由"+wayremarkArray[0]+"发往"+wayremarkArray[1]; 
			    }
				pInfo.logisticsfee="包邮";
				String img="";
			    List<String> imgList = new ArrayList<String>();
			    for(Product_images image :productService.getProductImages(productTemp.getPid())){
			    	img=image.getFilename();
			    	break;
			    }   
				pInfo.img = img;
				proList.add(pInfo);
			}
			result.setProductlist(proList);
			return ok(Json.toJson(result));
		}
		
		
		
	
	
	
}
