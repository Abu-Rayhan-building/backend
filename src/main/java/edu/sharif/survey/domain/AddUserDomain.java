package edu.sharif.survey.domain;

import lombok.Data;

@Data
public class AddUserDomain {
    private String name;
    private String email;
    private String password;
}
