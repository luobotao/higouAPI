package models;

import utils.Numbers;
import net.logstash.logback.encoder.org.apache.commons.lang.StringUtils;

public class Version {
	
	private Byte os;
	private String latest_version;
	private Byte isforced;
	private Byte remind;
	private String message;
	private String url;
	
	private String has_new;
	private String client_version;
	
	
	public Version(Byte os, String latest_version, Byte isforced,
			Byte remind, String message, String url) {
		super();
		this.os = os;
		this.latest_version = latest_version;
		this.isforced = isforced;
		this.remind = remind;
		this.message = message;
		this.url = url;
	}
	
	public Byte getOs() {
		return os;
	}



	public void setOs(Byte os) {
		this.os = os;
	}



	public Byte getIsforced() {
		return isforced;
	}



	public void setIsforced(Byte isforced) {
		this.isforced = isforced;
	}



	public Byte getRemind() {
		return remind;
	}



	public void setRemind(Byte remind) {
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
	public String getLatest_version() {
		return latest_version;
	}
	public void setLatest_version(String latest_version) {
		this.latest_version = latest_version;
	}
	public String getClient_version() {
		return client_version;
	}
	public void setClient_version(String client_version) {
		this.client_version = client_version;
	}
	public String getHas_new() {
		return has_new;
	}
	public void setHas_new(String has_new) {
		this.has_new = has_new;
	}
	
}
