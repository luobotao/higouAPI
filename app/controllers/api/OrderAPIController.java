package controllers.api;

import java.math.BigDecimal;
import java.net.URLEncoder;
import java.security.MessageDigest;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import models.Address;
import models.Coupon;
import models.Currency;
import models.Endorsement;
import models.OrderLoveLy;
import models.OrderLoveLyBack;
import models.OrderProduct;
import models.Parcels;
import models.Product;
import models.ProductGroup;
import models.ProductPriceExt;
import models.Reffer;
import models.ShoppingOrder;
import models.User;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.springframework.data.domain.Page;

import play.Logger;
import play.data.Form;
import play.libs.Json;
import play.mvc.Result;
import services.AliPayService;
import services.ServiceFactory;
import services.WXPayService;
import services.api.AddressService;
import services.api.CertificationService;
import services.api.CouponService;
import services.api.EndorsementService;
import services.api.ProductService;
import services.api.RefferService;
import services.api.ShoppingCartService;
import services.api.ShoppingOrderService;
import services.api.UserService;
import utils.AjaxHellper;
import utils.Constants;
import utils.Numbers;
import utils.StringUtil;
import utils.wxpay.Sha1Util;
import utils.wxpay.TenpayHttpClient;
import vo.StatusMsgVO;
import vo.StatusOnlyVO;
import vo.product.ProductDetailCostpresellVO.ProductDetailCostpresellItem;
import vo.product.ProductNewVO;
import vo.shoppingCart.ShoppingCartCategoryVO;
import vo.shoppingOrder.ShoppingOrderCostPreVO;
import vo.shoppingOrder.ShoppingOrderCostPreVO.ShoppingOrderCostPreItem;
import vo.shoppingOrder.ShoppingOrderCostVO;
import vo.shoppingOrder.ShoppingOrderCostVO.ShoppingOrderCostItem;
import vo.shoppingOrder.ShoppingOrderDetailResultVO;
import vo.shoppingOrder.ShoppingOrderDetailResultVO.AddressData;
import vo.shoppingOrder.ShoppingOrderDetailResultVO.ShoppingOrderDetail;
import vo.shoppingOrder.ShoppingOrderPresellDetailResultVO;
import vo.shoppingOrder.ShoppingOrderPresellDetailResultVO.AddressPreselData;
import vo.shoppingOrder.ShoppingOrderPresellDetailResultVO.ShoppingOrderPresellDetail;
import vo.shoppingOrder.ShoppingOrderQueryVO;
import vo.shoppingOrder.ShoppingOrderResultVO;
import vo.shoppingOrder.ShoppingOrderResultVO.PackageItem;
import vo.shoppingOrder.ShoppingOrderResultVO.PackageProductItem;
import vo.shoppingOrder.ShoppingOrderResultVO.ShoppingOrderItem;
import vo.shoppingOrder.ShoppingOrderWayBillVO;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * 
 * @author luobotao
 *
 */
@Named
@Singleton
public class OrderAPIController extends BaseApiController {
	private static final SimpleDateFormat CHINESE_DATE_TIME_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	private static final Logger.ALogger logger = Logger.of(UserAPIController.class);
	private static final java.util.regex.Pattern PHONE_PATTERN = java.util.regex.Pattern.compile("^((13[0-9])|(15[^4,\\D])|(18[0,5-9]))\\d{8}$");
	private static final SimpleDateFormat CHINESE_DATE_MONTH = new SimpleDateFormat("yyyyMM");
	private final ShoppingOrderService shoppingOrderService;
	private final ProductService productService;
	private final ShoppingCartService shoppingCartService;
	private final UserService userService;
	private final CouponService couponService;
	private final AddressService addressService;
	private final EndorsementService endorsementService;
	private final RefferService refferService;
	private final CertificationService certificationService;
	@Inject
	public OrderAPIController(final ShoppingOrderService shoppingOrderService,final ProductService productService,final UserService userService,final ShoppingCartService shoppingCartService,final CouponService couponService,final AddressService addressService,final EndorsementService endorsementService,final RefferService refferService,final CertificationService certificationService){
		this.shoppingOrderService = shoppingOrderService;
		this.productService = productService;
		this.userService = userService;
		this.shoppingCartService =shoppingCartService;
		this.couponService =couponService;
		this.addressService =addressService;
		this.endorsementService=endorsementService;
		this.refferService=refferService;
		this.certificationService=certificationService;
	}
	
	
	//包裹删除接口 (GET方式)
	public Result pardels_del(){
		response().setContentType("application/json;charset=utf-8");
		String uid = AjaxHellper.getHttpParam(request(), "uid");
		String pid = AjaxHellper.getHttpParam(request(), "packId");
		String devid = AjaxHellper.getHttpParam(request(), "devid")==null?"":AjaxHellper.getHttpParam(request(), "devid");
		String appversion=AjaxHellper.getHttpParam(request(), "appversion");
		String wdhjy = AjaxHellper.getHttpParam(request(),"wdhjy");
		StatusOnlyVO result = new StatusOnlyVO();
		if (!StringUtil.checkMd5(devid, wdhjy,appversion))
		{
			result.setStatus(0);
			return ok(Json.toJson(result));
		}
		
		shoppingOrderService.pardelsDel("-99", Numbers.parseLong(uid, 0L), Numbers.parseLong(pid, 0L));
		result.setStatus(1);
		return ok(Json.toJson(result));
	}

	//订单取消接口 (GET方式) 
	public Result  shoppingOrder_cancel(){
		response().setContentType("application/json;charset=utf-8");
		String uid = AjaxHellper.getHttpParam(request(), "uid");
		String orderCode = AjaxHellper.getHttpParam(request(), "orderCode");
		String devid = AjaxHellper.getHttpParam(request(), "devid")==null?"":AjaxHellper.getHttpParam(request(), "devid");
		String appversion=AjaxHellper.getHttpParam(request(), "appversion");
		String wdhjy = AjaxHellper.getHttpParam(request(),"wdhjy");
		StatusMsgVO result = new StatusMsgVO();
		if (!StringUtil.checkMd5(devid, wdhjy,appversion))
		{
			result.setStatus("0");
			return ok(Json.toJson(result));
		}
		
		String req_status=shoppingOrderService.updateOrderStatus(Numbers.parseLong(uid, 0L),orderCode,"5");
		if(!"0".equals(req_status)){
			result.setStatus("2");
			result.setMsg("您的订单状态有变化");
		}else{
			result.setStatus("1");
			result.setMsg("");
		}
		return ok(Json.toJson(result));
	}
	
	//(四十四)	获取物流信息接口 (GET方式) shoppingOrder_wayBill.php
	public Result shoppingOrder_wayBill() {
		response().setContentType("application/json;charset=utf-8");
		String uid = AjaxHellper.getHttpParam(request(), "uid");
		String packId = AjaxHellper.getHttpParam(request(), "packId");
		String devid = AjaxHellper.getHttpParam(request(), "devid")==null?"":AjaxHellper.getHttpParam(request(), "devid");
		String appversion=AjaxHellper.getHttpParam(request(), "appversion");
		String wdhjy = AjaxHellper.getHttpParam(request(),"wdhjy");
		ShoppingOrderWayBillVO result = new ShoppingOrderWayBillVO();
		if (!StringUtil.checkMd5(devid, wdhjy,appversion))
		{
			result.status=0;
			return ok(Json.toJson(result));
		}
		
		
		if(StringUtils.isBlank(packId)||StringUtils.isBlank(uid)){
			result.status=0;
			return ok(Json.toJson(result));
		}else{
			ShoppingOrder shoppingOrder = shoppingOrderService.getOrderInfo_ByPackId(packId);
			result.orderType = shoppingOrder.getOrdertype();
			result.status=1;
			result = shoppingOrderService.getPardelsWaybill_ByPardelsId(packId,result);
		}
		return ok(Json.toJson(result));
	}
	
	
	//获取结算明细接口（去结算）(GET方式) shoppingOrder_cost.php
	public Result  shoppingOrder_cost(){
		response().setContentType("application/json;charset=utf-8");
		String uid = AjaxHellper.getHttpParam(request(), "uid");
		String datastr = AjaxHellper.getHttpParam(request(), "datastr");
		String couponId = AjaxHellper.getHttpParam(request(), "couponId")==null?"0": AjaxHellper.getHttpParam(request(), "couponId");
		String devid = AjaxHellper.getHttpParam(request(), "devid")==null?"":AjaxHellper.getHttpParam(request(), "devid");
		String appversion=AjaxHellper.getHttpParam(request(), "appversion");
		String wdhjy = AjaxHellper.getHttpParam(request(),"wdhjy");
		ShoppingOrderCostVO result = new ShoppingOrderCostVO();
		if (!StringUtil.checkMd5(devid, wdhjy,appversion))
		{
			result.status=0;
			return ok(Json.toJson(result));
		}
		
		
		ShoppingOrderCostItem data = new ShoppingOrderCostItem();
		
		String pidstr = "";
		if(StringUtils.isBlank(uid)||StringUtils.isBlank(datastr)){
			result.status=0;
			return ok(Json.toJson(result));
		}
		
		Coupon coupon = null;
		int couponType =0;
		
		int couponFee = 0;
		
		if (Numbers.parseInt(couponId, 0)>0)
		{	
			List<Coupon> couponList = couponService.getCouponListByUseId(couponId, "0", uid);
			if (couponList!=null && couponList.size()>0)
			{
				coupon= couponList.get(0);
				couponType=coupon.getTyp();
				switch (couponType) {
				case 1:
					data.coupon = coupon.getCouponprice().intValue() + "元券";
					break;
				case 2:
					data.coupon = coupon.getCouponprice().intValue() + "元券";
					break;
				case 3:
					data.coupon = "商品券";
					break;
				case 4:
					data.coupon = "商品券";
					break;
				case 5:
					data.coupon = "指定商品"+coupon.getCouponprice().intValue() + "元券";
					break;
				case 6:
					data.coupon = "指定商品"+coupon.getCouponprice().intValue() + "元券";
					break;
				}
				if(couponType==3||couponType==4){
					datastr=datastr+","+coupon.getPid()+"_a_1";
				}
				data.couponfee = couponFee+"";
			}
		}
		
		List<Object[]> deviceUsers = userService.getDeviceUser(uid);
		if(deviceUsers!=null && deviceUsers.size()>0){
			String dataArray[] = datastr.split(",");
			data.p_list = new ArrayList<Object>();
			int i=0;
			
			BigDecimal freight=new BigDecimal(0);
			int fe=0;
			String fname="";
			String wayremark="";
			
			BigDecimal weightTemp =new BigDecimal(0);
			for(String dataTemp:dataArray){
				String dataNeed[] = dataTemp.split("_");
				String pid=dataNeed[0];
				String price=dataNeed[1];
				String counts=dataNeed[2];
				if(price!=null && "¥".equals(price.substring(0, 1)) ){
					price=price.substring(1, price.length());
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
						ShoppingCartCategoryVO shoppingCartCategoryVO=new ShoppingCartCategoryVO();
						shoppingCartCategoryVO.setTyp("3");
						shoppingCartCategoryVO.setFreight("¥"+freight.intValue());
						data.p_list.add(shoppingCartCategoryVO);
						data.foreignfee = freight.add(new BigDecimal(data.foreignfee)).setScale(0, BigDecimal.ROUND_CEILING).toString();
					}
				}
				
				weightTemp =weightTemp.add(new BigDecimal( product.getWeight()*Numbers.parseInt(counts, 0)+"").setScale(1, BigDecimal.ROUND_CEILING)) ;
				
				
				ShoppingOrderCostItem shoppingOrderCostItem =shoppingOrderService.getShoppingOrderCostList(Numbers.parseLong(uid, 0L),Numbers.parseLong(pid, 0L),price,counts,fname,wayremark);
				fname = shoppingOrderCostItem.fname;
				wayremark = shoppingOrderCostItem.wayremark;
				data.p_list.addAll(shoppingOrderCostItem.p_list);
			
				data.total_fee+=shoppingOrderCostItem.total_fee.intValue();
				Double tempGoodsFee = (Numbers.parseDouble(data.goods_fee, 0D)+Numbers.parseDouble(shoppingOrderCostItem.goods_fee, 0D));
				data.goods_fee = ""+tempGoodsFee.intValue();
				
				
				if(i==dataArray.length-1){
					freight = new BigDecimal(ShoppingCartService.getfreight(fe, weightTemp.toString()));
					ShoppingCartCategoryVO shoppingCartCategoryVO=new ShoppingCartCategoryVO();
					shoppingCartCategoryVO.setTyp("3");
					shoppingCartCategoryVO.setFreight("¥"+freight.intValue());
					data.p_list.add(shoppingCartCategoryVO);
					data.foreignfee = freight.add(new BigDecimal(data.foreignfee)).setScale(0, BigDecimal.ROUND_CEILING).toString();
				}
				i++;
			}
			data.total_fee = new BigDecimal(data.foreignfee).add(new BigDecimal(data.total_fee)).intValue();
			if (pidstr.endsWith(",")){
				pidstr=pidstr.substring(0, pidstr.length()-1);
			}
			if(coupon!=null)
			{
				couponFee = couponService.getCouponPrice(couponId, pidstr);
				if(couponType==1 || couponType==5)
				{
					data.total_fee=(data.total_fee-couponFee);
				}
				if (couponType==2 || couponType==6)
				{
					if (data.total_fee>=coupon.getTprice())
					{
						data.total_fee=data.total_fee-couponFee;
					}
				}
				if (data.total_fee<=0)
				{
					data.total_fee=0;
				}
			}
			data.foreignfee ="¥"+data.foreignfee;
			data.goods_fee = "¥"+data.goods_fee;
			data.totalfee = "¥"+data.total_fee;
			data.money = data.total_fee;
			data.addressdata = (addressService.address_default(Numbers.parseLong(uid, 0L))==null||addressService.address_default(Numbers.parseLong(uid, 0L)).size()==0)?null:addressService.address_default(Numbers.parseLong(uid, 0L)).get(0);
			if(data.addressdata!=null){
				Address a=data.addressdata;
				if(!StringUtils.isBlank(a.getImgpath())){
					if(a.getImgpath().indexOf(",")>0){
						String[] tmp=a.getImgpath().split(",");
						a.setImgpath(tmp[0]);
						a.setSecondimgpath(tmp[1]);						
					}					
				}
				data.addressdata=a;
			}

			data.couponcount = couponService.getUserCouponCnt(uid, data.total_fee,pidstr);
			data.balance = userService.getUserBalance(uid);
			data.hgPaymentType = shoppingOrderService.getHgPaymentType();
			result.data = data;
			result.status = 1;
		}
		
		return ok(Json.toJson(result));
	}
	
	//获取结算明细接口（去结算）(GET方式) shoppingOrder_costpresell.php
	public Result  shoppingOrder_costpresell(){
		response().setContentType("application/json;charset=utf-8");
		String uid = AjaxHellper.getHttpParam(request(), "uid");
		String datastr = AjaxHellper.getHttpParam(request(), "datastr");
		String orderCode = AjaxHellper.getHttpParam(request(), "orderCode")==null?"": AjaxHellper.getHttpParam(request(), "orderCode");
		String couponId = AjaxHellper.getHttpParam(request(), "couponId")==null?"0": AjaxHellper.getHttpParam(request(), "couponId");
		String devid = AjaxHellper.getHttpParam(request(), "devid")==null?"":AjaxHellper.getHttpParam(request(), "devid");
		String appversion=AjaxHellper.getHttpParam(request(), "appversion");
		String wdhjy = AjaxHellper.getHttpParam(request(),"wdhjy");
		ShoppingOrderCostPreVO result = new ShoppingOrderCostPreVO();
		if (!StringUtil.checkMd5(devid, wdhjy,appversion))
		{
			result.status=0;
			return ok(Json.toJson(result));
		}
		
		
		
		String path=StringUtil.getOSSUrl();
		if (Numbers.parseInt(couponId, 0)>0)
		{	
			List<Coupon> couponList = couponService.getCouponListByUseId(couponId, "0", uid);
			if (couponList!=null && couponList.size()>0)
			{
				Coupon coupon = couponList.get(0);
			}
		}
		if(StringUtils.isBlank(uid)||StringUtils.isBlank(datastr)){
			result.status=0;
			return ok(Json.toJson(result));
		}
		List<Object[]> deviceUsers = userService.getDeviceUser(uid);
		if(deviceUsers!=null && deviceUsers.size()>0){
			String dataArray[] = datastr.split(",");
			ShoppingOrderCostPreItem data = new ShoppingOrderCostPreItem();
			data.p_list = new ArrayList<Object>();
			BigDecimal deposit=new BigDecimal(0);
			BigDecimal tfreightFee=new BigDecimal(0);
			BigDecimal tgoodsfee=new BigDecimal(0);
			for(String dataTemp:dataArray){
				String dataNeed[] = dataTemp.split("_");
				String pid=dataNeed[0];
				String price=dataNeed[1];
				String counts=dataNeed[2];
				if(price!=null && "¥".equals(price.substring(0, 1)) ){
					price=price.substring(1, price.length());
				}
				Product product =productService.getProductById(Numbers.parseLong(pid, 0L));
				deposit = deposit.add(new BigDecimal(product.getDeposit()*Numbers.parseInt(counts, 0)));
				ProductDetailCostpresellItem productDetailItem = productService.covertToProductDetailCostPresellItem(product,productService,uid);
				productDetailItem.counts=counts;
				Double weightTemp =product.getWeight()*Numbers.parseInt(counts, 0);
				tfreightFee = new BigDecimal(ShoppingCartService.getfreight(product.getFromsite(), weightTemp.toString()));
				tgoodsfee = tgoodsfee.add(new BigDecimal(productDetailItem.rmb_price_no_symbol).multiply(new BigDecimal(counts)));
				
				int balance_due=tgoodsfee.subtract(deposit).intValue();
				if (balance_due<0)
				{
					productDetailItem.finalpay="¥0";
					productDetailItem.final_pay=String.valueOf(balance_due);
				}else{
					productDetailItem.finalpay="¥"+balance_due;
					productDetailItem.final_pay=String.valueOf(balance_due);
				}
				if (!StringUtils.isBlank(orderCode)){
					productDetailItem.paytim = productService.getPayTime(orderCode);
				}
				productDetailItem.deposit="¥"+deposit;
				productDetailItem.iscoupon="0";
				
				data.p_list.add(productDetailItem);
			}
			data.domestic_fee="¥0";
			data.foreignfee="¥"+tfreightFee.toString();
			data.tariff_fee="¥0";
			data.cost_fee="¥0";
			data.total_fee=deposit.add(tfreightFee).intValue();
			data.totalfee="¥"+deposit.add(tfreightFee);
			data.pay_fee=deposit.add(tfreightFee).intValue();
			data.payfee="¥"+deposit.add(tfreightFee);
			data.goods_fee="¥"+tgoodsfee;
			data.money=tgoodsfee.add(tfreightFee).intValue();
			data.balance = userService.getUserBalance(uid);
			data.addressdata = (addressService.address_default(Numbers.parseLong(uid, 0L))==null||addressService.address_default(Numbers.parseLong(uid, 0L)).size()==0)?null:addressService.address_default(Numbers.parseLong(uid, 0L)).get(0);
			if(data.addressdata!=null){
				Address a=data.addressdata;
				if(!StringUtils.isBlank(a.getImgpath())){
					if(a.getImgpath().indexOf(",")>1){
						String[] tmp=a.getImgpath().split(",");
						a.setImgpath(path+tmp[0]);
						a.setSecondimgpath(path+tmp[1]);						
					}
					else
					{
						a.setImgpath(path+a.getImgpath().replace(",", ""));
					}
				}
				data.addressdata=a;
			}
			data.hgPaymentType = shoppingOrderService.getHgPaymentType();
			result.data = data;
			result.status=1;
		}
		
		return ok(Json.toJson(result));
	}
	
	//订单删除接口(GET方式)
	public Result shoppingOrder_del(){
		response().setContentType("application/json;charset=utf-8");
		String uid = AjaxHellper.getHttpParam(request(), "uid");
		String orderCode = AjaxHellper.getHttpParam(request(), "orderCode");
		String devid = AjaxHellper.getHttpParam(request(), "devid")==null?"":AjaxHellper.getHttpParam(request(), "devid");
		String appversion=AjaxHellper.getHttpParam(request(), "appversion");
		String wdhjy = AjaxHellper.getHttpParam(request(),"wdhjy");
		StatusOnlyVO result = new StatusOnlyVO();
		if (!StringUtil.checkMd5(devid, wdhjy,appversion))
		{
			result.setStatus(0);
			return ok(Json.toJson(result));
		}
		
		
		String req_status=shoppingOrderService.updateOrderStatus(Numbers.parseLong(uid, 0L),orderCode,"-99");
		result.setStatus(1);
		return ok(Json.toJson(result));
	}
	
	//(三十六)	订单详情接口(GET方式) shoppingOrder_Info.php
	public Result shoppingOrder_Info(){
		response().setContentType("application/json;charset=utf-8");
		String uid = AjaxHellper.getHttpParam(request(), "uid");
		String orderCode = AjaxHellper.getHttpParam(request(), "orderCode");
		String devid = AjaxHellper.getHttpParam(request(), "devid")==null?"":AjaxHellper.getHttpParam(request(), "devid");
		String appversion=AjaxHellper.getHttpParam(request(), "appversion");
		String wdhjy = AjaxHellper.getHttpParam(request(),"wdhjy");
		ShoppingOrderDetailResultVO result = new ShoppingOrderDetailResultVO();
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
		ShoppingOrderDetail shoppingOrderDetail = new ShoppingOrderDetail();
		AddressData addressData = new AddressData();
		addressData.province=shoppingOrder.getProvince();
		addressData.address = shoppingOrder.getAddress();
		addressData.name = shoppingOrder.getName();
		addressData.phone=shoppingOrder.getPhone();
		addressData.postcode=shoppingOrder.getPostcode();
		addressData.cardId = shoppingOrder.getCardId();
		
		
		shoppingOrderDetail.addressdata =addressData; 
		shoppingOrderDetail.id = String.valueOf(shoppingOrder.getId());
		shoppingOrderDetail.orderCode = shoppingOrder.getOrderCode();
		shoppingOrderDetail.ordertype = shoppingOrder.getOrdertype();

		List<OrderLoveLyBack> orderLoveLyBackList = shoppingOrderService.getOrderLovelyBack(shoppingOrder.getOrderCode());
		if(orderLoveLyBackList.size()>0){
			shoppingOrderDetail.lovely_peolpe =orderLoveLyBackList.get(0).getName();
			shoppingOrderDetail.lovely_speck = orderLoveLyBackList.get(0).getRemark();
		}else{
			shoppingOrderDetail.lovely_peolpe = "";
			shoppingOrderDetail.lovely_speck = "";
		}
		
		shoppingOrderDetail.original_fee = "¥"+new BigDecimal(shoppingOrder.getOriginal_fee()).setScale(0, BigDecimal.ROUND_CEILING).intValue();
		shoppingOrderDetail.totalfee = "¥"+new BigDecimal(shoppingOrder.getTotalFee()).setScale(0, BigDecimal.ROUND_CEILING).intValue();
		shoppingOrderDetail.total_fee = String.valueOf(new BigDecimal(shoppingOrder.getTotalFee()).setScale(0, BigDecimal.ROUND_CEILING).intValue());
		shoppingOrderDetail.paymethod = String.valueOf(shoppingOrder.getPaymethod()) ;
		shoppingOrderDetail.paystat = String.valueOf(shoppingOrder.getPaystat()) ;
		shoppingOrderDetail.stage = String.valueOf(shoppingOrder.getPaystat()) ;
		
		if(shoppingOrder.getStatus()==15)
		{
			shoppingOrderDetail.status= "6";
		}else{
			shoppingOrderDetail.status= String.valueOf(shoppingOrder.getStatus());
		}
		
		switch(shoppingOrder.getStatus())
		{
				case 0:
				shoppingOrderDetail.orderToast="待支付";
			break;
				case 1:
				shoppingOrderDetail.orderToast="待发货";
			break;
				case 2:
				shoppingOrderDetail.orderToast="已发货";
			break;
				case 3:
				shoppingOrderDetail.orderToast="已完成";
			break;
				case 5:
				shoppingOrderDetail.orderToast="已取消";
			break;
				case 6:
				shoppingOrderDetail.orderToast="已退款";
			break;
				case 15:
				shoppingOrderDetail.orderToast="已退款";
			break;
				case 16:
				shoppingOrderDetail.orderToast="尾款未支付，订金退款";
			break;
				case 21:
				shoppingOrderDetail.orderToast="订金已支付";
			break;
				case 22:
				shoppingOrderDetail.orderToast="尾款待支付";
			break;
			
		}
		shoppingOrderDetail.goods_fee = "¥"+shoppingOrder.getGoods_fee().intValue();
		shoppingOrderDetail.domestic_fee = String.valueOf(shoppingOrder.getDomestic_fee().intValue());
		shoppingOrderDetail.foreignfee = "¥"+shoppingOrder.getForeignfee().intValue();
		shoppingOrderDetail.tariff_fee = "¥"+shoppingOrder.getTariff_fee().intValue();
		shoppingOrderDetail.cost_fee = "¥"+shoppingOrder.getCost_fee().intValue();
		
		
		if(shoppingOrder.getOrdertype().equals("3")){
			if(shoppingOrder.getStatus()==0){
				shoppingOrderDetail.orderCode_Pay = shoppingOrderDetail.orderCode+"001";
			}else{
				shoppingOrderDetail.orderCode_Pay = shoppingOrderDetail.orderCode+"002";
			}
			BigDecimal foreignfee= new BigDecimal(shoppingOrder.getForeignfee()).setScale(0,BigDecimal.ROUND_CEILING);
			BigDecimal deposit= new BigDecimal(shoppingOrder.getDeposit()).setScale(0,BigDecimal.ROUND_CEILING);
			BigDecimal totalFee= new BigDecimal(shoppingOrder.getTotalFee()) ;
			if("0".equals(shoppingOrderDetail.status)||"21".equals(shoppingOrderDetail.status)||"5".equals(shoppingOrderDetail.status)){
				shoppingOrderDetail.pay_fee =String.valueOf(foreignfee.add(deposit).intValue()) ;
				shoppingOrderDetail.payfee ="¥"+shoppingOrderDetail.pay_fee ;
			}else{
				if("22".equals(shoppingOrderDetail.status)){
					shoppingOrderDetail.pay_fee=String.valueOf(totalFee.subtract(deposit).subtract(foreignfee).intValue());
					shoppingOrderDetail.payfee="¥"+shoppingOrderDetail.pay_fee;
				}else{
					shoppingOrderDetail.pay_fee=String.valueOf(totalFee.intValue());
					shoppingOrderDetail.payfee="¥"+shoppingOrderDetail.pay_fee;
				}
			}
			shoppingOrderDetail.deposit="¥"+String.valueOf(deposit.intValue());
			shoppingOrderDetail.final_pay=String.valueOf(shoppingOrder.getFinalpay().intValue());
			shoppingOrderDetail.finalpay="¥"+shoppingOrderDetail.final_pay;
		}else{
			shoppingOrderDetail.orderCode_Pay = shoppingOrderDetail.orderCode;
		}
		List<Coupon> couponList = couponService.getByUserId(shoppingOrder.getCouponUserId());
		if(couponList.size()>0){
			Coupon coupon = couponList.get(0);
			int couponType = coupon.getTyp();

			switch(couponType)
			{
				case 1:
					shoppingOrderDetail.coupon = coupon.getCouponprice().intValue() + "元券";
					break;
				case 2:
					shoppingOrderDetail.coupon = coupon.getCouponprice().intValue() + "元券";
					break;
				case 3:
					shoppingOrderDetail.coupon = "商品券";
					break;
				case 4:
					shoppingOrderDetail.coupon = "商品券";
					break;
				case 5:
					shoppingOrderDetail.coupon = "指定商品"+coupon.getCouponprice().intValue() + "元券";
					break;
				case 6:
					shoppingOrderDetail.coupon = "指定商品"+coupon.getCouponprice().intValue() + "元券";
					break;
			}
			shoppingOrderDetail.coupontyp = couponType+"";
		}else{
			shoppingOrderDetail.coupon = "";
			shoppingOrderDetail.coupontyp = "";
		}
		shoppingOrderDetail.dateAddTime = CHINESE_DATE_TIME_FORMAT.format(shoppingOrder.getDate_add());
		List<Parcels> parcelsList= shoppingOrderService.queryPardelsByOrderId(shoppingOrder.getId());
		if(parcelsList.size()>0)
			shoppingOrderDetail.isHavePack = "1";
		else
			shoppingOrderDetail.isHavePack = "0";
		shoppingOrderDetail.packCount = parcelsList.size();
		List<Product> productList = new ArrayList<Product>();
		for(Parcels parcels:parcelsList){
			productList.addAll(shoppingOrderService.queryProductListByParcelsId(parcels.getId()));
		}
		List<Product> productListOutParcels = productService.getOutPardelsProduct_ByOrderCode(orderCode,"0");
		if(productListOutParcels!=null&& productListOutParcels.size()>0){
			productList.addAll(productListOutParcels);
		}
		shoppingOrderDetail.p_list = productService.covertToOrderInfoList(productList);
		String depositPayTime = shoppingOrderService.getpaytim(orderCode,"0");
		shoppingOrderDetail.depositPayTime = depositPayTime;
		
		String finalPayTime = shoppingOrderService.getpaytim(orderCode,"1");
		if(!"".equals(finalPayTime)){
			shoppingOrderDetail.finalPayTime = finalPayTime;
		}else{
			shoppingOrderDetail.finalPayTime = productList.size()>0?productList.get(0).getPaytim():"";
		}
		shoppingOrderDetail.rtitle = productList.size()>0?productList.get(0).getRtitle():"";
		if("2".equals(shoppingOrder.getOrdertype())||"5".equals(shoppingOrder.getOrdertype())){
			shoppingOrderDetail.toast = "撒娇已享受"+String.valueOf(shoppingOrder.getLovelydistinct()).replace(".0", "")+"折";
		}else{
			shoppingOrderDetail.toast = "";
		}
		shoppingOrderDetail.remark = productList.size()>0?productList.get(0).getPreselltoast():"";
		shoppingOrderDetail.balance = userService.getUserBalance(uid);
		shoppingOrderDetail.hgPaymentType = shoppingOrderService.getHgPaymentType();
		result.data = shoppingOrderDetail;
		return ok(Json.toJson(result));
	}
	
	
	
	//(三十六)	预售订单详情接口(GET方式) shoppingOrder_Info_presell.php
	public Result shoppingOrder_Info_presell(){
		response().setContentType("application/json;charset=utf-8");
		String uid = AjaxHellper.getHttpParam(request(), "uid");
		String orderCode = AjaxHellper.getHttpParam(request(), "orderCode");
		String devid = AjaxHellper.getHttpParam(request(), "devid")==null?"":AjaxHellper.getHttpParam(request(), "devid");
		String appversion=AjaxHellper.getHttpParam(request(), "appversion");
		String wdhjy = AjaxHellper.getHttpParam(request(),"wdhjy");
		ShoppingOrderPresellDetailResultVO result = new ShoppingOrderPresellDetailResultVO();
		if (!StringUtil.checkMd5(devid, wdhjy,appversion))
		{
			result.status=0;
			return ok(Json.toJson(result));
		}
		
		
		ShoppingOrder shoppingOrder = shoppingOrderService.getShoppingOrderByOrderCode(orderCode);
		if(shoppingOrder==null){
			return ok();
		}
		result.status=1;
		ShoppingOrderPresellDetail shoppingOrderDetail = new ShoppingOrderPresellDetail();
		AddressPreselData addressData = new AddressPreselData();
		addressData.province=shoppingOrder.getProvince();
		addressData.address = shoppingOrder.getAddress();
		addressData.name = shoppingOrder.getName();
		addressData.phone=shoppingOrder.getPhone();
		addressData.postcode=shoppingOrder.getPostcode();
		addressData.cardId = shoppingOrder.getCardId();
		
		shoppingOrderDetail.addressdata =addressData; 
		shoppingOrderDetail.id = String.valueOf(shoppingOrder.getId());
		shoppingOrderDetail.orderCode = shoppingOrder.getOrderCode();
		shoppingOrderDetail.ordertype = shoppingOrder.getOrdertype();

		List<OrderLoveLyBack> orderLoveLyBackList = shoppingOrderService.getOrderLovelyBack(shoppingOrder.getOrderCode());
		if(orderLoveLyBackList.size()>0){
			shoppingOrderDetail.lovely_peolpe =orderLoveLyBackList.get(0).getName();
			shoppingOrderDetail.lovely_speck = orderLoveLyBackList.get(0).getRemark();
		}else{
			shoppingOrderDetail.lovely_peolpe = "";
			shoppingOrderDetail.lovely_speck = "";
		}
		
		shoppingOrderDetail.original_fee = "¥"+new BigDecimal(shoppingOrder.getOriginal_fee()).setScale(0, BigDecimal.ROUND_CEILING).intValue();
		shoppingOrderDetail.totalfee = "¥"+new BigDecimal(shoppingOrder.getTotalFee()).setScale(0, BigDecimal.ROUND_CEILING).intValue();
		shoppingOrderDetail.total_fee = String.valueOf(new BigDecimal(shoppingOrder.getTotalFee()).setScale(0, BigDecimal.ROUND_CEILING).intValue());
		shoppingOrderDetail.paymethod = String.valueOf(shoppingOrder.getPaymethod()) ;
		shoppingOrderDetail.paystat = String.valueOf(shoppingOrder.getPaystat()) ;
		shoppingOrderDetail.stage = String.valueOf(shoppingOrder.getPaystat()) ;
		
		if(shoppingOrder.getStatus()==15)
		{
			shoppingOrderDetail.status= "6";
		}else{
			shoppingOrderDetail.status= String.valueOf(shoppingOrder.getStatus());
		}
		
		switch(shoppingOrder.getStatus())
		{
				case 0:
				shoppingOrderDetail.orderToast="待支付";
			break;
				case 1:
				shoppingOrderDetail.orderToast="待发货";
			break;
				case 2:
				shoppingOrderDetail.orderToast="已发货";
			break;
				case 3:
				shoppingOrderDetail.orderToast="已完成";
			break;
				case 5:
				shoppingOrderDetail.orderToast="已取消";
			break;
				case 6:
				shoppingOrderDetail.orderToast="已退款";
			break;
				case 15:
				shoppingOrderDetail.orderToast="已退款";
			break;
				case 16:
				shoppingOrderDetail.orderToast="尾款未支付，订金退款";
			break;
				case 21:
				shoppingOrderDetail.orderToast="订金已支付";
			break;
				case 22:
				shoppingOrderDetail.orderToast="尾款待支付";
			break;
			
		}
		shoppingOrderDetail.goods_fee = "¥"+shoppingOrder.getGoods_fee().intValue();
		shoppingOrderDetail.domestic_fee = String.valueOf(shoppingOrder.getDomestic_fee().intValue());
		shoppingOrderDetail.foreignfee = "¥"+shoppingOrder.getForeignfee().intValue();
		shoppingOrderDetail.tariff_fee = "¥"+shoppingOrder.getTariff_fee().intValue();
		shoppingOrderDetail.cost_fee = "¥"+shoppingOrder.getCost_fee().intValue();
		
		
		if(shoppingOrder.getOrdertype().equals("3")){
			if(shoppingOrder.getStatus()==0){
				shoppingOrderDetail.orderCode_Pay = shoppingOrderDetail.orderCode+"001";
			}else{
				shoppingOrderDetail.orderCode_Pay = shoppingOrderDetail.orderCode+"002";
			}
			BigDecimal foreignfee= new BigDecimal(shoppingOrder.getForeignfee()).setScale(0,BigDecimal.ROUND_CEILING);
			BigDecimal deposit= new BigDecimal(shoppingOrder.getDeposit()).setScale(0,BigDecimal.ROUND_CEILING);
			BigDecimal totalFee= new BigDecimal(shoppingOrder.getTotalFee()) ;
			if("0".equals(shoppingOrderDetail.status) || "16".equals(shoppingOrderDetail.status) || "21".equals(shoppingOrderDetail.status)||"5".equals(shoppingOrderDetail.status)){
				shoppingOrderDetail.pay_fee =String.valueOf(foreignfee.add(deposit).intValue()) ;
				shoppingOrderDetail.payfee ="¥"+shoppingOrderDetail.pay_fee ;
			}else{
				if("22".equals(shoppingOrderDetail.status)){
					shoppingOrderDetail.pay_fee=String.valueOf(totalFee.subtract(deposit).subtract(foreignfee).intValue());
					shoppingOrderDetail.payfee="¥"+shoppingOrderDetail.pay_fee;
				}else{
					shoppingOrderDetail.pay_fee=String.valueOf(totalFee.intValue());
					shoppingOrderDetail.payfee="¥"+shoppingOrderDetail.pay_fee;
				}
			}
			shoppingOrderDetail.deposit="¥"+String.valueOf(deposit.intValue());
			shoppingOrderDetail.final_pay=String.valueOf(shoppingOrder.getFinalpay().intValue());
			shoppingOrderDetail.finalpay="¥"+shoppingOrderDetail.final_pay;
		}else{
			shoppingOrderDetail.orderCode_Pay = shoppingOrderDetail.orderCode;
		}
		List<Coupon> couponList = couponService.getByUserId(shoppingOrder.getCouponUserId());
		if(couponList.size()>0){
			Coupon coupon = couponList.get(0);
			int couponType = coupon.getTyp();

			switch(couponType)
			{
				case 1:
				shoppingOrderDetail.coupon =coupon.getCouponprice().intValue()+"元券";
				break;
				case 2:
				shoppingOrderDetail.coupon =coupon.getCouponprice().intValue()+"元券";
				break;
				case 3:
				shoppingOrderDetail.coupon ="商品券";
				break;
				case 4:
				shoppingOrderDetail.coupon ="商品券";
				break;
			}
		}else{
			shoppingOrderDetail.coupon = "";
			shoppingOrderDetail.coupontyp = "";
		}
		shoppingOrderDetail.dateAddTime = CHINESE_DATE_TIME_FORMAT.format(shoppingOrder.getDate_add());
		List<Parcels> parcelsList= shoppingOrderService.queryPardelsByOrderId(shoppingOrder.getId());
		if(parcelsList.size()>0)
			shoppingOrderDetail.isHavePack = "1";
		else
			shoppingOrderDetail.isHavePack = "0";
		shoppingOrderDetail.packCount = parcelsList.size();
		List<Product> productList = new ArrayList<Product>();
		for(Parcels parcels:parcelsList){
			productList.addAll(shoppingOrderService.queryProductListByParcelsId(parcels.getId()));
		}
		List<Product> productListOutParcels = productService.getOutPardelsProduct_ByOrderCode(orderCode,"0");
		if(productListOutParcels!=null&& productListOutParcels.size()>0){
			productList.addAll(productListOutParcels);
		}
		List<Product> productListRefund = productService.getRefundProduct_ByOrderCode(orderCode,"0");
		if(productListRefund!=null&& productListRefund.size()>0){
			productList.addAll(productListRefund);
		}
		shoppingOrderDetail.p_list = productService.covertToOrderPresellInfoList(productList);
		
		String depositPayTime = shoppingOrderService.getpaytim(orderCode,"0");
		shoppingOrderDetail.depositPayTime = depositPayTime;
		shoppingOrderDetail.balance = userService.getUserBalance(uid);
		String finalPayTime = shoppingOrderService.getpaytim(orderCode,"1");
		
		if(!"".equals(finalPayTime)){
			shoppingOrderDetail.finalPayTime = finalPayTime;
		}else{
			shoppingOrderDetail.finalPayTime = productList.size()>0?productList.get(0).getPaytim():"";
		}
		
		if("2".equals(shoppingOrder.getOrdertype())||"5".equals(shoppingOrder.getOrdertype())){
			BigDecimal lovelydistinct = new BigDecimal(shoppingOrder.getLovelydistinct()).setScale(1,BigDecimal.ROUND_CEILING);
			shoppingOrderDetail.toast = "撒娇已享受"+String.valueOf(lovelydistinct)+"折";
		}else{
			shoppingOrderDetail.toast = "";
		}
		shoppingOrderDetail.hgPaymentType = shoppingOrderService.getHgPaymentType();
		result.data = shoppingOrderDetail;
		return ok(Json.toJson(result));
	}
	
		
		
		
	// 订单列表接口(GET方式) （修改）shoppingOrder_list.php
	public Result shoppingOrder_list() {
		response().setContentType("application/json;charset=utf-8");
		String uid = AjaxHellper.getHttpParam(request(), "uid");
		String typ = AjaxHellper.getHttpParam(request(), "typ")==null?"0":AjaxHellper.getHttpParam(request(), "typ");
		String page = AjaxHellper.getHttpParam(request(), "page")==null?"0":AjaxHellper.getHttpParam(request(), "page");
		String devid = AjaxHellper.getHttpParam(request(), "devid")==null?"":AjaxHellper.getHttpParam(request(), "devid");
		String appversion=AjaxHellper.getHttpParam(request(), "appversion");
		String wdhjy = AjaxHellper.getHttpParam(request(),"wdhjy");
		ShoppingOrderResultVO result =new ShoppingOrderResultVO();
		if (!StringUtil.checkMd5(devid, wdhjy,appversion))
		{
			result.status=0;
			return ok(Json.toJson(result));
		}
		
		
		if(StringUtils.isBlank(uid)){
			result.status=0;
			return ok(Json.toJson(result));
		}
		List<Object[]> deviceUsers = userService.getDeviceUser(uid);
		if(deviceUsers!=null && deviceUsers.size()>0){
			ShoppingOrderQueryVO shoppingOrderQueryVO =new ShoppingOrderQueryVO();
			shoppingOrderQueryVO.typ = typ;
			shoppingOrderQueryVO.uid =Numbers.parseLong(uid, 0L);
			
			if ("5".equals(typ))
			{   
				//消红点
				userService.updateUserRedFlag(Numbers.parseLong(uid,0L), "myPresellsRedFlag", 0);
			}
			
			Page<ShoppingOrder> shoppingOrderPage = shoppingOrderService.queryShoppingOrderPage(shoppingOrderQueryVO,Numbers.parseInt(page, 0),Constants.PAGESIZE);
			int totalPage = shoppingOrderPage.getTotalPages();
			if(totalPage==Numbers.parseInt(page, 0)){
				result.endflag="1";
			}else{
				result.endflag="0";
			}
			List<ShoppingOrderItem> shoppingOrderItemList = new ArrayList<ShoppingOrderResultVO.ShoppingOrderItem>();
			int orderstatus=0;
			for(ShoppingOrder shoppingOrder:shoppingOrderPage){
				ShoppingOrderItem shoppingOrderItem = new ShoppingOrderItem();
				shoppingOrderItem.id = String.valueOf(shoppingOrder.getId());
				shoppingOrderItem.ordercode = shoppingOrder.getOrderCode();
				shoppingOrderItem.ordertype = shoppingOrder.getOrdertype();
				shoppingOrderItem.deposit = "¥"+shoppingOrder.getDeposit().intValue();
				shoppingOrderItem.finalpay = "¥"+String.valueOf(shoppingOrder.getFinalpay()).replace(".0", "");
				shoppingOrderItem.finalDate = "";
				shoppingOrderItem.totalfee = "¥"+shoppingOrder.getTotalFee().intValue();
				shoppingOrderItem.status = String.valueOf(shoppingOrder.getStatus());
				shoppingOrderItem.refund_amount = String.valueOf(shoppingOrder.getRefund_amount().intValue());
				shoppingOrderItem.lovelydistinct = String.valueOf(shoppingOrder.getLovelydistinct()).replace(".0", "")+"折";
				List<PackageItem> packagelist = new ArrayList<ShoppingOrderResultVO.PackageItem>();
				List<Parcels> parcelList = shoppingOrderService.queryPardelsByOrderId(shoppingOrder.getId());
				for(Parcels parcels:parcelList){
					PackageItem packageItem = new PackageItem();
					packageItem.packId=String.valueOf(parcels.getId());
					packageItem.src=parcels.getSrc();
					packageItem.packagecode = parcels.getParcelCode();
					packageItem.packagestatus = String.valueOf(parcels.getStatus());
					orderstatus=parcels.getStatus()==null?0:parcels.getStatus().intValue();
					if ("1".equals(shoppingOrderItem.status)||"2".equals(shoppingOrderItem.status))
					{
						switch(orderstatus)
						{
							case 0:
								orderstatus=1;
							break;
							case 1:
								orderstatus=1;
							break;
							case 2:
								orderstatus=2;
							break;
							case 3:
								orderstatus=2;
							break;
							case 4:
								orderstatus=2;
							break;
							case 5:
								orderstatus=3;
							break;
							case 11:
								orderstatus=2;
							break;
							case 12:
								orderstatus=3;
							break;
						}
					}else{
						orderstatus =Numbers.parseInt(shoppingOrderItem.status, 0) ;
					}
					packageItem.packagestatus=String.valueOf(orderstatus);
					
					List<PackageProductItem> packageProductItemList = new ArrayList<ShoppingOrderResultVO.PackageProductItem>();
					List<Product> productList = shoppingOrderService.queryProductListByParcelsId(parcels.getId());
					for(Product product : productList){
						PackageProductItem packageProductItem = productService.covertToPackageProductItem(product, parcels, shoppingOrder.getOrdertype());
						packageProductItemList.add(packageProductItem);
					}
					packageItem.packagelist = packageProductItemList;
					if (packageProductItemList.size()>0)
					{
						packagelist.add(packageItem);
					}
				}
				shoppingOrderItem.packagelist = packagelist;
				
				/*获取不属于包裹商品列表*/
				List<PackageProductItem> productOutParcel = shoppingOrderService.getOutPardelsProduct_ByOrderCode(shoppingOrder.getOrderCode(),0,shoppingOrder.getOrdertype());
				if(productOutParcel!=null && productOutParcel.size()>0){
					PackageItem packageItem = new PackageItem();
					packageItem.packId="0";
					packageItem.src="";
					packageItem.packagecode = "0";
					if(shoppingOrder.getStatus()==2){
						packageItem.packagestatus = "1";;
					}else{
						packageItem.packagestatus =String.valueOf(shoppingOrder.getStatus()) ;
					}
					packageItem.packagelist=productOutParcel;
					shoppingOrderItem.packagelist.add(packageItem);
				}
				
				/*获取退款商品列表*/
				List<PackageProductItem> productRefundList = shoppingOrderService.getRefundProduct_ByOrderCode(shoppingOrder.getOrderCode(),0,shoppingOrder.getOrdertype());
				if(productRefundList!=null && productRefundList.size()>0){
					PackageItem packageItem = new PackageItem();
					packageItem.packId="0";
					packageItem.src="";
					packageItem.packagecode = "-1";
					if(shoppingOrder.getStatus()==15||shoppingOrder.getStatus()==16){
						packageItem.packagestatus = String.valueOf(shoppingOrder.getStatus()) ;
					}else{
						packageItem.packagestatus ="15";
					}
					packageItem.packagelist=productRefundList;
					shoppingOrderItem.packagelist.add(packageItem);
				}
				
				shoppingOrderItemList.add(shoppingOrderItem);
			}
			result.status=1;
			result.data = shoppingOrderItemList;
			return ok(Json.toJson(result));
		}else{
			result.status=4;
			return ok(Json.toJson(result));
		}
	}
	
	
	//订单生成接口（去支付）(POST方式) shoppingOrder_pay.php
		public Result shoppingOrder_pay(){
			response().setContentType("application/json;charset=utf-8");
			String orderId=AjaxHellper.getHttpParamOfFormUrlEncoded(request(), "orderId");
			String usewallet=AjaxHellper.getHttpParamOfFormUrlEncoded(request(), "usewallet");
			String devid = AjaxHellper.getHttpParamOfFormUrlEncoded(request(), "devid")==null?"":AjaxHellper.getHttpParamOfFormUrlEncoded(request(), "devid");
			String appversion=AjaxHellper.getHttpParamOfFormUrlEncoded(request(), "appversion");
			String wdhjy = AjaxHellper.getHttpParamOfFormUrlEncoded(request(),"wdhjy");
			ProductNewVO result = new ProductNewVO();
			if (!StringUtil.checkMd5(devid, wdhjy,appversion))
			{
				result.status=0;
				return ok(Json.toJson(result));
			}
			
			result = shoppingOrderService.OrderPay(orderId,usewallet);
			return ok(Json.toJson(result));
		}
		
		
		
		
	//订单生成接口（去支付）(POST方式) shoppingOrder_new.php
	public Result shoppingOrder_new(){
		response().setContentType("application/json;charset=utf-8");
		Map<String, String> pramt = new HashMap<String, String>();
		String addressid = AjaxHellper.getHttpParamOfFormUrlEncoded(request(), "addressid");
		if(addressid!=null){
			pramt.put("addressid",addressid);
		}
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
		String typ = AjaxHellper.getHttpParamOfFormUrlEncoded(request(), "typ");
		if(typ!=null){
			pramt.put("typ",typ);
		}
		String uid = AjaxHellper.getHttpParamOfFormUrlEncoded(request(), "uid");
		if(uid!=null){
			pramt.put("uid",uid);
		}
		String usewallet=AjaxHellper.getHttpParamOfFormUrlEncoded(request(), "usewallet");
		if(usewallet!=null){
			pramt.put("usewallet",usewallet);
		}
		String wdhjy = AjaxHellper.getHttpParamOfFormUrlEncoded(request(),"wdhjy");
		if(wdhjy!=null){
			pramt.put("wdhjy",wdhjy);
		}
		String lovely = AjaxHellper.getHttpParamOfFormUrlEncoded(request(), "lovely");
		if(lovely!=null){
			pramt.put("lovely",lovely);
		}else{
			lovely ="1";
		}
		String couponId = AjaxHellper.getHttpParamOfFormUrlEncoded(request(), "couponId");
		if(couponId!=null){
			pramt.put("couponId",couponId);
		}else{
			couponId="0";
		}
		String vstr = AjaxHellper.getHttpParamOfFormUrlEncoded(request(), "vstr");
		if(vstr!=null){
			pramt.put("vstr",vstr);
		}
		String endorsementId=AjaxHellper.getHttpParamOfFormUrlEncoded(request(), "eid");
		if(endorsementId!=null){
			pramt.put("eid",endorsementId);
		}
		String mcode=AjaxHellper.getHttpParamOfFormUrlEncoded(request(), "mcode");
		if(mcode!=null){
			pramt.put("mcode",mcode);
		}
		String wx=AjaxHellper.getHttpParamOfFormUrlEncoded(request(),"wx");
		if(wx!=null){
			pramt.put("wx",wx);
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
		
		String reffer=AjaxHellper.getHttpParamOfFormUrlEncoded(request(), "ref");
		reffer=StringUtils.isBlank(reffer)?"":reffer;
		
		ProductNewVO result = new ProductNewVO();
		
		if (!StringUtil.checkMd5(devid, wdhjy,appversion))
		{
			result.status=0;
			return ok(Json.toJson(result));
		}
		
		if (!StringUtil.checksign(lwdjl,md5sign,appversion)){
			result.status=0;
			return ok(Json.toJson(result));
		}
		
		if(StringUtils.isBlank(wx))
			wx="0";
		String shareType=AjaxHellper.getHttpParamOfFormUrlEncoded(request(), "shareType");
		if(StringUtils.isBlank(shareType))
			shareType="";
		
		if(StringUtils.isBlank(uid)||StringUtils.isBlank(datastr)){
			result.status=0;
			return ok(Json.toJson(result));
		}
		/*List<Object[]> deviceUsers = userService.getDeviceUser(uid);
		User user=null;
		if(typ.equals("6")||typ.equals("7"))
			user=userService.getUserByUid(Numbers.parseLong(uid, 0L));
		*/
		/*
		 * 埋点
		 */
		Reffer ref=new Reffer();
		ref.setIp(request().remoteAddress());
		ref.setRefer(reffer);
		ref.setTyp(Constants.MAIDIAN_DINGDAN);
		ref.setTid(0L);
		refferService.addReffer(ref, "reffer"+CHINESE_DATE_MONTH.format(new Date()));

			Coupon coupon = null;
			String lovelyflg="0";
			Double p_distinct=10D;
			
			int p_couponUserId=0;
			String p_coupon="";
			Double p_coupon_price=0D;
			int couponType =0;
			
			/********typ 1:普通订单 2：商品撒娇订单 3：预售订单 5：全局撒娇订单  6:代言订单***********/
			if("5".equals(typ)){
				OrderLoveLy orderLovely = shoppingCartService.getOrderLovely();
				lovelyflg = String.valueOf(orderLovely.getLovely());
				p_distinct = orderLovely.getLovelydistinct();
			}
			int couponFee = 0;
			if(!"0".equals(couponId)){
//				couponFee=couponService.getCouponPrice(couponId, datastr);
				List<Coupon> couponList = couponService.getCouponListByUseId(couponId, "0", uid);
				if (couponList==null || couponList.size()<=0)
				{
					result.status = 5;
					result.msg="该优惠券已过期，或已使用";
					result.orderCode= "";
					return ok(Json.toJson(result));
				}else{
					coupon = couponList.get(0);
					couponType=coupon.getTyp();
					p_coupon_price = Numbers.parseDouble(couponFee+"", 0L);
					p_couponUserId =Numbers.parseInt(couponId, 0);
					p_coupon = coupon.getTitle();
					if(couponType==3||couponType==4){
						datastr=datastr+","+coupon.getPid()+"_a_1";
					}
				}
			}
			
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
							result.status = 7;
							result.msg="您购买的部分商品数量不合法，请重新选择";
							return ok(Json.toJson(result));
						}
						if(product.getStatus()!=10){
							logger.info("产品已下架"+product.getNstock());
							result.status = 7;
							result.msg="您购买的商品【"+product.getTitle()+"】已经下架,请重新选择";
							return ok(Json.toJson(result));
						}
						if("a".equals(price)){
							product.setRmbprice(0.0);
							product.setCounts(1);
							product.setWeight(0.0);
						}else{
							//如果不是代言商品则向上取整,以后去掉
							if(typ.equals("6") || typ.equals("66"))
								price=price;
							else
							{
								price=new BigDecimal(price).setScale(0, BigDecimal.ROUND_UP).intValue()+"";
							}
							////////3.0以后上述代码去掉
							product.setCounts(Numbers.parseInt(cnt, 0));
							Currency currency = productService.queryCurrencyById(product.getCurrency());
							 BigDecimal rate = new BigDecimal(currency.getRate()/100).setScale(4,  BigDecimal.ROUND_CEILING) ;
							 BigDecimal	priceDec=	new BigDecimal(product.getPrice()/100).setScale(2,  BigDecimal.ROUND_CEILING);   
							 BigDecimal rmb_price= rate.multiply(priceDec).setScale(2,BigDecimal.ROUND_CEILING);
							 /*******3.0以后以下去掉**********/
							 if(typ.equals("6")||typ.equals("66"))
								 rmb_price=rmb_price;
							 else
								 rmb_price=rmb_price.setScale(0, BigDecimal.ROUND_UP);
							 BigDecimal endorseprice= new BigDecimal(product.getEndorsementPrice()).setScale(2,BigDecimal.ROUND_CEILING);
							 if (product.getIslockprice()==1) {
								rmb_price = new BigDecimal(product.getRmbprice()).setScale(2, BigDecimal.ROUND_CEILING);
							 }
//							//新人价
							if(userService.checkFirstFlag(uid) && !"7".equals(typ) && !"8".equals(typ)){
								ProductPriceExt pe=productService.getProductPrice(product.getPid(),Constants.getSystemGroupOne("newman"),"rmbprice");
								if(pe!=null)
									rmb_price=new BigDecimal(pe.getSaleprice()).setScale(2, BigDecimal.ROUND_CEILING);
							}
							
							if("3".equals(typ)){
								Double deposit = product.getDeposit();
								if(Numbers.parseDouble(price, 0D)!=deposit.doubleValue()){
									result.status = 3;
									result.msg="您购买的部分商品价格发生了改变哦~请重新结算";
									return ok(Json.toJson(result));
								}
							}
							else if("6".equals(typ) || "66".equals(typ)){
								if("66".equals(typ)){
									endorsementId=dataNeed[3];	
								}
								Endorsement endorsetmp=endorsementService.getEndorseById(Numbers.parseLong(endorsementId, 0L));
								if(endorsetmp!=null && (endorsetmp.getGid().longValue()==4 || endorsetmp.getGid().longValue()==6))
									endorseprice=new BigDecimal(endorsetmp.getEndorsementPrice()).setScale(2,BigDecimal.ROUND_HALF_DOWN);

								//Double endorseprice=product.getEndorsementPrice();
								if(Numbers.parseDouble(price, 0D)!=endorseprice.doubleValue()){
									result.status = 6;
									result.msg="您购买的部分商品价格发生了改变哦~请重新结算";
									return ok(Json.toJson(result));
								}
								
								
								if(Numbers.parseLong(endorsementId, 0L)==0L)
								{
									result.status = 6;
									result.msg="该产品为非代言,不能代言价格购买";
									return ok(Json.toJson(result));
								}
								//判断是否本人购买，购买人UID与代言人UID相同不能购买；购买人收货电话号码也代言人号码相同不能购买
								Endorsement endorse=endorsementService.getEndorseById(Numbers.parseLong(endorsementId, 0L));
								if(endorse==null || endorse.getUserId().compareTo(Numbers.parseLong(uid, 0L))==0){
									result.status = 6;
									result.msg="代言人本人不能购买自己代言的商品";
									return ok(Json.toJson(result));
								}
								User euser=userService.getUserByUid(endorse.getUserId());
								if(euser!=null){
									Address adr=addressService.findByAddressId(Numbers.parseLong(addressid, 0L));
									if(adr!=null){
										if(adr.getPhone().equals(euser.getPhone())){
											result.status = 6;
											result.msg="代言人本人不能购买自己代言的商品";
											return ok(Json.toJson(result));
										}
									}
								}
							}
							else{
								if(Numbers.parseDouble(price, 0D)!=rmb_price.doubleValue()){
									result.status = 3;
									result.msg="您购买的部分商品价格发生了改变哦~请重新结算";
									return ok(Json.toJson(result));
								}
							}
							if("2".equals(typ)){
								lovelyflg = "1";
								p_distinct = product.getLovelydistinct();
							}
							p_deposit = p_goods_fee.add(new BigDecimal(product.getDeposit()).multiply(new BigDecimal(cnt)));
							if ("6".equals(typ) || "66".equals(typ)){
								p_goods_fee = p_goods_fee.add(endorseprice.multiply(new BigDecimal(cnt)));
							}else{
								p_goods_fee = p_goods_fee.add(rmb_price.multiply(new BigDecimal(cnt)));
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
			
			if(typ.equals("6") || typ.equals("66")){
				User ust=userService.getUserByUid(Numbers.parseLong(uid, 0L));
				String openid="";
				if(ust!=null)
					openid=StringUtils.isBlank(ust.getOpenId())?"":ust.getOpenId();
				p_cost_fee=new BigDecimal(productService.getEndorseRateFee(pidstr, openid)).setScale(2, BigDecimal.ROUND_HALF_DOWN);
			}
			else
				p_cost_fee=new BigDecimal(productService.getRateFee(pidstr, Numbers.parseLong(uid, 0L))).setScale(2, BigDecimal.ROUND_HALF_DOWN);

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
			p_total_fee = p_total_fee.add(p_foreignfee).add(p_goods_fee).add(p_cost_fee);
			
			if (pidstr.endsWith(",")){
				pidstr=pidstr.substring(0, pidstr.length()-1);
			}
			if(coupon!=null){
				p_coupon_price = Double.valueOf(couponService.getCouponPrice(couponId, pidstr));
				if(couponType==1 || couponType==5)
				{
					p_total_fee=p_total_fee.subtract(new BigDecimal(p_coupon_price));
				}
				if (couponType==2  || couponType==6)
				{
					if (p_total_fee.doubleValue()>=coupon.getTprice())
					{
						p_total_fee=p_total_fee.subtract(new BigDecimal(p_coupon_price));
					}
				}				
				if (p_total_fee.doubleValue()<=0)
				{
					p_total_fee=new BigDecimal(0);
				}
			}
			Double p_original_fee=p_total_fee.doubleValue();
			if("1".equals(lovelyflg) ){
				p_total_fee = p_total_fee.multiply(new BigDecimal(p_distinct)).multiply(new BigDecimal(0.1)).setScale(0, BigDecimal.ROUND_CEILING);
			}
			
			p_finalpay = p_total_fee.subtract(p_deposit).subtract(p_foreignfee);
			if(!"6".equals(typ)){
				if("7".equals(typ)){
					result = shoppingOrderService.newOrderTG(uid, addressid,
							p_domestic_fee, p_foreignfee, p_tariff_fee, p_cost_fee,
							p_total_fee, p_goods_fee, typ, lovely, p_couponUserId,
							p_coupon, p_coupon_price, p_distinct, p_deposit,
							p_finalpay, p_original_fee,usewallet,mcode,Long.valueOf(pid));
				}
				else if("8".equals(typ)){
					if(Numbers.parseLong(endorsementId, 0L)!=0L){
						result = shoppingOrderService.newOrder(uid, addressid,
								p_domestic_fee, p_foreignfee, p_tariff_fee, p_cost_fee,
								p_total_fee, p_goods_fee, typ, lovely, p_couponUserId,
								p_coupon, p_coupon_price, p_distinct, p_deposit,
								p_finalpay, p_original_fee,Numbers.parseLong(endorsementId, 0L),wx,shareType,usewallet);
					}
				}
				else{
					result = shoppingOrderService.newOrder(uid, addressid,
							p_domestic_fee, p_foreignfee, p_tariff_fee, p_cost_fee,
							p_total_fee, p_goods_fee, typ, lovely, p_couponUserId,
							p_coupon, p_coupon_price, p_distinct, p_deposit,
							p_finalpay, p_original_fee,usewallet,devid,ip,pidstr);
				}
			}
			else
			{				
				if(Numbers.parseLong(endorsementId, 0L)!=0L){
					result = shoppingOrderService.newOrder(uid, addressid,
							p_domestic_fee, p_foreignfee, p_tariff_fee, p_cost_fee,
							p_total_fee, p_goods_fee, typ, lovely, p_couponUserId,
							p_coupon, p_coupon_price, p_distinct, p_deposit,
							p_finalpay, p_original_fee,Numbers.parseLong(endorsementId, 0L),wx,shareType,usewallet);
				}
				
			}
			if(result.orderId!=null && result.orderId>0){
				if("8".equals(typ)){
					//变更代言编号进订单表
					shoppingOrderService.editorderEid(result.orderId, Numbers.parseLong(endorsementId, 0L));
				}
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
						String daiyanid=endorsementId;
						if("66".equals(typ))
							daiyanid=dataNeed[3];
						if(StringUtils.isBlank(daiyanid))
							daiyanid="0";
						
						shoppingOrderService.addProductToOrder(result.orderId,pid,price,cnt,daiyanid);
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
			}
		return ok(Json.toJson(result));
	}
	
	
	//订单生成接口（去支付）(POST方式) shoppingOrder_new.php
	public Result shoppingOrder_add(){
		response().setContentType("application/json;charset=utf-8");
		String uid = AjaxHellper.getHttpParamOfFormUrlEncoded(request(), "uid");
		String datastr = AjaxHellper.getHttpParamOfFormUrlEncoded(request(), "datastr");
		String devid = AjaxHellper.getHttpParamOfFormUrlEncoded(request(),"devid");
		String addressid = AjaxHellper.getHttpParamOfFormUrlEncoded(request(), "addressid");
		String typ = AjaxHellper.getHttpParamOfFormUrlEncoded(request(), "typ")==null?"1":AjaxHellper.getHttpParamOfFormUrlEncoded(request(), "typ");
		String lovely = AjaxHellper.getHttpParamOfFormUrlEncoded(request(), "lovely")==null?"1":AjaxHellper.getHttpParamOfFormUrlEncoded(request(), "lovely");
		String couponId = AjaxHellper.getHttpParamOfFormUrlEncoded(request(), "couponId")==null?"0":AjaxHellper.getHttpParamOfFormUrlEncoded(request(), "couponId");
		String endorsementId=AjaxHellper.getHttpParamOfFormUrlEncoded(request(), "eid");
		String usewallet=AjaxHellper.getHttpParamOfFormUrlEncoded(request(), "usewallet")==null?"0":AjaxHellper.getHttpParamOfFormUrlEncoded(request(), "usewallet");
		String wx=AjaxHellper.getHttpParamOfFormUrlEncoded(request(), "wx");
		String ip = StringUtil.getIpAddr(request());
		String appversion=AjaxHellper.getHttpParamOfFormUrlEncoded(request(), "appversion");
		String wdhjy = AjaxHellper.getHttpParamOfFormUrlEncoded(request(),"wdhjy");
		ProductNewVO result = new ProductNewVO();
		if (!StringUtil.checkMd5(devid, wdhjy,appversion))
		{
			result.status=0;
			return ok(Json.toJson(result));
		}
		
		if(StringUtils.isBlank(wx))
			wx="0";
		String shareType=AjaxHellper.getHttpParamOfFormUrlEncoded(request(), "shareType");
		if(StringUtils.isBlank(shareType))
			wx="0";
		
		
		if(StringUtils.isBlank(uid)||StringUtils.isBlank(datastr)){
			result.status=0;
			return ok(Json.toJson(result));
		}
		List<Object[]> deviceUsers = userService.getDeviceUser(uid);
		User user=null;
		if(typ.equals("6"))
			user=userService.getUserByUid(Numbers.parseLong(uid, 0L));
		
		if((deviceUsers!=null && deviceUsers.size()>0) || (typ.equals("6") && user!=null)){
			
			Coupon coupon = null;
			String lovelyflg="0";
			Double p_distinct=10D;
			
			int p_couponUserId=0;
			String p_coupon="";
			Double p_coupon_price=0D;
			int couponType =0;
			
			/********typ 1:普通订单 2：商品撒娇订单 3：预售订单 5：全局撒娇订单  6:代言订单***********/
			if("5".equals(typ)){
				OrderLoveLy orderLovely = shoppingCartService.getOrderLovely();
				lovelyflg = String.valueOf(orderLovely.getLovely());
				p_distinct = orderLovely.getLovelydistinct();
			}
			
			if(!"0".equals(couponId)){	
				List<Coupon> couponList = couponService.getCouponListByUseId(couponId, "0", uid);
				if (couponList==null || couponList.size()<=0)
				{
					result.status = 5;
					result.msg="该优惠券已过期，或已使用";
					result.orderCode= "";
					return ok(Json.toJson(result));
				}else{
					coupon = couponList.get(0);
					couponType=coupon.getTyp();
					p_coupon_price = coupon.getCouponprice();
					p_couponUserId =Numbers.parseInt(couponId, 0);
					p_coupon = coupon.getTitle();
					if(couponType==3||couponType==4){
						datastr=datastr+","+coupon.getPid()+"_a_1";
					}
				}
			}
			
			BigDecimal p_domestic_fee = new BigDecimal(0);
			BigDecimal p_foreignfee = new BigDecimal(0);
			BigDecimal p_tariff_fee = new BigDecimal(0);
			BigDecimal p_cost_fee  = new BigDecimal(0);
			BigDecimal p_total_fee  = new BigDecimal(0);
			BigDecimal p_goods_fee  = new BigDecimal(0);
			BigDecimal p_deposit  = new BigDecimal(0);
			BigDecimal p_finalpay  = new BigDecimal(0);
			String pidstr="";
			
			
			String data[] = datastr.split(",");
			Map<Integer, List<Product>> productMap = new HashMap<Integer, List<Product>>(); 
			for(String dataTemp:data){
				String dataNeed[] = dataTemp.split("_");
				if(dataNeed.length>=3){
					String pid=dataNeed[0];
					pidstr=pidstr+pid+",";
					Product product = productService.getProductById(Numbers.parseLong(pid, 0L));
					if(product!=null){
						String price = dataNeed[1];
						String cnt=dataNeed[2];
						if("a".equals(price)){
							product.setRmbprice(0.0); ;
							product.setCounts(1);
							product.setWeight(0.0);
						}else{
							product.setCounts(Numbers.parseInt(cnt, 0));
							Currency currency = productService.queryCurrencyById(product.getCurrency());
							 BigDecimal rate = new BigDecimal(currency.getRate()/100).setScale(4,  BigDecimal.ROUND_CEILING) ;
							 BigDecimal	priceDec=	new BigDecimal(product.getPrice()/100).setScale(2,  BigDecimal.ROUND_CEILING);   
							 BigDecimal rmb_price= rate.multiply(priceDec).setScale(0,BigDecimal.ROUND_CEILING);
							 BigDecimal endorseprice= new BigDecimal(product.getEndorsementPrice()).setScale(0,BigDecimal.ROUND_CEILING);
							 if (product.getIslockprice()==1) {
								rmb_price = new BigDecimal(product.getRmbprice()).setScale(0, BigDecimal.ROUND_CEILING);
							 }
							if("3".equals(typ)){
								Double deposit = product.getDeposit();
								if(Numbers.parseDouble(price, 0D)!=deposit.doubleValue()){
									result.status = 3;
									result.msg="您购买的部分商品价格发生了改变哦~请重新结算";
									return ok(Json.toJson(result));
								}
							}
							else if("5".equals(typ)){
								if(userService.checkFirstFlag(uid) && product.getNewMantype().equals("1")){
									result.status = 99;
									result.msg="您享受的新人价格不能撒娇支付";
									return ok(Json.toJson(result));
								}
							}
							else if("6".equals(typ) || "66".equals(typ)){
								//Double endorseprice=product.getEndorsementPrice();
								if(Numbers.parseDouble(price, 0D)!=endorseprice.doubleValue()){
									result.status = 6;
									result.msg="您购买的部分商品价格发生了改变哦~请重新结算";
									return ok(Json.toJson(result));
								}
								if("66".equals(typ))
									endorsementId=dataNeed[3];
								
								if(Numbers.parseLong(endorsementId, 0L)==0L)
								{
									result.status = 6;
									result.msg="该产品为非代言,不能代言价格购买";
									return ok(Json.toJson(result));
								}
							}
							else{
								if(Numbers.parseDouble(price, 0D)!=rmb_price.doubleValue()){
									result.status = 3;
									result.msg="您购买的部分商品价格发生了改变哦~请重新结算";
									return ok(Json.toJson(result));
								}
							}
							if("2".equals(typ)){
								lovelyflg = "1";
								p_distinct = product.getLovelydistinct();
							}
							p_deposit = p_goods_fee.add(new BigDecimal(product.getDeposit()).multiply(new BigDecimal(cnt)));
							if ("6".equals(typ)){
								p_goods_fee = p_goods_fee.add(endorseprice.multiply(new BigDecimal(cnt)));
							}else{
								p_goods_fee = p_goods_fee.add(rmb_price.multiply(new BigDecimal(cnt)));
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
			
			if(typ.equals("6") || typ.equals("66")){
				User ust=userService.getUserByUid(Numbers.parseLong(uid, 0L));
				String openid="";
				if(ust!=null)
					openid=StringUtils.isBlank(ust.getOpenId())?"":ust.getOpenId();
				
				p_cost_fee=new BigDecimal(productService.getEndorseRateFee(pidstr, openid)).setScale(2, BigDecimal.ROUND_HALF_DOWN);
			}
			else
				p_cost_fee=new BigDecimal(productService.getRateFee(pidstr, Numbers.parseLong(uid, 0L))).setScale(2, BigDecimal.ROUND_HALF_DOWN);
			
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
			p_total_fee = p_total_fee.add(p_foreignfee).add(p_goods_fee).add(p_cost_fee);
			
			if(coupon!=null){
				if(couponType==1)
				{
					p_total_fee=p_total_fee.subtract(new BigDecimal(coupon.getCouponprice()));
				}
				if (couponType==2)
				{
					if (p_total_fee.doubleValue()>=coupon.getCouponprice())
					{
						p_total_fee=p_total_fee.subtract(new BigDecimal(coupon.getCouponprice()));
					}
				}
				
				if (p_total_fee.doubleValue()<=0)
				{
					p_total_fee=new BigDecimal(0);
				}
			}
			Double p_original_fee=p_total_fee.doubleValue();
			if("1".equals(lovelyflg) ){
				p_total_fee = p_total_fee.multiply(new BigDecimal(p_distinct)).multiply(new BigDecimal(0.1)).setScale(0, BigDecimal.ROUND_CEILING);
			}
			
			if (pidstr.endsWith(",")){
				pidstr=pidstr.substring(0, pidstr.length()-1);
			}
			
			
			p_finalpay = p_total_fee.subtract(p_deposit).subtract(p_foreignfee);
			if(!"6".equals(typ)){
				result = shoppingOrderService.newOrder(uid, addressid,
						p_domestic_fee, p_foreignfee, p_tariff_fee, p_cost_fee,
						p_total_fee, p_goods_fee, typ, lovely, p_couponUserId,
						p_coupon, p_coupon_price, p_distinct, p_deposit,
						p_finalpay, p_original_fee,usewallet,devid,ip,pidstr);
			}
			else
			{
				if(Numbers.parseLong(endorsementId, 0L)!=0L){
					result = shoppingOrderService.newOrder(uid, addressid,
							p_domestic_fee, p_foreignfee, p_tariff_fee, p_cost_fee,
							p_total_fee, p_goods_fee, typ, lovely, p_couponUserId,
							p_coupon, p_coupon_price, p_distinct, p_deposit,
							p_finalpay, p_original_fee,Numbers.parseLong(endorsementId, 0L),wx,shareType,usewallet);
				}
				
			}
			if(result.orderId!=null){
				for(String dataTemp:data){
					String dataNeed[] = dataTemp.split("_");
					if(dataNeed.length>=3){
						String pid=dataNeed[0];
						String price = dataNeed[1];
						String cnt=dataNeed[2];	
						String daiyanid="0";
						if("66".equals(typ))
							daiyanid=dataNeed[3];
						if("a".equals(price)){
							price="0";
							cnt="1";
						}
						shoppingOrderService.addProductToOrder(result.orderId,pid,price,cnt,daiyanid);
					}
				}
			}
		}
		
		if(result.totalfee<=0){
			result.status=2;
			String method = "80";
			if("1".equals(usewallet)){
				method = "90";
			}
			String state = "20";
			productService.setPayStatus(result.orderCode,method,state,result.totalfee,"");
		}
		return ok(Json.toJson(result));
	}
	
	//(四十七)	检查是否需要身份信息接口(GET方式) checkopencardId.php
	public Result  checkopencardId(){
		response().setContentType("application/json;charset=utf-8");
		String uid = AjaxHellper.getHttpParam(request(), "uid");
		String addressId = AjaxHellper.getHttpParam(request(), "addressId")==null?"":AjaxHellper.getHttpParam(request(), "addressId");
		String orderCode = AjaxHellper.getHttpParam(request(), "orderCode")==null?"":AjaxHellper.getHttpParam(request(), "orderCode");
		String opencardId = userService.getOpencardId();
		String devid = AjaxHellper.getHttpParam(request(),"devid");
		String appversion=AjaxHellper.getHttpParam(request(), "appversion");
		String wdhjy = AjaxHellper.getHttpParam(request(),"wdhjy");
		ObjectNode result = Json.newObject();
		if (!StringUtil.checkMd5(devid, wdhjy,appversion))
		{
			result.put("status", 0);
			return ok(Json.toJson(result));
		}
		
		
		int status=0;
		if("0".equals(opencardId)){
			result.put("status", 1);
			return ok(Json.toJson(result));
		}else{
			if(!StringUtils.isBlank(orderCode)){
				ShoppingOrder order = shoppingOrderService.getShoppingOrderByOrderCode(orderCode);
				if(order!=null){
					int length = order.getCardId()==null?0:order.getCardId().length();
					if(length>15){
						status=1;
					}else{
						status=2;
					}
					result.put("status", status);
				}
				return ok(Json.toJson(result));
			}
			if(!StringUtils.isBlank(addressId)){
				Address address = addressService.findByAddressId(Numbers.parseLong(addressId, 0L));
				if(address!=null){
					int length = address.getCardId()==null?0:address.getCardId().length();
					if(length>15){
						status=1;
					}else{
						status=2;
					}
					result.put("status", status);
				}
				return ok(Json.toJson(result));
			}
			return ok(Json.toJson(result));
		}
	}
	//(四十八)	检查是否需要身份信息接口(GET方式) checkopencardIdByPid.php
	public Result  checkopencardIdByPid(){
		response().setContentType("application/json;charset=utf-8");
		String uid = AjaxHellper.getHttpParam(request(), "uid");
		String pids = AjaxHellper.getHttpParam(request(), "pids")==null?"":AjaxHellper.getHttpParam(request(), "pids");
		String addressId = AjaxHellper.getHttpParam(request(), "addressId")==null?"":AjaxHellper.getHttpParam(request(), "addressId");
		String devid = AjaxHellper.getHttpParam(request(),"devid");
		String appversion=AjaxHellper.getHttpParam(request(), "appversion");
		String wdhjy = AjaxHellper.getHttpParam(request(),"wdhjy");
		ObjectNode result = Json.newObject();
		if (!StringUtil.checkMd5(devid, wdhjy,appversion))
		{
			result.put("status", 0);
			return ok(Json.toJson(result));
		}
		
		int opencardId = userService.getOpencardIdByPids(pids);
		int opencardIdimg = userService.getOpencardIdImgByPids(pids);
		
		int status=0;
		String toast="";
		if (opencardIdimg>0){
			if(!StringUtils.isBlank(addressId)){
				Address address = addressService.findByAddressId(Numbers.parseLong(addressId, 0L));
				if(address!=null){
					if(address.getCardImg()==null ||address.getCardImg().intValue()==0){
						status=3;
						toast="您购买的商品中有海外直邮商品，海关需要核查身份证照片信息，请您完善。";
					}else{
						status=1;
						toast="";
					}
					result.put("status", status);
					result.put("toast",toast);
				}
			}
			return ok(Json.toJson(result));
		}
		
		if(opencardId>0){
			if(!StringUtils.isBlank(addressId)){
				Address address = addressService.findByAddressId(Numbers.parseLong(addressId, 0L));
				if(!String.valueOf(address.getuId()).equals(uid)){
					status = 2;
					toast = "当前用户不存在此收货地址，请查证。";
					result.put("status", status);
					result.put("toast", "");
					return ok(Json.toJson(result));
				}
				if(address!=null){
					int length = address.getCardId() == null ? 0 : address.getCardId().length();
					if (length > 15) {
						int flag = certificationService.checkNameWithCard(uid, address.getName(), address.getCardId());
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
			}else{
				status = 2;
				toast = "您购买的商品中有海外直邮商品，由海关统一清关入境，需要您完善真实的收货人身份信息。";
			}
			result.put("status", status);
			result.put("toast", toast);
			if(status==1){//如果验证通过，则需要对购买次数等信息进行检验
				result = shoppingOrderService.checkOrderWithAddress(pids,Numbers.parseLong(addressId, 0L));
			}
			return ok(Json.toJson(result));
		}else{

			result.put("status", 1);
			result.put("toast","");
			return ok(Json.toJson(result));
		}
	}
	
	//下单及支付参数一次性接口
	public Result createAndPayOrder(){
		
		response().setContentType("application/json;charset=utf-8");
		String uid = Form.form().bindFromRequest().get("uid");
		String devid = Form.form().bindFromRequest().get("devid");
		// String pids = Form.form().bindFromRequest().get("pids")==null?"":Form.form().bindFromRequest().get("pids");
		String addressId = Form.form().bindFromRequest().get("addressid")==null?"":Form.form().bindFromRequest().get("addressid");
		String usewallet=Form.form().bindFromRequest().get("usewallet");
		String typ=Form.form().bindFromRequest().get("typ");
		String datastr=Form.form().bindFromRequest().get("datastr");
		
		String thirdPartyPayMethod=Form.form().bindFromRequest().get("thirdPartyPayMethod");
		
		String vstr=Form.form().bindFromRequest().get("vstr");
		String couponId=Form.form().bindFromRequest().get("couponId");
		String ip = StringUtil.getIpAddr(request());
		
		if(StringUtils.isBlank(couponId))
			couponId="0";
		if(StringUtils.isBlank(usewallet))
			usewallet="0";
		if(StringUtils.isBlank(thirdPartyPayMethod))
			thirdPartyPayMethod="2";
		
		String endorsementId=Form.form().bindFromRequest().get("daiyanId");
		String lovely=Form.form().bindFromRequest().get("lovely");
		
		String pids = "";
		int status=0;
		String toast="";
		ObjectNode result = Json.newObject();
		
		if(StringUtils.isBlank(uid)||StringUtils.isBlank(datastr)){
			result=returnresult("4","生成订单失败，参数错误","","","");
			status=2;
			return ok(result);
		}
		
		String vmd5str=StringUtil.getMD5("DX2014"+datastr);
		if(!vmd5str.equals(vstr)){
			status=2;
			result=returnresult("4","生成订单失败，加密参数错误","","","");
			return ok(result);
		}

		/***********生成订单**************************************/	
			ProductNewVO resultorder = new ProductNewVO();
			Coupon coupon = null;
			String lovelyflg="0";
			Double p_distinct=10D;
			
			int p_couponUserId=0;
			String p_coupon="";
			Double p_coupon_price=0D;
			int couponType =0;
			
			/********typ 1:普通订单 2：商品撒娇订单 3：预售订单 5：全局撒娇订单  6:代言订单***********/
			if("5".equals(typ)){
				OrderLoveLy orderLovely = shoppingCartService.getOrderLovely();
				lovelyflg = String.valueOf(orderLovely.getLovely());
				p_distinct = orderLovely.getLovelydistinct();
			}
			
			if(!"0".equals(couponId)){	
				List<Coupon> couponList = couponService.getCouponListByUseId(couponId, "0", uid);
				if (couponList==null || couponList.size()<=0)
				{
					status = 4;
					toast="该优惠券已过期，或已使用";
					result=returnresult(String.valueOf(status),toast,"","","");
					return ok(result);
				}else{
					coupon = couponList.get(0);
					couponType=coupon.getTyp();
					p_coupon_price = coupon.getCouponprice();
					p_couponUserId =Numbers.parseInt(couponId, 0);
					p_coupon = coupon.getTitle();
					if(couponType==3||couponType==4){
						datastr=datastr+","+coupon.getPid()+"_a_1";
					}
				}
			}
			
			BigDecimal p_domestic_fee = new BigDecimal(0);
			BigDecimal p_foreignfee = new BigDecimal(0);
			BigDecimal p_tariff_fee = new BigDecimal(0);
			BigDecimal p_cost_fee  = new BigDecimal(0);
			BigDecimal p_total_fee  = new BigDecimal(0);
			BigDecimal p_goods_fee  = new BigDecimal(0);
			BigDecimal p_deposit  = new BigDecimal(0);
			BigDecimal p_finalpay  = new BigDecimal(0);
			
			String data[] = datastr.split(",");
			String pid="0";
			Map<Integer, List<Product>> productMap = new HashMap<Integer, List<Product>>(); 
			for(String dataTemp:data){
				String dataNeed[] = dataTemp.split("_");
				if(dataNeed.length>=3){
					pid=dataNeed[0];
					
					pids = pids+pid+",";
					Product product = productService.getProductById(Numbers.parseLong(pid, 0L));
					if(product!=null){
						String price = dataNeed[1];
						String cnt=dataNeed[2];
						if("a".equals(price)){
							product.setRmbprice(0.0); ;
							product.setCounts(1);
							product.setWeight(0.0);
						}else{
							product.setCounts(Numbers.parseInt(cnt, 0));
							Currency currency = productService.queryCurrencyById(product.getCurrency());
							 BigDecimal rate = new BigDecimal(currency.getRate()/100).setScale(4,  BigDecimal.ROUND_CEILING) ;
							 BigDecimal	priceDec=	new BigDecimal(product.getPrice()/100).setScale(2,  BigDecimal.ROUND_CEILING);   
							 BigDecimal rmb_price= rate.multiply(priceDec).setScale(0,BigDecimal.ROUND_CEILING);
							 BigDecimal endorseprice= new BigDecimal(product.getEndorsementPrice()).setScale(0,BigDecimal.ROUND_CEILING);
							 if (product.getIslockprice()==1) {
								rmb_price = new BigDecimal(product.getRmbprice()).setScale(0, BigDecimal.ROUND_CEILING);
							 }
							if("3".equals(typ)){
								Double deposit = product.getDeposit();
								if(Numbers.parseDouble(price, 0D)!=deposit.doubleValue()){
									status=4;
									toast="您购买的部分商品价格发生了改变哦~请重新结算";
									result=returnresult(String.valueOf(status),toast,"","","");
									return ok(result);
								}
							}							
							else{
								if(Numbers.parseDouble(price, 0D)!=rmb_price.doubleValue()){
									status = 4;
									toast="您购买的部分商品价格发生了改变哦~请重新结算";
									result=returnresult(String.valueOf(status),toast,"","","");
									return ok(result);
								}
							}
							if("2".equals(typ)){
								lovelyflg = "1";
								p_distinct = product.getLovelydistinct();
							}
							p_deposit = p_goods_fee.add(new BigDecimal(product.getDeposit()).multiply(new BigDecimal(cnt)));
							p_goods_fee = p_goods_fee.add(rmb_price.multiply(new BigDecimal(cnt)));
						}
						
						List<Product> productList = productMap.get(product.getFromsite());
						if(productList==null)
							productList = new ArrayList<Product>();
						productList.add(product);
						productMap.put(product.getFromsite(), productList);
					}
				}
			}
			
			if (pids.endsWith(",")){
				pids = pids.substring(0,pids.length()-1);
			}
			//检验身份证
			int opencardId = userService.getOpencardIdByPids(pids);
			int opencardIdimg = userService.getOpencardIdImgByPids(pids);
			if (opencardIdimg>0){
				if(!StringUtils.isBlank(addressId)){
					Address address = addressService.findByAddressId(Numbers.parseLong(addressId, 0L));
					if(address!=null){
						if(address.getCardImg()==0){
							status=3;
							toast="您购买的商品中有海外直邮商品，海关需要核查身份证照片信息，请您完善。";
						}else{
							status=1;
							toast="";
						}
					}
				}
			}
			if(opencardId>0){
				if(!StringUtils.isBlank(addressId)){
					Address address = addressService.findByAddressId(Numbers.parseLong(addressId, 0L));
					if(address!=null){
						int length = address.getCardId()==null?0:address.getCardId().length();
						if(length>15){
							status=1;
						}else{
							status=2;
						}
						toast="您购买的商品中有海外直邮商品，由海关统一清关入境，需要您完善真实的收货人身份信息。";
					}
				}
			}else{
				status=1;
				//toast="您购买的商品中有海外直邮商品，由海关统一清关入境，需要您完善真实的收货人身份信息。";
			}
			if(status!=1){
				result=returnresult(String.valueOf(status),toast,"","","");
				return ok(result);
			}
			/***********检查身份证结束///////////////*******************/
			
			
			
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
			
			if(coupon!=null){
				if(couponType==1)
				{
					p_total_fee=p_total_fee.subtract(new BigDecimal(coupon.getCouponprice()));
				}
				if (couponType==2)
				{
					if (p_total_fee.doubleValue()>=coupon.getCouponprice())
					{
						p_total_fee=p_total_fee.subtract(new BigDecimal(coupon.getCouponprice()));
					}
				}
				
				if (p_total_fee.doubleValue()<=0)
				{
					p_total_fee=new BigDecimal(0);
				}
			}
			Double p_original_fee=p_total_fee.doubleValue();
			if("1".equals(lovelyflg) ){
				p_total_fee = p_total_fee.multiply(new BigDecimal(p_distinct)).multiply(new BigDecimal(0.1)).setScale(0, BigDecimal.ROUND_CEILING);
			}
			
			p_finalpay = p_total_fee.subtract(p_deposit).subtract(p_foreignfee);
			//代言购买
			if(Numbers.parseLong(endorsementId, 0L)!=0L){
				resultorder = shoppingOrderService.newOrderEndorse(uid, addressId,
						p_domestic_fee, p_foreignfee, p_tariff_fee, p_cost_fee,
						p_total_fee, p_goods_fee, typ, lovely, p_couponUserId,
						p_coupon, p_coupon_price, p_distinct, p_deposit,
						p_finalpay, p_original_fee,usewallet,endorsementId);
			}else{
				resultorder = shoppingOrderService.newOrder(uid, addressId,
						p_domestic_fee, p_foreignfee, p_tariff_fee, p_cost_fee,
						p_total_fee, p_goods_fee, typ, lovely, p_couponUserId,
						p_coupon, p_coupon_price, p_distinct, p_deposit,
						p_finalpay, p_original_fee,usewallet,devid,ip,pids);
			}
			if(resultorder.orderId!=null && resultorder.orderId>0){
				for(String dataTemp:data){
					String dataNeed[] = dataTemp.split("_");
					if(dataNeed.length>=3){
						pid=dataNeed[0];
						String price = dataNeed[1];
						String cnt=dataNeed[2];	
						String daiyanid="0";
						if("66".equals(typ))
							daiyanid=dataNeed[3];
						
						if("a".equals(price)){
							price="0";
							cnt="1";
						}
						shoppingOrderService.addProductToOrder(resultorder.orderId,pid,price,cnt,daiyanid);
					}
				}
			}
			else
			{
				result=returnresult("3","创建订单失败","","","");				
				return ok(result);
			}
		
		/***********获取微信参数************************************/
			if(thirdPartyPayMethod.equals("1")){
				if(resultorder.totalfee.compareTo(Double.valueOf("0"))>0){
					//String ip=request().remoteAddress();
					JsonNode reslutwx = WXPayService.getInstance().getSignAndPrepayID(resultorder.orderCode,  resultorder.totalfee.longValue(),ip);
					if(reslutwx==null){
						result=returnresult("2","支付订单获取参数失败","","","");						
						return ok(result);
					}else{
						String nostr=reslutwx.get("noncestr").asText();
						String partnerid=reslutwx.get("partnerid").asText();
						String prepayid=reslutwx.get("prepayid").asText();
						String timstr=reslutwx.get("timestamp").asText();
						String packg=reslutwx.get("package").asText();
						String sign=reslutwx.get("sign").asText();
						String wxpaystr="noncestr="+nostr+"&partnerid="+partnerid+"&prepayid="+prepayid+"&timestamp="+timstr+"&sign="+sign+"&package="+packg;
						wxpaystr=Base64.encodeBase64String(wxpaystr.getBytes());
						result=returnresult("1","",resultorder.orderCode,"1",wxpaystr);
						
						return ok(result);
					}
				}
				else{
					result=returnresult("1","",resultorder.orderCode,"0","");					
					return ok(result);
				}
			}
		/***********获取支付宝快捷支付参数**********************************/
		if(thirdPartyPayMethod.equals("2")){
			if(resultorder.totalfee.compareTo(Double.valueOf(0))>0){
				String alipaysign = AliPayService.getInstance().alipay_Wap_sign(resultorder.orderCode,resultorder.totalfee);
				result=returnresult("1","",resultorder.orderCode,"1",alipaysign);
				return ok(result);
			}
			else
			{
				result=returnresult("1","",resultorder.orderCode,"0","");
				return ok(result);
			}
		}
		
		if(thirdPartyPayMethod.equals("3")){
			if(resultorder.totalfee.compareTo(Double.valueOf(0))>0){
				result=returnresult("1","",resultorder.orderCode,"1","");
				return ok(result);
			}
			else
			{
				result=returnresult("1","",resultorder.orderCode,"0","");
				return ok(result);
			}
		}
		return ok(result);
	}
	
	//二次订单支付与预付款支付合并
	public Result payOrder(){
		response().setContentType("application/json;charset=utf-8");
		String orderCode=Form.form().bindFromRequest().get("orderCodePay");
		String usewallet=Form.form().bindFromRequest().get("usewallet");
		String thirdPartyPayMethod=Form.form().bindFromRequest().get("thirdPartyPayMethod");
		String vstr=Form.form().bindFromRequest().get("vstr");
		
		ObjectNode result=Json.newObject();
		ProductNewVO resultpay = new ProductNewVO();
		if(StringUtils.isBlank(orderCode) || StringUtils.isBlank(usewallet)){
			result=this.returnresult("2", "支付失败，参数错误", "","","");
			return ok(result);
		}
		

		ShoppingOrder order=shoppingOrderService.getShoppingOrderByOrderCode(orderCode);
		if(order==null){
			result=this.returnresult("2", "支付失败，订单不存在", "","","");
			return ok(result);
		}
		
		String datastr="";
		StringBuilder dstr=new StringBuilder();
		List<OrderProduct> oplist=shoppingOrderService.getOrderproList(orderCode);
		if(oplist==null || oplist.isEmpty()){
			result=this.returnresult("2", "支付失败，订单不存在", "","","");
			return ok(result);
		}
		for(OrderProduct p:oplist){
			dstr.append(p.getpId()+"_"+p.getPrice()+"_"+p.getCounts()+",");
		}
		datastr=dstr.toString();
		datastr=datastr.substring(0,datastr.length()-1);
		
		String vmd5str=StringUtil.getMD5("DX2014"+datastr);
		if(!vmd5str.equals(vstr)){
			result=returnresult("2","生成订单失败，加密参数错误","","","");
			return ok(result);
		}
		resultpay = shoppingOrderService.OrderPay(orderCode,usewallet);
		if(resultpay==null){
			result=this.returnresult("2", "支付失败", resultpay.orderCode, "", "");
			return ok(result);
		}else
		{
			if(!StringUtils.isBlank(resultpay.orderCode)){
				//获取支付参数
				/***********获取微信参数************************************/
				if(thirdPartyPayMethod.equals("1")){
					if(resultpay.totalfee.compareTo(Double.valueOf("0"))>0){
						String ip=request().remoteAddress();
						JsonNode reslutwx = WXPayService.getInstance().getSignAndPrepayID(resultpay.orderCode,  resultpay.totalfee.longValue(),ip);
						if(reslutwx==null){
							result=returnresult("2","支付订单获取参数失败","","","");						
							return ok(result);
						}else{
							String nostr=reslutwx.get("noncestr").asText();
							String partnerid=reslutwx.get("partnerid").asText();
							String prepayid=reslutwx.get("prepayid").asText();
							String timstr=reslutwx.get("timestamp").asText();
							String packg=reslutwx.get("package").asText();
							String sign=reslutwx.get("sign").asText();
							String wxpaystr="noncestr="+nostr+"&partnerid="+partnerid+"&prepayid="+prepayid+"&timestamp="+timstr+"&sign="+sign+"&package="+packg;
							wxpaystr=Base64.encodeBase64String(wxpaystr.getBytes());
							result=returnresult("1","",resultpay.orderCode,"1",wxpaystr);
							
							return ok(result);
						}
					}
					else{
						result=returnresult("1","",resultpay.orderCode,"0","");					
						return ok(result);
					}
				}
			/***********获取支付宝快捷支付参数**********************************/
			if(thirdPartyPayMethod.equals("2")){
				if(resultpay.totalfee.compareTo(Double.valueOf(0))>0){
					String alipaysign = AliPayService.getInstance().alipay_Wap_sign(resultpay.orderCode,resultpay.totalfee);
					result=returnresult("1","",resultpay.orderCode,"1",alipaysign);
					return ok(result);
				}
				else
				{
					result=returnresult("1","",resultpay.orderCode,"0","");
					return ok(result);
				}
			}
			
			if(thirdPartyPayMethod.equals("3")){
				if(resultpay.totalfee.compareTo(Double.valueOf(0))>0){
					result=returnresult("1","",resultpay.orderCode,"1","");
					return ok(result);
				}
				else
				{
					result=returnresult("1","",resultpay.orderCode,"0","");
					return ok(result);
				}
			}
			}
			else
			{
				result=this.returnresult("2", "支付失败", "", "", "");
				return ok(result);
			}
		}
		return ok(result);
	}
	
	private ObjectNode returnresult (String status,String msg,String ordercode,String needpaymode,String sign){
		ObjectNode result=Json.newObject();
		result.put("status", status);
		result.put("msg", msg);
		ObjectNode datanode=Json.newObject();
		datanode.put("orderCode", ordercode);
		datanode.put("needPayMore", needpaymode);
		datanode.put("thirdPartyPaySign", sign);
		result.putPOJO("data", datanode);
		return result;
	}
}
