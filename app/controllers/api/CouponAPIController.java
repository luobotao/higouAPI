package controllers.api;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import models.Coupon;

import org.apache.commons.lang3.StringUtils;

import play.libs.Json;
import play.mvc.Result;
import services.api.CouponService;
import services.api.UserService;
import utils.AjaxHellper;
import utils.Constants;
import utils.Numbers;
import utils.StringUtil;
import vo.StatusErrMsgVO;
import vo.coupon.CouponQueryVO;
import vo.coupon.CouponResultVO;
import vo.coupon.PayEndResultVO;

/**
 * 
 * @author luobotao
 *
 */
@Named
@Singleton
public class CouponAPIController extends BaseApiController {

	private final CouponService couponService;
	private final UserService userService;
	@Inject
	public CouponAPIController(final CouponService couponService,final UserService userService){
		this.couponService = couponService;
		this.userService=userService;
	}
	
	
	public Result coupon_exchange(){
		response().setContentType("application/json;charset=utf-8");
		String uid = AjaxHellper.getHttpParam(request(), "uid");
		String marketCode = AjaxHellper.getHttpParam(request(), "marketCode");
		String osversion = AjaxHellper.getHttpParam(request(), "osversion");
		String model = AjaxHellper.getHttpParam(request(), "model");
		String deviceType = AjaxHellper.getHttpParam(request(), "deviceType");
		String idfa= AjaxHellper.getHttpParam(request(), "idfa");
		String couponcode = AjaxHellper.getHttpParam(request(), "couponcode");
		String devid = AjaxHellper.getHttpParam(request(), "devid");
		String wdhjy = AjaxHellper.getHttpParam(request(),"wdhjy");
		String appversion = AjaxHellper.getHttpParam(request(), "appversion");
		
		StatusErrMsgVO statusErrMsgVO = new StatusErrMsgVO();
		if (!StringUtil.checkMd5(devid, wdhjy,appversion))
		{
			statusErrMsgVO.setStatus("0");	
			return ok(Json.toJson(statusErrMsgVO));
		}
		if(StringUtils.isBlank(uid)||StringUtils.isBlank(couponcode)){
			statusErrMsgVO.setStatus("0");
		}else{
			int states = couponService.checkCode(uid,couponcode);
			String status="";
			String errmsg="";
			switch (states)
			{
				case 0:
				status="1";
				errmsg="校验成功";
				break;
				case 1:
				status="0";
				errmsg="该兑换码已被兑换";
				break;
				case 2:
				status="0";
				errmsg="兑换码已过期";
				break;
				case -1:
				status="0";
				errmsg="兑换码错误";
				break;
				default:
				status="0";
				errmsg="兑换码错误";
				break;
			}
			statusErrMsgVO.setStatus(status);
			statusErrMsgVO.setErrmsg(errmsg);
		}
		
		return ok(Json.toJson(statusErrMsgVO));
	}
	
	//(五十)	优惠券列表接口(GET方式) coupon_list.php
	public Result coupon_list(){
		response().setContentType("application/json;charset=utf-8");
		String uid = AjaxHellper.getHttpParam(request(), "uid");
		String page = AjaxHellper.getHttpParam(request(), "page")==null?"0":AjaxHellper.getHttpParam(request(), "page");
		String devid = AjaxHellper.getHttpParam(request(), "devid");
		String marketCode = AjaxHellper.getHttpParam(request(), "marketCode")==null?"":AjaxHellper.getHttpParam(request(), "marketCode");
		String appversion = AjaxHellper.getHttpParam(request(), "appversion");
		String osversion = AjaxHellper.getHttpParam(request(), "osversion");
		String model = AjaxHellper.getHttpParam(request(), "model");
		String deviceType = AjaxHellper.getHttpParam(request(), "deviceType");
		String idfa= AjaxHellper.getHttpParam(request(), "idfa");
		String price = AjaxHellper.getHttpParam(request(), "price")==null?"0":AjaxHellper.getHttpParam(request(), "price");
		String pidStr =AjaxHellper.getHttpParam(request(), "pidStr")==null?"":AjaxHellper.getHttpParam(request(), "pidStr");
		String wdhjy = AjaxHellper.getHttpParam(request(),"wdhjy");
		
		CouponResultVO result = new CouponResultVO();
		if (!StringUtil.checkMd5(devid, wdhjy,appversion))
		{
			result.setStatus("0");	
			return ok(Json.toJson(result));
		}
		
		CouponQueryVO couponQuery = new CouponQueryVO();
		couponQuery.uid=uid;
		couponQuery.price = price;
		
		
		couponService.bindcoupon(uid,marketCode);
		
//		Page<Coupon> couponPage = couponService.getCouponPage(couponQuery,Numbers.parseInt(page, 0));
		List<Coupon> couponList = couponService.getCouponListByUidAndPrice(uid,price,pidStr,page,Constants.PAGESIZE);
		//消红点
		userService.updateUserRedFlag(Numbers.parseLong(uid, 0L), "couponRedFlag", 0);
		if(couponList.size()<10){
			result.setEndflg("1");
		}else{
			result.setEndflg("0");
		}
		result.setStatus("1");
		result.setData(couponService.conponListCovertTocouponVOList(couponList));
		return ok(Json.toJson(result));
	}
	
	//(五十三)	支付完成后提示通知（新增）payEnd.php 
	public Result payEnd(){
		response().setContentType("application/json;charset=utf-8");
		String uid = AjaxHellper.getHttpParam(request(), "uid");
		String page = AjaxHellper.getHttpParam(request(), "page")==null?"0":AjaxHellper.getHttpParam(request(), "page");
		String devid = AjaxHellper.getHttpParam(request(), "devid");
		String marketCode = AjaxHellper.getHttpParam(request(), "marketCode")==null?"":AjaxHellper.getHttpParam(request(), "marketCode");
		String appversion = AjaxHellper.getHttpParam(request(), "appversion");
		String osversion = AjaxHellper.getHttpParam(request(), "osversion");
		String model = AjaxHellper.getHttpParam(request(), "model");
		String deviceType = AjaxHellper.getHttpParam(request(), "deviceType");
		String idfa= AjaxHellper.getHttpParam(request(), "idfa");
		String wdhjy = AjaxHellper.getHttpParam(request(),"wdhjy");
		String orderCode = AjaxHellper.getHttpParam(request(), "orderCode")==null?"":AjaxHellper.getHttpParam(request(), "orderCode");
		PayEndResultVO result = new PayEndResultVO();
		if (!StringUtil.checkMd5(devid, wdhjy,appversion))
		{
			result.setStatus("0");	
			return ok(Json.toJson(result));
		}
		result =couponService.getPayendInfo(uid, orderCode);
		List<Coupon> couponList = couponService.getCouponList(result.getCouponId()+"");
		Integer couponsSum=0;
		if (couponList.size()>0)
		{
			couponsSum=couponList.get(0).getCouponprice().intValue();
		}
		result.setCouponsSum(couponsSum);
		result.setCoupondata(couponService.conponListCovertTocouponVOList(couponList));
		
		
		return ok(Json.toJson(result));
	}


}
