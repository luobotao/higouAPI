package vo.product;

import java.util.List;

import javax.persistence.Column;

public class ProductEndorsementVO {

	private String status;
	private String bannerImg;
	private String daiyanURL;
	private String reffer;
	
	public String getReffer() {
		return reffer;
	}
	public void setReffer(String reffer) {
		this.reffer = reffer;
	}
	public String getDaiyanURL() {
		return daiyanURL;
	}
	public void setDaiyanURL(String daiyanURL) {
		this.daiyanURL = daiyanURL;
	}
	public String getBannerImg() {
		return bannerImg;
	}
	public void setBannerImg(String bannerImg) {
		this.bannerImg = bannerImg;
	}

	private List<ProductEndorsementItem> Data;
	
	public String getStatus() {
		return status;
	}
	
	public void setStatus(String status) {
		this.status = status;
	}
	public List<ProductEndorsementItem> getData() {
		return Data;
	}

	public void setData(List<ProductEndorsementItem> data) {
		Data = data;
	}

	public static class ProductEndorsementItem{
		public String pid;                       
		public String pcode;                     
		public String title;
		public String subtitle;
		public String rmb_price;                 
		public String listpic;
		public String nationalFlagImg;
		public Double rmb_price_no_symbol;
		public String type;
		public String endorsementPrice;
		public String commision;
		public String commision_average;
		public String linkUrl;
	}
}
