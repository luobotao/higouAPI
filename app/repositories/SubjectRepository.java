package repositories;

import javax.inject.Named;
import javax.inject.Singleton;

import models.Subject;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

@Named
@Singleton
public interface SubjectRepository extends JpaRepository<Subject, Long>,JpaSpecificationExecutor<Subject> {
	
	
}