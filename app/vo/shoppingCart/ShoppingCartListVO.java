package vo.shoppingCart;

import java.util.List;

/**
 * 购物车列表获取result VO
 * @author luobotao
 * @Date 2015年5月8日
 */
public class ShoppingCartListVO {

	private String status;
	private String lovely;
	private String lovelytxt;
	private String lovelyimg;
	private String lovelyurl;
	private String lovelydistinct;
	private List<Object> data;
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}
	public String getLovely() {
		return lovely;
	}
	public void setLovely(String lovely) {
		this.lovely = lovely;
	}
	public String getLovelytxt() {
		return lovelytxt;
	}
	public void setLovelytxt(String lovelytxt) {
		this.lovelytxt = lovelytxt;
	}
	public String getLovelyimg() {
		return lovelyimg;
	}
	public void setLovelyimg(String lovelyimg) {
		this.lovelyimg = lovelyimg;
	}
	public String getLovelyurl() {
		return lovelyurl;
	}
	public void setLovelyurl(String lovelyurl) {
		this.lovelyurl = lovelyurl;
	}
	public String getLovelydistinct() {
		return lovelydistinct;
	}
	public void setLovelydistinct(String lovelydistinct) {
		this.lovelydistinct = lovelydistinct;
	}
	public List<Object> getData() {
		return data;
	}
	public void setData(List<Object> data) {
		this.data = data;
	}
	
	
	
}



