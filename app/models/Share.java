package models;

public class Share {
	private String sharetitle;
	private String shareimg;
	private String shareurl;
	private String sharetxt;
	
	
	public Share(String sharetitle, String shareimg, String shareurl,
			String sharetxt) {
		super();
		this.sharetitle = sharetitle;
		this.shareimg = shareimg;
		this.shareurl = shareurl;
		this.sharetxt = sharetxt;
	}
	public String getSharetitle() {
		return sharetitle;
	}
	public void setSharetitle(String sharetitle) {
		this.sharetitle = sharetitle;
	}
	public String getShareimg() {
		return shareimg;
	}
	public void setShareimg(String shareimg) {
		this.shareimg = shareimg;
	}
	public String getShareurl() {
		return shareurl;
	}
	public void setShareurl(String shareurl) {
		this.shareurl = shareurl;
	}
	public String getSharetxt() {
		return sharetxt;
	}
	public void setSharetxt(String sharetxt) {
		this.sharetxt = sharetxt;
	}
	
}
