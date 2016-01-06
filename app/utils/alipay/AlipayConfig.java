package utils.alipay;

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

public class AlipayConfig {
	
//	public static String partner = "2088711832091925";
//	public static String seller_email = "jiejinghua@neolix.cn";
//	public static String key = "dmskx0qz7l3pvlasrgq98if1vwj4a4n8";
	public static String partner = "2088811656917752";
	public static String seller_email = "yangtao@neolix.cn";
	public static String key = "7gtskdc2ei7zky532m7dwu5fw2v16xp3";
	
    // 商户的私钥
    // 如果签名方式设置为“0001”时，请设置该参数
	public static String private_key = "MIICdQIBADANBgkqhkiG9w0BAQEFAASCAl8wggJbAgEAAoGBAJL6ZDllWJaJyPpNV1thlhVRg7UjtpSzSFlV5uuFSF/NRnescjgaUptTSVTyLFbA2W/awRRXhvNqA4CZ+Bf8MmJNBvLL0phLoPKs+ZeAgsNsUTrDqLu1kcgmXWtiTdHtD3mTUxjpFnkTKNDToz+ZcAO7o5j2K8It7VXVn1AGtpO1AgMBAAECgYA0jpsGNypNcmnOr5dcUvIDH4n0XRft5oopf+E6OQffUh0vsBeL3kXJyyd+27ZlM0LNj7DZYE8efbtQ4j3n/cPZ/itWG3QpAaGOd5V23TQqCq27jtQmWte1xGVpUj/WrsB+vZtEVkMlY4BZIMYviuspx4hIi1uSTnglHlS3Fck6BQJBAMPz1+alCV1GnyInI1bHSWhDd+eNERR20bztgGGs5P71yxJmAlP6JM/80tk9BQ/HY/IBMerR0ADEaj/20/yvZNMCQQDABJWSeIBv+tRBmbMWgY5IAKDN2EDolxVY1LyzNa0S5piwB5RBX8xtqx3lbt2vG9ofzQbGysgsf94Wpgh1pXBXAkBx/3d4cEUNZduIc/qELrZPGQk1xYTNQf7tCcLpkDs89OPqVTw/fMRT2AMWbQB32IkVropK0TtQZvRlOATF+YgPAkAYxD6ajspaJysbbvynaXx1kwcqpbrxhRMuyvmvz7uMFYwaIFAiAn42ovyPLDaRsHD46xP1rhAVlUSK/U1YB0evAkA7ia3DFnlit81Sw2TsmstQS9OH0O0IXu1Jy/yN3g1zHljxkF0UqnZFSsrUmpf5an2wYtba8alkN1DFGwYcFCos";

    // 支付宝的公钥
    // 如果签名方式设置为“0001”时，请设置该参数
	public static String ali_public_key = "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQCS+mQ5ZViWicj6TVdbYZYVUYO1I7aUs0hZVebrhUhfzUZ3rHI4GlKbU0lU8ixWwNlv2sEUV4bzagOAmfgX/DJiTQbyy9KYS6DyrPmXgILDbFE6w6i7tZHIJl1rYk3R7Q95k1MY6RZ5EyjQ06M/mXADu6OY9ivCLe1V1Z9QBraTtQIDAQAB";

	//↑↑↑↑↑↑↑↑↑↑请在这里配置您的基本信息↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑
	

	// 调试用，创建TXT日志文件夹路径
	public static String log_path = "D:\\";

	// 字符编码格式 目前支持  utf-8
	public static String input_charset = "utf-8";
	
	// 签名方式，选择项：0001(RSA)、MD5
	public static String sign_type = "MD5";
//	public static String sign_type = "MD5";
	// 无线的产品中，签名方式为rsa时，sign_type需赋值为0001而不是RSA

}
