package edu.sharif.survey.domain;


import edu.sharif.survey.usermanagement.model.entity.User;
import lombok.Data;

@Data
public class Subscription {

    private User user;

    public String toString(){
        return user.getEmail();
    }
}
