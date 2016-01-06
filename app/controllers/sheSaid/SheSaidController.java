package controllers.sheSaid;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
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
import models.Endorsement;
import models.EndorsementSort;
import models.Product;
import models.ProductDetail;
import models.Product_images;
import models.Qanswer;
import models.Question;
import models.ShoppingCartEndorse;
import models.ShoppingOrder;
import models.User;
import models.WxRequest;
import models.WxSign;
import net.sf.json.JSONObject;

import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.StringRequestEntity;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
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
import services.api.AddressService;
import services.api.CommentService;
import services.api.EndorsementService;
import services.api.ProductService;
import services.api.ShoppingCartService;
import services.api.ShoppingOrderService;
import services.api.UserService;
import utils.AjaxHellper;
import utils.Constants;
import utils.Numbers;
import utils.StringUtil;
import utils.alipay.AlipayNotify;
import utils.wxpay.Sha1Util;
import utils.wxpay.TenpayHttpClient;
import vo.shoppingCart.ShoppingCartCategoryVO;
import assets.CdnAssets;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import controllers.BaseController;
import controllers.api.H5ProductController;
import controllers.api.H5ShoppingController;

@Named
@Singleton
public class SheSaidController extends BaseController {
	private static final Logger.ALogger logger = Logger
			.of(SheSaidController.class);
	private static final SimpleDateFormat CHINESE_DATE_TIME_FORMAT = new SimpleDateFormat(
			"yyyyMMddHHmmss");
	private static final SimpleDateFormat CHINESE_D_TIME_FORMAT = new SimpleDateFormat(
			"yyyy-MM-dd HH:mm:ss");
	private final ApplicationService applicationService;
	private final UserService userService;
	private final ProductService productService;
	private final CommentService commentService;
	private final EndorsementService endorseService;
	private final AddressService addressService;
	private final ShoppingOrderService orderService;
	private final ShoppingCartService shoppingCartService;
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
					"http://h5.higegou.com");
		}
	}

	@Inject
	public SheSaidController(final ApplicationService applicationService,
			final UserService userService, final ProductService productService,
			final CommentService commentService,
			final EndorsementService endorseService,
			final AddressService addressService,
			final ShoppingOrderService orderService,
			final ShoppingCartService shoppingCartService) {
		this.applicationService = applicationService;
		this.userService = userService;
		this.productService = productService;
		this.commentService = commentService;
		this.endorseService = endorseService;
		this.addressService = addressService;
		this.orderService = orderService;
		this.shoppingCartService = shoppingCartService;
	}

	// @Before
	public boolean checkses() {

		if (StringUtils.isBlank(session("hgUid"))) {
			// return redirect(domains+"/sheSaid/pageerr");
			return false;
		}
		return true;
	}

	public static Result index() {
		return ok(views.html.sheSaid.index.render());
	}

	public Result login() {
		String pid = AjaxHellper.getHttpParam(request(), "pid");
		Long uid = Numbers.parseLong(
				AjaxHellper.getHttpParam(request(), "uid"), 0L);
		String openid = AjaxHellper.getHttpParam(request(), "op");

		User user = new User();// userService.getUserByUid(uid);
		String flg = AjaxHellper.getHttpParam(request(), "flg");
		String shareType = AjaxHellper.getHttpParam(request(), "st");
		if (StringUtils.isBlank(shareType))
			shareType = "";

		if (StringUtils.isBlank(openid))
			openid = "";

		user.setOpenId(openid);
		user.setUnionid(cache.get(openid + "unionid"));
		user.setNickname(cache.get(openid + "nickname"));
		user.setHeadIcon(cache.get(openid + "headicon"));
		String dyid = session("daiyanid");
		if (StringUtils.isBlank(dyid))
			dyid = AjaxHellper.getHttpParam(request(), "daiyanid");

		Endorsement endorse = endorseService.getEndorseById(Numbers.parseLong(
				dyid, 0L));
		if (endorse == null) {
			return ok(views.html.sheSaid.pageError.render());
		}

		endorse.setProducinfo(productService.getProductById(endorse
				.getProductId()));
		return ok(views.html.sheSaid.login.render(user, pid, dyid,
				uid.toString(), flg, shareType));
	}

	public Result order() {
		Long postmanid = Numbers.parseLong(session("postmanid"), 0L);
		if (postmanid.longValue() > 0)
			session("postmanid", postmanid.longValue() + "");
		else
			postmanid = Numbers.parseLong(CdnAssets.HIGOUSHOPID, 0L);
		Long uid = Numbers.parseLong(session("uid"), 0L);
		Long pid = Numbers.parseLong(
				AjaxHellper.getHttpParam(request(), "pid"), Long.valueOf(0));
		Long dyid = Numbers.parseLong(
				AjaxHellper.getHttpParam(request(), "daiyanid"),
				Long.valueOf(0));

		String iswx = AjaxHellper.getHttpParam(request(), "iswx");
		iswx = StringUtils.isBlank(iswx) ? "" : iswx;

		WxSign addrSign = null;
		if (!StringUtils.isBlank(session("op")))
			addrSign = H5ShoppingController.getcacheWxsign();// (WxSign)cache.getObject("wxcontant"+session("op"));

		if (addrSign != null) {
			String orderurl = StringUtil.getDomainH5() + "/sheSaid/order?pid="
					+ pid + "&daiyanid=" + dyid + "&code=" + addrSign.getCode()
					+ "&state=" + addrSign.getState();
			String addrSignstr = StringUtil.getWXaddressSign(
					addrSign.getAccess_token(), orderurl, addrSign.getTimstr(),
					addrSign.getNostr());
			addrSign.setSign(addrSignstr);
		}

		String openid = session("op");// AjaxHellper.getHttpParam(request(),
										// "op");
		if (StringUtils.isBlank(openid))
			openid = "";
		String unionid = session("un");// AjaxHellper.getHttpParam(request(),
										// "un");
		unionid = StringUtils.isBlank(unionid) ? "" : unionid;
		if (pid == 0L) {
			return ok(views.html.sheSaid.pageError.render());
		}
		User user = userService.getUserByUid(uid);

		if (user == null) {
			user = new User();
			user.setUid(Long.valueOf("0"));
			user.setOpenId(openid);
			user.setUnionid(unionid);
			user.setNickname(cache.get(openid + "nickname"));
			user.setHeadIcon(cache.get(openid + "headicon"));
		}
		Endorsement endorse = endorseService.getEndorseById(dyid);
		if (endorse == null)
			return ok(views.html.sheSaid.pageError.render());
		if (endorse.getProductId().longValue() != pid.longValue()) {
			return ok(views.html.sheSaid.pageError.render());
		}
		user.setGid(endorse.getUserId());

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
			if (endorse.getUserId().longValue() == 423908) {
				addressobj.setAddress("新开街28-14号 东方美妆");
				addressobj.setProvince("北京市平谷区");
			}
		}

		Product proinfo = productService.getProductById(pid);
		if (proinfo == null) {
			return ok(views.html.sheSaid.pageError.render());
		}
		proinfo.setFromobj(productService.getfrom(proinfo.getFromsite()));
		proinfo.setRmbprice(new BigDecimal(proinfo.getRmbprice()).setScale(2,
				BigDecimal.ROUND_CEILING).doubleValue());
		// proinfo.setEndorsementPrice(Double.valueOf(Integer.valueOf(new
		// BigDecimal(proinfo.getEndorsementPrice()).setScale(0,BigDecimal.ROUND_CEILING).toString())));

		if (endorse.getGid() != null
				&& (endorse.getGid().longValue() == 4 || endorse.getGid()
						.longValue() == 6)) {
			proinfo.setEndorsementPrice(endorse.getEndorsementPrice());
			proinfo.setRmbprice(new BigDecimal(proinfo.getChinaprice())
					.setScale(2, BigDecimal.ROUND_CEILING).doubleValue());
		} else
			proinfo.setEndorsementPrice(new BigDecimal(proinfo
					.getEndorsementPrice()).setScale(2,
					BigDecimal.ROUND_CEILING).doubleValue());

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
		if (proinfo.getLimitcount() > Integer.valueOf(proinfo.getNstock().toString()))
			proinfo.setLimitcount(Integer.valueOf(proinfo.getNstock().toString()));		


		if (endorse.getGid() != null
				&& (endorse.getGid().longValue() == 4 || endorse.getGid()
						.longValue() == 6)) {
			Long postmanuid = Numbers.parseLong(session("postmanuid"), 0L);
			User postman = null;
			if (postmanuid.longValue() > 0)
				postman = userService.getUserByUid(postmanuid);
			
		}
		String code = AjaxHellper.getHttpParam(request(), "code");
		code = StringUtils.isBlank(code) ? "" : code;
		return ok(views.html.sheSaid.order.render(addressobj, proinfo, endorse,
				user, Constants.WXappID,
				session("wx_access_token") == null ? ""
						: session("wx_access_token"), addrSign, code,
				postmanid));
	}

	// 订单完成页面
	public Result saveorderend() {
		Long uid = Numbers.parseLong(session("uid"), 0L);// Numbers.parseLong(AjaxHellper.getHttpParam(request(),
		String orderCode = AjaxHellper.getHttpParam(request(), "ordercode");
		String timstr = AjaxHellper.getHttpParam(request(), "timstr");
		String nostr = AjaxHellper.getHttpParam(request(), "nostr");
		String sign = AjaxHellper.getHttpParam(request(), "sign");
		String prepayid = AjaxHellper.getHttpParam(request(), "payid");
		if (StringUtils.isBlank(orderCode) || uid.longValue() == 0) {
			return ok(views.html.sheSaid.pageError.render());
		}
		User user = userService.getUserByUid(uid);
		ShoppingOrder order = orderService
				.getShoppingOrderByOrderCode(orderCode);
		List<Product> plist = orderService.getproductListByOrderCode(orderCode);

		Product proinfo = plist == null || plist.isEmpty() ? null : plist
				.get(0);

		if (proinfo == null || order == null) {
			return ok(views.html.sheSaid.pageError.render());
		}
		// 更新产品支付状态
		String method = "11";
		String state = "20";
		productService.setPayStatus(orderCode, method, state,
				order.getTotalFee(), prepayid);
		String postmanuid = StringUtils.isBlank(session("postmanuid")) ? CdnAssets.HIGOUSHOPID
				: session("postmanuid");
		return ok(views.html.sheSaid.orderend.render(
				"/sheSaid/showorder?ordercode=" + orderCode, postmanuid));
	}

	// 订单展示完成
	public Result showorder() {
		// Long uid = Numbers.parseLong(session("uid"), 0L);//
		// Numbers.parseLong(AjaxHellper.getHttpParam(request(),
		String orderCode = AjaxHellper.getHttpParam(request(), "ordercode");
		if (StringUtils.isBlank(orderCode)) {
			return ok(views.html.sheSaid.pageError.render());
		}
		// User user = userService.getUserByUid(uid);

		ShoppingOrder order = orderService
				.getShoppingOrderByOrderCode(orderCode);
		List<Product> plist = orderService.getproductListByOrderCode(orderCode);

		Product proinfo = plist == null || plist.isEmpty() ? null : plist
				.get(0);
		if (proinfo == null) {
			return ok(views.html.sheSaid.pageError.render());
		}
		User user = userService.getUserByUid(order.getuId());
		List<Endorsement> elist = orderService.getEndorselistByOrderId(order
				.getId());
		if (elist == null || elist.isEmpty()) {
			// return ok(views.html.sheSaid.pageError.render());
			Endorsement endo = endorseService.getEndorseById(order
					.getEndorsementid());
			if (endo != null) {
				elist = new ArrayList<Endorsement>();
				elist.add(endo);
			}
		}

		String postmanid = StringUtils.isBlank(session("postmanuid")) ? CdnAssets.HIGOUSHOPID
				: session("postmanuid");
		Long pid = proinfo.getPid();
		Endorsement endorse = endorseService.getEndorseById(elist.get(0)
				.getEid());
		Integer pcount = plist == null | plist.isEmpty() ? 0 : plist.size();
		List<Long> pidlist = new ArrayList<Long>();
		if (plist.size() == 1) {
			pcount=proinfo.getCounts();
			proinfo.setRmbprice(Double.valueOf(Integer.valueOf(new BigDecimal(
					proinfo.getRmbprice())
					.setScale(0, BigDecimal.ROUND_CEILING).toString())));
			// proinfo.setEndorsementPrice(Double.valueOf(Integer.valueOf(new
			// BigDecimal(proinfo.getEndorsementPrice()).setScale(0,BigDecimal.ROUND_CEILING).toString())));
			if (endorse.getGid() != null
					&& (endorse.getGid().longValue() == 4 || endorse.getGid()
							.longValue() == 6)) {
				proinfo.setRmbprice(Double.valueOf(Integer
						.valueOf(new BigDecimal(proinfo.getChinaprice())
								.setScale(0, BigDecimal.ROUND_CEILING)
								.toString())));
				proinfo.setEndorsementPrice(endorse.getEndorsementPrice());
			} else
				proinfo.setEndorsementPrice(Double.valueOf(Integer
						.valueOf(new BigDecimal(proinfo.getEndorsementPrice())
								.setScale(0, BigDecimal.ROUND_CEILING)
								.toString())));

			proinfo.setFromobj(productService.getfrom(proinfo.getFromsite()));
			
			return ok(views.html.sheSaid.ordersuccess.render(
					order.getProvince() + order.getAddress(), proinfo, user,
					order, pcount, endorse.getGid()==null?"0":endorse.getGid().longValue()+""));
		} else {			
			Long postuid = Numbers.parseLong(session("postmanuid"), 0L);
			User postman = null;
			if (postuid.longValue() > 0)
				postman = userService.getUserByUid(postuid);
			String shoptyp="1";//0普通代言，4商户，6棒棒糖
			plist = orderService.getproductListByOrderCode(orderCode);
			pidlist = new ArrayList<Long>();
			for (Endorsement en : elist) {
				for (Product p : plist) {
					if (en.getProductId().longValue() == p.getPid().longValue()
							&& en.getGid() != null
							&& (en.getGid().longValue() == 4 || en.getGid()
									.longValue() == 6)) {
						p.setRmbprice(Double.valueOf(Integer
								.valueOf(new BigDecimal(p.getChinaprice())
										.setScale(0, BigDecimal.ROUND_CEILING)
										.toString())));
						p.setEndorsementPrice(en.getEndorsementPrice());
					
						shoptyp=en.getGid()==null?"0":en.getGid().longValue()+"";
					}
				}
				pidlist.add(en.getProductId());
			}
			Integer totalcount = 0;
			if (!StringUtils.isBlank(session("op")))
				totalcount = shoppingCartService
						.getCartEndorseAllCount(session("op"));
			int weightfee = productService.getEndorseWeightFee(pidlist,
					session("op"), totalcount).intValue();
			int costfee = productService.getEndorseRateFee(
					pidlist.toString().substring(1, pidlist.size() - 1),
					session("op")).intValue();
			
			return ok(views.html.sheSaid.ordercartsuccess.render(
					order.getProvince() + order.getAddress(), plist, user,
					order, weightfee, costfee, shoptyp));
		}
	}

	// 提交订单完成支付宝返回商城回调
	public Result alipayreturn() {
		// response().setContentType("application/json;charset=utf-8");
		String method = "21";
		String state = "20";

		String postmanid = StringUtils.isBlank(session("postmanuid")) ? CdnAssets.HIGOUSHOPID
				: session("postmanuid");
		// ObjectNode result = Json.newObject();
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
				return ok(views.html.sheSaid.pageError.render());
			}

			boolean verify_result = AlipayNotify.verifyReturn(params);

			if (!verify_result)
				return ok(views.html.sheSaid.pageError.render());

			if (out_trade_no.length() > 10) {
				out_trade_no = out_trade_no.substring(0, 10);
			}
			ShoppingOrder shoppingOrder = productService
					.getShoppingOrderByOrderCode(out_trade_no);
			if (shoppingOrder != null) {
				int status = productService.checkOrderPayStat(out_trade_no,
						shoppingOrder.getTotalFee());
				productService.setPayStatusFast(out_trade_no, method, state,
						shoppingOrder.getTotalFee(), trade_no);
				User user = userService.getUserByUid(shoppingOrder.getuId());
				List<Product> plist = orderService
						.getproductListByOrderCode(shoppingOrder.getOrderCode());
				Product proinfo = plist == null || plist.isEmpty() ? null
						: plist.get(0);

				if (proinfo == null) {
					return ok(views.html.sheSaid.pageError.render());
				}

				List<Endorsement> elist = orderService
						.getEndorselistByOrderId(shoppingOrder.getId());
				if (elist == null || elist.isEmpty()) {
					// return ok(views.html.sheSaid.pageError.render());
					Endorsement endo = endorseService
							.getEndorseById(shoppingOrder.getEndorsementid());
					if (endo != null) {
						elist = new ArrayList<Endorsement>();
						elist.add(endo);
					}
				}

				Endorsement endorse = endorseService.getEndorseById(elist
						.get(0).getEid());
				Integer pcount = plist == null | plist.isEmpty() ? 0 : plist
						.size();
				if (plist.size() == 1) {
					pcount=proinfo.getCounts();
					proinfo.setRmbprice(Double.valueOf(Integer
							.valueOf(new BigDecimal(proinfo.getRmbprice())
									.setScale(0, BigDecimal.ROUND_CEILING)
									.toString())));
					// proinfo.setEndorsementPrice(Double.valueOf(Integer.valueOf(new
					// BigDecimal(proinfo.getEndorsementPrice()).setScale(0,BigDecimal.ROUND_CEILING).toString())));
					if (endorse.getGid() != null
							&& (endorse.getGid().longValue() == 4 || endorse
									.getGid().longValue() == 6)) {
						proinfo.setRmbprice(Double.valueOf(Integer
								.valueOf(new BigDecimal(proinfo.getChinaprice())
										.setScale(0, BigDecimal.ROUND_CEILING)
										.toString())));
						proinfo.setEndorsementPrice(endorse
								.getEndorsementPrice());
					} else
						proinfo.setEndorsementPrice(Double.valueOf(Integer
								.valueOf(new BigDecimal(proinfo
										.getEndorsementPrice()).setScale(0,
										BigDecimal.ROUND_CEILING).toString())));

					proinfo.setFromobj(productService.getfrom(proinfo
							.getFromsite()));

					return ok(views.html.sheSaid.ordersuccess.render(
							shoppingOrder.getProvince()
									+ shoppingOrder.getAddress(), proinfo,
							user, shoppingOrder, pcount, postmanid));
				} else {
					List<Long> pidlist = new ArrayList<Long>();
					pidlist = new ArrayList<Long>();
					for (Endorsement en : elist) {
						for (Product p : plist) {
							if (en.getProductId().longValue() == p.getPid()
									.longValue()
									&& en.getGid() != null
									&& (en.getGid().longValue() == 4 || en
											.getGid().longValue() == 6)) {
								p.setRmbprice(Double.valueOf(Integer
										.valueOf(new BigDecimal(p
												.getChinaprice()).setScale(0,
												BigDecimal.ROUND_CEILING)
												.toString())));
								p.setEndorsementPrice(en.getEndorsementPrice());
							}
						}
						pidlist.add(en.getProductId());
					}
					Integer totalcount = 0;
					if (!StringUtils.isBlank(session("op")))
						totalcount = shoppingCartService
								.getCartEndorseAllCount(session("op"));
					int weightfee = productService.getEndorseWeightFee(pidlist,
							session("op"), totalcount).intValue();
					int costfee = productService
							.getEndorseRateFee(
									pidlist.toString().substring(1,
											pidlist.size() - 1), session("op"))
							.intValue();

					return ok(views.html.sheSaid.ordercartsuccess.render(
							shoppingOrder.getProvince()
									+ shoppingOrder.getAddress(), plist, user,
							shoppingOrder, weightfee, costfee, postmanid));
				}
			} else {
				return ok(views.html.sheSaid.pageError.render());
			}
		} else
			return ok(views.html.sheSaid.pageError.render());
	}

	// 0元结单
	public Result zeroturn() {
		String method = "21";
		String state = "20";

		String out_trade_no = AjaxHellper.getHttpParam(request(), "ordercode");// 商户订单号
		String trade_no = "";// 支付宝交易号
		String trade_status = "";// 交易状态

		if (StringUtils.isBlank(out_trade_no)) {
			return ok(views.html.sheSaid.pageError.render());
		}

		String postmanid = StringUtils.isBlank(session("postmanuid")) ? "": session("postmanuid");
		// 验证订单是否真0元
		boolean verify_result = true;
		ShoppingOrder shoppingOrder = productService
				.getShoppingOrderByOrderCode(out_trade_no);
		if (shoppingOrder.getFinalpay() > 0)
			verify_result = false;
		if (!verify_result)
			return ok(views.html.sheSaid.pageError.render());

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
					Endorsement endorse = endorseService
							.getEndorseById(shoppingOrder.getEndorsementid() == null ? 0
									: shoppingOrder.getEndorsementid()
											.longValue());
					if (proinfo == null || endorse == null) {
						return ok(views.html.sheSaid.pageError.render());
					}
					proinfo.setEndorsementPrice(Double.valueOf(Integer
							.valueOf(new BigDecimal(proinfo
									.getEndorsementPrice()).setScale(0,
									BigDecimal.ROUND_CEILING).toString())));

					if (endorse.getGid() != null
							&& (endorse.getGid().longValue() == 4 || endorse
									.getGid().longValue() == 6)) {
						proinfo.setRmbprice(Double.valueOf(Integer
								.valueOf(new BigDecimal(proinfo.getChinaprice())
										.setScale(0, BigDecimal.ROUND_CEILING)
										.toString())));
						proinfo.setEndorsementPrice(endorse
								.getEndorsementPrice());
					} else
						proinfo.setEndorsementPrice(Double.valueOf(Integer
								.valueOf(new BigDecimal(proinfo
										.getEndorsementPrice()).setScale(0,
										BigDecimal.ROUND_CEILING).toString())));

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


					return ok(views.html.sheSaid.ordersuccess.render(
							shoppingOrder.getAddress(), proinfo, user,
							shoppingOrder, pcount, postmanid));

				} else {
					return ok(views.html.sheSaid.pageError.render());
				}
			} else {
				return ok(views.html.sheSaid.pageError.render());
			}
		} else {
			return ok(views.html.sheSaid.pageError.render());
		}
	}

	public static Result order_app() {
		return ok(views.html.sheSaid.order_app.render());
	}

	public static Result order_null() {
		return ok(views.html.sheSaid.order_null.render());
	}

	public static Result showshare() {
		Long uid = Numbers.parseLong(
				AjaxHellper.getHttpParam(request(), "uid"), Long.valueOf(0));
		Long pid = Numbers.parseLong(
				AjaxHellper.getHttpParam(request(), "pid"), Long.valueOf(0));
		Long dyid = Numbers.parseLong(
				AjaxHellper.getHttpParam(request(), "daiyanid"),
				Long.valueOf(0));
		return redirect("/sheSaid/show?pid=" + pid + "&uid=" + uid
				+ "&daiyanid=" + dyid + "&wx=1");
	}

	public Result show() {
		WxSign s = H5ShoppingController.getcacheWxsign();

		Long pid = Numbers.parseLong(
				AjaxHellper.getHttpParam(request(), "pid"), Long.valueOf(0));
		Long dyid = Numbers.parseLong(
				AjaxHellper.getHttpParam(request(), "daiyanid"),
				Long.valueOf(0));
		String flg = AjaxHellper.getHttpParam(request(), "flg");
		flg = StringUtils.isBlank(flg) ? "voke" : flg;
		Long uid = Numbers.parseLong(session("uid"), 0L);
		WxSign addrSign = null;
		if (uid.longValue() > 0)
			addrSign = H5ShoppingController.getcacheWxsign();

		String domainH5 = "http://ht2.neolix.cn:9004";
		String wx = AjaxHellper.getHttpParam(request(), "wx");
		wx = wx == null ? "" : wx;
		String shareType = AjaxHellper.getHttpParam(request(), "st");
		shareType = StringUtils.isBlank(shareType) ? "" : shareType;

		String openid = session("op");
		String unionid = session("un");
		openid = openid==null || StringUtils.isBlank(openid) ? "" : openid;
		unionid = unionid==null || StringUtils.isBlank(unionid) ? "" : unionid;

		if (pid == 0L || dyid == 0L) {
			return ok(views.html.sheSaid.pageError.render());
		}
		// 取代言
		Endorsement endorse = endorseService.getEndorseDetail(dyid, 8);
		if (endorse == null) {
			return ok(views.html.sheSaid.pageError.render());
		}
		if (endorse.getProductId().compareTo(pid) != 0) {
			return ok(views.html.sheSaid.pageError.render());
		}
		if (endorse.getGid() == null)
			endorse.setGid(0L);

		if (StringUtils.isBlank(endorse.getPreImgPath()))
			endorse.setPreImgPath("http://ht.neolix.cn/pimgs/site/share.jpg");
		else
			endorse.setPreImgPath(StringUtil.getOSSUrl()
					+ endorse.getPreImgPath());

		// 取代言用户
		User user = userService.getUserByUid(endorse.getUserId());
		if (user == null)
			return ok(views.html.sheSaid.pageError.render());

		Long postmanid = user.getUid();
		if (StringUtils.isBlank(user.getPostmanid()))
			postmanid = Numbers.parseLong(CdnAssets.HIGOUSHOPID, 0L);

		if (user.getHeadIcon() == null || user.getHeadIcon().equals("")) {
			if (user.getSex() != null && user.getSex().equals("1"))
				user.setHeadIcon(domainimg
						+ "images/sheSaidImages/H5_boy_48x48.png");
			else
				user.setHeadIcon(domainimg
						+ "images/sheSaidImages/H5_girl_48x48.png");
		}
		// 取商品
		Product proinfo = productService.getProductById(pid);
		List<Product_images> imglist=productService.getProductImages(pid);
		if(imglist!=null && !imglist.isEmpty())
			proinfo.setListpic(imglist.get(0).getPicname());
		
		if (proinfo == null || user == null)
			return ok(views.html.sheSaid.pageError.render());
		if (proinfo.getIshot() == 1) {// 组合商品
			proinfo.setNstock(productService.dealNstockWithProduct(pid));
		}

		proinfo.setProUnion(productService.getproUnion(pid));
		// 取当前登录用户
		Long tuid = Numbers.parseLong(session("uid"), 0L);
		User usr = null;
		if(!StringUtils.isBlank(openid) && !StringUtils.isBlank(unionid))
			usr=userService.getUserByopenid(openid, unionid);
		if(usr==null){	
			usr=new User();
			usr.setUid(0L);
			usr.setOpenId(openid);
			usr.setUnionid(unionid);
		}

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

		proinfo.setRmbprice(new BigDecimal(proinfo.getRmbprice()).setScale(2,
				BigDecimal.ROUND_CEILING).doubleValue());
		// proinfo.setEndorsementPrice(Double.valueOf(Integer.valueOf(new
		// BigDecimal(proinfo.getEndorsementPrice()).setScale(0,BigDecimal.ROUND_CEILING).toString())));
		if (endorse.getGid() != null
				&& (endorse.getGid().longValue() == 4 || endorse.getGid()
						.longValue() == 6)) {
			proinfo.setRmbprice(new BigDecimal(proinfo.getChinaprice())
					.setScale(2, BigDecimal.ROUND_CEILING).doubleValue());
			proinfo.setEndorsementPrice(endorse.getEndorsementPrice());
		} else
			proinfo.setEndorsementPrice(new BigDecimal(proinfo
					.getEndorsementPrice()).setScale(2,
					BigDecimal.ROUND_CEILING).doubleValue());

		if (proinfo.getEndorsementPrice() == null)
			proinfo.setEndorsementPrice(Double.valueOf(0));

		if (!StringUtils.isBlank(proinfo.getNationalFlag())) {
			// proinfo.setNationalFlag(domain+"/pimgs/site/"+proinfo.getNationalFlag());
			proinfo.setNationalFlag("http://ht.neolix.cn/pimgs/site/"
					+ proinfo.getNationalFlag());
		} else
			proinfo.setNationalFlag(null);

		// proinfo.setPrice(Double.valueOf(rmb_price.toString()));

		// 算折扣
		if (proinfo.getRmbprice() > 0) {
			BigDecimal mData = new BigDecimal(10
					* proinfo.getEndorsementPrice() / proinfo.getRmbprice())
					.setScale(1, BigDecimal.ROUND_UP);
			proinfo.setExtcode(mData.toString());
		} else
			proinfo.setExtcode("");
		Page<Comment> comments = commentService.commentPage(0, pid);
		List<Comment> listcomment = null;
		if (comments != null) {
			listcomment = comments.getContent();
		}

		proinfo.setPostfee(weightfee);
		if (proinfo.getIshot() == 1) {
			proinfo.setNstock(productService.dealNstockWithProduct(proinfo
					.getPid()));
		}

		String ptim = this.CHINESE_D_TIME_FORMAT.format(new Date());
		// 取产品详情
		List<ProductDetail> pdlist = productService.getdetailist(proinfo
				.getSkucode());
		// H5分享调用微信获取TOKEN，jsp_tackit
		WxSign wxsign = this.getwxstr();
		wxsign.setSharecontent("嗨个购—与你一起买世界");
		wxsign.setShareurl(domainH5 + "/sheSaid/endorsement?daiyanid="
				+ endorse.getEid());
		wxsign.setSharetitle(StringUtil.getShesaidTitle(user.getNickname()));
		// wxsign.setShareimg(endorse.getPreImgPath());

		if (endorse.getGid() != null && endorse.getGid().longValue() == 6) {
			Long postmanuid = endorse.getUserId();// Numbers.parseLong(session("postmanuid"),
													// 0L);
			session("postmanuid", endorse.getUserId().longValue() + "");

			User postman = null;
			if (postmanuid.longValue() > 0)
				postman = userService.getUserByUid(postmanuid);
			if(endorse.getGid()!=null && endorse.getGid().longValue()==6){
				wxsign.setSharecontent("Hi-嗨个购");
				wxsign.setSharetitle("嗨个购-与你一起买世界");
				wxsign.setShareimg(StringUtil.getDomainH5()+"/public/images/sheSaidImages/hi.gif");
				wxsign.setShareurl(StringUtil.getDomainH5()+"/H5/shoplist");
			}else{
				if (postman == null || StringUtils.isBlank(postman.getPostmanid())) {
					wxsign.setShareimg(endorse.getProducinfo().getListpic());
					wxsign.setSharetitle("我刚才看到了一个海淘商品，又要败家了，一起来吧！");
					wxsign.setSharecontent(endorse.getProducinfo().getSubtitle());
					wxsign.setShareurl(StringUtil.getDomainH5()
							+ "/sheSaid/showshare?uid=" + user.getUid() + "&pid="
							+ pid + "&daiyanid=" + dyid);
				} else {
					wxsign.setShareimg(StringUtil.getDomainH5()
							+ "/public/images/sheSaidImages/hi.gif");
					wxsign.setSharetitle("我是快递员" + postman.getNickname()
							+ ",我能帮你带来最优惠的价格");
					wxsign.setSharecontent("棒棒糖商城");
					wxsign.setShareurl(StringUtil.getDomainH5()
							+ "/H5/prolist?uid=" + postman.getUid());
				}
			}
		}

		Integer cartcount = 0;
		if (!StringUtils.isBlank(session("op"))) {
			cartcount = shoppingCartService
					.getCartEndorseAllCount(session("op"));
			if (cartcount == null)
				cartcount = 0;
		}
		// 设置输出状态
		String sta = "1";// 设置个状态显示办理用,1显示立即买，11显示立即购买和加购物车，2抢光及下架，3定时及预售,4新人购买,00元购买
		if ((proinfo.getProUnion() == null || !proinfo.getProUnion()
				.getBuyNowFlag().equals("1"))
				&& (endorse.getGid() != null && (endorse.getGid().longValue() == 4 || endorse
						.getGid().longValue() == 6)))
			sta = "11";
		if (proinfo.getStatus() != 10 || proinfo.getNstock().longValue() < 1)
			sta = "2";
		if (proinfo.getStatus() == 10 && proinfo.getNstock().longValue() > 0) {
			if (proinfo.getPtyp() != null
					&& (proinfo.getPtyp().equals("3") || (proinfo.getPtyp()
							.equals("4") && proinfo.getBtim() != null && proinfo
							.getBtim().compareTo(
									CHINESE_D_TIME_FORMAT.format(new Date())
											.toString()) >= 0)))
				sta = "3";

			if (!proinfo.getNewMantype().equals("0") && !proinfo.getNewMantype().equals("3"))
				sta = "4";
			if (proinfo.getEndorsementPrice().intValue() == 0)
				sta = "0";// 0元购买
		}
		String wxtoken = "";
		if (uid.longValue() > 0
				&& !StringUtils.isBlank(cache.get("wx_access_token_voke"
						+ session("uid"))))
			wxtoken = cache.get("wx_access_token_voke" + session("uid"));
		String backurl = StringUtil.getDomainH5() + "/H5/order_address?pid="
				+ proinfo.getPid() + "&daiyanid=" + endorse.getEid();
		return ok(views.html.sheSaid.show.render(proinfo, user, listcomment,
				dyid, pdlist, wxsign, shareType, cartcount, usr, flg, endorse,
				sta, addrSign, Constants.WXappID, URLEncoder.encode(backurl),
				wxtoken));
	}

	public static Result getsignstri() {
		String tim = Form.form().bindFromRequest().get("tim");
		String nostr = Form.form().bindFromRequest().get("nostr");
		String jkt = Form.form().bindFromRequest().get("jkt");
		String uri = Form.form().bindFromRequest().get("uri");
		// uri=uri.replace("&", "%26").replace(":9004", "");

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

	// 微信鉴权获取用户信息
	public Result wxauth() {
		String uid = AjaxHellper.getHttpParam(request(), "uid");
		Long pid = Numbers.parseLong(
				AjaxHellper.getHttpParam(request(), "pid"), 0L);
		String dyid = AjaxHellper.getHttpParam(request(), "daiyanid");
		String flg = AjaxHellper.getHttpParam(request(), "flg");
		flg = StringUtils.isBlank(flg) ? "order" : "praise";
		String shareType = AjaxHellper.getHttpParam(request(), "st");
		if (StringUtils.isBlank(shareType))
			shareType = "";

		String domains = StringUtil.getDomainH5();

		if (StringUtils.isBlank(uid) || pid == 0L || StringUtils.isBlank(dyid)) {
			return ok(views.html.sheSaid.pageError.render());
		}
		String access_token = "";
		String openid = "";
		String unionid = "";
		String redirecturl = java.net.URLEncoder.encode(StringUtil
				.getDomainH5()
				+ "/sheSaid/vokewx?uid="
				+ uid
				+ "&pid="
				+ pid
				+ "&daiyanid=" + dyid + "&shareType=" + shareType);
		String wxauthURL = "https://open.weixin.qq.com/connect/oauth2/authorize?appid="
				+ Constants.WXappID
				+ "&redirect_uri="
				+ redirecturl
				+ "&response_type=code&scope=snsapi_userinfo&state=123#wechat_redirect";
		// wxauthURL="https://open.weixin.qq.com/connect/oauth2/authorize?appid="+Constants.WXappID+"&redirect_uri="+redirecturl+"&response_type=code&scope=snsapi_base&state=123&connect_redirect=1#wechat_redirect";
		if (flg.equals("praise")) {
			logger.info(uid + "点赞鉴权");
			redirecturl = URLEncoder.encode(StringUtil.getDomainH5()
					+ "/sheSaid/praise?uid=" + uid + "&pid=" + pid
					+ "&daiyanid=" + dyid + "&shareType=" + shareType);
			wxauthURL = "https://open.weixin.qq.com/connect/oauth2/authorize?appid="
					+ Constants.WXappID
					+ "&redirect_uri="
					+ redirecturl
					+ "&response_type=code&scope=snsapi_userinfo&state=123#wechat_redirect";
			return redirect(wxauthURL);
		} else {
			CountH5 cnt = new CountH5();
			String channel = "daiyan";
			Endorsement endorse = endorseService.getEndorseById(Numbers
					.parseLong(dyid, 0L));
			if (endorse != null) {
				if (endorse.getGid() != null
						&& (endorse.getGid().longValue() == 4 || endorse
								.getGid().longValue() == 6))
					channel = "daiyanshanghu";
			}
			channel = channel + "wxauth";
			cnt.setChannel(channel);
			cnt.setIp(request().remoteAddress());
			cnt.setIswx("1");
			cnt.setShareType("");
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

			String urls = "http://" + request().host() + request().path() + qry;

			cnt.setUrl(urls);
			cnt.setCreateTime(new Date());
			cnt.setUserId(Numbers.parseLong(uid, 0L));
			cnt.setDaiyanid(Numbers.parseLong(dyid, 0L));
			endorseService.saveCount(cnt);
			wxauthURL = "https://open.weixin.qq.com/connect/oauth2/authorize?appid="
					+ Constants.WXappID
					+ "&redirect_uri="
					+ redirecturl
					+ "&response_type=code&scope=snsapi_base&state=123&connect_redirect=1#wechat_redirect";
			logger.info(uid + "订购鉴权");
			return redirect(wxauthURL);
		}

	}

	// 微信鉴权购物车使用
	public Result wxauthcart() {
		Long pid = Numbers.parseLong(
				AjaxHellper.getHttpParam(request(), "pid"), 0L);
		String dyid = AjaxHellper.getHttpParam(request(), "daiyanid");
		dyid = StringUtils.isBlank(dyid) ? "" : dyid;
		String flg = AjaxHellper.getHttpParam(request(), "flg");
		flg = StringUtils.isBlank(flg) ? "voke" : flg;

		String redirecturl = java.net.URLEncoder.encode(StringUtil
				.getDomainH5()
				+ "/sheSaid/vokewxcart?pid="
				+ pid
				+ "&daiyanid=" + dyid + "&flg=" + flg);

		String wxauthURL = "https://open.weixin.qq.com/connect/oauth2/authorize?appid="
				+ Constants.WXappID
				+ "&redirect_uri="
				+ redirecturl
				+ "&response_type=code&scope=snsapi_userinfo&state=123#wechat_redirect";
		CountH5 cnt = new CountH5();
		String channel = "daiyan";
		Endorsement endorse = endorseService.getEndorseById(Numbers.parseLong(
				dyid, 0L));
		if (endorse != null) {
			if (endorse.getGid() != null
					&& (endorse.getGid().longValue() == 4 || endorse.getGid()
							.longValue() == 6))
				channel = "daiyanshanghu";
		}
		channel = channel + "wxauth";
		cnt.setChannel(channel);
		cnt.setIp(request().remoteAddress());
		cnt.setIswx("1");
		cnt.setShareType("");
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

		String urls = "http://" + request().host() + request().path() + qry;

		cnt.setUrl(urls);
		cnt.setCreateTime(new Date());
		cnt.setUserId(Numbers.parseLong(session("uid"), 0L));
		cnt.setDaiyanid(Numbers.parseLong(dyid, 0L));
		endorseService.saveCount(cnt);
		wxauthURL = "https://open.weixin.qq.com/connect/oauth2/authorize?appid="
				+ Constants.WXappID
				+ "&redirect_uri="
				+ redirecturl
				+ "&response_type=code&scope=snsapi_base&state=123&connect_redirect=1#wechat_redirect";
		return redirect(wxauthURL);
	}

	// 微信鉴权回调base授权
	public Result vokewx() {
		Long uid = Numbers.parseLong(
				AjaxHellper.getHttpParam(request(), "uid"), 0L);
		Long pid = Numbers.parseLong(
				AjaxHellper.getHttpParam(request(), "pid"), 0L);
		Long dyid = Numbers.parseLong(
				AjaxHellper.getHttpParam(request(), "daiyanid"), 0L);
		String shareType = AjaxHellper.getHttpParam(request(), "shareType");
		Endorsement endorse = endorseService.getEndorseById(dyid);
		if (pid == 0L || dyid == 0L || endorse == null) {
			return ok(views.html.sheSaid.pageError.render());
		}

		User usert = null;
		if (usert == null) {
			usert = new User();
			usert.setOpenId("");
			usert.setUnionid("");
		}
		// 先进行基础授权，如果是第一次没有用户妮称等信息则进行用户级授权

		String access_token = "";
		String openid = "";
		String unionid = "";
		String refresh_token = "";
		String code = "";
		String state = "";
		code = StringUtils.isBlank(code) ? "" : code;
		state = StringUtils.isBlank(state) ? "" : state;
		WxSign tmpsign = H5ShoppingController.getcacheWxsign();
		if (tmpsign == null || StringUtils.isBlank(tmpsign.getAccess_token())) {
			JsonNode res = H5ShoppingController.getwxtoken();
			if (res != null) {
				access_token = res.get("access_token").textValue();
				openid = res.get("openid").textValue();
				unionid = res.get("unionid").textValue();
				code = res.get("code").textValue();
				state = res.get("state").textValue();
			}
		} else {
			access_token = tmpsign.getAccess_token();
			openid = tmpsign.getOpenid();
			unionid = tmpsign.getUnionid();
			code = tmpsign.getCode();
			state = tmpsign.getState();
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

			// cache.set(openid+"unionid", unionid);
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
			session("uid", String.valueOf(ust.getUid().longValue()));
		}
		session("op", openid);
		session("un", unionid);
		// 重置cache wxcontant
		WxSign adSign = new WxSign();
		adSign.setOpenid(openid);
		adSign.setUnionid(unionid);
		adSign.setAccess_token(access_token);
		adSign.setAppId(Constants.WXappID);
		adSign.setTimstr(RandomStringUtils.randomAlphanumeric(16));
		adSign.setNostr(Sha1Util.getTimeStamp());
		adSign.setCode(code);
		adSign.setState(state);
		if (usert.getUid().longValue() > 0)
			cache.setObject("wxcontant" + session("uid"), adSign, 5400);

		return redirect("/sheSaid/order?pid=" + pid + "&daiyanid=" + dyid
				+ "&code=" + code + "&state=" + state);
	}

	// 微信鉴权购物车处理
	public Result vokewxcart() {
		Long pid = Numbers.parseLong(
				AjaxHellper.getHttpParam(request(), "pid"), 0L);
		Long dyid = Numbers.parseLong(
				AjaxHellper.getHttpParam(request(), "daiyanid"), 0L);
		String flg = AjaxHellper.getHttpParam(request(), "flg");
		flg = StringUtils.isBlank(flg) ? "voke" : flg;

		// Endorsement endorse = endorseService.getEndorseById(dyid);

		String code = AjaxHellper.getHttpParam(request(), "code");

		User usert = null;
		if (usert == null) {
			usert = new User();
			usert.setOpenId("");
			usert.setUnionid("");
		}
		String access_token = "";
		String openid = "";
		String unionid = "";
		String state = "";
		WxSign tmpsign = H5ShoppingController.getcacheWxsign();
		if (tmpsign == null || StringUtils.isBlank(tmpsign.getAccess_token())) {
			// 先进行基础授权，如果是第一次没有用户妮称等信息则进行用户级授权
			JsonNode res = H5ShoppingController.getwxtoken();
			if (res != null) {
				access_token = res.get("access_token").textValue();
				openid = res.get("openid").textValue();
				unionid = res.get("unionid").textValue();
			}
		} else {

			access_token = tmpsign.getAccess_token();
			openid = tmpsign.getOpenid();
			unionid = tmpsign.getUnionid();
			code = tmpsign.getCode();
			state = tmpsign.getState();
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
			session("uid", String.valueOf(ust.getUid().longValue()));
		}
		
		if (flg.equals("voke")) {
			if (!StringUtils.isBlank(openid)) {
				JsonNode result = addCartvoke(String.valueOf(openid),
						String.valueOf(dyid.longValue()),
						String.valueOf(pid.longValue()), 1);
				String msg=StringUtils.isBlank(result.get("msg").textValue())?"":"1";
				if (result.get("status").textValue().equals("1")) {
					return redirect("/sheSaid/ordercart?eid=" + dyid);
				} else
					return redirect("/sheSaid/ordercart?msg="+msg+"&eid=" + dyid);
			} else
				return redirect("/sheSaid/ordercart?msg="
						+ URLEncoder.encode("鉴权失败") + "&eid=" + dyid);
		} else
			return redirect("/sheSaid/ordercart?eid=" + dyid);
	}

	// 微信鉴权回调 点赞处理
	public Result praise() {
		Long uid = Numbers.parseLong(
				AjaxHellper.getHttpParam(request(), "uid"), 0L);
		Long dyid = Numbers.parseLong(
				AjaxHellper.getHttpParam(request(), "daiyanid"), 0L);
		String shareType = AjaxHellper.getHttpParam(request(), "st");
		if (dyid == 0L) {
			return ok(views.html.sheSaid.pageError.render());
		}
		String flg = AjaxHellper.getHttpParam(request(), "flg");

		String isLiked = "2";
		flg = StringUtils.isBlank(flg) ? "" : flg;

		if (flg.equals("praise")) {
			if (uid > 0) {
				User ust = userService.getUserByUid(uid);
				if (ust != null) {
					// 点赞
					Boolean sucs = endorseService.endorsmentPraise(dyid, ust);
					if (sucs)
						isLiked = "1";
					return redirect("/sheSaid/endorsement?uid=" + uid
							+ "&daiyanid=" + dyid + "&il=" + isLiked
							+ "&shareType=" + shareType);
				}
			}
		}
		String openid = "";
		String unionid = "";
		String access_token = "";
		String nickname = "";
		WxSign wxsign = H5ShoppingController.getcacheWxsign();
		if (wxsign != null && !StringUtils.isBlank(wxsign.getAccess_token())) {
			openid = wxsign.getOpenid();
			unionid = wxsign.getUnionid();
			access_token = wxsign.getAccess_token();
		} else {
			JsonNode nd = H5ShoppingController.getwxtoken();
			if (nd != null) {
				openid = nd.get("openid").textValue();
				unionid = nd.get("unionid").textValue();
				access_token = nd.get("access_token").textValue();
			}
		}
		User usert = new User();
		usert.setUid(0L);
		access_token = access_token == null ? "" : access_token;
		openid = openid == null ? "" : openid;
		unionid = unionid == null ? "" : unionid;
		if (!access_token.equals("") && !openid.equals("")
				&& !unionid.equals("")) {

			User ust = userService.getUserByopenid(openid, unionid);
			if (ust != null)
				usert = ust;
			// 如果用户存在则跳到购物页面
			String headicon = "";
			usert.setOpenId(openid);
			usert.setHeadIcon("");
			usert.setNickname("");
			usert.setUnionid(unionid);
			// 不存在则跳到输入手机号注册页面
			String wxgetinfourl = "https://api.weixin.qq.com/sns/userinfo?access_token="
					+ access_token + "&openid=" + openid + "&lang=zh_CN";

			try {
				// 发送请求，返回json
				HttpClient clientc = new DefaultHttpClient();
				HttpGet get = new HttpGet(wxgetinfourl);

				String resContenti = "";
				HttpResponse resc = clientc.execute(get);
				if (resc.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
					HttpEntity entity = resc.getEntity();

					resContenti = EntityUtils.toString(resc.getEntity(),
							"UTF-8");
					Logger.info("调用getAccessToken 接口返回报文内容:" + resContenti);
					JSONObject jsons = JSONObject.fromObject(resContenti);

					if (!StringUtils.isBlank(jsons.getString("nickname"))) {
						usert.setNickname(jsons.getString("nickname"));
						session().put("nickname", usert.getNickname());
					}
					if (!StringUtils.isBlank(jsons.getString("headimgurl"))) {
						usert.setHeadIcon(jsons.getString("headimgurl"));
						session().put("headicon", usert.getHeadIcon());
					}
					logger.info("nickname22222222211111111111111111111111111"
							+ usert.getNickname());
					// 点赞
					Boolean sucs = endorseService.endorsmentPraise(dyid,
							usert.getHeadIcon(), openid, unionid, nickname);
					if (sucs)
						isLiked = "1";
				}
			} catch (Exception e) {
				// return
				// ok(views.html.sheSaid.pageError.render());
				logger.error("微信二次鉴权返回参数解析失败" + e.toString());
			}
			// 点赞
			if (ust != null) {
				Boolean sucs = endorseService.endorsmentPraise(dyid,
						usert.getHeadIcon(), openid, unionid,
						usert.getNickname());
				if (sucs)
					isLiked = "1";
			}
		}

		return redirect("/sheSaid/endorsement?uid=" + uid + "&daiyanid=" + dyid
				+ "&il=" + isLiked + "&shareType=" + shareType);
	}

	// 微信提交预支付
	public Result orderx() {
		String orderCode = AjaxHellper.getHttpParam(request(), "orderCode");
		// String pid = AjaxHellper.getHttpParam(request(), "pid");

		if (StringUtils.isBlank(orderCode)) {
			return ok(views.html.sheSaid.pageError.render());
		}

		ShoppingOrder order = orderService
				.getShoppingOrderByOrderCode(orderCode);
		if (order == null)
			return ok(views.html.sheSaid.pageError.render());

		Endorsement endorse = new Endorsement();
		List<Product> plist = orderService.getproductListByOrderCode(orderCode);

		Product proinfo = plist == null || plist.isEmpty() ? null : plist
				.get(0);

		if (order.getEndorsementid() != null)
			endorse = endorseService.getEndorseById(order.getEndorsementid());
		else {
			List<Endorsement> elist = orderService
					.getEndorselistByOrderId(order.getId());
			if (elist != null && !elist.isEmpty())
				endorse = elist.get(0);
		}
		String code = AjaxHellper.getHttpParam(request(), "code");

		String prepayid = "";
		String nostr = RandomStringUtils.randomAlphanumeric(32);
		String sign = "";
		String openid = session("op");
		String unionid = session("un");
		String access_token = "";
		String state = "";
		WxSign tmpsign = H5ShoppingController.getcacheWxsign();
		if (tmpsign == null || StringUtils.isBlank(tmpsign.getAccess_token())) {
			JsonNode res = H5ShoppingController.getwxtoken();
			if (res != null) {
				access_token = res.get("access_token").textValue();
				openid = res.get("openid").textValue();
				unionid = res.get("unionid").textValue();
				code = res.get("code").textValue();
				state = res.get("state").textValue();
			}
		} else {
			access_token = tmpsign.getAccess_token();
			openid = tmpsign.getOpenid();
			unionid = tmpsign.getUnionid();
			code = tmpsign.getCode();
			state = tmpsign.getState();
		}

		try {
			if (!access_token.equals("") && !StringUtils.isBlank(openid)
					&& !StringUtils.isBlank(unionid)) {
				// if(1==1){
				// 如果正确返回调用预支付订单接口
				String wxorderurl = "https://api.mch.weixin.qq.com/pay/unifiedorder";
				Map<String, String> prams = new HashMap<String, String>();
				WxRequest rq = new WxRequest();

				prams.put("appid", Constants.WXappID);
				prams.put("attach", order.getName());
				rq.setAttach(order.getName());
				prams.put("body", "嗨个购-" + order.getOrderCode());
				rq.setBody("嗨个购-" + order.getOrderCode());
				prams.put("mch_id", Constants.WXMCID);

				prams.put("nonce_str", nostr);
				rq.setNonce_str(nostr);

				// prams.put("notify_url",URLEncoder.encode(notify_url));
				// rq.setNotify_url(URLEncoder.encode(notify_url));

				prams.put("notify_url", notify_url);
				rq.setNotify_url(notify_url);
				logger.info("微信回调地址：" + rq.getNotify_url());

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

				if (endorse.getWxMinus() != null && endorse.getWxMinus() == 1) {
					// 取随机立减商品标识
					// 取1到10的随机数
					if (endorse.getGoodsTagNum() != null
							&& endorse.getGoodsTagNum() > 0
							&& !StringUtils.isBlank(endorse.getGoodsTag())) {
						java.util.Random random = new java.util.Random();
						int result = random.nextInt(endorse.getGoodsTagNum());
						String goods_tag = endorse.getGoodsTag() + (result + 1)
								+ "";
						prams.put("goods_tag", goods_tag);
						rq.setGoods_tag(goods_tag);
					}
				}

				prams.put("trade_type", "JSAPI");

				sign = StringUtil.getSign(prams);
				// sign=Sha1Util.createSHA1Sign(prams)
				prams.put("sign", sign);
				rq.setSign(sign);

				/* String postData =gson.toJson(packageParams); */
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
		if (StringUtils.isBlank(prepayid)) {
			// return ok(views.html.sheSaid.pageError.render());
			return redirect("/sheSaid/showorder?ordercode=" + orderCode);
		} else {

			return redirect("/sheSaid/wxpayh5?nostr=" + nostr + "&sign=" + sign
					+ "&ordercode=" + orderCode + "&payid=" + prepayid
					+ "&uid=" + order.getuId() + "&pid=" + proinfo.getPid());

		}
	}

	public Result wxpayh5() {
		String nostr = AjaxHellper.getHttpParam(request(), "nostr");
		String sign = AjaxHellper.getHttpParam(request(), "sign");
		String orderCode = AjaxHellper.getHttpParam(request(), "ordercode");
		String payid = AjaxHellper.getHttpParam(request(), "payid");
		String uid = AjaxHellper.getHttpParam(request(), "uid");
		String aid = AjaxHellper.getHttpParam(request(), "aid");
		String pid = AjaxHellper.getHttpParam(request(), "pid");
		pid = StringUtils.isBlank(pid) ? "0" : pid;

		Map<String, String> pramt = new HashMap<String, String>();
		nostr = RandomStringUtils.randomAlphanumeric(32);
		String timstr = Sha1Util.getTimeStamp();
		pramt.put("appId", Constants.WXappID);
		pramt.put("timeStamp", timstr);
		pramt.put("nonceStr", nostr);
		pramt.put("package", "prepay_id=" + payid);
		pramt.put("signType", "MD5");
		sign = StringUtil.getSign(pramt);

		ShoppingOrder order = orderService
				.getShoppingOrderByOrderCode(orderCode);
		if (order == null) {
			return ok(views.html.sheSaid.pageError.render());
		}
		Endorsement endorse = new Endorsement();
		if (order.getEndorsementid() != null)
			endorse = endorseService.getEndorseById(order.getEndorsementid());
		else {
			List<Endorsement> elist = orderService
					.getEndorselistByOrderId(order.getId());
			if (elist != null && !elist.isEmpty())
				endorse = elist.get(0);
		}

		return ok(views.html.sheSaid.wxpayh5.render(Constants.WXappID, timstr,
				nostr, payid, sign, orderCode, uid, aid, pid));
	}

	// H5代言页面
	public Result endorsement() {
		Long daiyanid = Numbers.parseLong(
				AjaxHellper.getHttpParam(request(), "daiyanid"), 0L);
		Integer islike = Numbers.parseInt(
				AjaxHellper.getHttpParam(request(), "il"), 0);
		if (daiyanid == 0L) {
			return ok(views.html.sheSaid.pageError.render());
		}
		String shareType = AjaxHellper.getHttpParam(request(), "st");
		if (StringUtils.isBlank(shareType))
			shareType = "";

		Endorsement endorse = endorseService.getEndorseDetail(daiyanid, 8);

		if (endorse == null)
			return ok(views.html.sheSaid.pageError.render());

		User user = userService.getUserByUid(endorse.getUserId());

		if (user == null)
			return ok(views.html.sheSaid.pageError.render());

		if (user.getHeadIcon() == null || user.getHeadIcon().equals("")) {
			if (user.getSex() != null && user.getSex().equals("1"))
				user.setHeadIcon(domainimg
						+ "images/sheSaidImages/H5_boy_48x48.png");
			else
				user.setHeadIcon(domainimg
						+ "images/sheSaidImages/H5_girl_48x48.png");
		}

		boolean IsProduct = Configuration.root()
				.getBoolean("production", false);

		String domainH5 = StringUtil.getDomainH5();

		String domainimg = StringUtil.getOSSUrl();

		if (endorse.getEndorsImgList() != null
				&& !endorse.getEndorsImgList().isEmpty()) {
			endorse.setEndorsImgList(endorse.getEndorsImgList());
		} else
			endorse.setEndorsImgList(null);
		if (endorse.getProducinfo().getRmbprice() == null)
			endorse.getProducinfo().setRmbprice(Double.valueOf("0"));
		if (endorse.getProducinfo().getEndorsementPrice() == null)
			endorse.getProducinfo().setEndorsementPrice(Double.valueOf("0"));

		endorse.getProducinfo().setRmbprice(
				Double.valueOf(Integer.valueOf(new BigDecimal(endorse
						.getProducinfo().getRmbprice()).setScale(0,
						BigDecimal.ROUND_CEILING).toString())));
		if (endorse.getGid() != null
				&& (endorse.getGid().longValue() == 4 || endorse.getGid()
						.longValue() == 6)) {
			endorse.getProducinfo().setRmbprice(
					Double.valueOf(Integer.valueOf(new BigDecimal(endorse
							.getProducinfo().getChinaprice()).setScale(0,
							BigDecimal.ROUND_CEILING).toString())));
			endorse.getProducinfo().setEndorsementPrice(
					endorse.getEndorsementPrice());
		} else
			endorse.getProducinfo().setEndorsementPrice(
					Double.valueOf(Integer.valueOf(new BigDecimal(endorse
							.getProducinfo().getEndorsementPrice()).setScale(0,
							BigDecimal.ROUND_CEILING).toString())));

		if (endorse.getProducinfo().getIshot() == 1) {// 组合商品
			endorse.getProducinfo()
					.setNstock(
							productService.dealNstockWithProduct(endorse
									.getProductId()));
		}

		if (StringUtils.isBlank(endorse.getPreImgPath()))
			endorse.setPreImgPath("http://ht.neolix.cn/pimgs/site/share.jpg");
		else
			endorse.setPreImgPath(domainimg + endorse.getPreImgPath());

		/*
		 * //发放代金券 String
		 * mcidsend=Constants.WXMCID+CHINESE_DATE_TIME_FORMAT.format(new
		 * Date()); String nosStrsend=RandomStringUtils.randomAlphanumeric(32);
		 * Map<String, String> pramsend = new HashMap<String, String>(); String
		 * sendwxcoupon
		 * ="https://api.mch.weixin.qq.com/mmpaymkttransfers/send_coupon"
		 * ;//"222997" StringBuilder sendcountstr=new StringBuilder();
		 * sendcountstr.append("<xml>");
		 * sendcountstr.append("<appid>"+Constants.WXappID+"</appid>");
		 * pramsend.put("appid", Constants.WXappID);
		 * sendcountstr.append("<coupon_stock_id>221785</coupon_stock_id>");
		 * pramsend.put("coupon_stock_id", "221785");
		 * sendcountstr.append("<mch_id>"+Constants.WXMCID+"</mch_id>");
		 * pramsend.put("mch_id", Constants.WXMCID);
		 * sendcountstr.append("<nonce_str>"+nosStrsend+"</nonce_str>");
		 * pramsend.put("nonce_str", nosStrsend);
		 * sendcountstr.append("<openid>oJdLot5g9n-CQZB0I8EWbOMYywIg</openid>");
		 * pramsend.put("openid", "oJdLot5g9n-CQZB0I8EWbOMYywIg");
		 * sendcountstr.append("<openid_count>1</openid_count>");
		 * pramsend.put("openid_count", "1");
		 * sendcountstr.append("<partner_trade_no>"
		 * +mcidsend+"</partner_trade_no>"); pramsend.put("partner_trade_no",
		 * mcidsend); String signsend=StringUtil.getSign(pramsend);
		 * sendcountstr.append("<sign>"+signsend+"</sign>");
		 * sendcountstr.append("</xml>"); try{ //指定读取证书格式为PKCS12 KeyStore
		 * keyStore = KeyStore.getInstance("PKCS12"); //读取本机存放的PKCS12证书文件
		 * FileInputStream instream = new FileInputStream(new
		 * File("H:/wxclientkey/apiclient_cert.p12")); try { //指定PKCS12的密码(商户ID)
		 * keyStore.load(instream, "1235413502".toCharArray()); }
		 * catch(Exception e){ logger.info(e.toString()); }finally {
		 * instream.close(); } SSLContext sslcontext = SSLContexts.custom()
		 * .loadKeyMaterial(keyStore, "1235413502".toCharArray()).build();
		 * SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(
		 * 
		 * sslcontext,new String[] { "TLSv1" },null,
		 * 
		 * SSLConnectionSocketFactory.BROWSER_COMPATIBLE_HOSTNAME_VERIFIER);
		 * CloseableHttpClient httpclient = HttpClients.custom()
		 * .setSSLSocketFactory(sslsf)
		 * 
		 * .build(); HttpPost postsend = new HttpPost(sendwxcoupon);
		 * postsend.setHeader("Content-Type", "text/xml");
		 * 
		 * postsend.setEntity(new StringEntity(sendcountstr.toString()));
		 * HttpResponse resSend = httpclient.execute(postsend); String
		 * strResultsend = EntityUtils.toString(resSend.getEntity());
		 * 
		 * logger.info("发送优惠券返回结果 send coupon result:" + strResultsend);
		 * }catch(Exception e){}
		 */

		WxSign wxsign = this.getwxstr();
		wxsign.setSharecontent("嗨个购—与你一起买世界");
		wxsign.setShareurl(domainH5 + "/sheSaid/endorsement?daiyanid="
				+ endorse.getEid());
		wxsign.setSharetitle(StringUtil.getShesaidTitle(user.getNickname()));
		// wxsign.setShareimg(endorse.getPreImgPath());

		return ok(views.html.sheSaid.endorsement.render(endorse, user, islike,
				wxsign, shareType));
	}

	public Result wxpaynotice() {
		return ok(views.html.sheSaid.testUpload.render());
	}

	// 支付宝支付

	public Result alipay() {

		String tradeno = AjaxHellper.getHttpParam(request(), "out_trade_no");
		// Double
		// totalfee=Numbers.parseDouble(AjaxHellper.getHttpParam(request(),
		// "total_fee"),Double.valueOf(0));
		Long uid = Numbers.parseLong(
				AjaxHellper.getHttpParam(request(), "uid"), 0L);
		Long pid = Numbers.parseLong(
				AjaxHellper.getHttpParam(request(), "pid"), 0L);
		String orderCode = tradeno;// AjaxHellper.getHttpParam(request(),"ordercode");

		if (StringUtils.isBlank(tradeno) || uid == 0L) {
			return ok(views.html.sheSaid.pageError.render());
		}

		ShoppingOrder order = orderService
				.getShoppingOrderByOrderCode(orderCode);
		if (order == null)
			return ok(views.html.sheSaid.pageError.render());
		Double totalfee = order.getTotalFee();

		String domains = StringUtil.getDomainH5();

		String backurl = URLEncoder.encode(domains + "/sheSaid/alipayreturn");

		return redirect("/api/alipaywapendorse?ordercode=" + tradeno
				+ "&amount=" + totalfee + "&backurl=" + backurl);
		// return
		// ok(views.html.sheSaid.alipay.render(tradeno,totalfee,orderCode,uid,pid,backurl));
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
		// if(obj.getDetail()!=null && !obj.getDetail().equals(""))
		// sb.append("<detail>"+obj.getDetail()+"</detail>");
		// if (obj.getDevice_info() != null && !obj.getDevice_info().equals(""))
		// sb.append("<device_info>" + obj.getDevice_info() + "</device_info>");
		// else
		// sb.append("<device_info></device_info>");
		// if (obj.getFee_type() != null && !obj.getFee_type().equals(""))
		// sb.append("<fee_type>" + obj.getFee_type() + "</fee_type>");
		// if (obj.getGoods_tag() != null && !obj.getGoods_tag().equals(""))
		// sb.append("<goods_tag>" + obj.getGoods_tag() + "</goods_tag>");
		// else
		// sb.append("<goods_tag></goods_tag>");
		// if (obj.getMch_id() != null && !obj.getMch_id().equals(""))

		if (!StringUtils.isBlank(obj.getGoods_tag()))
			sb.append("<goods_tag>" + obj.getGoods_tag() + "</goods_tag>");
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
		// if (obj.getTime_expire() != null && obj.getTime_expire().equals(""))
		// sb.append("<time_expire>" + obj.getTime_expire() + "</time_expire>");
		// else
		// sb.append("<time_expire></time_expire>");
		//
		// if (obj.getTime_start() != null && !obj.getTime_start().equals(""))
		// sb.append("<time_start>" + obj.getTime_start() + "</time_start>");
		// else
		// sb.append("<time_start></time_start>");

		if (obj.getTotal_fee().intValue() > 0)
			sb.append("<total_fee>" + obj.getTotal_fee().intValue()
					+ "</total_fee>");
		if (obj.getTrade_type() != null && !obj.getTrade_type().equals(""))
			sb.append("<trade_type>" + obj.getTrade_type() + "</trade_type>");
		sb.append("</xml>");

		String sbt = sb.toString();
		logger.info("send wxpay string:" + sbt);
		// Logger.debug( sbt );

		/*
		 * try { return new String(sbt.toString().getBytes(), "ISO8859_1"); }
		 * catch (Exception e) { // TODO Auto-generated catch block
		 * Logger.info(" wx "+e.toString()); }
		 */
		return sbt;
	}

	// 微信用户注册成HIGOU用户接口
	public Result reguser() {
		response().setContentType("application/json;charset=utf-8");
		ObjectNode result = Json.newObject();
		String fromuid = AjaxHellper.getHttpParamOfFormUrlEncoded(request(),
				"uid");
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
		String dyid = AjaxHellper.getHttpParamOfFormUrlEncoded(request(),
				"daiyanid");
		if (StringUtils.isBlank(dyid))
			dyid = "";

		// try {
		// nickname = new String(nickname.getBytes(),"GBK");
		// } catch (UnsupportedEncodingException e) {
		// e.printStackTrace();
		// }
		if (StringUtils.isBlank(phone) || StringUtils.isBlank(verifys)) {
			result.put("status", "0");
			return ok(Json.toJson(result));
		}

		result.put("status", "1");
		Long uid = 0L;// userService.checkandreguser(nickname, phone, smscode,
						// headicon, unionid);
		// unionid=cache.get(openid+"unionid");
		nickname = cache.get(openid + "nickname");
		headicon = cache.get(openid + "headicon");
		unionid = StringUtils.isBlank(unionid) ? "" : unionid;
		nickname = StringUtils.isBlank(nickname) ? "用户"
				+ (phone.length() > 5 ? phone.substring(5) : phone) : nickname;
		headicon = StringUtils.isBlank(headicon) ? "" : headicon;
		String mcode = AjaxHellper.getHttpParam(request(), "mcode");
		if (StringUtils.isBlank(mcode))
			mcode = "daiyan";

		uid = userService.checkandreguser(nickname, phone, verifys, headicon,
				unionid, openid, "daiyan", fromuid);
		if (uid.longValue() == 0) {
			result.put("msg", "注册失败");
			result.put("status", "0");
		} else {
			// 添加统计
			CountH5 cnt = new CountH5();
			String channel = "daiyanshanghuloginsuccess";

			cnt.setChannel(channel);
			cnt.setIp(request().remoteAddress());
			cnt.setIswx("1");
			cnt.setShareType("");
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

			String urls = "http://" + request().host() + request().path() + qry;

			cnt.setUrl(urls);
			cnt.setCreateTime(new Date());
			cnt.setUserId(uid);
			cnt.setDaiyanid(Numbers.parseLong(dyid, 0L));
			endorseService.saveCount(cnt);

			session().put("hgUid", uid.toString());
			List<Address> alist = addressService.address_default(uid);
			Address ad = new Address();
			ad.setAddress("");
			ad.setAddressId(Long.valueOf("0"));
			if (alist != null && !alist.isEmpty()) {
				ad = alist.get(0);
			} else {
				User user = userService.getUserByUid(uid);
				ad.setName(user.getNickname());
				ad.setPhone(user.getPhone());
				ad.setProvince("");
			}
			result.put("userid", uid);
			result.put("aid", ad.getAddressId());
			result.put("name", ad.getName());
			result.put("phone", ad.getPhone());
			result.put("city", ad.getProvince());
			result.put("address", ad.getAddress());
		}
		session("uid", uid.toString());
		result.put("uid", uid.toString());
		return ok(Json.toJson(result));
	}

	public Result editaddress() {
		Long uid = Numbers.parseLong(
				AjaxHellper.getHttpParam(request(), "uid"), 0L);
		Long pid = Numbers.parseLong(
				AjaxHellper.getHttpParam(request(), "pid"), Long.valueOf(0));
		Long dyid = Numbers.parseLong(
				AjaxHellper.getHttpParam(request(), "daiyanid"),
				Long.valueOf(0));
		Long aid = Numbers.parseLong(
				AjaxHellper.getHttpParam(request(), "aid"), Long.valueOf(0));
		if (pid == 0L || uid == 0L) {
			return ok(views.html.sheSaid.pageError.render());
		}

		Address address = null;
		User user = userService.getUserByUid(uid);

		Endorsement endorse = endorseService.getEndorseById(dyid);
		if (endorse == null || user == null)
			return ok(views.html.sheSaid.pageError.render());

		if (aid > 0L) {
			address = addressService.findByAddressId(aid);
		}
		if (endorse.getUserId().longValue() == 423908) {
			address = new Address();
			address.setAddressId(Long.valueOf("0"));
			address.setPhone(user.getPhone());
			address.setCardId("");
			address.setAddress("新开街28-14号 东方美妆");
			address.setProvince("北京市平谷区");
		}
		
		return ok(views.html.sheSaid.person.render(address, uid, pid, dyid));
	}

	public Result saveaddress() {

		Long uid = Numbers.parseLong(Form.form().bindFromRequest().get("uid"),
				0L);
		Long aid = Numbers.parseLong(Form.form().bindFromRequest().get("aid"),
				0L);
		ObjectNode result = Json.newObject();
		result.put("status", "0");
		result.put("aid", "0");
		result.put("msg", "地址保存失败");

		if (!this.checkses()) {
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
		return ok(result);
	}

	// H5申请调研页面
	public static Result applyh() {
		String uid = AjaxHellper.getHttpParam(request(), "uid");
		return ok(views.html.sheSaid.applyh.render(uid));
	}

	// H5调研保存
	public Result saveapply() {
		response().setContentType("application/json;charset=utf-8");
		ObjectNode result = Json.newObject();

		Map<String, String> ap = new HashMap<String, String>();
		ap.put("uname", Form.form().bindFromRequest().get("uname"));
		ap.put("sex", Form.form().bindFromRequest().get("sex"));
		ap.put("yearold", Form.form().bindFromRequest().get("yearold"));
		ap.put("oper", Form.form().bindFromRequest().get("oper"));
		ap.put("wx", Form.form().bindFromRequest().get("wx"));
		ap.put("phone", Form.form().bindFromRequest().get("phone"));
		ap.put("income", Form.form().bindFromRequest().get("income"));
		ap.put("remark", Form.form().bindFromRequest().get("remark"));
		Long uid = Numbers.parseLong(Form.form().bindFromRequest().get("uid"),
				0L);

		if (ap.size() > 0) {
			Question quest = new Question();
			quest.setAddtime(new Date());
			quest.setUid(uid);
			// 检查提交过
			List<Question> qlist = endorseService.getQuestionbyUid(uid);
			if (qlist == null || qlist.isEmpty()) {
				quest = endorseService.saveQuestion(quest);
				if (quest.getId() > 0L) {
					Iterator it = ap.keySet().iterator();
					while (it.hasNext()) {
						String keyv;
						String valuev;
						keyv = it.next().toString();
						valuev = ap.get(keyv);
						Qanswer an = new Qanswer();
						an.setPid(quest.getId());
						an.setQkey(keyv);
						an.setQValues(valuev);
						endorseService.saveAnswer(an);
					}
					result.put("status", "1");
				}
			} else {
				result.put("status", "0");
			}
		}

		return ok(Json.toJson(result));
		// return ok(views.html.sheSaid.applysuccess.render());
	}

	// H5保存成功页面
	public static Result applysuccess() {
		return ok(views.html.sheSaid.applysuccess.render());
	}

	public static Result h5app() {
		return ok(views.html.sheSaid.h5app.render());
	}

	public static Result pageerr() {
		return ok(views.html.sheSaid.pageError.render());
	}

	// 访问统计添加页面
	public Result countjs() {
		response().setContentType("application/json;charset=utf-8");
		ObjectNode result = Json.newObject();
		String ip = AjaxHellper.getHttpParamOfFormUrlEncoded(request(), "ips");
		String url = AjaxHellper
				.getHttpParamOfFormUrlEncoded(request(), "curl");
		String sharetype = AjaxHellper.getHttpParamOfFormUrlEncoded(request(),
				"sharetype");
		String iswx = AjaxHellper.getHttpParamOfFormUrlEncoded(request(),
				"iswx");
		String channel = AjaxHellper.getHttpParamOfFormUrlEncoded(request(),
				"channel");
		String uid = AjaxHellper.getHttpParamOfFormUrlEncoded(request(), "uid");
		Long daiyanid = Numbers
				.parseLong(AjaxHellper.getHttpParamOfFormUrlEncoded(request(),
						"daiyanid"), 0L);
		String unionid=AjaxHellper.getHttpParamOfFormUrlEncoded(request(), "unionid");
		if(unionid==null || StringUtils.isBlank(unionid))
			unionid="";

		if (!StringUtils.isBlank(url) && !StringUtils.isBlank(channel)) {
			CountH5 cnt = new CountH5();
			cnt.setChannel(channel);
			cnt.setIp(StringUtils.isBlank(ip) ? request().remoteAddress() : ip);
			cnt.setIswx(iswx);
			cnt.setShareType(sharetype);
			cnt.setUrl(url);
			cnt.setDaiyanid(daiyanid);
			cnt.setCreateTime(new Date());
			cnt.setUserId(Numbers.parseLong(uid, 0L));
			cnt.setUnionid(unionid);
			endorseService.saveCount(cnt);
		}
		result.put("status", "1");
		return ok(Json.toJson(result));
	}

	public static Result test() {
		String data = "<xml>"
				+ "<appid><![CDATA[wx2421b1c4370ec43b]]></appid>"
				+ "<attach><![CDATA[支付测试]]></attach>"
				+ "<bank_type><![CDATA[CFT]]></bank_type>"
				+ "<fee_type><![CDATA[CNY]]></fee_type>"
				+ "<is_subscribe><![CDATA[Y]]></is_subscribe>"
				+ "<mch_id><![CDATA[10000100]]></mch_id>"
				+ "<nonce_str><![CDATA[5d2b6c2a8db53831f7eda20af46e531c]]></nonce_str>"
				+ "<openid><![CDATA[oUpF8uMEb4qRXf22hE3X68TekukE]]></openid>"
				+ "<out_trade_no><![CDATA[1409811653]]></out_trade_no>"
				+ "<result_code><![CDATA[SUCCESS]]></result_code>"
				+ "<return_code><![CDATA[SUCCESS]]></return_code>"
				+ "<sign><![CDATA[B552ED6B279343CB493C5DD0D78AB241]]></sign>"
				+ "<sub_mch_id><![CDATA[10000100]]></sub_mch_id>"
				+ "<time_end><![CDATA[20140903131540]]></time_end>"
				+ "<total_fee>1</total_fee>"
				+ "<trade_type><![CDATA[JSAPI]]></trade_type>"
				+ "<transaction_id><![CDATA[1004400740201409030005092168]]></transaction_id></xml>";

		// 发送请求，返回json
		try {

			String wxurl = "http://localhost:9000/api/wxpayreturnjsapi";
			// WSUtils.postByXML(wxurl, data);

			// HttpPost post = new HttpPost(wxurl);
			org.apache.commons.httpclient.HttpClient client = new org.apache.commons.httpclient.HttpClient();
			PostMethod myPost = new PostMethod(wxurl);
			client.getParams().setSoTimeout(300 * 1000);

			myPost.setRequestEntity(new StringRequestEntity(data, "text/xml",
					"utf-8"));
			int statusCode = client.executeMethod(myPost);
			if (statusCode == HttpStatus.SC_OK) {
				BufferedInputStream bis = new BufferedInputStream(
						myPost.getResponseBodyAsStream());
				byte[] bytes = new byte[1024];
				ByteArrayOutputStream bos = new ByteArrayOutputStream();
				int count = 0;
				while ((count = bis.read(bytes)) != -1) {
					bos.write(bytes, 0, count);
				}
				byte[] strByte = bos.toByteArray();
				String responseString = new String(strByte, 0, strByte.length,
						"utf-8");
				bos.close();
				bis.close();
			}

		} catch (Exception e) {
		}
		return ok("ok");
	}

	// 代言排名榜
	public Result systemnote() {
		WxSign wxs = this.getwxstr();
		List<EndorsementSort> eflist = userService.getEendorsementSort();
		return ok(views.html.sheSaid.Systemnote.render(eflist, wxs));
	}

	/*
	 * 添加购物车
	 */
	public Result addCart() {
		String pid = AjaxHellper.getHttpParamOfFormUrlEncoded(request(), "pid");
		String daiyanid = AjaxHellper.getHttpParamOfFormUrlEncoded(request(),
				"eid");

		Integer cntInt = Numbers.parseInt(
				AjaxHellper.getHttpParamOfFormUrlEncoded(request(), "cnt"), 1);
		JsonNode result = this
				.addCartvoke(session("op"), daiyanid, pid, cntInt);
		return ok(result);
	}

	/*
	 * 购物车处理
	 */
	public JsonNode addCartvoke(String openid, String daiyanid, String pid,
			Integer cntInt) {
		ObjectNode result = Json.newObject();
		result.put("status", "0");

		Endorsement endorse = endorseService.getEndorseById(Numbers.parseLong(
				daiyanid, 0L));
		Product product = productService.getProductById(Numbers.parseLong(pid,
				0L));
		if (endorse == null
				|| product == null
				|| endorse.getProductId().longValue() != product.getPid()
						.longValue()) {
			result.put("status", "5");
			result.put("msg", "该商品不存在");
			return result;
		}
		if (endorse.getGid() != null
				&& (endorse.getGid().longValue() == 4 || endorse.getGid()
						.longValue() == 6)) {
			product.setRmbprice(new BigDecimal(product.getChinaprice())
					.setScale(2, BigDecimal.ROUND_CEILING).doubleValue());
			product.setEndorsementPrice(endorse.getEndorsementPrice());
		} else
			product.setEndorsementPrice(new BigDecimal(product
					.getEndorsementPrice()).setScale(2,
					BigDecimal.ROUND_CEILING).doubleValue());

		if (product.getEndorsementPrice() == null)
			product.setEndorsementPrice(Double.valueOf(0));

		int limit = product.getLimitcount();
		Long nstock = product.getNstock();
		if (product.getIshot() == 1) {
			nstock = productService.dealNstockWithProduct(product.getPid());
		}

		String opend = StringUtils.isBlank(session("op")) ? "" : session("op");

		Integer totalcount = 0;
		if (!StringUtils.isBlank(opend))
			totalcount = shoppingCartService
					.getCartEndorseCount(session("op"),
							Numbers.parseLong(pid, 0L),
							Numbers.parseLong(daiyanid, 0L));
		if (totalcount == null) {
			totalcount = 0;
		}
		Integer counts = shoppingCartService
				.getCartEndorseAllCount(session("op"));
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
			List<ShoppingCartEndorse> sclist = shoppingCartService
					.getEnCartlist(session("op"), Numbers.parseLong(pid, 0L),
							Numbers.parseLong(daiyanid, 0L));
			ShoppingCartEndorse shoppingCart = sclist.get(0);

			shoppingCart.setCounts(shoppingCart.getCounts() + cntInt);
			shoppingCart = shoppingCartService
					.saveShopCartEndorse(shoppingCart);
			logger.info(shoppingCart.getCounts() + "..............");
		} else {
			// 判断如果同一商品不同商户则先删除再添加
			Integer ddcount = shoppingCartService.getCartEndorseCount(
					session("op"), Numbers.parseLong(pid, 0L), 0L);
			if (ddcount != null && ddcount > 0) {
				shoppingCartService
						.delShopCartEndorse(session("op"), pid, null);
			}
			ShoppingCartEndorse shoppingCart = new ShoppingCartEndorse();
			shoppingCart.setCounts(cntInt);
			shoppingCart.setOpenid(session("op"));
			shoppingCart.setpId(Numbers.parseLong(pid, 0L));
			shoppingCart.setEid(Numbers.parseLong(daiyanid, 0L));

			shoppingCart.setDate_add(new Date());
			shoppingCartService.saveShopCartEndorse(shoppingCart);
		}

		List<ShoppingCartEndorse> shoppingCartList = shoppingCartService
				.getEnCartlist(session("op"), 0L, 0L);

		if (shoppingCartList == null || shoppingCartList.isEmpty()) {
			result.put("status", "7");
			result.put("msg", "购物车为空");
			return result;
		}

		Double totalfee = 0D;

		List<Long> pidlist = new ArrayList<Long>();
		for (ShoppingCartEndorse vo : shoppingCartList) {
			pidlist.add(vo.getpId());
			totalfee = totalfee + vo.getProinfo().getEndorsementPrice()
					* vo.getCounts();
		}
		// 累计税费，邮费
		if (pidlist != null && !pidlist.isEmpty()) {
			Double costfee = productService
					.getEndorseRateFee(
							pidlist.toString().substring(1,
									pidlist.toString().indexOf("]") + 1),
							session("op"));
			Double postfee = productService.getEndorseWeightFee(pidlist,
					session("op"), totalcount);
			totalfee = totalfee + costfee + postfee;
		}
		totalfee = new BigDecimal(totalfee).setScale(2,
				BigDecimal.ROUND_HALF_DOWN).doubleValue();

		result.put("status", "1");
		result.put("tfee", (totalfee.intValue() < totalfee ? totalfee
				: totalfee.intValue()) + "");
		result.put("totalcount", counts + "");
		result.put("msg", "");
		return result;
	}

	/*
	 * 修改购物车
	 */
	public Result editCart() {
		String uid = session("op");// AjaxHellper.getHttpParamOfFormUrlEncoded(request(),
									// "uid");
		String datastr = AjaxHellper.getHttpParamOfFormUrlEncoded(request(),
				"datastr");

		ObjectNode result = Json.newObject();
		result.put("status", "1");
		result.put("msg", "");
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
			if (dataNeedArray.length == 3) {
				String eid = dataNeedArray[2];
				List<ShoppingCartEndorse> sclist = shoppingCartService
						.getEnCartlist(session("op"),
								Numbers.parseLong(pid, 0L),
								Numbers.parseLong(eid, 0L));
				ShoppingCartEndorse shoppingCart = sclist == null
						|| sclist.isEmpty() ? null : sclist.get(0);
				if (shoppingCart != null) {
					shoppingCart.setCounts(Numbers.parseInt(cnt, 0));
					shoppingCartService.saveShopCartEndorse(shoppingCart);
				}
			}
		}
		List<ShoppingCartEndorse> shoppingCartList = shoppingCartService
				.getEnCartlist(session("op"), 0L, 0L);

		if (shoppingCartList == null || shoppingCartList.isEmpty()) {
			result.put("status", "1");
			return ok(result);
		}

		Double totalfee = 0D;
		List<ShoppingCartEndorse> tclist = new ArrayList<ShoppingCartEndorse>();

		List<Long> pidlist = new ArrayList<Long>();
		for (ShoppingCartEndorse sc : shoppingCartList) {
			if (sc.getEndorse().getGid() != null
					&& (sc.getEndorse().getGid().longValue() == 4 || sc
							.getEndorse().getGid().longValue() == 6))
				sc.getProinfo().setEndorsementPrice(
						sc.getEndorse().getEndorsementPrice());
			totalfee = totalfee + sc.getProinfo().getEndorsementPrice()
					* sc.getCounts();
			tclist.add(sc);
			pidlist.add(sc.getpId());
		}

		totalfee = new BigDecimal(totalfee).setScale(2,
				BigDecimal.ROUND_HALF_DOWN).doubleValue();
		shoppingCartList = tclist;
		String tfee = totalfee.doubleValue() + "";
		if (!StringUtils.isBlank(tfee) && tfee.indexOf(".") >= 0) {
			if (Numbers.parseInt(tfee.substring(tfee.indexOf(".") + 1), 0) == 0)
				tfee = tfee.substring(0, tfee.indexOf("."));
		}

		result.put("tfee", tfee);
		return ok(Json.toJson(result));
	}

	// 删除购物车中的商品
	public Result Cartdel() {
		String uid = session("op");
		String pids = AjaxHellper.getHttpParamOfFormUrlEncoded(request(),
				"pids");
		String eids = AjaxHellper.getHttpParamOfFormUrlEncoded(request(),
				"eids");

		ObjectNode result = Json.newObject();

		if (StringUtils.isBlank(uid) || StringUtils.isBlank(pids)) {
			result.put("status", "0");
			result.put("msg", "用户不存在");
			return ok(Json.toJson(result));
		}

		shoppingCartService.delShopCartEndorse(session("op"), pids, eids);
		result.put("status", "1");
		return ok(Json.toJson(result));
	}

	// 购物车列表
	public Result ordercart() {
		
		// 如果没有鉴权则鉴权
		String w = AjaxHellper.getHttpParam(request(), "w");
		w = w == null || StringUtils.isBlank("w") ? "" : w;
		// if (!StringUtils.isBlank(w) && w.equals("1")) {
		if (StringUtils.isBlank(session("op")) && w.equals("1")) {
			// 微信鉴权
			String wxurl = "/sheSaid/wxauthcart?flg=orderlist";
			return redirect(wxurl);
		}
		// }
		//session("uid","87744");
		//session("op","oJdLot5g9n-CQZB0I8EWbOMYywIg");
		Long uid = Numbers.parseLong(session("uid"), 0L);
		WxSign addrSign = null;
		if (!StringUtils.isBlank(session("op")))
			addrSign = H5ShoppingController.getcacheWxsign();

		// if(StringUtils.isBlank(cache.get("wx_access_token_voke"+session("uid"))))
		// addrSign=null;

		if (addrSign != null) {
			String addressstr = StringUtil.getWXaddressSign(
					addrSign.getAccess_token(),
					StringUtil.getDomainH5() + "/sheSaid/ordercartcrm?code="
							+ addrSign.getCode() + "&state="
							+ addrSign.getState(), addrSign.getTimstr(),
					addrSign.getNostr());
			addrSign.setSign(addressstr);
		}
		String dyid = AjaxHellper.getHttpParam(request(), "eid");
		dyid = dyid == null || StringUtils.isBlank(dyid) ? "" : dyid;

		User user = userService.getUserByUid(uid);
		if (user == null) {
			// return ok(views.html.H5.pageError.render());
			user = new User();
			user.setUid(0L);
			user.setOpenId("");
			user.setUnionid("");
		}
		List<ShoppingCartEndorse> shoppingCartList = shoppingCartService
				.getEnCartlist(session("op"), 0L, 0L);
		// if(shoppingCartList==null || shoppingCartList.isEmpty())
		// return ok(views.html.H5.pageError.render());
		Double totalfee = 0D;
		List<ShoppingCartEndorse> tclist = null;

		String display = "none";
		if (shoppingCartList != null && !shoppingCartList.isEmpty()) {
			tclist = new ArrayList<ShoppingCartEndorse>();
			for (ShoppingCartEndorse sc : shoppingCartList) {
				if (sc.getEndorse().getGid() != null
						&& (sc.getEndorse().getGid().longValue() == 4 || sc
								.getEndorse().getGid() == 6))
					sc.getProinfo().setEndorsementPrice(
							sc.getEndorse().getEndorsementPrice());
				if (sc.getEndorse().getGid() != null
						&& sc.getEndorse().getGid().longValue() == 4)
					display = "";

				totalfee = totalfee + sc.getProinfo().getEndorsementPrice()
						* sc.getCounts();
				tclist.add(sc);
			}
		}

		totalfee = new BigDecimal(totalfee).setScale(2,
				BigDecimal.ROUND_HALF_DOWN).doubleValue();
		shoppingCartList = tclist;
		if (shoppingCartList == null || shoppingCartList.isEmpty())
			shoppingCartList = null;

		List<ShoppingCartCategoryVO> fromwaylist = new ArrayList<ShoppingCartCategoryVO>();
		Map<String, ShoppingCartCategoryVO> fwlist = new HashMap<String, ShoppingCartCategoryVO>();
		if (shoppingCartList != null) {
			for (ShoppingCartEndorse c : shoppingCartList) {
				ShoppingCartCategoryVO co = new ShoppingCartCategoryVO();
				co.setFromsite(c.getFromsite().getName());
				co.setFromsiteimg(c.getFromsite().getImg());
				co.setTyp(c.getProinfo().getTyp());
				co.setWayremark(c.getProinfo().getWayremark());
				fwlist.put(co.getFromsite(), co);
			}
		}

		if (fwlist != null && fwlist.size() > 0) {
			Iterator keyIt = fwlist.keySet().iterator();
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
		String eid = shoppingCartList == null ? "0" : shoppingCartList.get(0)
				.getEid().toString();

		Endorsement endorse = endorseService.getEndorseById(Numbers.parseLong(
				dyid, 0L));

		
		Long postuid = Numbers.parseLong(session("postmanuid"), 0L);
		User postman = null;
		if (postuid.longValue() > 0)
			postman = userService.getUserByUid(postuid);

		String msg = AjaxHellper.getHttpParam(request(), "msg");
		msg = StringUtils.isBlank(msg) ? "" : msg;
		String postmark = StringUtil.getSystemConfigValue("post_fee_mark");
		postmark = StringUtils.isBlank(postmark) ? "" : postmark;
		String postmarkurl = StringUtil
				.getSystemConfigValue("post_fee_mark_url");
		postmarkurl = StringUtils.isBlank(postmarkurl) ? "" : postmarkurl;
		String tfee = totalfee.doubleValue() + "";
		if (!StringUtils.isBlank(tfee) && tfee.indexOf(".") >= 0) {
			if (Numbers.parseInt(tfee.substring(tfee.indexOf(".") + 1), 0) == 0)
				tfee = tfee.substring(0, tfee.indexOf("."));
		}
		String postmanuid = StringUtils.isBlank(session("postmanuid")) ? "": session("postmanuid");

		String preurl = CdnAssets.H5_SHOPPING_URL(postmanuid);
		if (dyid != null && !StringUtils.isBlank(dyid) && !dyid.equals("0"))
			preurl = "/sheSaid/show?pid=" + endorse.getProductId()
					+ "&daiyanid=" + dyid;
		
		WxSign sign=this.getwxstr();

//		if (postman == null || StringUtils.isBlank(postman.getPostmanid())) {
//			sign.setShareimg(endorse.getProducinfo().getListpic());
//			sign.setSharetitle("我刚才看到了一个海淘商品，又要败家了，一起来吧！");
//			sign.setSharecontent(endorse.getProducinfo().getSubtitle());
//			sign.setShareurl(StringUtil.getDomainH5()
//					+ "/sheSaid/showshare?uid=" + user.getUid() + "&pid="
//					+ endorse.getProductId() + "&daiyanid=" + dyid);
//		} else {
//			sign.setShareimg(StringUtil.getDomainH5()
//					+ "/public/images/sheSaidImages/hi.gif");
//			sign.setSharetitle("我是快递员" + postman.getNickname()
//					+ ",我能帮你带来最优惠的价格");
//			sign.setSharecontent("棒棒糖商城");
//			sign.setShareurl(StringUtil.getDomainH5()
//					+ "/H5/prolist?uid=" + postman.getUid());
//		}
		return ok(views.html.sheSaid.ordercart.render(shoppingCartList, user,sign,
				tfee,msg, postmark, postmarkurl, fromwaylist,
				addrSign, postmanuid, preurl, display));
	}

	// 订单预览页面
	public Result ordercartcrm() {
		Long uid = Numbers.parseLong(session("uid"), 0L);
		WxSign addrSign = null;
		if (!StringUtils.isBlank(session("op")))
			addrSign = H5ShoppingController.getcacheWxsign();

		if (addrSign != null) {
			String ordercarturl = StringUtil.getDomainH5()
					+ "/sheSaid/ordercartcrm?code=" + addrSign.getCode()
					+ "&state=" + addrSign.getState();
			String addressstr = StringUtil.getWXaddressSign(
					addrSign.getAccess_token(), ordercarturl,
					addrSign.getTimstr(), addrSign.getNostr());
			addrSign.setSign(addressstr);
		}
		String openid = session("op");
		if (StringUtils.isBlank(openid))
		openid = "";
		String unionid = session("un");
		unionid = StringUtils.isBlank(unionid) ? "" : unionid;
		User user = userService.getUserByopenid(openid,unionid);
		if (user == null) {
			user = new User();
			user.setUid(0L);
			user.setOpenId(openid);
			user.setUnionid(unionid);
		}

		List<ShoppingCartEndorse> shoppingCartList = shoppingCartService
				.getEnCartlist(session("op"), 0L, 0L);

		Double totalfee = 0D;
		String pid = "0";
		String datastr = "";
		String isopen = "0";// 1需要填写身份证号
		Double costfee = 0D;// 税费
		String postfee = "0";// 运费
		String pidlist = "";
		List<Long> pidList = new ArrayList<Long>();
		List<ShoppingCartEndorse> tclist = new ArrayList<ShoppingCartEndorse>();
		if (shoppingCartList != null && !shoppingCartList.isEmpty()) {
			for (ShoppingCartEndorse sc : shoppingCartList) {
				pidlist = pidlist + "," + sc.getProinfo().getPid();
				pidList.add(sc.getProinfo().getPid());
				if (sc.getProinfo() != null
						&& sc.getProinfo().getIsopenid() == 1)
					isopen = "1";
				if (sc.getEndorse().getGid() != null
						&& (sc.getEndorse().getGid().longValue() == 4 || sc
								.getEndorse().getGid().longValue() == 6))
					sc.getProinfo().setEndorsementPrice(
							sc.getEndorse().getEndorsementPrice());
				datastr = datastr + "," + sc.getProinfo().getPid() + "_"
						+ sc.getProinfo().getEndorsementPrice() + "_"
						+ sc.getCounts() + "_" + sc.getEid();
				totalfee = totalfee + sc.getProinfo().getEndorsementPrice()
						* sc.getCounts();
				tclist.add(sc);
			}
		}

		shoppingCartList = tclist;
		if (shoppingCartList == null || shoppingCartList.isEmpty())
			shoppingCartList = null;

		if (!StringUtils.isBlank(pidlist)) {
			pidlist = pidlist.substring(1);
			postfee = new BigDecimal(productService.getWeightFee(pidList, uid)
					.doubleValue()).setScale(2, BigDecimal.ROUND_HALF_DOWN)
					.doubleValue()
					+ "";
			costfee = productService.getEndorseRateFee(pidlist, session("op"));
		}

		if (!StringUtils.isBlank(datastr))
			datastr = datastr.substring(1);

		totalfee = new BigDecimal(totalfee).setScale(2,
				BigDecimal.ROUND_HALF_DOWN).doubleValue();
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

		Long postuid = Numbers.parseLong(session("postmanuid"), 0L);
		User postman = null;
		if (postuid.longValue() > 0)
			postman = userService.getUserByUid(postuid);
		
		String taxfee = new BigDecimal(costfee).setScale(2,
				BigDecimal.ROUND_HALF_DOWN).doubleValue()
				+ "";
		tfee = String.valueOf(Numbers.parseDouble(tfee, 0L)
				+ Numbers.parseDouble(postfee, 0L)
				+ Numbers.parseDouble(taxfee, 0L));
		if (!StringUtils.isBlank(taxfee) && taxfee.indexOf(".") >= 0) {
			if (Numbers.parseInt(taxfee.substring(taxfee.indexOf(".") + 1), 0) == 0)
				taxfee = taxfee.substring(0, taxfee.indexOf("."));
		}
		if (!StringUtils.isBlank(tfee) && tfee.indexOf(".") >= 0) {
			if (Numbers.parseInt(tfee.substring(tfee.indexOf(".") + 1), 0) == 0)
				tfee = tfee.substring(0, tfee.indexOf("."));
		}
		if (!StringUtils.isBlank(postfee) && postfee.indexOf(".") >= 0) {
			if (Numbers
					.parseInt(postfee.substring(postfee.indexOf(".") + 1), 0) == 0)
				postfee = postfee.substring(0, postfee.indexOf("."));
		}
		String wxtoken = AjaxHellper.getHttpParam(request(), "code");
		if (StringUtils.isBlank(wxtoken))
			wxtoken = "";
		String postmanuid = StringUtils.isBlank(session("postmanuid")) ? CdnAssets.HIGOUSHOPID
				: session("postmanuid");
		return ok(views.html.sheSaid.ordercartcrm.render(addressobj,
				shoppingCartList, user, isopen, postfee, taxfee, tfee,
				datastr, pidlist, addrSign, Constants.WXappID, postmanuid,
				wxtoken));
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
				}
			} catch (Exception e) {
			}
		}
		if (!StringUtils.isBlank(access_token) && StringUtils.isBlank(ticket)) {
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
						cache.setWithOutTime("getwxSign_ticket", ticket, 7150);
					}
				}
			} catch (Exception ee) {
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
		String dport = IsProduct == true ? "" : "";// ":9004";
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
		wxsign.setSharetitle("给好友福利，还能免费游日本");
		wxsign.setSharecontent("我跟老板特别好，好友福利少不了");
		wxsign.setShareurl(StringUtil.getDomainH5() + "/sheSaid/en_notes");
		wxsign.setShareimg(StringUtil.getDomainH5()
				+ "/public/images/sheSaidImages/bordnotepre.jpg");
		return wxsign;
	}
}
