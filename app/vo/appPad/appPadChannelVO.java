package vo.appPad;

import java.util.List;

import vo.comment.CommentListVO.CommentItem;
import vo.product.ProductDetailVO.ProductDetailItem;

public class appPadChannelVO {
	// 状态 0：失败 1：成功
	private String status;
	// 返回提示（不管请求状态，有值就提示）
	private String msg;
	
	private List<Channel> channels;

	public List<Channel> getChannels() {
		return channels;
	}
	public void setChannels(List<Channel> channels) {
		this.channels = channels;
	}
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}
	

	public String getMsg() {
		return msg;
	}
	public void setMsg(String msg) {
		this.msg = msg;
	}
	
	public  static class Channel{
		public String cid;
		public String cname;
	}
	
}