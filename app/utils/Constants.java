package utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import play.Configuration;


/**
 * 常量配置
 * @author luobotao
 * Date: 2015年4月18日 上午11:46:25
 */
public class Constants {

    public static final String NL = "\r\n";

    public static final String COMMA = ",";

    public static final String NAMESPACE_SMS = "sms.";
    public static final String product_KEY = "product.id.";//商品
    public static final String category_KEY = "category.id.";//品类
    public static final String currency_KEY = "currency.id.";//符号
    public static final String fromsite_KEY = "fromsite.id.";//来源
    public static final String mould_KEY = "mould.id.";//卡片
    public static final String channel_mould_KEY = "channel_mould.id.";//频道卡片
    public static final String channel_mould_pro_KEY = "channel_mould_pro.id.";//卡片商品
    public static final String channelMouldIds_KEY = "channelMouldIds.cid.";
    public static final String channelMouldProIds_KEY = "channelMouldProIds.cid.";
    public static final String subject_mould_KEY = "subject_mould.id.";//专题卡片
    public static final String subject_mould_pro_KEY = "subject_mould_pro.id.";//专题卡片商品
    public static final String subjectMouldIds_KEY = "subjectMouldIds.cid.";	
    public static final String subjectMouldProIds_KEY = "subjectMouldProIds.cid.";
    
    public static final String shoppingOrder_KEY = "shoppingOrder.id.";
    public static final String orderProduct_KEY = "orderProduct.id.";
    public static final String parcels_KEY = "parcels.id.";
    public static final String parcelsWaybill_KEY = "parcelsWaybill.id.";
    public static final String NAMESPACE_SESSION_USER = "session.user.";
    public static final String CERTIFICATION_TIME_CHECKED = "certification.times.";

    public static final String NAMESPACE_SESSION_TOKEN = "session.token.";

    public static final Integer FIVE_MINUTES = 60 * 5;

    public static final Integer THIRTY_MINUTES = 60 * 30;

    public static final Integer SESSION_TOKEN_EXPIRE_IN = 60 * 20;

    public static final Integer SMS_CODE_CACHE_EXPIRATION = 60 * 30;

    public static final String INIT = "init";

    public static final String CONFIRM = "confirm";

    public static final String MODIFY = "modify";

    public static final List<String> USER_AGENTS = new ArrayList(Arrays.asList("iphone","android","ucbrowser"));

    public static final String DYNAMIC_TOKEN_HEADER = "dToken";

    public static final Integer DYNAMIC_TOKEN_LENGTH = 32;

    public static final String CACHE_NAMESPACE_LOGIN_TOKEN = "loginToken.";

    public static final Integer LOGIN_TOKEN_EXPIRE_IN = 14 * 24 * 60 * 60;

    public static final List<Pattern> NEED_CHECK_REFERER_URL_PATTERN = new ArrayList<>();

    public static final String NAMESPACE_CACHE_INIT = Constants.INIT + ".";

    public static final String NAMESPACE_CACHE_CONFIRM = Constants.CONFIRM + ".";

    public static final String NAMESPACE_CACHE_MODIFY = Constants.MODIFY + ".";

    public static final String SESSION_TOKEN = "token";

    public static final String ADMIN_SESSION_NAME = "_A_S";

    public static final String CACHE_NAMESPACE_ADMINSESSION_USER = "admin.session.user.";

    public static final Integer ADMINSESSION_TOKEN_EXPIRE_IN = 60 * 20;

    public static final String CACHE_NAMESPACE_CONFIG = "config.";

    public static final Integer CONFIG_CACHE_EXPIRE_IN = 60 * 2;
    
    public static final Integer PAGESIZE = 10;
    
	public final static String Session_Validate_Image = "imgcode"; // 图形验证码

    public static final String TMP_DIR = System.getProperty("java.io.tmpdir");

    public static final boolean IsProduct = Configuration.root().getBoolean("production", false);

    /*
     * 代言H5页页URL
     */
    public static final String ENDORSEMENT_URL="http://h5.higegou.com/sheSaid/endorsement?daiyanid=";
    /*
     * 申请代言URL
     */
    public static final String ENDORSEMENT_QUEST_URL="http://www.badu.com";
    
    /*
     * H5申请提现URL
     */
    public static final String ENDORSEMENT_MONEY_URL="http://www.163.com";
    
    /*
     * 微信AppID
     */
    //public static final String WXappID="wx0cef7e835f598e36";
    public static final String WXappID="wx99199cff15133f37";//wx80a445e58ff37347";
    /*
     * 微信密钥
     */
    //public static final String WXappsecret="416704762a6b40c20025ea169bb00e61";
    public static final String WXappsecret="a017774f117bf0100a2f7939ef56c89a";//"9992b9b4a1cc16c59c6ff49d99378cc5";
    /*
     * 微信支付密钥
     */
    public static final String WXappsecretpay="neolixxinshiqiqinengwanwei123456";//"neolixxinshiqiqinengwanwei123456";
    /*
     * 微信支付商户号
     */
    public static final String WXMCID="1235413502";//"1228736802";
	
    /*
     * 微信支付ＪＳＡＰＩ回调地址
     */
    public static final String WXCALLBACK="http://h5.higegou.com/api/wxpayreturnjsapi";
    
    /*
     * 微博授权
	 */
	public static final String APP_KEY = "3543275707";
	public static final String APP_SECRET = "3476c14cd5994a23e9a24c914e8f5435";
	
	public static final String doJobWithAddOrderErrorIds_KEY = "doJobWithAddOrder.errorIds";
	
	
    
	/********埋点数据********************************/
	public static final String MAIDIAN_SHANGPINXIANGQING="0";
	public static final String  MAIDIAN_PINDAO="1";
	public static final String MAIDIAN_ZHUANTI="2";
	public static final String MAIDIAN_GOUWUCHE="3";
	public static final String MAIDIAN_DINGDAN="4";
	public static final String MAIDIAN_TUIJIAN="5";
	public static final String MAIDIAN_DAIYAN="6";
	public static final String MAIDIAN_CAINIXIHUAN="7";
	public static final String MAIDIAN_WODEZUIAI="8";
	public static final String MAIDIAN_SOUSUOTUIJIAN="9";
	public static final String MAIDIAN_SOUSUO="a";
	public static final String MAIDIAN_PUSH="b";
	/********埋点数据结束*****************************/
	
	/********棒棒糖数据*******************************/
	public static final String BBTPRICE="3.99";
	
	/********棒棒糖数据结束****************************/
    static{
        NEED_CHECK_REFERER_URL_PATTERN.add(Pattern.compile(""));
        NEED_CHECK_REFERER_URL_PATTERN.add(Pattern.compile(""));
        NEED_CHECK_REFERER_URL_PATTERN.add(Pattern.compile(""));
    }
    

    //订单状态
	public static enum OrderStatus {
		DELETE(-99, "已删除"), NOPAY(0, "未支付"), WAITTODELIVER(1, "已支付(待发货)"), DELIVERED(
				2, "已发货"), END(3, "已完成"), CANCEL(5, "已取消"), REMAINPAY(20,
				"尾款支付");

		private int status;
		private String message;

		private OrderStatus(int status, String message) {
			this.message = message;
			this.status = status;
		}

		public int getStatus() {
			return status;
		}

		public String getMessage() {
			return message;
		}
	}
    
	//支付方式
	public static enum PayMethod {
		WXAPPPAY(10, "微信app"), WXWEBPPAY(11, "微信网页"), ALIDIRECT(20, "支付宝快捷"), ALIWAP(
				21, "支付宝wap"), ALIWEB(22, "支付宝wab"), COUPONS(90, "优惠券");

		private int status;
		private String message;

		private PayMethod(int status, String message) {
			this.message = message;
			this.status = status;
		}

		public int getStatus() {
			return status;
		}

		public String getMessage() {
			return message;
		}
		
		public static String status2HTML(String value) {
            StringBuilder sb = new StringBuilder();
            PayMethod[] payMethod = PayMethod.values();
            sb.append(Htmls.generateOption(-1, "默认全部"));
            for (PayMethod m : payMethod) {
                if (value.equals(m.status+"")) {
                    sb.append(Htmls.generateSelectedOption(m.status,
                            m.message));
                } else {
                    sb.append(Htmls.generateOption(m.status, m.message));
                }
            }
            return sb.toString();
        }
	}

	//是否是撒娇支付
	public static enum LoveLyStatus {
		LOVELYNOT(1, "否"), LOVELYYES(2, "是");

		private int status;
		private String message;

		private LoveLyStatus(int status, String message) {
			this.message = message;
			this.status = status;
		}

		public int getStatus() {
			return status;
		}

		public String getMessage() {
			return message;
		}
		
		public static String status2HTML(String value) {
            StringBuilder sb = new StringBuilder();
            LoveLyStatus[] loveLyStatus = LoveLyStatus.values();
            sb.append(Htmls.generateOption(-1, "默认全部"));
            for (LoveLyStatus s : loveLyStatus) {
                if (value.equals(s.status+"")) {
                    sb.append(Htmls.generateSelectedOption(s.status,
                            s.message));
                } else {
                    sb.append(Htmls.generateOption(s.status, s.message));
                }
            }
            return sb.toString();
        }
	}
	
	
	// 包裹状态
	public static enum ParcelStatus {
		WAITINGTODELIVER(1, "待发货"), DELIVERD(2, "已发货"), END(3, "已完成"), WAITINGTOPAY(
				4, "待支付"), CANCELED(5, "已取消"), DELETED(-99, "已删除");

		private int status;
		private String message;

		private ParcelStatus(int status, String message) {
			this.message = message;
			this.status = status;
		}

		public int getStatus() {
			return status;
		}

		public String getMessage() {
			return message;
		}

		public static String status2HTML(String value) {
			StringBuilder sb = new StringBuilder();
			ParcelStatus[] parcelStatus = ParcelStatus.values();
			sb.append(Htmls.generateOption(-1, "默认全部"));
			for (ParcelStatus s : parcelStatus) {
				if (value.equals(s.status + "")) {
					sb.append(Htmls.generateSelectedOption(s.status, s.message));
				} else {
					sb.append(Htmls.generateOption(s.status, s.message));
				}
			}
			return sb.toString();
		}
	}
	
	//访问平台
	public static enum Devices {
		Android("Android"), IOS("IPhone");

		private String message;

		private Devices( String message) {
			this.message = message;
		}

		public String getMessage() {
			return message;
		}
		
		public static String devices2HTML(Integer value) {
            StringBuilder sb = new StringBuilder();
            Devices[] devices = Devices.values();
            sb.append(Htmls.generateOption(-1, "默认全部"));
            for (Devices s : devices) {
                if (value.equals(s.ordinal())) {
                    sb.append(Htmls.generateSelectedOption(s.ordinal(),
                            s.message));
                } else {
                    sb.append(Htmls.generateOption(s.ordinal(), s.message));
                }
            }
            return sb.toString();
        }
	}
	//用户组概念，目前只设定新人，老人
	public static final Map<String,String> SystemGroup(){
		Map<String,String> ms=new HashMap<String,String>();
		ms.put("allman", "0");//全部人
		ms.put("newman", "1");//新人
		ms.put("oldman", "2");//老人
		return ms;
	}
	
	//根据ＫＥＹ获取　值
public static String getSystemGroupOne(String key) {
		String kv="0";
		switch(key){
		case "allman":
			kv = "0";
			break;
		case "newman":
			kv = "1";
			break;
		case "oldman":
			kv = "2";
			break;
		default:
			kv = "0";
			break;
		}
	
		return kv;
	}

}
