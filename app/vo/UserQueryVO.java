package vo;

import java.util.Date;

/**
 * Created by ShenTeng on 2015/3/5.
 */
public class UserQueryVO {
    private String name;
    private String credential;
    private String mobile;
    private String email;
    private String company;
    private Integer status;
    private Integer payStatus;
    private Integer fundStatus;
    private Integer origin;
    private Integer credentialStatus;
    private Integer cardStatus;
    private Date joinDateStart;
    private Date joinDateEnd;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCredential() {
        return credential;
    }

    public void setCredential(String credential) {
        this.credential = credential;
    }

    public String getMobile() {
        return mobile;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getCompany() {
        return company;
    }

    public void setCompany(String company) {
        this.company = company;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public Integer getPayStatus() {
        return payStatus;
    }

    public void setPayStatus(Integer payStatus) {
        this.payStatus = payStatus;
    }

    public Integer getFundStatus() {
        return fundStatus;
    }

    public void setFundStatus(Integer fundStatus) {
        this.fundStatus = fundStatus;
    }

    public Integer getOrigin() {
        return origin;
    }

    public void setOrigin(Integer origin) {
        this.origin = origin;
    }

    public Integer getCredentialStatus() {
        return credentialStatus;
    }

    public void setCredentialStatus(Integer credentialStatus) {
        this.credentialStatus = credentialStatus;
    }

    public Integer getCardStatus() {
        return cardStatus;
    }

    public void setCardStatus(Integer cardStatus) {
        this.cardStatus = cardStatus;
    }

    public Date getJoinDateStart() {
        return joinDateStart;
    }

    public void setJoinDateStart(Date joinDateStart) {
        this.joinDateStart = joinDateStart;
    }

    public Date getJoinDateEnd() {
        return joinDateEnd;
    }

    public void setJoinDateEnd(Date joinDateEnd) {
        this.joinDateEnd = joinDateEnd;
    }
}
