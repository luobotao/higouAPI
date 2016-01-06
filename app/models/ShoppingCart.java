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
 * 购物车实体
 * 
 * @author luobotao
 *
 */
@Entity
@Table(name = "shopping_Cart")
public class ShoppingCart implements Serializable {


	
	/**
	 * 
	 */
	private static final long serialVersionUID = -5773577118399520996L;

	@Id
	@GeneratedValue
	private Long id;

	private Long uId;

	private Long pId;
	
	@Column(columnDefinition = " int(11) DEFAULT '0' ")
	private int counts;
	@Column(name="`date_add`")
	@Temporal(TemporalType.TIMESTAMP)
	private Date date_add;
	@Column(columnDefinition = "varchar(500)")
	private String reffer;
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Long getuId() {
		return uId;
	}

	public void setuId(Long uId) {
		this.uId = uId;
	}

	public Long getpId() {
		return pId;
	}

	public void setpId(Long pId) {
		this.pId = pId;
	}

	public int getCounts() {
		return counts;
	}

	public void setCounts(int counts) {
		this.counts = counts;
	}

	public Date getDate_add() {
		return date_add;
	}

	public void setDate_add(Date date_add) {
		this.date_add = date_add;
	}

	public String getReffer() {
		return reffer;
	}

	public void setReffer(String reffer) {
		this.reffer = reffer;
	}


}
