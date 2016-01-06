package controllers.api;

import java.math.BigDecimal;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import net.sf.json.JSONObject;

import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import models.Channel;
import models.Product;
import models.Product_images;
import models.User;
import models.WxSign;
import play.Configuration;
import play.Logger;
import play.libs.Json;
import play.mvc.Result;
import services.ICacheService;
import services.ServiceFactory;
import services.api.AppPadService;
import services.api.H5ShoppingService;
import services.api.ProductService;
import services.api.UserService;
import utils.AjaxHellper;
import utils.Constants;
import utils.Numbers;
import utils.StringUtil;
import utils.wxpay.Sha1Util;
import utils.wxpay.TenpayHttpClient;
import vo.appPad.appPadChannelVO;

@Named
@Singleton
public class H5ShoppingController extends BaseApiController{
	private static final Logger.ALogger logger = Logger.of(H5ProductController.class);
	private static AppPadService appPadService;
	private static H5ShoppingService H5Shoppingservice;
	private static UserService userService;
	private static ICacheService cache = ServiceFactory.getCacheService();
	private static ProductService productService;
	@Inject
	public H5ShoppingController(AppPadService appPadService,H5ShoppingService H5Shoppingservice,UserService userService,ProductService productService){
		this.appPadService=appPadService;
		this.H5Shoppingservice=H5Shoppingservice;
		this.userService = userService;
		this.productService=productService;
	}
	
	/*
	 * 商户商品列表
	 */
	public Result getProlist(){
		Long uid=Numbers.parseLong(AjaxHellper.getHttpParam(request(),"uid"), 0L);
		session("postmanuid","");
		if(uid.longValue()>0)
			session("postmanuid",uid.longValue()+"");
		
		if(session("op")==null || StringUtils.isBlank(session("op"))){
			//鉴权
			String backurl=StringUtil.getDomainH5()+"/H5/order_address?uid="+uid;
			backurl=backurl+"&flg=prolist";
			return redirect("https://open.weixin.qq.com/connect/oauth2/authorize?appid="+Constants.WXappID+"&redirect_uri="+URLEncoder.encode(backurl)+"&response_type=code&scope=snsapi_base&state=123&connect_redirect=1#wechat_redirect");
		}
		
		List<appPadChannelVO.Channel> clist=null;
		Integer page=Numbers.parseInt(AjaxHellper.getHttpParam(request(), "page"), 0);
		String key=AjaxHellper.getHttpParam(request(), "key");
		key=StringUtils.isBlank(key)?"":key;
		String cid=AjaxHellper.getHttpParam(request(), "cid");
		
		appPadChannelVO cvo=appPadService.getPadChannelList(uid.longValue()+"");

		if(cvo!=null){
			clist=cvo.getChannels();
			if(clist!=null && !clist.isEmpty() && StringUtils.isBlank(cid)){
				cid=clist.get(0).cid;
			}
		}

		Map<Integer,List<Product>> pmap=appPadService.getChannelProlist(uid+"", cid, key, page+"", 20+"");
		
		List<Product> plist=null;
		Integer totalcount=0;
		if(pmap!=null && pmap.keySet()!=null && pmap.size()>0){
			Iterator<Integer> keys=pmap.keySet().iterator();
			while(keys.hasNext()){
				totalcount=keys.next();
				plist=pmap.get(totalcount);
			}
		}

		if(plist!=null &&!plist.isEmpty()){
			for(Product proinfo:plist){
				List<Product_images> imglist=productService.getProductImages(proinfo.getPid());
				if(imglist!=null && !imglist.isEmpty())
					proinfo.setListpic(imglist.get(0).getPicname());
			}
		}
		logger.info("count------"+totalcount);
		WxSign wxsign=H5ProductController.getwxstr();
		if(uid.longValue()>0){
			User postman = userService.getUserByUid(uid);
			if(postman!=null){
				wxsign.setShareimg(StringUtil.getDomainH5()+"/public/images/sheSaidImages/hi.gif");
				wxsign.setSharetitle("我是快递员"+postman.getNickname()+",我能帮你带来最优惠的价格");
				wxsign.setSharecontent("全场19元起！全球生鲜极速达，好吃到停不下来！");
				wxsign.setShareurl(StringUtil.getDomainH5()+"/H5/prolist?uid="+postman.getUid());
			}else{
				wxsign.setSharecontent("Hi-嗨个购");
				wxsign.setSharetitle("嗨个购-与你一起买世界");
				wxsign.setShareimg(StringUtil.getDomainH5()+"/public/images/sheSaidImages/hi.gif");
				wxsign.setShareurl(StringUtil.getDomainH5()+"/H5/prolist?uid="+uid);
			}
		}else{
			wxsign.setSharecontent("Hi-嗨个购");
			wxsign.setSharetitle("嗨个购-与你一起买世界");
			wxsign.setShareimg(StringUtil.getDomainH5()+"/public/images/sheSaidImages/hi.gif");
			wxsign.setShareurl(StringUtil.getDomainH5()+"/H5/prolist?uid="+uid);
		}
		Double ct=Double.valueOf(totalcount.doubleValue()/Double.valueOf(20));
		BigDecimal cc=new BigDecimal(ct).setScale(0, BigDecimal.ROUND_UP);	
		Long buyuid=Numbers.parseLong(session("uid"),0L);
		String openid=session("op");
		String union=session("un");
		User user=null;
		if(!StringUtils.isBlank(openid) && !StringUtils.isBlank(union))
			user = userService.getUserByopenid(openid, union);
		if (user == null) {
			user = new User();
			user.setUid(0L);
			user.setOpenId(openid==null||StringUtils.isBlank(openid)?"":openid);
			user.setUnionid(union==null||StringUtils.isBlank(union)?"":union);
		}
		return ok(views.html.H5.bbt.prolist.render(clist,plist,uid,cid,cc.intValue(),wxsign,user));
	}
	
	/*
	 * 商户商品列表
	 */
	public Result getProlistJson(){
		Long uid=Numbers.parseLong(AjaxHellper.getHttpParam(request(),"uid"), 0L);		
		List<appPadChannelVO.Channel> clist=null;
		Integer page=Numbers.parseInt(AjaxHellper.getHttpParam(request(), "page"), 0);
		String key=AjaxHellper.getHttpParam(request(), "key");
		key=StringUtils.isBlank(key)?"":key;
		String cid=AjaxHellper.getHttpParam(request(), "cid");
		
		appPadChannelVO cvo=appPadService.getPadChannelList(uid.longValue()+"");

		if(cvo!=null){
			clist=cvo.getChannels();
			if(clist!=null && StringUtils.isBlank(cid)){
				cid=clist.get(0).cid;
			}
		}

		Map<Integer,List<Product>> pmap=appPadService.getChannelProlist(uid+"", cid, key, page+"", 20+"");
		
		List<Product> plist=null;
		Integer totalcount=0;
		if(pmap!=null && pmap.keySet()!=null && pmap.size()>0){
			Iterator<Integer> keys=pmap.keySet().iterator();
			while(keys.hasNext()){
				totalcount=keys.next();
				plist=pmap.get(totalcount);
			}
		}

		ObjectNode result=Json.newObject();
		result.put("status", "1");
		StringBuilder htmlstr=new StringBuilder();
		htmlstr.append("<ul class=\"content\">");
		if(plist!=null && !plist.isEmpty()){
			for(Product p:plist){
				List<Product_images> imglist=productService.getProductImages(p.getPid());
				if(imglist!=null && !imglist.isEmpty())
					p.setListpic(imglist.get(0).getPicname());
				
				htmlstr.append("<li onclick=\"gosaid('"+p.getPid()+"','"+p.getEndorsementId()+"')\">");
				htmlstr.append("<div class=\"pic-img\">");
				htmlstr.append("<img src=\""+p.getListpic()+"\"/>");
				htmlstr.append("</div>");
				htmlstr.append("<div class=\"name\">");
				htmlstr.append("<div class=\"name-info\">");
				htmlstr.append("<h2>"+p.getTitle()+"</h2>");
				//htmlstr.append("<p>"+p.getSubtitle()+"</p>");
				htmlstr.append("</div>");
				htmlstr.append("<div class=\"price\">");
				if(p.getEndorsementPrice().doubleValue()>new BigDecimal(p.getEndorsementPrice()).setScale(1, BigDecimal.ROUND_DOWN).doubleValue())
					htmlstr.append("<span>￥<b>"+p.getEndorsementPrice()+"</b>");
				else
					htmlstr.append("<span>￥<b>"+new BigDecimal(p.getEndorsementPrice()).setScale(1,BigDecimal.ROUND_DOWN).intValue()+"</b>");
				if(p.getChinaprice().doubleValue()>new BigDecimal(p.getChinaprice()).intValue())
					htmlstr.append("<s>￥"+p.getChinaprice()+"</s></span>");
				else
					htmlstr.append("<s>￥"+new BigDecimal(p.getChinaprice()).intValue()+"</s></span>");
				
				htmlstr.append("<em><a href=\"javsscript:;\">"+p.getZhekou()+"折</a></em>");
				htmlstr.append("</div>");
				htmlstr.append("</div>");
				//htmlstr.append("<div class=\"china\">");
				//htmlstr.append("<img src=\""+p.getNationalFlag()+"\">");
				//htmlstr.append("<p>"+p.getWayremark()+"</p>");
				//htmlstr.append("</div>");
				htmlstr.append("</li>");
			}
			htmlstr.append("</ul>");
			result.put("htmlstr", htmlstr.toString());
		}else{
			result.put("htmlstr", "");
			result.put("status", "0");
		}
		return ok(Json.toJson(result));
	}
	
	public Result vokeh5(){
		return null;
	}
	public static JsonNode getwxtoken(){
		String code=AjaxHellper.getHttpParam(request(), "code");
		String state=AjaxHellper.getHttpParam(request(), "state");
		if(StringUtils.isBlank(state))
			state="";
		
		if(code==null || code.equals("") || StringUtils.isBlank(state)){
			//用户取消或景鉴权失败
			return null;
		}

		String access_token="";
		String openid="";
		String unionid="";
		String refresh_token="";
		//先进行基础授权，如果是第一次没有用户妮称等信息则进行用户级授权		
				//微信第二步认证
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
							}
							if(!StringUtils.isBlank(json.getString("unionid"))){
								unionid=json.getString("unionid");
							}
							if(!StringUtils.isBlank(json.getString("access_token")))
								access_token=json.getString("access_token");
							if(!StringUtils.isBlank(json.getString("refresh_token")))
								refresh_token=json.getString("refresh_token");
						}
						catch(Exception e){}
						access_token=access_token==null?"":access_token;						
					}
				}catch (Exception e) {}
				if(!StringUtils.isBlank(access_token)){
					//微信鉴权延时
					String wxrefuressurl="https://api.weixin.qq.com/sns/oauth2/refresh_token?appid="+Constants.WXappID+"&grant_type=refresh_token&refresh_token="+refresh_token;
					try{ 
						HttpGet getr = new HttpGet(wxrefuressurl);
						HttpClient clientcr = new DefaultHttpClient();
						String resContentir = "";
						HttpResponse rescr = clientcr.execute(getr);
						if (rescr.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
							resContentir = EntityUtils.toString(rescr.getEntity(),"UTF-8");
							Logger.info("调用getRefreshAccessToken 接口返回报文内容:" + resContentir);				
							JSONObject jsons = JSONObject.fromObject(resContentir);
							if(!StringUtils.isBlank(jsons.getString("access_token"))){
								access_token=jsons.getString("access_token");							
							}
						}
					}catch(Exception e){}
					
					ObjectNode result=Json.newObject();
					result.put("access_token", access_token);
					result.put("code", code);
					result.put("state", state);
					result.put("openid", openid);
					result.put("unionid", unionid);
					result.put("timstr",RandomStringUtils.randomAlphanumeric(16));
					result.put("nostr", Sha1Util.getTimeStamp());	
					result.put("refresh_token",refresh_token);
					User ust=userService.getUserByopenid(openid, unionid);
					if(ust!=null && ust.getUid()!=null)
						session("uid",ust.getUid().longValue()+"");
					
					session("op",openid);
					session("un",unionid);
					
					session("cache_timstr",result.get("timstr").textValue());
					session("cache_nostr",result.get("nostr").textValue());
					session("cache_token",access_token);
					session("cache_code",code);
					session("cache_state",state);
					if(!StringUtils.isBlank(openid)){
						cache.setWithOutTime("wx_access_token_voke"+openid,access_token,5400);
						cache.setWithOutTime("wxaddress"+openid,openid+","+unionid+","+access_token+","+result.get("timstr").textValue()+","+result.get("nostr").textValue()+","+code+","+state,5400);
					}
					return Json.toJson(result);
				}
			return null;
	}
	
	/*
	 * 代言地址获取跳转
	 */
	public static Result wxauth_address(){
		String pid=AjaxHellper.getHttpParam(request(), "pid");
		String dyid=AjaxHellper.getHttpParam(request(), "daiyanid");
		pid=StringUtils.isBlank(pid)?"":pid;
		dyid=StringUtils.isBlank(dyid)?"":dyid;
		String backurl=StringUtil.getDomainH5()+"/H5/order_address?pid="+pid+"&daiyanid="+dyid;
		String flg=AjaxHellper.getHttpParam(request(), "flg");
		backurl=backurl+"&flg="+flg;
		return redirect("https://open.weixin.qq.com/connect/oauth2/authorize?appid="+Constants.WXappID+"&redirect_uri="+URLEncoder.encode(backurl)+"&response_type=code&scope=snsapi_base&state=123&connect_redirect=1#wechat_redirect");
	}
	/*
	 * 微信回调
	 */
	public Result order_address(){
		String pid=AjaxHellper.getHttpParam(request(), "pid");
		String dyid=AjaxHellper.getHttpParam(request(), "daiyanid");
		pid=StringUtils.isBlank(pid)?"":pid;
		dyid=StringUtils.isBlank(dyid)?"":dyid;
		String uid=AjaxHellper.getHttpParam(request(), "uid");
		if(uid==null || StringUtils.isBlank(uid))
			uid="";
		
		String flg=AjaxHellper.getHttpParam(request(), "flg");
		JsonNode wxobject=(JsonNode)getwxtoken();
		WxSign addrSign=null;
		if(wxobject!=null && wxobject.get("openid")!=null && !StringUtils.isBlank(wxobject.get("openid").textValue())){
			addrSign=new WxSign();
			addrSign.setAppId(Constants.WXappID);
			addrSign.setTimstr(wxobject.get("timstr").textValue());
			addrSign.setNostr(wxobject.get("nostr").textValue());
			addrSign.setOpenid(wxobject.get("openid").textValue());
			addrSign.setUnionid(wxobject.get("unionid").textValue());
			addrSign.setCode(wxobject.get("code").textValue());
			addrSign.setState(wxobject.get("state").textValue());
			addrSign.setAccess_token(wxobject.get("access_token").textValue());				
			session("iswx","1");
			Long sesuid=Numbers.parseLong(session("uid"), 0L);
			logger.info("wxaddress  flg........."+flg);
			if(sesuid.longValue()>0)
				cache.setObject("wxcontant"+session("uid"), addrSign, 5400);
			if(StringUtils.isBlank(flg))
				return redirect("/sheSaid/order?pid="+pid+"&daiyanid="+dyid+"&code="+addrSign.getCode()+"&state="+addrSign.getState());
			else if(flg.equals("cart"))
				return redirect("/sheSaid/ordercartcrm?code="+addrSign.getCode()+"&state="+addrSign.getState());
			else if(flg.equals("pro"))
				return redirect("/H5/orderpro?pid="+pid+"&code="+addrSign.getCode()+"&state="+addrSign.getState());
			else if(flg.equals("procart"))
				return redirect("/H5/orderprocartcrm?code="+addrSign.getCode()+"&state="+addrSign.getState());
			else if(flg.equals("shoplist"))
				return redirect("/H5/shoplist");
			else if(flg.equals("prolist"))
				return redirect("/H5/prolist?uid="+uid);
			
		}
		return redirect("/sheSaid/order?pid="+pid+"&daiyanid="+dyid);
	}
	/*
	 * 大转盘
	 */
	public static Result dazhuanpan(){		
		  //return -1;
		Integer leftcount=2;
		Long uid=Numbers.parseLong(AjaxHellper.getHttpParam(request(), "uid"), 0L);
  		Integer islogin=Numbers.parseInt(AjaxHellper.getHttpParam(request(), "islogin"),0);
  		String token=AjaxHellper.getHttpParam(request(), "token");
  		String devid = AjaxHellper.getHttpParam(request(),"devid");
  		session("cur",100+"");
  		String uuid = "";
  		try{
  			uuid = StringUtils.isBlank(request().cookie("token").value())?"":request().cookie("token").value().toString();
  			logger.info("uuid==============="+uuid);
  		}catch(Exception ex){
  			//logger.info(ex.toString());
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
	  			islogin=0;//未登录
	  		}
  		}
  		
  		if(uid.longValue()>0)
  			leftcount=H5Shoppingservice.getcjCount(uid);
  		else
  			leftcount=2;
  		
		//判断是否登录 ，登录抽奖，未登录进登录页面
		return ok(views.html.H5.act1.dazhuanpan.render(uid.longValue()+"",String.valueOf(2-leftcount),islogin));
	}
	
	/*
	 * 计算转动角度
	 */
	public static Integer getCur(Integer money,Integer nextmoney){
		Integer ncur=0;
		money=100;

				switch(nextmoney){
					case 100:
						return 0;
					case 0:
						return 315;
					case 80:
						return 270;
					case 5:
						return 225;
					case 20:
						return 180;
					case 10:
						return 135;
					case 50:
						return 90;
					case 2:
						return 45;
				}
		return ncur;
	}
	/*
	 * 获取随机数抽奖
	 */
	public static Result getRandom(){
		Double rate0=0.05D;
		Double rate1=0.20D;
		Double rate2=0.15D;
		Double rate3=0.20D;
		Double rate4=0.20D;
		Double rate5=0.10D;
		Double rate6=0.05D;
		Double rate7=0.05D;
		double randomNumber;  
		String str="";
		Long uid=Numbers.parseLong(AjaxHellper.getHttpParam(request(),"uid"), 0L);
		ObjectNode result=Json.newObject();
		result.put("status","0");
		Integer cur=Numbers.parseInt(session("cur"), 100);
		Integer nextmoney=100;
		  randomNumber = Math.random();  
		  if (randomNumber >= 0 && randomNumber <= rate0)  
		  {  
			  result.put("status","1");
			  result.put("title", "又一次不蛋定了");
			  result.put("cid", "0");
			  result.put("money", 0);
			  result.put("cur", getCur(cur,0));
			  result.put("img", assets.CdnAssets.urlForAPIPublic("images/H5/dazhuanpan/0.png"));
			  nextmoney=0;
		  }  
		  else if (randomNumber >= rate0 / 100 && randomNumber <= rate0 + rate1)  
		  {  
			  result.put("status","1");
			  result.put("title", "二货的人生不需要解释");
			  result.put("cid", "1325");
			  result.put("money", 2);
			  result.put("cur", getCur(cur,2));
			  result.put("img", assets.CdnAssets.urlForAPIPublic("images/H5/dazhuanpan/2.png"));
			  nextmoney=2;
		  }  
		  else if (randomNumber >= rate0 + rate1 && randomNumber <= rate0 + rate1 + rate2)  
		  {  			  
			  result.put("status","1");
			  result.put("title", "五体投地的佩服自己");
			  result.put("cid", "1326");
			  result.put("money", 5);
			  result.put("cur", getCur(cur,5));
			  result.put("img", assets.CdnAssets.urlForAPIPublic("images/H5/dazhuanpan/5.png"));
			  nextmoney=5;
		  }  
		  else if (randomNumber >= rate0 + rate1 + rate2 && randomNumber <= rate0 + rate1 + rate2 + rate3)  
		  {  
			  result.put("status","1");
			  result.put("title", "买遍九块九");
			  result.put("cid", "1327");
			  result.put("money", 10);
			  result.put("cur", getCur(cur,10));
			  result.put("img", assets.CdnAssets.urlForAPIPublic("images/H5/dazhuanpan/10.png"));
			  nextmoney=10;
		  }  
		  else if (randomNumber >= rate0 + rate1 + rate2 + rate3 && randomNumber <= rate0 + rate1 + rate2 + rate3 + rate4)  
		  {  			
			  result.put("status","1");
			  result.put("title", "就这样中了十张彩票");
			  result.put("cid", "1328");
			  result.put("money", 20);
			  result.put("cur", getCur(cur,20));
			  result.put("img", assets.CdnAssets.urlForAPIPublic("images/H5/dazhuanpan/20.png"));
			  nextmoney=20;
		  }  
		  else if (randomNumber >= rate0 + rate1 + rate2 + rate3 + rate4 && randomNumber <= rate0 + rate1 + rate2 + rate3 + rate4 + rate5)  
		  {  		
			  result.put("status","1");
			  result.put("title", "大票，拿去");
			  result.put("cid", "1329");
			  result.put("money", 50);
			  result.put("cur", getCur(cur,50));
			  result.put("img", assets.CdnAssets.urlForAPIPublic("images/H5/dazhuanpan/50.png"));
			  nextmoney=50;
		  }  
		  else if (randomNumber >= rate0 + rate1 + rate2 + rate3 + rate4+rate5 && randomNumber <= rate0 + rate1 + rate2 + rate3 + rate4 + rate5+rate6){
			  result.put("status","1");
			  result.put("title", "一不小心就发了");
			  result.put("cid", "1330");
			  result.put("money", 80);
			  result.put("cur", getCur(cur,80));
			  result.put("img", assets.CdnAssets.urlForAPIPublic("images/H5/dazhuanpan/80.png"));
			  nextmoney=80;
		  }
		  else if (randomNumber >= rate0 + rate1 + rate2 + rate3 + rate4+rate5+rate6 && randomNumber <= rate0 + rate1 + rate2 + rate3 + rate4 + rate5+rate6+rate7){
			  result.put("status","1");
			  result.put("title", "还有没有天理");
			  result.put("cid", "1331");
			  result.put("money", 100);
			  result.put("cur", getCur(cur,100));
			  result.put("img", assets.CdnAssets.urlForAPIPublic("images/H5/dazhuanpan/100.png"));
			  nextmoney=100;
		  }		  
		  
		  //抽奖处理
		  
		  boolean suc=false;
		  if(!result.get("status").equals("1")){
			  if(!result.get("cid").textValue().equals("0")){
				  suc=H5Shoppingservice.choujiang(uid, Numbers.parseInt(result.get("cid").textValue(), 0));
				  if(!suc){
					  result.put("status", "0");
					  result.put("msg", "您今天抽奖机会已用完，请明天再试!");
					  result.put("cid", "0");
					  result.put("money", 100);
					  result.put("cur", getCur(cur,100));
					  result.put("img", assets.CdnAssets.urlForAPIPublic("images/H5/dazhuanpan/no.png"));
				  }
			  }
		  }
		  session("cur",nextmoney+"");
		  return ok(result);
	}
	
	public static WxSign getcacheWxsign(){
		String adt=cache.get("wxaddress"+session("op"));
		if(!StringUtils.isBlank(adt)){
			WxSign addrSign=new WxSign();
			String[] tmp=adt.split(",");
			addrSign.setOpenid(tmp[0]);
			addrSign.setUnionid(tmp[1]);
			addrSign.setAccess_token(tmp[2]);
			addrSign.setTimstr(tmp[3]);
			addrSign.setNostr(tmp[4]);
			addrSign.setCode(tmp[5]);
			addrSign.setState(tmp[6]);
			addrSign.setAppId(Constants.WXappID);			
			return addrSign;
		}
		return  null;
	}	
	
	/*
	 * 取首页频道商品列表
	 */
	public Result getIndexProlist(){
		session("postmanuid","");
		String v=AjaxHellper.getHttpParam(request(), "v");
		if(v==null)
			v="";
		if(session("op")==null || StringUtils.isBlank(session("op"))){
			//鉴权
			String backurl=StringUtil.getDomainH5()+"/H5/order_address";
			backurl=backurl+"?flg=shoplist";
			return redirect("https://open.weixin.qq.com/connect/oauth2/authorize?appid="+Constants.WXappID+"&redirect_uri="+URLEncoder.encode(backurl)+"&response_type=code&scope=snsapi_base&state=123&connect_redirect=1#wechat_redirect");
		}
		Integer page=Numbers.parseInt(AjaxHellper.getHttpParam(request(), "page"), 0);
		Integer pagesize=20;
		Integer totalcount=0;
		List<Product> plist=null;
		Map<Integer,List<Product>> pmlist=H5Shoppingservice.getprolistByCid(34, page,pagesize);
		if(pmlist!=null && pmlist.keySet()!=null && pmlist.size()>0){
			Iterator<Integer> keys=pmlist.keySet().iterator();
			while(keys.hasNext()){
				totalcount=keys.next();
				plist=pmlist.get(totalcount);
			}
		}
		if(plist!=null &&!plist.isEmpty()){
			for(Product proinfo:plist){
				List<Product_images> imglist=productService.getProductImages(proinfo.getPid());
				if(imglist!=null && !imglist.isEmpty())
					proinfo.setListpic(imglist.get(0).getPicname());
			}
		}
		String openid=session("op");
		String union=session("un");
		User user=null;
		if(!StringUtils.isBlank(openid) && !StringUtils.isBlank(union))
			user = userService.getUserByopenid(openid, union);
		if (user == null) {
			user = new User();
			user.setUid(0L);
			user.setOpenId(StringUtils.isBlank(openid)?"":openid);
			user.setUnionid(StringUtils.isBlank(union)?"":union);
		}
		
		Double ct=Double.valueOf(totalcount.doubleValue()/Double.valueOf(20));
		
		WxSign wxsign=H5ProductController.getwxstr();
		wxsign.setSharecontent("Hi-嗨个购");
		wxsign.setSharetitle("嗨个购-与你一起买世界");
		wxsign.setShareimg(StringUtil.getDomainH5()+"/public/images/sheSaidImages/hi.gif");
		wxsign.setShareurl(StringUtil.getDomainH5()+"/H5/shoplist");
		
		return ok(views.html.H5.shoplist.render(plist,ct.intValue(),wxsign,user,v));
	}
	
	public Result getshoplistJson(){
		Integer page=Numbers.parseInt(AjaxHellper.getHttpParam(request(), "page"), 0);
		Integer pagesize=20;
		List<Product> plist=null;
		Map<Integer,List<Product>> hmp=H5Shoppingservice.getprolistByCid(34, page,pagesize);
		if(hmp!=null && hmp.keySet()!=null && hmp.size()>0){
			Iterator<Integer> keys=hmp.keySet().iterator();
			while(keys.hasNext()){
				plist=hmp.get(keys.next());
			}
		}
		ObjectNode result=Json.newObject();
		result.put("status", "1");
		StringBuilder htmlstr=new StringBuilder();
		htmlstr.append("<ul class=\"content\">");
		if(plist!=null && !plist.isEmpty()){
			for(Product p:plist){
				List<Product_images> imglist=productService.getProductImages(p.getPid());
				if(imglist!=null && !imglist.isEmpty())
					p.setListpic(imglist.get(0).getPicname());
				htmlstr.append("<li onclick=\"goH5('"+p.getPid()+"')\">");
				htmlstr.append("<div class=\"pic-img\">");
				htmlstr.append("<img src=\""+p.getListpic()+"\"/>");
				htmlstr.append("</div>");
				htmlstr.append("<div class=\"name\">");
				htmlstr.append("<div class=\"name-info\">");
				htmlstr.append("<h2>"+p.getTitle()+"</h2>");
				//htmlstr.append("<p>"+p.getSubtitle()+"</p>");
				htmlstr.append("</div>");
				htmlstr.append("<div class=\"price\">");
				if(p.getRmbprice().doubleValue()>new BigDecimal(p.getRmbprice()).setScale(1, BigDecimal.ROUND_DOWN).doubleValue())
					htmlstr.append("<span>￥<b>"+p.getRmbprice()+"</b>");
				else
					htmlstr.append("<span>￥<b>"+new BigDecimal(p.getRmbprice()).setScale(1,BigDecimal.ROUND_DOWN).intValue()+"</b>");
				if(p.getChinaprice().doubleValue()>new BigDecimal(p.getChinaprice()).intValue())
					htmlstr.append("<s>￥"+p.getChinaprice()+"</s></span>");
				else
					htmlstr.append("<s>￥"+new BigDecimal(p.getChinaprice()).intValue()+"</s></span>");
				
				htmlstr.append("<em><a href=\"javsscript:;\">"+p.getZhekou()+"折</a></em>");
				htmlstr.append("</div>");
				htmlstr.append("</div>");
				htmlstr.append("</li>");
			}
			htmlstr.append("</ul>");
			result.put("htmlstr", htmlstr.toString());
		}else{
			result.put("htmlstr", "");
			result.put("status", "0");
		}
		return ok(result);
	}	
	
}
