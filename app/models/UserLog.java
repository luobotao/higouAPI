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

/**
 * 
 * <p>Title: UserLog.java</p> 
 * <p>Description: 用户搜索日志</p> 
 * <p>Company: changyou</p> 
 * @author  somebody
 * date  2015年7月10日  下午2:42:52
 * @version
 */
@Entity
@Table(name = "user_log")
public class UserLog implements Serializable {


	/**
	 * 
	 */
	private static final long serialVersionUID = 2660581194963918354L;

	@Id
	@GeneratedValue
	private Long id;

	@Column(nullable = false)
	private Long uid;

	private Long atype;

	private Long viewid;

	@Column(columnDefinition = " varchar(256) DEFAULT '' ")
	private String content;
	@Column(name="`date_add`")
	@Temporal(TemporalType.TIMESTAMP)
	private Date date_add;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Long getUid() {
		return uid;
	}

	public void setUid(Long uid) {
		this.uid = uid;
	}

	public Long getAtype() {
		return atype;
	}

	public void setAtype(Long atype) {
		this.atype = atype;
	}

	public Long getViewid() {
		return viewid;
	}

	public void setViewid(Long viewid) {
		this.viewid = viewid;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public Date getDate_add() {
		return date_add;
	}

	public void setDate_add(Date date_add) {
		this.date_add = date_add;
	}
	
}
