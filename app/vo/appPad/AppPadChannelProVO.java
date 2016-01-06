package vo.appPad;

import java.util.List;

/**
 * @author luobotao
 * @Date 2015年9月22日
 */
public class AppPadChannelProVO {
	// 状态 0：失败 1：成功
	private String status;
	// 返回提示（不管请求状态，有值就提示）
	private String msg;
	private String endflag;
	private List<channelInfo> channellist;
	

	public String getEndflag() {
		return endflag;
	}
	public void setEndflag(String endflag) {
		this.endflag = endflag;
	}


	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}
	

	public List<channelInfo> getChannellist() {
		return channellist;
	}
	public void setChannellist(List<channelInfo> channellist) {
		this.channellist = channellist;
	}
	public String getMsg() {
		return msg;
	}
	public void setMsg(String msg) {
		this.msg = msg;
	}
	
	public static class channelInfo{
		private String mid;
		private String mtype;
		private List<productInfo> productlist;
		public String getMid() {
			return mid;
		}
		public void setMid(String mid) {
			this.mid = mid;
		}
		public String getMtype() {
			return mtype;
		}
		public void setMtype(String mtype) {
			this.mtype = mtype;
		}
		public List<productInfo> getProductlist() {
			return productlist;
		}
		public void setProductlist(List<productInfo> productlist) {
			this.productlist = productlist;
		}
	}
	
	public static class productInfo{
		public String pid="";
		public String linkurl="";
		public String title="";
		public String subtitle="";
		public String price="";
		public String listprice="";
		public String discount="";
		public String nstock="";
		public String img="";
		public String imgmask="";
		public String nationalflagimg="";
		public String nationalflag="";
		public String soldoutimg="";
	}
}