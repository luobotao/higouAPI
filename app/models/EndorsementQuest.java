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
 * 代言问题表
 */
@Entity
@Table(name="endorsement_quest")
public class EndorsementQuest implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = -1616351814282542107L;

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Long tid;
	
	@Column
	private Integer questType;	//问题类型 0单选，1多选，2问答
	
	@Column
	private String title;	//问题标题
	
	@Column
	private String titleSelect;	//（选择题使用,分隔 a:123,b:456)
	
	@Column
	@Temporal(TemporalType.TIMESTAMP)
	private Date createTime; //创建时间

	public Long getTid() {
		return tid;
	}

	public void setTid(Long tid) {
		this.tid = tid;
	}

	public Integer getQuestType() {
		return questType;
	}

	public void setQuestType(Integer questType) {
		this.questType = questType;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getTitleSelect() {
		return titleSelect;
	}

	public void setTitleSelect(String titleSelect) {
		this.titleSelect = titleSelect;
	}

	public Date getCreateTime() {
		return createTime;
	}

	public void setCreateTime(Date createTime) {
		this.createTime = createTime;
	}

}
