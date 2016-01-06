package utils.kuaidi100;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;
import play.Logger;

public class Kuaidi100 {

	private static String appFreeKey = "aa54cc23759a9c76";// 查询运单时使用
	private static String appKey = "qWmaQkHp269";// 订阅时使用
	private static String show = "0"; // 返回类型： 0：返回json字符串， 1：返回xml对象，
										// 2：返回html对象， 3：返回text文本。
										// 如果不填，默认返回json字符串。
	private static String muti = "1";// 返回信息数量：1:返回多行完整的信息， 0:只返回一行信息。 不填默认返回多行。
	private static String order = "desc";// 排序：desc：按时间由新到旧排列，asc：按时间由旧到新排列。不填默认返回倒序（大小写不敏感）

	/**
	 * 实时查询运单物流信息
	 * 
	 * @param company
	 * @param orderNumber
	 * @return
	 */
	public static String queryKuaidi(String company, String orderNumber) {
		String result = "";
		try {
			String urlStr = "http://api.kuaidi100.com/api?id=" + appFreeKey
					+ "&com=" + company + "&nu=" + orderNumber + "&show="
					+ show + "&muti=" + muti + "&order=" + order;
			System.out.println(urlStr);
			URL url = new URL(urlStr);
			URLConnection con = url.openConnection();
			con.setAllowUserInteraction(false);
			InputStream urlStream = url.openStream();
			String type = con.guessContentTypeFromStream(urlStream);
			String charSet = null;
			if (type == null)
				type = con.getContentType();

			if (type == null || type.trim().length() == 0
					|| type.trim().indexOf("text/html") < 0)
				return "";

			if (type.indexOf("charset=") > 0)
				charSet = type.substring(type.indexOf("charset=") + 8);

			byte b[] = new byte[10000];
			int numRead = urlStream.read(b);
			result = new String(b, 0, numRead);
			while (numRead != -1) {
				numRead = urlStream.read(b);
				if (numRead != -1) {
					String newContent = new String(b, 0, numRead, charSet);
					result += newContent;
				}
			}
			urlStream.close();
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return result;
	}

	
	public static boolean subscribe(String company, String orderNumber,String from,String to) {
		TaskRequest req = new TaskRequest();
		req.setCompany(company);
		req.setFrom(from);
		req.setTo(to);
		req.setNumber(orderNumber);
		req.getParameters().put("callbackurl", "http://ht.neolix.cn/api/kuaidi100_callback.php");
		req.setKey(appKey);
		
		HashMap<String, String> p = new HashMap<String, String>(); 
		p.put("schema", "json");
		p.put("param", JacksonHelper.toJSON(req));
		try {
			String ret = HttpRequest.postData("http://www.kuaidi100.com/poll", p, "UTF-8");
			//System.out.println(ret);
			TaskResponse resp = JacksonHelper.fromJSON(ret, TaskResponse.class);
			if(resp.getResult()==true){
				return true;//订阅成功
			}else{
				Logger.info("error message is :"+resp.getMessage()+",and returncode is :"+resp.getReturnCode());
				return false;//订阅失败
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;//订阅失败
	}
	
	public static void main(String[] agrs) {
		String com = "shunfeng";
		String orderNumber = "782003525852";
		System.out.println(Kuaidi100.queryKuaidi(com, orderNumber));
		System.out.println(Kuaidi100.subscribe(com, orderNumber,"北京","赣州"));;
	}

}
