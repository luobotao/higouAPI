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
 * 用户验证码实体
 * 
 * @author luobotao
 *
 */
@Entity
@Table(name = "userVerify")
public class UserVerify implements Serializable {


	/**
	 * 
	 */
	private static final long serialVersionUID = 2660581194963918354L;

	@Id
	@GeneratedValue
	private Long id;

	@Column(nullable = false)
	private Long uid;

	@Column(columnDefinition = " varchar(32) DEFAULT '' ")
	private String phone;

	@Column(columnDefinition = " varchar(16) DEFAULT '' ")
	private String verify;

	@Column(columnDefinition = " char(1) DEFAULT '1' ")
	private String flg;
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

	public String getPhone() {
		return phone;
	}

	public void setPhone(String phone) {
		this.phone = phone;
	}

	public String getVerify() {
		return verify;
	}

	public void setVerify(String verify) {
		this.verify = verify;
	}


	public String getFlg() {
		return flg;
	}

	public void setFlg(String flg) {
		this.flg = flg;
	}

	public Date getDate_add() {
		return date_add;
	}

	public void setDate_add(Date date_add) {
		this.date_add = date_add;
	}

	
}
