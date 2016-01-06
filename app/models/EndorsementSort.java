package models;

public class EndorsementSort {

	private String id;
	private String nickname;//用户妮称
	private String totalfee;//代言产生总销售额
	private String oldfee;//产品原价总额
	private String plusfee;//节省总额
	private String headIcon;//头像
	
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getNickname() {
		return nickname;
	}
	public void setNickname(String nickname) {
		this.nickname = nickname;
	}
	public String getTotalfee() {
		return totalfee;
	}
	public void setTotalfee(String totalfee) {
		this.totalfee = totalfee;
	}
	public String getOldfee() {
		return oldfee;
	}
	public void setOldfee(String oldfee) {
		this.oldfee = oldfee;
	}
	public String getPlusfee() {
		return plusfee;
	}
	public void setPlusfee(String plusfee) {
		this.plusfee = plusfee;
	}
	public String getHeadIcon() {
		return headIcon;
	}
	public void setHeadIcon(String headIcon) {
		this.headIcon = headIcon;
	}
	
	
}
