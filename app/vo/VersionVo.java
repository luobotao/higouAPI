package vo;

import net.logstash.logback.encoder.org.apache.commons.lang.StringUtils;
import utils.Numbers;


public class VersionVo {
	
	private String os;
	private String latest_version;
	private String isforced;
	private String remind;
	private String message;
	private String url;
	
	private String has_new;
	private String client_version;
	public String getOs() {
		if("1".equals(os)){
			return "ios";
		}else{
			return "android";
		}
	}
	public void setOs(String os) {
		this.os = os;
	}
	public String getLatest_version() {
		return latest_version;
	}
	public void setLatest_version(String latest_version) {
		this.latest_version = latest_version;
	}
	public String getIsforced() {
		return isforced;
	}
	public void setIsforced(String isforced) {
		this.isforced = isforced;
	}
	public String getRemind() {
		return remind;
	}
	public void setRemind(String remind) {
		this.remind = remind;
	}
	public String getMessage() {
		return message;
	}
	public void setMessage(String message) {
		this.message = message;
	}
	public String getUrl() {
		return url;
	}
	public void setUrl(String url) {
		this.url = url;
	}
	public String getHas_new() {
		if(!StringUtils.isBlank(latest_version) && !StringUtils.isBlank(client_version) ){
			String late = latest_version.replaceAll("\\.", "");
			String client = client_version.replaceAll("\\.", "");
			if(Numbers.parseInt(late, 0)>Numbers.parseInt(client, 0)){
				has_new = "1";
			}else{
				has_new = "0";
			}
		}else{
			has_new = "0";
		}
		return has_new;
	}

	public void setHas_new(String has_new) {
		this.has_new = has_new;
	}
	public String getClient_version() {
		return client_version;
	}
	public void setClient_version(String client_version) {
		this.client_version = client_version;
	}
	
	
}
