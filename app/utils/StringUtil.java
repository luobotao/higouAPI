package utils;

import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.nio.charset.Charset;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.ResultSet;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.Formatter;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESKeySpec;
import javax.crypto.spec.IvParameterSpec;

import models.WxSign;
import net.logstash.logback.encoder.org.apache.commons.lang.StringUtils;
import net.sf.json.JSONObject;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.RandomStringUtils;

import com.fasterxml.jackson.databind.node.ObjectNode;

import assets.CdnAssets;
import play.Configuration;
import play.Logger;
import play.data.validation.ValidationError;
import play.libs.Json;
import play.mvc.Http.Request;
import services.ICacheService;
import services.ServiceFactory;
import utils.wxpay.MD5Util;
import utils.wxpay.Sha1Util;
import utils.wxpay.TenpayHttpClient;

/**
 * String加密与解密
 * 
 * @author luobotao Date: 2015年4月14日 下午5:44:40
 */
public class StringUtil {
	private static ICacheService cache = ServiceFactory.getCacheService();
	private static final Logger.ALogger LOGGER = Logger.of(StringUtil.class);
	private static final SimpleDateFormat CHINESE_DATE_TIME_FORMAT = new SimpleDateFormat(
			"yyyy年MM月dd日HHmm");
	private static final SimpleDateFormat CHINESE_DATE_WithOutYear_FORMAT = new SimpleDateFormat(
			"MM月dd日HH:mm");
	private static final SimpleDateFormat CHINESE_TIME_FORMAT = new SimpleDateFormat(
			"HH:mm");
	private static final byte[] KEY = "a9b8c7d6".getBytes();
	private static final DESKeySpec DKS;
	private static final SecretKeyFactory KEY_FACTORY;
	private static final Key SECRET_KEY;
	private static final IvParameterSpec IV = new IvParameterSpec(KEY);
	private static final String CIPHER_INSTANCE_NAME = "DES/CBC/PKCS5Padding";
	private static final Charset UTF_8;
	private final static String[] hexDigits = {
		"0", "1", "2", "3", "4", "5", "6", "7",
		"8", "9", "a", "b", "c", "d", "e", "f"};
	static {
		try {
			UTF_8 = Charset.forName("UTF-8");
			DKS = new DESKeySpec(KEY);
			KEY_FACTORY = SecretKeyFactory.getInstance("DES");
			SECRET_KEY = KEY_FACTORY.generateSecret(DKS);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public static String encode(String s) {
		try {
			Cipher cipherEncode;
			cipherEncode = Cipher.getInstance(CIPHER_INSTANCE_NAME);
			cipherEncode.init(Cipher.ENCRYPT_MODE, SECRET_KEY, IV);
			return Base64.encodeBase64String(cipherEncode.doFinal(s
					.getBytes(UTF_8)));
		} catch (IllegalBlockSizeException | BadPaddingException
				| InvalidKeyException | NoSuchAlgorithmException
				| NoSuchPaddingException | InvalidAlgorithmParameterException e) {
			LOGGER.error("salary codec encode " + s + " error.", e);
			return null;
		}
	}

	public static String decode(String str) {
		try {
			Cipher cipherDecode;
			cipherDecode = Cipher.getInstance(CIPHER_INSTANCE_NAME);
			cipherDecode.init(Cipher.DECRYPT_MODE, SECRET_KEY, IV);
			return new String(cipherDecode.doFinal(Base64.decodeBase64(str)),
					UTF_8);
		} catch (IllegalBlockSizeException | BadPaddingException
				| InvalidKeyException | NoSuchAlgorithmException
				| NoSuchPaddingException | InvalidAlgorithmParameterException e) {
			LOGGER.error("salary codec decode " + str + " error.", e);
			return null;
		}
	}

	public static String salt(String str) {
		byte[] payload = str.getBytes();
		byte f = (byte) (sum(payload, 0, payload.length / 2) % 26 + 97);
		byte l = (byte) (sum(payload, payload.length / 2, payload.length) % 26 + 97);
		byte[] bytes = new byte[payload.length + 2];
		bytes[0] = f;
		System.arraycopy(payload, 0, bytes, 1, payload.length);
		bytes[bytes.length - 1] = l;
		return new String(bytes);
	}

	private static int sum(byte[] payload, int start, int end) {
		int sum = 0;
		for (; start < end; start++) {
			sum += payload[start];
		}
		return sum;
	}

	/**
	 * 生成随机数
	 * @return
	 */
	public static String genRandomCode(int Length) {
		String key = "";
		for(int i=0; i<Length; i++) {
			key += Math.round(Math.random()*9);    //生成验证码随机数
		}
		return key;
		
		/*long Temp = Math.round(Math.random() * 8999 + 1000);
		return Temp+"";*/
	}
	
	
	
	/**
	 * 
	 * @param text
	 *            目标字符串
	 * @param length
	 *            截取长度
	 * @param encode
	 *            采用的编码方式
	 * @return
	 * @throws UnsupportedEncodingException
	 */
	public static String substring(String text, int length, String encode) {
		if (text == null) {
			return null;
		}
		StringBuilder sb = new StringBuilder();
		int currentLength = 0;
		for (char c : text.toCharArray()) {
			try {
				currentLength += String.valueOf(c).getBytes(encode).length;
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
			if (currentLength <= length) {
				sb.append(c);
			} else {
				break;
			}
		}
		return sb.toString();
	}

	 public static String getIpAddr(Request request) {  
	        String ip = request.getHeader("x-forwarded-for");  
	        if(ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {  
	            ip = request.getHeader("Proxy-Client-IP");  
	        }  
	        if(ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {  
	            ip = request.getHeader("WL-Proxy-Client-IP");  
	        }  
	        if(ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {  
	            ip = request.remoteAddress();  
	        }  
	        return ip;
	    }
	 
	/**
	 * 获取PIC域名
	 * @return
	 */
	public static String getPICDomain(){
		boolean IsProduct = Configuration.root().getBoolean("production", false);
		String domains = Configuration.root().getString("domain_dev","http://apitestpic.higegou.com");
		if(IsProduct){
			domains = Configuration.root().getString("domain_product","http://apipic.higegou.com");
		}
		return domains;
	}
	/**
	 * 获取列表图或者规格图
	 * @param pic
	 * @return
	 */
	public static String getListpic(String pic) {
		if(StringUtils.isBlank(pic)){
			return "";
		}
		String kjdImagePre = Configuration.root().getString("kjt.imagePre","http://image.kjt.com.pre");
		if(pic.startsWith(kjdImagePre)){
			return pic;
		}
		String domain = getPICDomain();//"http://apitestpic.higegou.com";
		if(pic.indexOf("/pimgs/")>=0){
			return domain+pic+"@800w";
		}
		if(pic.indexOf("-")>0){
			String listPicArray[] = pic.split("-");
			if (listPicArray.length > 2) {
				String pathTemp = listPicArray[1];
				pic = domain + "/pimgs/p1/" + pathTemp.substring(0, 2) + "/"
						+ pathTemp.substring(2, 4) + "/" + pathTemp.substring(4, 6)
						+ "/" + pathTemp.substring(6, 8) + "/" + pic;
			}
		}else{
			String listPicArray[] = pic.split("_");
			if (listPicArray.length > 1) {
				String pathTemp = listPicArray[0];
				if(pathTemp.length()>=8){
					pic = domain + "/pimgs/p1/" + pathTemp.substring(0, 2) + "/"
						+ pathTemp.substring(2, 4) + "/" + pathTemp.substring(4, 6)
						+ "/" + pathTemp.substring(6, 8) + "/" + pic;
				}
			}
		}
		if(pic.indexOf(domain)<0){
			pic = domain+pic;
		}
		return pic+"@800w";
	}
	
	/**
	 * 获取列表图或者规格图(不根据分辨率改变宽度)
	 * @param pic
	 * @return
	 */
	public static String getListpicOld(String pic) {
		if(StringUtils.isBlank(pic)){
			return "";
		}
		String kjdImagePre = Configuration.root().getString("kjt.imagePre","http://image.kjt.com.pre");
		if(pic.startsWith(kjdImagePre)){
			return pic;
		}
		String domain =getPICDomain();
		pic=pic.replaceAll(domain, "");
		if(pic.indexOf("/pimgs/adload/")>=0 || pic.indexOf("/pimgs")>=0 ||pic.indexOf("/upload")>=0){
			return StringUtil.getPICDomain()+pic;
		}
		if(pic.indexOf("-")>0){
			String listPicArray[] = pic.split("-");
			if (listPicArray.length > 2) {
				String pathTemp = listPicArray[1];
				pic = domain + "/pimgs/p1/" + pathTemp.substring(0, 2) + "/"
						+ pathTemp.substring(2, 4) + "/" + pathTemp.substring(4, 6)
						+ "/" + pathTemp.substring(6, 8) + "/" + pic;
			}
		}else{
			String listPicArray[] = pic.split("_");
			if (listPicArray.length > 1) {
				String pathTemp = listPicArray[0];
				if(pathTemp.length()>=8){
					pic = domain + "/pimgs/p1/" + pathTemp.substring(0, 2) + "/"
						+ pathTemp.substring(2, 4) + "/" + pathTemp.substring(4, 6)
						+ "/" + pathTemp.substring(6, 8) + "/" + pic;
				}
			}
		}
		if(pic.indexOf(domain)<0){
			pic = domain+pic;
		}
		return pic;
	}
	
	/**
	 * 根据分辨率真来获取商品图片
	 * 分辨率除以2 双图
	 * @param pcode
	 * @param listpic
	 * @param resolution
	 * @return
	 */
	public static String getProductListpic(String pcode,String listpic,String resolution) {
		if(StringUtils.isBlank(listpic)){
			return "";
		}
		String kjdImagePre = Configuration.root().getString("kjd.ImagePre","http://image.kjt.com.pre");
		if(listpic.startsWith(kjdImagePre)){
			return listpic;
		}
		if (StringUtils.isBlank(resolution))
		{
			resolution="1080_640";
		}
		String resolutionArray[] =resolution.split("_");
		int width = Numbers.parseInt(resolutionArray[0], 0);
		width = width/2;
		if(width==0 || width>800){
			width=800;
		}
		String fixname = listpic.substring(listpic.lastIndexOf(".")+1,listpic.lastIndexOf(".")+4);
		if (StringUtils.isBlank(pcode))
		{
			if ("gif".equals(fixname))
			{
				return listpic;
			}
			return listpic+"@"+width+"w";
		}
		if(listpic.indexOf("pimgs")>=0||listpic.indexOf("upload")>=0){
			return listpic+"@"+width+"w";
		}
		
		String pathTemp = pcode;
		listpic = "/pimgs/p1/" + pathTemp.substring(0, 2) + "/"
				+ pathTemp.substring(2, 4) + "/" + pathTemp.substring(4, 6)
				+ "/" + pathTemp.substring(6, 8) + "/" + listpic+"@"+width+"w";
		return listpic;
	}
	
	/**
	 * 根据分辨率来获取商品图片(结合OSS已经做过处理)
	 * pcode为空 或 mould id 为3或4
	 * @param pcode
	 * @param listpic
	 * @param resolution
	 * @param divide 
	 * @return
	 */
	public static String getWebListpic(String pcode,String listpic,String resolution, BigDecimal divide) {
		if(StringUtils.isBlank(listpic)){
			return "";
		}
		String kjdImagePre = Configuration.root().getString("kjd.ImagePre","http://image.kjt.com.pre");
		if(listpic.startsWith(kjdImagePre)){
			return listpic;
		}
		if (StringUtils.isBlank(resolution))
		{
			resolution="1080_640";
		}
		String resolutionArray[] =resolution.split("_");
		int width = Numbers.parseInt(resolutionArray[0], 0);
		if(divide!=null ){
			width = new BigDecimal(width).divide(divide,0,BigDecimal.ROUND_CEILING).intValue();
		}
		if(width<=0 || width>800){
			width=800;
		}
		String fixname = listpic.substring(listpic.lastIndexOf(".")+1,listpic.lastIndexOf(".")+4);
		if (StringUtils.isBlank(pcode))
		{
			if ("gif".equals(fixname))
			{
				return listpic;
			}
			return listpic+"@"+width+"w";
		}
		if(listpic.indexOf("pimgs")>=0||listpic.indexOf("upload")>=0){
			return listpic+"@"+width+"w";
		}
		String pathTemp = pcode;
		listpic = "/pimgs/p1/" + pathTemp.substring(0, 2) + "/"
				+ pathTemp.substring(2, 4) + "/" + pathTemp.substring(4, 6)
				+ "/" + pathTemp.substring(6, 8) + "/" +listpic+"@"+width+"w";
		return listpic;
	}
	
	/**
	 * 根据设备与分辨率不同来获取相应的徽章图片
	 * @param deviceType
	 * @param resolution
	 * @return
	 */
	public static String getCoverPic(String deviceType,String resolution) {
		String result="";
		if (StringUtils.isBlank(resolution))
		{
			resolution="1080_640";
		}
		String resolutionArray[] =resolution.split("_");
		int width = Numbers.parseInt(resolutionArray[0], 0);
		switch (width){
			case 480:
				width = 540;
				break;
			case 720:
				width = 640;
				break;
			case 640:
				width = 640;
				break;
			case 750:
				width = 800;
				break;
			case 1:
				width = 800;
				break;
			case 2:
				width = 540;
				break;
			default:
				width = 800;
				break;
		}
		if("0".equals(deviceType)){//ios
			if(width==640){
				result = CdnAssets.CDN_API_PUBLIC_URL+"images/sheSaidImages/cover_superstar@2x.png";
			}else{
				result = CdnAssets.CDN_API_PUBLIC_URL+"images/sheSaidImages/cover_superstar@3x.png";
			}
		}else{
			if(width>720){
				result = CdnAssets.CDN_API_PUBLIC_URL+"images/sheSaidImages/cover_superstar_xxhdpi.png";
			}else{
				result = CdnAssets.CDN_API_PUBLIC_URL+"images/sheSaidImages/cover_superstar_xhdpi.png";
			}
		}
		return result;
	}
	/**
	 * 图片上传至OSS服务器，此方法返回OSS服务器的URL
	 * @return
	 */
	public static String getOSSUrl(){
		String BUCKET_NAME=Configuration.root().getString("oss.bucket.name.higouAPIDev", "higou-api");
		boolean IsProduct = Configuration.root().getBoolean("production", false);
		if(IsProduct){
			BUCKET_NAME=Configuration.root().getString("oss.bucket.name.higouAPIProduct", "higou-api");
		}
		return OSSUtils.PROTOCOL + "://" + BUCKET_NAME + "." + OSSUtils.OSS_ENDPOINT;
	}
	/*
	 * 获取域名
	 */
	public static String getDomainH5(){
		boolean IsProduct = Configuration.root().getBoolean("production", false);
		//String domains = "http://ht2.neolix.cn:9004";//Configuration.root().getString("domain.dev","http://ht2.neolix.cn:9004");
		String domains="http://ht2.neolix.cn:9004";
		if(IsProduct){
			domains = Configuration.root().getString("domain.productH5","http://api.higegou.com");
		}
		return domains;
	}
	
	/*
	 * 获取域名，app及其它使用
	 */
	public static String getDomainAPI(){
		boolean IsProduct = Configuration.root().getBoolean("production", false);
		String domain = Configuration.root().getString("domain.dev","http://ht2.neolix.cn");
		if(IsProduct){
			domain = Configuration.root().getString("domain.product","http://ht.neolix.cn");
		}
		return domain;
	}
	
	public static String getMD5(String str){
		try {  
            MessageDigest md = MessageDigest.getInstance("MD5");  
            String result = MD5(str,md);  
            return result;
        } catch (NoSuchAlgorithmException e) {  
            // TODO Auto-generated catch block  
            e.printStackTrace();  
            return "";
        }  
	}
	
	public static String MD5(String strSrc,MessageDigest  md) {  
        byte[] bt = strSrc.getBytes();  
        md.update(bt);  
        String strDes = bytes2Hex(md.digest()); // to HexString  
        return strDes;  
    }  
    
	public static String bytes2Hex(byte[] bts) {  
        StringBuffer des = new StringBuffer();  
        String tmp = null;  
        for (int i = 0; i < bts.length; i++) {  
            tmp = (Integer.toHexString(bts[i] & 0xFF));  
            if (tmp.length() == 1) {  
                des.append("0");  
            }  
            des.append(tmp);  
        }  
        return des.toString();  
    }
    
    public static String MD5Encode(String origin) {
    	String resultString = null;

    	try {
    	resultString = new String(origin);
    	MessageDigest md = MessageDigest.getInstance("MD5");
    	resultString =
    	byteArrayToString(md.digest(resultString.getBytes()));
    	}
    	catch (Exception ex) {

    	}
    	return resultString;
    	}
    
    /**
    * 转换字节数组为16进制字串
    * @param b 字节数组
    * @return 16进制字串
    */
    public static String byteArrayToString(byte[] b) {
    StringBuffer resultSb = new StringBuffer();
    for (int i = 0; i < b.length; i++) {
    //resultSb.append(byteToHexString(b[i]));//若使用本函数转换则可得到加密结果的16进制表示，即数字字母混合的形式
    resultSb.append(byteToNumString(b[i]));//使用本函数则返回加密结果的10进制数字字串，即全数字形式
    }
    return resultSb.toString();
    }
    
    private static String byteToNumString(byte b) {

    	int _b = b;
    	if (_b < 0) {
    	_b = 256 + _b;
    	}

    	return String.valueOf(_b);
    	}
    
    private static String byteToHexString(byte b) {
    	int n = b;
    	if (n < 0) {
    	n = 256 + n;
    	}
    	int d1 = n / 16;
    	int d2 = n % 16;
    	return hexDigits[d1] + hexDigits[d2];
    }
    
    //计算时间1分钟内，x秒前/1⼩小时内，x分钟前/24⼩小时内，x⼩小时前/48⼩小时内，昨天 hh:mm/超过48⼩小时，M⽉月d⽇日HH:MM
    public static String getfomatdate(Date date1,Date date2){
    	if(date1==null)
        	date1=new Date();
    	if(date2==null)
    		date2=new Date();
    	
    	long temp = date2.getTime() - date1.getTime();    //相差毫秒数
        long hours = temp / 1000 / 3600;                //相差小时数
        long temp2 = temp % (1000 * 3600);
        double mins =  ((double)temp2 / 1000 / 60);                    //相差分钟数
        long ss=temp/1000;
        
        String out="";
        
        
        if(hours>48)
        	out=CHINESE_DATE_WithOutYear_FORMAT.format(date1);
        else{
        	if(hours>24)
        		out="昨天"+CHINESE_TIME_FORMAT.format(date1);
        	else{
        		if(hours>1)
        			out=String.valueOf(hours).toString()+"小时前";
        		else
        		{
        			if(mins>1)
        				out=(int)mins+"分钟前";
        			else
        				out=String.valueOf(ss)+"秒前";
        		}
        	}
        }
    	return out;
    }
    
    //格式化数量
    public static String formatnum(Long nums){
    	if(nums>10000){
    		if(nums<=100000)
    			return (nums/10000)+"万";
    		else
    		{
    			if(nums>=10000000)
    				return (nums/10000000)+"百万";
    			else
    				return (nums/1000000)+"万";
    		}
    	}
    	return nums.toString();
    }

    
    public static String filterString(String str){
    	String regEx="[`~!@#$%^&*()+=|{}':;',\\[\\].<>/?~！@#￥%……&*（）——+|{}【】‘；：”“’。，、？]";  
    	Pattern p = Pattern.compile(regEx);
    	Matcher m = p.matcher(str);
    	return m.replaceAll("").trim();
    }
    
    //取随机代言APP使用图章
    public static String getSheSaidIcon(){
		ArrayList<String> strtemp= new ArrayList<String>();
		String domainimg="";//CdnAssets.CDN_API_PUBLIC_URL;
		strtemp.add(domainimg+"images/appIcon/1.png");
		strtemp.add(domainimg+"images/appIcon/2.png");
		strtemp.add(domainimg+"images/appIcon/3.png");
		strtemp.add(domainimg+"images/appIcon/4.png");
		strtemp.add(domainimg+"images/appIcon/5.png");
		strtemp.add(domainimg+"images/appIcon/6.png");
		strtemp.add(domainimg+"images/appIcon/7.png");
		strtemp.add(domainimg+"images/appIcon/8.png");
		strtemp.add(domainimg+"images/appIcon/9.png");
		strtemp.add(domainimg+"images/appIcon/10.png");
		strtemp.add(domainimg+"images/appIcon/11.png");
		
		Integer num = (int) Math.ceil(Math.random()*11);
		return strtemp.get(num-1);
    }
    
    
    //取随机频道商品使用图章
    public static String getProductIcon(int pid,String mould){
    	return "";
		/*ArrayList<String> strtemp= new ArrayList<String>();
		String domainimg=CdnAssets.CDN_API_PUBLIC_URL;
		if ("1".equals(mould))
		{
			strtemp.add(domainimg+"images/bannerImg/11.png");
			strtemp.add(domainimg+"images/bannerImg/12.png");
			strtemp.add(domainimg+"images/bannerImg/13.png");
			strtemp.add(domainimg+"images/bannerImg/14.png");
			strtemp.add(domainimg+"images/bannerImg/15.png");
			strtemp.add(domainimg+"images/bannerImg/16.png");
			strtemp.add(domainimg+"images/bannerImg/17.png");
			strtemp.add(domainimg+"images/bannerImg/18.png");
			strtemp.add(domainimg+"images/bannerImg/19.png");
			strtemp.add(domainimg+"images/bannerImg/10.png");
		}else if ("2".equals(mould)){
			strtemp.add(domainimg+"images/bannerImg/21.png");
			strtemp.add(domainimg+"images/bannerImg/22.png");
			strtemp.add(domainimg+"images/bannerImg/23.png");
			strtemp.add(domainimg+"images/bannerImg/24.png");
			strtemp.add(domainimg+"images/bannerImg/25.png");
			strtemp.add(domainimg+"images/bannerImg/26.png");
			strtemp.add(domainimg+"images/bannerImg/27.png");
			strtemp.add(domainimg+"images/bannerImg/28.png");
			strtemp.add(domainimg+"images/bannerImg/29.png");
			strtemp.add(domainimg+"images/bannerImg/20.png");
		}else{
			strtemp.add(domainimg+"images/bannerImg/11.png");
			strtemp.add(domainimg+"images/bannerImg/12.png");
			strtemp.add(domainimg+"images/bannerImg/13.png");
			strtemp.add(domainimg+"images/bannerImg/14.png");
			strtemp.add(domainimg+"images/bannerImg/15.png");
			strtemp.add(domainimg+"images/bannerImg/16.png");
			strtemp.add(domainimg+"images/bannerImg/17.png");
			strtemp.add(domainimg+"images/bannerImg/18.png");
			strtemp.add(domainimg+"images/bannerImg/19.png");
			strtemp.add(domainimg+"images/bannerImg/10.png");
		}
		if (strtemp!= null && strtemp.size()>0)
		{
			Integer num = (int)pid%10;
			return strtemp.get(num)+"?1=1";
		}else{
			return "";
		}*/
    }
    
    public static boolean checkMd5(String deviceId,String md5str,String appversion) {
    	if (StringUtils.isBlank(appversion))
    	{
    		return true;
    	}
    	String appTemp = appversion.replace(".", "");
		if(Numbers.parseInt(appTemp, 0)<222){
			return true;
		}
    	try{
			/*获取deviceid 0、8、2、9 字符串*/
	    	String secretStr = deviceId.substring(0, 1)+deviceId.substring(8, 9)+deviceId.substring(2, 3)+deviceId.substring(9, 10);
	    	String str = getMD5(deviceId+secretStr);
	    	if (str.equals(md5str)){
	    		return true;	
	    	}else{
	    		return false;
	    	}
    	}catch(Exception ex){
    		return false;
    	}
    	
	}

    public static boolean checksign(String lwdjl,String md5sign,String appversion) {
    	if (StringUtils.isBlank(appversion))
    	{
    		return true;
    	}
    	String appTemp = appversion.replace(".", "");
		if(Numbers.parseInt(appTemp, 0)<222){
			return true;
		}
    	try{
    		if (lwdjl.equals(md5sign)){
    			return true;
    		}else{
    			return false;
    		}
    	}catch(Exception ex){
    		return false;
    	}
    	
	}
    
    /**
     * 验证是否电话
     * @param phone
     * @return
     */
    public static boolean checkPhone(String phone){
    	java.util.regex.Pattern phone_PATTERN = java.util.regex.Pattern.compile("^\\s*\\d{11}\\s*$");
    	if(!phone_PATTERN.matcher(phone).matches()){
			return false;
		}else{
			return true;
		}
    }
    //取代言APPTag图片
    public static String getSheSaidTagImg(){
		String domainimg=CdnAssets.CDN_API_PUBLIC_URL;
		return domainimg+"images/daiyan_mark.png";
    }
    
    //随机取代言标题
    public static String getShesaidTitle(String uname){
		List<String> strtemp= new ArrayList<String>();
		int i=0;
		strtemp = (List<String>)cache.getObject("endorse_share_title"+uname);
		if(strtemp==null || strtemp.isEmpty()){
			strtemp=new ArrayList<String>();
			
			String sql="SELECT title from endorsement_share_title";
			JdbcOper db = JdbcOper.getInstance();// 创建DBHelper对象
			try {
				db.getPrepareStateDao(sql);
				ResultSet rs = db.pst.executeQuery();// 执行语句，得到结果集
				while(rs.next()){
					//result = rs.getString("filename");
					if(!StringUtils.isBlank(rs.getString("title")) && rs.getString("title").indexOf("||||")>=0)
						strtemp.add(rs.getString("title").replace("||||", uname));
					else{
						if(!StringUtils.isBlank(rs.getString("title")))
							strtemp.add(rs.getString("title"));
					}
					i++;
				}
				cache.setObject("endorse_share_title"+uname, strtemp, 7200);
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				db.close();
			}
		}

		if((strtemp==null || strtemp.isEmpty()) && i==0){
			strtemp.add("多亏我有好容颜，你买东西能省钱");
			i++;
		}
		Random rs=new Random();
		Integer num = rs.nextInt(strtemp.size());
		return strtemp.get(num);
	}
    //取代言分享内容
    public static String getSheSaidremark(){
    	return "嗨个购—与你一起买世界";
    }
    
    //获取随机邀请分享标题及内容,list.get(0)-标题；get(1)内容
    public static String[] getSharecontent(){
    	List<String[]> strtemp= new ArrayList<String[]>();
		strtemp = (List<String[]>)cache.getObject("invite_share");
		if(strtemp==null || strtemp.isEmpty()){
			strtemp=new ArrayList<String[]>();
			
			String sql="SELECT * from invite_share_title";
			JdbcOper db = JdbcOper.getInstance();// 创建DBHelper对象
			try {
				db.getPrepareStateDao(sql);
				ResultSet rs = db.pst.executeQuery();// 执行语句，得到结果集
				while(rs.next()){
					String[] tempstr= new String[2];
					tempstr[0]=rs.getString("title")==null?"":rs.getString("title");
					tempstr[1]=rs.getString("remark")==null?"":rs.getString("remark");
					strtemp.add(tempstr);
					}
				cache.setObject("invite_share", strtemp, 7200);
			} catch (Exception e) {
				//e.printStackTrace();
			} finally {
				db.close();
			}
		}

		if((strtemp==null || strtemp.isEmpty())){
			return null;
		}
		else{
			Random rs=new Random();
			Integer num = rs.nextInt(strtemp.size());
			return strtemp.get(num);
		}
    }
    
    //根据参数名称获取系统参数值
    public static String getSystemConfigValue(String paramnam){
    	//cache.clear("SystemConfig_higou");
    	Map<String,String> systemmap=(Map)cache.getObject("SystemConfig_higou");
    	Integer i=0;
		if(systemmap==null || systemmap.isEmpty()){
			systemmap=new HashMap<String,String>();
			
			String sql="SELECT * from systemconfig";
			JdbcOper db = JdbcOper.getInstance();// 创建DBHelper对象
			try {
				db.getPrepareStateDao(sql);
				ResultSet rs = db.pst.executeQuery();// 执行语句，得到结果集
				while(rs.next()){
					if(!systemmap.containsKey(rs.getString("data_key")))
							systemmap.put(rs.getString("data_key"),rs.getString("data_value"));
					}
				cache.setObject("SystemConfig_higou", systemmap, 7200);
			} catch (Exception e) {
				//e.printStackTrace();
			} finally {
				db.close();
			}
			i++;
		}

		String rvalue="";
		if((systemmap==null || systemmap.isEmpty())){
			return null;
		}
		else{
				if(systemmap.containsKey(paramnam))
				{
					rvalue=systemmap.get(paramnam);
				}
			}
		return rvalue;
    }
    
  //根据参数名称获取系统参数值
    public static String getSystemConfigValuenocache(String paramnam){
    	//cache.clear("SystemConfig_higou");
    	Map<String,String> systemmap=null;
    	Integer i=0;
		if(systemmap==null || systemmap.isEmpty()){
			systemmap=new HashMap<String,String>();
			
			String sql="SELECT * from systemconfig";
			JdbcOper db = JdbcOper.getInstance();// 创建DBHelper对象
			try {
				db.getPrepareStateDao(sql);
				ResultSet rs = db.pst.executeQuery();// 执行语句，得到结果集
				while(rs.next()){
					if(!systemmap.containsKey(rs.getString("data_key")))
							systemmap.put(rs.getString("data_key"),rs.getString("data_value"));
					}
			} catch (Exception e) {
				//e.printStackTrace();
			} finally {
				db.close();
			}
			i++;
		}

		String rvalue="";
		if((systemmap==null || systemmap.isEmpty())){
			return null;
		}
		else{
				if(systemmap.containsKey(paramnam))
				{
					rvalue=systemmap.get(paramnam);
				}
			}
		return rvalue;
    }
    
    //根据参数名称获取系统参数值
    public static List<String> getSystemConfignocache(String paramnam){
    	//cache.clear("SystemConfig_higou");
    	List<String> slist=new ArrayList<String>();			
			String sql="SELECT * from systemconfig where data_key like '"+paramnam+"%'";
			JdbcOper db = JdbcOper.getInstance();// 创建DBHelper对象
			try {
				db.getPrepareStateDao(sql);
				ResultSet rs = db.pst.executeQuery();// 执行语句，得到结果集
				while(rs.next()){
					slist.add(rs.getString("data_key")+"_"+rs.getString("data_value"));
				}
			} catch (Exception e) {
				//e.printStackTrace();
			} finally {
				db.close();
			}
			return slist;
    }
  	
  //组装分享
  	public static String getShareSign(Map<String,String> map){
		String str = makeSig(map);

		try {
			MessageDigest crypt = MessageDigest.getInstance("SHA-1");
			crypt.reset();
			crypt.update(str.getBytes("UTF-8"));
			return byteToHex(crypt.digest());
		} catch (Exception e) {
			str = "";
		}
		return str;
  	}
  	// 组装签名字符串
  		public static String makeSig(Map<String, String> sortMap) {
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
  	
  		public static String byteToHex(final byte[] hash) {
  	        Formatter formatter = new Formatter();
  	        for (byte b : hash)
  	        {
  	            formatter.format("%02x", b);
  	        }
  	        String result = formatter.toString();
  	        formatter.close();
  	        return result;
  	    }
  		
  		// 组装微信支付签名
  		public static String getSign(Map<String, String> map) {

  			String str = makeSig(map);
  			str = str + "&key=" + Constants.WXappsecretpay;
  			try{
  			MessageDigest md5 = MessageDigest.getInstance("MD5");
  			str=MD5Util.MD5Encode(str, "UTF-8");
  			}
  			catch(Exception e){
  				str="";
  			}
  			return str.toUpperCase();
  		}
  		
  		/*生成微信地址签名*/
  		public static String getWXaddressSign(String token,String url,String timstr,String nostr){
  			Map<String, String> pramt = new HashMap<String, String>();
			pramt.put("accesstoken",token);
			pramt.put("appid",Constants.WXappID);
			pramt.put("url", url);
			pramt.put("timestamp", timstr);
			pramt.put("noncestr",nostr);
			return getShareSign(pramt);
  		}
  		 public static void main(String[] args) {
  			
  			String t = getListpic("10001298_14262921.jpg");
  			System.out.println(t);
  		}
  		 
  		 /*
  		  * 产生比例随机数,比例最大的放在最前面List<String> String 为 “key_couponId_限制数量_出现比例”
  		  */
  		 public static String getRandomNum(List<String> numsmap,Integer numscale){
  			 Integer rn=0;
  			 if(numsmap==null || numsmap.size()==0)
  				 return "";
  			 
  			Random rm=new Random();
  			Integer rdm=Math.abs(rm.nextInt(numscale));

  			Integer tmpn=Integer.valueOf(numsmap.get(0).substring(numsmap.get(0).lastIndexOf("_")));
  			Integer etmpn=Integer.valueOf(numsmap.get(0).substring(numsmap.get(0).lastIndexOf("_")));
  			if(rdm>=1 && rdm<=Integer.valueOf(numsmap.get(0).substring(numsmap.get(0).lastIndexOf("_"))))
  				return numsmap.get(0);
  			else{
  				for (int n = 1; n < numsmap.size() - 1; n++) {
  					etmpn=tmpn+Numbers.parseInt(numsmap.get(n).substring(numsmap.get(n).lastIndexOf("_")), 1);
  					if(rdm>=tmpn && rdm<=etmpn)
  						return numsmap.get(n);
  					tmpn=etmpn;
  				}
  			}
			
  			 return numsmap.get(0);
  		 }
}
