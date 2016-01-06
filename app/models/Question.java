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
 * 问题表
 */
@Entity
@Table(name = "question")
public class Question  implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = -6346949093505775965L;
	@Id
	@GeneratedValue
	private Long id;
	@Column
	@Temporal(TemporalType.TIMESTAMP)
	private Date addtime;//添加时间
	@Column
	private Long uid;
	
	public Long getUid() {
		return uid;
	}

	public void setUid(Long uid) {
		this.uid = uid;
	}

	public Date getAddtime() {
		return addtime;
	}

	public void setAddtime(Date addtime) {
		this.addtime = addtime;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}
	
}
