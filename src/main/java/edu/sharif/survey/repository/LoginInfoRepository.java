package edu.sharif.survey.repository;

import edu.sharif.survey.model.LoginInfo;
import org.springframework.data.repository.CrudRepository;

public interface LoginInfoRepository extends CrudRepository<LoginInfo, Integer> {
}
