package vo.coupon;

import java.util.List;

public class PayEndResultVO {

	private String status;
	private int displayflg;
	private String toast;
	private int couponsSum;
	private int couponId;
	private List<CouponVO> coupondata;
	
	
	
	public int getCouponId() {
		return couponId;
	}

	public void setCouponId(int couponId) {
		this.couponId = couponId;
	}



	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public int getDisplayflg() {
		return displayflg;
	}

	public void setDisplayflg(int displayflg) {
		this.displayflg = displayflg;
	}

	public String getToast() {
		return toast;
	}

	public void setToast(String toast) {
		this.toast = toast;
	}

	public int getCouponsSum() {
		return couponsSum;
	}

	public void setCouponsSum(int couponsSum) {
		this.couponsSum = couponsSum;
	}



	public List<CouponVO> getCoupondata() {
		return coupondata;
	}

	public void setCoupondata(List<CouponVO> coupondata) {
		this.coupondata = coupondata;
	}



	public static class CouponVO{
		public String id;
		public String title;
		public String description;
		public String flg;
		public String coupontxt;
		public String couponpic;
		public String validdate;
		public String type;
		public String status;
	}
}
