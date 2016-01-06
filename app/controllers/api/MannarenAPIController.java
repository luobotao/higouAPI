package controllers.api;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import models.AutoSyncTask;
import models.AutoSyncTaskLog;
import models.Currency;
import models.Parcels;
import models.ParcelsPro;
import models.Product;
import models.ProductGroup;
import models.ShoppingOrder;
import models.User;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpResponse;
import org.apache.http.ParseException;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

import play.Configuration;
import play.Logger;
import play.Logger.ALogger;
import play.Play;
import play.libs.Json;
import play.mvc.Result;
import services.ServiceFactory;
import services.api.AutoSyncTaskLogService;
import services.api.AutoSyncTaskService;
import services.api.ErpAddressService;
import services.api.ParcelsProService;
import services.api.ParcelsService;
import services.api.ParcelsWaybillService;
import services.api.ProductService;
import services.api.ShoppingOrderService;
import services.api.UserService;
import utils.AjaxHellper;
import utils.Constants;
import utils.Dates;
import utils.EmailUtil;
import utils.JdbcOper;
import utils.StringUtil;
import utils.WSUtils;
import utils.kuaidi100.Kuaidi100;
import vo.StatusToastVO;
import vo.trafCustom.OrderInfoVO;
import vo.trafCustom.OrderInfoVO.GOODS_INFO;
import vo.trafCustom.OrderInfoVO.ORDER_INFO;
import vo.trafCustom.OrderInfoVO.WAYBILL_INFO;
import vo.trafCustom.PaymentInfoVO;
import vo.trafCustom.PaymentInfoVO.PAYMENT_INFO;
import vo.trafCustom.WaybillInfoVO;

import com.fasterxml.jackson.databind.JsonNode;
/**
 * 
 * @author luobotao
 *
 */
@Named
@Singleton
public class MannarenAPIController extends BaseApiController {
	
	private static String encryptText_Order="556e5b17bfc8640e7ccb9df3";
	private static String encryptKey_Order="cf70b5566ddf49a3a4d788a10a265c40";
	private static String accessToken__Order="gAAAADBcaMdb1ok74kFgyUoe5THxYFQ4qF8Iw8ZfXoGiV2ImY-h2IDFv6qtDbMiEJq5O1E_G8xup81heA_7d2tNysmfEm62nuSUkQ5qzayTmlrCySFBgjgXXZi7tORD0ZTaffZ4wNMhslAm8OkfEpmTwtU12Inaj_G1p7gcrdXMTiCKXVAEAAIAAAABJtLqELp256DkyG64I0QI3FxtJDutWStKMzrTBHgmcpj6kE3Blw6yRndOcL44KBoeU9duf0a_uEPui8S7iufkxCVoynQT_33dKS5UQC_g0CtLJ1sbFhheRSQ-1YqnFnqz139QOCiK9X2DDyAa5d9DEu6K-wF7eEMIzNcUPRgt7eMgs5ftDkE8dsmpR5R0mTSdx_4lHTpDRObUNcX-Ud0PnY7ApgCV6Mn4pxb_6vqebQVz31QtZSgrsaHu8ofqviZ7czBYKMua-TTuINUYeEmPbZaC6N6XI0WUjdM_l49gQfxnPMb4BKgtb1ejsxbTW4x8iWv0cDkBRzq-lZs9brR0Xrqq-7GrkCYlOzGLftK5z5aPHyzMmnAXp4o501nA7Inhd9Ufbzlx6KvT9z3M2KEWx8znGMP8I5E8WJ4b-6cG6eT4RnAOfiAfSO-i9L-v1ZVQ";
	private static String Authorization_Order="Bearer "+accessToken__Order;
	
	private static String encryptText_Pay="55822ae7bfc8641208c23c2b";
	private static String encryptKey_Pay="a2cfcb899ae24dd997bcd472779705e9";
	private static String accessToken__Pay="gAAAAGLqrQXAQCskQ2N7-dNpXQgHukHJoZOBn-pSCsBNptR_lqMD7PNH2sjmLatpV2l-m1iS7nADVtDGkte0_QmaTNwR-CaAKgSxOYoSjpXAumvm4mVEVMgea8xdcarn42EaYc-NrvpJmxwfymqUD27At66jScRC_VNlWk4DYJpOaPVtVAEAAIAAAACbrkkNyXMFrf8yI5lSjxgb16eYzWS-FmOYsGUgdsTM9kqFmQKDLqnYZkCmI3kjXXIIPDqs0SGIPUvy3onbT8ybgJuXXA8gs6ls2ZeaIvGE4AO01tuiS3sEe7qX39iw5zioRLxxGXWN9gEOg-qpOPN80cM_65JzXPSGIsYpegbI1wJ0zFy9m38NeZlwoc2RvTa4qKehgOpFEqlCWDjSSmj7C3I1y5oNMBPoflc-OqOza_PAZ6uLbBv_DPiFt9YXU94sh0N65ZdR41eBFgPjnQ4jHayO278kpWmFDw3MbyZdSovEfXUUq5f0rBpFVQLg9YOLzsKTxq9Ko3T5OxNFOrDneAvInnuALJNmHPAhrKBjaSiuCjC7DePXvyjA3VYLJId7Y9HMl9b7Ho3KFmqyuGkCD0Ve2PB2y9Lkhw4o0MJ2rdRaKi8SgtL43JF6Bhu2Gfk";
	private static String Authorization_Pay="Bearer "+accessToken__Pay;
	
	private static String encryptText_WayBill="55823197bfc8641208c23c2e";
	private static String encryptKey_WayBill="edbba08d170441b6a94674230fecc58d";
	private static String accessToken__WayBill="gAAAALh8GICeakwiL-dP_2q45sL7dJJc6xaLL4MVquzN0yaBGpK50jHF5qq1yCAdpLyDcV3Ctj3v1ClWucB8Rdw2pzGaASsm03j-9nKtXV72z2YX5feLAusSLqjFl2kZj1iH8-oKv8iOaRbI-PcnTFLIX86oLp_Xsz7Pd7mUn5NbjhRZVAEAAIAAAAANHYPeVu1hoIFovfm14DJA1qw6DCK6x3uv3OOqFv-T7uTVXXYoBONG1MSMPx_xhNGy2NLSyoKtUnXa228Fo7wBOkOwqJoKaXcTz_kTThgibaKWGmkr_VE95amFFQansynq1cha61GUQyyU9bypI3W5U19wMd4FyASnmXUflqH-PnkcKTkBVoDnfAANgar9N6wbh4CjzhDQ5jSGL-2fnhMNW-zQxd0SsCxBOYH5aWKkF66dhL3RQWsQF_0IDlEM5kYK3CVizWklLfBM0BZ2R4caWHTcw95KO1AbOQmtusA2_WG0G5eQgEtaqZKXHbU2xHuCBnrHw80JAJZI9BPDVzwqOJQwjP4g4xFNLDJRtHlUTVnTS0XBfol4uuR-ID5PnLnOwuwNv5yQ5UewZm2PCa5tDZ3JNp2Fe6eifsllG2TIZGOg8omRkOG-GQlPhdiI8_o";
	private static String Authorization_WayBill="Bearer "+accessToken__WayBill;
	
	private static final String MAC_NAME = "HmacSHA1";    
	private static final String ENCODING = "UTF-8";
	private static final String COM = "shunfeng";
	private static final ALogger logger = Logger.of(MannarenAPIController.class);
	private final UserService userService;
	private final ProductService productService;
	private final ParcelsService parcelsService;
	private final ParcelsProService parcelsProService;
	private final ShoppingOrderService shoppingOrderService;
	private final ErpAddressService erpAddressService;
	private final AutoSyncTaskService autoSyncTaskService;
	private final AutoSyncTaskLogService autoSyncTaskLogService;
	private final ParcelsWaybillService parcelsWaybillService;
	@Inject
    public MannarenAPIController(final UserService userService, final ProductService productService,
    		final ParcelsService parcelsService,final ShoppingOrderService shoppingOrderService,final ParcelsProService parcelsProService,final ErpAddressService erpAddressService,final AutoSyncTaskService autoSyncTaskService,final ParcelsWaybillService parcelsWaybillService,
    		final AutoSyncTaskLogService autoSyncTaskLogService) {
		 this.userService = userService;        
		 this.productService = productService;
		 this.parcelsService = parcelsService;
		 this.parcelsProService = parcelsProService;
		 this.shoppingOrderService = shoppingOrderService;
		 this.erpAddressService = erpAddressService;
		 this.autoSyncTaskService = autoSyncTaskService;
		 this.parcelsWaybillService = parcelsWaybillService;
		 this.autoSyncTaskLogService = autoSyncTaskLogService;
    }
	
	
	/**
	 * 海关支付
	 * @return
	 */
	public Result sendCustomsPay(){
		response().setContentType("application/json;charset=utf-8");
		StatusToastVO result = new StatusToastVO();
		
		String signature = createSignature(encryptText_Pay,encryptKey_Pay);
		signature = signature.replace("-","").toUpperCase();
		String url = "http://api.mannaren.com/1/api/payment?client_id=" + encryptText_Pay + "&signature=" + signature;
		Map<String, String> requestContent = new HashMap<String, String>();
		List<PAYMENT_INFO> payMentList = new ArrayList<PAYMENT_INFO>();
		PAYMENT_INFO payment_Info = new PAYMENT_INFO();
		payment_Info.DEAL_ID="Pay2015061202";
		payment_Info.DEAL_PLAT_ID="123321123";
		payment_Info.PAYMENT_DATE="2015-06-02 13:01:00";
		payment_Info.PAYMENT_DESC="无";
		payment_Info.PAYMENT_AMOUNT="118.00";
		payment_Info.PAYER_ACCOUNT="6222111212121";
		payment_Info.PAYER_NAME="杨涛";
		payment_Info.PAYER_CERT_TYPE="1";
		payment_Info.PAYER_CERT_ID="211322198410021234";
		payment_Info.PAYEE_ACCOUNT="1032423423423";
		payment_Info.PAYEE_NAME="杨涛";
		payment_Info.PAYEE_CERT_TYPE="1";
		payment_Info.PAYEE_CERT_ID="211322198410021234";
		payment_Info.EB_PLAT_ID="1201";
		payment_Info.ORDER_ID="2586898749";
		payment_Info.NOTE="16";
		payMentList.add(payment_Info);
		
		PaymentInfoVO paymentInfoVO = new PaymentInfoVO();
		paymentInfoVO.PAYMENT_HEAD = payMentList;
		requestContent.put("paymentInfo",Json.toJson(paymentInfoVO)+"");
		requestContent.put("identifier",encryptText_Order);
		JsonNode postResult = WSUtils.postByFormWithAuth(url, requestContent, Authorization_Pay);
		logger.info(url+requestContent+postResult);
		String strResult = postResult.get("result").asText();
		if("true".equals(strResult)){
			result.setStatus("1");
		}else{
			result.setStatus("0");
		}
		result.setToast(strResult);
        return ok(Json.toJson(result));
	}
	
	/**
	 * 向海关发送订单
	 * @return
	 */
	public Result sendCustomsOrder(){
		response().setContentType("application/json;charset=utf-8");
		StatusToastVO result = new StatusToastVO();
		
		String signature = createSignature(encryptText_Order,encryptKey_Order);
		signature = signature.replace("-","").toUpperCase();
		String url = "http://api.mannaren.com/1/api/order?client_id=" + encryptText_Order + "&signature=" + signature;
		Map<String, String> requestContent = new HashMap<String, String>();
		OrderInfoVO orderInfo = new OrderInfoVO();
		List<ORDER_INFO> ORDER_HEAD = new ArrayList<OrderInfoVO.ORDER_INFO>();
		List<GOODS_INFO> GOODS_LIST = new ArrayList<OrderInfoVO.GOODS_INFO>();
		List<WAYBILL_INFO> WAYBILL_LIST = new ArrayList<OrderInfoVO.WAYBILL_INFO>();
		
		ORDER_INFO order_info = new ORDER_INFO(); 
		order_info.ORDER_ID="2586898749";
		order_info.IE_FLAG="I";
		order_info.TRADE_MODE="0";
		order_info.EB_CODE="1101110327";
		order_info.EB_NAME="北京嗨购电子商务有限公司";
		order_info.TOTAL_PAYMENT="43.0";
		order_info.CURR_CODE="142";
		order_info.BUYER_NAME="骆骆";
		order_info.BUYER_CERT_TYPE="1";
		order_info.BUYER_CERT_ID="211322198410021231";
		order_info.BUYER_COUNTRY="110";
		order_info.BUYER_TEL="13998421232";
		order_info.DELIVERY_ADDR="北京朝阳区百子湾路苹果社区2-B26";
		order_info.EB_PLAT_ID="1201";
		order_info.NOTE="无";
		ORDER_HEAD.add(order_info);
		orderInfo.ORDER_HEAD = ORDER_HEAD;
		GOODS_INFO goods_info = new GOODS_INFO();
		goods_info.G_NO="3";
		goods_info.CODE_TS="1234567891";
		goods_info.G_NAME="再也不脱皮了~[Shiseido资生堂] MOILIP药用治疗型唇膏 8g ";
		goods_info.G_DESC="无";
		goods_info.G_MODEL="PL_KJTBHDLY00021";
		goods_info.G_NUM="1";
		goods_info.G_UNIT="011";
		goods_info.PRICE="43.00";
		goods_info.CURR_CODE="142";
		goods_info.NOTE="无";
		goods_info.ORDER_ID="2586898749";
		goods_info.EB_PLAT_ID="1201";
		GOODS_LIST.add(goods_info);
		orderInfo.GOODS_LIST =GOODS_LIST;
		WAYBILL_INFO wayBill_infos = new WAYBILL_INFO();
		wayBill_infos.WAYBILL_ID="RM803720026CN";
		wayBill_infos.LOGI_ENTE_CODE="1101110316";
		wayBill_infos.ORDER_ID="2586898749";
		wayBill_infos.G_DESC="无";
		wayBill_infos.EB_PLAT_ID="1201";
		WAYBILL_LIST.add(wayBill_infos);
		orderInfo.WAYBILL_LIST = WAYBILL_LIST;
		requestContent.put("orderInfo",Json.toJson(orderInfo)+"");
		requestContent.put("identifier",encryptText_Order);
		JsonNode postResult = WSUtils.postByFormWithAuth(url, requestContent, Authorization_Order);
		logger.info(url+requestContent+postResult);
		String strResult = postResult.get("result").asText();
		if("true".equals(strResult)){
			result.setStatus("1");
		}else{
			result.setStatus("0");
		}
		result.setToast(strResult);
        return ok(Json.toJson(result));
//		 return ok(Json.toJson(orderInfo));
	}
	/**
	 * 向海关发送物流
	 * @return
	 */
	public Result sendCustomsWaybill(){
		response().setContentType("application/json;charset=utf-8");
		StatusToastVO result = new StatusToastVO();
		
		String signature = createSignature(encryptText_WayBill,encryptKey_WayBill);
		signature = signature.replace("-","").toUpperCase();
		String url = "http://api.mannaren.com/1/api/waybill?client_id=" + encryptText_WayBill + "&signature=" + signature;
		Map<String, String> requestContent = new HashMap<String, String>();
		WaybillInfoVO waybillInfoVO = new WaybillInfoVO();
		List<WaybillInfoVO.ORDER_INFO> ORDER_HEAD = new ArrayList<WaybillInfoVO.ORDER_INFO>();
		List<WaybillInfoVO.GOODS_INFO> GOODS_LIST = new ArrayList<WaybillInfoVO.GOODS_INFO>();
		List<WaybillInfoVO.WAYBILL_INFO> WAYBILL_HEAD = new ArrayList<WaybillInfoVO.WAYBILL_INFO>();
		WaybillInfoVO.ORDER_INFO order_info = new WaybillInfoVO.ORDER_INFO(); 
		order_info.WAYBILL_ID="RM803720026CN";
		order_info.LOGI_ENTE_CODE="1101110326";
		order_info.ORDER_ID="2586898749";
		order_info.EB_PLAT_ID="1201";
		ORDER_HEAD.add(order_info);
		waybillInfoVO.ORDER_LIST=ORDER_HEAD;
		
		WaybillInfoVO.GOODS_INFO goods_info = new WaybillInfoVO.GOODS_INFO();
		goods_info.G_NO="1";
		goods_info.WAYBILL_ID="RM803720026CN";
		goods_info.LOGI_ENTE_CODE="1101110326";
		goods_info.CODE_TS="1234567891";
		goods_info.G_NAME="日本进口花王Merries拉拉裤超级增量装L56";
		goods_info.G_DESC="无";
		goods_info.G_MODEL="PL_KJTBHDLY00021";
		goods_info.G_NUM="1";
		goods_info.G_UNIT="011";
		goods_info.PRICE="118.00";
		goods_info.CURR_CODE="142";
		goods_info.NOTE="无";
		goods_info.FREIGHT="23";
		goods_info.F_CURR_CODE="142";
		goods_info.ORDER_ID="2586898749";
		goods_info.EB_PLAT_ID="1201";
		GOODS_LIST.add(goods_info);
		waybillInfoVO.GOODS_LIST=GOODS_LIST;
		
		WaybillInfoVO.WAYBILL_INFO wayBill_infos = new WaybillInfoVO.WAYBILL_INFO();
		wayBill_infos.WAYBILL_ID="RM803720026CN";
		wayBill_infos.TRAF_MODE="9";
		wayBill_infos.DECL_PORT="0912";
		wayBill_infos.IE_PORT="0912";
		wayBill_infos.TRAF_NAME="CZ9856";
		wayBill_infos.VOYAGE_NO="CZ9856";
		wayBill_infos.BILL_NO="20141216_RM803721015CN";
		wayBill_infos.PACK_ID="";
		wayBill_infos.LOGI_ENTE_CODE="1101110326";
		wayBill_infos.LOGI_ENTE_NAME="辽宁 EMS";
		wayBill_infos.TOTAL_FREIGHT="25.00";
		wayBill_infos.CURR_CODE="142";
		wayBill_infos.GROSS_WEIGHT="0.78";
		wayBill_infos.PACK_NUM="1";
		wayBill_infos.CONSIGNEE_NAME="杨涛";
		wayBill_infos.CONSIGNEE_ADDR="北京朝阳区百子湾路苹果社区2-B26";
		wayBill_infos.CONSIGNEE_TEL="15101610220";
		wayBill_infos.CONSIGNEE_COUN="142";
		wayBill_infos.CONSIGNER_NAME="日本花王";
		wayBill_infos.CONSIGNER_ADDR="日本";
		wayBill_infos.CONSIGNER_COUN="116";
		wayBill_infos.NOTE="无";
		WAYBILL_HEAD.add(wayBill_infos);
		waybillInfoVO.WAYBILL_HEAD=WAYBILL_HEAD;
		
		requestContent.put("waybillInfo",Json.toJson(waybillInfoVO)+"");
		requestContent.put("identifier",encryptText_Order);
		JsonNode postResult = WSUtils.postByFormWithAuth(url, requestContent, Authorization_WayBill);
		logger.info(url+requestContent+postResult);
		String strResult = postResult.get("result").asText();
		if("true".equals(strResult)){
			result.setStatus("1");
		}else{
			result.setStatus("0");
		}
		result.setToast(strResult);
		return ok(Json.toJson(result));
	}
	
	/**
	 * 添加包裹
	 * @return
	 * @throws Exception
	 */
	public Result addOrder() throws Exception{
		response().setContentType("application/json;charset=utf-8");
		StatusToastVO result = new StatusToastVO();
		String pardelsId = AjaxHellper.getHttpParamOfFormUrlEncoded(request(), "id");
        //判断包裹是否为空
        if(StringUtils.isBlank(pardelsId)){
             result.setStatus("0");
             logger.error("添加包裹到ERP失败：传入的包裹ID参数为空");
             return ok(Json.toJson(result));
        }
        JsonNode strResult = dealAddOrderWithId(pardelsId);
        result.setStatus("1");
        result.setToast(strResult.toString());
        logger.info("添加包裹到ERP成功,接收返回结果："+strResult.toString());
        return ok(Json.toJson(result));
	}
	
	/**
	 * 
	 * <p>Title: piliangAddOrder</p> 
	 * <p>Description: 批量同步包裹到erp</p> 
	 * @return
	 * @throws Exception
	 */
	public Result piliangAddOrder() throws Exception{
		response().setContentType("application/json;charset=utf-8");
		String pardelCodes = AjaxHellper.getHttpParamOfFormUrlEncoded(request(), "pardelCodes");
		String[] codes = pardelCodes.split(",");
		for (String str : codes) {
			Parcels parcels = parcelsService.getParcelsByParcelCode(str);
			if(parcels!=null){
				JsonNode strResult = dealAddOrderWithId(String.valueOf(parcels.getId()));
				logger.info("批量添加包裹到ERP成功,接收返回结果："+strResult.toString());
			}else{
				logger.info("批量添加包裹到ERP失败,包裹号："+str);
			}
		}
		return ok(Json.toJson("ok"));
	}
	
	
	
	private JsonNode dealAddOrderWithId(String pardelsId) {
		 //获取包裹信息
        Parcels parcels = parcelsService.getParcelsById(Long.parseLong(pardelsId));
        ShoppingOrder shoppingOrder = shoppingOrderService.getShoppingOrderById(parcels.getOrderId());
        if(parcels==null||shoppingOrder==null){
        	logger.error("添加包裹到ERP失败：包裹 or 商品订单 or 用户信息为空");
        	return null;
        }
        List<ParcelsPro> parcelsPros = parcelsProService.queryParcelsProListByParcelsId(parcels.getId());
		String shopCode = "";	//店铺code
		String warehouseCode = "";//仓库code
		User user = new User();
		Long uid = (long) 0;
		//这里需要添加针对代下单商品的新逻辑		003 代下单   001 自营  002 嘿客店   新石器手机信息：店铺005  仓库XSQBJ001
		if("1".equals(parcels.getSrc())){
			shopCode="003";
			warehouseCode="DXDBJ001";
			user = userService.getUserByUid(shoppingOrder.getuId());
			uid = user.getUid();
		}else if("".equals(shoppingOrder.getSfcode())){
			shopCode="001";				
			warehouseCode="BJ001";		//嗨购自营
			user = userService.getUserByUid(shoppingOrder.getuId());
			uid = user.getUid();
		}else if(!"".equals(shoppingOrder.getSfcode())){
			if(99==shoppingOrder.getPaymethod()){
				shopCode="002";
				warehouseCode="HKD001";		//嘿客店
				uid = (long) -101;
			}else if(98==shoppingOrder.getPaymethod()){
				shopCode="005";
				warehouseCode="XSQBJ001";		//新石器
				uid = (long) -102;
			}
		}
		String url = "http://v2.api.guanyierp.com/rest/erp_open";
		
		JSONObject requestContent = new JSONObject();  
		List<Map> list_details = new ArrayList<Map>();
		for (ParcelsPro parcelsPro : parcelsPros) {
			Product product = productService.getProductById(parcelsPro.getpId());
			if(product.getIshot()==1){
				List<ProductGroup> productGroup = productService.findProductGroupListByPgId(product.getPid());
				for (ProductGroup productGroup2 : productGroup) {
					HashMap<String, String> m_details = new HashMap<String, String>();
					Product product2 = productService.getProductById(productGroup2.getPid());
					m_details.put("qty", String.valueOf(parcelsPro.getCounts()*productGroup2.getNum()));
					m_details.put("price", String.valueOf(product2.getRmbprice()));
					m_details.put("item_code", product2.getNewSku());
					m_details.put("note", "");
					m_details.put("refund", "0");
					m_details.put("oid", "");
					m_details.put("sku_code", "");
					list_details.add(m_details);
				}
			}else{
				HashMap<String, String> m_details = new HashMap<String, String>();
				m_details.put("qty", String.valueOf(parcelsPro.getCounts()));
				m_details.put("price", String.valueOf(parcelsPro.getPrice()));
				m_details.put("item_code", product.getNewSku());
				m_details.put("note", "");
				m_details.put("refund", "0");
				m_details.put("oid", "");
				m_details.put("sku_code", "");
				list_details.add(m_details);
			}
		}
		
		HashMap<String, String> m_payments = new HashMap<String, String>();
		m_payments.put("pay_type_code", String.valueOf(shoppingOrder.getPaymethod()));
		m_payments.put("payment", String.valueOf(shoppingOrder.getTotalFee()));
		m_payments.put("pay_code", shoppingOrder.getTradeno());
		m_payments.put("account", "");
		
		HashMap<String, String> m_invoices = new HashMap<String, String>();
		m_invoices.put("invoice_type", "1");
		m_invoices.put("invoice_title", "");
		m_invoices.put("invoice_content", "");
		m_invoices.put("invoice_amount", "");
		
	
		List<Map> list_payments = new ArrayList<Map>();
		list_payments.add(m_payments);
		List<Map> list_invoices = new ArrayList<Map>();
		list_invoices.add(m_invoices);
		
		
		JSONArray ja_details = JSONArray.fromObject(list_details);
		JSONArray ja_payments = JSONArray.fromObject(list_payments);
		JSONArray ja_invoices = JSONArray.fromObject(list_invoices);
        
	
		requestContent.put("appkey", "183822");
		requestContent.put("sessionkey","8e9da4390298410cbb62c23746abf28f");
		requestContent.put("method","gy.erp.trade.add");
		requestContent.put("refund","0");
		requestContent.put("cod","false");
		requestContent .put("platform_code" ,parcels.getParcelCode());
		
        requestContent.put( "shop_code", shopCode);         //店铺code，写死
        requestContent.put( "express_code", "SF_TH");      //物流公司code,写死
        requestContent.put( "warehouse_code", warehouseCode);    //仓库code,写死
        requestContent.put( "vip_code", uid);         //user->id
        requestContent.put( "receiver_name", parcels.getName());
        String str[] = new String[3];
        str = dealParcelsWithProvince(str, parcels.getProvince());
        if(str!=null&&str.length>=3){
	        requestContent.put( "receiver_province" ,str[0]);
	        requestContent.put( "receiver_city", str[1]);
	        requestContent.put( "receiver_district" ,str[2]);
	        requestContent.put( "receiver_address" ,parcels.getAddress());
        }else{
        	requestContent.put( "receiver_province" ,parcels.getProvince());
  	        requestContent.put( "receiver_city", "");
  	        requestContent.put( "receiver_district" ,"");
  	        requestContent.put( "receiver_address" ,parcels.getProvince()+" "+parcels.getAddress());
        }
        requestContent.put( "receiver_zip", "");
        requestContent.put( "receiver_mobile" ,parcels.getPhone());
        if(user!=null){
        	requestContent.put("receiver_phone", user.getPhone());
        }
      
        requestContent.put( "deal_datetime", shoppingOrder.getDate_add().toString());
        requestContent.put( "pay_datetime", shoppingOrder.getPaytime());
        requestContent.put( "post_fee", shoppingOrder.getForeignfee());
        
		requestContent.put("details",ja_details);
		requestContent.put("payments",ja_payments);
		requestContent.put("invoices",ja_invoices);
		//logger.info("post info:"+requestContent.toString());
        String sign_str=sign(requestContent.toString(),"d91440b2b9a34ebdb357e8dceca62765");
        JSONObject reqContent = new JSONObject();  
        reqContent.put("appkey", "183822");
        reqContent.put("sessionkey","8e9da4390298410cbb62c23746abf28f");
        reqContent.put("sign",sign_str);
        reqContent.put("method","gy.erp.trade.add");
        reqContent.put("refund","0");
        reqContent.put("cod","false");
        reqContent .put("platform_code" ,parcels.getParcelCode());
        reqContent.put( "shop_code", shopCode);         //店铺code，写死
        reqContent.put( "express_code", "SF_TH");      //物流公司code,写死
        reqContent.put( "warehouse_code", warehouseCode);    //仓库code,写死
        reqContent.put( "vip_code", uid);         //user->id
        reqContent.put( "receiver_name", parcels.getName());
        if(str!=null&&str.length>=3){
        	reqContent.put( "receiver_province" ,str[0]);
        	reqContent.put( "receiver_city", str[1]);
        	reqContent.put( "receiver_district" ,str[2]);
        	reqContent.put( "receiver_address" ,parcels.getAddress());
        }else{
        	reqContent.put( "receiver_province" ,parcels.getProvince());
        	reqContent.put( "receiver_city", "");
        	reqContent.put( "receiver_district" ,"");
        	reqContent.put( "receiver_address" ,parcels.getProvince()+" "+parcels.getAddress());
        }
        reqContent.put( "receiver_zip", "");
        reqContent.put( "receiver_mobile" ,parcels.getPhone());
        if(user!=null){
        	reqContent.put("receiver_phone", user.getPhone());
        }
        reqContent.put( "deal_datetime", shoppingOrder.getDate_add().toString());
        reqContent.put( "pay_datetime", shoppingOrder.getPaytime());
        reqContent.put( "post_fee", shoppingOrder.getForeignfee());

        reqContent.put("details",ja_details);
        reqContent.put("payments",ja_payments);
        reqContent.put("invoices",ja_invoices);
        //logger.info(" add Order by Pid:"+pardelsId+", shopCode:"+shopCode+", warehouseCode:"+warehouseCode+",address:"+reqContent.getString("receiver_address"));
        JsonNode strResult = WSUtils.postByJSON(url, Json.toJson(reqContent));
        logger.info(" add Order by Pid:"+pardelsId+", PardelCode :"+ parcels.getParcelCode() +" result:"+strResult.toString());
        return strResult;
	}


	/**
	 * 处理嗨购包裹的省份信息，对应ERP的省市区
	 * table erpaddress
	 * @param province
	 * @return
	 */
	private String[] dealParcelsWithProvince(String str[], String province) {
		int tempNum = 0; 
		while(true){
			str = erpAddressService.dealParcelsWithProvince(str, province);
			//如果区的数值不存在，则继续搜索
			if(StringUtils.isBlank(str[2])){
				//替换省市的信息继续搜索
				if(!StringUtils.isBlank(str[1])){
					province = province.replace(str[1].split("-")[1], "");
				}else if(!StringUtils.isBlank(str[0])){
					province = province.replace(str[0].split("-")[1], "");
				}
			}else{
				break;
			}
			tempNum++;
			if(tempNum>=3){
				break;
			}
		}
		//针对异常数据进行置空处理
		for (int i = 0; i < str.length; i++) {
			if(StringUtils.isBlank(str[i])){
				str = null;
				break;
			}else{
				str[i]=str[i].split("-")[1];
			}
		}
		return str;
	}


	/**
	 * 添加商品到erp
	 * @return
	 * @throws Exception
	 */
	@SuppressWarnings("deprecation")
	public Result addProduct() throws Exception{
		response().setContentType("application/json;charset=utf-8");
		 //获取商品
        String pid = AjaxHellper.getHttpParamOfFormUrlEncoded(request(), "pid");
        StatusToastVO result = new StatusToastVO();
        //判断用户和商品是否为空
        if(StringUtils.isBlank(pid)){
             result.setStatus("0");
             logger.error("添加商品到ERP失败：传入的商品PID参数为空");
             return ok(Json.toJson(result));
        }
        String strResult = dealAddProductWithPid(pid).toString();
		result.setStatus("1");
        result.setToast(strResult);
        //logger.info("添加商品到ERP,接收返回结果："+strResult);
        return ok(Json.toJson(result));
	}
	
	/**
	 * 根据PID添加商品信息
	 * @param result
	 * @param pid
	 * @return
	 * @throws IOException 
	 * @throws ClientProtocolException 
	 */
	private JsonNode dealAddProductWithPid(String pid) throws ClientProtocolException, IOException {
		String url = "http://v2.api.guanyierp.com/rest/erp_open";
		JSONObject requestContent = new JSONObject();  
        Product product = productService.getProductById(Long.parseLong(pid));
        //获取商品信息
        List<Map> list = new ArrayList<Map>();
        /*List<Product> products = productService.queryProductListByPpId(product.getPpid());
        for (Product product2 : products) {
        	 String sku_name = product2.getSpecifications();
        	 if(StringUtils.isBlank(sku_name)){
        		 sku_name=product2.getTitle();
        	 }
             HashMap<String, String> m1 = new HashMap<String, String>();
             m1.put("sku_code", product2.getNewSku());
             m1.put("sku_name", sku_name);
             m1.put("sku_weight", String.valueOf(product2.getWeight()));
             list.add(m1);
        }*/
        
		JSONArray ja2 = JSONArray.fromObject(list);
		
		BigDecimal rmb_price = getRmbPriceWithProduct(product);
		requestContent.put("appkey", "183822");
		requestContent.put("sessionkey","8e9da4390298410cbb62c23746abf28f");
		requestContent.put("method","gy.erp.item.add");
		requestContent.put("name",StringUtil.filterString(product.getTitle()));
		requestContent.put("code",product.getNewSku());
		requestContent.put("simple_name","");
		requestContent.put("weight",String.valueOf(product.getWeight()));
		requestContent.put("sales_point","");
		requestContent.put("package_point","");
		requestContent.put("purchase_price","");
		requestContent.put("sales_price",rmb_price+"");
		requestContent.put("note","");
		requestContent.put("skus",ja2);
        String sign_str=sign(requestContent.toString(),"d91440b2b9a34ebdb357e8dceca62765");
        JSONObject reqContent = new JSONObject();  
        reqContent.put("appkey", "183822");
        reqContent.put("sessionkey","8e9da4390298410cbb62c23746abf28f");
		reqContent.put("sign",sign_str);
        reqContent.put("method","gy.erp.item.add");
        reqContent.put("name",StringUtil.filterString(product.getTitle()));
        reqContent.put("code",product.getNewSku());
        reqContent.put("simple_name","");
        reqContent.put("weight",String.valueOf(product.getWeight()));
        reqContent.put("sales_point","");
        reqContent.put("package_point","");
        reqContent.put("purchase_price","");
        reqContent.put("sales_price",rmb_price+"");
        reqContent.put("note","");
        reqContent.put("skus",ja2);
        JsonNode result = WSUtils.postByJSON(url, Json.toJson(reqContent));
        if("false".equals(result.findValue("success").asText())){
        	logger.info(":addProduct by Pid:" + pid + " error:"+result);
        	//logger.info(":post info:"+requestContent.toString());
        }
		return result;
	}

	/**
	 * 获取到指定商品的rmb价格
	 * @param product
	 * @return
	 */
	private BigDecimal getRmbPriceWithProduct(Product product) {
		BigDecimal rmbPrice = new BigDecimal(0);
		Currency currency = productService.queryCurrencyById(product.getCurrency());
	    BigDecimal rate = new BigDecimal(currency.getRate()/100).setScale(4,  BigDecimal.ROUND_CEILING) ;
	    BigDecimal price = new BigDecimal(product.getPrice()/100).setScale(2,  BigDecimal.ROUND_CEILING) ;

		if(product.getIslockprice()==1){
			rmbPrice = new BigDecimal(product.getRmbprice()).setScale(1,  BigDecimal.ROUND_CEILING) ;
	    }else{
	    	rmbPrice = rate.multiply(price).setScale(1,  BigDecimal.ROUND_CEILING) ;
	    }
		return rmbPrice;
	}


	public Result GettestStockGet() throws Exception {
		response().setContentType("application/json;charset=utf-8");
		StatusToastVO result = new StatusToastVO();
		String url = "http://v2.api.guanyierp.com/rest/erp_open";
		HttpClient client = new DefaultHttpClient();
		HttpPost post = new HttpPost(url);
		post.setHeader("Content-Type", "text/xml; charset=UTF-8");
		
		  String requestContent = "{\n" +
                  "    \"appkey\": \"183822\",\n" +
                  "    \"method\": \"gy.erp.shop.get\",\n" +
                  "    \"page_no\": 1,\n" +
                  "    \"page_size\": 3,\n" +
                  "    \"sessionkey\": \"8e9da4390298410cbb62c23746abf28f\"\n"+
                  "}";
		  JSONObject requestJson=JSONObject.fromObject(requestContent);
          String sign_str=sign(requestJson.toString(),"d91440b2b9a34ebdb357e8dceca62765");
          String content = "{\n" +
                  "    \"appkey\": \"183822\",\n" +
                  "    \"method\": \"gy.erp.shop.get\",\n" +
                  "    \"page_no\": 1,\n" +
                  "    \"page_size\": 3,\n" +
                  "    \"sessionkey\": \"8e9da4390298410cbb62c23746abf28f\",\n"+
                  "    \"sign\": \""+sign_str+"\"\n"+
                  "}";
          JSONObject contentJson=JSONObject.fromObject(content);  
      	post.setEntity(new StringEntity(contentJson.toString(),"UTF-8"));
		HttpResponse res = client.execute(post);
		String strResult = EntityUtils.toString(res.getEntity(), "UTF-8");	
          result.setStatus("1");
          result.setToast(strResult);
          return ok(Json.toJson(result));
          //System.out.print(responseContent );
	}
	
	

	public String sign(String json,String secret){
        StringBuilder enValue = new StringBuilder();
        //前后加上secret
        enValue.append(secret);
        enValue.append(json);
        enValue.append(secret);
        // 使用MD5加密(32位大写)
        byte[] bytes = encryptMD5(enValue.toString());
        return byte2hex(bytes);
    }

    private static byte[] encryptMD5(String data) {
        byte[] bytes = null;
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            bytes = md.digest(data.getBytes("UTF-8"));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return bytes;
    }

    private static String byte2hex(byte[] bytes) {
        StringBuilder sign = new StringBuilder();
        for (int i = 0; i < bytes.length; i++) {
            String hex = Integer.toHexString(bytes[i] & 0xFF);
            if (hex.length() == 1) {
                sign.append("0");
            }
            sign.append(hex.toUpperCase());
        }
        return sign.toString();
    }
	
	// 客户端通知提醒接口（新增）(GET方式)
	public Result getHash1() throws Exception {
		response().setContentType("application/json;charset=utf-8");
		StatusToastVO result = new StatusToastVO();
		/*  订单
		String encryptText="556e5b17bfc8640e7ccb9df3";
		String encryptKey="cf70b5566ddf49a3a4d788a10a265c40";
		*/
		/*支付*/
		String encryptText="55822ae7bfc8641208c23c2b";
		String encryptKey="a2cfcb899ae24dd997bcd472779705e9";
		
		/*物流
		String encryptText="55823197bfc8641208c23c2e";
		String encryptKey="edbba08d170441b6a94674230fecc58d";
		*/ 
		String signature = createSignature(encryptText,encryptKey);
		signature = signature.replace("-","").toUpperCase();
        result.setStatus("1");
        result.setToast(signature);
		return ok(Json.toJson(result));
	}
	
	
	public static String createSignature(String baseString, String consumerkey) {
		String algorithm = "HmacSHA1";
		Mac mac = null;
		try {
			mac = Mac.getInstance(algorithm);
		} catch (NoSuchAlgorithmException e) {
			throw new RuntimeException(e);
		}
		byte[] key = consumerkey.getBytes();

		SecretKeySpec spec = new SecretKeySpec(key, algorithm);

		try {
			mac.init(spec);
		} catch (InvalidKeyException e) {
			throw new RuntimeException(e);
		}

		byte[] data;
		try {
			data = baseString.getBytes("UTF-8");
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException(e);
		}

		mac.update(data);
		StringBuilder sb = new StringBuilder();
		byte[] result = mac.doFinal();
		for (int i = 0; i < result.length; i++) {
			String str = Integer.toHexString(result[i] & 0xFF);

			if (str.length() == 1) {
				str = "0" + str;
			}
			sb.append(str.toLowerCase());
		}

		String signature = sb.toString();

		return signature;
	}
	
	/**
	 * 接口用于添加所有商品到ERP
	 * @throws Exception
	 */
	@SuppressWarnings("deprecation")
	public Result addAllProduct() throws Exception{
		String sql = "select pid from product where newSku!=''";
		JdbcOper db = JdbcOper.getInstance();// 创建DBHelper对象
		try {
			db.getPrepareStateDao(sql);
			ResultSet rs = db.pst.executeQuery();// 执行语句，得到结果集
			List<String> pids = new ArrayList<String>();
			int sendCount = 0;
			while(rs.next()){
				String pid = rs.getString("pid").trim(); 
				JsonNode result = dealAddProductWithPid(pid);
				if("false".equals(result.findValue("success").asText())){
		        	pids.add(pid);
		        }
			}
			while(true){
				if(pids!=null&&pids.size()>0){
					for (Iterator it = pids.iterator(); it.hasNext();) {
						String pid = (String)it.next();
						JsonNode result = dealAddProductWithPid(pid);
						if("true".equals(result.findValue("success").asText())){
							it.remove();
						}
					}
				}else{
					break;
				}
				sendCount++;
				if(sendCount>=2){
					for (String pid : pids) {
						logger.error("商品发送到ERP失败，失败商品ID:"+pid);
					}
					break;
				}
			}
			logger.info("所有商品添加完成！");
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			db.close();
		}
		return ok();
	}
	
	@SuppressWarnings("deprecation")
	public Result deleteAllProduct() throws Exception{
		String sql = "select newSku from product where newSku!=''";
		JdbcOper db = JdbcOper.getInstance();// 创建DBHelper对象
		try {
			db.getPrepareStateDao(sql);
			ResultSet rs = db.pst.executeQuery();// 执行语句，得到结果集
			while(rs.next()){
				String newSku = rs.getString("newSku"); 
				//判断用户和商品是否为空
		        if(!StringUtils.isBlank(newSku)){
		        	deleteProductWithPid(newSku);
		        }
			}
			logger.info("所有商品删除完成！");
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			db.close();
		}
		return ok();
	}
	
	@SuppressWarnings("deprecation")
	public Result deleteProductBySku() throws Exception{
		String newSku = AjaxHellper.getHttpParamOfFormUrlEncoded(request(), "newSku");
        StatusToastVO result = new StatusToastVO();
        //判断用户和商品是否为空
        if(StringUtils.isBlank(newSku)){
             result.setStatus("0");
             logger.error("添加商品到ERP失败：传入的商品PID参数为空");
             return ok(Json.toJson(result));
        }
		String strResult = deleteProductWithPid(newSku);
		logger.info("delete with newSku:"+newSku+", result:"+strResult);
		return ok();
	}

	/**
	 * 删除指定newSku的商品
	 * @param pid
	 * @throws IOException 
	 * @throws ParseException 
	 */
	private String deleteProductWithPid(String newSku) throws ParseException, IOException {
		String url = "http://v2.api.guanyierp.com/rest/erp_open";
		HttpClient client = new DefaultHttpClient();
		HttpPost post = new HttpPost(url);
		post.setHeader("Content-Type", "text/xml; charset=UTF-8");
		
		JSONObject requestContent = new JSONObject();		
        
		requestContent.put("appkey", "183822");
		requestContent.put("sessionkey","8e9da4390298410cbb62c23746abf28f");
		//requestContent.put("sign","A1B4FDF04F2B3F913E41E7F1F5D0DE97");
		requestContent.put("method","gy.erp.item.delete");
		requestContent.put("id","");
		requestContent.put("code",newSku);
        String sign_str=sign(requestContent.toString(),"d91440b2b9a34ebdb357e8dceca62765");
        JSONObject reqContent = new JSONObject();  
        reqContent.put("appkey", "183822");
        reqContent.put("sessionkey","8e9da4390298410cbb62c23746abf28f");
		reqContent.put("sign",sign_str);
        reqContent.put("method","gy.erp.item.delete");
        reqContent.put("id","");
        reqContent.put("code",newSku);
      	post.setEntity(new StringEntity(reqContent.toString(),"UTF-8"));
		HttpResponse res = client.execute(post);
		String strResult = EntityUtils.toString(res.getEntity(), "UTF-8");
		return strResult;
	}
	
	/**
	 * 修改商品信息
	 * @return
	 * @throws Exception
	 */
	@SuppressWarnings("deprecation")
	public Result updateProduct() throws Exception{
		String pid = AjaxHellper.getHttpParamOfFormUrlEncoded(request(), "pid");
        StatusToastVO result = new StatusToastVO();
        //判断用户和商品是否为空
        if(StringUtils.isBlank(pid)){
             result.setStatus("0");
             logger.error("通过ERP修改指定商品信息失败：传入的商品Id参数为空");
             return ok(Json.toJson(result));
        }
        updateProductByPid(pid);
		return ok();
	}

	/**
	 * 更新指定商品信息
	 * 暂未测试
	 * @param pid
	 * @throws IOException 
	 * @throws ClientProtocolException 
	 * @throws ParseException 
	 */
	public JsonNode updateProductByPid(String pid) throws ClientProtocolException, IOException, ParseException {
		String url = "http://v2.api.guanyierp.com/rest/erp_open";
		Product product = productService.getProductById(Long.parseLong(pid));
		JSONObject requestContent = new JSONObject();		
		requestContent.put("appkey", "183822");
		requestContent.put("sessionkey","8e9da4390298410cbb62c23746abf28f");
		requestContent.put("method","gy.erp.item.update");
		requestContent.put("code",product.getNewSku());
		requestContent.put("name",StringUtil.filterString(product.getTitle()));
		requestContent.put("simple_name","");
		requestContent.put("weight",String.valueOf(product.getWeight()));
		requestContent.put("sales_point","");
		requestContent.put("package_point","");
		requestContent.put("purchase_price","");
		requestContent.put("sales_price",getRmbPriceWithProduct(product)+"");
		requestContent.put("cost_price",product.getCostPrice()+"");
		requestContent.put("note","");
        String sign_str=sign(requestContent.toString(),"d91440b2b9a34ebdb357e8dceca62765");
        JSONObject reqContent = new JSONObject();  
        reqContent.put("appkey", "183822");
        reqContent.put("sessionkey","8e9da4390298410cbb62c23746abf28f");
		reqContent.put("sign",sign_str);
        reqContent.put("method","gy.erp.item.update");
        reqContent.put("code",product.getNewSku());
		reqContent.put("name",StringUtil.filterString(product.getTitle()));
		reqContent.put("simple_name","");
		reqContent.put("weight",String.valueOf(product.getWeight()));
		reqContent.put("sales_point","");
		reqContent.put("package_point","");
		reqContent.put("purchase_price","");
		reqContent.put("sales_price",getRmbPriceWithProduct(product)+"");
		reqContent.put("cost_price",product.getCostPrice()+""); 
		reqContent.put("note","");
      	JsonNode result = WSUtils.postByJSON(url, Json.toJson(reqContent));
      	logger.info("update product:"+product.getNewSku()+", result:"+result);
      	return result;
	}
	
	/**
	 * 通过包裹code查询到指定的物流运单号
	 * @return
	 * @throws Exception
	 */
	@SuppressWarnings("deprecation")
	public Result getDeliveryCodeByPardelCode() throws Exception{
		String pardelCode = AjaxHellper.getHttpParamOfFormUrlEncoded(request(), "pardelCode");
        StatusToastVO result = new StatusToastVO();
        //判断用户和商品是否为空
        if(StringUtils.isBlank(pardelCode)){
             result.setStatus("0");
             logger.error("通过ERP查询指定包裹的运单号失败：传入的包裹Code参数为空");
             return ok(Json.toJson(result));
        }
        getDeliveryCodeByPardelCode(pardelCode);
		return ok();
	}

	/**
	 * 通过包裹号获得运单信息
	 * @param pardelCode
	 * @throws IOException 
	 * @throws ClientProtocolException 
	 * @throws ParseException 
	 */
	public void getDeliveryCodeByPardelCode(String pardelCode) throws ClientProtocolException, IOException, ParseException {
		pardelCode = pardelCode==null?"":pardelCode.trim();
		String url = "http://v2.api.guanyierp.com/rest/erp_open";
		JSONObject requestContent = new JSONObject();		
		requestContent.put("appkey", "183822");
		requestContent.put("sessionkey","8e9da4390298410cbb62c23746abf28f");
		requestContent.put("method","gy.erp.trade.get");
		requestContent.put("platform_code",pardelCode);
        String sign_str=sign(requestContent.toString(),"d91440b2b9a34ebdb357e8dceca62765");
        JSONObject reqContent = new JSONObject();  
        reqContent.put("appkey", "183822");
        reqContent.put("sessionkey","8e9da4390298410cbb62c23746abf28f");
		reqContent.put("sign",sign_str);
        reqContent.put("method","gy.erp.trade.get");
        reqContent.put("platform_code",pardelCode);
      	JsonNode result = WSUtils.postByJSON(url, Json.toJson(reqContent));
      	logger.info("use parcels code:"+pardelCode+"get deliveryCode, result:"+result);
      	List jsonNodes = result.findValues("deliverys");
      	if(jsonNodes!=null&&jsonNodes.size()>0){
      		JsonNode jsonNode = (JsonNode)jsonNodes.get(0);
      		JsonNode jsonMailNo = jsonNode.findValue("mail_no");
      		JsonNode jsonDelivery = jsonNode.findValue("delivery");
      		JsonNode jsonExpressCode = jsonNode.findValue("express_code");
      		//更新运单号
      		if(jsonMailNo!=null&&jsonDelivery!=null){
      			String mail_no = jsonMailNo.asText();
      			String expressCode = jsonExpressCode.asText();
      			boolean delivery = jsonDelivery.asBoolean();
      			if(!StringUtils.isBlank(mail_no)&&delivery){
		      		//更新包裹的运单号：mailNo
      				try{
	      				Parcels parcels = parcelsService.getParcelsByParcelCode(pardelCode);
	      				if(parcels!=null&&"".equals(parcels.getMailnum())){
	      					//同时在pardels_Waybill插入一条数据，用于记录运单信息
	    		      		parcelsWaybillService.addNewWaybillWith(parcels.getId(),mail_no,expressCode);
	      					parcels.setMailnum(mail_no);
	    		      		if("1".equals(parcels.getSrc())){		//海外
	    		      			parcels.setStatus(4);
	    		      		}else if("2".equals(parcels.getSrc())||"90".equals(parcels.getSrc())){	//自营  || 嘿客
	    		      			parcels.setStatus(11);
	    		      		}
	    		      		parcels = parcelsService.saveParcels(parcels);
	    		      		ServiceFactory.getCacheService().clear(Constants.parcels_KEY+parcels.getId());
	    		      		ServiceFactory.getCacheService().setObject(Constants.parcels_KEY+parcels.getId(), parcels,0 );//写入缓存
	    		      		//logger.info("update mailnum:"+mail_no+"success");
	    		      		//同时订阅快递100的接口，以为后来获取物流信息
	    		      		boolean flag = Kuaidi100.subscribe(COM, mail_no,"","");
	    		      		if(!flag){
	    		      			logger.info("快递100订阅失败，包裹号："+pardelCode+" 物流单号："+mail_no);
	    		      		}else{
	    		      			logger.info("快递100订阅成功，包裹号："+pardelCode+" 物流单号："+mail_no);
	    		      		}
	      				}
      				}catch(Exception e){
      					logger.info("doJobWithGetDelivery Exception，包裹号更新物流信息异常,请检查包裹号是否重复,当前包裹号："+pardelCode);
      				}
      			}
      		}
      	}
	}
	
	/**
	 * 定时执行获取运单号到ERP
	 */
	@SuppressWarnings("deprecation")
	public Result doJobWithGetMailNo(){
		//获取所有无物运单号的包裹，同一记录包裹id
		String sql = "select pardelCode from pardels where mailnum='' and date_add>DATE_ADD(NOW(),INTERVAL -3 MONTH) order by id desc";
		JdbcOper db = JdbcOper.getInstance();// 创建DBHelper对象
		try {
			db.getPrepareStateDao(sql);
			ResultSet rs = db.pst.executeQuery();// 执行语句，得到结果集
			while(rs.next()){
				String pardelCode = rs.getString("pardelCode"); 
				//判断用户和商品是否为空
		        if(pardelCode!=null&&!StringUtils.isBlank(pardelCode)){
		        	getDeliveryCodeByPardelCode(pardelCode);
		        }
		        new Thread().sleep(1500);
			}
			logger.info("所有商品更新运单号完成！");
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			db.close();
		}
		return ok();
	}
	
	/**
	 * 定时执行添加包裹到ERP
	 */
	public Result doJobWithAddOrder(){
		//获取所有包裹更新时间在记录同步时间后的，进行同步添加到ERP
		long startTime = System.currentTimeMillis();
		AutoSyncTask autoSyncTask = autoSyncTaskService.getTaskWithCond("erp", "addOrder");
		Date currentTime = new Date();
		//监控当前处理的定时同步包裹的时间间隔是否超事
		monitorSyncTime(currentTime, autoSyncTask.getLasttime());
		//获取满足条件的包裹集合
		List<Parcels> parcelses = parcelsService.findParcelsWithDateAdd(autoSyncTask.getLasttime());
		//更新记录同步时间为当前
		saveAutoSyncTask(currentTime, autoSyncTask);
		//发送包裹到ERP
		if(parcelses!=null&&parcelses.size()>0){
			List<String> ids = new ArrayList<String>();
			int sendCount = 0;
			for (Parcels parcels : parcelses) {
				JsonNode result = dealAddOrderWithId(String.valueOf(parcels.getId()));
				if(result!=null&&"false".equals(result.findValue("success").asText())){
		        	ids.add(String.valueOf(parcels.getId()));
		        }
			}
			while(true){
				if(ids!=null&&ids.size()>0){
					for (Iterator it = ids.iterator(); it.hasNext();) {
						String id = (String)it.next();
						JsonNode result = dealAddOrderWithId(id);
						if("true".equals(result.findValue("success").asText())){
							it.remove();
						}
					}
				}else{
					break;
				}
				sendCount++;
				if(sendCount>=2){
					//将异常的数据放入数据库中
					for (String pardelId : ids) {
						AutoSyncTaskLog autoSyncTaskLog = autoSyncTaskLogService.queryWithRecord(pardelId,"addOrderErr","erperr");
						if(autoSyncTaskLog==null){
							autoSyncTaskLog = new AutoSyncTaskLog();
							autoSyncTaskLog.setRecord(pardelId);
							autoSyncTaskLog.setDateAdd(new Date());
							autoSyncTaskLog.setMemo("包裹同步至ERP失败");
							autoSyncTaskLog.setOperType("addOrderErr");
							autoSyncTaskLog.setTarget("erperr");
							autoSyncTaskLog.setDateUpd(new Date());
							autoSyncTaskLogService.save(autoSyncTaskLog);
							logger.info("添加包裹同步至ERP失败日志,包裹ID为："+autoSyncTaskLog.getRecord()+"，当前时间为："+Dates.formatDateTime(new Date()));
						}else{
							logger.info("添加包裹同步至ERP失败,日志已记录，将会在凌晨2点进行再次同步，包裹ID为："+autoSyncTaskLog.getRecord()+"，当前时间为："+Dates.formatDateTime(new Date()));
						}
					}
					break;
				}
			}
		}
		//获取当前时间段内的所有包裹的商品newSku
		/*List<String> newSkus = parcelsService.findNewSkusWithDateAdd(autoSyncTask.getLasttime());
		for (String newSku : newSkus) {
			//判断用户和商品是否为空
	        if(newSku!=null&&!StringUtils.isBlank(newSku)){
	        	//同步Erp嗨购商品信息入库
	        	getStockInfoByNewSku(newSku);
	        }
		}*/
		logger.info("定时同步包裹和相应商品库存信息操作完成,本次同步包裹数量："+parcelses.size());
		//获取嗨购所有自营商品信息newSku
		long endTime = System.currentTimeMillis();
		long time = (endTime-startTime)/1000/60;
		logger.info("本次同步包裹到Erp信息耗时："+time+"分");
		try {
			doJobWithUpdateProductStock();
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		endTime = System.currentTimeMillis();
		time = (endTime-startTime)/1000/60;
		logger.info("本次同步Erp库存信息入库时长："+time+"分");
		return ok();
	}

	/**
	 * 
	 * <p>Title: monitorSyncTime</p> 
	 * <p>Description: 监控当前处理的定时同步包裹的时间间隔是否超时</p> 
	 * @param currentTime
	 * @param lasttime
	 */
	private void monitorSyncTime(Date currentTime, Date lasttime) {
		long diff = currentTime.getTime()-lasttime.getTime();
		if(diff > (10*60*1000)){
			String htmlContent = dealWithSyncWarnings(currentTime,lasttime);
	        HashMap<String,Object> prop=new HashMap<String,Object>();
			prop.put("subject", "定时同步包裹到ERP警报 - "+Dates.getCurrentDay()); 
			prop.put("html", htmlContent);
			String sendTo = Play.application().configuration().getString("erp.syncwarning.sendTo");
	        EmailUtil.getInstance().sendMsg("", sendTo, prop);
	        logger.info("商品库存超量邮件发送完成");
		}
	}

	/**
	 * 
	 * <p>Title: dealWithSyncWarnings</p> 
	 * <p>Description: 定时同步包裹到erp预警邮件</p> 
	 * @return
	 */
	private String dealWithSyncWarnings(Date currentTime, Date lasttime) {
		StringBuilder builder = new StringBuilder();  
        builder.append("<html><head>");  
        builder.append("<meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\" />");  
        builder.append("</head><body>");  
        builder.append("Hi All：<br />");  
        builder.append("\t此封邮件为系统预警邮件，请高度重视，及时排查问题！<br />");  
        builder.append("<h1 color='red'>定时同步包裹至ERP超时</h1>");
        builder.append("\t当前时间为："+Dates.formatDateTime(currentTime));
        builder.append("\t同步最近一次操作时间为："+lasttime+"<br />");
        builder.append("已超过10分钟的预警时间，请及时排查，避免包裹多发重发！！！");
        builder.append("</body></html>");  
        String htmlContent = builder.toString(); 
		return htmlContent;
	}


	/**
	 * 
	 * <p>Title: doJobWithAddErrorOrder</p> 
	 * <p>Description: 定时执行将前一天失败的包裹同步到erp</p> 
	 * @return
	 */
	public Result doJobWithAddErrorOrder(){
		int sendCount = 0;
		while(true){
			List<AutoSyncTaskLog> autoSyncTaskLogs = autoSyncTaskLogService.getAllWithOperType("addOrderErr","erperr");
			if(autoSyncTaskLogs!=null&&autoSyncTaskLogs.size()>0){
				for (AutoSyncTaskLog autoSyncTaskLog : autoSyncTaskLogs) {
					String id = autoSyncTaskLog.getRecord();
					JsonNode result = dealAddOrderWithId(id);
					if("true".equals(result.findValue("success").asText())){
						autoSyncTaskLogService.del(autoSyncTaskLog);
					}else{
						autoSyncTaskLog.setDateUpd(new Date());
						autoSyncTaskLogService.save(autoSyncTaskLog);
						logger.info("凌晨2点，再次同步包裹至ERP失败日志："+autoSyncTaskLog.getRecord()+"，当前时间为："+Dates.formatDateTime(new Date()));
					}
				}
			}else{
				break;
			}
			sendCount++;
			if(sendCount>=2){
				logger.error("昨天失败的包裹重新发送到ERP仍然存在失败的包裹,请排查！！！");
				break;
			}
		}
		return ok();
	}
	
	/**
	 * 定时执行更新商品信息到ERP
	 * @throws IOException 
	 * @throws ClientProtocolException 
	 */
	public Result doJobWithUpdateProduct() throws ClientProtocolException, IOException{
		//获取所有商品更新时间在记录同步时间后的，进行同步添加到ERP
		AutoSyncTask autoSyncTask = autoSyncTaskService.getTaskWithCond("erp", "updateProduct");
		Date currentTime = new Date();
		//获取满足条件的商品集合
		List<Product> products = productService.findProductsWithDateUpd(autoSyncTask.getLasttime());
		//更新记录同步时间为当前
		saveAutoSyncTask(currentTime, autoSyncTask);
		//发送商品到ERP
		if(products!=null&&products.size()>0){
			List<String> pids = new ArrayList<String>();
			int sendCount = 0;
			for (Product product : products) {
				JsonNode result = updateProductByPid(String.valueOf(product.getPid()));
				if("false".equals(result.findValue("success").asText())){
		        	pids.add(String.valueOf(product.getPid()));
		        }
			}
			while(true){
				if(pids!=null&&pids.size()>0){
					for (Iterator it = pids.iterator(); it.hasNext();) {
						String pid = (String)it.next();
						JsonNode result = dealAddProductWithPid(pid);
						if("true".equals(result.findValue("success").asText())){
							it.remove();
						}
					}
				}else{
					break;
				}
				sendCount++;
				if(sendCount>=2){
					for (String pid : pids) {
						logger.error("商品发送到ERP失败，失败商品ID:"+pid);
					}
					break;
				}
			}
		}
		logger.info("定时同步商品信息到ERP操作完成,本次同步商品数量："+products.size());
		return ok();
	}
	
	/**
	 * 更新同步任务信息
	 * @param autoSyncTask
	 */
	private void saveAutoSyncTask(Date currentTime, AutoSyncTask autoSyncTask) {
		autoSyncTask.setLasttime(currentTime);
		logger.info("更新同步任务："+autoSyncTask.getMemo()+"时间为："+currentTime);
		autoSyncTaskService.save(autoSyncTask);
	}
	
	/**
	 * 定时执行从Erp获取库存信息更新入库product->nstock
	 * @throws IOException 
	 * @throws ClientProtocolException 
	 */
	public Result doJobWithUpdateProductStock() throws ClientProtocolException, IOException{
		//获取嗨购所有自营商品信息newSku
		long startTime = System.currentTimeMillis();
		String sql = "SELECT newSku FROM product WHERE typ=2 AND newSku!=''  AND STATUS='10' AND pid NOT IN(SELECT pid FROM adminproduct)";
		JdbcOper db = JdbcOper.getInstance();// 创建DBHelper对象
		try {
			db.getPrepareStateDao(sql);
			ResultSet rs = db.pst.executeQuery();// 执行语句，得到结果集
			while(rs.next()){
				String newSku = rs.getString("newSku"); 
				//判断用户和商品是否为空
		        if(newSku!=null&&!StringUtils.isBlank(newSku)){
		        	//同步Erp嗨购商品信息入库
		        	getStockInfoByNewSku(newSku);
		        	new Thread().sleep(1500);
		        }
			}
			logger.info("同步Erp库存信息入库操作完成！");
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			db.close();
		}
		long endTime = System.currentTimeMillis();
		long time = (endTime-startTime)/1000/60;
		logger.info("本次同步Erp库存信息入库时长："+time+"分");
		return ok();
	}
	
	/**
	 * 通过商品newsku获取库存余量
	 * @return
	 * @throws Exception
	 */
	@SuppressWarnings("deprecation")
	public Result getStockInfoByNewSku() throws Exception{
		String newSku = AjaxHellper.getHttpParamOfFormUrlEncoded(request(), "newSku");
        StatusToastVO result = new StatusToastVO();
        //判断商品newSku
        if(StringUtils.isBlank(newSku)){
             result.setStatus("0");
             logger.error("通过ERP查询指定包裹的运单号失败：传入的包裹Code参数为空");
             return ok(Json.toJson(result));
        }
        getStockInfoByNewSku(newSku);
		return ok();
	}
	
	/**
	 * 获取到指定sku对应的库存信息
	 * @param newSku
	 * @return
	 */
	private String getStockInfoByNewSku(String newSku) {
		String url = "http://v2.api.guanyierp.com/rest/erp_open";
		JSONObject requestContent = new JSONObject();		
		requestContent.put("appkey", "183822");
		requestContent.put("sessionkey","8e9da4390298410cbb62c23746abf28f");
		requestContent.put("method","gy.erp.stock.get");
		requestContent.put("item_code",newSku);
		requestContent.put("warehouse_code","BJ001");
        String sign_str=sign(requestContent.toString(),"d91440b2b9a34ebdb357e8dceca62765");
        JSONObject reqContent = new JSONObject();  
        reqContent.put("appkey", "183822");
        reqContent.put("sessionkey","8e9da4390298410cbb62c23746abf28f");
		reqContent.put("sign",sign_str);
        reqContent.put("method","gy.erp.stock.get");
        reqContent.put("item_code",newSku);
        reqContent.put("warehouse_code","BJ001");
      	JsonNode result = WSUtils.postByJSON(url, Json.toJson(reqContent));
      	//logger.info("use product newSku:"+newSku+"get salable_qty, result:"+result.toString());
      	List jsonNodes = result.findValues("stocks");
      	if(jsonNodes!=null&&jsonNodes.size()>0){
      		JsonNode jsonNode = (JsonNode)jsonNodes.get(0);
      		JsonNode jsonItemCode = jsonNode.findValue("item_code");
      		//更新指定商品的可用库存
      		if(jsonItemCode!=null){
      			String itemCode = jsonItemCode.asText();
      			if(!StringUtils.isBlank(itemCode)&&itemCode.equals(newSku)){
		      		//更新库存量nstock
      				JsonNode jsonSalableQty = jsonNode.findValue("salable_qty");
      				if(jsonSalableQty!=null){
      					String str = "";
      					List<Product> products = productService.getProductByNewSku(newSku);
      					if(products!=null&&products.size()>0){
      						for (Product product : products) {
      							ServiceFactory.getCacheService().clear(Constants.product_KEY+product.getPid());
      							Long salable_qty = jsonSalableQty.asLong();
      							int isSyncErp = product.getIsSyncErp();
      							Long stockThreshold = Configuration.root().getLong("stock.threshold");
      							if(isSyncErp==9){
      								return "";
      							} else if((isSyncErp==1&&salable_qty<=stockThreshold)||(isSyncErp==2&&salable_qty<=0)){
      								str = product.getPid()+":"+product.getTitle()+":"+product.getNewSku()+":"+salable_qty+":";
      								salable_qty = (long) 0;
      							}
      							product.setNstock(salable_qty);
      							product = productService.saveProduct(product);
      							ServiceFactory.getCacheService().setObject(Constants.product_KEY+product.getPid(), product, 0);
      							//logger.info("更新库存成功，商品NewSku："+newSku+" 可售数量："+salable_qty);
							}
      					}
		      			return str;
      				}else{
      					logger.info("更新库存失败，商品NewSku："+newSku);
      				}
      			}
      		}
      	}
      	return "";
	}
	
	/**
	 * 
	 * <p>Title: updateTradeRefund</p> 
	 * <p>Description: 订单退款状态修改</p> 
	 * @param pardelsCode
	 * @return
	 */
	public Result updateTradeRefund() {
		StatusToastVO result = new StatusToastVO();
		String pardelsCodes = AjaxHellper.getHttpParamOfFormUrlEncoded(request(), "pardelsCodes");
        //判断包裹是否为空
        if(StringUtils.isBlank(pardelsCodes)){
             result.setStatus("0");
             logger.error("订单退款状态修改失败：传入的包裹code参数为空");
             return ok(Json.toJson(result));
        }
        String[] pardelsCodees = pardelsCodes.split(",");
        for (int i = 0 ; i < pardelsCodees.length; i++) {
        	JsonNode jsonNode = getReturn(i,pardelsCodees[i]);
          	logger.info("update trade refund pardelsCode"+pardelsCodees[i]+", result:"+Json.toJson(jsonNode));
		}
      	return ok(Json.toJson(result));
	}
	
	private JsonNode getReturn(int i,String string) {
		String url = "http://v2.api.guanyierp.com/rest/erp_open";
		JSONObject requestContent = new JSONObject();		
		requestContent.put("appkey", "183822");
		requestContent.put("sessionkey","8e9da4390298410cbb62c23746abf28f");
		requestContent.put("method","gy.erp.trade.refund.update");
		requestContent.put("tid",string);
		requestContent.put("oid",i);
		requestContent.put("refund_state",1);
        String sign_str=sign(requestContent.toString(),"d91440b2b9a34ebdb357e8dceca62765");
        JSONObject reqContent = new JSONObject();  
        reqContent.put("appkey", "183822");
        reqContent.put("sessionkey","8e9da4390298410cbb62c23746abf28f");
		reqContent.put("sign",sign_str);
        reqContent.put("method","gy.erp.trade.refund.update");
        reqContent.put("tid",string);
        reqContent.put("oid",i);
        reqContent.put("refund_state",1);
      	JsonNode strResult = WSUtils.postByJSON(url, Json.toJson(reqContent));
      	return strResult;
	}


	/**
	 * 每天早上9:35，对可售库存数大于100的整理成邮件发送给 jiejinghua@higegou.com liumiao@higegou.com wangshuangshi@higegou.com kangxue@higegou.com wangyujue@higegou.com lvcheng@higegou.com
	 * 获取邮件需要发送的内容
	 */
	@SuppressWarnings("deprecation")
	public Result sendOverStockInfos(){
		Integer overLine = Play.application().configuration().getInt("erp.overstock.overLine");
		List<Product> productInfos = productService.getOverStockInfo(overLine);
		if(productInfos!=null&&productInfos.size()>0){
			String htmlContent = dealWithOverStockInfos(overLine,productInfos);
	        HashMap<String,Object> prop=new HashMap<String,Object>();
			prop.put("subject", "真实库存排名  "+Dates.getCurrentDay()); 
			prop.put("html", htmlContent);
			String sendTo = Play.application().configuration().getString("erp.overstock.sendTo");
	        EmailUtil.getInstance().sendMsg("", sendTo, prop);
	        logger.info("商品库存超量邮件发送完成");
  		}else{
  			logger.info("商品库存合理，无超量商品");
  		}
        return ok();
	}
	
	/**
	 * 每天早上9:30发送更新库存任务，对可售库存数低于5的整理成邮件发送给wangshuangshi@higegou.com
	 * 获取邮件需要发送的内容
	 */
	@SuppressWarnings("deprecation")
	public Result sendStockInfos(){
		List<String> stockInfos = getStockInfo();
		if(stockInfos!=null&&stockInfos.size()>0){
			String htmlContent = dealWithStockInfos(stockInfos);
			HashMap<String,Object> prop=new HashMap<String,Object>();
			prop.put("subject", "真实库存阀值预警  "+Dates.getCurrentDay()); 
			prop.put("html", htmlContent);
			String sendTo = Play.application().configuration().getString("erp.stock.sendTo");
			EmailUtil.getInstance().sendMsg("", sendTo, prop);
			logger.info("商品库存预警邮件完成");
		}else{
			logger.info("商品库存充足，无预警商品");
		}
		return ok();
	}
	
	/**
	 * 获取邮件需要发送的内容
	 * @return
	 */
	public List<String> getStockInfo(){
		//获取嗨购所有自营商品信息newSku
		String sql = "select newSku from product where typ=2 and newSku!=''";
		JdbcOper db = JdbcOper.getInstance();// 创建DBHelper对象
		List<String> stockInfos = new ArrayList<String>();
		try {
			db.getPrepareStateDao(sql);
			ResultSet rs = db.pst.executeQuery();// 执行语句，得到结果集
			while(rs.next()){
				String newSku = rs.getString("newSku"); 
				//判断用户和商品是否为空
		        if(newSku!=null&&!StringUtils.isBlank(newSku)){
		        	//同步Erp嗨购商品信息入库
		        	String str = getStockInfoByNewSku(newSku);
		        	if(str!=null&&!"".equals(str)){
		        		//str格式为->商品id:商品title:商品newsku:商品库存
		        		stockInfos.add(str);
		        	}
		        }
			}
			logger.info("同步Erp库存信息入库操作完成！");
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			db.close();
		}
		return stockInfos;
	}
	
	/**
	 * 处理邮件发送内容
	 * @param stockInfos
	 * @return
	 */
	private String dealWithStockInfos(List<String> stockInfos) {
		StringBuilder builder = new StringBuilder();  
        builder.append("<html><head>");  
        builder.append("<meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\" />");  
        builder.append("</head><body>");  
        builder.append("Hi All：<br />");  
        builder.append("\t此封邮件为系统自动发送，请勿回复！<br />");  
        builder.append("库存阀值预警，请相关负责人查看并处理，辛苦各位！<br />以下为预警商品详细内容，请查阅：");  
        builder.append("<table style='width:60%' border='1'><thead><th>商品ID</th><th>商品名称</th><th>新SKU</th><th>可售库存</th></thead><tbody>");
        for (String stockInfo : stockInfos) {
        	//stockInfo格式为->商品id:商品title:商品newsku:商品库存
        	String[] infos = stockInfo.split(":");
        	builder.append("<tr><td>");
        	builder.append(infos[0]);
        	builder.append("</td><td>");
        	builder.append(infos[1]);
        	builder.append("</td><td>");
        	builder.append(infos[2]);
        	builder.append("</td><td>");
        	builder.append(infos[3]);
        	builder.append("</td></tr>");
		}
        builder.append("</tbody></table>");  
        builder.append("</body></html>");  
        String htmlContent = builder.toString(); 
		return htmlContent;
	}
	
	/**
	 * 处理邮件发送内容
	 * @param stockInfos
	 * @return
	 */
	private String dealWithOverStockInfos(Integer overLine, List<Product> productInfos) {
		StringBuilder builder = new StringBuilder();  
        builder.append("<html><head>");  
        builder.append("<meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\" />");  
        builder.append("</head><body>");  
        builder.append("Hi All：<br />");  
        builder.append("\t此封邮件为系统自动发送，请勿回复！<br />");  
        builder.append("此邮件为可售库存大于 "+overLine+" 的“商品ID、商品名称、新SKU、可售库存”，按照可售库存数降序排列！<br />以下为商品详细内容，请查阅：");  
        builder.append("<table style='width:60%' border='1'><thead><th>商品ID</th><th>商品名称</th><th>新SKU</th><th>可售库存</th></thead><tbody>");
        for (Product productInfo : productInfos) {
        	builder.append("<tr><td>");
        	builder.append(productInfo.getPid());
        	builder.append("</td><td>");
        	builder.append(productInfo.getTitle());
        	builder.append("</td><td>");
        	builder.append(productInfo.getNewSku());
        	builder.append("</td><td>");
        	builder.append(productInfo.getNstock());
        	builder.append("</td></tr>");
		}
        builder.append("</tbody></table>");  
        builder.append("</body></html>");  
        String htmlContent = builder.toString(); 
		return htmlContent;
	}

	/**
	 * 定时执行添加商品到ERP
	 * @throws IOException 
	 * @throws ClientProtocolException 
	 */
	public Result doJobWithAddProduct() throws ClientProtocolException, IOException{
		//获取所有商品更新时间在记录同步时间后的，进行同步添加到ERP
		AutoSyncTask autoSyncTask = autoSyncTaskService.getTaskWithCond("erp", "addProduct");
		Date currentTime = new Date();
		//获取满足条件的商品集合
		List<Product> products = productService.findProductsWithDateAdd(autoSyncTask.getLasttime());
		//更新记录同步时间为当前
		saveAutoSyncTask(currentTime, autoSyncTask);
		//发送商品到ERP
		if(products!=null&&products.size()>0){
			List<String> ids = new ArrayList<String>();
			int sendCount = 0;
			for (Product product : products) {
				JsonNode result = dealAddProductWithPid(String.valueOf(product.getPid()));
				if(result!=null&&"false".equals(result.findValue("success").asText())){
		        	ids.add(String.valueOf(product.getPid()));
		        }
			}
			while(true){
				if(ids!=null&&ids.size()>0){
					for (Iterator it = ids.iterator(); it.hasNext();) {
						String id = (String)it.next();
						JsonNode result = dealAddProductWithPid(id);
						if("true".equals(result.findValue("success").asText())){
							it.remove();
						}
					}
				}else{
					break;
				}
				sendCount++;
				if(sendCount>=2){
					for (String id : ids) {
						logger.error("商品发送到ERP失败，失败商品ID:"+id);
					}
					break;
				}
			}
		}
		logger.info("定时同步商品信息到ERP操作完成,本次同步商品数量："+products.size());
		return ok();
	}

	public static void main(String[] args) {
	            String JsonStr = "";
	            JsonStr = JsonStr + "{";
	            JsonStr = JsonStr + "\"WAYBILL_HEAD\"";
	            JsonStr = JsonStr + ":";
	            JsonStr = JsonStr + "[{";
	            JsonStr = JsonStr + "\"WAYBILL_ID\":\"RM803720026CN\",";
	            JsonStr = JsonStr + "\"TRAF_MODE\":\"9\",";
	            JsonStr = JsonStr + "\"DECL_PORT\":\"0912\",";
	            JsonStr = JsonStr + "\"IE_PORT\":\"0912\",";
	            JsonStr = JsonStr + "\"TRAF_NAME\":\"CZ9855\",";
	            JsonStr = JsonStr + "\"VOYAGE_NO\":\"CZ9855\",";
	            JsonStr = JsonStr + "\"BILL_NO\":\"20141216_RM803720016CN\",";
	            JsonStr = JsonStr + "\"PACK_ID\":\" \",";
	            JsonStr = JsonStr + "\"LOGI_ENTE_CODE\":\"1101110326\",";
	            JsonStr = JsonStr + "\"LOGI_ENTE_NAME\":\"辽宁EMS\",";
	            JsonStr = JsonStr + "\"TOTAL_FREIGHT\":\"25.00\",";
	            JsonStr = JsonStr + "\"CURR_CODE\":\"142\",";
	            JsonStr = JsonStr + "\"GROSS_WEIGHT\":\"0.78\",";
	            JsonStr = JsonStr + "\"PACK_NUM\":\"1\",";
	            JsonStr = JsonStr + "\"CONSIGNEE_NAME\":\"林红阳\",";
	            JsonStr = JsonStr + "\"CONSIGNEE_ADDR\":\"大连市高新区黄浦路541号网络产业大厦910室\",";
	            JsonStr = JsonStr + "\"CONSIGNEE_TEL\":\"13998421234\",";
	            JsonStr = JsonStr + "\"CONSIGNEE_COUN\":\"142\",";
	            JsonStr = JsonStr + "\"CONSIGNER_NAME\":\"日本花王\",";
	            JsonStr = JsonStr + "\"CONSIGNER_ADDR\":\"日本\",";
	            JsonStr = JsonStr + "\"CONSIGNER_COUN\":\"116\",";
	            JsonStr = JsonStr + "\"NOTE\":\"无\"";
	            JsonStr = JsonStr + "}],";

	            JsonStr = JsonStr + "\"GOODS_LIST\"";
	            JsonStr = JsonStr + ":";
	            JsonStr = JsonStr + "[";

	            JsonStr = JsonStr + "{";
	            JsonStr = JsonStr + "\"WAYBILL_ID\":\"RM803720026CN\",";
	            JsonStr = JsonStr + "\"LOGI_ENTE_CODE\":\"1101110326\",";
	            JsonStr = JsonStr + "\"G_NO\":\"1\",";
	            JsonStr = JsonStr + "\"CODE_TS\":\"1234567890\",";
	            JsonStr = JsonStr + "\"G_NAME\":\"日本进口 花王Merries拉拉裤 超级增量装L56\",";
	            JsonStr = JsonStr + "\"G_DESC\":\"无\",";
	            JsonStr = JsonStr + "\"G_MODEL\":\"PL_KJTBHDLY00024\",";
	            JsonStr = JsonStr + "\"G_NUM\":\"1\",";
	            JsonStr = JsonStr + "\"G_UNIT\":\"011\",";
	            JsonStr = JsonStr + "\"PRICE\":\"118.00\",";
	            JsonStr = JsonStr + "\"CURR_CODE\":\"142\",";
	            JsonStr = JsonStr + "\"FREIGHT\":\"25.00\",";
	            JsonStr = JsonStr + "\"F_CURR_CODE\":\"142\",";
	            JsonStr = JsonStr + "\"ORDER_ID\":\"20150627001\",";
	            JsonStr = JsonStr + "\"EB_PLAT_ID\":\"1201\",";
	            JsonStr = JsonStr + "\"NOTE\":\"无\"";
	            JsonStr = JsonStr + "},";

	            JsonStr = JsonStr + "{";
	            JsonStr = JsonStr + "\"WAYBILL_ID\":\"RM803720026CN\",";
	            JsonStr = JsonStr + "\"LOGI_ENTE_CODE\":\"1101110326\",";
	            JsonStr = JsonStr + "\"G_NO\":\"2\",";
	            JsonStr = JsonStr + "\"CODE_TS\":\"1234567890\",";
	            JsonStr = JsonStr + "\"G_NAME\":\"日本进口 花王Merries拉拉裤 超级增量装L56\",";
	            JsonStr = JsonStr + "\"G_DESC\":\"无\",";
	            JsonStr = JsonStr + "\"G_MODEL\":\"PL_KJTBHDLY00024\",";
	            JsonStr = JsonStr + "\"G_NUM\":\"1\",";
	            JsonStr = JsonStr + "\"G_UNIT\":\"011\",";
	            JsonStr = JsonStr + "\"PRICE\":\"118.00\",";
	            JsonStr = JsonStr + "\"CURR_CODE\":\"142\",";
	            JsonStr = JsonStr + "\"FREIGHT\":\"25.00\",";
	            JsonStr = JsonStr + "\"F_CURR_CODE\":\"142\",";
	            JsonStr = JsonStr + "\"ORDER_ID\":\"20150627001\",";
	            JsonStr = JsonStr + "\"EB_PLAT_ID\":\"1201\",";
	            JsonStr = JsonStr + "\"NOTE\":\"无\"";
	            JsonStr = JsonStr + "}";

	            JsonStr = JsonStr + "],";


	            JsonStr = JsonStr + "\"ORDER_LIST\"";
	            JsonStr = JsonStr + ":";
	            JsonStr = JsonStr + "[";
	            JsonStr = JsonStr + "{";
	            JsonStr = JsonStr + "\"WAYBILL_ID\":\"RM803720016CN\",";
	            JsonStr = JsonStr + "\"LOGI_ENTE_CODE\":\"1101110326\",";
	            JsonStr = JsonStr + "\"ORDER_ID\":\"20150627001\",";
	            JsonStr = JsonStr + "\"EB_PLAT_ID\":\"1201\"";
	            JsonStr = JsonStr + "}";
	            JsonStr = JsonStr + "]";


	            JsonStr = JsonStr + "}";
	            System.out.println(JsonStr);

	}
	
}
