package controllers.api;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.net.InetAddress;
import java.net.URLEncoder;
import java.security.MessageDigest;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Formatter;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import models.Address;
import models.Comment;
import models.CountH5;
import models.Currency;
import models.Fromsite;
import models.Product;
import models.ProductDetail;
import models.Product_images;
import models.Qanswer;
import models.Question;
import models.ShoppingCart;
import models.ShoppingOrder;
import models.User;
import models.WxRequest;
import models.WxSign;
import net.sf.json.JSONObject;

import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.springframework.data.domain.Page;

import play.Configuration;
import play.Logger;
import play.data.Form;
import play.libs.Json;
import play.mvc.Result;
import services.ApplicationService;
import services.ICacheService;
import services.ServiceFactory;
import services.SmsService;
import services.api.AddressService;
import services.api.CertificationService;
import services.api.CommentService;
import services.api.CouponService;
import services.api.EndorsementService;
import services.api.ProductService;
import services.api.ShoppingCartService;
import services.api.ShoppingOrderService;
import services.api.UserService;
import utils.AjaxHellper;
import utils.Constants;
import utils.Numbers;
import utils.OSSUtils;
import utils.StringUtil;
import utils.alipay.AlipayNotify;
import utils.wxpay.MD5Util;
import utils.wxpay.Sha1Util;
import utils.wxpay.TenpayHttpClient;
import vo.StatusMsgVO;
import vo.StatusOnlyVO;
import vo.shoppingCart.ShoppingCartCategoryVO;
import vo.shoppingCart.ShoppingCartItemVO;
import assets.CdnAssets;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

@Named
@Singleton
public class H5ProductController extends BaseApiController {
	private static final Logger.ALogger logger = Logger
			.of(H5ProductController.class);
	private static final SimpleDateFormat CHINESE_DATE_TIME_FORMAT = new SimpleDateFormat(
			"yyyyMMddHHmmss");
	private static final SimpleDateFormat CHINESE_DATE_TIME_FORMAT_NORMAL = new SimpleDateFormat(
			"yyyy-MM-dd HH:mm:ss");
	private static final java.util.regex.Pattern PHONE_PATTERN = java.util.regex.Pattern
			.compile("^((1))\\d{10}$");

	private final UserService userService;
	private final ProductService productService;
	private final CommentService commentService;
	private final EndorsementService endorseService;
	private final AddressService addressService;
	private final ShoppingOrderService orderService;
	private final CouponService couponService;
	private final SmsService smsService;
	private final ShoppingCartService shoppingCartService;
	private final CertificationService certificationService;
	private ICacheService cache = ServiceFactory.getCacheService();

	private String domainimg = CdnAssets.CDN_API_PUBLIC_URL;
	// 支付完成后的回调处理页面
	private static String notify_url = Constants.WXCALLBACK;
	static {
		boolean IsProduct = Configuration.root()
				.getBoolean("production", false);
		String domain = Configuration.root().getString("alipay.url.dev",
				"http://182.92.227.140:9004");
		if (IsProduct) {
			domain = Configuration.root().getString("alipay.url.productH5",
					"http://123.56.105.53:9002");
		}
	}

	@Inject
	public H5ProductController(final ApplicationService applicationService,
			final UserService userService, final ProductService productService,
			final CommentService commentService,
			final EndorsementService endorseService,
			final AddressService addressService,
			final ShoppingOrderService orderService,
			final CouponService couponService, final SmsService smsService,
			final ShoppingCartService shoppingCartService,
			final CertificationService certificationService) {
		this.userService = userService;
		this.productService = productService;
		this.commentService = commentService;
		this.endorseService = endorseService;
		this.addressService = addressService;
		this.orderService = orderService;
		this.couponService = couponService;
		this.smsService = smsService;
		this.shoppingCartService = shoppingCartService;
		this.certificationService = certificationService;
	}

	public Result login() {
		String pid = AjaxHellper.getHttpParam(request(), "pid");
		Long uid = Numbers.parseLong(
				AjaxHellper.getHttpParam(request(), "uid"), 0L);
		String mycode = AjaxHellper.getHttpParam(request(), "mcode");

		User user = new User();// userService.getUserByUid(uid);
		String flg = AjaxHellper.getHttpParam(request(), "flg");

		if (!StringUtils.isBlank(session("openid")))
			user.setOpenId(session("openid"));
		if (!StringUtils.isBlank(session("unionid")))
			user.setUnionid(session("unionid"));
		if (!StringUtils.isBlank(session("access_token")))
			user.setAccessToken(session("access_token"));
		if (!StringUtils.isBlank(session("wx_nickname")))
			user.setNickname(session("wx_nickname"));

		if (!StringUtils.isBlank(session("wx_headimgurl")))
			user.setHeadIcon(session("wx_headimgurl"));

		return ok(views.html.H5.login.render(user, pid, flg, mycode,
				String.valueOf(uid)));
	}

	// @Before
	public boolean checkses() {
		if (StringUtils.isBlank(session("H5uid"))) {
			return false;
		}
		return true;
	}

	public String gettempStr(Map<String, String> map, String key) {
		String sign = "";
		if (map != null) {
			for (Map.Entry<String, String> entry : map.entrySet()) {
				if (entry.getKey().equals(key))
					return entry.getValue();
			}
		}
		return sign;
	}

	// 订单完成
	public Result saveorderend() {

		Long uid = Numbers.parseLong(session("uid"), 0L);// Numbers.parseLong(AjaxHellper.getHttpParam(request(),
															// "uid"), 0L);
		String orderCode = AjaxHellper.getHttpParam(request(), "ordercode");
		String timstr = AjaxHellper.getHttpParam(request(), "timstr");
		String nostr = AjaxHellper.getHttpParam(request(), "nostr");

		String sign = AjaxHellper.getHttpParam(request(), "sign");
		String prepayid = AjaxHellper.getHttpParam(request(), "payid");

		if (StringUtils.isBlank(orderCode) || uid.longValue() == 0) {
			return ok(views.html.H5.pageError.render());
		}

		User user = userService.getUserByUid(uid);

		ShoppingOrder order = orderService
				.getShoppingOrderByOrderCode(orderCode);
		List<Product> plist = orderService.getproductListByOrderCode(orderCode);

		Product proinfo = plist == null || plist.isEmpty() ? null : plist
				.get(0);

		if (proinfo == null || order == null) {
			return ok(views.html.H5.pageError.render());
		}

		// 更新产品支付状态
		String method = "11";
		String state = "20";
		productService.setPayStatus(orderCode, method, state,
				order.getTotalFee(), prepayid);
		// return
		// ok(views.html.sheSaid.orderend.render("/H5/showorder?ordercode="+orderCode));

		proinfo.setRmbprice(Double.valueOf(Integer.valueOf(new BigDecimal(
				proinfo.getRmbprice()).toString())));
		// proinfo.setEndorsementPrice(Double.valueOf(Integer.valueOf(new
		// BigDecimal(proinfo.getEndorsementPrice()).toString())));
		proinfo.setFromobj(productService.getfrom(proinfo.getFromsite()));

		int weightfee = proinfo.getFromobj().getFee();
		if (proinfo.getWeight() * 1000 > 500) {
			Double wf = proinfo.getFromobj().getFee()
					+ ((proinfo.getWeight() * 1000 - 500) / 100)
					* proinfo.getFromobj().getAddfee();
			BigDecimal b = new BigDecimal(String.valueOf(wf));
			weightfee = Integer.valueOf(b.setScale(0, BigDecimal.ROUND_CEILING)
					.toString());
		}
		proinfo.setPostfee(weightfee);

		Integer pcount = plist == null || plist.isEmpty() ? 0 : plist.size();


		if (plist.size() > 1) {
			plist = orderService.getproductListByOrderCode(orderCode);
			List<Long> pidlist = new ArrayList<Long>();
			for (Product p : plist) {
				if (p.getTyp().equals("2")) {
					// 算折扣
					if (p.getRmbprice() > 0 && p.getList_price() > 0) {
						if (p.getChinaprice() != null
								&& p.getChinaprice().doubleValue() > 0) {
							BigDecimal mData = new BigDecimal(10
									* p.getRmbprice() / p.getChinaprice())
									.setScale(1, BigDecimal.ROUND_UP);
							p.setExtcode(mData.toString());
						} else {
							p.setExtcode("");
						}
					} else
						p.setExtcode("");
				} else {
					Currency currency = productService.queryCurrencyById(p
							.getCurrency());
					BigDecimal rate = new BigDecimal(currency.getRate() / 100)
							.setScale(4, BigDecimal.ROUND_CEILING);
					BigDecimal price = new BigDecimal(p.getPrice() / 100)
							.setScale(2, BigDecimal.ROUND_CEILING);
					BigDecimal rmb_price = rate.multiply(price).setScale(0,
							BigDecimal.ROUND_CEILING);
					p.setRmbprice(Double.valueOf(rmb_price.toString()));
					if (p.getChinaprice() > 0) {
						BigDecimal mData = new BigDecimal(10 * p.getRmbprice()
								/ p.getChinaprice()).setScale(1,
								BigDecimal.ROUND_UP);
						p.setExtcode(mData.toString());
					} else
						p.setExtcode("");

				}
				pidlist.add(p.getPid());
			}
			weightfee = productService.getWeightFee(pidlist,
					Numbers.parseLong(session("uid"), 0L)).intValue();
			int costfee = productService.getRateFee(
					pidlist.toString().substring(1, pidlist.size() - 1), uid)
					.intValue();

			return ok(views.html.H5.ordercartsuccess.render(order.getProvince()
					+ order.getAddress(), plist, user, order, pcount, weightfee, costfee));
		}
		return ok(views.html.H5.ordersuccess.render(
				order.getProvince() + order.getAddress(), proinfo, user, order,
				pcount, String.valueOf(proinfo.getRmbprice())));
	}

	// 提交订单完成支付宝返回商城回调
	public Result alipayreturn() {
		// response().setContentType("application/json;charset=utf-8");
		String method = "21";
		String state = "20";

		Map<String, String> params = new HashMap<String, String>();
		Map<String, String[]> allQue = request().queryString();
		if (allQue != null && allQue.keySet() != null) {
			Iterator<String> keyIt = allQue.keySet().iterator();
			while (keyIt.hasNext()) {
				String key = keyIt.next();
				String[] values = allQue.get(key);
				String valueStr = "";
				for (int i = 0; i < values.length; i++) {
					valueStr = (i == values.length - 1) ? valueStr + values[i]
							: valueStr + values[i] + ",";
				}
				// 乱码解决，这段代码在出现乱码时使用。如果mysign和sign不相等也可以使用这段代码转化
				try {
					valueStr = new String(valueStr.getBytes("ISO-8859-1"),
							"utf-8");
				} catch (UnsupportedEncodingException e) {
					e.printStackTrace();
				}
				params.put(key, valueStr);
			}
			String out_trade_no = "";// 商户订单号
			String trade_no = "";// 支付宝交易号
			String trade_status = "";// 交易状态
			try {
				out_trade_no = new String(params.get("out_trade_no").getBytes(
						"ISO-8859-1"), "UTF-8");
				trade_no = new String(params.get("trade_no").getBytes(
						"ISO-8859-1"), "UTF-8");
				trade_status = params.get("result") == null ? "" : new String(
						params.get("result").getBytes("ISO-8859-1"), "UTF-8");
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}

			if (StringUtils.isBlank(out_trade_no)) {
				return ok(views.html.H5.pageError.render());
			}
			boolean verify_result = AlipayNotify.verifyReturn(params);

			if (!verify_result) 
				return ok(views.html.H5.pageError.render());

				if (out_trade_no.length() > 10) {
					out_trade_no = out_trade_no.substring(0, 10);
				}
				ShoppingOrder shoppingOrder = productService
						.getShoppingOrderByOrderCode(out_trade_no);
				if(shoppingOrder==null)
					return ok(views.html.H5.pageError.render());
				
					int status = productService.checkOrderPayStat(out_trade_no,
							shoppingOrder.getTotalFee());
					if (status !=1)
						return ok(views.html.H5.pageError.render());

						productService.setPayStatusFast(out_trade_no, method,
								state, shoppingOrder.getTotalFee(), trade_no);
						User user = userService.getUserByUid(shoppingOrder
								.getuId());
						List<Product> plist = orderService
								.getproductListByOrderCode(shoppingOrder
										.getOrderCode());
						Product proinfo = plist == null || plist.isEmpty() ? null
								: plist.get(0);

						if (proinfo == null) {
							return ok(views.html.H5.pageError.render());
						}

						proinfo.setFromobj(productService.getfrom(proinfo
								.getFromsite()));

						int weightfee = proinfo.getFromobj().getFee();
						if (proinfo.getWeight() * 1000 > 500) {
							Double wf = proinfo.getFromobj().getFee()
									+ ((proinfo.getWeight() * 1000 - 500) / 100)
									* proinfo.getFromobj().getAddfee();
							BigDecimal b = new BigDecimal(String.valueOf(wf));
							weightfee = Integer.valueOf(b.setScale(0,
									BigDecimal.ROUND_CEILING).toString());
						}
						proinfo.setPostfee(weightfee);

						Integer pcount = plist == null || plist.isEmpty() ? 0
								: plist.size();
						if(plist.size()==1)
							pcount=proinfo.getCounts();
						

						if (plist.size() > 1) {
							plist = orderService.getproductListByOrderCode(shoppingOrder.getOrderCode());
							List<Long> pidlist = new ArrayList<Long>();
							for (Product p : plist) {
								if (p.getTyp().equals("2")) {
									// 算折扣
									if (p.getRmbprice() > 0 && p.getList_price() > 0) {
										if (p.getChinaprice() != null
												&& p.getChinaprice().doubleValue() > 0) {
											BigDecimal mData = new BigDecimal(10
													* p.getRmbprice() / p.getChinaprice())
													.setScale(1, BigDecimal.ROUND_UP);
											p.setExtcode(mData.toString());
										} else {
											p.setExtcode("");
										}
									} else
										p.setExtcode("");
								} else {
									Currency currency = productService.queryCurrencyById(p
											.getCurrency());
									BigDecimal rate = new BigDecimal(currency.getRate() / 100)
											.setScale(4, BigDecimal.ROUND_CEILING);
									BigDecimal price = new BigDecimal(p.getPrice() / 100)
											.setScale(2, BigDecimal.ROUND_CEILING);
									BigDecimal rmb_price = rate.multiply(price).setScale(0,
											BigDecimal.ROUND_CEILING);
									p.setRmbprice(Double.valueOf(rmb_price.toString()));
									if (p.getChinaprice() > 0) {
										BigDecimal mData = new BigDecimal(10 * p.getRmbprice()
												/ p.getChinaprice()).setScale(1,
												BigDecimal.ROUND_UP);
										p.setExtcode(mData.toString());
									} else
										p.setExtcode("");

								}
								pidlist.add(p.getPid());
							}
							weightfee = productService.getWeightFee(pidlist,
									Numbers.parseLong(session("uid"), 0L)).intValue();
							int costfee = productService.getRateFee(
									pidlist.toString().substring(1, pidlist.size() - 1), user.getUid())
									.intValue();

							return ok(views.html.H5.ordercartsuccess.render(shoppingOrder.getProvince()
									+ shoppingOrder.getAddress(), plist, user, shoppingOrder, pcount, 
									weightfee, costfee));
						}
						return ok(views.html.H5.ordersuccess.render(
								shoppingOrder.getProvince() + shoppingOrder.getAddress(), proinfo, user, shoppingOrder,
								pcount, String.valueOf(proinfo.getRmbprice())));
			// Address addressobj=addressService.findByAddressId(aid);
		} else
			return ok(views.html.H5.pageError.render());
	}

	// 0元结单
	public Result zeroturn() {
		String method = "21";
		String state = "20";

		String out_trade_no = AjaxHellper.getHttpParam(request(), "ordercode");// 商户订单号
		String trade_no = "";// 支付宝交易号
		String trade_status = "";// 交易状态

		if (StringUtils.isBlank(out_trade_no)) {
			return ok(views.html.H5.pageError.render());
		}

		// 验证订单是否真0元
		boolean verify_result = true;
		ShoppingOrder shoppingOrder = productService
				.getShoppingOrderByOrderCode(out_trade_no);
		if (shoppingOrder.getFinalpay() > 0)
			verify_result = false;
		if (!verify_result)
			return ok(views.html.H5.pageError.render());

		if (verify_result) {// 验证成功

			if (out_trade_no.length() > 10) {
				out_trade_no = out_trade_no.substring(0, 10);
			}

			if (shoppingOrder != null) {
				int status = productService.checkOrderPayStat(out_trade_no,
						shoppingOrder.getTotalFee());
				if (status == 1) {
					productService.setPayStatusFast(out_trade_no, method,
							state, shoppingOrder.getTotalFee(), trade_no);
					User user = userService
							.getUserByUid(shoppingOrder.getuId());
					List<Product> plist = orderService
							.getproductListByOrderCode(shoppingOrder
									.getOrderCode());
					Product proinfo = plist == null || plist.isEmpty() ? null
							: plist.get(0);

					if (proinfo == null) {
						return ok(views.html.H5.pageError.render());
					}
					proinfo.setEndorsementPrice(Double.valueOf(Integer
							.valueOf(new BigDecimal(proinfo
									.getEndorsementPrice()).toString())));
					proinfo.setFromobj(productService.getfrom(proinfo
							.getFromsite()));

					int weightfee = proinfo.getFromobj().getFee();
					if (proinfo.getWeight() * 1000 > 500) {
						Double wf = proinfo.getFromobj().getFee()
								+ ((proinfo.getWeight() * 1000 - 500) / 100)
								* proinfo.getFromobj().getAddfee();
						BigDecimal b = new BigDecimal(String.valueOf(wf));
						weightfee = Integer.valueOf(b.setScale(0,
								BigDecimal.ROUND_CEILING).toString());
					}
					proinfo.setPostfee(weightfee);

					Integer pcount = plist == null || plist.isEmpty() ? 0
							: plist.size();

					String rmbprice = String.valueOf(proinfo.getRmbprice());

					return ok(views.html.H5.ordersuccess.render(
							shoppingOrder.getAddress(), proinfo, user,
							shoppingOrder, pcount, rmbprice));

				} else {
					return ok(views.html.H5.pageError.render());
				}
			} else {
				return ok(views.html.H5.pageError.render());
			}
		} else {
			return ok(views.html.H5.pageError.render());
		}
	}

	public static Result getsignstri() {
		String tim = Form.form().bindFromRequest().get("tim");
		String nostr = Form.form().bindFromRequest().get("nostr");
		String jkt = Form.form().bindFromRequest().get("jkt");
		String uri = Form.form().bindFromRequest().get("uri");

		String str = "";
		Map<String, String> pramt = new HashMap<String, String>();
		if (!StringUtils.isBlank(jkt)) {
			pramt.put("timestamp", tim);
			pramt.put("noncestr", nostr);
			pramt.put("jsapi_ticket", jkt);
			pramt.put("url", uri);
			str = StringUtil.getShareSign(pramt);
		}
		Logger.info("sing:" + str);
		return ok(Json.toJson(str));
	}

	/*
	 * 检查发送验证码次数24小时内
	 */
	public Result checkimgVerify() {
		String phone = AjaxHellper.getHttpParamOfFormUrlEncoded(request(),
				"phone");
		ObjectNode result = Json.newObject();
		result.put("msg", "请输入图形验证码");
		if (StringUtils.isBlank(phone)) {
			result.put("status", "0");
			result.put("msg", "请输入图形验证码");
			return ok(result);
		}
		Integer count = smsService.getVerifynum(phone, 1440);
		if (count > 100)
			result.put("status", "1");
		else
			result.put("status", "2");

		return ok(result);
	}

	public Result wxpayh5() {
		String nostr = AjaxHellper.getHttpParam(request(), "nostr");
		String sign = AjaxHellper.getHttpParam(request(), "sign");
		String orderCode = AjaxHellper.getHttpParam(request(), "ordercode");
		String payid = AjaxHellper.getHttpParam(request(), "payid");
		String uid = AjaxHellper.getHttpParam(request(), "uid");
		String aid = AjaxHellper.getHttpParam(request(), "aid");
		String pid = AjaxHellper.getHttpParam(request(), "pid");
		String mycode = AjaxHellper.getHttpParam(request(), "mm");
		Map<String, String> pramt = new HashMap<String, String>();
		nostr = RandomStringUtils.randomAlphanumeric(32);
		String timstr = Sha1Util.getTimeStamp();
		pramt.put("appId", Constants.WXappID);
		pramt.put("timeStamp", timstr);
		pramt.put("nonceStr", nostr);
		pramt.put("package", "prepay_id=" + payid);
		pramt.put("signType", "MD5");
		sign = StringUtil.getSign(pramt);
		Product proinfo = null;
		List<Product> plist = orderService.getproductListByOrderCode(orderCode);
		if (plist != null && !plist.isEmpty()) {
			pid = plist.get(0).getPid().toString();
			proinfo = plist.get(0);
		}
		ShoppingOrder order = orderService
				.getShoppingOrderByOrderCode(orderCode);
		if (order != null)
			mycode = order.getMcode();

		if (order == null || proinfo == null)
			return ok(views.html.H5.pageError.render());

		
		return ok(views.html.H5.wxpayh5.render(Constants.WXappID, timstr,
				nostr, payid, sign, orderCode, uid, aid, pid,
				StringUtils.isBlank(mycode) ? "" : mycode));
	}

	// 组装签名字符串
	private static String makeSig(Map<String, String> sortMap) {
		// TODO Auto-generated method stub
		StringBuilder sb = new StringBuilder();
		Object[] keys = sortMap.keySet().toArray();
		Arrays.sort(keys);
		for (int i = 0; i < keys.length; i++) {
			String mapkey = (String) keys[i];
			if (i == keys.length - 1) {// 拼接时，不包括最后一个&字符
				sb.append(mapkey).append("=").append(sortMap.get(mapkey));// QSTRING_EQUAL为=,QSTRING_SPLIT为&
			} else {
				sb.append(mapkey).append("=").append(sortMap.get(mapkey))
						.append("&");
			}
		}
		String data = sb.toString();// 参数拼好的字符串

		System.out.println("加密参数为：" + data);
		return data;
	}

	// 支付宝支付

	public Result alipay() {
		String tradeno = AjaxHellper.getHttpParam(request(), "out_trade_no");
		Long uid = Numbers.parseLong(
				AjaxHellper.getHttpParam(request(), "uid"), 0L);
		Long pid = Numbers.parseLong(
				AjaxHellper.getHttpParam(request(), "pid"), 0L);
		String orderCode = tradeno;// AjaxHellper.getHttpParam(request(),"ordercode");
		String phone = AjaxHellper.getHttpParam(request(), "phone");

		if (StringUtils.isBlank(tradeno) || uid == 0L) {
			return ok(views.html.H5.pageError.render());
		}

		ShoppingOrder order = orderService
				.getShoppingOrderByOrderCode(orderCode);
		if (order == null)
			return ok(views.html.H5.pageError.render());
		Double totalfee = order.getTotalFee();

		String domains = StringUtil.getDomainH5();

		String backurl = URLEncoder.encode(domains + "/H5/alipayreturn");
		// return
		// ok(views.html.H5.alipay.render(tradeno,totalfee,orderCode,uid,pid,backurl,phone));
		return redirect("/api/alipaywap_new_H5?ordercode=" + tradeno
				+ "&amount=" + totalfee + "&backurl=" + backurl
				+ "&pid=@pid&phone=" + phone);
	}

	// 组装统一订购接口XML字符串
	static private String getXmlInfo(WxRequest obj) {
		StringBuilder sb = new StringBuilder();
		sb.append("<xml>");
		sb.append("<appid>" + Constants.WXappID + "</appid>");
		if (obj.getAttach() != null && !obj.getAttach().equals(""))
			sb.append("<attach>" + obj.getAttach() + "</attach>");
		else
			sb.append("<attach></attach>");

		if (obj.getBody() != null && !obj.getBody().equals(""))
			sb.append("<body>" + obj.getBody() + "</body>");
		sb.append("<mch_id>" + Constants.WXMCID + "</mch_id>");
		if (obj.getNonce_str() != null && !obj.getNonce_str().equals(""))
			sb.append("<nonce_str>" + obj.getNonce_str() + "</nonce_str>");
		if (obj.getNotify_url() != null && !obj.getNotify_url().equals(""))
			sb.append("<notify_url>" + obj.getNotify_url() + "</notify_url>");
		if (obj.getOpenid() != null && !obj.getOpenid().equals(""))
			sb.append("<openid>" + obj.getOpenid() + "</openid>");
		if (obj.getOut_trade_no() != null && !obj.getOut_trade_no().equals(""))
			sb.append("<out_trade_no>" + obj.getOut_trade_no()
					+ "</out_trade_no>");
		if (obj.getProduct_id() != null && !obj.getProduct_id().equals(""))
			sb.append("<product_id>" + obj.getProduct_id() + "</product_id>");
		if (obj.getSign() != null && !obj.getSign().equals(""))
			sb.append("<sign>" + obj.getSign() + "</sign>");
		if (obj.getSpbill_create_ip() != null
				&& !obj.getSpbill_create_ip().equals(""))
			sb.append("<spbill_create_ip>" + obj.getSpbill_create_ip()
					+ "</spbill_create_ip>");

		if (obj.getTotal_fee().intValue() > 0)
			sb.append("<total_fee>" + obj.getTotal_fee().intValue()
					+ "</total_fee>");
		if (obj.getTrade_type() != null && !obj.getTrade_type().equals(""))
			sb.append("<trade_type>" + obj.getTrade_type() + "</trade_type>");
		sb.append("</xml>");

		String sbt = sb.toString();
		Logger.debug(sbt);

		return sbt;
	}

	// 微信用户注册成HIGOU用户接口
	public Result reguser() {
		response().setContentType("application/json;charset=utf-8");
		ObjectNode result = Json.newObject();
		String phone = AjaxHellper.getHttpParamOfFormUrlEncoded(request(),
				"phone");
		String verifys = AjaxHellper.getHttpParamOfFormUrlEncoded(request(),
				"verify_sms");
		String nickname = AjaxHellper.getHttpParamOfFormUrlEncoded(request(),
				"nickname");
		String headicon = AjaxHellper.getHttpParamOfFormUrlEncoded(request(),
				"headicon");
		String unionid = AjaxHellper.getHttpParamOfFormUrlEncoded(request(),
				"unionid");
		String openid = AjaxHellper.getHttpParamOfFormUrlEncoded(request(),
				"openid");
		String mycode = AjaxHellper.getHttpParamOfFormUrlEncoded(request(),
				"mcode");
		String fromuid = AjaxHellper.getHttpParamOfFormUrlEncoded(request(),
				"uid");
		String channel = AjaxHellper.getHttpParamOfFormUrlEncoded(request(),
				"channel");

		fromuid = Numbers.parseInt(fromuid, 0).toString();

		if (!StringUtils.isBlank(mycode) && mycode.equals("bbt"))
			channel = "bbt";

		if (StringUtils.isBlank(phone) || StringUtils.isBlank(verifys)) {
			result.put("status", "0");
			return ok(Json.toJson(result));
		}

		result.put("status", "1");
		Long uid = 0L;// userService.checkandreguser(nickname, phone, smscode,
						// headicon, unionid);
		nickname = phone.length() > 5 ? "用户" + phone.substring(5) : "用户"
				+ phone;
		if (!StringUtils.isBlank(mycode)) {
			// 推广使用
			uid = userService.checkandreguserTG(nickname, phone, verifys,
					headicon, unionid, openid, mycode, channel, fromuid);
			cache.setWithOutTime("H5uid", uid.toString(), 1200);
			session("H5uid", uid.toString());
		} else {
			// 正常渠道使用
			uid = userService.checkandreguser(nickname, phone, verifys,
					headicon, unionid, openid, channel, fromuid);
			cache.setWithOutTime("H5uid", uid.toString(), 1200);
			session("H5uid", uid.toString());
		}
		if (uid == 0L) {
			result.put("status", "0");

		} else {
			session("hgUid", uid.toString());
			session("uid", uid.toString());
		}

		result.put("uid", uid.toString());
		return ok(Json.toJson(result));
	}

	public Result editaddress() {
		// if(!this.checkses())
		// return ok(views.html.H5.pageError.render());

		Long uid = Numbers.parseLong(
				AjaxHellper.getHttpParam(request(), "uid"), 0L);
		Long pid = Numbers.parseLong(
				AjaxHellper.getHttpParam(request(), "pid"), Long.valueOf(0));
		Long aid = Numbers.parseLong(
				AjaxHellper.getHttpParam(request(), "aid"), Long.valueOf(0));
		String mycode = AjaxHellper.getHttpParam(request(), "mcode");
		if (pid == 0L || uid == 0L) {
			return ok(views.html.H5.pageError.render());
		}

		Address address = new Address();

		if (aid > 0L) {
			address = addressService.findByAddressId(aid);
		}
		Product proinfo = productService.getProductById(pid);
		
		return ok(views.html.H5.person.render(address, uid, pid, mycode));
	}

	public Result editaddrespro() {
		// if(!this.checkses())
		// return ok(views.html.H5.pageError.render());

		Long uid = Numbers.parseLong(
				AjaxHellper.getHttpParam(request(), "uid"), 0L);
		Long pid = Numbers.parseLong(
				AjaxHellper.getHttpParam(request(), "pid"), Long.valueOf(0));
		Long aid = Numbers.parseLong(
				AjaxHellper.getHttpParam(request(), "aid"), Long.valueOf(0));
		String phone = AjaxHellper.getHttpParam(request(), "phone");

		if (pid == 0L || uid == 0L) {
			return ok(views.html.H5.pageError.render());
		}

		Address address = new Address();

		if (aid > 0L) {
			address = addressService.findByAddressId(aid);
		}
		return ok(views.html.H5.personpro.render(address, uid, pid, phone));
	}

	public Result saveaddressbbt() {
		Long uid = Numbers.parseLong(Form.form().bindFromRequest().get("uid"),
				0L);
		Long pid = Numbers.parseLong(Form.form().bindFromRequest().get("pid"),
				0L);

		Long aid = Numbers.parseLong(Form.form().bindFromRequest().get("aid"),
				0L);
		if (pid == 0L || uid == 0L) {
			return ok(views.html.H5.pageError.render());
		}
		String phone = Form.form().bindFromRequest().get("phone");
		String mycode = Form.form().bindFromRequest().get("mcode");
		String wx = Form.form().bindFromRequest().get("wx");

		// 保存地址
		String aname = Form.form().bindFromRequest().get("Name");
		String post_code = "";// Form.form().bindFromRequest().get("postcode");
		String address = Form.form().bindFromRequest().get("address");
		String province = Form.form().bindFromRequest().get("city_dummy");
		if (StringUtils.isBlank(aname) || StringUtils.isBlank(mycode)
				|| StringUtils.isBlank(address)) {
			return ok(views.html.H5.pageError.render());
		}
		Address ad = new Address();
		ad.setAddress(address);
		ad.setName(aname);
		ad.setPostcode(post_code);
		ad.setPhone(phone);
		ad.setProvince(province);
		ad.setuId(uid);
		ad.setFlg("1");
		ad.setAreaCode(mycode);

		if (aid > 0L) {
			ad.setAddressId(aid);
		}
		ad = addressService.saveAddress(ad);

		// 跳转到购物页面
		Product proinfo = productService.getProductById(pid);
		if (proinfo == null) {
			return ok(views.html.H5.pageError.render());
		}
		proinfo.setFromobj(productService.getfrom(proinfo.getFromsite()));

		if (proinfo.getTyp().equals("2")) {
			// 算折扣
			if (proinfo.getRmbprice() > 0 && proinfo.getList_price() > 0) {
				BigDecimal mData = new BigDecimal(10 * proinfo.getRmbprice()
						/ proinfo.getChinaprice()).setScale(1,
						BigDecimal.ROUND_UP);
				proinfo.setExtcode(mData.toString());
			} else
				proinfo.setExtcode("");
		} else {
			Currency currency = productService.queryCurrencyById(proinfo
					.getCurrency());
			BigDecimal rate = new BigDecimal(currency.getRate() / 100)
					.setScale(4, BigDecimal.ROUND_CEILING);
			BigDecimal price = new BigDecimal(proinfo.getPrice() / 100)
					.setScale(2, BigDecimal.ROUND_CEILING);
			BigDecimal rmb_price = rate.multiply(price).setScale(0,
					BigDecimal.ROUND_CEILING);
			proinfo.setRmbprice(Double.valueOf(rmb_price.toString()));
			if (proinfo.getChinaprice() > 0) {
				BigDecimal mData = new BigDecimal(10 * proinfo.getRmbprice()
						/ proinfo.getChinaprice()).setScale(1,
						BigDecimal.ROUND_UP);
				proinfo.setExtcode(mData.toString());
			} else
				proinfo.setExtcode("");

		}

		// proinfo.setRmbprice(Double.valueOf(Integer.valueOf(new
		// BigDecimal(proinfo.getRmbprice()).toString())));
		int weightfee = proinfo.getFromobj().getFee();
		if (proinfo.getWeight() * 1000 > 500) {
			Double wf = proinfo.getFromobj().getFee()
					+ ((proinfo.getWeight() * 1000 - 500) / 100)
					* proinfo.getFromobj().getAddfee();
			BigDecimal b = new BigDecimal(String.valueOf(wf));
			weightfee = Integer.valueOf(b.setScale(0, BigDecimal.ROUND_CEILING)
					.toString());
		}
		proinfo.setPostfee(weightfee);

		List<Address> alist = addressService.address_default(uid);
		Address addressobj = alist == null || alist.isEmpty() ? null : alist
				.get(0);

		String sign = "";
		String timstr = "";
		String nostr = "";

		return redirect("/H5/order?uid=" + uid + "&pid=" + proinfo.getPid()
				+ "&mcode=" + mycode);
	}

	public Result saveaddress() {
		Long uid = Numbers.parseLong(Form.form().bindFromRequest().get("uid"),
				0L);
		Long aid = Numbers.parseLong(Form.form().bindFromRequest().get("aid"),
				0L);
		String pids = Form.form().bindFromRequest().get("pids");
		ObjectNode result = Json.newObject();
		result.put("status", "0");
		result.put("aid", "0");
		result.put("msg", "地址保存失败");
		if (uid.longValue() == 0) {
			result.put("msg", "用户不存在");
			return ok(result);
		}

		// 保存地址
		String aname = Form.form().bindFromRequest().get("Name");
		String phone = Form.form().bindFromRequest().get("phone");
		String post_code = Form.form().bindFromRequest().get("postcode");
		String address = Form.form().bindFromRequest().get("address");
		String province = Form.form().bindFromRequest().get("city_dummy");
		String cardid = Form.form().bindFromRequest().get("cardid");
		Address ad = null;

		if (!StringUtils.isBlank(aname) && !StringUtils.isBlank(phone)
				&& !StringUtils.isBlank(address)) {
			ad = new Address();
			ad.setAddress(address);
			ad.setName(aname);
			ad.setPostcode(StringUtils.isBlank(post_code) ? "" : post_code);
			ad.setPhone(phone);
			ad.setProvince(province);
			ad.setuId(uid);
			ad.setCardId(cardid);
			ad.setFlg("1");
			String md5str = StringUtil.MD5Encode(aname + phone + province
					+ address);// name+phone+province+address
			ad.setMd5str(md5str);
			if (aid > 0L) {
				Address atmp = addressService.findByAddressId(aid);
				if (atmp != null && !StringUtils.isBlank(atmp.getMd5str())
						&& atmp.getMd5str().equals(ad.getMd5str()))
					ad.setAddressId(aid);
			}
			ad = addressService.saveAddress(ad);
		} else {
			result.put("msg", "请检查资料是否填写完整");
		}

		if (ad != null && ad.getAddressId().longValue() > 0) {
			result.put("aid", ad.getAddressId());
			result.put("msg", "");
			result.put("status", "1");
		}

		int opencardId = userService.getOpencardIdByPids(pids);
		int opencardIdimg = userService.getOpencardIdImgByPids(pids);

		int status = 1;
		String toast = "";
		if (opencardIdimg > 0) {
			if (aid.longValue() > 0) {
				if (ad != null) {
					if (ad.getCardImg() == null
							|| ad.getCardImg().intValue() == 0) {
						status = 3;
						toast = "您购买的商品中有海外直邮商品，海关需要核查身份证照片信息，请您完善。";
					}
				}
			}
		}
		if (status == 1) {
			if (opencardId > 0) {
				if (aid.longValue() > 0) {
					if (!String.valueOf(ad.getuId())
							.equals(String.valueOf(uid))) {
						status = 2;
						toast = "当前用户不存在此收货地址，请查证。";
					}
					int length = ad.getCardId() == null ? 0 : ad.getCardId()
							.length();
					if (length > 15) {
						int flag = certificationService.checkNameWithCard(
								uid.longValue() + "", ad.getName(),
								ad.getCardId());
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
			}
		}
		result.put("status", status + "");
		result.put("msg", toast);
		if (status != 1)
			aid = 0L;

		if (status == 1) {
			ObjectNode rtmp = orderService.checkOrderWithAddress(pids, aid);
			if (rtmp != null) {
				JsonNode rtmpj = Json.toJson(rtmp);
				if (rtmpj != null && rtmpj.has("status")
						&& !(rtmpj.get("status").intValue() + "").equals("1")) {
					result.put("msg", rtmp.get("toast").textValue());
					result.put("aid", "0");
					result.put("status", "2");
				}
			}
		}
		return ok(result);
	}

	public Result saveaddresspro() {
		Long uid = Numbers.parseLong(Form.form().bindFromRequest().get("uid"),
				0L);
		Long pid = Numbers.parseLong(Form.form().bindFromRequest().get("pid"),
				0L);

		Long aid = Numbers.parseLong(Form.form().bindFromRequest().get("aid"),
				0L);
		if (pid == 0L || uid == 0L) {
			return ok(views.html.H5.pageError.render());
		}
		String phone = Form.form().bindFromRequest().get("phone");
		String ph = Form.form().bindFromRequest().get("ph");

		// 保存地址
		String aname = Form.form().bindFromRequest().get("Name");
		String post_code = "";// Form.form().bindFromRequest().get("postcode");
		String address = Form.form().bindFromRequest().get("address");
		String province = Form.form().bindFromRequest().get("city_dummy");
		if (StringUtils.isBlank(aname) || StringUtils.isBlank(phone)
				|| StringUtils.isBlank(address)) {
			return ok(views.html.H5.pageError.render());
		}
		Address ad = new Address();
		ad.setAddress(address);
		ad.setName(aname);
		ad.setPostcode(post_code);
		ad.setPhone(phone);
		ad.setProvince(province);
		ad.setuId(uid);
		ad.setFlg("1");
		if (aid > 0L) {
			ad.setAddressId(aid);
		}
		ad = addressService.saveAddress(ad);

		return null;
		// return
		// ok(views.html.H5.orderpro.render(ad,proinfo,uid,Constants.WXappID,session("wx_access_token")==null?"":session("wx_access_token"),buycode,ph));
	}

	// H5申请调研页面
	public static Result applyh() {
		return ok(views.html.H5.applyh.render());
	}

	// H5调研保存
	public Result saveapply() {
		Map<String, String> ap = new HashMap<String, String>();
		ap.put("uname", Form.form().bindFromRequest().get("uname"));
		ap.put("sex", Form.form().bindFromRequest().get("sexh"));
		ap.put("yearold", Form.form().bindFromRequest().get("yearold"));
		ap.put("oper", Form.form().bindFromRequest().get("oper"));
		ap.put("wx", Form.form().bindFromRequest().get("wx"));
		ap.put("phone", Form.form().bindFromRequest().get("phone"));
		ap.put("income", Form.form().bindFromRequest().get("incomeh"));
		ap.put("remark", Form.form().bindFromRequest().get("remark"));

		if (ap.size() > 0) {
			Question quest = new Question();
			quest.setAddtime(new Date());
			quest = endorseService.saveQuestion(quest);
			if (quest.getId() > 0L) {
				Iterator it = ap.keySet().iterator();
				while (it.hasNext()) {
					String key;
					String value;
					key = it.next().toString();
					value = ap.get(key);
					Qanswer an = new Qanswer();
					an.setPid(quest.getId());
					an.setQkey(key);
					an.setQValues(value);
					endorseService.saveAnswer(an);
				}
			}
		}

		return ok(views.html.H5.applysuccess.render());
	}

	// H5保存成功页面
	public static Result applysuccess() {
		return ok(views.html.H5.applysuccess.render());
	}

	private static String byteToHex(final byte[] hash) {
		Formatter formatter = new Formatter();
		for (byte b : hash) {
			formatter.format("%02x", b);
		}
		String result = formatter.toString();
		formatter.close();
		return result;
	}

	// 120计划
	public static Result star120() {
		Long uid = Numbers.parseLong(
				AjaxHellper.getHttpParam(request(), "uid"), 0L);

		if (uid == 0L) {
			return ok(views.html.H5.pageError.render());
		}

		return ok(views.html.H5.star120.render(uid));
	}

	public Result login_code() {
		response().setContentType("application/json;charset=utf-8");
		Long uid = Numbers.parseLong(Form.form().bindFromRequest().get("uid"),
				0L);
		String code = Form.form().bindFromRequest().get("code");
		ObjectNode result = Json.newObject();
		// result.put("status", "1");
		if (uid == 0L || StringUtils.isBlank(code)) {
			result.put("status", "0");
			return ok(Json.toJson(result));
		}
		User user = userService.getUserByUid(uid);
		if (user == null) {
			result.put("status", "0");
			return ok(Json.toJson(result));
		}

		boolean loginsuc = userService.usesecretcode(uid, code);
		if (loginsuc)
			result.put("status", "1");
		else
			result.put("status", "0");

		return ok(Json.toJson(result));
	}

	public Result showit() {
		String uid = AjaxHellper.getHttpParam(request(), "uid");

		String urls = request().path();
		logger.info(urls + "=====");
		if (StringUtils.isBlank(urls)) {
			return ok(views.html.H5.pageError.render());
		}
		urls = urls.replace("/H5/", "");
		String pid = "";

		String domains = StringUtil.getDomainH5();

		String endurl = domains + "/H5/show?pid=";

		switch (urls) {
		case "11uhBipZ":
			pid = "3487";
			break;
		case "26rVDeJl":
			pid = "3487";
			break;
		case "81WwtFTD":
			pid = "3487";
			break;
		case "75WvneHV":
			pid = "3487";
			break;
		case "70Jnvpoz":
			pid = "3487";
			break;
		case "70dTgbPV":
			pid = "3487";
			break;
		case "70guELTi":
			pid = "3487";
			break;
		case "70JVKNId":
			pid = "3487";
			break;
		case "70dgxUgX":
			pid = "3487";
			break;
		case "70EHzyUe":
			pid = "3487";
			break;
		default:
			return ok(views.html.H5.pageError.render());
			// break;
		}
		return ok(views.html.H5.showit.render(endurl + pid, urls, uid));
	}

	public static Result saveimg() {
		String img = Form.form().bindFromRequest().get("img");
		HttpClient httpclient = new DefaultHttpClient();
		String mcode = Form.form().bindFromRequest().get("mcode");
		String uid = Form.form().bindFromRequest().get("uid");
		ObjectNode r = Json.newObject();
		if (StringUtils.isBlank(mcode) || StringUtils.isBlank(uid)) {
			r.put("status", "0");
		}
		OutputStream os = null;
		try {
			String access_token = "";// session("sign_access_token")==null?"":session("sign_access_token");//"AxYV6mYhMziTt0--3jQ_0QzXzF0SJh7kq_Ko-t0FE2cgnFhAh8M3TkdLmMJAh-IhgFrGNGMA1XNQeaJTBV4OIo63nOwvGYCqk1DpewX6R7M";//
			logger.info("access_token-------------------------" + access_token);
			if (StringUtils.isBlank(access_token)) {
				String gettokenurl = "https://api.weixin.qq.com/cgi-bin/token?grant_type=client_credential&appid="
						+ Constants.WXappID
						+ "&secret="
						+ Constants.WXappsecret;
				TenpayHttpClient httpClientr = new TenpayHttpClient();
				httpClientr.setReqContent(gettokenurl);
				String resContent = "";

				if (httpClientr.callHttpPost(gettokenurl, "")) {
					resContent = httpClientr.getResContent();
					Logger.info("微信请求access_token返回：" + resContent);
					JSONObject json = JSONObject.fromObject(resContent);
					try {
						access_token = json.getString("access_token");
						session().put("sign_access_token", access_token);
					} catch (Exception ee) {
					}
				}
			}

			String url = "http://file.api.weixin.qq.com/cgi-bin/media/get?access_token="
					+ access_token + "&media_id=" + img;
			HttpGet httpget = new HttpGet(url);
			HttpResponse response = httpclient.execute(httpget);
			HttpEntity entity = response.getEntity();
			Header[] hs = response.getHeaders("Content-disposition");
			if (hs != null) {
				for (int i = 0; i < hs.length; i++) {
					logger.info(hs[i].getValue() + "0000000000");
				}
			}
			if (entity != null
					&& entity.getContentType().toString().indexOf("image/jpeg") >= 0) {

				logger.info(entity.getContentType().toString());
				logger.info(String.valueOf(entity.getContentLength()));
				InputStream instream = entity.getContent();
				String path = Configuration.root().getString("oss.upload.card",
						"upload/address/");// 上传路径

				String BUCKET_NAME = Configuration.root().getString(
						"oss.bucket.name.higouAPIDev", "higou-api");

				String filepath = OSSUtils.uploadFile(instream, path + mcode
						+ "/", String.valueOf(System.currentTimeMillis()),
						entity.getContentLength(), ".jpg", BUCKET_NAME);
				//
				if (!StringUtils.isBlank(filepath)) {
					r.put("status", "1");
					r.put("filepath", filepath);
				}
			} else
				r.put("status", "0");
		} catch (Exception e) {
		} finally {
			httpclient.getConnectionManager().shutdown();
		}
		return ok(Json.toJson(r));
	}

	// 保存由H5支付时创建的地址信息
	public Result saveOrderAddress() {
		response().setContentType("application/json;charset=utf-8");
		String facecard = AjaxHellper.getHttpParamOfFormUrlEncoded(request(),
				"file1");
		String backcard = AjaxHellper.getHttpParamOfFormUrlEncoded(request(),
				"file2");
		String aid = AjaxHellper.getHttpParamOfFormUrlEncoded(request(), "aid");

		String uid = AjaxHellper.getHttpParamOfFormUrlEncoded(request(), "uid");
		StringBuilder imgpathSb = new StringBuilder("");
		if (!StringUtils.isBlank(facecard)) {
			imgpathSb.append(facecard);
		}
		if (!StringUtils.isBlank(backcard)) {
			if (!StringUtils.isBlank(imgpathSb.toString())) {
				imgpathSb.append(",").append(backcard);
			} else {
				imgpathSb.append(backcard);
			}
		}
		ObjectNode result = Json.newObject();
		if (StringUtils.isBlank(aid) || StringUtils.isBlank(uid)) {
			result.put("status", "0");
			return ok(Json.toJson(result));
		}
		Address addressEntity = addressService.findByAddressId(Numbers
				.parseLong(aid, 0L));
		if (addressEntity == null
				|| addressEntity.getAddressId().compareTo(
						Numbers.parseLong(aid, 0L)) != 0) {
			result.put("status", "0");
			return ok(Json.toJson(result));
		}
		addressEntity.setImgpath(imgpathSb.toString());
		addressEntity.setCardImg(1);// 已上传身份证
		addressEntity = addressService.saveAddress(addressEntity);

		if (addressEntity != null && addressEntity.getAddressId() != null) {
			result.put("status", "1");
			result.put("aid", addressEntity.getAddressId());
		} else {
			result.put("status", "0");
		}
		return ok(Json.toJson(result));
	}

	/* H5正常分享页面购买相关///////////////////////////////////////////////////////////// */
	public static Result pro() {
		String pid = AjaxHellper.getHttpParam(request(), "pid");
		String phone = AjaxHellper.getHttpParam(request(), "phone");
		if (StringUtils.isBlank(pid)) {
			return ok(views.html.H5.pageError.render());
		}
		String url = "/H5/product";
		return ok(views.html.H5.pro.render(url, pid, phone));
	}

	public Result product() {
		Long pid = Numbers.parseLong(
				AjaxHellper.getHttpParam(request(), "pid"), Long.valueOf(0));
		String domainH5 = StringUtil.getDomainH5();
		String wx = AjaxHellper.getHttpParam(request(), "wx");
		wx = StringUtils.isBlank(wx) ? "" : wx;
		String flg = "voke";// AjaxHellper.getHttpParam(request(), "flg");
		// flg=StringUtils.isBlank(flg)?"voke":"orderlist";

		String openid = AjaxHellper.getHttpParam(request(), "op");
		String unionid = AjaxHellper.getHttpParam(request(), "un");
		openid = StringUtils.isBlank(openid) ? "" : openid;
		unionid = StringUtils.isBlank(unionid) ? "" : unionid;
		Long uid = Numbers.parseLong(session("uid"), 0L);

		WxSign addrSign = null;
		if (!StringUtils.isBlank(session("op")))
			addrSign = H5ShoppingController.getcacheWxsign();

		if (addrSign != null) {
			String addressstr = StringUtil.getWXaddressSign(
					addrSign.getAccess_token(), StringUtil.getDomainH5()
							+ "/H5/orderprocartcrm?code=" + addrSign.getCode()
							+ "&state=" + addrSign.getState(),
					addrSign.getTimstr(), addrSign.getNostr());
			addrSign.setSign(addressstr);
		}
		String phone = AjaxHellper.getHttpParam(request(), "phone");
		if (StringUtils.isBlank(phone))
			phone = "";

		if (pid == 0L) {
			return ok(views.html.H5.pageError.render());
		}

		User usr = new User();
		usr.setUid(uid);
		usr.setOpenId(openid);
		usr.setUnionid(unionid);

		Integer cartcount = 0;

		if (uid.longValue() > 0) {
			cartcount = shoppingCartService.gettotalNum(uid, 0L);
			if (cartcount == null)
				cartcount = 0;
		}
		// 取商品
		Product proinfo = productService.getProductById(pid);
		List<Product_images> imgplist=productService.getProductImages(pid);
		if(imgplist!=null && !imgplist.isEmpty())
			proinfo.setListpic(imgplist.get(0).getPicname());

		if (proinfo == null)
			return ok(views.html.H5.pageError.render());

		if (!StringUtils.isBlank(proinfo.getWayremark()))
			proinfo.setWayremark("由"
					+ proinfo.getWayremark().replace("_", "发往"));
		proinfo.setFromobj(productService.queryFnamyByFromSite(proinfo
				.getFromsite()));

		DecimalFormat df = new DecimalFormat("0");
		int weightfee = proinfo.getFromobj().getFee();
		if (proinfo.getWeight() * 1000 > 500) {
			Double wf = proinfo.getFromobj().getFee()
					+ ((proinfo.getWeight() * 1000 - 500) / 100)
					* proinfo.getFromobj().getAddfee();
			BigDecimal b = new BigDecimal(String.valueOf(wf));
			weightfee = Integer.valueOf(b.setScale(0, BigDecimal.ROUND_CEILING)
					.toString());
		}

		if (proinfo.getRmbprice() == null)
			proinfo.setRmbprice(Double.valueOf(0));
		if (proinfo.getEndorsementPrice() == null)
			proinfo.setEndorsementPrice(Double.valueOf(0));

		proinfo.setRmbprice(new BigDecimal(proinfo.getRmbprice()).setScale(2,
				BigDecimal.ROUND_CEILING).doubleValue());
		proinfo.setList_price(proinfo.getList_price());

		if (!StringUtils.isBlank(proinfo.getNationalFlag())) {
			proinfo.setNationalFlag("http://ht.neolix.cn/pimgs/site/"
					+ proinfo.getNationalFlag());
		} else
			proinfo.setNationalFlag(null);

		if (proinfo.getTyp().equals("2")) {
			// 算折扣
			if (proinfo.getRmbprice() > 0 && proinfo.getList_price() > 0) {
				if (proinfo.getChinaprice() != null
						&& proinfo.getChinaprice().doubleValue() > 0) {
					BigDecimal mData = new BigDecimal(10
							* proinfo.getRmbprice() / proinfo.getChinaprice())
							.setScale(1, BigDecimal.ROUND_UP);
					proinfo.setExtcode(mData.toString());
				} else {
					proinfo.setExtcode("");
				}
			} else
				proinfo.setExtcode("");
		} else {
			Currency currency = productService.queryCurrencyById(proinfo
					.getCurrency());
			BigDecimal rate = new BigDecimal(currency.getRate() / 100)
					.setScale(4, BigDecimal.ROUND_CEILING);
			BigDecimal price = new BigDecimal(proinfo.getPrice() / 100)
					.setScale(2, BigDecimal.ROUND_CEILING);
			BigDecimal rmb_price = rate.multiply(price).setScale(0,
					BigDecimal.ROUND_CEILING);
			proinfo.setRmbprice(Double.valueOf(rmb_price.toString()));
			if (proinfo.getChinaprice() > 0) {
				BigDecimal mData = new BigDecimal(10 * proinfo.getRmbprice()
						/ proinfo.getChinaprice()).setScale(1,
						BigDecimal.ROUND_UP);
				proinfo.setExtcode(mData.toString());
			} else
				proinfo.setExtcode("");

		}
		Page<Comment> comments = commentService.commentPage(0, pid);
		List<Comment> listcomment = null;
		if (comments != null) {
			listcomment = comments.getContent();
			if (listcomment != null && !listcomment.isEmpty()) {
				for (Comment c : listcomment) {
					if (StringUtils.isBlank(c.getHeadIcon()))
						c.setHeadIcon(CdnAssets.CDN_API_PUBLIC_URL
								+ "images/sheSaidImages/H5_boy_48x48.png");
				}
			}
		}

		proinfo.setPostfee(weightfee);

		// 取税率等
		proinfo.setProUnion(productService.getproUnion(pid));

		if (proinfo.getIshot() == 1) {
			proinfo.setNstock(productService.dealNstockWithProduct(proinfo
					.getPid()));
		}

		Fromsite fromsite = productService.queryFnamyByFromSite(proinfo
				.getFromsite());
		String fname = fromsite.getName();
		String fromsitemsg = "";

		if ("嗨个购".equals(fname)) {
			fromsitemsg = "（5天内到货）";
		} else {
			if ("日本亚马逊".equals(fname)) {
				fromsitemsg = "（20天左右到货）";
			} else {
				fromsitemsg = "（15天左右到货）";
			}
		}

		proinfo.setRtitle(fromsitemsg);
		// 取产品详情
		List<ProductDetail> pdlist = productService.getdetailist(proinfo
				.getSkucode());
		// 判断商品预售时间到否
		if (proinfo.getPtyp().equals("4")) {
			String nowstr = CHINESE_DATE_TIME_FORMAT_NORMAL.format(new Date());
			if (proinfo.getBtim().compareTo(nowstr) >= 0)
				proinfo.setPtyp("2");
		}
		// 取多规格产品
		List<Product> pplist = null;
		// if(proinfo.getPpid().longValue()!=proinfo.getPid().longValue()){
		pplist = productService.findPproductByPid(proinfo.getPpid(), "10");
		// }
		if (pplist == null || pplist.isEmpty()) {
			pplist = new ArrayList<Product>();
			List<Product_images> imglist = productService.getProductImages(pid);
			if (imglist == null || imglist.isEmpty())
				proinfo.setSpecpic(proinfo.getListpic());
			else
				proinfo.setSpecpic(imglist.get(0).getFilename());

			pplist.add(proinfo);
		} else {
			List<Product> tplist = new ArrayList<Product>();
			for (Product tp : pplist) {
				if (tp.getIshot() == 1) {
					tp.setNstock(productService.dealNstockWithProduct(tp
							.getPid()));
				}
				if (tp.getPid().longValue() == proinfo.getPid().longValue()) {
					tp = proinfo;
					tp.setSpecpic(proinfo.getListpic());
					tp.setSpecifications(proinfo.getTitle());
				} else {
					List<Product_images> imglist = productService
							.getProductImages(pid);
					if (StringUtils.isBlank(tp.getSpecpic())) {
						if (imglist == null || imglist.isEmpty())
							tp.setSpecpic(tp.getListpic());
						else
							tp.setSpecpic(imglist.get(0).getFilename());
					}
					if (StringUtils.isBlank(tp.getSpecifications()))
						tp.setSpecifications(tp.getTitle());
				}

				if (tp.getTyp().equals("2")) {
					// 算折扣
					if (tp.getRmbprice() > 0 && tp.getList_price() > 0) {
						if (tp.getChinaprice() != null
								&& tp.getChinaprice().doubleValue() > 0) {
							BigDecimal mData = new BigDecimal(10
									* tp.getRmbprice() / tp.getChinaprice())
									.setScale(1, BigDecimal.ROUND_UP);
							tp.setExtcode(mData.toString());
						} else {
							tp.setExtcode("");
						}
					} else
						tp.setExtcode("");
				} else {
					Currency currency = productService.queryCurrencyById(tp
							.getCurrency());
					BigDecimal rate = new BigDecimal(currency.getRate() / 100)
							.setScale(4, BigDecimal.ROUND_CEILING);
					BigDecimal price = new BigDecimal(tp.getPrice() / 100)
							.setScale(2, BigDecimal.ROUND_CEILING);
					BigDecimal rmb_price = rate.multiply(price).setScale(0,
							BigDecimal.ROUND_CEILING);
					tp.setRmbprice(Double.valueOf(rmb_price.toString()));
					if (tp.getChinaprice() > 0) {
						BigDecimal mData = new BigDecimal(10 * tp.getRmbprice()
								/ tp.getChinaprice()).setScale(1,
								BigDecimal.ROUND_UP);
						tp.setExtcode(mData.toString());
					} else
						tp.setExtcode("");

				}
				// 下期去掉
				tp.setRmbprice(new BigDecimal(tp.getRmbprice()).setScale(0,
						BigDecimal.ROUND_UP).doubleValue());
				tplist.add(tp);
			}
			pplist = tplist;
		}

		// if(Numbers.parseInt((proinfo.getRmbprice().doubleValue()+"").substring((proinfo.getRmbprice().doubleValue()+"").indexOf(".")+1),0)==0){
		// proinfo.setRmbprice(new BigDecimal(proinfo.getRmbprice()).setScale(0,
		// BigDecimal.ROUND_UP).doubleValue());
		// }
		String ptim = this.CHINESE_DATE_TIME_FORMAT_NORMAL.format(new Date());
		// H5分享调用微信获取TOKEN，jsp_tackit
		WxSign wxs = this.getwxstr();
		wxs.setShareurl(domainH5 + "/H5/pro?pid=" + pid);
		wxs.setShareimg(proinfo.getListpic());
		if (proinfo.getLimitcount() > proinfo.getNstock())
			proinfo.setLimitcount(Numbers.parseInt(
					String.valueOf(proinfo.getNstock().longValue()), 0));

		// 设置输出状态
		String sta = "1";// 设置个状态显示办理用,1显示立即买，11显示立即购买和加购物车，2抢光及下架，3定时及预售,4新人购买,00元购买
		if (proinfo.getProUnion() == null
				|| !proinfo.getProUnion().getBuyNowFlag().equals("1"))
			sta = "11";
		if (proinfo.getStatus() != 10 || proinfo.getNstock().longValue() < 1)
			sta = "2";
		if (proinfo.getStatus() == 10 && proinfo.getNstock().longValue() > 0) {
			if (proinfo.getPtyp() != null
					&& (proinfo.getPtyp().equals("3") || (proinfo.getPtyp()
							.equals("4") && proinfo.getBtim() != null && proinfo
							.getBtim().compareTo(
									CHINESE_DATE_TIME_FORMAT_NORMAL.format(
											new Date()).toString()) >= 0)))
				sta = "3";

			if (!proinfo.getNewMantype().equals("0"))
				sta = "4";
			if (proinfo.getRmbprice().intValue() == 0)
				sta = "0";// 0元购买
		}
		String wxtoken = "";
		if (uid.longValue() > 0
				&& !StringUtils.isBlank(cache.get("wx_access_token_voke"
						+ session("op"))))
			wxtoken = cache.get("wx_access_token_voke" + session("op"));
		String redirecturl = java.net.URLEncoder.encode(StringUtil
				.getDomainH5() + "/H5/vokewxpro?pid=" + pid);
		// 下期去掉
		proinfo.setRmbprice(new BigDecimal(proinfo.getRmbprice()).setScale(0,
				BigDecimal.ROUND_UP).doubleValue());
		return ok(views.html.H5.product.render(usr, proinfo, listcomment,
				pdlist, wxs, phone, cartcount, pplist, flg, sta, redirecturl,
				Constants.WXappID, addrSign, wxtoken));
	}

	// 微信鉴权获取用户信息(正常商品使用)
	public Result wxauthpro() {
		Long pid = Numbers.parseLong(
				AjaxHellper.getHttpParam(request(), "pid"), 0L);
		String flg = AjaxHellper.getHttpParam(request(), "flg");
		flg = StringUtils.isBlank(flg) ? "order" : "praise";
		String domains = StringUtil.getDomainH5();
		String phone = AjaxHellper.getHttpParam(request(), "phone");
		String uid = AjaxHellper.getHttpParam(request(), "uid");
		if (StringUtils.isBlank(phone))
			phone = "";

		if (pid == 0L) {
			return ok(views.html.sheSaid.pageError.render());
		}
		String access_token = "";
		String openid = "";
		String unionid = "";

		String redirecturl = java.net.URLEncoder.encode(StringUtil
				.getDomainH5() + "/H5/vokewxpro?pid=" + pid + "&uid=" + uid);
		String wxauthURL = "https://open.weixin.qq.com/connect/oauth2/authorize?appid="
				+ Constants.WXappID
				+ "&redirect_uri="
				+ redirecturl
				+ "&response_type=code&scope=snsapi_base&state=123&connect_redirect=1#wechat_redirect";
		// String
		// wxauthURL="https://open.weixin.qq.com/connect/oauth2/authorize?appid="+Constants.WXappID+"&redirect_uri="+redirecturl+"&response_type=code&scope=snsapi_userinfo&state=123#wechat_redirect";
		return redirect(wxauthURL);
	}

	// 微信鉴权（加购物车使用）
	public Result wxauthprocart() {
		Long pid = Numbers.parseLong(
				AjaxHellper.getHttpParam(request(), "pid"), 0L);
		String flg = AjaxHellper.getHttpParam(request(), "flg");
		flg = StringUtils.isBlank(flg) ? "voke" : flg;
		logger.info("flgwxxxxxxxxxx-------------" + flg);
		String domains = StringUtil.getDomainH5();

		// if(pid==0L){
		// return ok(views.html.H5.pageError.render());
		// }
		String access_token = "";
		String openid = "";
		String unionid = "";

		String redirecturl = java.net.URLEncoder
				.encode(StringUtil.getDomainH5() + "/H5/vokewxprocart?pid="
						+ pid + "&flg=" + flg);
		String wxauthURL = "https://open.weixin.qq.com/connect/oauth2/authorize?appid="
				+ Constants.WXappID
				+ "&redirect_uri="
				+ redirecturl
				+ "&response_type=code&scope=snsapi_base&state=123&connect_redirect=1#wechat_redirect";
		// String
		// wxauthURL="https://open.weixin.qq.com/connect/oauth2/authorize?appid="+Constants.WXappID+"&redirect_uri="+redirecturl+"&response_type=code&scope=snsapi_userinfo&state=123#wechat_redirect";
		return redirect(wxauthURL);
	}

	// 微信鉴权回调
	// 微信鉴权回调 通过鉴权后的处理
	public Result vokewxpro() {
		Long uid = Numbers.parseLong(
				AjaxHellper.getHttpParam(request(), "uid"), 0L);
		Long pid = Numbers.parseLong(
				AjaxHellper.getHttpParam(request(), "pid"), 0L);
		String phone = AjaxHellper.getHttpParam(request(), "phone");
		if (StringUtils.isBlank(phone) || phone.toLowerCase().equals("null"))
			phone = "";

		if (pid == 0L) {
			return ok(views.html.sheSaid.pageError.render());
		}
		// 先进行基础授权，如果是第一次没有用户妮称等信息则进行用户级授权
		String openid = "";
		String unionid = "";
		String access_token = "";
		String code = "";
		String state = "";
		
		WxSign tmpsign=H5ShoppingController.getcacheWxsign();
		if(tmpsign==null || StringUtils.isBlank(tmpsign.getAccess_token())){
			JsonNode res = H5ShoppingController.getwxtoken();
			if (res != null) {
				access_token = res.get("access_token").textValue();
				openid = res.get("openid").textValue();
				unionid = res.get("unionid").textValue();
				code = res.get("code").textValue();
				state = res.get("state").textValue();
			}
		}else{
			access_token=tmpsign.getAccess_token();
			openid=tmpsign.getOpenid();
			unionid=tmpsign.getUnionid();
			code=tmpsign.getCode();
			state=tmpsign.getState();
		}
		User usert = null;
		if (usert == null) {
			usert = new User();
			usert.setOpenId("");
			usert.setUnionid("");
		}

		access_token = access_token == null ? "" : access_token;
		openid = openid == null ? "" : openid;
		unionid = unionid == null ? "" : unionid;

		User ust = null;

		// 检查并获取用户基本信息
		if (!access_token.equals("") && !openid.equals("")
				&& !unionid.equals("")) {
			session("wx_access_token", access_token);
			session("unionid", unionid);
			session("openid", openid);
			// 如果用户存在则跳到购物页面
			ust = userService.getUserByopenid(openid, unionid);
		}
		if (ust == null || ust.getUid() == 0L) {
			usert.setUid(Long.valueOf("0"));
			usert.setUnionid(unionid);
			usert.setOpenId(openid);
		} else {
			usert.setOpenId(openid);
			usert.setUnionid(unionid);
			usert.setUid(ust.getUid());
			session("uid", ust.getUid().longValue() + "");
		}

		String buycode = "H5";
		if (!StringUtils.isBlank(phone))
			buycode = "kuaige";
		session("op", openid);
		session("un", unionid);
		return redirect("/H5/orderpro?pid=" + pid + "&code=" + code + "&state="
				+ state);
	}

	// 微信鉴权回调
	public Result vokewxprocart() {
		Long pid = Numbers.parseLong(
				AjaxHellper.getHttpParam(request(), "pid"), 0L);
		String flg = AjaxHellper.getHttpParam(request(), "flg");
		flg = StringUtils.isBlank(flg) ? "voke" : flg;

		String code = AjaxHellper.getHttpParam(request(), "code");
//		if (code == null || code.equals("")) {
//			// 用户取消或景鉴权失败
//			redirect("/H5/product?pid=" + pid);
//		}
		User usert = null;
		if (usert == null) {
			usert = new User();
			usert.setOpenId("");
			usert.setUnionid("");
		}

		// 先进行基础授权，如果是第一次没有用户妮称等信息则进行用户级授权
		String openid = "";
		String unionid = "";
		String access_token = "";
		String state = "";
		
		WxSign tmpsign=H5ShoppingController.getcacheWxsign();
		if(tmpsign==null || StringUtils.isBlank(tmpsign.getAccess_token())){
			JsonNode res = H5ShoppingController.getwxtoken();
			if (res != null) {
				access_token = res.get("access_token").textValue();
				openid = res.get("openid").textValue();
				unionid = res.get("unionid").textValue();
				code = res.get("code").textValue();
				state = res.get("state").textValue();
			}
		}else{
				access_token=tmpsign.getAccess_token();
				openid=tmpsign.getOpenid();
				unionid=tmpsign.getUnionid();
				code=tmpsign.getCode();
				state=tmpsign.getState();
				session("op",openid);
				session("un",unionid);
			}
		access_token = access_token == null ? "" : access_token;
		openid = openid == null ? "" : openid;
		unionid = unionid == null ? "" : unionid;

		User ust = null;

		// 检查并获取用户基本信息
		if (!access_token.equals("") && !openid.equals("")
				&& !unionid.equals("")) {
			session("wx_access_token", access_token);
			session("unionid", unionid);
			session("openid", openid);

			// 如果用户存在则跳到购物车列表页面
			ust = userService.getUserByopenid(openid, unionid);
		}
		if (ust == null || ust.getUid() == 0L) {
			usert.setUid(Long.valueOf("0"));
			usert.setUnionid(unionid);
			usert.setOpenId(openid);
		} else {

			usert.setOpenId(openid);
			usert.setUnionid(unionid);
			usert.setUid(ust.getUid());
			session("uid", usert.getUid().longValue() + "");
			if (flg.equals("voke")) {
				// 添加购物车
				ObjectNode result = (ObjectNode) this.addCart(String.valueOf(usert.getUid().longValue()),String.valueOf(pid.longValue()), 1);
				if (result.get("status").textValue().equals("1"))
					return redirect("/H5/orderprocart?pid=" + pid);
				else
					return redirect("/H5/orderprocart?msg="+ URLEncoder.encode(result.get("msg").textValue()) + "&pid=" + pid);
			} else if (flg.equals("orderlist")) {
				return redirect("/H5/orderprocart?pid=" + pid);
			}
		}
		if(flg.equals("porderlist"))
			return redirect("/H5/shoplist?v=1");
		else
			return redirect("/H5/product?pid=" + pid + "&op=" + usert.getOpenId()+ "&un=" + usert.getUnionid() + "&mcode=cart&flg=" + flg);
	}

	// 登录添加购物车
	public Result addshoppingCart() {
		String uid = session("uid");// AjaxHellper.getHttpParamOfFormUrlEncoded(request(),
									// "uid");
		String pid = AjaxHellper.getHttpParamOfFormUrlEncoded(request(), "pid");
		Integer cntInt = Numbers.parseInt(
				AjaxHellper.getHttpParamOfFormUrlEncoded(request(), "cnt"), 1);

		return ok(this.addCart(uid, pid, cntInt));
	}

	// 添加购物车
	public ObjectNode addCart(String uid, String pid, Integer cntInt) {
		ObjectNode result = Json.newObject();
		result.put("status", "0");
		Product product = productService.getProductById(Numbers.parseLong(pid,
				0L));
		if (product != null) {
			if ("1".equals(product.getNewMantype())) {// 首购商品
				// 判断用户是否是首次下单
				boolean flag = userService.checkFirstFlag(uid);
				if (flag == false) {
					result.put("status", "2");
					Integer counts = shoppingCartService.gettotalNum(
							Numbers.parseLong(uid, 0L), 0L) == null ? 0
							: shoppingCartService.gettotalNum(
									Numbers.parseLong(uid, 0L),
									Numbers.parseLong(pid, 0L));
					result.put("totalcount", counts + "");
					result.put("msg", "该商品仅限新人购买");
					return result;
				} else {
					boolean checkFlag = shoppingCartService
							.checkShoppingCart_newMan(
									Numbers.parseLong(uid, 0L),
									Numbers.parseLong(pid, 0L));// 购物车是否已存在新人商品
					if (checkFlag) {
						result.put("status", "2");
						Integer counts = shoppingCartService.gettotalNum(
								Numbers.parseLong(uid, 0L),
								Numbers.parseLong(pid, 0L));
						result.put("totalcount", counts + "");
						result.put("msg", "新人商品仅限购买一件");
						return result;
					}
					// 判断当前的
					Integer pcounts = shoppingCartService.gettotalNum(
							Numbers.parseLong(uid, 0L),
							Numbers.parseLong(pid, 0L));
					if (pcounts != null && pcounts > 0) {
						Integer counts = shoppingCartService.gettotalNum(
								Numbers.parseLong(uid, 0L), 0L);
						result.put("totalcount", counts + "");
						result.put("msg", "新人商品仅限购买一件");
						return result;
					}
				}
			}
			if ("3".equals(product.getNewMantype())) {// 0元商品
				// 判断用户是否是首次购买0元商品
				boolean flag = userService.checkBuyOrNotFlag(uid, pid);
				if (flag == false) {
					result.put("status", "2");
					Integer counts = shoppingCartService.gettotalNum(
							Numbers.parseLong(uid, 0L), 0L) == null ? 0
							: shoppingCartService.gettotalNum(
									Numbers.parseLong(uid, 0L), 0L);
					result.put("totalcount", counts + "");
					result.put("msg", "该商品只能购买一件");
					return result;
				} else {
					boolean checkFlag = shoppingCartService
							.checkShoppingCart_newManZero(
									Numbers.parseLong(uid, 0L),
									Numbers.parseLong(pid, 0L));// 购物车是否已存在0元商品
					if (checkFlag) {
						result.put("status", "2");
						Integer counts = shoppingCartService.gettotalNum(
								Numbers.parseLong(uid, 0L), 0L) == null ? 0
								: shoppingCartService.gettotalNum(
										Numbers.parseLong(uid, 0L), 0L);
						result.put("totalcount", counts + "");
						result.put("msg", "该商品只能购买一件");
						return result;
					}
				}
			}

			int limit = product.getLimitcount();
			Long nstock = product.getNstock();
			if (product.getIshot() == 1) {
				nstock = productService.dealNstockWithProduct(product.getPid());
			}

			Integer totalcount = shoppingCartService.gettotalNum(
					Numbers.parseLong(uid, 0L), Numbers.parseLong(pid, 0L));
			if (totalcount == null) {
				totalcount = 0;
			}
			Integer counts = shoppingCartService.gettotalNum(
					Numbers.parseLong(uid, 0L), 0L);
			if (counts == null) {
				counts = 0;
			}
			if (nstock <= 0) {
				result.put("status", "4");
				result.put("totalcount", counts + "");
				result.put("msg", "该商品已售罄");
				return result;
			}
			if (totalcount >= nstock) {
				result.put("status", "3");
				result.put("totalcount", counts + "");
				result.put("msg", "您已超出库存数量");
				return result;
			}
			if (totalcount >= limit) {
				result.put("status", "2");
				result.put("totalcount", counts + "");
				result.put("msg", "您已超出限购数量");
				return result;
			}
			if (limit == 0) {
				result.put("status", "2");
				result.put("totalcount", "0");
				result.put("msg", "您已超出限购数量");
				return result;
			}
			if (cntInt >= limit) {
				cntInt = limit;
			}
			if (totalcount > 0) {
				ShoppingCart shoppingCart = shoppingCartService
						.getShoppingCartByUIdAndPId(Numbers.parseLong(uid, 0L),
								Numbers.parseLong(pid, 0L));
				shoppingCart.setCounts(shoppingCart.getCounts() + cntInt);
				shoppingCartService.saveShoppingCart(shoppingCart);
			} else {
				ShoppingCart shoppingCart = new ShoppingCart();
				shoppingCart.setCounts(cntInt);
				shoppingCart.setuId(Numbers.parseLong(uid, 0L));
				shoppingCart.setpId(Numbers.parseLong(pid, 0L));
				shoppingCart.setDate_add(new Date());
				shoppingCartService.saveShoppingCart(shoppingCart);
			}

			List<Object> shoppingCartList = shoppingCartService
					.getShoppingCart_list(Numbers.parseLong(uid, 0L), 0);

			if (shoppingCartList == null || shoppingCartList.isEmpty()) {
				result.put("status", "9");//
				result.put("msg", "该商品已售罄");
				return result;
			}
			Double totalfee = 0D;
			List<Long> pidlist = new ArrayList<Long>();
			for (Object vo : shoppingCartList) {
				JsonNode jss = Json.toJson(vo);
				if (jss != null) {
					ShoppingCartItemVO s = new ShoppingCartItemVO();
					if (jss.get("pid") != null)
						s.pid = jss.get("pid").textValue();

					if (jss.get("rmbprice") != null)
						s.rmbprice = jss.get("rmbprice").textValue();
					if (jss.get("counts") != null)
						s.counts = jss.get("counts").textValue();

					if (!StringUtils.isBlank(s.pid)) {
						pidlist.add(Numbers.parseLong(s.pid, 0L));
						// s.rmbprice=new
						// BigDecimal(s.rmbprice).setScale(2,BigDecimal.ROUND_CEILING).toString();
						if (!StringUtils.isBlank(s.rmbprice)
								&& s.rmbprice.indexOf(".") >= 0) {
							if (Numbers.parseInt(s.rmbprice
									.substring(s.rmbprice.indexOf(".") + 1), 0) == 0)
								s.rmbprice = s.rmbprice.substring(s.rmbprice
										.indexOf("."));
						}
						totalfee = totalfee
								+ Numbers.parseDouble(s.rmbprice, 0D)
								* Numbers.parseDouble(s.counts, 0D);
					}
				}
			}

			// 累计税费，邮费
			if (pidlist != null && !pidlist.isEmpty()) {
				Double costfee = productService.getRateFee(pidlist.toString()
						.substring(1, pidlist.toString().indexOf("]") + 1),
						Numbers.parseLong(uid, 0L));
				Double postfee = productService.getWeightFee(pidlist,
						Numbers.parseLong(uid, 0L));
				totalfee = totalfee + costfee + postfee;
			}
			totalfee = new BigDecimal(totalfee).setScale(2,
					BigDecimal.ROUND_HALF_DOWN).doubleValue();
			String tfee = totalfee.doubleValue() + "";

			if (!StringUtils.isBlank(tfee) && tfee.indexOf(".") >= 0) {
				if (Integer.valueOf(tfee.substring(tfee.indexOf(".") + 1)) == 0)
					tfee = tfee.substring(0, tfee.indexOf("."));
			}
			result.put("status", "1");
			result.put("tfee", tfee);
			result.put(
					"totalcount",
					shoppingCartService.gettotalNum(Numbers.parseLong(uid, 0L),
							Numbers.parseLong(pid, 0L)) + "");
			result.put("msg", "");
			return result;
		} else {
			result.put("status", "3");//
			result.put("msg", "该商品您没有权限购买");
			return result;
		}
	}

	// 删除购物车中的商品
	public Result shoppingCart_del() {
		String uid = session("uid");
		String pids = AjaxHellper.getHttpParamOfFormUrlEncoded(request(),
				"pids");

		ObjectNode result = Json.newObject();

		if (StringUtils.isBlank(uid) || StringUtils.isBlank(pids)) {
			result.put("status", "0");
			result.put("msg", "用户不存在");
			return ok(Json.toJson(result));
		}

		String[] ids = pids.split(",");
		List<Long> longs = new ArrayList<>(ids.length);
		for (int i = 0, size = ids.length; i < size; i++) {
			longs.add(Numbers.parseLong(ids[i], 0L));
		}
		shoppingCartService.deleteShoppingCartByPIds(
				Numbers.parseLong(uid, 0L), longs);
		result.put("status", "1");
		return ok(Json.toJson(result));
	}

	// 购物车列表
	public Result orderprocart() {
		// 如果没有鉴权则鉴权
		String w = AjaxHellper.getHttpParam(request(), "w");
		if (!StringUtils.isBlank(w) && w.equals("1")) {
			if (StringUtils.isBlank(session("uid"))) {
				// 微信鉴权
				String wxurl = "/H5/wxauthprocart?flg=orderlist";
				return redirect(wxurl);
			}
		}
		Long spid = Numbers.parseLong(
				AjaxHellper.getHttpParam(request(), "pid"), 0L);
		if (spid.longValue() > 0)
			session("prepid", spid.longValue() + "");

		Product spro = new Product();
		if (spid > 0) {
			spro = productService.getProductById(spid);
		}

		Long uid = Numbers.parseLong(session("uid"), 0L);

		WxSign addrSign = null;
		if (uid.longValue() > 0)
			addrSign = H5ShoppingController.getcacheWxsign();
		if (addrSign == null && !StringUtils.isBlank(session("cache_token"))) {
			addrSign = new WxSign();
			addrSign.setAppId(Constants.WXappID);
			addrSign.setTimstr(session("cache_timstr"));
			addrSign.setNostr(session("cache_nostr"));
			addrSign.setAccess_token(session("cache_token"));
			addrSign.setState(session("cache_state"));
			addrSign.setCode(session("cache_code"));
		}
		if (addrSign != null) {
			String addressstr = StringUtil.getWXaddressSign(
					addrSign.getAccess_token(), StringUtil.getDomainH5()
							+ "/H5/orderprocartcrm?code=" + addrSign.getCode()
							+ "&state=" + addrSign.getState(),
					addrSign.getTimstr(), addrSign.getNostr());
			session("iswx", "");
			addrSign.setSign(addressstr);
		}
		User user = userService.getUserByUid(uid);
		if (user == null) {
			// return ok(views.html.H5.pageError.render());
			user = new User();
			user.setUid(0L);
			user.setOpenId("");
			user.setUnionid("");
		}

		List<Object> shoppingCartList = shoppingCartService
				.getShoppingCart_list(uid, 0);

		String pid = "0";
		String pic = "";
		List<ShoppingCartItemVO> sv = new ArrayList<ShoppingCartItemVO>();
		List<ShoppingCartCategoryVO> fromwaylist = new ArrayList<ShoppingCartCategoryVO>();
		Map<String, ShoppingCartCategoryVO> fwlist = new HashMap<String, ShoppingCartCategoryVO>();

		Double totalfee = 0D;
		if (shoppingCartList != null && !shoppingCartList.isEmpty()) {
			for (Object vo : shoppingCartList) {
				JsonNode jss = Json.toJson(vo);
				if (jss != null) {
					ShoppingCartCategoryVO waycat = new ShoppingCartCategoryVO();
					ShoppingCartItemVO s = new ShoppingCartItemVO();
					if (jss.get("fromsiteimg") != null)
						waycat.setFromsiteimg(jss.get("fromsiteimg")
								.textValue());
					if (jss.get("fromsite") != null)
						waycat.setFromsite(jss.get("fromsite").textValue());
					if (jss.get("typ") != null)
						waycat.setTyp(jss.get("typ").textValue());
					if (jss.get("wayremark") != null)
						waycat.setWayremark(jss.get("wayremark").textValue());

					if (jss.get("pid") != null)
						s.pid = jss.get("pid").textValue();
					if (jss.get("title") != null)
						s.title = jss.get("title").textValue();
					if (jss.get("img") != null)
						s.img = jss.get("img").textValue();
					if (jss.get("rmbprice") != null)
						s.rmbprice = jss.get("rmbprice").textValue();
					if (jss.get("chinaprice") != null)
						s.chinaprice = jss.get("chinaprice").textValue();
					if (jss.get("fromsite") != null)
						s.fromsite = jss.get("fromsite").textValue();
					if (jss.get("iscoupon") != null)
						s.iscoupon = jss.get("iscoupon").textValue();
					if (jss.get("limitcount") != null)
						s.limitcount = jss.get("limitcount").textValue();
					if (jss.get("counts") != null)
						s.counts = jss.get("counts").textValue();
					if (jss.get("rate") != null)
						s.rate = jss.get("rate").textValue();
					if (jss.get("limitcount") != null)
						s.limitcount = jss.get("limitcount").textValue();

					if (!StringUtils.isBlank(waycat.getFromsite())
							&& !StringUtils.isBlank(waycat.getWayremark())) {
						fwlist.put(waycat.getFromsite(), waycat);
					}
					if (!StringUtils.isBlank(s.pid)) {
						if (StringUtils.isBlank(s.rmbprice))
							s.rmbprice = "0";

						// 下期去掉
						s.rmbprice = new BigDecimal(s.rmbprice).setScale(0,
								BigDecimal.ROUND_UP).toString();

						if (!StringUtils.isBlank(s.rmbprice)
								&& s.rmbprice.indexOf(".") >= 0) {
							if (Numbers.parseInt(s.rmbprice
									.substring(s.rmbprice.indexOf(".") + 1), 0) == 0)
								s.rmbprice = s.rmbprice.substring(0,
										s.rmbprice.indexOf("."));
						}

						// s.rmbprice=new
						// BigDecimal(s.rmbprice).setScale(2,BigDecimal.ROUND_CEILING).toString();
						sv.add(s);
						totalfee = totalfee
								+ Numbers.parseDouble(s.rmbprice, 0D)
								* Numbers.parseDouble(s.counts, 0D);
					}
				}
			}
		}

		if (fwlist != null && fwlist.size() > 0) {
			Iterator<String> keyIt = fwlist.keySet().iterator();
			while (keyIt.hasNext()) {
				ShoppingCartCategoryVO co = fwlist.get(keyIt.next());
				if (co != null && !StringUtils.isBlank(co.getWayremark()))
					co.setWayremark(co
							.getWayremark()
							.replace(
									"_",
									"<img src='"
											+ assets.CdnAssets
													.urlForAPIPublic("images/H5/ico-10.png")
											+ "' />"));

				fromwaylist.add(co);
			}
		}
		if (fromwaylist == null || fromwaylist.isEmpty())
			fromwaylist = null;

		if (sv == null || sv.isEmpty())
			sv = null;

		totalfee = new BigDecimal(totalfee).setScale(2,
				BigDecimal.ROUND_HALF_DOWN).doubleValue();
		String tfee = totalfee.toString();

		if (!StringUtils.isBlank(tfee) && tfee.indexOf(".") >= 0) {
			if (Integer.valueOf(tfee.substring(tfee.indexOf(".") + 1)) == 0)
				tfee = tfee.substring(0, tfee.indexOf("."));
		}
		
		
		String alertmsg = AjaxHellper.getHttpParam(request(), "msg");
		alertmsg = StringUtils.isBlank(alertmsg) ? "" : alertmsg;

		String postmark = StringUtil.getSystemConfigValue("post_fee_mark");
		postmark = StringUtils.isBlank(postmark) ? "" : postmark;
		String postmarkurl = StringUtil
				.getSystemConfigValue("post_fee_mark_url");
		postmarkurl = StringUtils.isBlank(postmarkurl) ? "" : postmarkurl;

		return ok(views.html.H5.orderprocart.render(sv, user, tfee, alertmsg, postmark, postmarkurl,fromwaylist, addrSign, StringUtils.isBlank(session("prepid")) ? "": session("prepid")));

	}

	// 购物车数量修改接口(GET方式)
	public Result shoppingCart_edit() {
		String uid = session("uid");// AjaxHellper.getHttpParamOfFormUrlEncoded(request(),
									// "uid");
		String datastr = AjaxHellper.getHttpParamOfFormUrlEncoded(request(),
				"datastr");

		ObjectNode result = Json.newObject();

		if (StringUtils.isBlank(uid) || StringUtils.isBlank(datastr)) {
			result.put("status", "0");
			result.put("msg", "修改失败");
			return ok(Json.toJson(result));
		}

		String[] dataArray = datastr.split(",");
		for (String data : dataArray) {
			String[] dataNeedArray = data.split("_");
			String pid = dataNeedArray[0];
			String cnt = dataNeedArray[1];
			ShoppingCart shoppingCart = shoppingCartService
					.getShoppingCartByUIdAndPId(Numbers.parseLong(uid, 0L),
							Numbers.parseLong(pid, 0L));
			if (shoppingCart != null) {
				shoppingCart.setCounts(Numbers.parseInt(cnt, 0));
				shoppingCartService.saveShoppingCart(shoppingCart);
			}
		}
		result.put("status", "1");
		List<Object> shoppingCartList = shoppingCartService
				.getShoppingCart_list(Numbers.parseLong(uid, 0L), 0);

		if (shoppingCartList == null || shoppingCartList.isEmpty())
			return ok(views.html.H5.pageError.render());

		Double totalfee = 0D;
		List<Long> pidlist = new ArrayList<Long>();
		for (Object vo : shoppingCartList) {
			JsonNode jss = Json.toJson(vo);
			if (jss != null) {
				ShoppingCartItemVO s = new ShoppingCartItemVO();
				if (jss.get("pid") != null)
					s.pid = jss.get("pid").textValue();

				if (jss.get("rmbprice") != null)
					s.rmbprice = jss.get("rmbprice").textValue();
				if (jss.get("counts") != null)
					s.counts = jss.get("counts").textValue();

				if (!StringUtils.isBlank(s.pid)) {
					pidlist.add(Numbers.parseLong(s.pid, 0L));
					totalfee = totalfee + Numbers.parseDouble(s.rmbprice, 0D)
							* Numbers.parseDouble(s.counts, 0D);
				}
			}
		}
		totalfee = new BigDecimal(totalfee).setScale(2,BigDecimal.ROUND_HALF_DOWN).doubleValue();
		result.put("tfee", (totalfee.intValue() < totalfee ? totalfee: totalfee.intValue()) + "");
		return ok(Json.toJson(result));
	}

	// 订单预览页面
	public Result orderprocartcrm() {
		Long uid = Numbers.parseLong(session("uid"), 0L);
		WxSign addrSign = null;
		if (!StringUtils.isBlank(session("op")))
			addrSign = H5ShoppingController.getcacheWxsign();

		if (addrSign != null) {
			String orderprourl=StringUtil.getDomainH5()+"/H5/orderprocartcrm?code="+addrSign.getCode()+"&state="+addrSign.getState();
			logger.info("wx-adress-orderprocart-url:-----------------"+orderprourl);
			String addressstr = StringUtil.getWXaddressSign(addrSign.getAccess_token(), orderprourl,addrSign.getTimstr(), addrSign.getNostr());
			session("iswx", "");
			addrSign.setSign(addressstr);
		}
		User user = userService.getUserByUid(uid);
		if (user == null) {
			user.setUid(0L);
			user.setOpenId("");
			user.setUnionid("");
		}

		List<Object> shoppingCartList = shoppingCartService
				.getShoppingCart_list(uid, 0);

		String pid = "0";
		String pic = "";
		List<ShoppingCartItemVO> sv = new ArrayList<ShoppingCartItemVO>();
		Double totalfee = 0D;
		String datastr = "";
		String isopen = "0";
		String pidlist = "";
		List<Long> pidList = new ArrayList<Long>();
		String postfee = "0";// 邮费
		for (Object vo : shoppingCartList) {
			JsonNode jss = Json.toJson(vo);
			if (jss != null) {
				ShoppingCartItemVO s = new ShoppingCartItemVO();
				if (jss.get("pid") != null)
					s.pid = jss.get("pid").textValue();
				if (jss.get("title") != null)
					s.title = jss.get("title").textValue();
				if (jss.get("img") != null)
					s.img = jss.get("img").textValue();
				if (jss.get("rmbprice") != null)
					s.rmbprice = jss.get("rmbprice").textValue();
				if (jss.get("chinaprice") != null)
					s.chinaprice = jss.get("chinaprice").textValue();
				if (jss.get("fromsite") != null)
					s.fromsite = jss.get("fromsite").textValue();
				if (jss.get("iscoupon") != null)
					s.iscoupon = jss.get("iscoupon").textValue();
				if (jss.get("limitcount") != null)
					s.limitcount = jss.get("limitcount").textValue();
				if (jss.get("counts") != null)
					s.counts = jss.get("counts").textValue();
				if (jss.get("rate") != null)
					s.rate = jss.get("rate").textValue();
				if (jss.get("limitcount") != null)
					s.limitcount = jss.get("limitcount").textValue();
				if (jss.get("isopenid") != null)
					s.isopenid = jss.get("isopenid").textValue();

				if (!StringUtils.isBlank(s.pid)) {
					s.rmbprice = new BigDecimal(s.rmbprice).setScale(2,
							BigDecimal.ROUND_CEILING).toString();
					sv.add(s);
					if (s.isopenid != null && s.isopenid.equals("1"))
						isopen = "1";
					// 下期去掉
					s.rmbprice = new BigDecimal(s.rmbprice).setScale(0,
							BigDecimal.ROUND_UP).toString();

					if (!StringUtils.isBlank(s.rmbprice)
							&& s.rmbprice.indexOf(".") >= 0) {
						if (Numbers.parseInt(s.rmbprice.substring(s.rmbprice
								.indexOf(".") + 1), 0) == 0)
							s.rmbprice = s.rmbprice.substring(0,
									s.rmbprice.indexOf("."));
					}
					totalfee = totalfee + Numbers.parseDouble(s.rmbprice, 0D)
							* Numbers.parseDouble(s.counts, 0D);
					datastr = datastr + "," + s.pid + "_" + s.rmbprice + "_"
							+ s.counts;
					pidlist = pidlist + "," + s.pid;
					pidList.add(Numbers.parseLong(s.pid, 0L));
				}
			}
		}

		if (!StringUtils.isBlank(datastr))
			datastr = datastr.substring(1);
		Double costfee = 0D;
		if (!StringUtils.isBlank(pidlist)) {
			pidlist = pidlist.substring(1);
			costfee = new BigDecimal(productService.getRateFee(pidlist, uid))
					.setScale(2, BigDecimal.ROUND_HALF_DOWN).doubleValue();
			postfee = new BigDecimal(productService.getWeightFee(pidList, uid)
					.doubleValue()).setScale(2, BigDecimal.ROUND_HALF_DOWN)
					.doubleValue()
					+ "";
		}
		totalfee = new BigDecimal(totalfee).setScale(2,
				BigDecimal.ROUND_HALF_DOWN).doubleValue();

		// 下期去掉
		totalfee = new BigDecimal(totalfee).setScale(0, BigDecimal.ROUND_UP)
				.doubleValue();

		String tfee = totalfee.toString();

		List<Address> alist = null;
		if (uid.longValue() > 0) {
			try {
				alist = addressService.address_default(uid);
			} catch (Exception e) {
			}
		}
		Address addressobj = alist == null || alist.isEmpty() ? null : alist
				.get(0);
		if (addressobj == null) {
			addressobj = new Address();
			addressobj.setAddressId(Long.valueOf("0"));
			addressobj.setPhone(user.getPhone());
			addressobj.setName("");
			addressobj.setAddress("");
			addressobj.setProvince("");
			addressobj.setCardId("");
		}

		if (sv == null || sv.isEmpty())
			sv = null;

		String taxfee = costfee == null ? "0" : costfee.doubleValue() + "";

		tfee = String.valueOf(Numbers.parseDouble(tfee, 0L)
				+ Numbers.parseDouble(postfee, 0L)
				+ Numbers.parseDouble(taxfee, 0L));
		if (!StringUtils.isBlank(tfee) && tfee.indexOf(".") >= 0) {
			if (Numbers.parseInt(tfee.substring(tfee.indexOf(".") + 1), 0) == 0)
				tfee = tfee.substring(0, tfee.indexOf("."));
		}
		if (!StringUtils.isBlank(postfee) && postfee.indexOf(".") >= 0) {
			if (Numbers.parseDouble(
					postfee.substring(postfee.indexOf(".") + 1), 0) == 0)
				postfee = postfee.substring(0, postfee.indexOf("."));
		}
		if (!StringUtils.isBlank(taxfee) && taxfee.indexOf(".") >= 0) {
			if (Numbers.parseDouble(taxfee.substring(taxfee.indexOf(".") + 1),
					0) == 0)
				taxfee = taxfee.substring(0, taxfee.indexOf("."));
		}
		String wxtoken = AjaxHellper.getHttpParam(request(), "code");
		
			wxtoken=StringUtils.isBlank(wxtoken)?"":wxtoken;
		return ok(views.html.H5.orderprocartcrm.render(addressobj, sv, user,isopen, postfee, taxfee, tfee, datastr, pidlist, addrSign,wxtoken));

	}

	public Result orderpro() {
		Long uid = Numbers.parseLong(session("uid"), 0L);// Numbers.parseLong(AjaxHellper.getHttpParam(request(),
															// "uid"), 0L);
		Long pid = Numbers.parseLong(
				AjaxHellper.getHttpParam(request(), "pid"), Long.valueOf(0));
		String phone = AjaxHellper.getHttpParam(request(), "phone");
		if (StringUtils.isBlank(phone) || phone.toLowerCase().equals("null"))
			phone = "";
		String unionid = session("un");// AjaxHellper.getHttpParam(request(),
										// "un");
		if (StringUtils.isBlank(unionid))
			unionid = "";
		String openid = session("op");// AjaxHellper.getHttpParam(request(),
										// "op");
		if (StringUtils.isBlank(openid))
			openid = "";

		WxSign addrSign = null;
		
		logger.info("session(op)......................."+session("op"));
		if (!StringUtils.isBlank(session("op"))) {
			addrSign = H5ShoppingController.getcacheWxsign();
		}
		// if(StringUtils.isBlank(cache.get("wx_access_token_voke"+session("uid"))))
		// addrSign=null;
		if (addrSign != null) {
			String orderprourl=StringUtil.getDomainH5()+"/H5/orderpro?pid="+pid+"&code="+addrSign.getCode()+"&state="+addrSign.getState();
			logger.info("wx-address-orderpro-url..........."+orderprourl);
			String addressstr = StringUtil.getWXaddressSign(addrSign.getAccess_token(),orderprourl,addrSign.getTimstr(),addrSign.getNostr());
			addrSign.setSign(addressstr);
		}
		if (pid == 0L)
			return ok(views.html.H5.pageError.render());

		Product proinfo = productService.getProductById(pid);
		if (proinfo == null) {
			return ok(views.html.sheSaid.pageError.render());
		}
		User user = userService.getUserByUid(uid);
		if (user == null) {
			user = new User();
			user.setUid(Long.valueOf("0"));
			user.setNickname("");
			user.setOpenId(openid);
			user.setUnionid(unionid);
			user.setHeadIcon("");
		}

		List<Address> alist = null;
		if (uid.longValue() > 0) {
			try {
				alist = addressService.address_default(uid);
			} catch (Exception e) {
			}
		}
		Address addressobj = alist == null || alist.isEmpty() ? null : alist
				.get(0);
		if (addressobj == null) {
			addressobj = new Address();
			addressobj.setAddressId(Long.valueOf("0"));
			addressobj.setPhone(user.getPhone());
			addressobj.setName("");
			addressobj.setAddress("");
			addressobj.setProvince("");
			addressobj.setCardId("");
		}

		proinfo.setFromobj(productService.getfrom(proinfo.getFromsite()));

		if (proinfo.getTyp().equals("1")) {
			Currency currency = productService.queryCurrencyById(proinfo
					.getCurrency());
			BigDecimal rate = new BigDecimal(currency.getRate() / 100)
					.setScale(4, BigDecimal.ROUND_CEILING);
			BigDecimal price = new BigDecimal(proinfo.getPrice() / 100)
					.setScale(2, BigDecimal.ROUND_CEILING);
			BigDecimal rmb_price = rate.multiply(price).setScale(0,
					BigDecimal.ROUND_CEILING);
			proinfo.setRmbprice(Double.valueOf(rmb_price.toString()));
		} else
			proinfo.setRmbprice(Double.valueOf(Integer.valueOf(new BigDecimal(
					proinfo.getRmbprice())
					.setScale(0, BigDecimal.ROUND_CEILING).toString())));

		int weightfee = proinfo.getFromobj().getFee();
		if (proinfo.getWeight() * 1000 > 500) {
			Double wf = proinfo.getFromobj().getFee()
					+ ((proinfo.getWeight() * 1000 - 500) / 100)
					* proinfo.getFromobj().getAddfee();
			BigDecimal b = new BigDecimal(String.valueOf(wf));
			weightfee = Integer.valueOf(b.setScale(0, BigDecimal.ROUND_CEILING)
					.toString());
		}
		proinfo.setPostfee(weightfee);
		//如果多规格取数量
		if (proinfo.getIshot() == 1) {
			proinfo.setNstock(productService.dealNstockWithProduct(proinfo.getPid()));
		}
		if (proinfo.getLimitcount() > Integer.valueOf(proinfo.getNstock()
				.toString()))
			proinfo.setLimitcount(Integer.valueOf(proinfo.getNstock()
					.toString()));

		String buycode = "H5";

		if (!StringUtils.isBlank(phone))
			buycode = "kuaige";


		String wxtoken = AjaxHellper.getHttpParam(request(), "code");
		wxtoken=StringUtils.isBlank(wxtoken)?"":wxtoken;

		return ok(views.html.H5.orderpro.render(addressobj, proinfo, user,Constants.WXappID, buycode, addrSign, wxtoken));
	}

	public Result loginpro() {
		String pid = AjaxHellper.getHttpParam(request(), "pid");
		String phone = AjaxHellper.getHttpParam(request(), "phone");
		if (StringUtils.isBlank(phone))
			phone = "";
		String mcode = AjaxHellper.getHttpParam(request(), "mcode");
		mcode = StringUtils.isBlank(mcode) ? "" : mcode;

		String uid = AjaxHellper.getHttpParam(request(), "uid");

		User user = new User();// userService.getUserByUid(uid);

		user.setOpenId(session("openid"));
		user.setUnionid(session("unionid"));

		if (StringUtils.isBlank(user.getOpenId()))
			user.setOpenId(StringUtils.isBlank(AjaxHellper.getHttpParam(
					request(), "op")) ? "" : AjaxHellper.getHttpParam(
					request(), "op"));
		if (StringUtils.isBlank(user.getUnionid()))
			user.setUnionid(StringUtils.isBlank(AjaxHellper.getHttpParam(
					request(), "un")) ? "" : AjaxHellper.getHttpParam(
					request(), "un"));

		user.setAccessToken(session("access_token"));
		user.setNickname(session("wx_nickname"));
		user.setHeadIcon(session("wx_headimgurl"));

		return ok(views.html.H5.loginpro.render(user, pid, phone, uid,
				mcode));
	}

	// 微信提交预支付
	public Result orderxpro() {
		String uid = AjaxHellper.getHttpParam(request(), "uid");
		String pid = AjaxHellper.getHttpParam(request(), "pid");
		String orderCode = AjaxHellper.getHttpParam(request(), "orderCode");
		String aid = AjaxHellper.getHttpParam(request(), "aid");
		String phone = AjaxHellper.getHttpParam(request(), "phone");

		if (StringUtils.isBlank(orderCode)) {
			return ok(views.html.H5.pageError.render());
		}

		ShoppingOrder order = orderService
				.getShoppingOrderByOrderCode(orderCode);
		if (order == null)
			return ok(views.html.H5.pageError.render());

		String code = AjaxHellper.getHttpParam(request(), "code");

		String prepayid = "";
		String nostr = RandomStringUtils.randomAlphanumeric(32);
		String sign = "";
		// 判断返回是否含有unionid,openid...
		String openid = session("op");
		String unionid = session("un");
		String access_token = "";
		String state = "";
		WxSign tmpsign=H5ShoppingController.getcacheWxsign();
		if(tmpsign==null || StringUtils.isBlank(tmpsign.getAccess_token())){
			JsonNode res = H5ShoppingController.getwxtoken();
			if (res != null) {
				access_token = res.get("access_token").textValue();
				openid = res.get("openid").textValue();
				unionid = res.get("unionid").textValue();
				code = res.get("code").textValue();
				state = res.get("state").textValue();
			}
		}else{
			access_token=tmpsign.getAccess_token();
			openid=tmpsign.getOpenid();
			unionid=tmpsign.getUnionid();
			code=tmpsign.getCode();
			state=tmpsign.getState();
		}
		
		try {
			// 检查并获取用户基本信息
			if (!access_token.equals("") && !openid.equals("")
					&& !unionid.equals("")) {
				// if(1==1){
				// 如果正确返回调用预支付订单接口
				String wxorderurl = "https://api.mch.weixin.qq.com/pay/unifiedorder";
				Map<String, String> prams = new HashMap<String, String>();
				WxRequest rq = new WxRequest();

				prams.put("appid", Constants.WXappID);
				prams.put("attach", "嗨个购-" + order.getOrderCode());
				rq.setAttach("嗨个购-" + order.getOrderCode());
				prams.put("body", "嗨个购-" + order.getOrderCode());
				rq.setBody("嗨个购-" + order.getOrderCode());
				prams.put("mch_id", Constants.WXMCID);

				prams.put("nonce_str", nostr);
				rq.setNonce_str(nostr);
				prams.put("notify_url", notify_url);
				rq.setNotify_url(notify_url);

				prams.put("openid", openid);
				rq.setOpenid(openid);

				prams.put("out_trade_no", orderCode);
				rq.setOut_trade_no(orderCode);
				prams.put("spbill_create_ip", "123.56.105.53");
				rq.setSpbill_create_ip("123.56.105.53");
				String tmpt = String.valueOf(new BigDecimal(
						order.getTotalFee() * 100).setScale(2,
						BigDecimal.ROUND_HALF_DOWN));
				if (!StringUtils.isBlank(tmpt))
					tmpt = new BigDecimal(tmpt).setScale(0,
							BigDecimal.ROUND_HALF_DOWN).intValue()
							+ "";

				prams.put("total_fee", tmpt);
				rq.setTotal_fee(Integer.valueOf(tmpt));
				prams.put("trade_type", "JSAPI");
				sign = StringUtil.getSign(prams);
				prams.put("sign", sign);
				rq.setSign(sign);

				String postData = "<xml>";
				Set es = prams.entrySet();
				Iterator it = es.iterator();
				while (it.hasNext()) {
					Map.Entry entry = (Map.Entry) it.next();
					String k = (String) entry.getKey();
					String v = (String) entry.getValue();
					if (k != "appkey") {
						postData += "<" + k + ">" + v + "</" + k + ">";
					}
				}
				postData += "</xml>";
				postData = this.getXmlInfo(rq);
				// postData=new String(postData.getBytes(), "ISO8859-1");
				String resContento = "";
				HttpClient client = new DefaultHttpClient();
				HttpPost post = new HttpPost(wxorderurl);
				post.setHeader("Content-Type", "text/xml; charset=UTF-8");
				try {
					post.setEntity(new StringEntity(postData, "UTF-8"));
					HttpResponse res = client.execute(post);
					String strResult = EntityUtils.toString(res.getEntity(),
							"UTF-8");

					Logger.info("统一订购接收到的报文:" + strResult);

					if (strResult != null && strResult.length() > 0) {
						Document doc = null;
						doc = DocumentHelper.parseText(strResult);
						Element rootElt = doc.getRootElement();
						// returnstr=strResult;
						String return_code = rootElt
								.elementTextTrim("return_code");
						String return_msg = rootElt
								.elementTextTrim("return_code");

						logger.info("win xin pay return:"
								+ rootElt.elementTextTrim("return_code"));
						logger.info("win xin pay return:"
								+ rootElt.elementText("return_msg"));
						if (return_code != null
								&& return_code.equals("SUCCESS")) {
							logger.info("win xin pay return:"
									+ rootElt.elementText("err_code"));
							logger.info("win xin pay return:"
									+ rootElt.elementText("err_code_des"));

							if (rootElt.element("result_code") != null
									&& rootElt.elementText("result_code")
											.equals("SUCCESS")) {
								if (rootElt.element("prepay_id") != null)
									prepayid = (rootElt
											.elementText("prepay_id"));
							}
						}
					}
				} catch (Exception e) {

					logger.error("win xin pay error:" + e.toString());
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		if (StringUtils.isBlank(prepayid))
			return ok(views.html.H5.pageError.render());
		else {

			return redirect("/H5/wxpayh5?nostr=" + nostr + "&sign=" + sign
					+ "&ordercode=" + orderCode + "&payid=" + prepayid
					+ "&uid=" + order.getuId() + "&pid=" + pid);

		}
	}

	/* H5正常分享页面购买相关结束/////////////////////////////////////////////////////////// */

	/*
	 * #代言说明页
	 */
	public static Result endorse_rule() {
		return ok(views.html.H5.newuser.endorse_rule.render());
	}

	/*
	 * 钱包流水提取说明页
	 */
	public static Result balancerule() {
		String v = StringUtil.getSystemConfigValue("wallet_moneylimit");
		if (StringUtils.isBlank(v))
			v = "";
		return ok(views.html.H5.newuser.balancerule.render(v));
	}

	/*
	 * 邀请好友页面
	 */
	public static Result invite() {
		String coupimg = "images/usernew/index_top-1.png";
		String fromUid = AjaxHellper.getHttpParam(request(), "fromUid");
		// H5分享调用微信获取TOKEN，jsp_tackit
		WxSign wxs = getwxstr();
		String domainH5 = "http://h5.higegou.com";
		String picurl = CdnAssets.CDN_API_PUBLIC_URL
				+ "images/H5/shareCouponImg.png";
		return ok(views.html.H5.newuser.invite.render(coupimg, fromUid,
				wxs.getAppId(), wxs.getNostr(), wxs.getTimstr(), wxs.getSign(),
				domainH5 + "/H5/invite?fromUid=" + fromUid, picurl));
	}

	/*
	 * 邀请好友领取优惠券绑定
	 */
	public Result adduserconpone() {
		String phone = AjaxHellper.getHttpParamOfFormUrlEncoded(request(),
				"phone");
		String ip = AjaxHellper.getHttpParamOfFormUrlEncoded(request(), "ip");
		String fromUid = AjaxHellper.getHttpParamOfFormUrlEncoded(request(),
				"fromUid");
		logger.info(fromUid);
		if (!StringUtil.checkPhone(phone)) {
			return ok(views.html.H5.pageError.render());
		}
		ObjectNode data = Json.newObject();
		data.put("status", "0");
		data.put("msg", "");
		data.put("couponmoney", "0");
		if (StringUtils.isBlank(ip)) {
			data.put("msg", "系统错误");
		}
		// 检查用户是否注册
		User user = userService.getUserByphone(phone);
		if (user == null) {
			String msg = userService.addCouponPhoneByIP(phone, ip,
					StringUtil.getSystemConfigValue("invite_coupon_market"),
					fromUid);
			if (StringUtils.isBlank(msg)) {
				data.put("status", "1");
			} else
				data.put("msg", msg);
		} else {
			data.put("msg", "此优惠券仅限新人领取");
		}

		return ok(data);
	}

	public Result inviteok() {
		String phone = AjaxHellper.getHttpParam(request(), "p");
		String cm = "";
		if (!StringUtils.isBlank(phone) && StringUtil.checkPhone(phone)) {
			// 取优惠券总额
			cm = String.valueOf(userService.getTotalCouponmoney(phone,
					StringUtil.getSystemConfigValue("invite_coupon_market")));
		}
		return ok(views.html.H5.newuser.inviteok.render(cm));
	}

	/*
	 * 发送验证码
	 */
	public Result userVerifysms() {
		response().setContentType("application/json;charset=utf-8");
		String uid = AjaxHellper.getHttpParamOfFormUrlEncoded(request(), "uid");

		String phone = AjaxHellper.getHttpParamOfFormUrlEncoded(request(),
				"phone");
		String reg = AjaxHellper.getHttpParamOfFormUrlEncoded(request(), "reg") == null ? "1"
				: AjaxHellper.getHttpParam(request(), "reg");
		Integer uidt = Numbers.parseInt(uid, -1);
		String imgcheck = AjaxHellper.getHttpParamOfFormUrlEncoded(request(),
				"imgcheck");

		String timstr = session(Constants.Session_Validate_Image + "timstr");
		String iswx = AjaxHellper.getHttpParamOfFormUrlEncoded(request(), "wx");
		iswx = StringUtils.isBlank(iswx) ? "" : iswx;

		StatusMsgVO result = new StatusMsgVO();
		if (!StringUtils.isBlank(timstr)) {
			try {
				// 判断时间如果低于60秒不能再次发送验证码
				Date d1 = CHINESE_DATE_TIME_FORMAT_NORMAL.parse(timstr);
				Date d2 = new Date();
				Long between = d2.getTime() - d1.getTime();
				long day = between / (24 * 60 * 60 * 1000);
				long hour = (between / (60 * 60 * 1000) - day * 24);
				long min = ((between / (60 * 1000)) - day * 24 * 60 - hour * 60);
				long s = (between / 1000 - day * 24 * 60 * 60 - hour * 60 * 60 - min * 60);
				if (min < 1) {
					result.setStatus("4");
					result.setMsg("验证码发送太频繁");
					return ok(Json.toJson(result));
				}
			} catch (Exception e) {
				result.setStatus("4");
				result.setMsg("验证码发送太频繁");
				return ok(Json.toJson(result));
			}
		}
		// 如果是微信数据不验图形
		Integer sentcount = smsService.getVerifynum(phone, 1440);
		/*
		 * if(!iswx.equals("1") && sentcount>3){ if (uidt<0 ||
		 * StringUtils.isBlank(imgcheck)) { result.setStatus("4");
		 * result.setMsg("验证码不正确"); return ok(Json.toJson(result)); }
		 * if(session(Constants.Session_Validate_Image)==null ||
		 * !imgcheck.toLowerCase
		 * ().equals(session(Constants.Session_Validate_Image).toLowerCase())){
		 * result.setStatus("4"); result.setMsg("验证码不正确"); return
		 * ok(Json.toJson(result)); } }
		 */
		session(Constants.Session_Validate_Image + "timstr",
				CHINESE_DATE_TIME_FORMAT_NORMAL.format(new Date()));
		session().remove(Constants.Session_Validate_Image);
		// 获取调用的上一个页面的URL地址
		String preurl = request().getHeader("Referer");
		boolean issms = false;
		boolean IsProduct = Configuration.root()
				.getBoolean("production", false);
		if (IsProduct) {
			if (preurl.indexOf("h5.higegou.com") >= 0
					|| preurl.indexOf("api.higegou.com") >= 0) {
				issms = true;
			}
		} else {
			issms = true;
		}
		// issms=true;
		try {
			String tempdomain = preurl.replace("http://", "");
			tempdomain = tempdomain.substring(0, tempdomain.indexOf("/"));
			InetAddress iAddress = InetAddress.getByName(tempdomain);
			String tempip = iAddress.getHostAddress();
			Logger.info("getsmsIP:" + tempip);
			if (tempip.equals("101.201.171.42")
					|| tempip.equals("182.92.227.140")) {
				issms = true;
			}
		} catch (Exception e) {
		}

		logger.info("preurl=" + preurl);
		if (issms) {
			if (!PHONE_PATTERN.matcher(phone).matches()) {
				result.setStatus("5");
				result.setMsg("请输入正确的手机号");
				return ok(Json.toJson(result));
			}
			String code = utils.StringUtil.genRandomCode(4);// 生成四位随机数
			logger.info(code);

			String ip = request().remoteAddress();
			// 获取有效时效内5分钟内发送的验证码重用
			String tmpcode = smsService.getsmscode(phone);
			if (!StringUtils.isBlank(tmpcode))
				code = tmpcode;

			String sendFlag = userService.saveVerifyInVerfify(ip, uid, phone,
					code, "");

			if ("1".equals(sendFlag)) {
				// smsService.getNewVerify(phone, code);
				smsService.getVerify(phone, code);
				result.setStatus("1");
				result.setMsg("");
			} else if ("-1".equals(sendFlag)) {
				result.setStatus("6");
				result.setMsg("发送太频繁，请稍后再试！");
			} else {
				result.setStatus("6");
				result.setMsg("该手机号发送次数太多，请稍后再试！");
			}
		} else {
			result.setStatus("-6");
			result.setMsg("发送成功");
		}
		return ok(Json.toJson(result));
	}

	public static Result bbtsync() {
		String tmpt = String.valueOf(Integer.valueOf(new BigDecimal(Double
				.valueOf(Constants.BBTPRICE) * 100).setScale(0,
				BigDecimal.ROUND_CEILING).toString()));
		logger.info(tmpt);
		String pid = "3993";
		if (pid.equals("3993"))
			// tmpt="399";
			tmpt = tmpt = String.valueOf(Integer.valueOf(new BigDecimal(
					4.0 * 100 - 1).toString()));
		else
			tmpt = "99";
		return ok(views.html.H5.bbtsync.render());
	}

	/* 棒棒糖送优惠券下载统计添加 */
	public Result downbbtyh() {
		String mcode = AjaxHellper.getHttpParam(request(), "mcode");
		String ip = request().remoteAddress();
		String url = "http://dwz.cn/BDTux";
		String sharetype = "";
		String iswx = "0";
		String channel = StringUtils.isBlank(mcode) ? "" : mcode;
		String uid = "0";

		if (!StringUtils.isBlank(url) && !StringUtils.isBlank(channel)) {
			CountH5 cnt = new CountH5();
			cnt.setChannel(channel + "_youhuijuan");
			cnt.setIp(StringUtils.isBlank(ip) ? request().remoteAddress() : ip);
			cnt.setIswx(iswx);
			cnt.setShareType(sharetype);
			cnt.setUrl(url);
			cnt.setCreateTime(new Date());
			cnt.setUserId(Numbers.parseLong(uid, 0L));
			endorseService.saveCount(cnt);
		}
		return redirect(url);
	}

	/* 棒棒糖下单短信下载统计添加 */
	public Result downbbt() {
		String mcode = AjaxHellper.getHttpParam(request(), "mcode");
		String ip = request().remoteAddress();
		String url = "http://dwz.cn/BDTux";
		String sharetype = "";
		String iswx = "0";
		String channel = StringUtils.isBlank(mcode) ? "" : mcode;
		String uid = "0";

		if (!StringUtils.isBlank(url) && !StringUtils.isBlank(channel)) {
			CountH5 cnt = new CountH5();
			cnt.setChannel(channel + "_kuaidi");
			cnt.setIp(StringUtils.isBlank(ip) ? request().remoteAddress() : ip);
			cnt.setIswx(iswx);
			cnt.setShareType(sharetype);
			cnt.setUrl(url);
			cnt.setCreateTime(new Date());
			cnt.setUserId(Numbers.parseLong(uid, 0L));
			endorseService.saveCount(cnt);
		}
		return redirect(url);
	}

	// 组装微信签名返回参数数据
	public static WxSign getwxstr() {
		ICacheService cache = ServiceFactory.getCacheService();
		String nostr = StringUtils.isBlank(cache.get("getwxSign_nostr")) ? RandomStringUtils
				.randomAlphanumeric(16) : cache.get("getwxSign_nostr");
		String timstr = StringUtils.isBlank(cache.get("getwxSign_timstr")) ? Sha1Util
				.getTimeStamp() : cache.get("getwxSign_timstr");
		String ticket = StringUtils.isBlank(cache.get("getwxSign_ticket")) ? ""
				: cache.get("getwxSign_ticket");// session("ticket");
		String access_token = StringUtils.isBlank(cache
				.get("getwxSign_access_token")) ? "" : cache
				.get("getwxSign_access_token");

		String resContent = "";
		String sign = "";
		boolean IsProduct = Configuration.root()
				.getBoolean("production", false);
		String status = "0";
		TenpayHttpClient httpClient = new TenpayHttpClient();
		if (StringUtils.isBlank(access_token)) {
			try {
				// 清缓存
				cache.clear("getwxSign_nostr");
				cache.clear("getwxSign_timstr");
				cache.clear("getwxSign_ticket");
				nostr = RandomStringUtils.randomAlphanumeric(16);
				timstr = Sha1Util.getTimeStamp();

				String gettokenurl = "https://api.weixin.qq.com/cgi-bin/token?grant_type=client_credential&appid="
						+ Constants.WXappID
						+ "&secret="
						+ Constants.WXappsecret;
				httpClient.setReqContent(gettokenurl);
				if (httpClient.callHttpPost(gettokenurl, "")) {
					resContent = httpClient.getResContent();
					Logger.info("微信请求access_token返回：" + resContent);
					JSONObject json = JSONObject.fromObject(resContent);
					try {
						access_token = json.getString("access_token");
						cache.setWithOutTime("getwxSign_nostr", nostr, 7200);
						cache.setWithOutTime("getwxSign_timstr", timstr, 7200);
						cache.setWithOutTime("getwxSign_access_token",
								access_token, 7200);
					} catch (Exception ee) {
					}
					if (!StringUtils.isBlank(access_token)) {
						try {
							// 获取 jsp_tackit
							String getTickaturl = "https://api.weixin.qq.com/cgi-bin/ticket/getticket?access_token="
									+ access_token + "&type=jsapi";
							httpClient = new TenpayHttpClient();
							httpClient.setReqContent(getTickaturl);
							if (httpClient.callHttpPost(getTickaturl, "")) {
								resContent = httpClient.getResContent();
								Logger.info("微信请求jsapi_ticket返回：" + resContent);
								JSONObject jsont = JSONObject
										.fromObject(resContent);
								if (jsont.getString("errcode").equals("0")) {
									ticket = jsont.getString("ticket");
									cache.setWithOutTime("getwxSign_ticket",
											ticket, 7150);
								}
							}
						} catch (Exception ee) {
						}
					}
				}
			} catch (Exception e) {
			}
		} else {
			if (StringUtils.isBlank(ticket)) {
				try {
					// 获取 jsp_tackit
					String getTickaturl = "https://api.weixin.qq.com/cgi-bin/ticket/getticket?access_token="
							+ access_token + "&type=jsapi";
					httpClient = new TenpayHttpClient();
					httpClient.setReqContent(getTickaturl);
					if (httpClient.callHttpPost(getTickaturl, "")) {
						resContent = httpClient.getResContent();
						Logger.info("微信请求jsapi_ticket返回：" + resContent);
						JSONObject jsont = JSONObject.fromObject(resContent);
						if (jsont.getString("errcode").equals("0")) {
							ticket = jsont.getString("ticket");
							cache.setWithOutTime("getwxSign_ticket", ticket,
									7150);
						}
					}
				} catch (Exception ee) {
				}
			}
		}
		String qry = "";
		String vn = "";
		String vl = "";
		Map<String, String[]> maps = request().queryString();
		if (maps != null && maps.keySet() != null) {
			Iterator<String> keyIt = maps.keySet().iterator();
			while (keyIt.hasNext()) {
				vn = keyIt.next();
				vl = maps.get(vn)[0];
				qry = qry + "&" + vn + "=" + vl;
			}
		}

		if (!StringUtils.isBlank(qry))
			qry = "?" + qry.substring(1);
		String dport = IsProduct == true ? "" : "";//":9004";
		String urls = "http://" + request().host() + dport + request().path()
				+ qry;
		Logger.info("解析urls:" + urls);
		Map<String, String> pramt = new HashMap<String, String>();
		if (!StringUtils.isBlank(ticket)) {
			status = "1";

			pramt.put("timestamp", timstr);
			pramt.put("noncestr", nostr);
			pramt.put("jsapi_ticket", ticket);
			pramt.put("url", urls);
			sign = StringUtil.getShareSign(pramt);
		}
		ObjectNode re = Json.newObject();
		WxSign wxsign = new WxSign();
		wxsign.setNostr(nostr);
		wxsign.setTimstr(timstr);
		wxsign.setAppId(Constants.WXappID);
		wxsign.setSign(sign);
		return wxsign;
	}
}
