package services;

import java.sql.ResultSet;
import java.util.Date;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import models.SmsInfo;

import org.apache.log4j.Logger;

import repositories.SmsRepository;
import utils.JdbcOper;

@Named
@Singleton
public class SmsService extends Thread{
	private Logger logger = Logger.getLogger(SmsService.class);
	
    @Inject
    private SmsRepository smsRepository;
    
    
	public void getVerify(String phone,String code) {
		String args="#code#="+code;
		String tpl_id = "776385";
		saveSmsInfo(args, phone, tpl_id,"1");
	}
	
	/**
	 * 王 叔叔短信验证码
	 * @param phone
	 * @param code
	 */
	public void getNewVerify(String phone,String code) {
		String args="#code#="+code;
		String tpl_id = "896135";
		saveSmsInfo(args, phone, tpl_id,"1");
	}
	
	public void getVoiceVerify(String phone,String code) {
		String args=code;
		String tpl_id = "";
		saveSmsInfo(args, phone, tpl_id,"3");
	}
	
	
	
	public SmsInfo saveSmsInfo(String args,String phone,String tpl_id,String type){
		logger.info("args is "+args+ "; Phone is "+phone+";tpl_id is "+tpl_id);
		SmsInfo smsInfo = new SmsInfo();
		smsInfo.setFlag("0");//未发送
		smsInfo.setTpl_id(tpl_id);
		smsInfo.setPhone(phone);
		smsInfo.setArgs(args);
		smsInfo.setType(type);//1普通短信 2营销短信 3语音短信
		smsInfo.setInsertTime(new Date());
		smsInfo.setUpdateTime(new Date());
		return smsRepository.save(smsInfo);
		
	}
	
	public String getsmscode(String phone){
		String sql="SELECT * FROM userVerify WHERE phone='"+phone+"' AND DATE_ADD>=DATE_ADD(NOW(),INTERVAL -5 MINUTE) and flg=1";
		JdbcOper db = JdbcOper.getInstance();// 创建DBHelper对象
		String smsverify="";
		try {
			db.getPrepareStateDao(sql);
			ResultSet rs = db.pst.executeQuery();
			if(rs.next()){
				smsverify=rs.getString("verify");
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			db.close();
		}
		return smsverify;
	}
	
	/*
	 * 判断同一手机号某一时间内发送验证次数
	 */
	public Integer getVerifynum(String phone,Integer minits){
		String sql="SELECT count(1) as count FROM userVerify WHERE phone='"+phone+"' AND DATE_ADD>=DATE_ADD(NOW(),INTERVAL -"+minits+" MINUTE)";
		JdbcOper db = JdbcOper.getInstance();// 创建DBHelper对象
		Integer count=0;
		try {
			db.getPrepareStateDao(sql);
			ResultSet rs = db.pst.executeQuery();
			if(rs.next()){
				count=rs.getInt("count");
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			db.close();
		}
		return count;
	}
}