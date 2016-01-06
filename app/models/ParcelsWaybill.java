package models;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;

/**
 * 包裹运单实体
 * 
 */
@Entity
@Table(name = "pardels_Waybill")
public class ParcelsWaybill implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -8611234270321297022L;

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Long id;

	@Column(name="`date_add`")
	@Temporal(TemporalType.TIMESTAMP)
	private Date date_add;

	@Column(nullable = false)
	private Long pardelsId;

	@Column(nullable = false, columnDefinition = "varchar(64) ")
	private String waybillCode;
	private String transport;

	private String status;
	private String date_upd;
	private String remark;
	private String date_txt;
	private int nsort;
	private String trancode;
	
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public Date getDate_add() {
		return date_add;
	}
	public void setDate_add(Date date_add) {
		this.date_add = date_add;
	}
	public Long getPardelsId() {
		return pardelsId;
	}
	public void setPardelsId(Long pardelsId) {
		this.pardelsId = pardelsId;
	}
	public String getWaybillCode() {
		return waybillCode;
	}
	public void setWaybillCode(String waybillCode) {
		this.waybillCode = waybillCode;
	}
	public String getTransport() {
		return transport;
	}
	public void setTransport(String transport) {
		this.transport = transport;
	}
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}
	public String getDate_upd() {
		return date_upd;
	}
	public void setDate_upd(String date_upd) {
		this.date_upd = date_upd;
	}
	public String getRemark() {
		return remark;
	}
	public void setRemark(String remark) {
		this.remark = remark;
	}
	public String getDate_txt() {
		return date_txt;
	}
	public void setDate_txt(String date_txt) {
		this.date_txt = date_txt;
	}
	public int getNsort() {
		return nsort;
	}
	public void setNsort(int nsort) {
		this.nsort = nsort;
	}
	public String getTrancode() {
		return trancode;
	}
	public void setTrancode(String trancode) {
		this.trancode = trancode;
	}

}
