package utils.bbt;

import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import play.libs.Json;
import utils.WSUtils;


 
/**棒棒糖UTIL
 * 
 * @author luobotao
 *
 */
public class BBTUtils {
   public static boolean bbtOrder(String reqs, String  paid,String paids){
	   String customer_com_code="BJ_HIGO";//  客户公司编号，这里是 BJ_HIGO
	   String sender_name ="嗨个购";//发件人名称
	   String sender_phone ="4009616909";// 发件人手机号
	   String sender_address="北京嗨购电子商务有限公司";//  发件人街道地址  如 "百子湾路32号苹果社区北区2号楼026室"
	   String sender_province="北京";//  发件人所在省份
	   String sender_city ="北京";//  发件人所在城市
	   String sender_district="朝阳区";//   发件人所在区县
	   String sender_region_code="86101310";//  
	   
	   String receiver_province ="上海";// 收件人所在省份
	   String receiver_city ="浦东";// 收件人所在城市
	   String receiver_district ="A";// 收件人所在区县
	   String receiver_name ="张三";// 收件人姓名
	   String receiver_phone ="13462218896";// 收件人手机
	   String receiver_address ="哈哈中国";// 收件人街道地址   如 "百子湾路32号苹果社区北区2号楼026室"
	   String goods_name ="一具";// 物品名称
	   String goods_number ="1";// 物品数量
	   String remark ="速度运到";//  备注
	   String  pay_mode ="2";//  付款方式   0 寄方付 1收方付（2为月结，2015.2.17）
	   Map json=new HashMap<>();
	   json.put("customer_com_code", customer_com_code);
	   json.put("sender_name", sender_name);
	   json.put("sender_phone", sender_phone);
	   json.put("sender_address", sender_address);
	   json.put("sender_province", sender_province);
	   json.put("sender_city", sender_city);
	   json.put("sender_district", sender_district);
	   json.put("sender_region_code", sender_region_code);
	   json.put("receiver_province", receiver_province);
	   json.put("receiver_city", receiver_city);
	   json.put("receiver_district", receiver_district);
	   json.put("receiver_name", receiver_name);
	   json.put("receiver_phone", receiver_phone);
	   json.put("receiver_address", receiver_address);
	   json.put("goods_name", goods_name);
	   json.put("goods_number", goods_number);
	   json.put("remark", remark);
	   json.put("pay_mode", pay_mode);
	   System.out.println(json);
	   System.out.println(json.get("remark"));
	   JsonNode result = WSUtils.postByForm("http://dev.neolix.cn/external/hg/new_order", json);
	   /*JsonNode result = WSUtils.postByJSON("http://dev.neolix.cn/external/hg/new_order", json);
	   System.out.println(result);*/
	   System.out.println(result);
	   return true;
   }
   public static void main(String[] args) {
	   BBTUtils bBTUtils =new BBTUtils();
	   bBTUtils.bbtOrder("", "", "");
}
}