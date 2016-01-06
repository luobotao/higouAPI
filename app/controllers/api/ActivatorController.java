package controllers.api;

import java.math.BigDecimal;
import java.security.MessageDigest;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.codec.BinaryEncoder;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import models.CountH5;
import models.Coupon;
import models.Coupon_user;
import models.LuckDraw;
import models.Product;
import models.ShoppingOrder;
import models.User;
import models.WxSign;
import models.WxUser;
import net.sf.json.JSONObject;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

import play.Configuration;
import play.Logger;
import play.data.Form;
import play.libs.Json;
import play.mvc.Result;
import services.ICacheService;
import services.ServiceFactory;
import services.SmsService;
import services.api.CouponService;
import services.api.EndorsementService;
import services.api.ProductService;
import services.api.ShoppingOrderService;
import services.api.UserService;
import utils.AjaxHellper;
import utils.Constants;
import utils.Numbers;
import utils.StringUtil;
import utils.wxpay.Sha1Util;
import utils.wxpay.TenpayHttpClient;
import assets.CdnAssets;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.thoughtworks.xstream.core.util.Base64Encoder;

@Named
@Singleton
public class ActivatorController extends BaseApiController {
	private static final Logger.ALogger logger = Logger.of(H5ProductController.class);
	private static final SimpleDateFormat CHINESE_DATE_TIME_FORMAT = new SimpleDateFormat(
			"yyyyMMddHHmmss");
	private static final SimpleDateFormat CHINESE_DATE_TIME_FORMAT_NORMAL = new SimpleDateFormat(
			"yyyy-MM-dd HH:mm:ss");
	private static final SimpleDateFormat CHINESE_DATE_TIME_FORMAT_DAYTIME = new SimpleDateFormat(
			"MM月dd日 HH:mm");
	private final UserService userService;
	private final CouponService couponService;
	private final SmsService smsService;
	private final EndorsementService endorseService;
	private final ProductService productService;
	private final ShoppingOrderService shoppingOrderService;
	private ICacheService cache = ServiceFactory.getCacheService();
	
	
	@Inject
	public ActivatorController(final UserService userService,final CouponService couponService,final SmsService smsService,final EndorsementService endorseService,
								final ProductService productService,final ShoppingOrderService shoppingOrderService){
		this.userService=userService;
		this.couponService=couponService;
		this.smsService=smsService;
		this.endorseService=endorseService;
		this.productService=productService;
		this.shoppingOrderService=shoppingOrderService;
	}

	//朋友拉新奖品列表
	public Result friendnew(){
		String uid=AjaxHellper.getHttpParam(request(), "u");
		WxUser wu=null;
		if(!StringUtils.isBlank(uid))
			wu=userService.getWxUser(uid);
		if(wu==null || StringUtils.isBlank(wu.getUnionid())){
			wu=new WxUser();
			wu.setUnionid("");
			wu.setNickname("嗨个购小嗨");
			wu.setHeadicon("http://tp4.sinaimg.cn/5359972175/50/5728979545/0");
		}
		List<List<String>> plist=new ArrayList<>();
		List<String> tmp=new ArrayList<String>();
		tmp.add("3478");
		tmp.add("http://ht.neolix.cn/pimgs/p1/10/00/34/78/ls-10003478-16852723.jpg");
		tmp.add("赫拉LongStay气垫BB");
		tmp.add("288");
		plist.add(tmp);
		
		tmp=new ArrayList<String>();
		tmp.add("1625");
		tmp.add("http://ht.neolix.cn/pimgs/p1/10/00/16/25/ls-10001625-18680744.jpg");
		tmp.add("安耐晒超防水防晒霜");
		tmp.add("188");
		plist.add(tmp);
		
		tmp=new ArrayList<String>();
		tmp.add("3354");
		tmp.add("http://ht.neolix.cn/pimgs/p1/10/00/33/54/ls-10003354-17892773.jpg");
		tmp.add("[RECIPE]水晶防晒喷雾");
		tmp.add("85");
		plist.add(tmp);
		
		tmp=new ArrayList<String>();
		tmp.add("2102");
		tmp.add("http://ht.neolix.cn/pimgs/p1/10/00/21/02/ls-10002102-9601925.jpg");
		tmp.add("悦诗风吟绿茶籽补水面霜");
		tmp.add("109");
		plist.add(tmp);
		
		tmp=new ArrayList<String>();
		tmp.add("1828");
		tmp.add("http://ht.neolix.cn/pimgs/p1/10/00/18/28/ls-10001828-14331085.jpg");
		tmp.add("九朵云美白祛斑淡斑精华");
		tmp.add("142");
		plist.add(tmp);
		
		tmp=new ArrayList<String>();
		tmp.add("1780");
		tmp.add("http://ht.neolix.cn/pimgs/p1/10/00/17/80/ls-10001780-8115594.jpg");
		tmp.add("SNP黄金胶原蛋白面膜");
		tmp.add("118");
		plist.add(tmp);
		
		tmp=new ArrayList<String>();
		tmp.add("1768");
		tmp.add("http://ht.neolix.cn/pimgs/p1/10/00/17/68/ls-10001768-14280096.jpg");
		tmp.add("VDL贝壳提亮液妆前乳");
		tmp.add("128");
		plist.add(tmp);
		
		tmp=new ArrayList<String>();
		tmp.add("1640");
		tmp.add("http://ht.neolix.cn/pimgs/p1/10/00/16/40/ls-10001640-8719232.jpg");
		tmp.add("资生堂胶原蛋白饮料");
		tmp.add("140");
		plist.add(tmp);
		
		WxSign wx=this.getwxstr();
		if(!StringUtils.isBlank(uid))
			wx.setShareurl(wx.getShareurl()+"?u="+uid);
		return ok(views.html.H5.act1.friendnew.render(wx,plist,wu));
	}
	
	//朋友圈拉新授权抽奖
	public Result cj(){
		//String pid=Form.form().bindFromRequest().get("pid");
		String pid=AjaxHellper.getHttpParam(request(), "pid");
		String code=AjaxHellper.getHttpParam(request(), "code");
		if(code==null || code.equals("")){
			//用户取消或景鉴权失败
			logger.error("微信鉴权返回没带参数");
			return ok(views.html.sheSaid.pageError.render());
		}
		if(StringUtils.isBlank(pid))
		{
			return ok(views.html.sheSaid.pageError.render());
		}
		LuckDraw luck=new LuckDraw();
		
		//微信授权
		//微信第二步认证
		String openid="";
		String unionid="";
		String access_token="";
		User usert=(User)cache.getObject("unionid"+unionid);
		String wxurl="https://api.weixin.qq.com/sns/oauth2/access_token?appid="+Constants.WXappID+"&secret="+Constants.WXappsecret+"&code="+code+"&grant_type=authorization_code";
		// 发送请求，返回json
		TenpayHttpClient httpClient = new TenpayHttpClient();
		httpClient.setReqContent(wxurl);
		String resContent = "";
		if (httpClient.callHttpPost(wxurl, "")){
			resContent = httpClient.getResContent();
			Logger.info("weixin第二步返回"+resContent);
			JSONObject json=JSONObject.fromObject(resContent);
			// 判断返回是否含有unionid,openid...
			try{
				if (!StringUtils.isBlank(json.getString("openid"))) {
					// 更新application值.
					openid = json.getString("openid");
				} 
				if(!StringUtils.isBlank(json.getString("unionid")))
					unionid=json.getString("unionid");
				if(!StringUtils.isBlank(json.getString("access_token")))
					access_token=json.getString("access_token");
				
				//记录PID，UNIONID				
				luck.setPid(Numbers.parseLong(pid, 0L));
				luck.setUnionid(unionid);
				luck.setCreateTime(new Date());
				luck.setPhone("");

				luck=userService.addLuckDraw(luck);
			}
			catch(Exception e){
				//return ok(views.html.sheSaid.pageError.render());
				logger.error("微信二次鉴权调用失败："+e.toString());
				//return ok(views.html.sheSaid.pageError.render());
			}

			
			if(usert==null && !StringUtils.isBlank(unionid) && !StringUtils.isBlank(openid) && !StringUtils.isBlank(access_token)){
				usert=new User();
				usert.setUnionid(unionid);
				
				//不存在则跳到输入手机号注册页面
				String wxgetinfourl="https://api.weixin.qq.com/sns/userinfo?access_token="+access_token+"&openid="+openid+"&lang=zh_CN";								
				try{
					// 发送请求，返回json
					HttpClient clientc = new DefaultHttpClient();
					HttpGet get = new HttpGet(wxgetinfourl);
					
					String resContenti = "";
					HttpResponse resc = clientc.execute(get);
					if (resc.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
						HttpEntity entity = resc.getEntity();
	
						resContenti = EntityUtils.toString(resc.getEntity(),"UTF-8");
						Logger.info("调用getAccessToken 接口返回报文内容:" + resContenti);				
						JSONObject jsons = JSONObject.fromObject(resContenti);
						
	
						if(!StringUtils.isBlank(jsons.getString("nickname"))){								
							usert.setNickname(jsons.getString("nickname"));
						}
						if(!StringUtils.isBlank(jsons.getString("headimgurl"))){									
							usert.setHeadIcon(jsons.getString("headimgurl"));
						}
					}
					if(StringUtils.isBlank(usert.getNickname()))
						usert.setNickname("嗨个购小嗨");
					if(StringUtils.isBlank(usert.getHeadIcon()))
						usert.setHeadIcon("http://tp4.sinaimg.cn/5359972175/50/5728979545/0");

					//保存或修改用户信息
					if(usert!=null && !StringUtils.isBlank(usert.getUnionid())){
						WxUser wu=new WxUser();
						wu.setHeadicon(usert.getHeadIcon());
						wu.setNickname(usert.getNickname());
						wu.setUnionid(usert.getUnionid());
						userService.addWxUser(wu);
					}
					
				}
				catch(Exception e){
					//return ok(views.html.sheSaid.pageError.render());
					logger.error("微信二次鉴权返回参数解析失败"+e.toString());
				}
			}
		}
			if(usert==null)
				usert=new User();
			if(StringUtils.isBlank(usert.getNickname()))
				usert.setNickname("嗨个购小嗨");
			if(StringUtils.isBlank(usert.getHeadIcon()))
				usert.setHeadIcon("http://tp4.sinaimg.cn/5359972175/50/5728979545/0");
		WxSign wxs=getwxstr();
		if(!StringUtils.isBlank(usert.getUnionid()))
			wxs.setShareurl(wxs.getShareurl()+"?u="+usert.getUnionid());
		return ok(views.html.H5.act1.sharefriend.render(luck,wxs,usert,StringUtil.getSystemConfigValue("friendnew_date")));
	}
	
	public Result sharefriend(){		
		//return ok(views.html.H5.act1.sharefriend.render(null,null,null));
		return null;
	}
	
	//拉新分享页面
	public Result friendlogin(){
		Long aid=Numbers.parseLong(AjaxHellper.getHttpParam(request(), "aid"), 0L);
		String uid=AjaxHellper.getHttpParam(request(),"u");
		if(aid==0L || StringUtils.isBlank(uid))
		{
			return ok(views.html.sheSaid.pageError.render());
		}
		LuckDraw luck=userService.getLuckDrawById(aid);
		if(luck==null){
			luck=new LuckDraw();
			luck.setUnionid("");
		}
		WxSign wx=this.getwxstr();
		return ok(views.html.H5.act1.friendlogin.render(aid.toString(),uid,wx,luck));
	}
	
	//分享后跳转优惠券页面
	public Result friendcoupon(){
		String id=AjaxHellper.getHttpParam(request(), "aid");
		LuckDraw luck=userService.getLuckDrawById(Numbers.parseLong(id, 0L));
		if(luck==null){
			luck=new LuckDraw();
			luck.setUnionid("");
		}
		Coupon co=couponService.getCouponById(Long.valueOf(StringUtil.getSystemConfigValue("friendshare_coupon")));
		if(co==null)
		{
			co=new Coupon();
			co.setCouponprice(Double.valueOf("39"));
		}
		WxSign wx=this.getwxstr();
		return ok(views.html.H5.act1.friendcoupon.render(new BigDecimal(co.getCouponprice()).setScale(0,BigDecimal.ROUND_CEILING).toString(),wx,luck));
	}
	
	//分享成功后拉新注册
	public Result updateluck(){
		ObjectNode re=Json.newObject();
		String uid=AjaxHellper.getHttpParamOfFormUrlEncoded(request(), "u");
		String ip=AjaxHellper.getHttpParamOfFormUrlEncoded(request(), "ip");
		String phone=Form.form().bindFromRequest().get("phone");
		Long id=Numbers.parseLong(Form.form().bindFromRequest().get("id"),0L);
		if(id==0L || !StringUtil.checkPhone(phone) || StringUtils.isBlank(uid)){
			re.put("status", "0");
			re.put("msg", "手机号码不正确");
			return ok(re);
		}
		
		//更新抽奖日志
		LuckDraw luck=userService.getLuckDrawById(id);
		if(luck==null){
			re.put("status", "0");
			re.put("msg", "非法操作");
			return ok(re);
		}

		//检查用户是否对应
		if(!luck.getUnionid().equals(uid)){
			re.put("status", "0");
			re.put("msg", "您已经参与过此活动了哦");
			return ok(re);
		}
		luck.setPhone(phone);
		//检查重复提交
		List<LuckDraw> lucklist=userService.getLuckDraw(luck.getPid(), luck.getUnionid(), phone);
		if(lucklist!=null && !lucklist.isEmpty()){
			re.put("status", "0");
			re.put("msg", "您已经参与过此活动了哦");
			return ok(re);
		}
		userService.editLuck(luck);
		//送优惠券
		boolean suc=userService.addCouponPhone(phone, StringUtil.getSystemConfigValue("friendshare_coupon"));
		if(suc){
			//发送短信
			smsService.saveSmsInfo("#link#="+luck.getUnionid(),phone,"912413","2");
			re.put("status", "1");
			re.put("msg", "");
		}
		else
		{
			re.put("status", "0");
			re.put("msg", "您已经参与过此活动了哦");
		}
		return ok(re);
	}
	
	
  	
  	public Result tj(){
  		String uid=AjaxHellper.getHttpParam(request(), "u");
  		if(StringUtils.isBlank(uid))
  			uid="";
  		String qry="";
			String vn="";
			String vl="";
			Map<String,String[]> maps=request().queryString();
			if(maps!=null && maps.keySet()!=null){
				Iterator<String> keyIt = maps.keySet().iterator();					
				while(keyIt.hasNext()){
					vn=keyIt.next();
					vl=maps.get(vn)[0];
					qry=qry+"&"+vn+"="+vl;
				}
			}
						
			if(!StringUtils.isBlank(qry))
				qry="?"+qry.substring(1);
			String urls="http://"+request().host()+request().path()+qry;
			
			CountH5 cnt=new CountH5();
			cnt.setChannel("friendshareSms");
			cnt.setIp(request().remoteAddress());
			cnt.setIswx("1");
			cnt.setShareType("0");
			cnt.setUrl(urls);
			cnt.setCreateTime(new Date());
			cnt.setUserId(0L);
			cnt.setUnionid(uid);
			endorseService.saveCount(cnt);

			return redirect("http://a.app.qq.com/o/simple.jsp?pkgname=cn.neolix.higo");
			//return ok(views.html.H5.act1.tj.render(uid));
			
  	}
  	
	/////////////////////免费送第一波//////////////////////////////////////////
  	//跳转分享，区分微信微博
  	public Result freeauth(){
  		String fp=AjaxHellper.getHttpParam(request(), "fp");
  		return ok(views.html.H5.FreeOne.freeauth.render(fp));
  	}
  	
  	//临时页面
  	public static Result freelimit(){
  		
  		return ok(views.html.H5.FreeOne.freelimit.render());
  	}
  	//微信鉴权
  	public static Result freewxauth(){
  		String fp=AjaxHellper.getHttpParam(request(), "fp");
		if(StringUtils.isBlank(fp))
			fp="";
		String redirecturl=java.net.URLEncoder.encode(StringUtil.getDomainH5()+"/H5/vokeFree?fp="+fp);
		String wxauthURL="https://open.weixin.qq.com/connect/oauth2/authorize?appid="+Constants.WXappID+"&redirect_uri="+redirecturl+"&response_type=code&scope=snsapi_userinfo&state=123#wechat_redirect";
		return redirect(wxauthURL);
  	}
  	//微信二次鉴权
  	public Result vokeFree(){
  		String fp=AjaxHellper.getHttpParam(request(),"fp");
  		String code=AjaxHellper.getHttpParam(request(), "code");
		Long pid = Numbers.parseLong(StringUtil.getSystemConfigValue("Free_One_pro_pid"), 0L);
		String freetime =StringUtil.getSystemConfigValue("Free_One_FreeTime");
		String count=StringUtil.getSystemConfigValuenocache("Free_One_pro_count");
		StringBuilder ctr=new StringBuilder();
		ctr.append("<b>");
		if(!StringUtils.isBlank(count)){
			count=String.valueOf(Numbers.parseInt(count, 0)*3);
			int cl=count.length();
			for(int i=0;i<cl;i++){
				ctr.append(count.substring(i,i+1)+"</b><b>");
			}
			count=ctr.toString().substring(0,ctr.length()-3);
		}
		if(pid==0L)
			return ok(views.html.H5.pageError.render());

		if(code==null || code.equals("")){
			//用户取消或景鉴权失败
			return redirect("/H5/freeget?fp="+fp);
		}
		//微信第二步认证
		User usert=new User();
		String wxurl="https://api.weixin.qq.com/sns/oauth2/access_token?appid="+Constants.WXappID+"&secret="+Constants.WXappsecret+"&code="+code+"&grant_type=authorization_code";
		try {
			// 发送请求，返回json
			TenpayHttpClient httpClient = new TenpayHttpClient();
			httpClient.setReqContent(wxurl);
			String resContent = "";
			if (httpClient.callHttpPost(wxurl, "")){
				resContent = httpClient.getResContent();
				Logger.info("weixin第二步返回"+resContent);
				
				JSONObject json=JSONObject.fromObject(resContent);
				String openid="";
				String unionid="";
				String access_token="";
				try{
					if (!StringUtils.isBlank(json.getString("openid"))) {
						// 更新application值.
						openid = json.getString("openid");
						usert.setOpenId(openid);
					}
					if(!StringUtils.isBlank(json.getString("unionid"))){
						unionid=json.getString("unionid");
						usert.setUnionid(unionid);
					}
					if(!StringUtils.isBlank(json.getString("access_token")))
						access_token=json.getString("access_token");
				}
				catch(Exception e){}
				access_token=access_token==null?"":access_token;
				openid=openid==null?"":openid;
				unionid=unionid==null?"":unionid;
//				access_token="dfads";
//				openid="afdasdf";
//				unionid="ocyEct-zfKKZJsqcV_LFMWAjlqoE";

				//检查并获取用户基本信息
				if(!access_token.equals("") && !openid.equals("") && !unionid.equals("")){
					//如果用户存在则跳到购物页面
					String nickname="";
					String headicon="";

						String wxgetinfourl="https://api.weixin.qq.com/sns/userinfo?access_token="+access_token+"&openid="+openid+"&lang=zh_CN";
							try{
							// 发送请求，返回json
							HttpClient clientc = new DefaultHttpClient();
							HttpGet get = new HttpGet(wxgetinfourl);
							
							String resContenti = "";
							HttpResponse resc = clientc.execute(get);
							if (resc.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
								HttpEntity entity = resc.getEntity();
								resContenti = EntityUtils.toString(resc.getEntity(),"UTF-8");
								Logger.info("免费活动调用getAccessToken 接口返回报文内容:" + resContenti);				
								JSONObject jsons = JSONObject.fromObject(resContenti);
								if(!StringUtils.isBlank(jsons.getString("nickname"))){
									nickname=jsons.getString("nickname");
									usert.setNickname(nickname);
								}
								if(!StringUtils.isBlank(jsons.getString("headimgurl"))){
									headicon=jsons.getString("headimgurl");
									usert.setHeadIcon(headicon);
								}
							}
						}
						catch(Exception e){
							//return ok(views.html.H5.FreeOne.freepro.render(pid.toString(),count,freetime,fp,unionid,"0"));
						}

						usert.setUnionid(unionid);
						usert.setOpenId(openid);
						
							//检查用户是否领取过，如果领取过直接跳转到领取列表页面
							boolean suc=userService.addfreeone("", Numbers.parseLong(StringUtil.getSystemConfigValue("Free_One_pro_pid"), 0L), "", unionid,"check","freeOne","","");
							if(suc){
								String phone=userService.getFreePhone(unionid);
								session("free_invitefromid", phone);
								return redirect("/H5/freelist?p="+fp+"&iswx=1&unionid="+unionid);
							}
							else{	
								userService.addfreeone("", pid, fp, unionid,"save","freeOne",usert.getNickname(),usert.getHeadIcon());
								return ok(views.html.H5.FreeOne.freepro.render(pid.toString(),count,freetime,fp,unionid,"1"));
							}
					}
				}
			}catch (Exception e) {}
		
		return ok(views.html.H5.FreeOne.freepro.render(pid.toString(),count,freetime,fp,"","0"));
  	}
  	//授权后或普通流览器首页面
	public Result getFree(){
		Long pid=Numbers.parseLong(StringUtil.getSystemConfigValue("Free_One_pro_pid"), 0L);
		String freetime =StringUtil.getSystemConfigValue("Free_One_FreeTime");
		String count=StringUtil.getSystemConfigValuenocache("Free_One_pro_count");
		String unionid=AjaxHellper.getHttpParam(request(), "unid");
		StringBuilder ctr=new StringBuilder();
		ctr.append("<b>");
		if(!StringUtils.isBlank(count)){
			count=String.valueOf(Numbers.parseInt(count, 0)*3);
			
			int cl=count.length();
			for(int i=0;i<cl;i++){
				ctr.append(count.substring(i,i+1)+"</b><b>");
			}
			count=ctr.toString().substring(0,ctr.length()-3);
		}

		String fp=AjaxHellper.getHttpParam(request(), "fp");
		String iswx=AjaxHellper.getHttpParam(request(), "iswx");
		if(StringUtils.isBlank(iswx))
			iswx="0";
		
		if(StringUtils.isBlank(fp))
			fp="";
		if(!StringUtil.checkPhone(fp))
			fp="";
		//如果不是领取用户手机号则设为空，归避盗刷
		if(!userService.addfreeone(fp, pid, "", unionid, "check", "freeOne", "",""))
			fp="";
		
		
		if(StringUtils.isBlank(unionid))
			unionid="";
		
		
		if(pid==0L)
			return ok(views.html.H5.pageError.render());
		
		
		return ok(views.html.H5.FreeOne.freepro.render(pid.toString(),count,freetime,fp,unionid,iswx));
	}
	
	//免费领
	public Result savefree(){
		response().setContentType("application/json;charset=utf-8");
		ObjectNode result=Json.newObject();
		result.put("status", "0");
		String phone=AjaxHellper.getHttpParamOfFormUrlEncoded(request(), "phone");
		String unionid=AjaxHellper.getHttpParamOfFormUrlEncoded(request(), "unionid");
		//unionid="ocyEct-zfKKZJsqcV_LFMWAjlqoE";
		String vsms=AjaxHellper.getHttpParamOfFormUrlEncoded(request(), "vsms");
		String fromphone=AjaxHellper.getHttpParamOfFormUrlEncoded(request(), "fromphone");
		Long pid=Numbers.parseLong(StringUtil.getSystemConfigValue("Free_One_pro_pid"), 0L);
		Integer count=Integer.valueOf(StringUtil.getSystemConfigValuenocache("Free_One_pro_count"));
		if(pid==0L || StringUtils.isBlank(phone)){
			return ok(views.html.H5.pageError.render());
		}
		if(StringUtils.isBlank(vsms) || !userService.checkverifysms(phone, vsms)){
			result.put("status", "2");
			result.put("msg", "验证码不正确");
			return ok(result);
		}
		boolean suc=false;
		if(count>1){
			suc=userService.addfreeone(phone, pid, fromphone, unionid,"save","freeOne","","");
			result.put("status", "1");
			if(suc)
			{
				userService.updateSystemConfig("Free_One_pro_count", String.valueOf((count-1)));
				cache.clear("SystemConfig_higou");
			}
			else{
				result.put("msg", "每个手机号只能领购领取一次");
				result.put("status", "0");
			}
		}
		
		session("free_invitefromid",phone);	
		if(suc && !StringUtils.isBlank(fromphone)){
			//检查是否领过此券，没领入券
				//928407发送领取短信
				List<String> coplist=new ArrayList<String>();
				coplist.add("264");
				coplist.add("262");
				coplist.add("263");
				
				List<String> smslist=new ArrayList<String>();
				smslist.add("5元现金");
				smslist.add("北海道马油商品");
				smslist.add("SNP面膜商品");
				List<User> ulist=userService.getInviteUserList(fromphone,"freeOne");
				String smstext=smslist.get(0);
				String copuonid="0";
				if(ulist==null || ulist.isEmpty())
				{	
					copuonid=coplist.get(0);
					smstext=smslist.get(0);
				}else
				{
					if(ulist.size()<5)
					{
						smstext=smslist.get(0);
						copuonid=coplist.get(0);
					}
					else
					{
						if(ulist.size()<10){
							smstext=smslist.get(1);
							copuonid=coplist.get(1);
						}
						else
						{
							smstext=smslist.get(2);
							copuonid=coplist.get(2);
						}
					}
				}
				List<Coupon> mclist = couponService.getUserCoupon(fromphone, Numbers.parseLong(copuonid, 0L));
				if(mclist==null || mclist.isEmpty()){
					//放券
					couponService.insertCouponPone(Numbers.parseLong(copuonid, 0L), fromphone, "");
					//如果是注册用户绑定
					User user=userService.getUserByphone(fromphone);
					if(user!=null){
						Coupon_user cu=new Coupon_user();
						cu.setCouponId(Numbers.parseLong(copuonid, 0L));
						cu.setCoupon_code("");
						cu.setFromuserId(0L);
						cu.setSource("0");
						cu.setDate_add(new Date());
						cu.setUid(user.getUid());
						cu.setStates("0");
						couponService.savecouponUser(cu);	
						
					}
					//发短信
					smsService.saveSmsInfo("#name#="+smstext,fromphone,"928407","2");
				}
				
				result.put("status", "1");
			
			//存cookie
			flash("free_invitefromid", phone);
			//session("free_invitefromid",phone);			
		}

		return ok(result);
	}
	
	
	//攻击
	public Result sd(){
		return null;
	}
	//免费领列表
	public Result freelist(){
		String phone= session("free_invitefromid");//AjaxHellper.getHttpParam(request(), "p");//;//取cookie//
		String fphone=AjaxHellper.getHttpParam(request(), "fp");
		String unionid="";

		String iswx=AjaxHellper.getHttpParam(request(), "iswx");
		if(StringUtils.isBlank(iswx))
			iswx="1";
//		if(iswx.equals("1")){
//			//phone=AjaxHellper.getHttpParam(request(), "p");
//			unionid=AjaxHellper.getHttpParam(request(), "unionid");
//		}
		

		//根据unionid获取获得的手机号码
		if(!StringUtils.isBlank(unionid))
			phone=userService.getFreePhone(unionid);
		
		String phoneselect=phone;
		if(!StringUtils.isBlank(phone) && phone.length()>10)
			phone=phone.substring(0,3)+"****"+phone.substring(7);
		
		if(StringUtils.isBlank(phone)){
			return redirect("/H5/freeauth");
		}
		
		List<User> invitulist=userService.getInviteUserList(phoneselect, "freeOne");
		
		Integer invitecount=invitulist==null || invitulist.isEmpty()?0:invitulist.size();
		
		List<User> ulist=userService.getFreeUserList(phoneselect,"freeOne",0,50);
		
		if(ulist!=null && !ulist.isEmpty()){
			List<User> utlist=new ArrayList<User>();
			for(User u:ulist){
				u.setToken(CHINESE_DATE_TIME_FORMAT_DAYTIME.format(u.getDate_add()));
				if(StringUtils.isBlank(u.getNickname()))
					u.setNickname(u.getPhone().substring(0,3)+"****"+u.getPhone().substring(7));
				utlist.add(u);
			}
			ulist=utlist;
		}
		
		//取签名
		WxSign wxsign=null;
		//if(iswx.equals("1")){
			wxsign=this.getwxstr();
			wxsign.setShareurl(StringUtil.getDomainH5()+"/H5/freeauth?fp="+phoneselect);
			wxsign.setSharetitle("免费送！免费送！免费送！828嗨个购大学生海淘节，预热送礼第一弹");
			wxsign.setSharecontent("白给的你都不要吗？");
		//}
		
		return ok(views.html.H5.FreeOne.freelist.render(ulist,wxsign,iswx,phone,invitecount));
	}
	
	/*
  	 * 客户端领取
  	 */
  	public Result freeget(){
  		String uid=AjaxHellper.getHttpParam(request(), "uid");
  		String islogin=AjaxHellper.getHttpParam(request(), "islogin");
  		if(StringUtils.isBlank(uid))
  			uid="";
  		if(StringUtils.isBlank(islogin))
  			islogin="";
  		
//  		if(StringUtils.isBlank(uid) || StringUtils.isBlank(islogin))
//  			return ok(views.html.H5.pageError.render());
  		
  		String acttim=StringUtil.getSystemConfigValue("Free_One_FreeTime");
  		return ok(views.html.H5.FreeOne.freeget.render(uid,islogin,acttim));
  	}
  	
  	/*
  	 * 客户端领取资格鉴权
  	 */
  	public Result freeAuthClien(){
  		ObjectNode result=Json.newObject();
  		Long uid=Numbers.parseLong(AjaxHellper.getHttpParamOfFormUrlEncoded(request(), "uid"), 0L);
  		if(uid==0L){
  			result.put("status", "0");
  			result.put("msg", "操作失败！");
  			return ok(result);
  		}
  		String msg=userService.checkuserfree(uid);
  		//成功后跳转到
  		if(StringUtils.isBlank(msg)){
  			//return redirect("pDe://higegou?pid="+StringUtil.getSystemConfigValue("Free_One_pro_pid"));
  			result.put("status", "1");
  			result.put("pid", StringUtil.getSystemConfigValue("Free_One_pro_pid"));
  		}
  		else{
  			result.put("status", "0");
  			result.put("msg", msg);
  		}
  		return ok(result);
  	}
 
	/////////////////////免费送第一小结束///////////////////////////////////////
	
  	/*****签到***************************************************************/
  	public Result getloginsign(){
  		Long uid=Numbers.parseLong(AjaxHellper.getHttpParamOfFormUrlEncoded(request(), "uid"), 0L);
  		Integer islogin=Numbers.parseInt(AjaxHellper.getHttpParamOfFormUrlEncoded(request(), "islogin"),0);
  		
  		if(uid==0L || islogin==0)
  		{
  			islogin=0;
  		}
  		
  		Integer logincount=0;
  		logincount=userService.loginact(uid);
  		int timeout=24;
  		Date dh=new Date();
  		timeout=24-dh.getHours();
  		
  		cache.setWithOutTime("hasact"+uid, "1", timeout*3600);
  		//session("hasact","1");
  		ObjectNode result=Json.newObject();
  		result.put("status", 1);
  		result.put("count", logincount);
  		return ok(result);
  		//return ok(views.html.H5.FreeOne.loginsign.render(logincount,islogin,1,uid.toString()));
  	}
  	
  	/*
  	 * 签到列表
  	 */
  	public Result loginact(){
  		Long uid=Numbers.parseLong(AjaxHellper.getHttpParam(request(), "uid"), 0L);
  		Integer islogin=Numbers.parseInt(AjaxHellper.getHttpParam(request(), "islogin"),0);
  		String token=AjaxHellper.getHttpParam(request(), "token");
  		String devid = AjaxHellper.getHttpParam(request(),"devid");
  		
  		String uuid = "";
  		try{
  			uuid = request().cookie("token").value().toString();
  			logger.info("uuid==============="+uuid);
  		}catch(Exception ex){
  			uuid="";
  		}
  		if(StringUtils.isBlank(uuid) && !StringUtils.isBlank(token)){
  			uuid=token;
  		}
  		
  		Integer hasact=0;//Numbers.parseInt(cache.get("hasact"+uid), 0);
  		if (!StringUtils.isBlank(uuid)){
  			// 获取签名逻辑
  			Map<String, String> mayarray=userService.getUserId_ByGuid(uid.intValue(),devid,uuid, "1");
			String Guid=mayarray.get("guid");
			Integer userid = Numbers.parseInt(mayarray.get("userid"), 0);
  			if(userid>0)
  			{
  				uid=Numbers.parseLong(userid.toString(), 0L);
  				islogin=1;
  			}
  		}else{
	  		if(uid==0L || islogin==0)
	  		{
	  			islogin=0;
	  		}
  		}	
  		Integer logincount=0;
  		List<Integer> loginlist=userService.getLoginSign(uid);
  		if(loginlist!=null && !loginlist.isEmpty()){
  			logincount=loginlist.get(0);
  			hasact=loginlist.get(1);
  		}
  		//logger.info("-------------"+cache.get("hasact"+uid));
  		
  		
  		return ok(views.html.H5.FreeOne.loginsign.render(logincount,islogin,hasact,uid.toString()));
  	}
  	/*****签到结束***********************************************************/
  	
  	/********垛手领*********************************************************/
  	public Result getdscoupon(){
  		Long uid=Numbers.parseLong(AjaxHellper.getHttpParam(request(), "uid"), 0L);
  		Integer islogin=Numbers.parseInt(AjaxHellper.getHttpParam(request(), "islogin"),0);
  		if(uid==0L)
  			islogin=0;
  		boolean hasget=false;
  		if(uid>0L)
  			hasget=userService.checkSendCoupon(uid);
  		
  		if(hasget)
  			return redirect("/H5/sendcouplist?uid="+uid);
  		else
  			return ok(views.html.H5.FreeOne.getcoupon.render(uid.toString(),islogin));
  	}
  	
  	/*
  	 * 领券
  	 */
  	public Result getcouonsave(){
  		Long uid=Numbers.parseLong(AjaxHellper.getHttpParamOfFormUrlEncoded(request(), "uid"), 0L);
  		ObjectNode result=Json.newObject();
  		if(uid>0L){
  			userService.getSendCoupon(uid);
  			result.put("status", "1");
  		}
  		else
  			result.put("status", "0");
  		
  		return ok(result);
  	}
  	/*
  	 * 领券列表
  	 */
  	public Result getdscouponlist(){
  		Long uid=Numbers.parseLong(AjaxHellper.getHttpParam(request(), "uid"), 0L);
  		boolean hasget=false;
  		if(uid>0L)
  			hasget=userService.checkSendCoupon(uid);
  		if(hasget)
  			return ok(views.html.H5.FreeOne.dscouponlist.render());
  		else
  			return ok(views.html.H5.pageError.render());
  	}
  	/********垛手领结束******************************************************/
  	
  	/**********免费送第二波***************************************************/  	
  //跳转分享，区分微信微博
  	public Result freetwoauth(){
  		String fp=AjaxHellper.getHttpParam(request(), "fp");
  		return ok(views.html.H5.FreeTwo.freetwoauth.render(fp));
  	}
  	
  //微信鉴权
  	public static Result freetwowxauth(){
  		String fromphone=AjaxHellper.getHttpParam(request(), "fp");
		String redirecturl=java.net.URLEncoder.encode(StringUtil.getDomainH5()+"/H5/vokeFreetwo?fp="+fromphone);
		String wxauthURL="https://open.weixin.qq.com/connect/oauth2/authorize?appid="+Constants.WXappID+"&redirect_uri="+redirecturl+"&response_type=code&scope=snsapi_userinfo&state=123#wechat_redirect";
		return redirect(wxauthURL);
  	}
  //微信二次鉴权
  	public Result vokeFreetwo(){
  		String code=AjaxHellper.getHttpParam(request(), "code");
  		String fromphone=AjaxHellper.getHttpParam(request(), "fp");
  		
		String openid="";
		String unionid="";
		String access_token="";
		//微信第二步认证
		User usert=new User();
		String wxurl="https://api.weixin.qq.com/sns/oauth2/access_token?appid="+Constants.WXappID+"&secret="+Constants.WXappsecret+"&code="+code+"&grant_type=authorization_code";
		try {
			// 发送请求，返回json
			TenpayHttpClient httpClient = new TenpayHttpClient();
			httpClient.setReqContent(wxurl);
			String resContent = "";
			if (httpClient.callHttpPost(wxurl, "")){
				resContent = httpClient.getResContent();
				Logger.info("weixin第二步返回"+resContent);
				
				JSONObject json=JSONObject.fromObject(resContent);

				try{
					if (!StringUtils.isBlank(json.getString("openid"))) {
						// 更新application值.
						openid = json.getString("openid");
						usert.setOpenId(openid);
					}
					if(!StringUtils.isBlank(json.getString("unionid"))){
						unionid=json.getString("unionid");
						usert.setUnionid(unionid);
					}
					if(!StringUtils.isBlank(json.getString("access_token")))
						access_token=json.getString("access_token");
				}
				catch(Exception e){}
				access_token=access_token==null?"":access_token;
				openid=openid==null?"":openid;
				unionid=unionid==null?"":unionid;
				//检查并获取用户基本信息
				if(!access_token.equals("") && !openid.equals("") && !unionid.equals("")){
					//如果用户存在则跳到购物页面
					String nickname="";
					String headicon="";

						String wxgetinfourl="https://api.weixin.qq.com/sns/userinfo?access_token="+access_token+"&openid="+openid+"&lang=zh_CN";
							try{
							// 发送请求，返回json
							HttpClient clientc = new DefaultHttpClient();
							HttpGet get = new HttpGet(wxgetinfourl);
							
							String resContenti = "";
							HttpResponse resc = clientc.execute(get);
							if (resc.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
								HttpEntity entity = resc.getEntity();
								resContenti = EntityUtils.toString(resc.getEntity(),"UTF-8");
								Logger.info("免费活动调用getAccessToken 接口返回报文内容:" + resContenti);				
								JSONObject jsons = JSONObject.fromObject(resContenti);
								if(!StringUtils.isBlank(jsons.getString("nickname"))){
									nickname=jsons.getString("nickname");
									usert.setNickname(nickname);
								}
								if(!StringUtils.isBlank(jsons.getString("headimgurl"))){
									headicon=jsons.getString("headimgurl");
									usert.setHeadIcon(headicon);
								}
							}
						}
						catch(Exception e){
							//return ok(views.html.H5.FreeOne.freepro.render(pid.toString(),count,freetime,fp,unionid,"0"));
						}

						usert.setUnionid(unionid);
						usert.setOpenId(openid);
						
							//检查用户是否领取过，如果领取过直接跳转到领取列表页面						
						boolean suc=userService.addfreeone("", 0L, "", unionid,"check","freeTwo","","");
						if(suc){
							String phone=userService.getFreePhone(unionid);
							session("freetwo_phone", phone);
							session("freetwo_unionid",unionid);
							return redirect("/H5/freetwolist?iswx=1");
						}
						else{	
							userService.addfreeone("", 0L, fromphone, unionid,"save","freeTwo",usert.getNickname(),usert.getHeadIcon());
							//return ok(views.html.H5.FreeTwo.freetwoget.render(unionid,1,fromphone));
							return redirect("/H5/freetwoget?iswx=1&fp="+fromphone+"&unionid="+unionid);
						}
					}
				}
			}catch (Exception e) {}
		
		return redirect("/H5/freetwoget?iswx=1&fp="+fromphone+"&unionid="+unionid);
  	}
  	
  	//发验证码领券
  	public Result savefreetwo(){
		response().setContentType("application/json;charset=utf-8");
		ObjectNode result=Json.newObject();
		result.put("status", "0");
		String phone=AjaxHellper.getHttpParamOfFormUrlEncoded(request(), "phone");
		String unionid=AjaxHellper.getHttpParamOfFormUrlEncoded(request(), "unionid");
		String fromphone=AjaxHellper.getHttpParamOfFormUrlEncoded(request(), "fromphone");
		String vsms=AjaxHellper.getHttpParamOfFormUrlEncoded(request(), "vsms");
		fromphone=StringUtils.isBlank(fromphone)?"":fromphone;
		unionid=StringUtils.isBlank(unionid)?"":unionid;
		phone=StringUtils.isBlank(phone)?"":phone;
		
		String ip=request().remoteAddress();

		result.put("status", "1");
		result.put("msg", "28元优惠券");
		
		String errmsg=userService.sendFreeCoupon(phone, fromphone, ip, vsms, unionid, "freeTwo");
		if(!StringUtils.isBlank(errmsg)){
			result.put("status", errmsg.substring(0,errmsg.indexOf("_")));
			result.put("msg", errmsg.substring(errmsg.indexOf("_")+1));
		}else{
			result.put("status", "0");
			result.put("msg", "领取失败");
		}
		session("freetwo_phone",phone);	
		session("freetwo_unionid",unionid);
		return ok(result);
  	}
  	//领取成功页面
  	public Result savefreetwosuc(){
  		String phone=session("freetwo_phone");
  		Integer iswx=Numbers.parseInt(AjaxHellper.getHttpParam(request(), "iswx"), 0);
  		if(StringUtils.isBlank(phone))
  			return redirect("/H5/freetwoauth");
  		
  		String phoneselect=phone;
		if(!StringUtils.isBlank(phone) && phone.length()>10)
			phoneselect=phone.substring(0,3)+"****"+phone.substring(7);

		List<User> ulist=userService.getFreetwoUserList(phone,"FreeTwo",0,50);
		
		List<User> invitelist=userService.getInviteUserList(phone, "freeTwo");
		Integer invtecount=invitelist==null||invitelist.isEmpty()?0:invitelist.size();
		
		if(ulist!=null && !ulist.isEmpty()){
			List<User> utlist=new ArrayList<User>();
			for(User u:ulist){
				u.setToken(CHINESE_DATE_TIME_FORMAT_DAYTIME.format(u.getDate_add()));
				if(StringUtils.isBlank(u.getNickname()))
					u.setNickname(u.getPhone().substring(0,3)+"****"+u.getPhone().substring(7));
				utlist.add(u);
			}
			ulist=utlist;
		}
		WxSign wxsign=null;
		//if(iswx.equals("1")){
			wxsign=this.getwxstr();
			wxsign.setShareurl(StringUtil.getDomainH5()+"/H5/freetwoauth?fp="+phone);
			wxsign.setSharetitle("免费送！免费送！免费送！828嗨个购大学生海淘节预热第二弹");
			wxsign.setSharecontent("白给的你都不要吗？");
		//}

  		return ok(views.html.H5.FreeTwo.freetwosuc.render(phoneselect,iswx,ulist,wxsign,invtecount));
  	}
  	
  	/*
  	 * 领券初始化页面
  	 */
  	public Result freetwoget(){
  		Integer iswx=Numbers.parseInt(AjaxHellper.getHttpParam(request(), "iswx"), 0);
  		String unionid=AjaxHellper.getHttpParam(request(), "unionid");
  		String fromphone=AjaxHellper.getHttpParam(request(), "fp");
  		WxSign wxsign=null;
		//if(iswx.equals("1")){
			wxsign=this.getwxstr();
			wxsign.setShareurl(StringUtil.getDomainH5()+"/H5/freetwoauth?fp="+fromphone);
			wxsign.setSharetitle("免费送！免费送！免费送！828嗨个购大学生海淘节预热第二弹");
			wxsign.setSharecontent("白给的你都不要吗？");
		//}
  		return ok(views.html.H5.FreeTwo.freetwoget.render(unionid,iswx,fromphone,wxsign));
  	}
  	//
  	/**********免费送第二波结束************************************************/
  	/**********棒棒糖活动*****************************************************/
  	/*
  	 * 访问商品
  	 */
  	public Result bbtpro(){
  		List<Product> plist=new ArrayList<Product>();
  		Product pro=productService.getProductById(Long.valueOf("3990"));
  		if(pro!=null){
	  		pro.setRmbprice(pro.getRmbprice());
	  		plist.add(pro);
  		}
  		else{
  			pro=new Product();
  			pro.setPid(0L);
  			pro.setRmbprice(Double.valueOf("0"));
  			pro.setTitle("no data");
  			plist.add(pro);
  		}
  		pro=productService.getProductById(Long.valueOf("4000"));
  		if(pro!=null){
	  		pro.setRmbprice(pro.getRmbprice());
	  		plist.add(pro);
  		}
  		else{
  			pro=new Product();
  			pro.setPid(0L);
  			pro.setRmbprice(Double.valueOf("0"));
  			pro.setTitle("no data");
  			plist.add(pro);
  		}
  		pro=productService.getProductById(Long.valueOf("4081"));
  		if(pro!=null){
	  		pro.setRmbprice(pro.getRmbprice());
	  		plist.add(pro);
  		}
  		else{
  			pro=new Product();
  			pro.setPid(0L);
  			pro.setRmbprice(Double.valueOf("0"));
  			pro.setTitle("no data");
  			plist.add(pro);
  		}
  		pro=productService.getProductById(Long.valueOf("4076"));
  		if(pro!=null){
	  		pro.setRmbprice(pro.getRmbprice());
	  		plist.add(pro);
  		}else{
  			pro=new Product();
  			pro.setPid(0L);
  			pro.setRmbprice(Double.valueOf("0"));
  			pro.setChinaprice(Double.valueOf("0"));
  			pro.setTitle("no data");
  			plist.add(pro);
  		}
  		
  		pro=productService.getProductById(Long.valueOf("4078"));
  		if(pro!=null){
	  		pro.setRmbprice(pro.getRmbprice());
	  		plist.add(pro);
  		}else{
  			pro=new Product();
  			pro.setPid(0L);
  			pro.setRmbprice(Double.valueOf("0"));
  			pro.setChinaprice(Double.valueOf("0"));
  			pro.setTitle("no data");
  			plist.add(pro);
  		}
  		
  		pro=productService.getProductById(Long.valueOf("4075"));
  		if(pro!=null){
	  		pro.setRmbprice(pro.getRmbprice());
	  		plist.add(pro);
  		}else{
  			pro=new Product();
  			pro.setPid(0L);
  			pro.setRmbprice(Double.valueOf("0"));
  			pro.setChinaprice(Double.valueOf("0"));
  			pro.setTitle("no data");
  			plist.add(pro);
  		}
  		
  		pro=productService.getProductById(Long.valueOf("3997"));
  		if(pro!=null){
	  		pro.setRmbprice(pro.getRmbprice());
	  		plist.add(pro);
  		}else{
  			pro=new Product();
  			pro.setPid(0L);
  			pro.setRmbprice(Double.valueOf("0"));
  			pro.setChinaprice(Double.valueOf("0"));
  			pro.setTitle("no data");
  			plist.add(pro);
  		}
  		
  		pro=productService.getProductById(Long.valueOf("3998"));
  		if(pro!=null){
	  		pro.setRmbprice(pro.getRmbprice());
	  		plist.add(pro);
  		}else{
  			pro=new Product();
  			pro.setPid(0L);
  			pro.setRmbprice(Double.valueOf("0"));
  			pro.setChinaprice(Double.valueOf("0"));
  			pro.setTitle("no data");
  			plist.add(pro);
  		}
  		
  		
  		WxSign wxs=new WxSign();
		wxs=getwxstr();
		wxs.setSharecontent("马上抢，手慢无！");
		wxs.setSharetitle("进口爆款零食一小时到身边！限量每天1000份!");
		wxs.setShareurl(StringUtil.getDomainH5()+"/H5/bbtpro");
		wxs.setShareimg(CdnAssets.CDN_API_PUBLIC_URL+"images/H5/bbt/banner.jpg");
  		return ok(views.html.H5.act1.bbtpro.render(wxs,plist));
  	}
  	
  	/*
  	 * 棒棒糖大望路
  	 */
  	public Result bbtpro_dawanglu(){
  		List<Product> plist=new ArrayList<Product>();
  		Product pro=productService.getProductById(Long.valueOf("4002"));
  		if(pro!=null){
	  		plist.add(pro);
  		}
  		else{
  			pro=new Product();
  			pro.setPid(0L);
  			pro.setRmbprice(Double.valueOf("0"));
  			pro.setChinaprice(Double.valueOf("0"));
  			pro.setTitle("no data");
  		}
  		pro=productService.getProductById(Long.valueOf("4003"));
  		if(pro!=null){
	  		plist.add(pro);
  		}
  		else{
  			pro=new Product();
  			pro.setPid(0L);
  			pro.setRmbprice(Double.valueOf("0"));
  			pro.setChinaprice(Double.valueOf("0"));
  			pro.setTitle("no data");
  			plist.add(pro);
  		}
  		pro=productService.getProductById(Long.valueOf("4005"));
  		if(pro!=null){
	  		plist.add(pro);
  		}
  		else{
  			pro=new Product();
  			pro.setPid(0L);
  			pro.setRmbprice(Double.valueOf("0"));
  			pro.setChinaprice(Double.valueOf("0"));
  			pro.setTitle("no data");
  			plist.add(pro);
  		}
  		pro=productService.getProductById(Long.valueOf("4004"));
  		if(pro!=null){
	  		plist.add(pro);
  		}
  		else{
  			pro=new Product();
  			pro.setPid(0L);
  			pro.setChinaprice(Double.valueOf("0"));
  			pro.setRmbprice(Double.valueOf("0"));
  			pro.setTitle("no data");
  			plist.add(pro);
  		}
  		/*
  		pro=productService.getProductById(Long.valueOf("4075"));
  		if(pro!=null){
	  		pro.setRmbprice(pro.getRmbprice());
	  		plist.add(pro);
  		}  
  		else{
  			pro=new Product();
  			pro.setPid(0L);
  			pro.setRmbprice(Double.valueOf("0"));
  			pro.setChinaprice(Double.valueOf("0"));
  			pro.setTitle("no data");
  			plist.add(pro);
  		}
  		
  		pro=productService.getProductById(Long.valueOf("4081"));
  		if(pro!=null){
	  		pro.setRmbprice(pro.getRmbprice());
	  		plist.add(pro);
  		}
  		else{
  			pro=new Product();
  			pro.setPid(0L);
  			pro.setRmbprice(Double.valueOf("0"));
  			pro.setChinaprice(Double.valueOf("0"));
  			pro.setTitle("no data");
  			plist.add(pro);
  		}
  		
  		pro=productService.getProductById(Long.valueOf("4076"));
  		if(pro!=null){
	  		pro.setRmbprice(pro.getRmbprice());
	  		plist.add(pro);
  		}else{
  			pro=new Product();
  			pro.setPid(0L);
  			pro.setRmbprice(Double.valueOf("0"));
  			pro.setChinaprice(Double.valueOf("0"));
  			pro.setTitle("no data");
  			plist.add(pro);
  		}
  		
  		pro=productService.getProductById(Long.valueOf("4078"));
  		if(pro!=null){
	  		pro.setRmbprice(pro.getRmbprice());
	  		plist.add(pro);
  		}else{
  			pro=new Product();
  			pro.setPid(0L);
  			pro.setRmbprice(Double.valueOf("0"));
  			pro.setChinaprice(Double.valueOf("0"));
  			pro.setTitle("no data");
  			plist.add(pro);
  		}
  		
  		pro=productService.getProductById(Long.valueOf("3993"));
  		if(pro!=null){
	  		pro.setRmbprice(pro.getRmbprice());
	  		plist.add(pro);
  		}else{
  			pro=new Product();
  			pro.setPid(0L);
  			pro.setRmbprice(Double.valueOf("0"));
  			pro.setChinaprice(Double.valueOf("0"));
  			pro.setTitle("no data");
  			plist.add(pro);
  		}  		
  		*/
  		WxSign wxs=new WxSign();
		wxs=getwxstr();
		wxs.setSharecontent("马上抢，手慢无！");
		wxs.setSharetitle("进口爆款零食一小时到身边！限量每天1000份!");
		wxs.setShareurl(StringUtil.getDomainH5()+"/H5/bbtpro");
		wxs.setShareimg(CdnAssets.CDN_API_PUBLIC_URL+"images/H5/bbt/banner.jpg");
  		return ok(views.html.H5.act1.bbtpro_dawanglu.render(wxs,plist));
  	}
	/*
	 * 棒棒糖O2O下单接口返回
	 */
	public Result sendBBTOrder(){
		response().setContentType("application/json;charset=utf-8");
		String pid=AjaxHellper.getHttpParamOfFormUrlEncoded(request(), "pid");
		String ordercode=AjaxHellper.getHttpParamOfFormUrlEncoded(request(), "ordercode");
		ShoppingOrder order=shoppingOrderService.getShoppingOrderByOrderCode(ordercode);
		Product pro=productService.getProductById(Numbers.parseLong(pid, 0L));
		ObjectNode re=Json.newObject();
		ObjectNode result=Json.newObject();
		result.put("status", "0");
		result.put("msg", "同步失败");
		if(order==null || pro==null){
			result.put("msg", "传递订单号错误");
			logger.info("棒棒糖同步："+ordercode+"，"+pid+"传递订单号或商品错误错误");
			return ok(result);
		}
		if(StringUtils.isBlank(order.getMcode()))
			order.setMcode("");
		
		Map<String,String> getmap=new HashMap<String,String>();
		String timstr= CHINESE_DATE_TIME_FORMAT_NORMAL.format(new Date());
		getmap.put("out_trade_no", order.getOrderCode());
		getmap.put("time_stamp", timstr);
		getmap.put("token", "aeb13b6a5cb82d43ce66b7f34f44c175");
		String signstr=StringUtil.makeSig(getmap);
		try{
			//String tm="mail_num=1234567890123&time_stamp=2015-08-27 16:41:58&token=993aecabe55ca20db52f186b6b5b726a";
			MessageDigest messageDigest = MessageDigest.getInstance("MD5");
            messageDigest.reset();
            messageDigest.update(signstr.getBytes("utf-8"));
            byte[] digest = messageDigest.digest(); 

            signstr=org.apache.commons.codec.binary.Base64.encodeBase64String(digest);
			logger.info(signstr);
		}
		catch(Exception e){}
		String mmcode=order.getMcode();
		if(mmcode.equals("bbt"))
			mmcode="higo-jwsh";
		else if(mmcode.equals("bbtdwl"))
			mmcode="higo-dwl";
		
		re.put("sign", signstr);
		re.put("time_stamp", timstr);
		re.put("out_trade_no", ordercode);
		re.put("goods_name", pro.getTitle());
		re.put("goods_detail",pro.getSubtitle());
		//re.put("goods_remark", pro.getSubtitle());
		re.put("receiver_name", order.getName());
		re.put("receiver_mobile",order.getPhone());
		re.put("receiver_address",order.getProvince()+order.getAddress());
		re.put("need_warm_box", "0");
		re.put("store_code", mmcode);
		TenpayHttpClient httpClient = new TenpayHttpClient();
		//测试环境：api.neolix.cn
		//生产环境：api.ibbt.com

		boolean IsProduct = Configuration.root().getBoolean("production", false);
		if(order!=null && !StringUtils.isBlank(mmcode)){
			String gettokenurl="http://api.neolix.cn/haigou/o2o/order/add";
			if(IsProduct)
				gettokenurl="http://api.ibbt.com/haigou/o2o/order/add";
			
			HttpClient client = new DefaultHttpClient();
			HttpPost post = new HttpPost(gettokenurl);
			post.setHeader("Content-Type", "text/json; charset=UTF-8");
			try {
				Logger.info("发送订单数据到棒棒糖："+re.toString());
				post.setEntity(new StringEntity(re.toString(),"UTF-8"));
				HttpResponse res = client.execute(post);
				String strResult = EntityUtils.toString(res.getEntity(), "UTF-8");
				try{
					JSONObject json=JSONObject.fromObject(strResult);
					String err=json.getString("result");
					if(err.equals("ok")){
						String billnum=json.getString("mail_num");
						//添加订单数据
						shoppingOrderService.addShoppingOrderEx(billnum, ordercode, order.getMcode());
						result.put("status", "1");
						result.put("msg","");
					}else{
						result.put("status", "0");
						result.put("msg",json.getString("message"));
					}
				}
				catch(Exception ee){}
	
				Logger.info("发送订单数据到棒棒糖返回结果："+strResult);	
			}
			catch(Exception e){}
		}
		return ok(result);
	}
  	/**********棒棒糖活动结束**************************************************/
	//组装微信签名返回参数数据
  	public static WxSign getwxstr(){
  		ICacheService cache = ServiceFactory.getCacheService();
  		String nostr=StringUtils.isBlank(cache.get("getwxSign_nostr"))?RandomStringUtils.randomAlphanumeric(16):cache.get("getwxSign_nostr");
  		String timstr=StringUtils.isBlank(cache.get("getwxSign_timstr"))?Sha1Util.getTimeStamp():cache.get("getwxSign_timstr");
  		String ticket=StringUtils.isBlank(cache.get("getwxSign_ticket"))?"":cache.get("getwxSign_ticket");//session("ticket");
  		String access_token=StringUtils.isBlank(cache.get("getwxSign_access_token"))?"":cache.get("getwxSign_access_token");
  		String resContent = "";
  		String sign="";
  		boolean IsProduct = Configuration.root().getBoolean("production", false);
  		String status="0";
  		TenpayHttpClient httpClient = new TenpayHttpClient();
  			if(StringUtils.isBlank(access_token)){
  				try
  				{
  					//清缓存
  					cache.clear("getwxSign_nostr");
  					cache.clear("getwxSign_timstr");
  					cache.clear("getwxSign_ticket");
  					nostr=RandomStringUtils.randomAlphanumeric(16);
  					timstr=Sha1Util.getTimeStamp();
  					String gettokenurl="https://api.weixin.qq.com/cgi-bin/token?grant_type=client_credential&appid="+Constants.WXappID+"&secret="+Constants.WXappsecret;
  					httpClient.setReqContent(gettokenurl);
  					if (httpClient.callHttpPost(gettokenurl, "")){
  							resContent = httpClient.getResContent();
  						Logger.info("微信请求access_token返回："+resContent);	
  						JSONObject json=JSONObject.fromObject(resContent);		
  						try{
  							access_token=json.getString("access_token");
  			  				cache.setWithOutTime("getwxSign_access_token", access_token.toString(),7200);
  			  				cache.setWithOutTime("getwxSign_nostr", nostr.toString(), 7200);
  			  				cache.setWithOutTime("getwxSign_timstr", timstr.toString(), 7200);
  			  				
  							}
  						catch(Exception ee){}
  						if(!StringUtils.isBlank(access_token)){
  							try{
  			  					//获取 jsp_tackit
  			  					String getTickaturl="https://api.weixin.qq.com/cgi-bin/ticket/getticket?access_token="+access_token+"&type=jsapi";
  			  					httpClient = new TenpayHttpClient();
  			  					httpClient.setReqContent(getTickaturl);
  			  					if (httpClient.callHttpPost(getTickaturl, "")){
  			  						resContent = httpClient.getResContent();
  			  						Logger.info("微信请求jsapi_ticket返回："+resContent);
  			  						JSONObject jsont=JSONObject.fromObject(resContent);
  			  						if(jsont.getString("errcode").equals("0")){
  			  							ticket=jsont.getString("ticket");
  			  							cache.setWithOutTime("getwxSign_ticket", ticket, 7150);
  			  						}
  			  					}
  			  				}
  			  				catch(Exception ee){}
  						}
  					}
  				}
  				catch(Exception e){}
  			}
  			else{
  				if(StringUtils.isBlank(ticket)){
  				try{
  					//获取 jsp_tackit
  					String getTickaturl="https://api.weixin.qq.com/cgi-bin/ticket/getticket?access_token="+access_token+"&type=jsapi";
  					httpClient = new TenpayHttpClient();
  					httpClient.setReqContent(getTickaturl);
  					if (httpClient.callHttpPost(getTickaturl, "")){
  						resContent = httpClient.getResContent();
  						Logger.info("微信请求jsapi_ticket返回："+resContent);
  						JSONObject jsont=JSONObject.fromObject(resContent);
  						if(jsont.getString("errcode").equals("0")){
  							ticket=jsont.getString("ticket");
  							cache.setWithOutTime("getwxSign_ticket", ticket, 7150);
  						}
  					}
  				}
  				catch(Exception ee){}
  				}
  			}
  			String qry="";
  			String vn="";
  			String vl="";
  			Map<String,String[]> maps=request().queryString();
  			if(maps!=null && maps.keySet()!=null){
  				Iterator<String> keyIt = maps.keySet().iterator();					
  				while(keyIt.hasNext()){
  					vn=keyIt.next();
  					vl=maps.get(vn)[0];
  					qry=qry+"&"+vn+"="+vl;
  				}
  			}
  						
  			if(!StringUtils.isBlank(qry))
  				qry="?"+qry.substring(1);
  			String dport=IsProduct==true?"":":9004";
  			String urls="http://"+request().host()+dport+request().path()+qry;
  			Logger.info("解析urls:"+urls);
  			Map<String, String> pramt = new HashMap<String, String>();
  			if(!StringUtils.isBlank(ticket)){
  				status="1";

  				pramt.put("timestamp",timstr);
  				pramt.put("noncestr", nostr);
  				pramt.put("jsapi_ticket", ticket);
  				pramt.put("url", urls);
  				sign=StringUtil.getShareSign(pramt);
  			}
  			ObjectNode re=Json.newObject();
  			WxSign wxsign=new WxSign();
  			wxsign.setNostr(nostr);
  			wxsign.setTimstr(timstr);
  			wxsign.setAppId(Constants.WXappID);
  			wxsign.setSign(sign);
  			wxsign.setSharecontent("我跟老板特别好，好友福利少不了");
  			wxsign.setSharetitle("我刚刚成为嗨个购代言人，免费送你海外商品");
  			wxsign.setShareimg(CdnAssets.CDN_API_PUBLIC_URL+"images/H5/act1/shareicon.jpg");
  			wxsign.setShareurl(StringUtil.getDomainH5()+"/H5/friendnew");
   			return wxsign;
  		} 

  	/*
  	 * 圣诞红包活动
  	 */
  	public Result christmas(){
  		Long uid=Numbers.parseLong(AjaxHellper.getHttpParam(request(), "uid"), 0L);
  		Integer islogin=Numbers.parseInt(AjaxHellper.getHttpParam(request(), "islogin"),0);
  		String token=AjaxHellper.getHttpParam(request(), "token");
  		String devid = AjaxHellper.getHttpParam(request(),"devid");
  		
  		String uuid = "";
  		try{
  			uuid = request().cookie("token").value().toString();
  			logger.info("uuid==============="+uuid);
  		}catch(Exception ex){
  			uuid="";
  		}
  		if(StringUtils.isBlank(uuid) && !StringUtils.isBlank(token)){
  			uuid=token;
  		}
  		logger.info("token..........."+token);
  		logger.info("uuid.............==============="+uuid);
  		Integer hasact=0;//Numbers.parseInt(cache.get("hasact"+uid), 0);
  		if (!StringUtils.isBlank(uuid)){
  			// 获取签名逻辑
  			Map<String, String> mayarray=userService.getUserId_ByGuid(uid.intValue(),devid,uuid, "1");
			String Guid=mayarray.get("guid");
			Integer userid = Numbers.parseInt(mayarray.get("userid"), 0);
  			if(userid>0)
  			{
  				uid=Numbers.parseLong(userid.toString(), 0L);
  				islogin=1;
  			}
  		}else{
	  		if(uid==0L || islogin==0)
	  		{
	  			islogin=0;
	  		}
  		}  		
  		
  		//if(islogin==0){
  			//return redirect("userLoginPage://");
  			//return ok(views.html.H5.gologin.render());
  		//}
  		//分配优惠券
  		List<Long> cids=new ArrayList<Long>();
  		cids.add(1630L);
  		cids.add(1631L);
  		cids.add(1632L);
  		cids.add(1633L);
  		cids.add(1634L);
  		cids.add(1635L);
  		cids.add(1636L);
  		cids.add(1638L);
//  		cids.add(469L);
//  		cids.add(470L);
//  		cids.add(471L);
//  		cids.add(472L);
//  		cids.add(473L);
//  		cids.add(474L);
//  		cids.add(475L);
//  		cids.add(476L);
  		for(Long cid:cids){
  			couponService.addUserCoupon(uid, cid);
  		}
  		return ok(views.html.H5.act1.christmas.render(islogin.intValue()+""));
  	}
}
