package repositories;

import java.util.List;

import models.Question;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

public interface QuestionInterface extends JpaRepository<Question, Long>,JpaSpecificationExecutor<Question> {

	@Query(value="select * from question where uid=?1",nativeQuery=true)
	public List<Question> getQuestbyUid(Long uid);
}
