package services;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;

import javax.inject.Named;
import javax.inject.Singleton;

import play.Logger;
import play.libs.Json;
import utils.push.AndroidUnicast;

import com.fasterxml.jackson.databind.JsonNode;

import forms.Umeng.MessagePush;


/**
 * 友盟推送 只针对Android
 * @author luobotao
 *
 */
@Named
@Singleton
public class UmengService extends Thread{
    private static final Logger.ALogger LOGGER = Logger.of(UmengService.class);
    private static final String appkey = "5496ccb5fd98c5c8060001ab"; //这里换成你的appkey
	private static final String appMasterSecret = "q9wdl5eby8wcwukbowmw0beoylzjurfn"; //这里换成你的appMasterSecret
	private String timestamp = null;
	private JsonNode resultJson = Json.newObject();
	
	private static UmengService instance = new UmengService();
    private Executor executor = Executors.newSingleThreadExecutor();
    private LinkedBlockingQueue<Runnable> tasks = new LinkedBlockingQueue<>();
	 /* 私有构造方法，防止被实例化 */
	private UmengService(){
		timestamp = Integer.toString((int)(System.currentTimeMillis() / 1000));
		this.start();
	}
	public void run(){
//		LOGGER.info("start UmengService service ");
		System.out.println("start UmengService service ");
		Runnable r;
		try {
			while((r = tasks.take()) != null){
				executor.execute(r);
			}
		} catch (InterruptedException e) {
			LOGGER.error("InterruptedException in UmengService service",e);
		}
	}
	public static UmengService getInstance(){
		return instance;
	}

	/**
	 * 向单个设备推送消息
	 * @param device_tokens
	 * @param messagePush 
	 * @return
	 */
	public JsonNode sendAndroidUnicast(String device_tokens, MessagePush messagePush){
		AndroidUnicast unicast = new AndroidUnicast();
		
		try {
			unicast.setAppMasterSecret(appMasterSecret);
			unicast.setPredefinedKeyValue("appkey", appkey);
			unicast.setPredefinedKeyValue("timestamp", timestamp);
			// TODO Set your device token
			unicast.setPredefinedKeyValue("device_tokens", device_tokens);
			unicast.setPredefinedKeyValue("ticker", messagePush.ticker);
			unicast.setPredefinedKeyValue("title",  messagePush.title);
			unicast.setPredefinedKeyValue("text",   messagePush.text);
			unicast.setPredefinedKeyValue("after_open", messagePush.after_open);
			unicast.setPredefinedKeyValue("display_type", messagePush.display_type);
			unicast.setPredefinedKeyValue("production_mode", "true");
			resultJson = unicast.send();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return resultJson;
	}
	
	/**
	 * 向所有设备推送消息
	 * @param messagePush
	 * @return
	 */
	public JsonNode sendAndroidBroadcast(MessagePush messagePush){
		AndroidUnicast unicast = new AndroidUnicast();
		
		try {
			unicast.setAppMasterSecret(appMasterSecret);
			unicast.setPredefinedKeyValue("appkey", appkey);
			unicast.setPredefinedKeyValue("timestamp", timestamp);
			unicast.setPredefinedKeyValue("ticker", messagePush.ticker);
			unicast.setPredefinedKeyValue("title",  messagePush.title);
			unicast.setPredefinedKeyValue("text",   messagePush.text);
			unicast.setPredefinedKeyValue("after_open", messagePush.after_open);
			unicast.setPredefinedKeyValue("display_type", messagePush.display_type);
			unicast.setPredefinedKeyValue("production_mode", "true");
			resultJson = unicast.send();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return resultJson;
	}
	
	public static void main(String args[]){
		MessagePush messagePush = new MessagePush();
		messagePush.ticker="ticker";
		messagePush.title="A";
		messagePush.text="11111111111111A";
		messagePush.display_type="notification";
		messagePush.after_open="go_app";
		UmengService.getInstance().sendAndroidUnicast("AqXm1qhBElnn0_MEX1u7IM62-NqmBclEO4k776POASe6",messagePush);
		UmengService.getInstance().sendAndroidUnicast("AqXm1qhBElnn0_MEX1u7IM62-NqmBclEO4k776POASe6",messagePush);
	}
	

}
