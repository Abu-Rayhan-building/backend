package edu.sharif.survey.repository;

import edu.sharif.survey.model.ResetRequest;
import edu.sharif.survey.usermanagement.model.entity.User;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;


@Repository
public interface ResetRequestRepository extends CrudRepository<ResetRequest, Integer> {


    Optional<ResetRequest> findByUser(User user);

    Optional<ResetRequest> findByToken(String token);
}
