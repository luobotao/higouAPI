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

import net.logstash.logback.encoder.org.apache.commons.lang.StringUtils;
import utils.StringUtil;
import assets.CdnAssets;

/**
 * product_images
 * @author luobotao
 * @Date 2015年5月11日
 */
@Entity
@Table(name = "product_images")
public class Product_images implements Serializable {

	private static final long serialVersionUID = 5876492532367553920L;

	@Id
	@GeneratedValue
	private Long id;

	@Column(nullable = false)
	private Long pid;

	private String pcode;
		
	@Column(length = 64)
	private String filename;
	@Column(name="`date_add`")
	@Temporal(TemporalType.TIMESTAMP)
	private Date date_add;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Long getPid() {
		return pid;
	}

	public void setPid(Long pid) {
		this.pid = pid;
	}

	public String getPcode() {
		return pcode;
	}

	public void setPcode(String pcode) {
		this.pcode = pcode;
	}

	public String getFilename() {
		if(StringUtils.isBlank(filename)){
			return CdnAssets.CDN_API_PUBLIC_URL+"images/sheSaidImages/p_e.jpg";
		}
		return StringUtil.getListpic(filename);
	}

	public String getPicname(){
		if(StringUtils.isBlank(filename)){
			return CdnAssets.CDN_API_PUBLIC_URL+"images/sheSaidImages/p_e.jpg";
		}
		return filename;
	}
	public void setFilename(String filename) {
		this.filename = filename;
	}

	public Date getDate_add() {
		return date_add;
	}

	public void setDate_add(Date date_add) {
		this.date_add = date_add;
	}
	
	


}
