package models.certification;

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
 * 身份证信息本地化实体
 * @author luobotao
 * @Date 2015年10月13日
 */
@Entity
@Table(name="certification")
public class Certification implements Serializable {
	
	private static final long serialVersionUID = 3917563262229903428L;

	@Id
	@GeneratedValue
	private Long id;
	
	@Column(length = 16,nullable=false)
	private String username;
	
	@Column(length = 64,nullable=false,unique=true)
	private String cardNo;
	
	@Column(length = 64,nullable=true)
	private String address;
	
	@Column(length = 4,nullable=true)
	private String sex;
	
	@Column(length = 32,nullable=true)
	private String birthday;
	
	@Column(name="`date_add`")
	@Temporal(TemporalType.TIMESTAMP)
	private Date dateAdd;


	public Long getId() {
		return id;
	}


	public void setId(Long id) {
		this.id = id;
	}


	public String getUsername() {
		return username;
	}


	public void setUsername(String username) {
		this.username = username;
	}


	public String getAddress() {
		return address;
	}


	public void setAddress(String address) {
		this.address = address;
	}


	public String getCardNo() {
		return cardNo;
	}


	public void setCardNo(String cardNo) {
		this.cardNo = cardNo;
	}


	public Date getDateAdd() {
		return dateAdd;
	}


	public void setDateAdd(Date dateAdd) {
		this.dateAdd = dateAdd;
	}


	public String getSex() {
		return sex;
	}


	public void setSex(String sex) {
		this.sex = sex;
	}


	public String getBirthday() {
		return birthday;
	}


	public void setBirthday(String birthday) {
		this.birthday = birthday;
	}
	
	
	
}
