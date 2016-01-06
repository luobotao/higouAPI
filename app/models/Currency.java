package models;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * currency
 * 
 * @author luobotao
 *
 */
@Entity
@Table(name = "currency")
public class Currency implements Serializable {

	private static final long serialVersionUID = -8274273798904696768L;

	@Id
	@GeneratedValue
	private int id;
	@Column(name="`name`")
	private String name;
	private String symbol;
	private Double rate;
	private String rate1;
	private String rate2;
	private String rate3;
	private String rate4;
	private String rate5;
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
	public String getSymbol() {
		return symbol;
	}
	public void setSymbol(String symbol) {
		this.symbol = symbol;
	}
	public Double getRate() {
		return rate;
	}
	public void setRate(Double rate) {
		this.rate = rate;
	}
	public String getRate1() {
		return rate1;
	}
	public void setRate1(String rate1) {
		this.rate1 = rate1;
	}
	public String getRate2() {
		return rate2;
	}
	public void setRate2(String rate2) {
		this.rate2 = rate2;
	}
	public String getRate3() {
		return rate3;
	}
	public void setRate3(String rate3) {
		this.rate3 = rate3;
	}
	public String getRate4() {
		return rate4;
	}
	public void setRate4(String rate4) {
		this.rate4 = rate4;
	}
	public String getRate5() {
		return rate5;
	}
	public void setRate5(String rate5) {
		this.rate5 = rate5;
	}
	
	

}
