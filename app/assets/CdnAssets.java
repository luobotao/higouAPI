package assets;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;

import play.Configuration;
import play.Play;
import utils.OSSUtils;
import utils.StringUtil;
import controllers.AssetsBuilder;


public class CdnAssets extends AssetsBuilder {
	private static final SimpleDateFormat CHINESE_D_TIME_FORMAT = new SimpleDateFormat(
			"yyyy-MM-dd HH:mm:ss");
    
    private static String getCDN_BASE_URL(){
    	boolean IsProduct = Configuration.root().getBoolean("production", false);
		if(IsProduct){
			return Configuration.root().getString("cdn.url.product", "http://h5.higegou.com");
		}else{
			return Configuration.root().getString("cdn.url.dev", "http://182.92.227.140:9004");
		}
    }
    

    public static final String CDN_API_PUBLIC_URL;
    static {
    	CDN_API_PUBLIC_URL = getCDN_BASE_URL() + "/public/";
    }

    public static String urlForAPIPublic(String file) {
        StringBuilder sb = new StringBuilder(CDN_API_PUBLIC_URL);
        sb.append(file);
        return sb.toString();
    }
   
    //*h5域名商城地址
    public static String H5_SHOPPING_URL(String postmanid) {
    	String url="";
    	if(StringUtils.isBlank(postmanid))
    		url= StringUtil.getDomainH5()+"/H5/shoplist";
    	else
    		url=StringUtil.getDomainH5()+"/H5/prolist?uid="+postmanid;
    	return url;
	}

    /*
	 * HIGOU商城商户ID
	 */
	public static final String HIGOUSHOPID="430003";
	/*系统时间*/
	public static final String SYSTEMTIMESTR=RandomStringUtils.randomAlphanumeric(16);
	
	/*
	 * 格式化输出时间
	 */
	public static final String FORMATEHHMMSS(Date datetime){
		return CHINESE_D_TIME_FORMAT.format(datetime);
	}
}
