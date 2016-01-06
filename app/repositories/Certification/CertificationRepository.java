package repositories.Certification;

import javax.inject.Named;
import javax.inject.Singleton;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import models.certification.Certification;

@Named
@Singleton
public interface CertificationRepository extends JpaRepository<Certification, Long>,JpaSpecificationExecutor<Certification> {

	
	/**
	 * 根据身份证号去库里查询是否存在
	 * @param card
	 * @return
	 */
	Certification findByCardNo(String card);
	

	
	
}