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

import play.Configuration;

/**
 * lovely 撒娇支付实体
 * 
 * @author luobotao
 *
 */
@Entity
@Table(name = "orderLovely")
public class OrderLoveLy implements Serializable {


	/**
	 * 
	 */
	private static final long serialVersionUID = 8630162972905610349L;

	@Id
	@GeneratedValue
	private Long id;
	@Column(nullable = true, columnDefinition = "int(11) DEFAULT '0' ")
	private int lovely;

	private String lovelytxt;
	private String lovelyimg;
	private String lovelyurl;
	@Column( columnDefinition = "decimal(2,1) DEFAULT '0.0' ")
	private Double lovelydistinct;
	@Column(nullable = true, columnDefinition = "varchar(200) DEFAULT '' ")
	private String img;
	@Column(name="`date_add`")
	@Temporal(TemporalType.TIMESTAMP)
	private Date date_add;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public int getLovely() {
		return lovely;
	}

	public void setLovely(int lovely) {
		this.lovely = lovely;
	}

	public String getLovelytxt() {
		return lovelytxt;
	}

	public void setLovelytxt(String lovelytxt) {
		this.lovelytxt = lovelytxt;
	}

	public String getLovelyimg() {
		return lovelyimg;
	}

	public void setLovelyimg(String lovelyimg) {
		this.lovelyimg = lovelyimg;
	}

	public String getLovelyurl() {
		return lovelyurl;
	}

	public void setLovelyurl(String lovelyurl) {
		this.lovelyurl = lovelyurl;
	}

	public Double getLovelydistinct() {
		return lovelydistinct;
	}

	public void setLovelydistinct(Double lovelydistinct) {
		this.lovelydistinct = lovelydistinct;
	}

	public String getImg() {
		boolean IsProduct = Configuration.root().getBoolean("production", false);
		String domain = Configuration.root().getString("domain.dev","http://ht2.neolix.cn");
		if(IsProduct){
			domain = Configuration.root().getString("domain.product","http://ht.neolix.cn");
		}
		return domain +"/"+img;
		
	}

	public void setImg(String img) {
		this.img = img;
	}

	public Date getDate_add() {
		return date_add;
	}

	public void setDate_add(Date date_add) {
		this.date_add = date_add;
	}

	
}
