package models;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * fromsite
 * 
 * @author luobotao
 *
 */
@Entity
@Table(name = "fromsite")
public class Fromsite implements Serializable {


	/**
	 * 
	 */
	private static final long serialVersionUID = -8274273798904696768L;

	@Id
	@GeneratedValue
	private int id;
	@Column(name="`name`")
	private String name;
	private String url;
	private String img;
	private int fee;
	private int addfee;
	
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getUrl() {
		return url;
	}
	public void setUrl(String url) {
		this.url = url;
	}
	public String getImg() {
		return img;
	}
	public void setImg(String img) {
		this.img = img;
	}
	public int getFee() {
		return fee;
	}
	public void setFee(int fee) {
		this.fee = fee;
	}
	public int getAddfee() {
		return addfee;
	}
	public void setAddfee(int addfee) {
		this.addfee = addfee;
	}

	
	

}
