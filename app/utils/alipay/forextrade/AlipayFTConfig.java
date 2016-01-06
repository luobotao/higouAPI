package utils.alipay.forextrade;

/* *
 *类名：AlipayConfig
 *功能：基础配置类
 *详细：设置帐户有关信息及返回路径
 *版本：3.3
 *日期：2012-08-10
 *说明：
 *以下代码只是为了方便商户测试而提供的样例代码，商户可以根据自己网站的需要，按照技术文档编写,并非一定要使用该代码。
 *该代码仅供学习和研究支付宝接口使用，只是提供一个参考。
	
 *提示：如何获取安全校验码和合作身份者ID
 *1.用您的签约支付宝账号登录支付宝网站(www.alipay.com)
 *2.点击“商家服务”(https://b.alipay.com/order/myOrder.htm)
 *3.点击“查询合作者身份(PID)”、“查询安全校验码(Key)”

 *安全校验码查看时，输入支付密码后，页面呈灰色的现象，怎么办？
 *解决方法：
 *1、检查浏览器配置，不让浏览器做弹框屏蔽设置
 *2、更换浏览器或电脑，重新登录查询。
 */

public class AlipayFTConfig {
	
	//↓↓↓↓↓↓↓↓↓↓请在这里配置您的基本信息↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓
	// 合作身份者ID，以2088开头由16位纯数字组成的字符串
	public static String partner = "2088021001445498";
	// 商户的私钥
	public static String private_key = "MIICdwIBADANBgkqhkiG9w0BAQEFAASCAmEwggJdAgEAAoGBAMtDzskqsXJq+3yIbTOH4PLjLRHg+KrbMgMxKUX18x1AcR1piptb0QKdWjnraDI+wFTTdlcH9mBlvDbO/LN2jG43mqhnroPHYMfESsfmgrnkScDH9qHuPe8843fuuCluiVEEiYIB9ECyAjpuoq1wx+edvOMLSdQ42+3oXsmlktMJAgMBAAECgYEAilQDDfP3/p2/t+hAKKl39NOVgG1RzTSOvI1gQTQ31thcImAI6+F4RAdBt9j0d/0sdo3SIzhP+xpBFCuY3aj3t3HUKBiMx+ckoHnyyu3dhQ/zAB8zT+aM/crizT+ACITiyYgxpxDwnFRMvaK3ofrZlxR61PwjB2tP+xJ4z6xKxAECQQD+m+SIT1lyOOV4ZYVjaERqFRTbIZ0V7+B6FM4YpL4oLNHi2Jt0GlU+GzoPnCtGOizGNji1Bd2486/k3fauwDf5AkEAzGAaW4Jtw/qcGBRv1KdGyr9wcnAAbQ4otMCoQwMSWM1Ij/iJ90illNIBVq8NGo2gfsyLzntpuHuyxbgwv3nXkQJAPIAkxPM+CPNWK87L7Hw5TY/m3c9V+YF/sOLCun5jJT7JPQNFpUggm1py6ISuj8iBHTSDMxjKwAk/8b2AwbOxWQJAMt8lEGwp2GN0IUZNA9jTdTorykB4yqyAk2V1PKDyGyqWCkgqR2RM2vtfG+2czGQ+c/GER1RO333i5PZRJAg3EQJBAPCyYXb1QyYcwPWzHV59oHH+rS/C2+zbPOUZdNcISPZwtFmLEVuQ7ES7tu4P5XtwhHw50l3/vgEuZn79SSL7O9U=";
	
	// 支付宝的公钥，无需修改该值
	public static String ali_public_key  = "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQCnxj/9qwVfgoUh/y2W89L6BkRAFljhNhgPdyPuBV64bfQNN1PjbCzkIM6qRdKBoLPXmKKMiFYnkd6rAoprih3/PrQEB/VsW8OoM8fxn67UDYuyBTqA23MML9q1+ilIZwBC2AQ2UBVOrFXfFl75p6/B5KsiNG9zpgmLCUYuLkxpLQIDAQAB";

	//↑↑↑↑↑↑↑↑↑↑请在这里配置您的基本信息↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑
	

	// 调试用，创建TXT日志文件夹路径
	public static String log_path = "D:\\";

	// 字符编码格式 目前支持 gbk 或 utf-8
	public static String input_charset = "utf-8";
	
	// 签名方式 不需修改
	public static String sign_type = "RSA";
}
