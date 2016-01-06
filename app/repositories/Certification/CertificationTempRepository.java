package repositories.Certification;

import javax.inject.Named;
import javax.inject.Singleton;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import models.certification.CertificationTemp;

@Named
@Singleton
public interface CertificationTempRepository extends JpaRepository<CertificationTemp, Long>,JpaSpecificationExecutor<CertificationTemp> {

	
	/**
	 * 根据身份证号去身份证临时表里查询是否存在
	 * @param card
	 * @return
	 */
	CertificationTemp findByUsernameAndCardNo(String username,String card);
	

	
	
}