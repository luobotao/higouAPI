package services.api;

import java.util.Calendar;
import java.util.Date;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.apache.commons.lang3.StringUtils;

import com.fasterxml.jackson.databind.JsonNode;

import models.certification.Certification;
import models.certification.CertificationError;
import models.certification.CertificationTemp;
import play.Logger;
import repositories.Certification.CertificationErrorRepository;
import repositories.Certification.CertificationRepository;
import repositories.Certification.CertificationTempRepository;
import services.ICacheService;
import services.ServiceFactory;
import utils.Constants;
import utils.Numbers;
import utils.certificate.CertificateUtils;

/**
 * 身份证验证相关Service
 * @author luobotao
 * Date: 2015年4月17日 下午2:26:14
 */
@Named
@Singleton
public class CertificationService {

    private static final Logger.ALogger logger = Logger.of(CertificationService.class);
    private static final java.util.regex.Pattern USERNAME_PATTERN = java.util.regex.Pattern.compile("[\u4E00-\u9FA5]{2,10}(?:·[\u4E00-\u9FA5]{2,10})*");
    private static final java.util.regex.Pattern CERTIFICATION_PATTERN = java.util.regex.Pattern.compile("^(\\d{6})(\\d{4})(\\d{2})(\\d{2})(\\d{3})([0-9]|X)$");
    @Inject
    private CertificationRepository certificationRepository;
    @Inject
    private CertificationErrorRepository certificationErrorRepository;
    @Inject
    private CertificationTempRepository certificationTempRepository;
    
    /**
     * 返回值为0成功，-1验证失败 1uid不存在 2姓名不合法 3身份证不合法 4超过最大次数限制
     * @param uid
     * @param name
     * @param card
     * @return
     */
    public int checkNameWithCard(String uid,String name, String card) {
    	
    	int result = -1;
    	
    	if(StringUtils.isBlank(uid)){
    		return 1;
    	}
    	if (!USERNAME_PATTERN.matcher(name).matches()) {
    		return 2;
    	}
    	if (!CERTIFICATION_PATTERN.matcher(card).matches()) {
    		return 3;
    	}
    	ICacheService cache = ServiceFactory.getCacheService();
    	int times = Numbers.parseInt(cache.get(Constants.CERTIFICATION_TIME_CHECKED+uid), 0);
    	if(times>9){
    		return 4;
    	}
    	CertificationTemp certificationNewTemp = certificationTempRepository.findByUsernameAndCardNo(name, card);
    	if(certificationNewTemp!=null){
    		try {
				Thread.sleep(5000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
    	}else{
    		certificationNewTemp = new CertificationTemp(); 
    		certificationNewTemp.setCardNo(card);
    		certificationNewTemp.setUsername(name);
    		certificationNewTemp.setDateAdd(new Date());
    		certificationNewTemp=certificationTempRepository.save(certificationNewTemp);
    	}
    	CertificationError certificationError = certificationErrorRepository.findByUsernameAndCardNo(name, card);
    	if(certificationError!=null){//说明在错误身份证表里存在
    		Calendar cal = Calendar.getInstance();
			cal.setTime(new Date());
			Long now = cal.getTimeInMillis();
			cal.add(Calendar.DATE, 1);
			cal.set(Calendar.HOUR_OF_DAY, 0);
			cal.set(Calendar.MINUTE, 0);
			cal.set(Calendar.SECOND, 1);
			int sub = (int) ((cal.getTimeInMillis()-now)/1000);
			cache.setWithOutTime(Constants.CERTIFICATION_TIME_CHECKED+uid,String.valueOf(++times),sub);
	    	certificationTempRepository.delete(certificationNewTemp);
			return -1;
    	}
    	Certification certificationTemp = certificationRepository.findByCardNo(card);
    	if(certificationTemp==null){
        	JsonNode jsonResult = CertificateUtils.checkNameWithCard(name, card);
        	logger.info(jsonResult+"=========");
    		if(jsonResult!=null){
    			JsonNode isok = jsonResult.get("isok");
    			if(isok!=null && isok.asInt()==1){// 查询成功
    				JsonNode code = jsonResult.get("code");//查询结果 1一致  2不一致  3无此身份证号码
    				if(code!=null && code.asInt()==1){
    					JsonNode data = jsonResult.get("data");//
    					if(data!=null){
    						Certification certification = new Certification(); 
    						String address = data.get("address")==null?"":data.get("address").asText();
    						String sex = data.get("sex")==null?"":data.get("sex").asText();
    						String birthday = data.get("birthday")==null?"":data.get("birthday").asText();
    						certification.setCardNo(card);
    						certification.setUsername(name);
    						certification.setAddress(address);
    						certification.setSex(sex);
    						certification.setBirthday(birthday);
    						certification.setDateAdd(new Date());
    						certificationRepository.save(certification);
    					}
    					result = 0;
    				}else{//查询不一致
        		    	Calendar cal = Calendar.getInstance();
        				cal.setTime(new Date());
        				Long now = cal.getTimeInMillis();
        				cal.add(Calendar.DATE, 1);
        				cal.set(Calendar.HOUR_OF_DAY, 0);
        				cal.set(Calendar.MINUTE, 0);
        				cal.set(Calendar.SECOND, 1);
        				int sub = (int) ((cal.getTimeInMillis()-now)/1000);
        				cache.setWithOutTime(Constants.CERTIFICATION_TIME_CHECKED+uid,String.valueOf(++times),sub);
        				//查询结果不一致，需要缓存不一致的身份证信息
        				CertificationError certificationErrorNew = new CertificationError(); 
        				certificationErrorNew.setCardNo(card);
        				certificationErrorNew.setUsername(name);
        				certificationErrorNew.setDateAdd(new Date());
        				certificationErrorRepository.save(certificationErrorNew);
    				}
    			}
    		}
    	}else{
    		if(name.equals(certificationTemp.getUsername())){
    			result = 0;
    		}else{//查询不一致
		    	Calendar cal = Calendar.getInstance();
				cal.setTime(new Date());
				Long now = cal.getTimeInMillis();
				cal.add(Calendar.DATE, 1);
				cal.set(Calendar.HOUR_OF_DAY, 0);
				cal.set(Calendar.MINUTE, 0);
				cal.set(Calendar.SECOND, 1);
				int sub = (int) ((cal.getTimeInMillis()-now)/1000);
				cache.setWithOutTime(Constants.CERTIFICATION_TIME_CHECKED+uid,String.valueOf(++times),sub);
    		}
    	}
    	certificationTempRepository.delete(certificationNewTemp);
		return result;
    }
	
	
	
}
