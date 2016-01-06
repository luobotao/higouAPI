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
 * 身份证信息验证临时表
 * @author luobotao
 * @Date 2015年10月13日
 */
@Entity
@Table(name="certification_temp")
public class CertificationTemp implements Serializable {
	
	private static final long serialVersionUID = -3798623838545644107L;

	@Id
	@GeneratedValue
	private Long id;
	
	@Column(length = 16,nullable=false)
	private String username;
	
	@Column(length = 64,nullable=false,unique=true)
	private String cardNo;
	
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

	
}
