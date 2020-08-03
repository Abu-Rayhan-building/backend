package edu.sharif.survey.usermanagement.dao;

import edu.sharif.survey.usermanagement.model.entity.User;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserDao extends CrudRepository<User, Integer> {

    Optional<User> findByEmail(String email);

    void deleteByEmail(String email);


}
