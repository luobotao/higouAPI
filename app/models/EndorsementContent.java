package models;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name="endorsement_content")
public class EndorsementContent implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1647493420663031286L;

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Integer cid;	//代言方案编号
	
	@Column
	private String remark;	//代言内容描述

	public Integer getCid() {
		return cid;
	}

	public void setCid(Integer cid) {
		this.cid = cid;
	}

	public String getRemark() {
		return remark;
	}

	public void setRemark(String remark) {
		this.remark = remark;
	}
	
	
}
