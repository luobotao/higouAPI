package models;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

/**
 * 评论实体
 * @author luobotao
 * @Date 2015年5月5日
 */
@Entity
@Table(name = "comment")
public class Comment implements Serializable {

	private static final long serialVersionUID = -6626482621250995938L;

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Long id;

	private Long uid;// 用户ID
	private Long pid;// 商品ID
	private String nickname;// 商品ID
	@Column(name="`date_add`")
	@Temporal(TemporalType.TIMESTAMP)
	private Date date_add;
	@Lob
	private String content;// 评论内容
	@Column(columnDefinition = " tinyint(4)  DEFAULT '1' ")
	private int status;// 状态 1新增 2审核通过
	@Column(columnDefinition = "tinyint(4) DEFAULT 1 ")
	private int editor;// 1普通用户2小编
	@Column(columnDefinition = "int(10) DEFAULT 1 ")
	private int nsort;// 1正常 2置顶
	private String headIcon;// 头像
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public Long getUid() {
		return uid;
	}
	public void setUid(Long uid) {
		this.uid = uid;
	}
	public Long getPid() {
		return pid;
	}
	public void setPid(Long pid) {
		this.pid = pid;
	}
	public String getNickname() {
		return nickname;
	}
	public void setNickname(String nickname) {
		this.nickname = nickname;
	}
	public Date getDate_add() {
		return date_add;
	}
	public void setDate_add(Date date_add) {
		this.date_add = date_add;
	}
	public String getContent() {
		return content;
	}
	public void setContent(String content) {
		this.content = content;
	}
	public int getStatus() {
		return status;
	}
	public void setStatus(int status) {
		this.status = status;
	}
	public int getEditor() {
		return editor;
	}
	public void setEditor(int editor) {
		this.editor = editor;
	}
	public int getNsort() {
		return nsort;
	}
	public void setNsort(int nsort) {
		this.nsort = nsort;
	}
	public String getHeadIcon() {
		return headIcon;
	}
	public void setHeadIcon(String headIcon) {
		this.headIcon = headIcon;
	}
	
	
}
