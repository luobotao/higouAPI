package models;

import java.text.SimpleDateFormat;
import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.Table;

/*
 * 埋点来源日志表
 */
public class Reffer {
	private Long id;
	private String refer;//上一级链接地址
	private String ip;//来源IP
	private String typ;//来源类型:1频道，2专题，3购物车，4订单，5推荐，6代言，7猜你喜欢，8我的最爱，9搜索推荐，a搜索，bPUSH
	private Long tid;//来源ID
	private Date createtime;
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public String getRefer() {
		return refer;
	}
	public void setRefer(String refer) {
		this.refer = refer;
	}
	public String getIp() {
		return ip;
	}
	public void setIp(String ip) {
		this.ip = ip;
	}
	public String getTyp() {
		return typ;
	}
	public void setTyp(String typ) {
		this.typ = typ;
	}
	public Long getTid() {
		return tid;
	}
	public void setTid(Long tid) {
		this.tid = tid;
	}
	public Date getCreatetime() {
		return createtime;
	}
	public void setCreatetime(Date createtime) {
		this.createtime = createtime;
	}
	
}
