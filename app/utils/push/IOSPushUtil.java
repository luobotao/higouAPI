package utils.push;


import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
 


import javapns.Push;
import javapns.communication.exceptions.CommunicationException;
import javapns.communication.exceptions.KeystoreException;
import javapns.devices.Device;
import javapns.devices.Devices;
import javapns.notification.PayloadPerDevice;
import javapns.notification.PushNotificationPayload;
import javapns.notification.PushedNotifications;
import javapns.notification.transmission.PushQueue;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONException;
 
public class IOSPushUtil {
    public static String keystore = null;
    public static String password = null;
    public static String host = null;
    public static Boolean production = true;//true：production false: sandbox
    public static final int numberOfThreads = 8;
    static{
        Properties propertie = new Properties();
        InputStream inputStream;
         
        try {
            inputStream = IOSPushUtil.class.getClassLoader()
                    .getResourceAsStream("push.properties");
            propertie.load(inputStream);
            keystore = propertie.getProperty("certificatePath");
            password = propertie.getProperty("certificatePassword","111111");
            host = propertie.getProperty("host","gateway.push.apple.com");
            production = Boolean.valueOf(propertie.getProperty("production", "true"));
            inputStream.close();
        } catch (FileNotFoundException ex) {
            ex.printStackTrace();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
    public static void main(String[] args) throws Exception {
    	String token="c58cd95b e7b78012 3cba0b0b af8ed933 7f3b9a71 81fb88f3 7aa07e20 3bb1668e";
    	String token2="108150cd cc112af5 10ca26b0 3da9923d 86c301ab cff06605 c55ffbb2 d068049c";
    	String token3="ab9accab 8c300b16 405fce9f 05395348 c8427b8c b77cc17c 36886c14 3a98ec51";
    	token = token.replaceAll(" ", "");
    	token2 = token2.replaceAll(" ", "");
    	token3 = token3.replaceAll(" ", "");
//        pushMsgNotification("hello!你好啊，哈哈",  "ef6c309527ce850ea6fe7d108b097468fae0742a0c1a284d0de75d9f4da90fd0");
//        pushMsgNotification("hello!你好要啊，哈哈",  "108150cdcc112af510ca26b03da9923d86c301abcff06605c55ffbb2d068049c");
//        pushMsgNotification("h 基本原则ello!你好要啊，哈哈",  token);
        pushMsgNotification("我发哈哈",  token2);
//        pushMsgNotification("h 基本原则ello!你好要啊，哈哈",  token3);
//        pushBadgeNotification(122,  token);
        pushBadgeNotification(133,  token2);
//        pushBadgeNotification(144,  token3);
        String[] devs= new String[10000];
      /*  for (int i = 0; i < devs.length; i++) {
        	devs[i] = "iostoken";
        }
      List<Device> devices=Devices.asDevices("ef6c309527ce850ea6fe7d108b097468fae0742a0c1a284d0de75d9f4da90fd0");
        System.out.println(devices.size());
        //pushPayLoadByThread(devices, "Hello 2222222", 1, null, null);
        //pushPayloadDevicePairs(devices, "Hello 111111111", 1, null, null);
        //pushPayloadDevicePairs(devices, "Hello +++", 1, null, null);
        queue(devices,"Hello 2222222", 1, null, null);*/
    }
    /**
     * 推送一个简单消息
     * @param msg 消息
     * @param devices 设备
     * @throws CommunicationException
     * @throws KeystoreException
     */
    public static void pushMsgNotification(String msg,Object devices) throws CommunicationException, KeystoreException{
    	PushedNotifications result = Push.alert(msg, keystore, password, production, devices);
    	System.out.println(result);
    }
    /**
     * 推送一个标记
     * @param badge 标记
     * @param devices 设备
     * @throws CommunicationException
     * @throws KeystoreException
     */
    public static void pushBadgeNotification(int badge,Object devices) throws CommunicationException, KeystoreException{
    	PushedNotifications result = Push.badge(badge, keystore, password, production, devices);
    	System.out.println(result);
    }
    /**
     * 推送一个语音
     * @param sound 语音
     * @param devices 设备
     * @throws CommunicationException
     * @throws KeystoreException
     */
    public static void pushSoundNotification(String sound,Object devices) throws CommunicationException, KeystoreException{
        Push.sound(sound, keystore, password, production, devices);
    }
    /**
     * 推送一个alert+badge+sound通知
     * @param message 消息
     * @param badge 标记
     * @param sound 声音
     * @param devices 设备
     * @throws CommunicationException
     * @throws KeystoreException
     */
    public static void pushCombinedNotification(String message,int badge,String sound,Object devices) throws CommunicationException, KeystoreException{
        Push.combined(message, badge, sound, keystore, password, production, devices);
    }
    /**
     * 通知Apple的杂志内容
     * @param devices 设备
     * @throws CommunicationException
     * @throws KeystoreException
     */
    public static void contentAvailable(Object devices) throws CommunicationException, KeystoreException{
        Push.contentAvailable(keystore, password, production, devices);
    }
    /**
     * 推送有用的调试信息
     * @param devices 设备
     * @throws CommunicationException
     * @throws KeystoreException
     */
    public static void test(Object devices) throws CommunicationException, KeystoreException{
        Push.test(keystore, password, production, devices);
    }
    /**
     * 推送自定义负载
     * @param devices
     * @param msg
     * @param badge
     * @param sound
     * @param map
     * @throws JSONException
     * @throws CommunicationException
     * @throws KeystoreException
     */
    public static void pushPayload(List<Device> devices, String msg,Integer badge,String sound,Map<String,String> map) throws JSONException, CommunicationException, KeystoreException{
        PushNotificationPayload payload = customPayload(msg, badge, sound, map);
        Push.payload(payload, keystore, password, production, devices);
    }
    /**
     * 用内置线程推送负载信息
     * @param devices
     * @param msg
     * @param badge
     * @param sound
     * @param map
     * @throws Exception
     */
    public static void pushPayLoadByThread(List<Device> devices, String msg,Integer badge,String sound,Map<String,String> map) throws Exception{
        PushNotificationPayload payload = customPayload(msg, badge, sound, map);
        Push.payload(payload, keystore, password, production, numberOfThreads, devices);
    }
    /**
     * 推送配对信息
     * @param devices
     * @param msg
     * @param badge
     * @param sound
     * @param map
     * @throws JSONException
     * @throws CommunicationException
     * @throws KeystoreException
     */
    public static void pushPayloadDevicePairs(List<Device> devices,String msg,Integer badge,String sound,Map<String,String> map) throws JSONException, CommunicationException, KeystoreException{
        List<PayloadPerDevice> payloadDevicePairs = new ArrayList<PayloadPerDevice>();
        PayloadPerDevice perDevice = null;
        for (int i = 0; i <devices.size(); i++) {
            perDevice = new PayloadPerDevice(customPayload(msg+"--->"+i, badge, sound, map), devices.get(i));
            payloadDevicePairs.add(perDevice);
        }
        Push.payloads(keystore, password, production, payloadDevicePairs);
    }
    /**
     * 用线程推配对信息
     * @param devices
     * @param msg
     * @param badge
     * @param sound
     * @param map
     * @throws Exception
     */
    public static void pushPayloadDevicePairsByThread(List<Device> devices,String msg,Integer badge,String sound,Map<String,String> map) throws Exception{
        List<PayloadPerDevice> payloadDevicePairs = new ArrayList<PayloadPerDevice>();
        PayloadPerDevice perDevice = null;
        for (int i = 0; i <devices.size(); i++) {
            perDevice = new PayloadPerDevice(customPayload(msg+"--->"+i, badge, sound, map), devices.get(i));
            payloadDevicePairs.add(perDevice);
        }
        Push.payloads(keystore, password, production,numberOfThreads, payloadDevicePairs);
    }
    /**
     * 队列多线程推送
     * @param devices
     * @param msg
     * @param badge
     * @param sound
     * @param map
     * @throws KeystoreException
     * @throws JSONException
     */
    public static void queue(List<Device> devices,String msg,Integer badge,String sound,Map<String,String> map) throws KeystoreException, JSONException{
        PushQueue queue = Push.queue(keystore, password, production, numberOfThreads);
        queue.start();
        PayloadPerDevice perDevice = null;
        for (int i = 0; i <devices.size(); i++) {
            perDevice = new PayloadPerDevice(customPayload(msg+"--->"+i, badge, sound, map), devices.get(i));
            queue.add(perDevice);
        }
    }
    /**
     * 自定义负载
     * @param msg
     * @param badge
     * @param sound
     * @param map 自定义字典
     * @return
     * @throws JSONException
     */
    private static PushNotificationPayload customPayload(String msg,Integer badge,String sound,Map<String,String> map) throws JSONException{
        PushNotificationPayload payload = PushNotificationPayload.complex();
        if(StringUtils.isNotEmpty(msg)){
            payload.addAlert(msg);         
        }
        if(badge != null){         
            payload.addBadge(badge);
        }
        payload.addSound(StringUtils.defaultIfEmpty(sound, "default"));
        if(map!=null && !map.isEmpty()){
            Object[] keys = map.keySet().toArray();    
            Object[] vals = map.values().toArray();
            if(keys!= null && vals != null && keys.length == vals.length){
                for (int i = 0; i < map.size(); i++) {                  
                    payload.addCustomDictionary(String.valueOf(keys[i]),String.valueOf(vals[i]));
                }
            }
        }
        return payload;
    }
}