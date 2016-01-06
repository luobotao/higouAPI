package models;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * 短信实体类
 * 从表中获取数据，依靠RabbitMQ进行消息发送
 * @author luobotao
 *
 */
@Entity
@Table(name = "smsinfo")
public class SmsInfo implements Serializable {
	


	/**
	 * 
	 */
	private static final long serialVersionUID = -6881239945444620545L;

	@Id
	@GeneratedValue
	private Long id;

	private String phone;

	private String tpl_id;

	private String args;
	
	private String flag;//标志是否已发送 0未发送 1已发送 2发送失败3正在发送
	
	private Date insertTime;//插入时间
	
	private Date updateTime;//更新时间
	@Column(columnDefinition = " varchar(255) DEFAULT '1' ")
	private String type;//1普通短信2营销短信3语音短信
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getPhone() {
		return phone;
	}

	public void setPhone(String phone) {
		this.phone = phone;
	}

	public String getTpl_id() {
		return tpl_id;
	}

	public void setTpl_id(String tpl_id) {
		this.tpl_id = tpl_id;
	}


	public String getArgs() {
		return args;
	}

	public void setArgs(String args) {
		this.args = args;
	}

	public String getFlag() {
		return flag;
	}

	public void setFlag(String flag) {
		this.flag = flag;
	}

	public Date getInsertTime() {
		return insertTime;
	}

	public void setInsertTime(Date insertTime) {
		this.insertTime = insertTime;
	}

	public Date getUpdateTime() {
		return updateTime;
	}

	public void setUpdateTime(Date updateTime) {
		this.updateTime = updateTime;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	
}
