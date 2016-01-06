package vo.coupon;

import java.util.List;

import vo.coupon.PayEndResultVO.CouponVO;

public class CouponResultVO {

	private String status;
	private String endflg;
	private List<CouponVO> data;
	

	public String getEndflg() {
		return endflg;
	}

	public void setEndflg(String endflg) {
		this.endflg = endflg;
	}

	public List<CouponVO> getData() {
		return data;
	}

	public void setData(List<CouponVO> data) {
		this.data = data;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

}
