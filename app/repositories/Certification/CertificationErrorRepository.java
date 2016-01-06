package repositories.Certification;

import javax.inject.Named;
import javax.inject.Singleton;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import models.certification.Certification;
import models.certification.CertificationError;

@Named
@Singleton
public interface CertificationErrorRepository extends JpaRepository<CertificationError, Long>,JpaSpecificationExecutor<CertificationError> {

	
	/**
	 * 根据身份证号去库里查询是否存在
	 * @param card
	 * @return
	 */
	CertificationError findByUsernameAndCardNo(String username,String card);
	

	
	
}