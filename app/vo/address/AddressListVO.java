package vo.address;

import java.util.List;

import models.Address;

/**
 * 收获地址列表获取result VO
 * @author luobotao
 * @Date 2015年5月8日
 */
public class AddressListVO {

	private String status;
	private List<Address> data;
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}
	public List<Address> getData() {
		return data;
	}
	public void setData(List<Address> data) {
		this.data = data;
	}
	
	
}



