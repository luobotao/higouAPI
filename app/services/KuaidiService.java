package services;

import java.util.HashMap;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;

import javax.inject.Named;
import javax.inject.Singleton;

import play.Configuration;
import play.Logger;
import play.libs.Json;
import utils.WSUtils;
import utils.kuaidi100.JacksonHelper;
import utils.kuaidi100.TaskRequest;

import com.alibaba.druid.Constants;
import com.fasterxml.jackson.databind.JsonNode;


/**
 * 快递100Service
 * @author luobotao
 *
 */
@Named
@Singleton
public class KuaidiService extends Thread{
    private static final Logger.ALogger LOGGER = Logger.of(KuaidiService.class);
    private static final String appFreeKey = "aa54cc23759a9c76"; //这里换成你的appFreeKey
	private static final String appKey="qWmaQkHp269"; //这里换成你的appKey
	
	private JsonNode resultJson = Json.newObject();
	
	private static KuaidiService instance = new KuaidiService();
    private Executor executor = Executors.newSingleThreadExecutor();
    private LinkedBlockingQueue<Runnable> tasks = new LinkedBlockingQueue<>();
	 /* 私有构造方法，防止被实例化 */
	private KuaidiService(){
		this.start();
	}
	public void run(){
		LOGGER.info("start KuaidiService service ");
//		System.out.println("start KuaidiService service ");
		Runnable r;
		try {
			while((r = tasks.take()) != null){
				executor.execute(r);
			}
		} catch (InterruptedException e) {
			LOGGER.error("InterruptedException in KuaidiService service",e);
		}
	}
	public static KuaidiService getInstance(){
		return instance;
	}

	/**
	 * @param company 快递公司
	 * @param orderNumber 快递单号
	 * @param muti 返回信息数量：1:返回多行完整的信息， 0:只返回一行信息。 不填默认返回多行。
	 * @param order 排序：desc：按时间由新到旧排列，asc：按时间由旧到新排列。不填默认返回倒序（大小写不敏感） 
	 * @return
	 */
	public JsonNode queryOrderDetailAsJson(String company, String orderNumber,String muti,String order){
		String url="http://api.kuaidi100.com/api?id="+appFreeKey+"&com="+company+"&nu="+orderNumber+"&show=0&muti="+muti+"&order="+order;
		return WSUtils.getResponseAsJson(url);
	}
	/**
	 * @param company 快递公司
	 * @param orderNumber 快递单号
	 * @param show 返回类型： 0：返回json字符串， 1：返回xml对象， 2：返回html对象， 3：返回text文本。 如果不填，默认返回json字符串。
	 * @param muti 返回信息数量：1:返回多行完整的信息， 0:只返回一行信息。 不填默认返回多行。
	 * @param order 排序：desc：按时间由新到旧排列，asc：按时间由旧到新排列。不填默认返回倒序（大小写不敏感） 
	 * @return
	 */
	public String queryOrderDetailAsString(String company, String orderNumber,String show,String muti,String order){
		String url="http://api.kuaidi100.com/api?id="+appFreeKey+"&com="+company+"&nu="+orderNumber+"&show="+show+"&muti="+muti+"&order="+order;
		return WSUtils.getResponseAsString(url);
	}
	/**
	 * 订阅一个订单
	 * @param company快递公司
	 * @param from
	 * @param to
	 * @param orderNumber快递单号
	 * @return
	 */
	public JsonNode subscribe(String company,String from,String to, String orderNumber){
		TaskRequest req = new TaskRequest();
		req.setCompany(company);
		req.setFrom(from);
		req.setTo(to);
		req.setNumber(orderNumber);
		boolean IsProduct = Configuration.root().getBoolean("production", false);
		String domain = Configuration.root().getString("domain.dev","http://ht2.neolix.cn");
		if(IsProduct){
			domain = Configuration.root().getString("domain.product","http://ht.neolix.cn");
		}
		req.getParameters().put("callbackurl", domain+"/api/kuaidi100_callback");
		req.setKey(appKey);
		
		HashMap<String, String> p = new HashMap<String, String>(); 
		p.put("schema", "json");
		p.put("param", JacksonHelper.toJSON(req));
		return WSUtils.postByForm("http://www.kuaidi100.com/poll", p);
	}

}
