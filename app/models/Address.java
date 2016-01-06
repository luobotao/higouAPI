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
import javax.persistence.Transient;

@Entity
@Table(name = "address")
public class Address implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = -381369555333910250L;

	@Id
	@GeneratedValue
	@Column(name="id")
	private Long addressId;
	
	private Long uId;
	private String province;
	private String address;
	@Column(name="`name`")
	private String name;
	private String phone;
	private String postcode;
	private String cardId;
	@Column(columnDefinition = " varchar(2) DEFAULT '0' ")
	private String flg;
	@Column(columnDefinition = " varchar(32) DEFAULT '' ")
	private String areaCode;
	
	@Column(name="`date_add`")
	@Temporal(TemporalType.TIMESTAMP)
	private Date date_add;

	/*
	 * “1”表示已上传 “0”表示未上传
	 */
	@Column(columnDefinition = " Int(5) DEFAULT 0 ")
	private Integer cardImg;
	
	@Column(columnDefinition = " varchar(512) DEFAULT ''")
	private String imgpath;
	
	@Transient
	private String secondimgpath;
	
	@Column(columnDefinition = " varchar(50) DEFAULT ''")
	private String md5str;//name+phone+province+address拼串后MD5串
	
	
	public String getMd5str() {
		return md5str;
	}

	public void setMd5str(String md5str) {
		this.md5str = md5str;
	}

	public String getSecondimgpath() {
		if (secondimgpath== null)
		{
			return "";
		}else
			return secondimgpath;
	}

	public void setSecondimgpath(String bimgpath) {
		this.secondimgpath = bimgpath;
	}
	
	public Integer getCardImg() {
		return cardImg;
	}

	public void setCardImg(Integer cardImg) {
		this.cardImg = cardImg;
	}

	public String getImgpath() {
		if (imgpath== null)
		{
			return "";
		}else
			return imgpath;
	}

	public void setImgpath(String imgpath) {
		this.imgpath = imgpath;
	}

	public Long getAddressId() {
		return addressId;
	}

	public void setAddressId(Long addressId) {
		this.addressId = addressId;
	}

	public Long getuId() {
		return uId;
	}

	public void setuId(Long uId) {
		this.uId = uId;
	}

	public String getProvince() {
		return province;
	}

	public void setProvince(String province) {
		this.province = province;
	}

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
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

	public String getPostcode() {
		return postcode;
	}

	public void setPostcode(String postcode) {
		this.postcode = postcode;
	}

	public String getCardId() {
		return cardId;
	}

	public void setCardId(String cardId) {
		this.cardId = cardId;
	}

	public String getFlg() {
		return flg;
	}

	public void setFlg(String flg) {
		this.flg = flg;
	}

	public String getAreaCode() {
		return areaCode;
	}

	public void setAreaCode(String areaCode) {
		this.areaCode = areaCode;
	}

	public Date getDate_add() {
		return date_add;
	}

	public void setDate_add(Date date_add) {
		this.date_add = date_add;
	}
	
	

}
