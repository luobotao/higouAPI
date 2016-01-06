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

import com.fasterxml.jackson.annotation.JsonFormat;

@Entity
@Table(name = "secret_code")
public class SecretCode implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = -5440719524758390198L;

	@Id
	@GeneratedValue
	private Long id;
	
	@Column
	private String code;//使用暗号
	
	@Column
	private Long userid;//使用人编号
	
	@Column
	@Temporal(TemporalType.TIMESTAMP)
	private Date addtime;//生成时间
	
	@Column
	@Temporal(TemporalType.TIMESTAMP)
	private Date updatetime;//被用户使用时间

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public Long getUserid() {
		return userid;
	}

	public void setUserid(Long userid) {
		this.userid = userid;
	}

	public Date getAddtime() {
		if(addtime==null)
			return new Date();
		return addtime;
	}

	public void setAddtime(Date addtime) {
		this.addtime = addtime;
	}

	public Date getUpdatetime() {
		return updatetime;
	}

	public void setUpdatetime(Date updatetime) {
		this.updatetime = updatetime;
	}
	
	
}
