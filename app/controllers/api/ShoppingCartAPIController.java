package controllers.api;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import models.Currency;
import models.OrderLoveLy;
import models.Product;
import models.Reffer;
import models.ShoppingCart;

import org.apache.commons.lang3.StringUtils;

import play.Configuration;
import play.libs.Json;
import play.mvc.Result;
import services.api.ProductService;
import services.api.RefferService;
import services.api.ShoppingCartService;
import services.api.ShoppingOrderService;
import services.api.UserService;
import utils.AjaxHellper;
import utils.Constants;
import utils.Numbers;
import utils.StringUtil;
import vo.StatusOnlyVO;
import vo.shoppingCart.ShoppingCartListVO;
import vo.shoppingCart.ShoppingCartLovelyVO;
import vo.shoppingCart.ShoppingCartLovelyVO.ShoppingCartLovelyItem;

/**
 * 购物车Controller
 * @author luobotao
 *
 */
@Named
@Singleton
public class ShoppingCartAPIController extends BaseApiController {
	private static final SimpleDateFormat CHINESE_DATE_MONTH = new SimpleDateFormat("yyyyMM");
	private final ShoppingCartService shoppingCartService;
	private final ShoppingOrderService shoppingOrderService;
	private final UserService userService;
	private final ProductService productService;
	private final RefferService refferService;
	@Inject
	public ShoppingCartAPIController(final ShoppingCartService shoppingCartService,final UserService userService,final ProductService productService,final ShoppingOrderService shoppingOrderService,final RefferService refferService){
		this.shoppingCartService = shoppingCartService;
		this.userService = userService;
		this.productService = productService;
		this.shoppingOrderService = shoppingOrderService;
		this.refferService=refferService;
	}
	
	//shoppingCart_lovely.php
	public Result shoppingCart_lovely(){
		String uid = AjaxHellper.getHttpParam(request(), "uid");
		String orderCode = AjaxHellper.getHttpParam(request(), "orderCode")==null?"":AjaxHellper.getHttpParam(request(), "orderCode");
		String datastr = AjaxHellper.getHttpParam(request(), "datastr")==null?"0":AjaxHellper.getHttpParam(request(), "datastr");
		String appversion = AjaxHellper.getHttpParam(request(), "appversion")==null?"":AjaxHellper.getHttpParam(request(), "appversion");
		String devid = AjaxHellper.getHttpParam(request(), "devid");
		String wdhjy = AjaxHellper.getHttpParam(request(),"wdhjy");
		ShoppingCartLovelyVO result = new ShoppingCartLovelyVO();
		if (!StringUtil.checkMd5(devid, wdhjy,appversion))
		{
			result.status="0";
			return ok(Json.toJson(result));
		}
		
		int appversionInt = Numbers.parseInt(appversion.replaceAll("\\.", ""), 0);
		
		
		List<ShoppingCartLovelyItem> itemList= new ArrayList<ShoppingCartLovelyVO.ShoppingCartLovelyItem>();
		Map<Integer, List<Product>> productMap = new HashMap<Integer, List<Product>>();
		BigDecimal p_foreignfee = new BigDecimal(0);
		BigDecimal p_goods_fee = new BigDecimal(0);
		BigDecimal p_total_fee = new BigDecimal(0);
		
		if(StringUtils.isBlank(uid)||userService.getUserByUid(Numbers.parseLong(uid, 0L))==null){
			result.status="0";
			return ok(Json.toJson(result));
		}
		if(StringUtils.isBlank(orderCode)){
			String dataArray[] = datastr.split(",");
			for(String dataTemp:dataArray){
				String dataNeed[] = dataTemp.split("_");
				String pid=dataNeed[0];
				String count=dataNeed[1];
				
				Product product =productService.getProductById(Numbers.parseLong(pid, 0L));
				
				
				ShoppingCartLovelyItem item = new ShoppingCartLovelyItem();
				item.pid = product.getPid()+"";
				item.title = product.getTitle();
				item.count = count;
				item.linkurl ="pDe://pid="+product.getPid(); 
				
				Currency currency = productService.queryCurrencyById(product.getCurrency());
			    BigDecimal rate = new BigDecimal(currency.getRate()/100).setScale(4,  BigDecimal.ROUND_CEILING) ;
			    BigDecimal price = new BigDecimal(product.getPrice()/100).setScale(2,  BigDecimal.ROUND_CEILING) ;
			    BigDecimal rmb_price= rate.multiply(price).setScale(0,BigDecimal.ROUND_CEILING);
				if (product.getIslockprice()==1) {
					rmb_price = new BigDecimal(product.getRmbprice()).setScale(0, BigDecimal.ROUND_CEILING);
				} 
				
				BigDecimal weight = new BigDecimal(product.getWeight()).multiply(new BigDecimal(count));//总重
				product.setWeight(weight.doubleValue());
				List<Product> productList = productMap.get(product.getFromsite());
				if(productList==null)
					productList = new ArrayList<Product>();
				productList.add(product);
				productMap.put(product.getFromsite(), productList);
				
				BigDecimal totalFee=rmb_price.multiply(new BigDecimal(count));	
				p_goods_fee = p_goods_fee.add(totalFee);
				item.price = rmb_price.doubleValue();
				item.rmb_price="¥"+totalFee;
				item.rmbprice=totalFee+"";
				item.img = product.getListpic();
				itemList.add(item);
			}
			
			
		}else{

			List<Product> productListOrder = shoppingOrderService.getproductListByOrderCode(orderCode);
			for(Product product:productListOrder){
				ShoppingCartLovelyItem item = new ShoppingCartLovelyItem();
				int count = product.getCounts();
				item.pid = product.getPid()+"";
				item.title = product.getTitle();
				item.count = count+"";
				item.linkurl ="pDe://pid="+product.getPid(); 
				
				Currency currency = productService.queryCurrencyById(product.getCurrency());
			    BigDecimal rate = new BigDecimal(currency.getRate()/100).setScale(4,  BigDecimal.ROUND_CEILING) ;
			    BigDecimal price = new BigDecimal(product.getPrice()/100).setScale(2,  BigDecimal.ROUND_CEILING) ;
			    BigDecimal rmb_price= rate.multiply(price).setScale(0,BigDecimal.ROUND_CEILING);
				if (product.getIslockprice()==1) {
					rmb_price = new BigDecimal(product.getRmbprice()).setScale(0, BigDecimal.ROUND_CEILING);
				} 
				
				BigDecimal weight = new BigDecimal(product.getWeight()).multiply(new BigDecimal(count));//总重
				product.setWeight(weight.doubleValue());
				List<Product> productList = productMap.get(product.getFromsite());
				if(productList==null)
					productList = new ArrayList<Product>();
				productList.add(product);
				productMap.put(product.getFromsite(), productList);
				
				BigDecimal totalFee=rmb_price.multiply(new BigDecimal(count));	
				p_goods_fee = p_goods_fee.add(totalFee);
				item.price = rmb_price.doubleValue();
				item.rmb_price="¥"+totalFee;
				item.rmbprice=totalFee+"";
				item.img = product.getListpic();
				itemList.add(item);
			}
		}
		
		for(Integer fromesite:productMap.keySet()){
			BigDecimal weight = new BigDecimal(0);
			List<Product> productList = productMap.get(fromesite);
			for(Product product:productList){
				weight = weight.add(new BigDecimal(product.getWeight()));
			}
			p_foreignfee = p_foreignfee.add(new BigDecimal(ShoppingCartService.getfreight(fromesite, String.valueOf(weight))));
		}
		p_total_fee = p_total_fee.add(p_foreignfee).add(p_goods_fee);
		
		result.lovelytxt=productService.getLovelyRemark("1");
		result.goods_fee = "¥"+p_goods_fee;
		result.foreignfee = "¥"+p_foreignfee+"";
		result.totalfee = "¥"+p_total_fee+"";
		result.domestic_fee = "0";
		result.cost_fee = "0";
		result.tariff_fee = "0";
		
		
		boolean IsProduct = Configuration.root().getBoolean("production", false);
		String domain = Configuration.root().getString("domain.dev","http://ht2.neolix.cn");
		if(IsProduct){
			domain = Configuration.root().getString("domain.product","http://ht.neolix.cn");
		}
		OrderLoveLy orderLovely = shoppingCartService.getOrderLovely();
		result.lovelyRule = domain+"/lovely/introduce_pay.html";
		result.shareImg = domain+"/pimgs/site/share.jpg";
		result.lovelyimg = orderLovely.getImg();
		result.lovelyurl = domain+"/"+orderLovely.getLovelyurl();
		result.lovelydistinct = orderLovely.getLovelydistinct()+"折（含运费）";
		result.money = "¥"+p_total_fee.multiply(new BigDecimal(orderLovely.getLovelydistinct())).multiply(new BigDecimal(0.1)).setScale(0,BigDecimal.ROUND_CEILING);
		result.data = itemList;
		result.status="1";
		return ok(Json.toJson(result));
	}
	//(二十四)	购物车列表接口(GET方式) （修改）shoppingCart_list.php
	public Result shoppingCart_list(){
		String uid = AjaxHellper.getHttpParam(request(), "uid");
		String appversion = AjaxHellper.getHttpParam(request(), "appversion")==null?"":AjaxHellper.getHttpParam(request(), "appversion");
		String devid = AjaxHellper.getHttpParam(request(), "devid");
		String wdhjy = AjaxHellper.getHttpParam(request(),"wdhjy");
		String reffer=AjaxHellper.getHttpParam(request(), "ref");
		reffer=StringUtils.isBlank(reffer)?"":reffer;
		
		ShoppingCartListVO result = new ShoppingCartListVO();
		if (!StringUtil.checkMd5(devid, wdhjy,appversion))
		{
			result.setStatus("0");
			return ok(Json.toJson(result));
		}
		int appversionInt = Numbers.parseInt(appversion.replaceAll("\\.", ""), 0);
		/*
		 * 埋点
		 */
		Reffer ref=new Reffer();
		ref.setIp(request().remoteAddress());
		ref.setRefer(reffer);
		ref.setTyp(Constants.MAIDIAN_GOUWUCHE);
		ref.setTid(0L);
		refferService.addReffer(ref, "reffer"+CHINESE_DATE_MONTH.format(new Date()));

		
		List<Object> shoppingCartList = shoppingCartService.getShoppingCart_list(Numbers.parseLong(uid, 0L),appversionInt);
		result.setStatus("1");
		result.setData(shoppingCartList);
		OrderLoveLy orderLovely = shoppingCartService.getOrderLovely();
		result.setLovely(String.valueOf(orderLovely.getLovely()));
		result.setLovelydistinct(String.valueOf(orderLovely.getLovelydistinct()));
		result.setLovelyimg(orderLovely.getImg());
		result.setLovelytxt(orderLovely.getLovelytxt());
		result.setLovelyurl(orderLovely.getLovelyurl());
		return ok(Json.toJson(result));
	}
	
	//(二十五)	购物车列表增加接口(POST方式) shoppingCart_new.php
	public Result shoppingCart_new(){
		String uidStr = AjaxHellper.getHttpParamOfFormUrlEncoded(request(), "uid");
		String pid = AjaxHellper.getHttpParamOfFormUrlEncoded(request(), "pid");
		String cnt = AjaxHellper.getHttpParamOfFormUrlEncoded(request(), "cnt");
		
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
		if(StringUtils.isBlank(uidStr)||StringUtils.isBlank(pid)||StringUtils.isBlank(cnt)){
			result.put("status", "0");//
			return ok(Json.toJson(result));
		}
		/*
		 * 埋点
		 */
		Reffer ref=new Reffer();
		ref.setIp(request().remoteAddress());
		ref.setRefer(reffer);
		ref.setTyp(Constants.MAIDIAN_GOUWUCHE);
		ref.setTid(0L);
		refferService.addReffer(ref, "reffer"+CHINESE_DATE_MONTH.format(new Date()));

		int cntInt= Numbers.parseInt(cnt, 0);
		List<Object[]> deviceUsers = userService.getDeviceUser(uidStr);
		if(deviceUsers!=null && deviceUsers.size()>0){
			Product product = productService.getProductById(Numbers.parseLong(pid, 0L));
			if(product!=null){
				if("1".equals(product.getNewMantype())){//首购商品
					//判断用户是否是首次下单
					boolean flag = userService.checkFirstFlag(uidStr);
					if(flag==false){
						result.put("status", "2");
						Integer counts = shoppingCartService.gettotalNum(Numbers.parseLong(uidStr, 0L),0L)==null?0:shoppingCartService.gettotalNum(Numbers.parseLong(uidStr, 0L),0L);
						result.put("totalcount", counts+"");
						result.put("msg", "该商品仅限新人购买");
						return ok(Json.toJson(result));
					}else{
						boolean checkFlag = shoppingCartService.checkShoppingCart_newMan(Numbers.parseLong(uidStr, 0L),Numbers.parseLong(pid, 0L));//购物车是否已存在新人商品
						if(checkFlag){
							result.put("status", "2");
							Integer counts = shoppingCartService.gettotalNum(Numbers.parseLong(uidStr, 0L),0L);
							result.put("totalcount", counts+"");
							result.put("msg", "新人商品仅限购买一件");
							return ok(Json.toJson(result));
						}
						//判断当前的
						Integer pcounts = shoppingCartService.gettotalNum(Numbers.parseLong(uidStr, 0L),Numbers.parseLong(pid, 0L));
						if(pcounts!=null && pcounts>0){
							Integer counts = shoppingCartService.gettotalNum(Numbers.parseLong(uidStr, 0L),0L);
							result.put("totalcount", counts+"");
							result.put("msg", "新人商品仅限购买一件");
							return ok(Json.toJson(result));
						}
					}
				}
				if("3".equals(product.getNewMantype())){//0元商品
					//判断用户是否是首次购买0元商品
					boolean flag = userService.checkBuyOrNotFlag(uidStr,pid);
					if(flag==false){
						result.put("status", "2");
						Integer counts = shoppingCartService.gettotalNum(Numbers.parseLong(uidStr, 0L),0L)==null?0:shoppingCartService.gettotalNum(Numbers.parseLong(uidStr, 0L),0L);
						result.put("totalcount", counts+"");
						result.put("msg", "该商品只能购买一件");
						return ok(Json.toJson(result));
					}else{
						boolean checkFlag = shoppingCartService.checkShoppingCart_newManZero(Numbers.parseLong(uidStr, 0L),Numbers.parseLong(pid, 0L));//购物车是否已存在0元商品
						if(checkFlag){
							result.put("status", "2");
							Integer counts = shoppingCartService.gettotalNum(Numbers.parseLong(uidStr, 0L),0L)==null?0:shoppingCartService.gettotalNum(Numbers.parseLong(uidStr, 0L),0L);
							result.put("totalcount", counts+"");
							result.put("msg", "该商品只能购买一件");
							return ok(Json.toJson(result));
						}
					}
				}
				
				int limit = product.getLimitcount();
				Long nstock = product.getNstock();
				if(product.getIshot()==1){
					nstock = productService.dealNstockWithProduct(product.getPid());
				}
				Long uid = Numbers.parseLong(uidStr, 0L);
				Integer totalcount = shoppingCartService.gettotalNum(uid,Numbers.parseLong(pid, 0L));
				if(totalcount==null){
					totalcount = 0;
				}
				Integer counts = shoppingCartService.gettotalNum(uid,0L);
				if(counts==null){
					counts=0;
				}
				if (nstock<=0){
					result.put("status", "4");
					result.put("totalcount", counts+"");
					result.put("msg", "该商品已售罄");
					return ok(Json.toJson(result));
				}
				if (totalcount>=nstock){
					result.put("status", "3");
					result.put("totalcount", counts+"");
					result.put("msg", "您已超出库存数量");
					return ok(Json.toJson(result));
				}
				if (totalcount>=limit)
				{
					result.put("status", "2");
					result.put("totalcount", counts+"");
					result.put("msg", "您已超出限购数量");
					return ok(Json.toJson(result));
				}
				if(limit==0)
				{
					result.put("status", "2");
					result.put("totalcount", "0");
					result.put("msg", "您已超出限购数量");
					return ok(Json.toJson(result));
				}
				if(cntInt>=limit){
					cntInt = limit;
				}
				if(totalcount>0){
					ShoppingCart shoppingCart = shoppingCartService.getShoppingCartByUIdAndPId(uid,Numbers.parseLong(pid, 0L));
					shoppingCart.setCounts(shoppingCart.getCounts()+cntInt);
					shoppingCartService.saveShoppingCart(shoppingCart);
				}else{
					ShoppingCart shoppingCart = new ShoppingCart();
					shoppingCart.setCounts(cntInt);
					shoppingCart.setuId(uid);
					shoppingCart.setpId(Numbers.parseLong(pid, 0L));
					shoppingCart.setDate_add(new Date());
					shoppingCartService.saveShoppingCart(shoppingCart);
				}
				result.put("status", "1");
				result.put("totalcount", shoppingCartService.gettotalNum(uid,0L)+"");
				result.put("msg", "");
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
	
	//(二十六)	购物车列表删除接口(GET方式) shoppingCart_del.php
	public Result shoppingCart_del(){
		String uid = AjaxHellper.getHttpParam(request(), "uid");
		String pids = AjaxHellper.getHttpParam(request(), "pids");
		String appversion = AjaxHellper.getHttpParam(request(), "appversion")==null?"":AjaxHellper.getHttpParam(request(), "appversion");
		String devid = AjaxHellper.getHttpParam(request(), "devid");
		String wdhjy = AjaxHellper.getHttpParam(request(),"wdhjy");
		StatusOnlyVO result = new StatusOnlyVO();
		if (!StringUtil.checkMd5(devid, wdhjy,appversion))
		{
			result.setStatus(0);//用户不存在
			return ok(Json.toJson(result));
		}
		
		
		if(StringUtils.isBlank(uid)||StringUtils.isBlank(pids)){
			result.setStatus(0);//用户不存在
			return ok(Json.toJson(result));
		}
		List<Object[]> deviceUsers = userService.getDeviceUser(uid);
		if(deviceUsers!=null && deviceUsers.size()>0){
			String[] ids = pids.split(",");
	        List<Long> longs = new ArrayList<>(ids.length);
	        for (int i = 0, size = ids.length; i < size; i++) {
	            longs.add(Numbers.parseLong(ids[i], 0L));
	        }
	        shoppingCartService.deleteShoppingCartByPIds(Numbers.parseLong(uid, 0L),longs);
	        result.setStatus(1);
			return ok(Json.toJson(result));
		}else{
			result.setStatus(4);//用户不存在
			return ok(Json.toJson(result));
		}
	}
	
	
	//购物车数量修改接口(GET方式) shoppingCart_edit.php
	public Result shoppingCart_edit(){
		String uid = AjaxHellper.getHttpParam(request(), "uid");
		String datastr = AjaxHellper.getHttpParam(request(), "datastr");
		String appversion = AjaxHellper.getHttpParam(request(), "appversion")==null?"":AjaxHellper.getHttpParam(request(), "appversion");
		String devid = AjaxHellper.getHttpParam(request(), "devid");
		String wdhjy = AjaxHellper.getHttpParam(request(),"wdhjy");
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
				ShoppingCart shoppingCart = shoppingCartService.getShoppingCartByUIdAndPId(Numbers.parseLong(uid, 0L), Numbers.parseLong(pid, 0L));
				if(shoppingCart!=null){
					shoppingCart.setCounts(Numbers.parseInt(cnt, 0));
					shoppingCartService.saveShoppingCart(shoppingCart);
				}
			}
	        result.setStatus(1);
			return ok(Json.toJson(result));
		}else{
			result.setStatus(4);//用户不存在
			return ok(Json.toJson(result));
		}
	}
	
	//获取购物车数量接口(GET方式) shoppingCart_count.php
	public Result shoppingCart_count(){
		String uid = AjaxHellper.getHttpParam(request(), "uid");
		String appversion = AjaxHellper.getHttpParam(request(), "appversion")==null?"":AjaxHellper.getHttpParam(request(), "appversion");
		String devid = AjaxHellper.getHttpParam(request(), "devid");
		String wdhjy = AjaxHellper.getHttpParam(request(),"wdhjy");
		Map<String, String> result = new HashMap<String, String>();
		if (!StringUtil.checkMd5(devid, wdhjy,appversion))
		{
			result.put("status", "0");//
			return ok(Json.toJson(result));
		}
	
		if(StringUtils.isBlank(uid)){
			result.put("status", "4");//
			return ok(Json.toJson(result));
		}
		List<Object[]> deviceUsers = userService.getDeviceUser(uid);
		if(deviceUsers!=null && deviceUsers.size()>0){
			Integer totalcount = shoppingCartService.gettotalNum(Numbers.parseLong(uid, 0L),0L)==null?0:shoppingCartService.gettotalNum(Numbers.parseLong(uid, 0L),0L);
			result.put("status", "1");//
			result.put("num", totalcount+"");
			return ok(Json.toJson(result));
		}else{
			result.put("status", "4");//
			return ok(Json.toJson(result));
		}
	}
	
}
