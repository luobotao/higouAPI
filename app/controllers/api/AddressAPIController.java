package controllers.api;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import models.Address;

import org.apache.commons.lang3.StringUtils;

import play.libs.Json;
import play.mvc.Result;
import services.api.AddressService;
import services.api.CertificationService;
import services.api.UserService;
import utils.AjaxHellper;
import utils.Numbers;
import utils.StringUtil;
import vo.StatusOnlyVO;
import vo.address.AddressAddVO;
import vo.address.AddressListVO;

/**
 * 
 * @author luobotao
 *
 */
@Named
@Singleton
public class AddressAPIController extends BaseApiController {

	private final UserService userService;
	private final AddressService addressService;
	private final CertificationService certificationService;
	@Inject
	public AddressAPIController(final UserService userService,final AddressService addressService,final CertificationService certificationService) {
		this.userService = userService;
		this.addressService = addressService;
		this.certificationService = certificationService;
	}

	//(二十九)	收货地址列表接口(GET方式) address_list.php
	public Result address_list(){
		response().setContentType("application/json;charset=utf-8");
		String uid = AjaxHellper.getHttpParam(request(), "uid");
		String devid = AjaxHellper.getHttpParam(request(), "devid");
		String wdhjy = AjaxHellper.getHttpParam(request(),"wdhjy");
		String appversion = AjaxHellper.getHttpParam(request(),"appversion");
		AddressListVO result = new AddressListVO();
		if (!StringUtil.checkMd5(devid, wdhjy,appversion))
		{
			result.setStatus("0");
			return ok(Json.toJson(result));
		}
		
		List<Address> addressList = addressService.address_list(Numbers.parseLong(uid, 0L));
		List<Address> tlist=new ArrayList<Address>();
		
		String domains=StringUtil.getOSSUrl();
		if(addressList!=null && !addressList.isEmpty()){
			for(Address a:addressList){
				Address t=new Address();
				t=a;
				String imgpath=a.getImgpath();
				if(!StringUtils.isBlank(imgpath)){
					if(imgpath.indexOf(",")>1){
						String[] tmp=imgpath.split(",");
						
						t.setImgpath(domains+tmp[0]);
						t.setSecondimgpath(domains+tmp[1]);
					}
					else
						t.setImgpath(domains+t.getImgpath().replace(",", ""));
				}else{
					t.setImgpath("");
					t.setSecondimgpath("");
				}
				tlist.add(t);
			}
		}
		
		result.setStatus("1");
		result.setData(tlist);
		return ok(Json.toJson(result));
	}
	
	//(三十)	收货地址增加接口(POST方式) address_new.php
	public Result address_new(){
		response().setContentType("application/json;charset=utf-8");
		
		String uid = AjaxHellper.getHttpParamOfFormUrlEncoded(request(), "uid");
		String address = AjaxHellper.getHttpParamOfFormUrlEncoded(request(), "address");
		String checkAddress = AjaxHellper.getHttpParamOfFormUrlEncoded(request(), "checkAddress");
		String name = AjaxHellper.getHttpParamOfFormUrlEncoded(request(), "name");
		String phone = AjaxHellper.getHttpParamOfFormUrlEncoded(request(), "phone");
		String cardId = AjaxHellper.getHttpParamOfFormUrlEncoded(request(), "cardId")==null?"":AjaxHellper.getHttpParamOfFormUrlEncoded(request(), "cardId");
		String province = AjaxHellper.getHttpParamOfFormUrlEncoded(request(), "province");
		String postcode = AjaxHellper.getHttpParamOfFormUrlEncoded(request(), "postcode")==null?"":AjaxHellper.getHttpParamOfFormUrlEncoded(request(), "postcode");
		String areaCode = AjaxHellper.getHttpParamOfFormUrlEncoded(request(), "areaCode")==null?"":AjaxHellper.getHttpParamOfFormUrlEncoded(request(), "areaCode");
		String devid = AjaxHellper.getHttpParamOfFormUrlEncoded(request(), "devid");
		String wdhjy = AjaxHellper.getHttpParamOfFormUrlEncoded(request(),"wdhjy");
		String appversion = AjaxHellper.getHttpParamOfFormUrlEncoded(request(),"appversion");
		
		AddressAddVO result = new AddressAddVO();
		if (!StringUtil.checkMd5(devid, wdhjy,appversion))
		{
			result.setStatus("0");
			result.setAddressId("0");
			result.setToast("验证失败");
			return ok(Json.toJson(result));
		}
		
		if(StringUtils.isBlank(uid)||StringUtils.isBlank(address)||StringUtils.isBlank(name)||StringUtils.isBlank(phone)){
			result.setStatus("0");
			result.setAddressId("0");
			result.setToast("缺少必填项");
			return ok(Json.toJson(result));
		}
		int status = 1;
		String toast = "";
		if("1".equals(checkAddress)){
			int flag = certificationService.checkNameWithCard(uid, name, cardId);
			if (flag == 0) {
				status = 1;
			} else {
				status = 0;
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
		}
		if(status==0){
			result.setStatus(String.valueOf(status));
			result.setToast(toast);
			result.setAddressId("0");
			return ok(Json.toJson(result));
		}
		phone = phone.trim();
		Address addressModel = new Address();
		addressModel.setAddress(address);
		addressModel.setAreaCode(areaCode);
		addressModel.setCardId(cardId);
		addressModel.setDate_add(new Date());
		addressModel.setName(name);
		addressModel.setPhone(phone);
		addressModel.setFlg("1");
		addressModel.setPostcode(postcode);
		addressModel.setProvince(province);
		addressModel.setCardImg(0);
		addressModel.setuId(Numbers.parseLong(uid, 0L));
		addressModel = addressService.saveAddress(addressModel);
		result.setStatus("1");
		result.setAddressId(String.valueOf(addressModel.getAddressId()));
		return ok(Json.toJson(result));
	}
	//(三十四)	收货地址修改接口(POST方式) address_edit.php
	public Result address_edit(){
		response().setContentType("application/json;charset=utf-8");
		
		String uid = AjaxHellper.getHttpParamOfFormUrlEncoded(request(), "uid");
		String addressid = AjaxHellper.getHttpParamOfFormUrlEncoded(request(), "addressid");
		String checkAddress = AjaxHellper.getHttpParamOfFormUrlEncoded(request(), "checkAddress");
		String address = AjaxHellper.getHttpParamOfFormUrlEncoded(request(), "address");
		String name = AjaxHellper.getHttpParamOfFormUrlEncoded(request(), "name");
		String phone = AjaxHellper.getHttpParamOfFormUrlEncoded(request(), "phone");
		String cardId = AjaxHellper.getHttpParamOfFormUrlEncoded(request(), "cardId")==null?"":AjaxHellper.getHttpParamOfFormUrlEncoded(request(), "cardId");
		String province = AjaxHellper.getHttpParamOfFormUrlEncoded(request(), "province");
		String postcode = AjaxHellper.getHttpParamOfFormUrlEncoded(request(), "postcode")==null?"":AjaxHellper.getHttpParamOfFormUrlEncoded(request(), "postcode");
		String areaCode = AjaxHellper.getHttpParamOfFormUrlEncoded(request(), "areaCode")==null?"":AjaxHellper.getHttpParamOfFormUrlEncoded(request(), "areaCode");
		String devid = AjaxHellper.getHttpParamOfFormUrlEncoded(request(), "devid");
		String wdhjy = AjaxHellper.getHttpParamOfFormUrlEncoded(request(),"wdhjy");
		String appversion = AjaxHellper.getHttpParamOfFormUrlEncoded(request(),"appversion");
		
		
		StatusOnlyVO result = new StatusOnlyVO();
		if (!StringUtil.checkMd5(devid, wdhjy,appversion))
		{
			result.setStatus(0);
			result.setToast("验证失败");
			return ok(Json.toJson(result));
		}
		
		if(StringUtils.isBlank(addressid)||StringUtils.isBlank(uid)||StringUtils.isBlank(address)||StringUtils.isBlank(name)||StringUtils.isBlank(phone)){
			result.setStatus(0);
			result.setToast("缺少必填项");
			return ok(Json.toJson(result));
		}
		phone = phone.trim();
		Address addressModel = addressService.findByAddressId(Numbers.parseLong(addressid, 0L));
		if(addressModel==null){
			result.setStatus(0);
			result.setToast("该地址不存在");
			return ok(Json.toJson(result));
		}
		int status = 1;
		String toast = "";
		if("1".equals(checkAddress)){
			int flag = certificationService.checkNameWithCard(uid, name, cardId);
			if (flag == 0) {
				status = 1;
			} else {
				status = 0;
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
		}
		if(status==0){
			result.setStatus(status);
			result.setToast(toast);
			return ok(Json.toJson(result));
		}
		addressModel.setAddress(address);
		addressModel.setAreaCode(areaCode);
		addressModel.setCardId(cardId);
		addressModel.setDate_add(new Date());
		addressModel.setName(name);
		addressModel.setPhone(phone);
		addressModel.setFlg("1");
		addressModel.setPostcode(postcode);
		addressModel.setProvince(province);
		addressModel.setuId(Numbers.parseLong(uid, 0L));
		addressModel = addressService.saveAddress(addressModel);
		result.setStatus(status);
		result.setToast(toast);
		return ok(Json.toJson(result));
	}
	//获取收货地址设置默认接口(GET方式) address_default.php
	public Result address_default(){
		response().setContentType("application/json;charset=utf-8");
		String uid = AjaxHellper.getHttpParam(request(), "uid");
		String devid = AjaxHellper.getHttpParam(request(), "devid");
		String wdhjy = AjaxHellper.getHttpParam(request(),"wdhjy");
		String appversion = AjaxHellper.getHttpParam(request(),"appversion");
		AddressListVO result = new AddressListVO();
		if (!StringUtil.checkMd5(devid, wdhjy,appversion))
		{
			result.setStatus("0");
			return ok(Json.toJson(result));
		}
		
		
		List<Address> addressList = addressService.address_default(Numbers.parseLong(uid, 0L));
		List<Address> tlist=new ArrayList<Address>();
		String domains=StringUtil.getOSSUrl();
		
		if(addressList!=null && !addressList.isEmpty()){
			for(Address a:addressList){
				Address t=new Address();
				t=a;
				String imgpath=a.getImgpath();
				if(!StringUtils.isBlank(imgpath)){
					if(imgpath.indexOf(",")>1){
						String[] tmp=imgpath.split(",");
						t.setImgpath(domains+tmp[0]);
						t.setSecondimgpath(domains+tmp[1]);
					}
					else
						t.setImgpath(domains+t.getImgpath().replace(",", ""));
					tlist.add(t);
				}
				
			}
		}
		
		result.setStatus("1");
		result.setData(addressList);
		return ok(Json.toJson(result));
	}
	
	//收货地址设置默认接口(GET方式) address_setdefault.php
	public Result address_setdefault(){
		response().setContentType("application/json;charset=utf-8");
		String uid = AjaxHellper.getHttpParam(request(), "uid");
		String addressid = AjaxHellper.getHttpParam(request(), "addressid");
		String devid = AjaxHellper.getHttpParam(request(), "devid");
		String wdhjy = AjaxHellper.getHttpParam(request(),"wdhjy");
		String appversion = AjaxHellper.getHttpParam(request(),"appversion");
		StatusOnlyVO result = new StatusOnlyVO();
		if (!StringUtil.checkMd5(devid, wdhjy,appversion))
		{
			result.setStatus(0);
			return ok(Json.toJson(result));
		}
		
		if(StringUtils.isBlank(uid)||StringUtils.isBlank(addressid)){
			result.setStatus(0);
			return ok(Json.toJson(result));
		}
		Address address = addressService.findByUIdAndAddressId(Numbers.parseLong(uid, 0L),Numbers.parseLong(addressid, 0L));
		if(address==null){
			result.setStatus(0);
			return ok(Json.toJson(result));
		}
		addressService.address_setdefault(Numbers.parseLong(uid, 0L),Numbers.parseLong(addressid, 0L));
		result.setStatus(1);
		return ok(Json.toJson(result));
	}
	
	//(三十一)	收货地址删除接口(GET方式) address_del.php
	public Result address_del(){
		response().setContentType("application/json;charset=utf-8");
		String uid = AjaxHellper.getHttpParam(request(), "uid");
		String addressid = AjaxHellper.getHttpParam(request(), "addressid");
		String devid = AjaxHellper.getHttpParam(request(), "devid");
		String wdhjy = AjaxHellper.getHttpParam(request(),"wdhjy");
		String appversion = AjaxHellper.getHttpParam(request(),"appversion");
		StatusOnlyVO result = new StatusOnlyVO();
		if (!StringUtil.checkMd5(devid, wdhjy,appversion))
		{
			result.setStatus(0);
			return ok(Json.toJson(result));
		}
		if(StringUtils.isBlank(uid)||StringUtils.isBlank(addressid)){
			result.setStatus(0);
			return ok(Json.toJson(result));
		}
		List<Object[]> deviceUsers = userService.getDeviceUser(uid);
		if(deviceUsers!=null && deviceUsers.size()>0){
			addressService.deleteAddress(Numbers.parseLong(uid, 0L),Numbers.parseLong(addressid, 0L));
			result.setStatus(1);
			return ok(Json.toJson(result));
		}else{
			result.setStatus(4);//用户不存在
			return ok(Json.toJson(result));
		}
	}
}
