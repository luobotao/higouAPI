package repositories;

import models.CountH5;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface CountH5Interface extends JpaRepository<CountH5, Long>,JpaSpecificationExecutor<CountH5> {

}
