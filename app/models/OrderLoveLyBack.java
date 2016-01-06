package models;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

/**
 * OrderLoveLyBack 
 * 
 * @author luobotao
 *
 */
@Entity
@Table(name = "Order_lovely_back")
public class OrderLoveLyBack implements Serializable {


	/**
	 * 
	 */
	private static final long serialVersionUID = 480922702472622957L;
	@Id
	@GeneratedValue
	private Long id;
	@Column(columnDefinition = "varchar(32)")
	private String orderCode;
	@Column(name="`name`",columnDefinition = "varchar(32)")
	private String name;
	@Column(columnDefinition = "varchar(32)")
	private String phone;
	@Lob
	private String remark;
	@Column(name="`date_add`")
	@Temporal(TemporalType.TIMESTAMP)
	private Date date_add;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getOrderCode() {
		return orderCode;
	}

	public void setOrderCode(String orderCode) {
		this.orderCode = orderCode;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getPhone() {
		return phone;
	}

	public void setPhone(String phone) {
		this.phone = phone;
	}

	public String getRemark() {
		return remark;
	}

	public void setRemark(String remark) {
		this.remark = remark;
	}

	public Date getDate_add() {
		return date_add;
	}

	public void setDate_add(Date date_add) {
		this.date_add = date_add;
	}

	
}
