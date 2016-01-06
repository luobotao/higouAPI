package utils.wxpay;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;

import services.ServiceFactory;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

/*
 '微信支付服务器签名支付请求请求类
 '============================================================================
 'api说明：
 'init(app_id, app_secret, partner_key, app_key);
 '初始化函数，默认给一些参数赋值，如cmdno,date等。
 'setKey(key_)'设置商户密钥
 'getLasterrCode(),获取最后错误号
 'GetToken();获取Token
 'getTokenReal();Token过期后实时获取Token
 'createMd5Sign(signParams);生成Md5签名
 'genPackage(packageParams);获取package包
 'createSHA1Sign(signParams);创建签名SHA1
 'sendPrepay(packageParams);提交预支付
 'getDebugInfo(),获取debug信息
 '============================================================================
 '*/
public class RequestHandler {
	/** Token获取网关地址地址 */
	private String tokenUrl;
	/** 预支付网关url地址 */
	private String gateUrl;
	/** 查询支付通知网关URL */
	private String notifyUrl;
	/** 商户参数 */
	private String appid;
	private String appkey;
	private String partnerkey;
	private String appsecret;
	private String key;
	/** 请求的参数 */
	private SortedMap parameters;
	/** Token */
	private String Token;
	private String charset;
	/** debug信息 */
	private String debugInfo;
	private String last_errcode;


	/**
	 * 初始构造函数。
	 * 
	 * @return
	 */
	public RequestHandler() {
		this.last_errcode = "0";
		this.charset = "GBK";
		this.parameters = new TreeMap();
		// 获取Token网关
		tokenUrl = "https://api.weixin.qq.com/cgi-bin/token";
		// 提交预支付单网关
		gateUrl = "https://api.weixin.qq.com/pay/genprepay";
		// 验证notify支付订单网关
		notifyUrl = "https://gw.tenpay.com/gateway/simpleverifynotifyid.xml";
	}

	/**
	 * 初始化函数。
	 */
	public void init(String app_id, String app_secret, String app_key,
			String partner, String key) {
		this.last_errcode = "0";
		this.Token = "token_";
		this.debugInfo = "";
		this.appkey = app_key;
		this.appid = app_id;
		this.partnerkey = partner;
		this.appsecret = app_secret;
		this.key = key;
	}

	public void init() {
	}

	/**
	 * 获取最后错误号
	 */
	public String getLasterrCode() {
		return last_errcode;
	}

	/**
	 *获取入口地址,不包含参数值
	 */
	public String getGateUrl() {
		return gateUrl;
	}

	/**
	 * 获取参数值
	 * 
	 * @param parameter
	 *            参数名称
	 * @return String
	 */
	public String getParameter(String parameter) {
		String s = (String) this.parameters.get(parameter);
		return (null == s) ? "" : s;
	}

	/**
	 * 设置密钥
	 */
	public void setKey(String key) {
		this.key = key;
	}

	/**
	 * 获取TOKEN，一天最多获取200次，需要所有用户共享值
	 */
	public String GetToken() {
		String wxpay_access_token = ServiceFactory.getCacheService().get("wxpay_access_token");
		if(StringUtils.isBlank(wxpay_access_token)){
			wxpay_access_token = getTokenReal();
		}
		return wxpay_access_token;

	}

	/**
	 * 实时获取token，并更新到application中
	 */
	public String getTokenReal() {
		String requestUrl = tokenUrl + "?grant_type=client_credential&appid="
				+ appid + "&secret=" + appsecret;
		try {
			// 发送请求，返回json
			TenpayHttpClient httpClient = new TenpayHttpClient();
			httpClient.setReqContent(requestUrl);
			String resContent = "";
			if (httpClient.callHttpPost(requestUrl, "")) {
				resContent = httpClient.getResContent();
				Gson gson = new Gson();
				Map<String, String> map = gson.fromJson(resContent,
						new TypeToken<Map<String, String>>() {
						}.getType());
				// 判断返回是否含有access_token
				if (map.containsKey("access_token")) {
					// 更新application值
					Token = map.get("access_token");
				} else {
					System.out.println("get token err ,info ="
							+ map.get("errmsg"));
				}
				ServiceFactory.getCacheService().set("wxpay_access_token",Token);
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
		return Token;
	}

	// 特殊字符处理
	public String UrlEncode(String src) throws UnsupportedEncodingException {
		return URLEncoder.encode(src, this.charset).replace("+", "%20");
	}

	// 获取package带参数的签名包
	public String genPackage(SortedMap<String, String> packageParams)
			throws UnsupportedEncodingException {
		String sign = createSign(packageParams);

		StringBuffer sb = new StringBuffer();
		Set es = packageParams.entrySet();
		Iterator it = es.iterator();
		while (it.hasNext()) {
			Map.Entry entry = (Map.Entry) it.next();
			String k = (String) entry.getKey();
			String v = (String) entry.getValue();
			sb.append(k + "=" + UrlEncode(v) + "&");
		}

		// 去掉最后一个&
		String packageValue = sb.append("sign=" + sign).toString();
		System.out.println("packageValue=" + packageValue);
		return packageValue;
	}

	/**
	 * 创建md5摘要,规则是:按参数名称a-z排序,遇到空值的参数不参加签名。
	 */
	public String createSign(SortedMap<String, String> packageParams) {
		StringBuffer sb = new StringBuffer();
		Set es = packageParams.entrySet();
		Iterator it = es.iterator();
		while (it.hasNext()) {
			Map.Entry entry = (Map.Entry) it.next();
			String k = (String) entry.getKey();
			String v = (String) entry.getValue();
			if (null != v && !"".equals(v) && !"sign".equals(k)
					&& !"key".equals(k)) {
				sb.append(k + "=" + v + "&");
			}
		}
		sb.append("key=" + this.getKey());
		System.out.println("md5 sb:" + sb);
		String sign = MD5Util.MD5Encode(sb.toString(), this.charset)
				.toUpperCase();

		return sign;

	}

	// 提交预支付
	public String sendPrepay(SortedMap packageParams) {
		String prepayid = "";
		// 转换成json
		Gson gson = new Gson();
		/* String postData =gson.toJson(packageParams); */
		String postData = "{";
		Set es = packageParams.entrySet();
		Iterator it = es.iterator();
		while (it.hasNext()) {
			Map.Entry entry = (Map.Entry) it.next();
			String k = (String) entry.getKey();
			String v = (String) entry.getValue();
			if (k != "appkey") {
				if (postData.length() > 1)
					postData += ",";
				postData += "\"" + k + "\":\"" + v + "\"";
			}
		}
		postData += "}";
		// 设置链接参数
		String requestUrl = this.gateUrl + "?access_token=" + this.Token;
		System.out.println("post url=" + requestUrl);
		System.out.println("post data=" + postData);
		TenpayHttpClient httpClient = new TenpayHttpClient();
		httpClient.setReqContent(requestUrl);
		String resContent = "";
		if (httpClient.callHttpPost(requestUrl, postData)) {
			resContent = httpClient.getResContent();
			System.out.println("res json=" + resContent);
			Map<String, String> map = gson.fromJson(resContent,
					new TypeToken<Map<String, String>>() {
					}.getType());
			if ("0".equals(map.get("errcode"))) {
				prepayid = map.get("prepayid");
			} else {
				System.out.println("get token err ,info =" + map.get("errmsg"));
			}
		}
		return prepayid;
	}

	/**
	 * 创建package签名
	 */
	public boolean createMd5Sign(String signParams) {
		StringBuffer sb = new StringBuffer();
		Set es = this.parameters.entrySet();
		Iterator it = es.iterator();
		while (it.hasNext()) {
			Map.Entry entry = (Map.Entry) it.next();
			String k = (String) entry.getKey();
			String v = (String) entry.getValue();
			if (!"sign".equals(k) && null != v && !"".equals(v)) {
				sb.append(k + "=" + v + "&");
			}
		}

		// 算出摘要
		String enc = TenpayUtil.getCharacterEncoding();
		String sign = MD5Util.MD5Encode(sb.toString(), enc).toLowerCase();

		String tenpaySign = this.getParameter("sign").toLowerCase();

		// debug信息
		this.setDebugInfo(sb.toString() + " => sign:" + sign + " tenpaySign:"
				+ tenpaySign);

		return tenpaySign.equals(sign);
	}

	/**
	 * 设置debug信息
	 */
	protected void setDebugInfo(String debugInfo) {
		this.debugInfo = debugInfo;
	}
	public void setPartnerkey(String partnerkey) {
		this.partnerkey = partnerkey;
	}
	public String getDebugInfo() {
		return debugInfo;
	}
	public String getKey() {
		return key;
	}

	public static void main(String[] args) {
		String jsapi_ticket="sM4AOVdWfPE4DxkXGEs8VFCgRh0iwf1tmZ6onJivOlrrxmvFnbWVGeFWZWpl0OBxCDsQkZptSlfSOfJxChpNKw";
	
		String noncestr="e698959e9b93e4de823526327ffed84a";
		String timestamp="1428483205" ;
		String url="http://ht2.neolix.cn/lovely/test2.html" ;
		String test = "jsapi_ticket="+jsapi_ticket+"&noncestr="+noncestr+"&timestamp="+timestamp+"&url="+url;
		System.out.println(Sha1Util.getSha1(test));
	}
}
