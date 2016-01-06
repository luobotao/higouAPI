package models;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

/*
 * 问题答案表
 */
@Entity
@Table(name="qanswer")
public class Qanswer implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = -5179595953644403716L;

	@Id
	@GeneratedValue
	private Long id;
	
	@Column
	private Long pid;//问题编号
	
	@Column(columnDefinition = " varchar(100) DEFAULT '' ")
	private String qkey;//问题
	
	@Column(columnDefinition = " varchar(500) DEFAULT '' ")
	private String qvalue;//问题答案
	
	
	public String getQkey() {
		return qkey;
	}

	public void setQkey(String key) {
		this.qkey = key;
	}

	public String getQvalue() {
		return qvalue;
	}

	public void setQValues(String Qvalue) {
		this.qvalue = Qvalue;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Long getPid() {
		return pid;
	}

	public void setPid(Long pid) {
		this.pid = pid;
	}

//	public String getKey() {
//		return keys;
//	}
//
//	public void setKey(String key) {
//		this.keys = key;
//	}
//
//	public String getValue() {
//		return values;
//	}
//
//	public void setValue(String value) {
//		this.values = value;
//	}
	
	
}
