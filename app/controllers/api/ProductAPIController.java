package controllers.api;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import models.Category;
import models.Coupon;
import models.Currency;
import models.Endorsement;
import models.EndorsementImg;
import models.HotSearchKey;
import models.InviteShareChannel;
import models.Product;
import models.ProductGroup;
import models.ProductPriceExt;
import models.Reffer;

import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.Page;

import play.Configuration;
import play.Logger;
import play.libs.Json;
import play.mvc.Result;
import services.api.CouponService;
import services.api.EndorsementService;
import services.api.ProductService;
import services.api.RefferService;
import services.api.ShoppingCartService;
import services.api.UserService;
import utils.AjaxHellper;
import utils.Constants;
import utils.Numbers;
import utils.StringUtil;
import vo.StatusOnlyVO;
import vo.product.ProductDetailVO;
import vo.product.ProductDetailVO.ProductDetailItem;
import vo.product.ProductEndorsementVO;
import vo.product.ProductLovelyVO;
import vo.product.ProductLovelyVO.ProductLovelyItem;
import vo.product.ProductQueryVO;
import vo.product.ProductRecomVO;
import vo.product.ProductVO;
import vo.product.ProductVO.ProductItem;
import vo.product.ProductsCheckVO;
import vo.product.ProductsCheckVO.ProdcutsCheckItem;
import vo.user.ProductSearchMouldVO;
import vo.user.ProductSearchMouldVO.DataInfo;
import assets.CdnAssets;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * 商品Controller
 * @author luobotao
 *
 */
@Named
@Singleton
public class ProductAPIController extends BaseApiController {
	private static final Logger.ALogger logger = Logger.of(ProductAPIController.class);
	private static final SimpleDateFormat CHINESE_DATE_TIME_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	private static final SimpleDateFormat CHINESE_DATE_MONTH = new SimpleDateFormat("yyyyMM");
	private final ProductService productService;
	private final ShoppingCartService shoppingCartService;
	private final UserService userService;
	private final EndorsementService endorseService;
	private final CouponService couponService;
	private final RefferService refferService;
	@Inject
	public ProductAPIController(final ProductService productService,final UserService userService,final ShoppingCartService shoppingCartService,final EndorsementService endorseService,final CouponService couponService,final RefferService refferService){
		this.productService = productService;
		this.userService = userService;
		this.shoppingCartService =shoppingCartService;
		this.endorseService=endorseService;
		this.couponService=couponService;
		this.refferService=refferService;
	}
	
	public Result productWish(){
		String uid = AjaxHellper.getHttpParam(request(), "uid");
		String pid = AjaxHellper.getHttpParam(request(), "pid");
		String devid = AjaxHellper.getHttpParam(request(), "devid")==null?"":AjaxHellper.getHttpParam(request(), "devid");
		String appversion = AjaxHellper.getHttpParam(request(), "appversion")==null?"":AjaxHellper.getHttpParam(request(), "appversion");
		String wdhjy = AjaxHellper.getHttpParam(request(),"wdhjy");
		StatusOnlyVO result = new StatusOnlyVO();
		if (!StringUtil.checkMd5(devid, wdhjy,appversion))
		{
			result.setStatus(0);
			return ok(Json.toJson(result));
		}
		if(productService.productWish(uid, pid)){
			result.setStatus(1);
		}else{
			result.setStatus(2);
		}
		return ok(Json.toJson(result));
	}
	
	//(六)	获取商品明细参数接口(GET方式) （修改）product.php
	public Result productDetail(){
		String uid = AjaxHellper.getHttpParam(request(), "uid");
		String pid = AjaxHellper.getHttpParam(request(), "pid");
		String devid = AjaxHellper.getHttpParam(request(), "devid")==null?"":AjaxHellper.getHttpParam(request(), "devid");
		String deviceType = AjaxHellper.getHttpParam(request(), "deviceType")==null?"0":AjaxHellper.getHttpParam(request(), "deviceType");
		
		String appversion = AjaxHellper.getHttpParam(request(), "appversion")==null?"":AjaxHellper.getHttpParam(request(), "appversion");
		String wdhjy = AjaxHellper.getHttpParam(request(),"wdhjy");
		String reffer=AjaxHellper.getHttpParam(request(), "ref");
		reffer=StringUtils.isBlank(reffer)?"":reffer;
		
		
		ProductVO productVO = new ProductVO();
		if (!StringUtil.checkMd5(devid, wdhjy,appversion))
		{
			productVO.setStatus("0");
			return ok(Json.toJson(productVO));
		}
		
		if(StringUtils.isBlank(uid)||userService.getUserByUid(Numbers.parseLong(uid, 0L))==null){
			productVO.setStatus("3");
			return ok(Json.toJson(productVO));
		}
		int totalCount = 0;
		List<ProductItem> itemList = new ArrayList<ProductItem>();
		if(StringUtils.isBlank(pid)){
			productVO.setStatus("2");
			return ok(Json.toJson(productVO));
		}else{
			Product product = productService.getProductById(Numbers.parseLong(pid, 0L));
			if(product == null ){
				productVO.setStatus("2");
				return ok(Json.toJson(productVO));
			}
			//加埋点
			Reffer ref=new Reffer();
			ref.setIp(request().remoteAddress());
			ref.setRefer(reffer);
			ref.setTyp(Constants.MAIDIAN_SHANGPINXIANGQING);
			ref.setTid(Numbers.parseLong(pid, 0L));
			refferService.addReffer(ref, "reffer"+CHINESE_DATE_MONTH.format(new Date()));

			List<Product> productList = productService.queryProductListByPpId(product.getPpid());
			for(Product productTemp:productList){
				if(productTemp.getIshot()==1){
					totalCount = (int) productService.dealNstockWithProduct(productTemp.getPid());
				}else{
					if(productTemp.getNstock()==null){
						totalCount +=99999;
					}else{
						totalCount +=productTemp.getNstock();
					}
					if (totalCount<=0){
						totalCount=0;
					}
				}
				
				ProductItem productItem = productService.covertToProductItem(productTemp,productService,uid);
				productItem.reffer=reffer;
				List<Integer> likeList = userService.getLikes(Numbers.parseLong(uid, 0L));
				productItem.PromisePic = userService.getPromisePic(Numbers.parseLong(uid, 0L),Numbers.parseLong(pid, 0L),devid,deviceType);
			    
				if(likeList.contains(Numbers.parseInt(pid, 0))){
			    	productItem.is_like="1";     
			    }else{
			    	productItem.is_like="0";     
			    }
				itemList.add(productItem);
			}
		}
		productVO.setStatus("1");
		productVO.setTotalcount(String.valueOf(totalCount));
		productVO.setData(itemList);
		return ok(Json.toJson(productVO));
	}
	
	//(七)	预售商品详情接口（新增）product_presell.php
	public Result product_presell(){
		String uid = AjaxHellper.getHttpParam(request(), "uid");
		String pid = AjaxHellper.getHttpParam(request(), "pid");
		String devid = AjaxHellper.getHttpParam(request(), "devid")==null?"":AjaxHellper.getHttpParam(request(), "devid");
		String deviceType = AjaxHellper.getHttpParam(request(), "deviceType")==null?"0":AjaxHellper.getHttpParam(request(), "deviceType");
		String wdhjy = AjaxHellper.getHttpParam(request(),"wdhjy");
		String appversion = AjaxHellper.getHttpParam(request(), "appversion")==null?"":AjaxHellper.getHttpParam(request(), "appversion");
		String reffer=AjaxHellper.getHttpParam(request(), "ref");
		reffer=StringUtils.isBlank(reffer)?"":reffer;
		
		ProductDetailVO productDetailVO = new ProductDetailVO();
		if (!StringUtil.checkMd5(devid, wdhjy,appversion))
		{
			productDetailVO.setStatus("0");
			return ok(Json.toJson(productDetailVO));
		}
		
		if(StringUtils.isBlank(uid)||userService.getUserByUid(Numbers.parseLong(uid, 0L))==null){
			productDetailVO.setStatus("3");
			return ok(Json.toJson(productDetailVO));
		}
		List<ProductDetailItem> itemList = new ArrayList<ProductDetailVO.ProductDetailItem>();
		if(StringUtils.isBlank(pid)){
			productDetailVO.setStatus("2");
			return ok(Json.toJson(productDetailVO));
		}else{
			Product product = productService.getProductById(Numbers.parseLong(pid, 0L));
			if(product == null ){
				productDetailVO.setStatus("2");
				return ok(Json.toJson(productDetailVO));
			}
			//加埋点
			Reffer ref=new Reffer();
			ref.setIp(request().remoteAddress());
			ref.setRefer(reffer);
			ref.setTyp(Constants.MAIDIAN_SHANGPINXIANGQING);
			ref.setTid(Numbers.parseLong(pid, 0L));
			refferService.addReffer(ref, "reffer"+CHINESE_DATE_MONTH.format(new Date()));

			List<Product> productList = productService.queryProductListByPpId(product.getPpid());
			for(Product productTemp:productList){
				ProductDetailItem productDetailItem = productService.covertToProductDetailItem(productTemp,productService,uid);
				productDetailItem.reffer=reffer;
				List<Integer> likeList = userService.getLikes(Numbers.parseLong(uid, 0L));
				productDetailItem.PromisePic = userService.getPromisePic(Numbers.parseLong(uid, 0L),Numbers.parseLong(pid, 0L),devid,deviceType);
			    if(likeList.contains(Numbers.parseInt(pid, 0))){
			    	productDetailItem.is_like="1";     
			    }else{
			    	productDetailItem.is_like="0";     
			    }
				itemList.add(productDetailItem);
			}
		}
		productDetailVO.setStatus("1");
		productDetailVO.setData(itemList);
		return ok(Json.toJson(productDetailVO));
	}
	
	// 搜索商品列表接口(GET方式) （修改） search_mouldlist.php
	public Result search_mouldlist() {
		response().setContentType("application/json;charset=utf-8");
		String uid = AjaxHellper.getHttpParam(request(), "uid");
		String keyword = AjaxHellper.getHttpParam(request(), "k");
		String page = AjaxHellper.getHttpParam(request(), "page")==null?"0":AjaxHellper.getHttpParam(request(), "page");
		String devid = AjaxHellper.getHttpParam(request(), "devid")==null?"":AjaxHellper.getHttpParam(request(), "devid");
		String version=AjaxHellper.getHttpParam(request(), "appversion");
		String wdhjy = AjaxHellper.getHttpParam(request(),"wdhjy");
		String reffer=AjaxHellper.getHttpParam(request(), "ref");
		reffer=StringUtils.isBlank(reffer)?"":reffer;
		
		ObjectNode rrst=Json.newObject();
		rrst.put("status", "1");
		rrst.put("msg", "");
		if (!StringUtil.checkMd5(devid, wdhjy,version))
		{
			rrst.put("status", "0");
			rrst.put("msg", "校验失败");
			return ok(Json.toJson(rrst));
		}
		
		if(version==null)
			version="2.1.0";
		
		ProductSearchMouldVO result =new ProductSearchMouldVO();
		if(StringUtils.isBlank(uid)||StringUtils.isBlank(keyword)){
			result.status="0";
			result.endflag=1;
			return ok(Json.toJson(result));
		}
		/*
		 * 埋点
		 */
		Reffer ref=new Reffer();
		ref.setIp(request().remoteAddress());
		ref.setRefer(reffer);
		ref.setTyp(Constants.MAIDIAN_SOUSUO);
		ref.setTid(0L);
		refferService.addReffer(ref, "reffer"+CHINESE_DATE_MONTH.format(new Date()));

		userService.addUserLog(uid,keyword);
		if(version.compareTo("2.1.0")>=0){
			//查询匹配热词对应的关键字，如果存在系统推广关键字优先
			HotSearchKey hk=productService.getkeyByWord(keyword);
			if(hk!=null)
				keyword=hk.getHotWordKey();
		}
		
		result.endflag=1;
		result.reffer="Typ="+Constants.MAIDIAN_SOUSUO+"&key="+keyword;
		
		rrst.put("endflag", "1");
		
		List<Object[]> deviceUsers = userService.getDeviceUser(uid);
		if(deviceUsers!=null && deviceUsers.size()>0){
			String resolution = UserService.getResolution(uid,devid);
			ProductQueryVO productQueryVO =new ProductQueryVO();
			productQueryVO.title = keyword;
			productQueryVO.status = "10";
			productQueryVO.newMantype = "0";
			Page<Product> productPage = productService.queryProductsPage(productQueryVO,Numbers.parseInt(page, 0),Constants.PAGESIZE);
			Long totalnum = productPage.getTotalElements();
			if(totalnum>(Numbers.parseInt(page, 0)+1)*Constants.PAGESIZE){
				result.endflag=0;
				rrst.put("endflag", "0");
			}
			
			List<ProductSearchMouldVO.DataInfo> dataList = new ArrayList<ProductSearchMouldVO.DataInfo>();
			String domain = StringUtil.getPICDomain();
			result.status="1";
			if(productPage!=null && productPage.getContent().size()>0){
				for(int i=0;i<productPage.getContent().size();i++){
					ProductSearchMouldVO.DataInfo dataInfo = new ProductSearchMouldVO.DataInfo();
					dataInfo.cardId =(i+1)+"";
					dataInfo.mould ="2";
					dataInfo.struct ="2*1";
					dataInfo.stxt ="";
					dataInfo.sdate ="";
					List<ProductSearchMouldVO.LayoutInfo> layout = new ArrayList<ProductSearchMouldVO.LayoutInfo>();
					ProductSearchMouldVO.LayoutInfo layoutInfo1 = new ProductSearchMouldVO.LayoutInfo();
					layoutInfo1.start="0_0";
					layoutInfo1.end="1_1";
					layout.add(layoutInfo1);
					ProductSearchMouldVO.LayoutInfo layoutInfo2 = new ProductSearchMouldVO.LayoutInfo();
					layoutInfo2.start="0_1";
					layoutInfo2.end="1_2";
					layout.add(layoutInfo2);
					dataInfo.layout = layout;
					List<ProductSearchMouldVO.PInfo> productlist = new ArrayList<ProductSearchMouldVO.PInfo>();
					
					ProductSearchMouldVO.PInfo pInfoTemp1 = new ProductSearchMouldVO.PInfo();
					Product product1 = productPage.getContent().get(i);
					if(product1!=null){
						pInfoTemp1.pinfo = productService.covertToProductSearchMouldItem(product1,productService,uid);
						pInfoTemp1.pinfo.cardMask = StringUtil.getProductIcon(product1.getPid().intValue(), "2");
						if ("3".equals(product1.getPtyp()))
						{
							pInfoTemp1.linkurl = "presellDetail://pid="+product1.getPid();
						}else{
							pInfoTemp1.linkurl = "pDe://pid="+product1.getPid();
						}
						String imgurl= productService.getmaxImgUrl(product1.getPid());
						pInfoTemp1.img=domain+StringUtil.getWebListpic(product1.getSkucode(),imgurl,resolution,new BigDecimal(2));
						productlist.add(pInfoTemp1);
					}
						
					if(i+1<productPage.getContent().size()){
						ProductSearchMouldVO.PInfo pInfoTemp2 = new ProductSearchMouldVO.PInfo();
						Product product2 = productPage.getContent().get(i+1);
						if(product2==null){
							continue;
						}
						pInfoTemp2.pinfo = productService.covertToProductSearchMouldItem(product2,productService,uid);
						pInfoTemp2.pinfo.cardMask = StringUtil.getProductIcon(product2.getPid().intValue(), "2");
						if ("3".equals(product2.getPtyp()))
						{
							pInfoTemp2.linkurl = "presellDetail://pid="+product2.getPid();
						}else{
							pInfoTemp2.linkurl = "pDe://pid="+product2.getPid();
						}
						
						String imgurl= productService.getmaxImgUrl(product2.getPid());
						
						pInfoTemp2.img=domain + StringUtil.getWebListpic(product2.getSkucode(),imgurl,resolution,new BigDecimal(2));
						productlist.add(pInfoTemp2);
					}
					
					dataInfo.plist = productlist;
					i++;
					dataList.add(dataInfo);
				}
				result.status="1";
				result.data = dataList;
				return ok(Json.toJson(result));
			}
			
			
			//如果没搜索到结果搜索相关连产品			
			if((productPage==null || productPage.getTotalElements()<1L) && version.compareTo("2.1.0")>=0){				
				List<JsonNode> dataolist=new ArrayList<JsonNode>();
				List<Product> plist=productService.searchProductlist(Numbers.parseInt(page, 0), Constants.PAGESIZE, keyword);
				
				if(plist==null || plist.isEmpty()){
					List<DataInfo> datalist=new ArrayList<DataInfo>();
					result.status="1";
					result.data=datalist;
					return ok(Json.toJson(result));
				}
				
				Integer pcount=productService.searchProductCountByKey(keyword);
				if(pcount>(Numbers.parseInt(page, 0)+1)*Constants.PAGESIZE)
					rrst.put("endflag", "0");
				
				//如果第一页有关键字商品，归避第二页搜索错误
				//查看分类下有没有这个名称，有不返回该模块
				List<Category> catelist=productService.getCagtegoryList(keyword);
				if(Numbers.parseInt(page, 0)==0 && (catelist==null || catelist.isEmpty())){
					ObjectNode re=Json.newObject();
					re.put("mould", "5");
					re.put("title", "抱歉,没有找到相关商品");
					re.put("text", "为您推荐与“"+keyword+"”相关的商品");
					re.put("link", "xxxxxx");
					dataolist.add(re);
				}
				
				for(int n=0;n<plist.size();n++){
					ProductSearchMouldVO.DataInfo dataInfo = new ProductSearchMouldVO.DataInfo();
					dataInfo.cardId =(n+1)+"";
					dataInfo.mould ="2";
					dataInfo.struct ="2*1";
					dataInfo.stxt ="";
					dataInfo.sdate ="";
					List<ProductSearchMouldVO.LayoutInfo> layout = new ArrayList<ProductSearchMouldVO.LayoutInfo>();
					ProductSearchMouldVO.LayoutInfo layoutInfo1 = new ProductSearchMouldVO.LayoutInfo();
					layoutInfo1.start="0_0";
					layoutInfo1.end="1_1";
					layout.add(layoutInfo1);
					ProductSearchMouldVO.LayoutInfo layoutInfo2 = new ProductSearchMouldVO.LayoutInfo();
					layoutInfo2.start="0_1";
					layoutInfo2.end="1_2";
					layout.add(layoutInfo2);
					dataInfo.layout = layout;
					List<ProductSearchMouldVO.PInfo> productlist = new ArrayList<ProductSearchMouldVO.PInfo>();
					
					
					ProductSearchMouldVO.PInfo pInfoTemp1 = new ProductSearchMouldVO.PInfo();
					Product product1 = plist.get(n);
					if(product1!=null){
						pInfoTemp1.pinfo = productService.covertToProductSearchMouldItem(product1,productService,uid);
						
						if ("3".equals(product1.getPtyp()))
						{
							pInfoTemp1.linkurl = "presellDetail://pid="+product1.getPid();
						}else{
							pInfoTemp1.linkurl = "pDe://pid="+product1.getPid();
						}
						pInfoTemp1.pinfo.cardMask = StringUtil.getProductIcon(product1.getPid().intValue(), "2");
						String imgurl= productService.getmaxImgUrl(product1.getPid());
						pInfoTemp1.img=domain+StringUtil.getWebListpic(product1.getSkucode(),imgurl,resolution,new BigDecimal(2));
						productlist.add(pInfoTemp1);
					}
						
					if(n+1<plist.size()){
						ProductSearchMouldVO.PInfo pInfoTemp2 = new ProductSearchMouldVO.PInfo();
						Product product2 = plist.get(n+1);
						if(product2==null){
							continue;
						}
						pInfoTemp2.pinfo = productService.covertToProductSearchMouldItem(product2,productService,uid);
						pInfoTemp2.pinfo.cardMask = StringUtil.getProductIcon(product2.getPid().intValue(), "2");
						if ("3".equals(product2.getPtyp()))
						{
							pInfoTemp2.linkurl = "presellDetail://pid="+product2.getPid();
						}else{
							pInfoTemp2.linkurl = "pDe://pid="+product2.getPid();
						}
						String imgurl= productService.getmaxImgUrl(product2.getPid());
						
						pInfoTemp2.img=domain + StringUtil.getWebListpic(product2.getSkucode(),imgurl,resolution,new BigDecimal(2));
						productlist.add(pInfoTemp2);
					}
					
					dataInfo.plist = productlist;
					n++;
					JsonNode jsn=Json.toJson(dataInfo);
					dataolist.add(jsn);
				}
				
				
				if(dataolist==null){
					rrst.put("endflag", "1");
				}
//				else{
//					rrst.put("endflag", "0");
//				}
				
				rrst.putPOJO("data", dataolist);
				return ok(rrst);
			}
			
		}else{
			result.status="4";
			return ok(Json.toJson(result));
		}
		//return ok();
		return ok(Json.toJson(result));
	}
	
	//推荐商品列表接口(GET方式) product_recom.php
	public Result product_recom() {
		response().setContentType("application/json;charset=utf-8");
		String uid = AjaxHellper.getHttpParam(request(), "uid");
		String pid = AjaxHellper.getHttpParam(request(), "pid");
		String devid = AjaxHellper.getHttpParam(request(), "devid")==null?"":AjaxHellper.getHttpParam(request(), "devid");
		String appversion=AjaxHellper.getHttpParam(request(), "appversion");
		String wdhjy = AjaxHellper.getHttpParam(request(),"wdhjy");
		String reffer=AjaxHellper.getHttpParam(request(), "ref");
		reffer=StringUtils.isBlank(reffer)?"":reffer;
		
		ProductRecomVO result =new ProductRecomVO();
		if (!StringUtil.checkMd5(devid, wdhjy,appversion))
		{
			result.status=0;
			return ok(Json.toJson(result));
		}
		if(StringUtils.isBlank(uid)||StringUtils.isBlank(pid)){
			result.status=0;
			return ok(Json.toJson(result));
		}
		/*List<Object[]> deviceUsers = userService.getDeviceUser(uid);
		if(deviceUsers!=null && deviceUsers.size()>0){
			String resolution = UserService.getResolution(uid);
			result.status=1;
			result.userlike = productService.getProductRecom(uid,pid,resolution);
			result.totalnum=result.userlike.size();
			return ok(Json.toJson(result));
		}else{
			result.status=4;
			return ok(Json.toJson(result));
		}*/
		//加埋点
		Reffer ref=new Reffer();
		ref.setIp(request().remoteAddress());
		ref.setRefer(reffer);
		ref.setTyp(Constants.MAIDIAN_TUIJIAN);
		ref.setTid(Numbers.parseLong(pid, 0L));
		refferService.addReffer(ref, "reffer"+CHINESE_DATE_MONTH.format(new Date()));

		String resolution = UserService.getResolution(uid,devid);
		result.status=1;
		result.reffer="Typ="+Constants.MAIDIAN_TUIJIAN;
		result.userlike = productService.getProductRecom(uid,pid,resolution,"Typ="+Constants.MAIDIAN_TUIJIAN);
		result.totalnum=result.userlike.size();
		return ok(Json.toJson(result));
	}
	
	
	//代言商品列表接口(GET方式) product_Endorsement
		public Result product_Endorsement(){
			response().setContentType("application/json;charset=utf-8");
			
			String devid = AjaxHellper.getHttpParam(request(), "devid")==null?"":AjaxHellper.getHttpParam(request(), "devid");
			String appversion=AjaxHellper.getHttpParam(request(), "appversion");
			String wdhjy = AjaxHellper.getHttpParam(request(),"wdhjy");
			String reffer=AjaxHellper.getHttpParam(request(), "ref");
			reffer=StringUtils.isBlank(reffer)?"":reffer;
			
			ProductEndorsementVO result =new ProductEndorsementVO();
			if (!StringUtil.checkMd5(devid, wdhjy,appversion))
			{
				result.setStatus("0");
				return ok(Json.toJson(result));
			}
			
			String domains = "http://ht2.neolix.cn:9004";//Configuration.root().getString("domain.dev","http://ht2.neolix.cn:9004");
			boolean IsProduct = Configuration.root().getBoolean("production", false);
			if(IsProduct){
				domains = Configuration.root().getString("domain.product","http://ht.neolix.cn");
			}
			Integer page=Numbers.parseInt(AjaxHellper.getHttpParam(request(), "page"), 0);
			
			Date dd=new Date();
			
			/*
			 * 埋点
			 */
			Reffer ref=new Reffer();
			ref.setIp(request().remoteAddress());
			ref.setRefer(reffer);
			ref.setTyp(Constants.MAIDIAN_DAIYAN);
			ref.setTid(0L);
			refferService.addReffer(ref, "reffer"+CHINESE_DATE_MONTH.format(new Date()));

			result.setStatus("1");
			result.setReffer("Typ="+Constants.MAIDIAN_DAIYAN);
			//result.setBannerImg(CdnAssets.CDN_API_PUBLIC_URL+"images/sheSaidImages/p_e.jpg?u="+ System.currentTimeMillis());
			result.setBannerImg(StringUtil.getSystemConfigValue("bannerImg"));
			result.setDaiyanURL(StringUtil.getSystemConfigValue("daiyanURL"));
			result.setData(productService.getProductEndorsement(page));
			return ok(Json.toJson(result));
		}
		
	//(四十六)	撒娇商品详细接口(GET方式) product_lovely.php
	public Result product_lovely(){
		String uid = AjaxHellper.getHttpParam(request(), "uid");
		String pid = AjaxHellper.getHttpParam(request(), "pid");
		String devid = AjaxHellper.getHttpParam(request(), "devid")==null?"":AjaxHellper.getHttpParam(request(), "devid");
		String deviceType = AjaxHellper.getHttpParam(request(), "deviceType")==null?"0":AjaxHellper.getHttpParam(request(), "deviceType");
		
		String appversion = AjaxHellper.getHttpParam(request(), "appversion")==null?"":AjaxHellper.getHttpParam(request(), "appversion");
		String wdhjy = AjaxHellper.getHttpParam(request(),"wdhjy");
		String reffer=AjaxHellper.getHttpParam(request(), "ref");
		reffer=StringUtils.isBlank(reffer)?"":reffer;
		
		ProductLovelyVO result = new ProductLovelyVO();
		if (!StringUtil.checkMd5(devid, wdhjy,appversion))
		{
			result.setStatus(0);
			return ok(Json.toJson(result));
		}
		
		int appversionInt = Numbers.parseInt(appversion.replaceAll("\\.", ""), 0);
		
		if(StringUtils.isBlank(uid)||userService.getUserByUid(Numbers.parseLong(uid, 0L))==null||StringUtils.isBlank(pid)){
			result.setStatus(0);
			return ok(Json.toJson(result));
		}
		
		Product product = productService.getProductById(Numbers.parseLong(pid, 0L));
		if(product == null ){
			result.setStatus(0);
			return ok(Json.toJson(result));
		}
		/*
		 * 埋点
		 */
		Reffer ref=new Reffer();
		ref.setIp(request().remoteAddress());
		ref.setRefer(reffer);
		ref.setTyp(Constants.MAIDIAN_SHANGPINXIANGQING);
		ref.setTid(Numbers.parseLong(pid, 0L));
		refferService.addReffer(ref, "reffer"+CHINESE_DATE_MONTH.format(new Date()));

		
	    Currency currency = productService.queryCurrencyById(product.getCurrency());
	    BigDecimal rate = new BigDecimal(currency.getRate()/100).setScale(4,  BigDecimal.ROUND_CEILING) ;
	    BigDecimal price = new BigDecimal(product.getPrice()/100).setScale(2,  BigDecimal.ROUND_CEILING) ;
	    BigDecimal rmb_price = new BigDecimal(0);
	    
	    ProductLovelyItem productLovelyItem = new ProductLovelyItem();
	    productLovelyItem.reffer=reffer;
	    productLovelyItem.pid = pid;
	    productLovelyItem.title=product.getTitle();  
	    if(product.getIslockprice()==1){
	    	rmb_price = new BigDecimal(product.getRmbprice()).setScale(1,  BigDecimal.ROUND_CEILING) ;
	    }else{
	    	rmb_price = rate.multiply(price).setScale(1,  BigDecimal.ROUND_CEILING) ;
	    }
	    Double freight=ShoppingCartService.getfreight(product.getFromsite(),String.valueOf(product.getWeight()));
	    BigDecimal tfee = rmb_price.setScale(0,  BigDecimal.ROUND_CEILING).add(new BigDecimal(freight));
	    productLovelyItem.rmb_price="¥"+tfee;
	    productLovelyItem.rmbprice=String.valueOf(rmb_price.setScale(0,  BigDecimal.ROUND_CEILING));
	    String lovelydistinct = String.valueOf(product.getLovelydistinct());
		if (".0".equals(lovelydistinct.substring(lovelydistinct.length()-2, lovelydistinct.length())))
		{
			productLovelyItem.lovelydistinct="撒娇再享"+lovelydistinct.substring(0, lovelydistinct.length()-2)+"折";
		}else{
			productLovelyItem.lovelydistinct="撒娇再享"+lovelydistinct+"折";
		}
		
		boolean IsProduct = Configuration.root().getBoolean("production", false);
		String domain = Configuration.root().getString("domain.dev","http://ht2.neolix.cn");
		if(IsProduct){
			domain = Configuration.root().getString("domain.product","http://ht.neolix.cn");
		}
		
		productLovelyItem.lovely_image=domain+"/pimgs/site/"+product.getDistinctimg();
		productLovelyItem.shareImg=domain+"/pimgs/site/share.jpg";
		productLovelyItem.lovely_shareURL=domain+"/lovely/lovely.php?orderCode=";
		productLovelyItem.lovelyRule=domain+"/lovely/introduce_pay.html";
		productLovelyItem.img=product.getListpic();
		productLovelyItem.lovely_price="¥"+tfee.multiply(new BigDecimal(product.getLovelydistinct())).divide(new BigDecimal(10)).setScale(0,  BigDecimal.ROUND_CEILING).toString();
		productLovelyItem.lovely_remark=productService.getLovelyRemark("1");
		
		productLovelyItem.linkurl = "pDe://pid="+pid+"&lmt="+CHINESE_DATE_TIME_FORMAT.format(product.getDate_upd());;
		result.setData(productLovelyItem);
		result.setStatus(1);
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
		ProductsCheckVO result = new ProductsCheckVO();
		if (!StringUtil.checkMd5(devid, wdhjy,appversion))
		{
			result.status=0;
			result.data=null;
			return ok(Json.toJson(result));
		}
		
		if(StringUtils.isBlank(uid)||StringUtils.isBlank(datastr)){
			result.status=0;
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
				BigDecimal rmb_price = rate.multiply(priceDec).setScale(0,BigDecimal.ROUND_CEILING);
				
				if (product.getIslockprice()==1){
					rmb_price =new BigDecimal(product.getRmbprice());
				}
				//新人价
				if(userService.checkFirstFlag(uid) && Numbers.parseInt(product.getNewMantype(), 0)>0){//是新人，并且此商品是新人商品
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
				
				ProdcutsCheckItem item = new ProdcutsCheckItem();
				
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
					item.errmsg="该商品已下架";
				}
				if (status==2)
				{
					item.errmsg="您所购买的部分商品已经卖光了哦~请重新结算";
				}
				if (status==3)
				{
					item.errmsg="您所购买的部分商品价格发生了改变哦~请重新结算";
				}
				if (status==4)
				{
					item.errmsg="您所购买的部分商品超出了限购数量哦~请重新结算";
				}
				if (status==5)
				{
					item.errmsg="该商品仅限新人购买";
				}
				if (status==6)
				{
					item.errmsg="新人商品只能购买一件";
				}
				if (status==7)
				{
					item.errmsg="您无法购买新人特权商品";
				}
				if (status==8)
				{
					item.errmsg="您无法购买该商品";
				}
				if (status==9)
				{
					item.errmsg="该商品只能购买一件";
				}
				if(status==101){
					item.errmsg="新人商品只能购买一件";
				}
				result.status=status;
				result.data=item;
			}
		}
		
		return ok(Json.toJson(result));
	}

	//商品详情页TA说列表
	@SuppressWarnings("deprecation")
	public Result DetailTopEndorsements(){
		response().setContentType("application/json;charset=utf-8");
		Long pid=Numbers.parseLong(AjaxHellper.getHttpParam(request(), "pid"), 0L);
		Integer page=Numbers.parseInt(AjaxHellper.getHttpParam(request(),"page"),0);
		Integer topvalue=Numbers.parseInt(AjaxHellper.getHttpParam(request(), "topvalue"), 0);
		String domains = StringUtil.getOSSUrl();
		Long uid=Numbers.parseLong(AjaxHellper.getHttpParam(request(), "uid"),0L);
		String devid = AjaxHellper.getHttpParam(request(), "devid")==null?"":AjaxHellper.getHttpParam(request(), "devid");
		String appversion=AjaxHellper.getHttpParam(request(), "appversion");
		String wdhjy = AjaxHellper.getHttpParam(request(),"wdhjy");
		String reffer=AjaxHellper.getHttpParam(request(), "ref");
		reffer=StringUtils.isBlank(reffer)?"":reffer;
		
		ObjectNode result=Json.newObject();
		if (!StringUtil.checkMd5(devid, wdhjy,appversion))
		{
			result.put("status", "0");
			result.put("msg", "校验失败");
			return ok(Json.toJson(result));
		}
		
		
		result.put("status", "1");
		result.put("msg", "没有代言数据");
		//取代言及评论数量
		Integer cnt=endorseService.getEPcount(pid,uid);
		result.put("ecount", cnt);
		result.put("endflag", "0");
		result.put("data", "");
		
		/*
		 * 埋点
		 */
		Reffer ref=new Reffer();
		ref.setIp(request().remoteAddress());
		ref.setRefer(reffer);
		ref.setTyp(Constants.MAIDIAN_DAIYAN);
		ref.setTid(pid);
		refferService.addReffer(ref, "reffer"+CHINESE_DATE_MONTH.format(new Date()));

		
		List<Endorsement> elist=null;
		if(topvalue==1)
		{
			elist = endorseService.getendorselistbyPid(uid,pid,4,page,0);
			result.put("endflag", "1");
		}	
		else if(topvalue==0)
		{
			elist = endorseService.getendorselistbyPid(uid,pid,10,page,1);
			if (elist== null || elist.size()<10){
				result.put("endflag", "1");	
			}
			//Integer pnt = endorseService.getPcount(pid, uid);
			if (cnt<=(page+1)*10){
				result.put("endflag", "1");
			}
		}
		result.put("reffer", reffer);
		
		List<ObjectNode> rrlist=new ArrayList<ObjectNode>();
		//代言列表
		if(elist!=null && !elist.isEmpty()){
			result.put("status", "1");
			result.put("msg", "");
			
			for(Endorsement e:elist){
				ObjectNode rl=Json.newObject();
				if(e.getEid().compareTo(0L)==0){
					rl.put("id", e.getUser().getPingid());
					rl.put("type", "1");
					rl.put("nickname", e.getUser().getNickname());
					rl.put("content", e.getRemark());
					rl.put("headIcon", e.getUser().getHeadIcon());
					rl.put("editor", e.getUser().getEditor());
				}
				else{					
					rl.put("eid", e.getEid().toString());
					rl.put("linkUrl", "endorsementDetail://eid="+e.getEid());
					rl.put("type","2");
					rl.put("userId", e.getUserId().toString());
					rl.put("userNickName",e.getUser().getNickname());
					rl.put("userHeadIcon",e.getUser().getHeadIcon());
					rl.put("preImgPath", domains+e.getPreImgPath());
					rl.put("userDescription", e.getRemark());
					rl.put("endorTagImgUrl", "http://h5.higegou.com/public/images/daiyan_mark.png");	
					List<EndorsementImg> imglist=endorseService.getEnImglist(e.getEid());
					List<String> ilist=new ArrayList<String>();
					if(imglist!=null && !imglist.isEmpty()){
						for(EndorsementImg i:imglist){
							ilist.add(domains+i.getImgPath());
						}
					}
					
					rl.putPOJO("imgList", ilist);
				}
				rrlist.add(rl);
			}			
		}
		//取商品评论
		result.put("data",Json.toJson(rrlist));
		return ok(result);
	}
	
	//获取搜索信息列表
	public Result preSearchInfo(){
		response().setContentType("application/json;charset=utf-8");
		String devid = AjaxHellper.getHttpParam(request(), "devid")==null?"":AjaxHellper.getHttpParam(request(), "devid");
		String appversion=AjaxHellper.getHttpParam(request(), "appversion");
		String wdhjy = AjaxHellper.getHttpParam(request(),"wdhjy");
		ObjectNode result=Json.newObject();
		if (!StringUtil.checkMd5(devid, wdhjy,appversion))
		{
			result.put("status", "0");
			result.put("msg", "校验失败");
			return ok(Json.toJson(result));
		}
		
		String domains = StringUtil.getOSSUrl();
		
		result.put("status", "1");
		result.put("msg", "");
		ObjectNode ts=Json.newObject();
		ts.put("text", "");
		ts.put("hotWordKey", "");		
		result.putPOJO("defaultSearchText", ts);
		String defaultkey="";
		String defaultcontent="";		
		result.put("hotwords", "");
		List<ObjectNode> list=new ArrayList<ObjectNode>();
		List<HotSearchKey> klist=productService.getallhotkey();
		if(klist!=null && !klist.isEmpty()){
			for(HotSearchKey k:klist){
				ObjectNode t=Json.newObject();
				t.put("hotWordKey", k.getHotWordKey());
				t.put("hotWordTitle", k.getHotWordTitle());
				t.put("hotWordDes", k.getHotWordDes());
				t.put("hotWordImageUrl", k.getHotWordImageUrl()==null||k.getHotWordImageUrl().equals("")?"":StringUtil.getDomainAPI()+"/pimgs/adload/hotkey/"+k.getHotWordImageUrl());
				if(k.getIsDefault()==1)
				{
					defaultkey=k.getHotWordKey();
					defaultcontent=k.getHotWordDes();
					ts.put("text", defaultcontent);
					ts.put("hotWordKey",defaultkey);
					result.putPOJO("defaultSearchText", ts);
				}
				list.add(t);
			}
		}
		result.putPOJO("hotwords", list);
		
		return ok(result);
	}
	
	//"我"tab是否展示邀请好友入口，目前返回展示，不展示则设宿值
	public Result usercenterShowInvitation(){
		response().setContentType("application/json;charset=utf-8");
		String devid = AjaxHellper.getHttpParam(request(), "devid")==null?"":AjaxHellper.getHttpParam(request(), "devid");
		Long uid=Numbers.parseLong(AjaxHellper.getHttpParam(request(), "uid"), 0L);
		String resolution = UserService.getResolution(uid.toString(),devid);
		String version=AjaxHellper.getHttpParam(request(), "appversion");
		String wdhjy = AjaxHellper.getHttpParam(request(),"wdhjy");
		ObjectNode result=Json.newObject();
		if (!StringUtil.checkMd5(devid, wdhjy,version))
		{
			result.put("status", "0");
			result.put("msg", "校验失败");
			return ok(Json.toJson(result));
		}
		
		if(StringUtils.isBlank(version))
			version="2.1.0";
		
		if (StringUtils.isBlank(resolution))
		{
			resolution="1080_640";
		}
		String resolutionArray[] =resolution.split("_");
		int width = Numbers.parseInt(resolutionArray[0], 0);
		switch (width){
			case 720:
				width = 720;
				break;
			case 640:
				width = 640;
				break;
			case 1080:
				width = 1080;
				break;
			default:
				width = 1080;
				break;
		}
		result.put("status", "1");
		result.put("msg", "");
		List<ObjectNode> olist=new ArrayList<ObjectNode>();
		List<InviteShareChannel> clist=userService.getsharechannellist(version);
		if(clist!=null && !clist.isEmpty()){
			for(InviteShareChannel in:clist){
				ObjectNode re=Json.newObject();
				re.put("icon", CdnAssets.CDN_API_PUBLIC_URL+in.getIcon()+"_"+width+".png");
				re.put("title", in.getTitle());
				re.put("remark", in.getRemark());
				re.put("link", in.getLinkurl());
				re.put("islogin", in.getIslogin());
				olist.add(re);
			}
		}
		
		result.putPOJO("data", olist);
		return ok(result);
	}
	//邀请好友页优惠认息接口
	public Result inviteFriends(){
		response().setContentType("application/json;charset=utf-8");
		Long uid=Numbers.parseLong(AjaxHellper.getHttpParam(request(), "uid"), 0L);
		Integer page=Numbers.parseInt(AjaxHellper.getHttpParam(request(), "page"), 0);
		String devid = AjaxHellper.getHttpParam(request(), "devid")==null?"":AjaxHellper.getHttpParam(request(), "devid");
		String resolution = UserService.getResolution(uid.toString(),devid);
		String version=AjaxHellper.getHttpParam(request(), "appversion");
		String wdhjy = AjaxHellper.getHttpParam(request(),"wdhjy");
		ObjectNode result=Json.newObject();
		if (!StringUtil.checkMd5(devid, wdhjy,version))
		{
			result.put("status", "0");
			result.put("msg", "校验失败");
			return ok(Json.toJson(result));
		}
		if(StringUtils.isBlank(version))
			version="2.1.0";
		
		if (StringUtils.isBlank(resolution))
		{
			resolution="1080_640";
		}
		String resolutionArray[] =resolution.split("_");
		int width = Numbers.parseInt(resolutionArray[0], 0);
		switch (width){
			case 720:
				width = 720;
				break;
			case 640:
				width = 640;
				break;
			case 1080:
				width = 1080;
				break;
			default:
				width = 1080;
				break;
		}
		String domain = Configuration.root().getString("domain.dev","http://ht2.neolix.cn:9000");
		boolean IsProduct = Configuration.root().getBoolean("production", false);
		if(IsProduct){
			domain = "http://h5.higegou.com";
		}
		result.put("status", "1");
		result.put("endflg", "1");
		result.put("msg", "");
		ObjectNode re=Json.newObject();
		re.put("inviteBgImgUrl",CdnAssets.CDN_API_PUBLIC_URL+"images/H5/inviteBgImg_"+width+".jpg"); 
		//re.put("couponTitle", "优惠券");
		//re.put("couponPrice", "20");
		//re.put("couponDescription", "优惠信息详细介绍");
		String[] strshare=StringUtil.getSharecontent();
		String sharecontent="";
		String sharetitle="";
		if(strshare!=null){
			sharetitle=strshare[0];
			sharecontent=strshare[1];
		}
		re.put("shareCouponTitle", sharetitle);
		re.put("shareCouponContent", sharecontent);
		re.put("shareCouponURL",StringUtil.getSystemConfigValue("H5invite")+"?fromUid="+uid);
		re.put("shareCouponImg", CdnAssets.CDN_API_PUBLIC_URL+"images/H5/shareCouponImg.png");
		List<ObjectNode> nlist=new ArrayList<ObjectNode>();
		Integer couponcount=couponService.gettotalCoupunmoney(uid);
		re.put("couponTotalPrice", couponcount+"元");
		List<Coupon> clist=couponService.getregcouplist(uid, page);
		if(clist==null || clist.isEmpty()){
			re.putPOJO("couponLoglist", new ArrayList<ObjectNode>());
			result.putPOJO("data", re);
			return ok(result);
		}
		if(clist.size()>=10){
			Integer count=couponService.getregcoupCount(uid);
			if(count>(page+1)*10)
				result.put("endflg", "0");
		}
		for(Coupon c:clist){
			ObjectNode ot=Json.newObject();
			ot.put("userName", c.getUser().getNickname());
			switch(c.getStates()){
				case "0":
					ot.put("getCouponStatus", "已领券");
					break;
				case "1":
					ot.put("getCouponStatus", "已注册");
					break;
				case "2":
					ot.put("getCouponStatus", "已购买");
					break;
				default:
					ot.put("getCouponStatus", "已注册");
			}
			//ot.put("getCouponStatus", c.getStates());
			if ("0".equals(c.getStates())){
				ot.put("couponPrice", "等待中");
			}else{
				ot.put("couponPrice", c.getCouponprice()+"元");
			}	
			nlist.add(ot);
		}
		re.putPOJO("couponLoglist", nlist);
		result.putPOJO("data", re);
		return ok(result);
	}	
	
	
	
}
