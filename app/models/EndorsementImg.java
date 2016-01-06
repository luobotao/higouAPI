package models;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

/*
 * 代言图片表
 */
@Entity
@Table(name="endorsementduct_img")
public class EndorsementImg implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 3797503405035189681L;

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Long mid;	//代言图片编号
	
	@Column
	private Long eid;	//代言编号
	
	@Column
	private String ImgName;	//图片名称
	
	@Column
	private String ImgPath;	//图片地址
	
	@Column
	private String remark;	//描述
	
	@Temporal(TemporalType.TIMESTAMP)
	@Column
	private Date createTime; //添加时间

	@Temporal(TemporalType.TIMESTAMP)
	@Column
	private Date updateTime; //添加时间
	
	@Column(columnDefinition="int(10) DEFAULT 1")
	private int picNO;	//客户端过来图片序号
	
	@Column
	private Long width;//图片宽度
	@Column
	private Long height;//图片高度
	
	public Long getWidth() {
		return width;
	}

	public void setWidth(Long width) {
		this.width = width;
	}

	public Long getHeight() {
		return height;
	}

	public void setHeight(Long height) {
		this.height = height;
	}

	public int getPicNO() {
		return picNO;
	}

	public void setPicNO(int picNO) {
		this.picNO = picNO;
	}

	public Date getUpdateTime() {
		return updateTime;
	}

	public void setUpdateTime(Date updateTime) {
		this.updateTime = updateTime;
	}

	public Long getMid() {
		return mid;
	}

	public void setMid(Long mid) {
		this.mid = mid;
	}

	public Long getEid() {
		return eid;
	}

	public void setEid(Long eid) {
		this.eid = eid;
	}

	public String getImgName() {
		return ImgName;
	}

	public void setImgName(String imgName) {
		ImgName = imgName;
	}

	public String getImgPath() {
		return ImgPath;
	}

	public void setImgPath(String imgPath) {
		ImgPath = imgPath;
	}

	public String getRemark() {
		return remark;
	}

	public void setRemark(String remark) {
		this.remark = remark;
	}

	public Date getCreateTime() {
		return createTime;
	}

	public void setCreateTime(Date createTime) {
		this.createTime = createTime;
	}
}
