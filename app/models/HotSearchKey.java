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
 * 搜索热词
 */
@Entity
@Table(name = "hot_search_key")
public class HotSearchKey implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = -2504159315284144261L;

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Long id;
	
	@Temporal(TemporalType.TIMESTAMP)
	@Column
	private Date createTime;
	@Column
	private String hotWordKey;
	@Column
	private String hotWordTitle;
	@Column
	private String hotWordDes;
	@Column
	private String hotWordImageUrl;
	@Column(columnDefinition = "int(2) DEFAULT 0")
	private Integer isDefault; //1默认显示
	@Column
	private String commentdesc;//方案内容
	@Column(columnDefinition = "int(10) DEFAULT 0")
	private Integer sort;//排序
	public String getCommentdesc() {
		return commentdesc;
	}
	public void setCommentdesc(String commentdesc) {
		this.commentdesc = commentdesc;
	}
	public Integer getIsDefault() {
		return isDefault;
	}
	public void setIsDefault(Integer isDefault) {
		this.isDefault = isDefault;
	}
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public Date getCreateTime() {
		return createTime;
	}
	public void setCreateTime(Date createTime) {
		this.createTime = createTime;
	}
	public String getHotWordKey() {
		return hotWordKey;
	}
	public void setHotWordKey(String hotWordKey) {
		this.hotWordKey = hotWordKey;
	}
	public String getHotWordTitle() {
		return hotWordTitle;
	}
	public void setHotWordTitle(String hotWordTitle) {
		this.hotWordTitle = hotWordTitle;
	}
	public String getHotWordDes() {
		return hotWordDes;
	}
	public void setHotWordDes(String hotWordDes) {
		this.hotWordDes = hotWordDes;
	}
	public String getHotWordImageUrl() {
		return hotWordImageUrl;
	}
	public void setHotWordImageUrl(String hotWordImageUrl) {
		this.hotWordImageUrl = hotWordImageUrl;
	}
	
}
