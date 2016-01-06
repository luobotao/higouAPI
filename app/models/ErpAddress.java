package models;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * ERP对应地址实体
 * 
 * @author luobotao Date: 2015年6月27日 上午9:59:24
 */
@Entity
@Table(name = "erpaddress")
public class ErpAddress implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 2759691572165781063L;

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Long id;

	@Column(columnDefinition = "int(10) DEFAULT 0 ")
	private Long parentid;
	private String code;
	@Column(name="`name`")
	private String name;
	@Column(columnDefinition = "int(4) DEFAULT 0 ")
	private int tier;
	
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public Long getParentid() {
		return parentid;
	}
	public void setParentid(Long parentid) {
		this.parentid = parentid;
	}
	public String getCode() {
		return code;
	}
	public void setCode(String code) {
		this.code = code;
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
