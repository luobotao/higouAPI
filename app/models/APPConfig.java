package models;

public class APPConfig {
	private String data_key;
	private String data_value;
	
	
	public APPConfig(String data_key, String data_value) {
		super();
		this.data_key = data_key;
		this.data_value = data_value;
	}
	public String getData_key() {
		return data_key;
	}
	public void setData_key(String data_key) {
		this.data_key = data_key;
	}
	public String getData_value() {
		return data_value;
	}
	public void setData_value(String data_value) {
		this.data_value = data_value;
	}
}
