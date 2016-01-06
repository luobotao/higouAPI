package vo.appPad;

import java.util.List;

import vo.product.ProductDetailVO.ProductDetailItem;

public class appPadVO {
	// 状态 0：失败 1：成功
	public String status;
	// 返回提示（不管请求状态，有值就提示）
	public String msg;
	// uid: 登录用户UID(是否支持加密过的UID？）
	public String uid;
	
	public String phone;
	
	//商户名称
	public String ushopname;
	//商户图片地址
	public String ushopicon;
	
	public String getPhone() {
		return phone;
	}
	public void setPhone(String phone) {
		this.phone = phone;
	}
	
	public String getUshopname() {
		return ushopname;
	}
	public void setUshopname(String ushopname) {
		this.ushopname = ushopname;
	}
	public String getUshopicon() {
		return ushopicon;
	}
	public void setUshopicon(String ushopicon) {
		this.ushopicon = ushopicon;
	}
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}
	public String getMsg() {
		return msg;
	}
	public void setMsg(String msg) {
		this.msg = msg;
	}
	public String getUid() {
		return uid;
	}
	public void setUid(String uid) {
		this.uid = uid;
	}
}