package models;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import play.data.validation.ValidationError;

/**
 * 首页banner
 * 
 * @author luobotao
 *
 */
@Entity
@Table(name = "adBanner")
public class AdBanner implements Serializable {

	private static final long serialVersionUID = 5876492532367553920L;

	@Id
	@GeneratedValue
	private Long id;

	@Column(nullable = false)
	private int rank;

	@Column(length = 64)
	private String filename;

	@Column()
	private int adtype;

	@Column(length = 1024)
	private String linkurl;
	@Column(name="`date_add`")
	@Temporal(TemporalType.TIMESTAMP)
	private Date date_add;

	@Temporal(TemporalType.TIMESTAMP)
	private Date date_upd;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public int getRank() {
		return rank;
	}

	public void setRank(int rank) {
		this.rank = rank;
	}

	public String getFilename() {
		return filename;
	}

	public void setFilename(String filename) {
		this.filename = filename;
	}

	public int getAdtype() {
		return adtype;
	}

	public void setAdtype(int adtype) {
		this.adtype = adtype;
	}

	public String getLinkurl() {
		return linkurl;
	}

	public void setLinkurl(String linkurl) {
		this.linkurl = linkurl;
	}

	public Date getDate_add() {
		return date_add;
	}

	public void setDate_add(Date date_add) {
		this.date_add = date_add;
	}

	public Date getDate_upd() {
		return date_upd;
	}

	public void setDate_upd(Date date_upd) {
		this.date_upd = date_upd;
	}

	public List<ValidationError> validate() {
		List<ValidationError> errors = new ArrayList<ValidationError>();
		return errors.isEmpty() ? null : errors;
	}

}
