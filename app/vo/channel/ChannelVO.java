package vo.channel;

import java.util.List;

import models.Channel;

public class ChannelVO {

	private String status;
	private String reffer;
	private List<Channel> data;
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}
	public List<Channel> getData() {
		return data;
	}
	public void setData(List<Channel> data) {
		this.data = data;
	}
	public String getReffer() {
		return reffer;
	}
	public void setReffer(String reffer) {
		this.reffer = reffer;
	}

	

}
