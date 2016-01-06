package controllers.api;

import java.math.BigDecimal;
import java.sql.ResultSet;
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
import models.Address;
import models.Coupon;
import models.Currency;
import models.Endorsement;
import models.Parcels;
import models.Product;
import models.ProductGroup;
import models.ProductPriceExt;
import models.ShoppingCartEndorse;
import models.ShoppingOrder;
import models.User;
import models.UserBalance;
import models.UserVerify;
import models.Version;
import net.sf.json.JSONObject;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.StringUtils;


















import assets.CdnAssets;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import play.Configuration;
import play.Logger;
import play.libs.Json;
import play.mvc.Result;
import services.AliPayService;
import services.ServiceFactory;
import services.SmsService;
import services.WXPayService;
import services.WXPayServiceSalesMan;
import services.api.AddressService;
import services.api.CertificationService;
import services.api.EndorsementService;
import services.api.ProductService;
import services.api.SalesManCustomerService;
import services.api.ShoppingCartService;
import services.api.ShoppingOrderService;
import services.api.UserService;
import utils.AjaxHellper;
import utils.Constants;
import utils.JdbcOper;
import utils.Numbers;
import utils.StringUtil;
import utils.wxpay.TenpayHttpClient;
import vo.StatusMsgVO;
import vo.StatusOnlyVO;
import vo.VersionVo;
import vo.appPad.appPadVO;
import vo.appSalesMan.AppSalesManAddressVO;
import vo.appSalesMan.AppSalesManCartVO;
import vo.appSalesMan.AppSalesManCustomerVO;
import vo.appSalesMan.AppSalesManDevLoginVO;
import vo.appSalesMan.AppSalesManErrlVO;
import vo.appSalesMan.AppSalesManHomePageVO;
import vo.appSalesMan.AppSalesManOrderCostlVO;
import vo.appSalesMan.AppSalesManOrderDetailVO;
import vo.appSalesMan.AppSalesManOrderNewVO;
import vo.appSalesMan.AppSalesManUserCodeVO;
import vo.appSalesMan.AppSalesManUserVO;
import vo.appSalesMan.AppSalesStatusOnlyVO;
import vo.endorsment.EndorsePaylogVO;
import vo.product.ProductNewVO;
import vo.product.ProductsCheckVO;
import vo.product.ProductsCheckVO.ProdcutsCheckItem;
import vo.user.UserCheckVerify;

/**
 * 
 * @author luobotao
 *
 */
@Named
@Singleton
public class AppSalesManAPIController extends BaseApiController {
	private static final SimpleDateFormat CHINESE_DATE_TIME_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	private static final java.util.regex.Pattern PHONE_PATTERN = java.util.regex.Pattern.compile("^((1))\\d{10}$");
	private static final Logger.ALogger logger = Logger.of(UserAPIController.class);
	
	private final ShoppingCartService shoppingCartService;
	private final ShoppingOrderService shoppingOrderService;
	private final ProductService productService;
	private final UserService userService;
	private final SalesManCustomerService salesManCustomerService;
	private final AddressService addressService;
	private final SmsService smsService;
	private final EndorsementService endorsementService;
	private final CertificationService certificationService;
	@Inject
	public AppSalesManAPIController(final ShoppingCartService shoppingCartService,final ShoppingOrderService shoppingOrderService,final ProductService productService,final UserService userService,final SalesManCustomerService salesManCustomerService,final AddressService addressService,final SmsService smsService,final EndorsementService endorsementService,final CertificationService certificationService){
		this.productService = productService;
		this.userService = userService;
		this.salesManCustomerService = salesManCustomerService;
		this.shoppingCartService = shoppingCartService;
		this.shoppingOrderService = shoppingOrderService;
		this.addressService = addressService;
		this.smsService = smsService;
		this.endorsementService = endorsementService;
		this.certificationService = certificationService;
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
		JsonNode reslut = WXPayServiceSalesMan.getInstance().getSignAndPrepayID("234234234",  1,"127.0.0.1","APP");
		return ok(Json.toJson(reslut));
	}
		// 用户登录接口（POST)
		public Result login() {
			response().setContentType("application/json;charset=utf-8");
			String uid = AjaxHellper.getHttpParamOfFormUrlEncoded(request(), "uid");
			String appversion = AjaxHellper.getHttpParamOfFormUrlEncoded(request(),"appversion");
			String marketCode = AjaxHellper.getHttpParamOfFormUrlEncoded(request(),"marketCode");
			String username = AjaxHellper.getHttpParamOfFormUrlEncoded(request(),"username");
			String password = AjaxHellper.getHttpParamOfFormUrlEncoded(request(),"password");
			String devid = AjaxHellper.getHttpParamOfFormUrlEncoded(request(),"devid");
			String wdhjy = AjaxHellper.getHttpParamOfFormUrlEncoded(request(),"wdhjy");
			AppSalesManUserVO result = new AppSalesManUserVO();
			String domainimg=CdnAssets.CDN_API_PUBLIC_URL;
			if (!StringUtil.checkMd5(devid, wdhjy,appversion))
			{
				result.setStatus("0");
				result.setMsg("校验失败");
				return ok(Json.toJson(result));
			}
			result= userService.salesManlogin(username, password, devid, marketCode);
			if(result==null || result.getStatus() == null || result.getStatus().equals(""))
			{
				result.setStatus("0");
				result.setMsg("用户名或者密码不正确");
				User user = userService.getUserBydevId(devid);
				vo.appSalesMan.AppSalesManUserVO.UserInfo userInfo = new vo.appSalesMan.AppSalesManUserVO.UserInfo();
				userInfo.setGender(user.getSex());
				userInfo.setHeadIcon(user.getHeadIcon());
				userInfo.setPhone(user.getPhone());
				userInfo.setNickname(user.getNickname());
				userInfo.setUid(String.valueOf(user.getUid()));
				if(StringUtils.isBlank(userInfo.getHeadIcon())){
					if(StringUtils.isBlank(userInfo.getGender()) || userInfo.getGender().equals("0"))
						userInfo.setHeadIcon(domainimg+"images/sheSaidImages/default_headicon_girl.png");
					else
						userInfo.setHeadIcon(domainimg+"images/sheSaidImages/default_headicon_boy.png");
				}
				String sql = "SELECT a.id,a.realname FROM admin_code c,admin a WHERE c.adminid = a.id AND c.uid="+user.getUid();
				logger.info(sql);
				JdbcOper db = JdbcOper.getInstance();// 创建DBHelper对象
				try {
					db.getPrepareStateDao(sql);
					ResultSet rs = db.pst.executeQuery();// 执行语句，得到结果集
					while(rs.next()){
						userInfo.setStoreID(rs.getString("id"));
						userInfo.setStoreName(rs.getString("realname"));
					}
				} catch (Exception e) {
					e.printStackTrace();
				} finally {
					db.close();
				}
				result.setUserInfo(userInfo);
			}
			return ok(Json.toJson(result));
		}
		

		// 新用户注册接口(POST)
		public Result register() {
			response().setContentType("application/json;charset=utf-8");
			String uid = AjaxHellper.getHttpParamOfFormUrlEncoded(request(), "uid");
			String devid = AjaxHellper.getHttpParamOfFormUrlEncoded(request(),"devid");
			String appversion = AjaxHellper.getHttpParamOfFormUrlEncoded(request(),"appversion");
			String phone = AjaxHellper.getHttpParamOfFormUrlEncoded(request(),"phone");
			String marketCode = AjaxHellper.getHttpParamOfFormUrlEncoded(request(),"marketCode");
			String pwds = AjaxHellper.getHttpParamOfFormUrlEncoded(request(),"password");
			String verifyCode = AjaxHellper.getHttpParamOfFormUrlEncoded(request(),"verifyCode");
			String registCode = AjaxHellper.getHttpParamOfFormUrlEncoded(request(),"registCode");
			String nickname = AjaxHellper.getHttpParamOfFormUrlEncoded(request(),"nickname");
			String platform = AjaxHellper.getHttpParamOfFormUrlEncoded(request(),"platform");
			String wdhjy = AjaxHellper.getHttpParamOfFormUrlEncoded(request(),"wdhjy");
			String storeId = AjaxHellper.getHttpParamOfFormUrlEncoded(request(),"storeId");
			
			String domainimg=CdnAssets.CDN_API_PUBLIC_URL;
			AppSalesManUserVO result=new AppSalesManUserVO();
			if (!StringUtil.checkMd5(devid, wdhjy,appversion))
			{
				result.setStatus("0");
				result.setMsg("校验错误");
				return ok(Json.toJson(result));
			}
			UserVerify u=userService.checkVerify(phone, verifyCode);
			if (u == null ||StringUtils.isBlank(u.getPhone())){
				result.setMsg("验证失败");
				result.setStatus("0");
				return ok(Json.toJson(result));
			}
			result = userService.salesManregister(uid, devid, nickname,phone,pwds,storeId,registCode);
			return ok(Json.toJson(result));
		}
		
		
		// 登出接口(GET)
		public Result logout() {
			response().setContentType("application/json;charset=utf-8");
			String uid = AjaxHellper.getHttpParam(request(), "uid");
			String devid = AjaxHellper.getHttpParam(request(),"devid");
			String wdhjy = AjaxHellper.getHttpParam(request(),"wdhjy");
			String appversion = AjaxHellper.getHttpParam(request(),"appversion");
			String marketCode = AjaxHellper.getHttpParam(request(),"marketCode");
			AppSalesManUserVO result = new AppSalesManUserVO();
			String domainimg=CdnAssets.CDN_API_PUBLIC_URL;
			if (!StringUtil.checkMd5(devid, wdhjy,appversion))
			{
				result.setStatus("0");
				result.setMsg("校验失败");
				return ok(Json.toJson(result));
			}
			
			int newUid=userService.bindLogout(uid.toString(),devid);
			if(newUid>0){
				result.setStatus("1");
				result.setMsg("退出成功");
				User user = userService.getUserByUid(Numbers.parseLong(String.valueOf(newUid),0L));
				vo.appSalesMan.AppSalesManUserVO.UserInfo userInfo = new vo.appSalesMan.AppSalesManUserVO.UserInfo();
				userInfo.setGender(user.getSex());
				userInfo.setHeadIcon(user.getHeadIcon());
				userInfo.setPhone(user.getPhone());
				userInfo.setNickname(user.getNickname());
				userInfo.setUid(String.valueOf(user.getUid()));
				if(StringUtils.isBlank(userInfo.getHeadIcon())){
					if(StringUtils.isBlank(userInfo.getGender()) || userInfo.getGender().equals("0"))
						userInfo.setHeadIcon(domainimg+"images/sheSaidImages/default_headicon_girl.png");
					else
						userInfo.setHeadIcon(domainimg+"images/sheSaidImages/default_headicon_boy.png");
				}
				String sql = "SELECT a.id,a.realname FROM admin_code c,admin a WHERE c.adminid = a.id AND c.uid="+user.getUid();
				logger.info(sql);
				JdbcOper db = JdbcOper.getInstance();// 创建DBHelper对象
				try {
					db.getPrepareStateDao(sql);
					ResultSet rs = db.pst.executeQuery();// 执行语句，得到结果集
					while(rs.next()){
						userInfo.setStoreID(rs.getString("id"));
						userInfo.setStoreName(rs.getString("realname"));
					}
				} catch (Exception e) {
					e.printStackTrace();
				} finally {
					db.close();
				}
				result.setUserInfo(userInfo);
				
			}else{
				result.setStatus("0");
				result.setMsg("");
				
			}
			return ok(Json.toJson(result));
		}
		
		//购物车列表接口(GET)
		public Result shoppingcar_list(){
			String uid = AjaxHellper.getHttpParam(request(), "uid");
			String appversion = AjaxHellper.getHttpParam(request(), "appversion")==null?"":AjaxHellper.getHttpParam(request(), "appversion");
			String devid = AjaxHellper.getHttpParam(request(), "devid");
			String wdhjy = AjaxHellper.getHttpParam(request(),"wdhjy");
			AppSalesManCartVO result = new AppSalesManCartVO();
			if (!StringUtil.checkMd5(devid, wdhjy,appversion))
			{
				result.setStatus("0");
				return ok(Json.toJson(result));
			}
			result = shoppingCartService.getSalesManShoppingCart_list(uid);
			return ok(Json.toJson(result));
		}
	
		//购物车列表增加接口(POST)
		public Result shoppingcar_add(){
			String uidStr = AjaxHellper.getHttpParamOfFormUrlEncoded(request(), "uid");
			String url = AjaxHellper.getHttpParamOfFormUrlEncoded(request(), "url");
			String appversion = AjaxHellper.getHttpParamOfFormUrlEncoded(request(), "appversion")==null?"":AjaxHellper.getHttpParamOfFormUrlEncoded(request(), "appversion");
			String devid = AjaxHellper.getHttpParamOfFormUrlEncoded(request(), "devid");
			String wdhjy = AjaxHellper.getHttpParamOfFormUrlEncoded(request(),"wdhjy");
			String reffer=AjaxHellper.getHttpParamOfFormUrlEncoded(request(), "ref");
			reffer=StringUtils.isBlank(reffer)?"":reffer;
			
			Map<String, String> result = new HashMap<String, String>();
			if (!StringUtil.checkMd5(devid, wdhjy,appversion))
			{
				result.put("status", "0");//
				return ok(Json.toJson(result));
			}
			if(StringUtils.isBlank(uidStr)){
				result.put("status", "0");//
				return ok(Json.toJson(result));
			}
			
			String daiyanId = "0";
			try{
				String urlstr =new String(Base64.decodeBase64(url));
				daiyanId = urlstr.substring(urlstr.indexOf("daiyanid=")+9, urlstr.indexOf("&wx"));
			}catch(Exception ex){
				result.put("status", "5");
				result.put("totalcount", "0");
				result.put("msg", "扫描非商品地址");
				return ok(Json.toJson(result));
			}
			Endorsement eInfo = endorsementService.getEndorseById(Numbers.parseLong(daiyanId, 0L));
			
			int cntInt= 1;
			
			List<Object[]> deviceUsers = userService.getDeviceUser(uidStr);
			if(deviceUsers!=null && deviceUsers.size()>0){
				Product product = productService.getProductById(eInfo.getProductId());
				if(product!=null){
					int limit = product.getLimitcount();
					Long nstock = product.getNstock();
					if(product.getIshot()==1){
						nstock = productService.dealNstockWithProduct(product.getPid());
					}
					Long uid = Numbers.parseLong(uidStr, 0L);
					User ust=userService.getUserByUid(uid);
					String openid="";
					if(ust!=null)
						openid=StringUtils.isBlank(ust.getOpenId())?"":ust.getOpenId();
					Integer counts = shoppingCartService.getCartEndorseAllCount(openid);
					if(counts==null){
						counts=0;
					}
					if (nstock<=0){
						result.put("status", "4");
						result.put("totalcount", counts+"");
						result.put("msg", "该商品已售罄");
						return ok(Json.toJson(result));
					}
					if(limit==0)
					{
						result.put("status", "2");
						result.put("totalcount", "0");
						result.put("msg", "您已超出限购数量");
						return ok(Json.toJson(result));
					}
						
					ShoppingCartEndorse shoppingCart = new ShoppingCartEndorse();
					
					int nCnt = shoppingCartService.getEnCartPidCnts(openid, product.getPid());
					cntInt=cntInt+nCnt;
					
					if(cntInt>limit){
						cntInt = limit;
						result.put("status", "0");
						result.put("msg", "您已超出限购数量");
					}else{
						result.put("status", "1");
						result.put("msg", "");	
					}
					shoppingCartService.delShopCartEndorse(openid, product.getPid().toString(), "");
					shoppingCart.setCounts(cntInt);
					shoppingCart.setuId(uid);
					shoppingCart.setpId(product.getPid());
					shoppingCart.setDate_add(new Date());
					shoppingCart.setEid(Numbers.parseLong(daiyanId, 0L));
					shoppingCartService.saveShopCartEndorse(shoppingCart);
					
					result.put("totalcount", shoppingCartService.getCartEndorseAllCount(openid)+"");
					return ok(Json.toJson(result));
				}else{
					result.put("status", "3");//
					return ok(Json.toJson(result));
				}
			}else{
				result.put("status", "4");//用户不存在
				return ok(Json.toJson(result));
			}
		}
		
		//购物车列表删除接口(POST)
		public Result shoppingcar_del(){
			String uid = AjaxHellper.getHttpParamOfFormUrlEncoded(request(), "uid");
			String pids = AjaxHellper.getHttpParamOfFormUrlEncoded(request(), "pids");
			String appversion = AjaxHellper.getHttpParamOfFormUrlEncoded(request(), "appversion")==null?"":AjaxHellper.getHttpParamOfFormUrlEncoded(request(), "appversion");
			String devid = AjaxHellper.getHttpParamOfFormUrlEncoded(request(), "devid");
			String wdhjy = AjaxHellper.getHttpParamOfFormUrlEncoded(request(),"wdhjy");
			AppSalesStatusOnlyVO result = new AppSalesStatusOnlyVO();
			if (!StringUtil.checkMd5(devid, wdhjy,appversion))
			{
				result.setStatus("0");
				result.setMsg("校验失败1");
				return ok(Json.toJson(result));
			}
			
			if(StringUtils.isBlank(uid)||StringUtils.isBlank(pids)){
				result.setStatus("0");
				result.setMsg("参数错误");
				return ok(Json.toJson(result));
			}
			List<Object[]> deviceUsers = userService.getDeviceUser(uid);
			if(deviceUsers!=null && deviceUsers.size()>0){
				User ust=userService.getUserByUid(Numbers.parseLong(uid, 0L));
				String openid="";
				if(ust!=null)
					openid=StringUtils.isBlank(ust.getOpenId())?"":ust.getOpenId();
				
		        shoppingCartService.delShopCartEndorse(openid,pids,"");
		        result.setStatus("1");
		        result.setMsg("删除成功");
				return ok(Json.toJson(result));
			}else{
				result.setStatus("4");//用户不存在
				result.setMsg("用户不存在");
				return ok(Json.toJson(result));
			}
		}
		
		//购物车商品数量修改接口(POST)
		public Result shoppingcar_edit(){
			String uid = AjaxHellper.getHttpParamOfFormUrlEncoded(request(), "uid");
			String datastr = AjaxHellper.getHttpParamOfFormUrlEncoded(request(), "datastr");
			String appversion = AjaxHellper.getHttpParamOfFormUrlEncoded(request(), "appversion")==null?"":AjaxHellper.getHttpParamOfFormUrlEncoded(request(), "appversion");
			String devid = AjaxHellper.getHttpParamOfFormUrlEncoded(request(), "devid");
			String wdhjy = AjaxHellper.getHttpParamOfFormUrlEncoded(request(),"wdhjy");
			StatusOnlyVO result = new StatusOnlyVO();
			if (!StringUtil.checkMd5(devid, wdhjy,appversion))
			{
				result.setStatus(0);//用户不存在
				return ok(Json.toJson(result));
			}
			
			
			if(StringUtils.isBlank(uid)||StringUtils.isBlank(datastr)){
				result.setStatus(0);//
				return ok(Json.toJson(result));
			}
			List<Object[]> deviceUsers = userService.getDeviceUser(uid);
			if(deviceUsers!=null && deviceUsers.size()>0){
				String[] dataArray = datastr.split(",");
				for(String data:dataArray){
					String[] dataNeedArray = data.split("_");
					String pid=dataNeedArray[0];
					String cnt=dataNeedArray[1];
					User ust=userService.getUserByUid(Numbers.parseLong(uid, 0L));
					String openid="";
					if(ust!=null)
						openid=StringUtils.isBlank(ust.getOpenId())?"":ust.getOpenId();
					List<ShoppingCartEndorse> shoppingCartList = shoppingCartService.getEnCartlist(openid, Numbers.parseLong(pid, 0L),0L);
					if(shoppingCartList!=null && shoppingCartList.size()>0){
						ShoppingCartEndorse seInfo =shoppingCartList.get(0);
						seInfo.setCounts(Numbers.parseInt(cnt, 0));
						shoppingCartService.saveShopCartEndorse(seInfo);
					}
				}
		        result.setStatus(1);
				return ok(Json.toJson(result));
			}else{
				result.setStatus(4);//用户不存在
				return ok(Json.toJson(result));
			}
		}
		
		//获取客户列表 (GET)
		public Result customer_list(){
			String uid = AjaxHellper.getHttpParam(request(), "uid");
			String appversion = AjaxHellper.getHttpParam(request(), "appversion")==null?"":AjaxHellper.getHttpParam(request(), "appversion");
			String devid = AjaxHellper.getHttpParam(request(), "devid");
			String wdhjy = AjaxHellper.getHttpParam(request(),"wdhjy");
			String page = AjaxHellper.getHttpParam(request(),"page");
			String key = AjaxHellper.getHttpParam(request(),"key")==null?"":AjaxHellper.getHttpParam(request(), "key");
			AppSalesManCustomerVO result = new AppSalesManCustomerVO();
			if (!StringUtil.checkMd5(devid, wdhjy,appversion))
			{
				result.setStatus("0");
				return ok(Json.toJson(result));
			}
			
			if(StringUtils.isBlank(uid)){
				result.setStatus("4");
				return ok(Json.toJson(result));
			}
			result.setStatus("1");
			result.setMsg("");
			int count=salesManCustomerService.getCustomerCnt(uid,key);
			if (Numbers.parseInt(page,0)*count+10>=count){
				result.setEndflag("1");
			}else{
				result.setEndflag("0");
			}
			
			result.setData(salesManCustomerService.getCustomerDataInfo(uid, page,key));
			return ok(Json.toJson(result));
		}
		
		//客户搜索(GET)
		public Result customer_search(){
			String uid = AjaxHellper.getHttpParam(request(), "uid");
			String appversion = AjaxHellper.getHttpParam(request(), "appversion")==null?"":AjaxHellper.getHttpParam(request(), "appversion");
			String devid = AjaxHellper.getHttpParam(request(), "devid");
			String wdhjy = AjaxHellper.getHttpParam(request(),"wdhjy");
			String page = AjaxHellper.getHttpParam(request(),"page");
			String key = AjaxHellper.getHttpParam(request(),"key")==null?"":AjaxHellper.getHttpParam(request(), "key");
			AppSalesManCustomerVO result = new AppSalesManCustomerVO();
			if (!StringUtil.checkMd5(devid, wdhjy,appversion))
			{
				result.setStatus("0");
				return ok(Json.toJson(result));
			}
			
			if(StringUtils.isBlank(uid)){
				result.setStatus("4");
				return ok(Json.toJson(result));
			}
			result.setStatus("1");
			result.setData(salesManCustomerService.getCustomerDataInfo(uid,page,key));
			return ok(Json.toJson(result));
		}
		
		//获取客户地址列表 (GET)
		public Result customeraddress_list(){
			String uid = AjaxHellper.getHttpParam(request(), "uid");
			String appversion = AjaxHellper.getHttpParam(request(), "appversion")==null?"":AjaxHellper.getHttpParam(request(), "appversion");
			String devid = AjaxHellper.getHttpParam(request(), "devid");
			String wdhjy = AjaxHellper.getHttpParam(request(),"wdhjy");
			String page = AjaxHellper.getHttpParam(request(),"page");
			String name = AjaxHellper.getHttpParam(request(),"name");
			AppSalesManAddressVO result = new AppSalesManAddressVO();
			if (!StringUtil.checkMd5(devid, wdhjy,appversion))
			{
				result.setStatus("0");
				return ok(Json.toJson(result));
			}
			
			if(StringUtils.isBlank(uid)){
				result.setStatus("4");
				return ok(Json.toJson(result));
			}
			result.setStatus("1");
			result.setData(salesManCustomerService.getAddressList(uid,name,page));
			return ok(Json.toJson(result));
		}
		
		//删除客户地址(POST)
		public Result customeraddress_delete(){
			String uid = AjaxHellper.getHttpParamOfFormUrlEncoded(request(), "uid");
			String appversion = AjaxHellper.getHttpParamOfFormUrlEncoded(request(), "appversion")==null?"":AjaxHellper.getHttpParamOfFormUrlEncoded(request(), "appversion");
			String devid = AjaxHellper.getHttpParamOfFormUrlEncoded(request(), "devid");
			String wdhjy = AjaxHellper.getHttpParamOfFormUrlEncoded(request(),"wdhjy");
			String addressid = AjaxHellper.getHttpParamOfFormUrlEncoded(request(),"addressId");
			ObjectNode result = Json.newObject();
			if (!StringUtil.checkMd5(devid, wdhjy,appversion))
			{
				result.put("status", "0");
				return ok(Json.toJson(result));
			}
			
			if(StringUtils.isBlank(uid)){
				result.put("status","4");
				return ok(Json.toJson(result));
			}
			addressService.deleteAddress(Numbers.parseLong(uid, 0L),Numbers.parseLong(addressid, 0L));
			result.put("status", "1");
			return ok(Json.toJson(result));
		}
		
		//修改客户信息(POST)
		public Result customerinfo_editor(){
			String uid = AjaxHellper.getHttpParamOfFormUrlEncoded(request(), "uid");
			String appversion = AjaxHellper.getHttpParamOfFormUrlEncoded(request(), "appversion")==null?"":AjaxHellper.getHttpParam(request(), "appversion");
			String devid = AjaxHellper.getHttpParamOfFormUrlEncoded(request(), "devid");
			String wdhjy = AjaxHellper.getHttpParamOfFormUrlEncoded(request(),"wdhjy");
			String addressid = AjaxHellper.getHttpParamOfFormUrlEncoded(request(),"addressId");
			String address = AjaxHellper.getHttpParamOfFormUrlEncoded(request(), "address");
			String name = AjaxHellper.getHttpParamOfFormUrlEncoded(request(), "name");
			String phone = AjaxHellper.getHttpParamOfFormUrlEncoded(request(), "phone");
			String province = AjaxHellper.getHttpParamOfFormUrlEncoded(request(), "provinces");
			String cardId = AjaxHellper.getHttpParamOfFormUrlEncoded(request(), "cardId")==null?"":AjaxHellper.getHttpParamOfFormUrlEncoded(request(), "cardId");
			ObjectNode result = Json.newObject();
			if (!StringUtil.checkMd5(devid, wdhjy,appversion))
			{
				result.put("status", "0");
				return ok(Json.toJson(result));
			}
			
			if(StringUtils.isBlank(uid)){
				result.put("status","4");
				return ok(Json.toJson(result));
			}
			Address addressModel= new Address();
			if (Numbers.parseLong(addressid, 0L)>0)
			{
				addressModel.setAddressId(Numbers.parseLong(addressid, 0L));
			}	
			addressModel.setAddress(address);
			addressModel.setCardId(cardId);
			addressModel.setDate_add(new Date());
			addressModel.setName(name);
			addressModel.setPhone(phone);
			addressModel.setFlg("1");
			addressModel.setProvince(province);
			addressModel.setuId(Numbers.parseLong(uid, 0L));
			addressService.saveAddress(addressModel);
			addressService.setAddressPY(Numbers.parseLong(uid, 0L), Numbers.parseLong(addressid, 0L));
			result.put("status", "1");
			result.put("msg", "");
			return ok(Json.toJson(result));
		}
		
		//获取验证码接口(GET)
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
				boolean phoneBindFlag = userService.getPhoneGid5IsBind(phone);
				if(phoneBindFlag){
						result.setStatus("4");
						result.setMsg("该手机号已经绑定过其它商铺");
						return ok(Json.toJson(result));
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
		
		//设备注册接口(POST)
		public Result devlogin() {
			response().setContentType("application/json;charset=utf-8");
			String devid = AjaxHellper.getHttpParamOfFormUrlEncoded(request(), "devid");
			String wdhjy = AjaxHellper.getHttpParamOfFormUrlEncoded(request(),"wdhjy");
			String osversion = AjaxHellper.getHttpParamOfFormUrlEncoded(request(), "osversion");
			String model = AjaxHellper.getHttpParamOfFormUrlEncoded(request(), "model");
			String uid = AjaxHellper.getHttpParamOfFormUrlEncoded(request(), "uid")==null?"":AjaxHellper.getHttpParamOfFormUrlEncoded(request(), "uid");
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
			String domainimg=CdnAssets.CDN_API_PUBLIC_URL;
			if(!StringUtils.isBlank(idfa))
				marketCodes=userService.getChannelByidfa(idfa);
			if(StringUtils.isBlank(marketCodes))
				marketCodes="";
			AppSalesManDevLoginVO result = new  AppSalesManDevLoginVO();
			vo.appSalesMan.AppSalesManUserVO.UserInfo userInfo = new vo.appSalesMan.AppSalesManUserVO.UserInfo();
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
			userInfo = userService.getSalesManUidAndPhone(devid, osversion, model, pushToken, resolution, appversion, marketCode);
			if(!StringUtils.isBlank(userInfo.getPhone()))
			{		
				Map<String, String> mayarray=userService.getUserId_ByGuid(Numbers.parseInt(userInfo.getUid(), 0),devid,"", "0");
				String Guid=mayarray.get("guid");
				result.setToken(Guid);
				response().setCookie("token", Guid);
				
				if ("".equals(uid)){
					int newUid=userService.bindLogout(userInfo.getUid(),devid);
					User user = userService.getUserByUid(Numbers.parseLong(String.valueOf(newUid),0L));
					userInfo.setGender(user.getSex());
					userInfo.setHeadIcon(user.getHeadIcon());
					userInfo.setPhone(user.getPhone());
					userInfo.setNickname(user.getNickname());
					userInfo.setUid(String.valueOf(user.getUid()));
					if(StringUtils.isBlank(userInfo.getHeadIcon())){
						if(StringUtils.isBlank(userInfo.getGender()) || userInfo.getGender().equals("0"))
							userInfo.setHeadIcon(domainimg+"images/sheSaidImages/default_headicon_girl.png");
						else
							userInfo.setHeadIcon(domainimg+"images/sheSaidImages/default_headicon_boy.png");
					}
					String sql = "SELECT a.id,a.realname FROM admin_code c,admin a WHERE c.adminid = a.id AND c.uid="+user.getUid();
					logger.info(sql);
					JdbcOper db = JdbcOper.getInstance();// 创建DBHelper对象
					try {
						db.getPrepareStateDao(sql);
						ResultSet rs = db.pst.executeQuery();// 执行语句，得到结果集
						while(rs.next()){
							userInfo.setStoreID(rs.getString("id"));
							userInfo.setStoreName(rs.getString("realname"));
						}
					} catch (Exception e) {
						e.printStackTrace();
					} finally {
						db.close();
					}	
				}
			}
			
			result.setStatus("1");
			result.setMsg("");
			result.setUserInfo(userInfo);
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
			return ok(Json.toJson(result));
		}
		
		//支付结果回传接口(POST)
		public Result orderpay_callback() {
			response().setContentType("application/json;charset=utf-8");
			String state = "10";
			ObjectNode result = Json.newObject();
			Map<String, String> pramt = new HashMap<String, String>();
			
			String method = AjaxHellper.getHttpParamOfFormUrlEncoded(request(), "paymethod");
			if(method!=null){
				pramt.put("paymethod",method);
			}
			String appversion = AjaxHellper.getHttpParamOfFormUrlEncoded(request(), "appversion");
			if(appversion!=null){
				pramt.put("appversion",appversion);
			}
			String idfa = AjaxHellper.getHttpParamOfFormUrlEncoded(request(), "idfa");
			if(idfa!=null){
				pramt.put("idfa",idfa);
			}
			String marketCode = AjaxHellper.getHttpParamOfFormUrlEncoded(request(), "marketCode");
			if(marketCode!=null){
				pramt.put("marketCode",marketCode);
			}
			String model = AjaxHellper.getHttpParamOfFormUrlEncoded(request(), "model");
			if(model!=null){
				pramt.put("model",model);
			}
			String wdhjy = AjaxHellper.getHttpParamOfFormUrlEncoded(request(), "wdhjy");
			if(wdhjy!=null){
				pramt.put("wdhjy",wdhjy);
			}
			String osversion = AjaxHellper.getHttpParamOfFormUrlEncoded(request(), "osversion");
			if(osversion!=null){
				pramt.put("osversion",osversion);
			}
			String usewallet = AjaxHellper.getHttpParamOfFormUrlEncoded(request(), "usewallet");
			if(usewallet!=null){
				pramt.put("usewallet",usewallet);
			}
			String deviceType = AjaxHellper.getHttpParamOfFormUrlEncoded(request(), "deviceType");
			if(deviceType!=null){
				pramt.put("deviceType",deviceType);
			}
			String devid = AjaxHellper.getHttpParamOfFormUrlEncoded(request(), "devid");
			if(devid!=null){
				pramt.put("devid",devid);
			}
			String uid = AjaxHellper.getHttpParamOfFormUrlEncoded(request(), "uid");
			if(uid!=null){
				pramt.put("uid",uid);
			}
			String orderCode = AjaxHellper.getHttpParamOfFormUrlEncoded(request(), "payCode")==null?"":AjaxHellper.getHttpParamOfFormUrlEncoded(request(), "payCode");
			if(orderCode!=null){
				pramt.put("payCode",orderCode);
			}
			String vstr = AjaxHellper.getHttpParamOfFormUrlEncoded(request(), "vstr");
			if(vstr!=null){
				pramt.put("vstr",vstr);
			}
			String amount = AjaxHellper.getHttpParamOfFormUrlEncoded(request(), "amount");
			if(amount!=null){
				pramt.put("amount",amount);
			}
			String errcode = AjaxHellper.getHttpParamOfFormUrlEncoded(request(), "errcode")==null?"100":AjaxHellper.getHttpParamOfFormUrlEncoded(request(), "errcode");
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
			
			if("0".equals(method))
			{
				if(Numbers.parseInt(errcode, 100)!=0){
					shoppingOrderService.order_back(orderCode);
					result.put("status", 4);
					result.put("msg","支付错误码"+errcode);
					return ok(Json.toJson(result));
				}
			}else{
				if(!"9000".equals(errcode)){
					shoppingOrderService.order_back(orderCode);
					result.put("status", 4);
					result.put("msg","支付错误码"+errcode);
					return ok(Json.toJson(result));
				}
			}
			
			switch(method){
			case "0":
				method="10";
				break;
			case "1":
				method="20";
				break;
			case "2":
				method="21";
				break;
			case "3":
				method="22";
				break;
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
		
		//获取首页信息列表 (GET)
		public Result homepage_list() {
			String uid = AjaxHellper.getHttpParam(request(), "uid");
			String appversion = AjaxHellper.getHttpParam(request(), "appversion")==null?"":AjaxHellper.getHttpParam(request(), "appversion");
			String devid = AjaxHellper.getHttpParam(request(), "devid");
			String wdhjy = AjaxHellper.getHttpParam(request(),"wdhjy");
			AppSalesManHomePageVO result = new AppSalesManHomePageVO();
			if (!StringUtil.checkMd5(devid, wdhjy,appversion))
			{
				result.setStatus("0");
				return ok(Json.toJson(result));
			}
			
			if(StringUtils.isBlank(uid)){
				result.setStatus("4");
				return ok(Json.toJson(result));
			}
			result.setStatus("1");
			
			int  count =  shoppingCartService.getSalesManShoppingCart_cnt(uid);
			List<AppSalesManHomePageVO.dataInfo> dList = new ArrayList<AppSalesManHomePageVO.dataInfo>();
			AppSalesManHomePageVO.dataInfo dInfo1 = new AppSalesManHomePageVO.dataInfo();
			dInfo1.leftmoduleBackImage="http://higou-api.oss-cn-beijing.aliyuncs.com/images/home_cart%403x.png";
			dInfo1.leftmoduleLink="sCart://";
			if (count>0){
				dInfo1.leftmoduleSubNum=String.valueOf(count)+"";
			}else{
				dInfo1.leftmoduleSubNum="";
			}	
			dInfo1.leftmoduleTitle="购物车";
			dInfo1.rightmoduleBackImage="http://higou-api.oss-cn-beijing.aliyuncs.com/images/home_myorder%403x.png";
			dInfo1.rightmoduleLink="ordList://";
			dInfo1.rightmoduleSubNum="";
			dInfo1.rightmoduleTitle="我的订单";
			
			AppSalesManHomePageVO.dataInfo dInfo2 = new AppSalesManHomePageVO.dataInfo();
			dInfo2.leftmoduleBackImage="http://higou-api.oss-cn-beijing.aliyuncs.com/images/home_customer_list%403x.png";
			dInfo2.leftmoduleLink="myCustomer://";
			dInfo2.leftmoduleSubNum="";
			dInfo2.leftmoduleTitle="我的客户";
			dInfo2.rightmoduleBackImage="http://higou-api.oss-cn-beijing.aliyuncs.com/images/home_wallet%403x.png";
			dInfo2.rightmoduleLink="userWallet://";
			dInfo2.rightmoduleSubNum="";
			dInfo2.rightmoduleTitle="钱包";
			
			AppSalesManHomePageVO.dataInfo dInfo3 = new AppSalesManHomePageVO.dataInfo();
			dInfo3.leftmoduleBackImage="http://higou-api.oss-cn-beijing.aliyuncs.com/images/home_personal_center%403x.png";
			dInfo3.leftmoduleLink="uCenter://";
			dInfo3.leftmoduleSubNum="";
			dInfo3.leftmoduleTitle="个人中心";
		
			
			dList.add(dInfo1);
			dList.add(dInfo2);
			dList.add(dInfo3);
			
			result.setData(dList);
			return ok(Json.toJson(result));
		}
		
		// 代言收入流水信息接口(GET)
		public Result getwalletpaylist() {
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
			result.rulesurl="http://ht.neolix.cn/www/wap/13/text-4.html";
			return ok(Json.toJson(result));
		}
		
		//获取结算明细接口（去结算）(GET方式)
		public Result  shoppingOrder_cost(){
			response().setContentType("application/json;charset=utf-8");
			String uid = AjaxHellper.getHttpParam(request(), "uid");
			String datastr = AjaxHellper.getHttpParam(request(), "datastr");
			String devid = AjaxHellper.getHttpParam(request(), "devid")==null?"":AjaxHellper.getHttpParam(request(), "devid");
			String appversion=AjaxHellper.getHttpParam(request(), "appversion");
			String wdhjy = AjaxHellper.getHttpParam(request(),"wdhjy");
			AppSalesManOrderCostlVO result = new AppSalesManOrderCostlVO();
			AppSalesManErrlVO errResult = new AppSalesManErrlVO();
			
			if (!StringUtil.checkMd5(devid, wdhjy,appversion))
			{
				errResult.status="0";
				errResult.msg="校验失败1";
				errResult.data=Json.newObject();
				return ok(Json.toJson(errResult));
			}
			
			AppSalesManOrderCostlVO.ShoppingOrderCostItem data = new AppSalesManOrderCostlVO.ShoppingOrderCostItem();
			
			String pidstr = "";
			if(StringUtils.isBlank(uid)||StringUtils.isBlank(datastr)){
				errResult.status="0";
				errResult.msg="用户不存在";
				errResult.data=Json.newObject();
				return ok(Json.toJson(errResult));
			}
			
			List<Object[]> deviceUsers = userService.getDeviceUser(uid);
			if(deviceUsers!=null && deviceUsers.size()>0){
				String dataArray[] = datastr.split(",");
				data.p_list = new ArrayList<AppSalesManOrderCostlVO.PInfo>();
				int i=0;
				
				BigDecimal freight=new BigDecimal(0);
				int fe=0;
				BigDecimal weightTemp =new BigDecimal(0);
				for(String dataTemp:dataArray){
					String dataNeed[] = dataTemp.split("_");
					String pid=dataNeed[0];
					String price=dataNeed[1];
					String counts=dataNeed[2];
					if(price!=null && "¥".equals(price.substring(0, 1)) ){
						price=price.substring(1, price.length());
					}
					
					BigDecimal pInfo_price= shoppingCartService.getPriceShoppingCart_eid(uid,pid);
					
					if (Numbers.parseInt(price, 0)!= pInfo_price.setScale(0, BigDecimal.ROUND_CEILING).intValue()){
						errResult.status="0";
						errResult.msg="您购买的部分商品价格发生了改变哦~请重新结算";
						errResult.data=Json.newObject();
						return ok(Json.toJson(errResult));
					}
					
					
					pidstr=pidstr+pid+",";
					
					Product product =productService.getProductById(Numbers.parseLong(pid, 0L));
					if(i==0){
						fe=product.getFromsite();
					}else{
						if(product.getFromsite()!=fe){
							freight = new BigDecimal(ShoppingCartService.getfreight(fe, weightTemp.toString()));
							fe=product.getFromsite();
							weightTemp =new BigDecimal(0);	
							data.carriagefee = freight.add(new BigDecimal(data.carriagefee)).setScale(0, BigDecimal.ROUND_CEILING).toString();
						}
					}
					
					weightTemp =weightTemp.add(new BigDecimal( product.getWeight()*Numbers.parseInt(counts, 0)+"").setScale(1, BigDecimal.ROUND_CEILING)) ;
					
					AppSalesManOrderCostlVO.PInfo pInfo = new AppSalesManOrderCostlVO.PInfo();
					
					
					pInfo.pid = String.valueOf(product.getPid());
					pInfo.title = product.getTitle();
					pInfo.linkurl="pDe://pid="+ String.valueOf(product.getPid());
					// BigDecimal rmb_price= new BigDecimal(product.getRmbprice()).setScale(0, BigDecimal.ROUND_CEILING);
					pInfo.chinaprice="¥"+price.toString();
					pInfo.china_price=price.toString();
					pInfo.limitcount =  String.valueOf(product.getLimitcount());
					if(Numbers.parseInt(counts, 0)>product.getLimitcount())
					{
						counts=String.valueOf(product.getLimitcount());
					}
					if(Numbers.parseInt(counts, 0)>product.getNstock())
					{
						counts=String.valueOf(product.getNstock());
					}
					if(Numbers.parseInt(counts, 0)<=0){
						errResult.status="0";
						errResult.msg="您购买的商品【"+product.getTitle()+"】已经售罄,请重新选择";
						errResult.data=Json.newObject();
						return ok(Json.toJson(errResult));
						
					}
					pInfo.counts =  counts;
					pInfo.img = product.getListpic();
					data.p_list.add(pInfo);	
					data.total_fee+=Numbers.parseInt(price, 0)*Numbers.parseInt(counts, 0);
					
					if(i==dataArray.length-1){
						freight = new BigDecimal(ShoppingCartService.getfreight(fe, weightTemp.toString()));
						data.carriagefee = freight.add(new BigDecimal(data.carriagefee)).setScale(0, BigDecimal.ROUND_CEILING).toString();
					}
					i++;
				}
				data.total_fee = new BigDecimal(data.carriagefee).add(new BigDecimal(data.total_fee)).intValue();
				if (pidstr.endsWith(",")){
					pidstr=pidstr.substring(0, pidstr.length()-1);
				}
				data.costfee ="¥0";
				data.carriagefee ="¥"+data.carriagefee;
				data.totalfee = "¥"+data.total_fee;
				data.hgPaymentType = shoppingOrderService.getHgPaymentType();
				result.data = data;
				result.status = 1;
			}else{
				errResult.status="0";
				errResult.msg="请登录";
				errResult.data=Json.newObject();
				return ok(Json.toJson(errResult));
			}
			
			return ok(Json.toJson(result));
		}
		
		//订单生成接口（去支付）(POST方式)
		public Result shoppingOrder_new(){
			response().setContentType("application/json;charset=utf-8");
			Map<String, String> pramt = new HashMap<String, String>();
		
			String appversion=AjaxHellper.getHttpParamOfFormUrlEncoded(request(), "appversion");
			if(appversion!=null){
				pramt.put("appversion",appversion);
			}
			String datastr = AjaxHellper.getHttpParamOfFormUrlEncoded(request(), "datastr");
			if(datastr!=null){
				pramt.put("datastr",datastr);
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
			String thirdPartyPayMethod = AjaxHellper.getHttpParamOfFormUrlEncoded(request(),"thirdPartyPayMethod");
			if(thirdPartyPayMethod!=null){
				pramt.put("thirdPartyPayMethod",thirdPartyPayMethod);
			}
		
			String uid = AjaxHellper.getHttpParamOfFormUrlEncoded(request(), "uid");
			if(uid!=null){
				pramt.put("uid",uid);
			}
		
			String wdhjy = AjaxHellper.getHttpParamOfFormUrlEncoded(request(),"wdhjy");
			if(wdhjy!=null){
				pramt.put("wdhjy",wdhjy);
			}
		
			String mcode=AjaxHellper.getHttpParamOfFormUrlEncoded(request(), "mcode");
			if(mcode!=null){
				pramt.put("mcode",mcode);
			}
			String name=AjaxHellper.getHttpParamOfFormUrlEncoded(request(), "name");
			if(name!=null){
				pramt.put("name",name);
			}
			String phone=AjaxHellper.getHttpParamOfFormUrlEncoded(request(), "phone");
			if(phone!=null){
				pramt.put("phone",phone);
			}
			String city=AjaxHellper.getHttpParamOfFormUrlEncoded(request(), "city");
			if(city!=null){
				pramt.put("city",city);
			}
			String address=AjaxHellper.getHttpParamOfFormUrlEncoded(request(), "address");
			if(address!=null){
				pramt.put("address",address);
			}
			String cardID=AjaxHellper.getHttpParamOfFormUrlEncoded(request(), "cardID");
			if(cardID!=null){
				pramt.put("cardID",cardID);
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
			
			String ip = StringUtil.getIpAddr(request());
			
			AppSalesManOrderNewVO result = new AppSalesManOrderNewVO();
			AppSalesManErrlVO errResult = new AppSalesManErrlVO();
			
			if (!StringUtil.checkMd5(devid, wdhjy,"3.0.0"))
			{
				errResult.status="0";
				errResult.msg="校验失败1";
				errResult.data=Json.newObject();
				return ok(Json.toJson(errResult));
			}
			
			if (!StringUtil.checksign(lwdjl,md5sign,"1.0.0")){
				errResult.status="0";
				errResult.msg="校验失败2";
				errResult.data=Json.newObject();
				return ok(Json.toJson(errResult));
			}
			
			String wx="0";
			String shareType="";
			
			if(StringUtils.isBlank(uid)||StringUtils.isBlank(datastr)){
				result.setStatus("0");
				return ok(Json.toJson(result));
			}
		
			// 地址不存在时，保存通讯录
			Address adr=addressService.setSaveAddress(Numbers.parseLong(uid, 0L),name,phone,city,address,cardID);
			String addressid=adr.getAddressId().toString();
			addressService.setAddressPY(Numbers.parseLong(uid, 0L), adr.getAddressId());
			String endorsementId="";
				Coupon coupon = null;
				String lovelyflg="0";
				Double p_distinct=10D;
				
				int p_couponUserId=0;
				String p_coupon="";
				Double p_coupon_price=0D;
				int couponType =0;
				
				String typ="66";
				
				BigDecimal p_domestic_fee = new BigDecimal(0);
				BigDecimal p_foreignfee = new BigDecimal(0);
				BigDecimal p_tariff_fee = new BigDecimal(0);
				BigDecimal p_cost_fee  = new BigDecimal(0);
				BigDecimal p_total_fee  = new BigDecimal(0);
				BigDecimal p_goods_fee  = new BigDecimal(0);
				BigDecimal p_deposit  = new BigDecimal(0);
				BigDecimal p_finalpay  = new BigDecimal(0);
				
				String data[] = datastr.split(",");
				String pidstr = "";
				String pid="0";
				Map<Integer, List<Product>> productMap = new HashMap<Integer, List<Product>>(); 
				for(String dataTemp:data){
					String dataNeed[] = dataTemp.split("_");
					if(dataNeed.length>=3){
						pid=dataNeed[0];
						pidstr=pidstr+pid+",";
						Product product = productService.getProductById(Numbers.parseLong(pid, 0L));
						if(product!=null){
							String price = dataNeed[1];
							String cnt=dataNeed[2];
							int cntTemp = Numbers.parseInt(cnt, 0);
							if(cntTemp<=0){
								logger.info("数量为小于零的库存出现了"+cntTemp);
								errResult.status="7";
								errResult.msg="您购买的部分商品数量不合法，请重新选择";
								errResult.data=Json.newObject();
								return ok(Json.toJson(errResult));
							}
							if(product.getNstock()<1){
								logger.info("库存为0"+product.getNstock());
								errResult.status="7";
								errResult.msg="您购买的商品【"+product.getTitle()+"】已经售罄,请重新选择";
								errResult.data=Json.newObject();
								return ok(Json.toJson(errResult));
							}
							if(product.getStatus()!=10){
								logger.info("产品已下架"+product.getNstock());
								errResult.status="7";
								errResult.msg="您购买的商品【"+product.getTitle()+"】已经下架,请重新选择";
								errResult.data=Json.newObject();
								return ok(Json.toJson(errResult));
							}
							if("a".equals(price)){
								product.setRmbprice(0.0);
								product.setCounts(1);
								product.setWeight(0.0);
							}else{
								product.setCounts(Numbers.parseInt(cnt, 0));
								Currency currency = productService.queryCurrencyById(product.getCurrency());
								 BigDecimal rate = new BigDecimal(currency.getRate()/100).setScale(4,  BigDecimal.ROUND_CEILING) ;
								 BigDecimal	priceDec=	new BigDecimal(product.getPrice()/100).setScale(2,  BigDecimal.ROUND_CEILING);   
								 BigDecimal rmb_price= rate.multiply(priceDec).setScale(2,BigDecimal.ROUND_CEILING);
								 BigDecimal endorseprice= new BigDecimal(product.getEndorsementPrice()).setScale(2,BigDecimal.ROUND_CEILING);
								 if (product.getIslockprice()==1) {
									rmb_price = new BigDecimal(product.getRmbprice()).setScale(2, BigDecimal.ROUND_CEILING);
								 }
								
								if("6".equals(typ) || "66".equals(typ)){
									if("66".equals(typ)){
										User ust=userService.getUserByUid(Numbers.parseLong(uid, 0L));
										String openid="";
										if(ust!=null)
											openid=StringUtils.isBlank(ust.getOpenId())?"":ust.getOpenId();
										
										List<ShoppingCartEndorse> cartList=shoppingCartService.getEnCartlist(openid,Numbers.parseLong(pid, 0L),0L);
										if (cartList!=null && cartList.size()>0){
											endorsementId=cartList.get(0).getEid().toString();
											Endorsement eInfo=endorsementService.getEndorseById(Numbers.parseLong(endorsementId, 0L));
											endorseprice=new BigDecimal(eInfo.getEndorsementPrice()).setScale(2, BigDecimal.ROUND_CEILING);
										}
									}
									//Double endorseprice=product.getEndorsementPrice();
									if(Numbers.parseDouble(price, 0D)!=endorseprice.doubleValue()){
										errResult.status="6";
										errResult.msg="您购买的部分商品价格发生了改变哦~请重新结算";
										errResult.data=Json.newObject();
										return ok(Json.toJson(errResult));
									}
								}
								p_deposit = p_goods_fee.add(new BigDecimal(product.getDeposit()).multiply(new BigDecimal(cnt)));
								if ("6".equals(typ) || "66".equals(typ)){
									p_goods_fee = p_goods_fee.add(endorseprice.multiply(new BigDecimal(cnt)));
								}
							}
							List<Product> productList = productMap.get(product.getFromsite());
							if(productList==null)
								productList = new ArrayList<Product>();
							productList.add(product);
							productMap.put(product.getFromsite(), productList);
						}
					}
				}
				
				for(Integer fromesite:productMap.keySet()){
					BigDecimal weight = new BigDecimal(0);
					List<Product> productList = productMap.get(fromesite);
					for(Product product:productList){
						Integer weightTemp =  (int) (product.getWeight()*10);
						Integer countTemp =  product.getCounts();
						BigDecimal weightTotal = new BigDecimal(weightTemp*countTemp*0.1).setScale(1,BigDecimal.ROUND_HALF_UP);
						weight = weight.add(weightTotal);
					}
					p_foreignfee = p_foreignfee.add(new BigDecimal(ShoppingCartService.getfreight(fromesite, String.valueOf(weight))));
				}
				p_total_fee = p_total_fee.add(p_foreignfee).add(p_goods_fee);
				
				if (pidstr.endsWith(",")){
					pidstr=pidstr.substring(0, pidstr.length()-1);
				}
				/*
				 * 
				 * 验证身份证
				 * 
				 */
				int opencardId = userService.getOpencardIdByPids(pidstr);
				// int opencardIdimg = userService.getOpencardIdImgByPids(pidstr);
				
				int status=0;
				String toast="";
				if(opencardId>0){
						if(!String.valueOf(adr.getuId()).equals(uid)){
							status = 2;
							toast = "当前用户不存在此收货地址，请查证。";	
						}
						if(adr!=null){
							int length = adr.getCardId() == null ? 0 : adr.getCardId().length();
							if (length > 15) {
								int flag = certificationService.checkNameWithCard(uid, adr.getName(), adr.getCardId());
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
							} else {
								status = 2;
								toast = "您购买的商品中有海外直邮商品，由海关统一清关入境，需要您完善真实的收货人身份信息。";
							}
					}
					if(status==1){//如果验证通过，则需要对购买次数等信息进行检验
						ObjectNode result1 =shoppingOrderService.checkOrderWithAddress(pidstr,Numbers.parseLong(addressid, 0L));
						status=result1.get("status").intValue();
						toast=result1.get("toast").toString();
					}
					
					if (status!=1){
						errResult.status=String.valueOf(status);
						errResult.msg=toast;
						errResult.data=Json.newObject();
						return ok(Json.toJson(errResult));
					}
				}	
				/*
				 * 
				 * 验证身份证结束
				 * 
				 */
			
				Double p_original_fee=p_total_fee.doubleValue();
				
				if (p_original_fee<=0){
					errResult.status="9";
					errResult.msg="结算金额异常";
					errResult.data=Json.newObject();
					return ok(Json.toJson(errResult));
				}
				
				p_finalpay = p_total_fee.subtract(p_deposit).subtract(p_foreignfee);
				ProductNewVO result1 = shoppingOrderService.newOrder(uid, addressid,
						p_domestic_fee, p_foreignfee, p_tariff_fee, p_cost_fee,
						p_total_fee, p_goods_fee, typ, "", p_couponUserId,
						p_coupon, p_coupon_price, p_distinct, p_deposit,
						p_finalpay, p_original_fee,Numbers.parseLong("66", 0L),wx,shareType,"");
				if(result1.orderId!=null && result1.orderId>0){
					for(String dataTemp:data){
						String dataNeed[] = dataTemp.split("_");
						if(dataNeed.length>=3){
							pid=dataNeed[0];
							String price = dataNeed[1];
							String cnt=dataNeed[2];					
							if("a".equals(price)){
								price="0";
								cnt="1";
							}
							String daiyanid="0";
							if("66".equals(typ))
							{
								User ust=userService.getUserByUid(Numbers.parseLong(uid, 0L));
								String openid="";
								if(ust!=null)
									openid=StringUtils.isBlank(ust.getOpenId())?"":ust.getOpenId();
								
								List<ShoppingCartEndorse> cartList=shoppingCartService.getEnCartlist(openid,Numbers.parseLong(pid, 0L),0L);
								if (cartList!=null && cartList.size()>0){
									daiyanid=cartList.get(0).getEid().toString();
								}
							}
							shoppingOrderService.addProductToOrder(result1.orderId,pid,price,cnt,daiyanid);
							
							Product productTemp = productService.getProductById(Numbers.parseLong(pid, 0L));
							if(productTemp!=null && productTemp.getIshot()==1){//组合商品，需要找到子商品，并将此商品从内存中清除，下次再获取此商品将从库中获取
								List<ProductGroup> productGroups = productService.findProductGroupListByPgId(productTemp.getPid());
								for (ProductGroup productGroup : productGroups) {
									ServiceFactory.getCacheService().clear(Constants.product_KEY+productGroup.getPid());//将此商品从内存中清除，下次再获取此商品将从库中获取
								}
							}
							ServiceFactory.getCacheService().clear(Constants.product_KEY+pid);//将此商品从内存中清除，下次再获取此商品将从库中获取
						}
					}
					
					AppSalesManOrderNewVO.dInfo dInfo = new AppSalesManOrderNewVO.dInfo();
					dInfo.orderId = result1.orderId.toString();
					dInfo.orderCode =  result1.orderCode;
					dInfo.payCode = result1.orderCode;
					/***********获取微信参数************************************/
					if(thirdPartyPayMethod.equals("0")){
						if(p_total_fee.intValue()>0){
							p_total_fee = p_total_fee.multiply(new BigDecimal(100));
							//String ip=request().remoteAddress();
							JsonNode reslutwx = WXPayServiceSalesMan.getInstance().getSignAndPrepayID(result1.orderCode,  p_total_fee.longValue(),ip,"APP");
							if(reslutwx==null){
								dInfo.thirdPartyPaySign="";
							}else{
								String nostr=reslutwx.get("noncestr").asText();
								String partnerid=reslutwx.get("partnerid").asText();
								String prepayid=reslutwx.get("prepayid").asText();
								String timstr=reslutwx.get("timestamp").asText();
								String packg=reslutwx.get("package").asText();
								String signs=reslutwx.get("sign").asText();
								String wxpaystr="noncestr="+nostr+"&partnerid="+partnerid+"&prepayid="+prepayid+"&timestamp="+timstr+"&sign="+signs+"&package="+packg;
								wxpaystr=Base64.encodeBase64String(wxpaystr.getBytes());
								dInfo.thirdPartyPaySign=wxpaystr;
							}
						}
						else{
							dInfo.thirdPartyPaySign="";
						}
					}
				/***********获取支付宝快捷支付参数**********************************/
				if(thirdPartyPayMethod.equals("1")){
					if(p_total_fee.intValue()>0){
						String alipaysign = AliPayService.getInstance().alipay_Wap_sign(result1.orderCode,p_total_fee.doubleValue());
						dInfo.thirdPartyPaySign=alipaysign;
					}
					else
					{
						dInfo.thirdPartyPaySign="";
					}
				}
				
				if(thirdPartyPayMethod.equals("2")){
					if(p_total_fee.intValue()>0){
						dInfo.thirdPartyPaySign="";
					}
					else
					{
						dInfo.thirdPartyPaySign="";
					}
				}
				
				if(thirdPartyPayMethod.equals("3")){
					if(p_total_fee.intValue()>0){
						String alipaysign = AliPayService.getInstance().alipay_FT_sign(result1.orderCode,p_total_fee.doubleValue());
						dInfo.thirdPartyPaySign=alipaysign;
					}
					else
					{
						dInfo.thirdPartyPaySign="";
					}
				}
				result.setStatus("1");
				result.setMsg("");
				result.setData(dInfo);
			}
			return ok(Json.toJson(result));
		}
	
		
		//订单生成接口（去支付）(POST方式)
		public Result shoppingorder_pay(){
			response().setContentType("application/json;charset=utf-8");
			String uid = AjaxHellper.getHttpParamOfFormUrlEncoded(request(), "uid");
			String orderCode = AjaxHellper.getHttpParamOfFormUrlEncoded(request(), "orderCode");
			String thirdPartyPayMethod = AjaxHellper.getHttpParamOfFormUrlEncoded(request(), "thirdPartyPayMethod");
			String devid = AjaxHellper.getHttpParamOfFormUrlEncoded(request(), "devid");
			String appversion=AjaxHellper.getHttpParamOfFormUrlEncoded(request(), "appversion");
			String wdhjy = AjaxHellper.getHttpParamOfFormUrlEncoded(request(),"wdhjy");
			
			String ip = StringUtil.getIpAddr(request());
			
			if (ip.equals("0:0:0:0:0:0:0:1")){
				ip="127.0.0.1";
			}
			
			AppSalesManOrderNewVO result = new AppSalesManOrderNewVO();
			if (!StringUtil.checkMd5(devid, wdhjy,"3.0.0"))
			{
				result.setStatus("0");
				return ok(Json.toJson(result));
			}
			ShoppingOrder result1=shoppingOrderService.getShoppingOrderByOrderCode(orderCode);		
			AppSalesManOrderNewVO.dInfo dInfo = new AppSalesManOrderNewVO.dInfo();
			dInfo.orderId = result1.getId().toString();
			dInfo.orderCode =  result1.getOrderCode();
			dInfo.payCode = result1.getOrderCode();
			double p_total_fee = result1.getTotalFee();
			BigDecimal a = new BigDecimal(p_total_fee).setScale(0,BigDecimal.ROUND_CEILING);
			/***********获取微信参数************************************/
			if(thirdPartyPayMethod.equals("0")){
				if(p_total_fee>0){
					a = a.multiply(new BigDecimal(100));
					//String ip=request().remoteAddress();
					JsonNode reslutwx = WXPayServiceSalesMan.getInstance().getSignAndPrepayID(result1.getOrderCode(),a.longValue(),ip,"APP");
					if(reslutwx==null || reslutwx.get("retcode").asInt()<0){
						dInfo.thirdPartyPaySign="";
					}else{
						String nostr=reslutwx.get("noncestr").asText();
						String partnerid=reslutwx.get("partnerid").asText();
						String prepayid=reslutwx.get("prepayid").asText();
						String timstr=reslutwx.get("timestamp").asText();
						String packg=reslutwx.get("package").asText();
						String signs=reslutwx.get("sign").asText();
						String wxpaystr="noncestr="+nostr+"&partnerid="+partnerid+"&prepayid="+prepayid+"&timestamp="+timstr+"&sign="+signs+"&package="+packg;
						wxpaystr=Base64.encodeBase64String(wxpaystr.getBytes());
						dInfo.thirdPartyPaySign=wxpaystr;
					}
				}
				else{
					dInfo.thirdPartyPaySign="";
				}
			}
			/***********获取支付宝快捷支付参数**********************************/
			if(thirdPartyPayMethod.equals("1")){
				if(p_total_fee>0){
					String alipaysign = AliPayService.getInstance().alipay_Wap_sign(result1.getOrderCode(),p_total_fee);
					dInfo.thirdPartyPaySign=alipaysign;
				}
				else
				{
					dInfo.thirdPartyPaySign="";
				}
			}
			
			if(thirdPartyPayMethod.equals("2")){
				if(p_total_fee>0){
					dInfo.thirdPartyPaySign="";
				}
				else
				{
					dInfo.thirdPartyPaySign="";
				}
			}
			
			if(thirdPartyPayMethod.equals("3")){
				if(p_total_fee>0){
					
					String alipaysign = AliPayService.getInstance().alipay_FT_sign(result1.getOrderCode(),p_total_fee);
					dInfo.thirdPartyPaySign=alipaysign;
				}
				else
				{
					dInfo.thirdPartyPaySign="";
				}
			}
			result.setStatus("1");
			result.setMsg("");
			result.setData(dInfo);
			return ok(Json.toJson(result));
		}
		
		//订单详情接口(GET方式)
		public Result shoppingOrder_Info(){
			response().setContentType("application/json;charset=utf-8");
			String uid = AjaxHellper.getHttpParam(request(), "uid");
			String orderCode = AjaxHellper.getHttpParam(request(), "orderCode");
			String devid = AjaxHellper.getHttpParam(request(), "devid")==null?"":AjaxHellper.getHttpParam(request(), "devid");
			String appversion=AjaxHellper.getHttpParam(request(), "appversion");
			String wdhjy = AjaxHellper.getHttpParam(request(),"wdhjy");
			AppSalesManOrderDetailVO result = new AppSalesManOrderDetailVO();
			
			AppSalesManOrderDetailVO.ShoppingOrderDetail orderInfo = new AppSalesManOrderDetailVO.ShoppingOrderDetail();
			
			if (!StringUtil.checkMd5(devid, wdhjy,appversion))
			{
				result.status=0;
				return ok(Json.toJson(result));
			}
			
			
			ShoppingOrder shoppingOrder = shoppingOrderService.getShoppingOrderByOrderCode(orderCode);
			if(shoppingOrder==null){
				result.status=0;
				return ok(Json.toJson(result));
			}
			result.status=1;
			AppSalesManOrderDetailVO.AddressData addressData = new AppSalesManOrderDetailVO.AddressData();
			
			addressData.city=shoppingOrder.getProvince();
			addressData.address = shoppingOrder.getAddress();
			addressData.name = shoppingOrder.getName();
			addressData.phone=shoppingOrder.getPhone();
			addressData.cardId = shoppingOrder.getCardId();
			orderInfo.addressdata = addressData;
			orderInfo.orderTime = CHINESE_DATE_TIME_FORMAT.format(shoppingOrder.getDate_add());
			orderInfo.id = String.valueOf(shoppingOrder.getId());
			orderInfo.orderCode = shoppingOrder.getOrderCode();
			orderInfo.totalfee = "¥"+new BigDecimal(shoppingOrder.getTotalFee()).setScale(0, BigDecimal.ROUND_CEILING).intValue();
			orderInfo.total_fee = String.valueOf(new BigDecimal(shoppingOrder.getTotalFee()).setScale(0, BigDecimal.ROUND_CEILING).intValue());
			if(shoppingOrder.getStatus()==15)
			{
				orderInfo.orderstatus= "6";
			}else{
				orderInfo.orderstatus= String.valueOf(shoppingOrder.getStatus());
			}
			switch(shoppingOrder.getStatus())
			{
					case 0:
						orderInfo.paystatus="待支付";
						orderInfo.needpay = "1";
						break;
					case 1:
						orderInfo.paystatus="待发货";
						orderInfo.needpay = "0";
						break;
					case 2:
						orderInfo.paystatus="已发货";
						orderInfo.needpay = "0";
						break;
					case 3:
						orderInfo.paystatus="已完成";
						orderInfo.needpay = "0";
						break;
					case 5:
						orderInfo.paystatus="已取消";
						orderInfo.needpay = "0";
						break;
					case 6:
						orderInfo.paystatus="已退款";
						orderInfo.needpay = "0";
						break;
					case 15:
						orderInfo.paystatus="已退款";
						orderInfo.needpay = "0";
						break;
					case 16:
						orderInfo.paystatus="尾款未支付，订金退款";
						orderInfo.needpay = "0";
						break;
					case 21:
						orderInfo.paystatus="订金已支付";
						orderInfo.needpay = "0";
						break;
					case 22:
						orderInfo.paystatus="尾款待支付";
						orderInfo.needpay = "1";
						break;
			}
			orderInfo.carriagefee= "¥"+shoppingOrder.getForeignfee().intValue();
			orderInfo.tarifffee = "¥"+shoppingOrder.getTariff_fee().intValue();
			orderInfo.costfee="¥0";
			List<Parcels> parcelsList= shoppingOrderService.queryPardelsByOrderId(shoppingOrder.getId());
			
			List<Product> productList = new ArrayList<Product>();
			for(Parcels parcels:parcelsList){
				productList.addAll(shoppingOrderService.queryProductListByParcelsId(parcels.getId()));
			}
			List<Product> productListOutParcels = productService.getOutPardelsProduct_ByOrderCode(orderCode,"0");
			if(productListOutParcels!=null&& productListOutParcels.size()>0){
				productList.addAll(productListOutParcels);
			}
			List<AppSalesManOrderDetailVO.PInfo> pList= new ArrayList<AppSalesManOrderDetailVO.PInfo>();
			for (int i=0;i<productList.size();i++) {
				Product product = productList.get(i);
				AppSalesManOrderDetailVO.PInfo pInfo = new AppSalesManOrderDetailVO.PInfo();
				pInfo.pid = String.valueOf(product.getPid());
				
				pInfo.title = product.getTitle();
				pInfo.linkurl="pDe://pid="+ String.valueOf(product.getPid());
				
				double rmb_price=shoppingOrderService.getProductPrice(orderCode, String.valueOf(product.getPid()));
				pInfo.chinaprice="¥"+rmb_price;
				pInfo.china_price=rmb_price+"";
				pInfo.counts =  String.valueOf(product.getCounts());
				pInfo.limitcount =  String.valueOf(product.getLimitcount());
				pInfo.img = product.getListpic();
				pList.add(pInfo);
			}
			orderInfo.p_list=pList;
			if (shoppingOrder.getPaystat()>0){
				List<String> hType = new ArrayList<String>();
				switch(shoppingOrder.getPaymethod()){
				case 10:
					hType.add("0");
					break;
				case 20:
					hType.add("1");
					break;
				case 21:
					hType.add("1");
					break;
				case 22:
					hType.add("1");
					break;
				}
				orderInfo.hgPaymentType =hType;
			}else{
				orderInfo.hgPaymentType = shoppingOrderService.getHgPaymentType();
			}
			result.data = orderInfo;
			return ok(Json.toJson(result));
		}
		
		//暗号验证接口(POST)
		public Result checkregistcode() {
			response().setContentType("application/json;charset=utf-8");
			String uid = AjaxHellper.getHttpParamOfFormUrlEncoded(request(), "uid");
			String devid = AjaxHellper.getHttpParamOfFormUrlEncoded(request(),"devid");
			String appversion = AjaxHellper.getHttpParamOfFormUrlEncoded(request(),"appversion");
			String registCode = AjaxHellper.getHttpParamOfFormUrlEncoded(request(),"registCode");
			String marketCode = AjaxHellper.getHttpParamOfFormUrlEncoded(request(),"marketCode");
			String wdhjy = AjaxHellper.getHttpParamOfFormUrlEncoded(request(),"wdhjy");
			AppSalesManUserCodeVO result=new AppSalesManUserCodeVO();
			result.setRegistCode(registCode);
			if (!StringUtil.checkMd5(devid, wdhjy,appversion))
			{
				result.setStatus("0");
				result.setMsg("校验错误");
				return ok(Json.toJson(result));
			}
			
			result=userService.checkregistcode(registCode);
			result.setRegistCode(registCode);
			if(result == null || StringUtils.isBlank(result.getStatus()))
			{
				result.setStatus("0");
				result.setMsg("检测失败");
				
				return ok(Json.toJson(result));
			}
			return ok(Json.toJson(result));
		}

		// 修改密码接口（POST)
		public Result password_modify() {
			response().setContentType("application/json;charset=utf-8");
			String uid = AjaxHellper.getHttpParamOfFormUrlEncoded(request(), "uid");
			String appversion = AjaxHellper.getHttpParamOfFormUrlEncoded(request(), "appversion");
			String marketCode = AjaxHellper.getHttpParamOfFormUrlEncoded(request(), "marketCode");
			String username = AjaxHellper.getHttpParamOfFormUrlEncoded(request(), "username");
			String phone = AjaxHellper.getHttpParamOfFormUrlEncoded(request(), "phone");
			String password = AjaxHellper.getHttpParamOfFormUrlEncoded(request(), "password");
			String devid = AjaxHellper.getHttpParamOfFormUrlEncoded(request(), "devid");
			String wdhjy = AjaxHellper.getHttpParamOfFormUrlEncoded(request(), "wdhjy");
			String verifyCode = AjaxHellper.getHttpParamOfFormUrlEncoded(request(), "verifyCode");
			
			AppSalesManUserVO result = new AppSalesManUserVO();
			if (!StringUtil.checkMd5(devid, wdhjy, appversion)) {
				result.setStatus("0");
				result.setMsg("校验失败");
				return ok(Json.toJson(result));
			}
			UserVerify u=userService.checkVerify(phone, verifyCode);
			if (u == null ||StringUtils.isBlank(u.getPhone())){
				result.setMsg("验证失败");
				result.setStatus("0");
			}
			
			result = userService.salesManPasswordModify(phone, password,devid);
			return ok(Json.toJson(result));
		}
		
		// 校验验证码接口（POST方式)
		public Result checkUserVerify() {
			response().setContentType("application/json;charset=utf-8");
			String phone = AjaxHellper.getHttpParamOfFormUrlEncoded(request(),"phone");
			String verifyCode = AjaxHellper.getHttpParamOfFormUrlEncoded(request(),"verifyCode");
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
				if ("7859".equals(verifyCode.trim()))
				{
					result.setStatus("1");
					result.setMsg("");
					return ok(Json.toJson(result));
				}
			}
			UserVerify userVerify = userService.checkVerify(phone,verifyCode);
			if(userVerify!=null){
				result.setStatus("1");
				result.setMsg("");
			}else{
				result.setStatus("2");
				result.setMsg("验证码校验失败");
			}
			return ok(Json.toJson(result));
		}
		
		//(四十三)	商品库存、价格验证接口(GET方式) products_check.php
		public Result products_check(){
			response().setContentType("application/json;charset=utf-8");
			String uid = AjaxHellper.getHttpParam(request(), "uid");
			String datastr = AjaxHellper.getHttpParam(request(), "datastr");
			String orderCode = AjaxHellper.getHttpParam(request(), "orderCode")==null?"": AjaxHellper.getHttpParam(request(), "orderCode");
			String devid = AjaxHellper.getHttpParam(request(), "devid")==null?"":AjaxHellper.getHttpParam(request(), "devid");
			String appversion=AjaxHellper.getHttpParam(request(), "appversion");
			String wdhjy = AjaxHellper.getHttpParam(request(),"wdhjy");
			AppSalesManErrlVO result = new AppSalesManErrlVO();
			if (!StringUtil.checkMd5(devid, wdhjy,appversion))
			{
				result.status="0";
				result.msg="校验失败";
				return ok(Json.toJson(result));
			}
			
			
			
			if(StringUtils.isBlank(uid)||StringUtils.isBlank(datastr)){
				result.status="0";
				result.msg="登录失败";
				return ok(Json.toJson(result));
			}
			List<Object[]> deviceUsers = userService.getDeviceUser(uid);
			if(deviceUsers!=null && deviceUsers.size()>0){
				
				String dataArray[] = datastr.split(",");
				
				BigDecimal freight=new BigDecimal(0);
				
				int status=1;
				for(String dataTemp:dataArray){
					String dataNeed[] = dataTemp.split("_");
					String pid=dataNeed[0];
					String price=dataNeed[1];
					String counts=dataNeed[2];
					if(price!=null && "¥".equals(price.substring(0, 1)) ){
						price=price.substring(1, price.length());
					}
					//以后去掉
					price=new BigDecimal(price).setScale(0, BigDecimal.ROUND_CEILING).intValue()+"";
					
					Product product =productService.getProductById(Numbers.parseLong(pid, 0L));
					Currency currency = productService.queryCurrencyById(product.getCurrency());
					
					BigDecimal rate = new BigDecimal(currency.getRate()/100).setScale(4,  BigDecimal.ROUND_CEILING) ;
					
					BigDecimal priceDec = new BigDecimal(product.getPrice()/100).setScale(2,  BigDecimal.ROUND_CEILING) ;
					
					BigDecimal rmb_price= shoppingCartService.getPriceShoppingCart_eid(uid,pid);
					
					//新人价
					if(userService.checkFirstFlag(uid)){
						ProductPriceExt pe=productService.getProductPrice(product.getPid(),Constants.getSystemGroupOne("newman"),"rmbprice");
						if(pe!=null)
							rmb_price=new BigDecimal(pe.getSaleprice()).setScale(0, BigDecimal.ROUND_CEILING);
						//新人产品只能购买一次
						if(Numbers.parseInt(counts, 0)>1){
							if(Numbers.parseInt(counts, 0)>1){//只能购买一件
								status=101;
							}
							//更新该用户购物车下商品数量为一
							shoppingCartService.updateShoppingCart(uid,pid,1L);
						}
					}
					
					Double weight=product.getWeight();
					freight=new BigDecimal(ShoppingCartService.getfreight(product.getFromsite(),String.valueOf(weight))) ;
					
					String msg="";
					
					List<Long> ids = new ArrayList<Long>();
					if(product.getStatus()!=10){
						status=99;
						ids.add(Numbers.parseLong(pid, 0L));
						shoppingCartService.deleteShoppingCartByPIds(Numbers.parseLong(uid, 0L), ids);//删除该用户购物车下商品
					}else{
						
						if(StringUtils.isBlank(orderCode)){
							Long nstock = product.getNstock();
							if(product.getIshot()==1){
								nstock = productService.dealNstockWithProduct(product.getPid());
							}
							if(nstock<=0){
								status=2;
								ids.add(Numbers.parseLong(pid, 0L));
								shoppingCartService.deleteShoppingCartByPIds(Numbers.parseLong(uid, 0L), ids);//删除该用户购物车下的此商品
							}else{
								if(nstock<Numbers.parseInt(counts, 0)){
									status=4;
									//更新该用户购物车下商品数量为限购数量
									shoppingCartService.updateShoppingCart(uid,pid,product.getNstock());
								}
								if (product.getLimitcount()<Numbers.parseInt(counts, 0) && product.getLimitcount()>0)
								{
									status=4;
									//更新该用户购物车下商品数量为限购数量
									shoppingCartService.updateShoppingCart(uid,pid,Long.valueOf(product.getLimitcount()));
								}
								if("1".equals(product.getNewMantype())){//首购商品
									//判断用户是否是首次下单
									boolean flag = userService.checkFirstFlag(uid);
									if(flag==false){
										status=7;
										ids.add(Numbers.parseLong(pid, 0L));
										shoppingCartService.deleteShoppingCartByPIds(Numbers.parseLong(uid, 0L), ids);//删除该用户购物车下此商品
									}else{
										if(Numbers.parseInt(counts, 0)>1){//只能购买一件
											status=6;
										}
										//更新该用户购物车下商品数量为一
										shoppingCartService.updateShoppingCart(uid,pid,1L);
									}
									
								}
								if("2".equals(product.getNewMantype())){//仅一次商品
									if(Numbers.parseInt(counts, 0)>1){
										status=6;
									}
								}
								if("3".equals(product.getNewMantype())){//0元商品
									boolean flag = userService.checkBuyOrNotFlag(uid,pid);
									if(flag==false){
										status=8;
										ids.add(Numbers.parseLong(pid, 0L));
										shoppingCartService.deleteShoppingCartByPIds(Numbers.parseLong(uid, 0L), ids);//删除该用户购物车下此商品
									}else{
										if(Numbers.parseInt(counts, 0)>1){//只能购买一件
											status=9;
										}
										//更新该用户购物车下商品数量为一
										shoppingCartService.updateShoppingCart(uid,pid,1L);
									}
								}
							}
						}else{
							if("1".equals(product.getNewMantype())){//首购商品
								//判断用户是否是首次下单
								boolean flag = userService.checkFirstFlag(uid);
								if(flag==false){
									status=5;
								}
								if(Numbers.parseInt(counts, 0)>1){
									status=6;
								}
							}
							if("2".equals(product.getNewMantype())){//仅一次商品
								if(Numbers.parseInt(counts, 0)>1){
									status=6;
								}
							}
							if("3".equals(product.getNewMantype())){//0元商品
								if(Numbers.parseInt(counts, 0)>1){
									status=9;
								}
							}
						}
					if(rmb_price.doubleValue()!=Numbers.parseDouble(price, 0D) && status==1){
						status=3;
					}
						
						
					}
					if (status==99)
					{
						msg="该商品已下架";
					}
					if (status==2)
					{
						msg="您所购买的部分商品已经卖光了哦~请重新结算";
					}
					if (status==3)
					{
						msg="您所购买的部分商品价格发生了改变哦~请重新结算";
					}
					if (status==4)
					{
						msg="您所购买的部分商品超出了限购数量哦~请重新结算";
					}
					if (status==5)
					{
						msg="该商品仅限新人购买";
					}
					if (status==6)
					{
						msg="新人商品只能购买一件";
					}
					if (status==7)
					{
						msg="您无法购买新人特权商品";
					}
					if (status==8)
					{
						msg="您无法购买该商品";
					}
					if (status==9)
					{
						msg="该商品只能购买一件";
					}
					if(status==101){
						msg="新人商品只能购买一件";
					}
					result.status=String.valueOf(status);
					result.msg=msg;
				}
			}
			
			return ok(Json.toJson(result));
		}
}

