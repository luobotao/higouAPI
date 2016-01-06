package models;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * 最新县及县以上行政区划代码（截止2013年8月31日）
 * @author luobotao
 * @Date 2015年9月28日
 */
@Entity
@Table(name = "chinaAddress")
public class ChinaAddress implements Serializable {

	private static final long serialVersionUID = 5067254946860451894L;

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Integer id;

	private String code;
	@Column(columnDefinition = "int(10) DEFAULT 0 ")
	private Integer parentid;
	@Column(name="`name`")
	private String name;
	@Column(columnDefinition = "int(4) DEFAULT 0 ")
	private int tier;
	public Integer getId() {
		return id;
	}
	public void setId(Integer id) {
		this.id = id;
	}
	public String getCode() {
		return code;
	}
	public void setCode(String code) {
		this.code = code;
	}
	public Integer getParentid() {
		return parentid;
	}
	public void setParentid(Integer parentid) {
		this.parentid = parentid;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public int getTier() {
		return tier;
	}
	public void setTier(int tier) {
		this.tier = tier;
	}
	
	

}
