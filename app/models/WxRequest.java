package models;

import java.math.BigDecimal;

public class WxRequest {
	private static String appid="wx0cef7e835f598e36";//公众账号ID
	private static String mch_id="1228736802";//商户号
	private static String device_info="";//设备号
	private String nonce_str;//随机字符串
	private String sign;//签名
	private String body;//商品描述
	private String detail;//商品详情
	private String attach;//附加数据
	private String out_trade_no;//商户订单号
	private static String fee_type="CNY";//货币类型
	private Integer total_fee;//总金额
	private String spbill_create_ip;//终端IP
	private String time_start;//交易起始时间
	private String time_expire;//交易结束时间
	private String goods_tag;//商品标记
	private String notify_url;//通知地址
	
	public String trade_type="JSAPI";//交易类型
	private String product_id;//商品ID
	private String openid;//用户标识
	
	
	
	public void setGoods_tag(String goods_tag) {
		this.goods_tag = goods_tag;
	}

	public String getAppid() {
		return appid;
	}

	public String getMch_id() {
		return mch_id;
	}

	public String getDevice_info() {
		return device_info;
	}


	public String getNonce_str() {
		return nonce_str;
	}
	public void setNonce_str(String nonce_str) {
		this.nonce_str = nonce_str;
	}
	public String getSign() {
		return sign;
	}
	public void setSign(String sign) {
		this.sign = sign;
	}
	public String getBody() {
		return body;
	}
	public void setBody(String body) {
		this.body = body;
	}
	public String getDetail() {
		return detail;
	}
	public void setDetail(String detail) {
		this.detail = detail;
	}
	public String getAttach() {
		return attach;
	}
	public void setAttach(String attach) {
		this.attach = attach;
	}
	public String getOut_trade_no() {
		return out_trade_no;
	}
	public void setOut_trade_no(String out_trade_no) {
		this.out_trade_no = out_trade_no;
	}
	public String getFee_type() {
		return fee_type;
	}

	public Integer getTotal_fee() {
		return total_fee;
	}
	public void setTotal_fee(Integer total_fee) {
		this.total_fee = total_fee;
	}
	public String getSpbill_create_ip() {
		return spbill_create_ip;
	}
	public void setSpbill_create_ip(String spbill_create_ip) {
		this.spbill_create_ip = spbill_create_ip;
	}
	public String getTime_start() {
		return time_start;
	}
	public void setTime_start(String time_start) {
		this.time_start = time_start;
	}
	public String getTime_expire() {
		return time_expire;
	}
	public void setTime_expire(String time_expire) {
		this.time_expire = time_expire;
	}
	public String getGoods_tag() {
		return goods_tag;
	}

	public String getNotify_url() {
		return notify_url;
	}
	public void setNotify_url(String notify_url) {
		this.notify_url = notify_url;
	}
	public String getTrade_type() {
		return trade_type;
	}

	public String getProduct_id() {
		return product_id;
	}
	public void setProduct_id(String product_id) {
		this.product_id = product_id;
	}
	public String getOpenid() {
		return openid;
	}
	public void setOpenid(String openid) {
		this.openid = openid;
	}
}
