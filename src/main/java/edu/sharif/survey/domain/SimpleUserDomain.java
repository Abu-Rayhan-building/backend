package edu.sharif.survey.domain;


import lombok.Data;

@Data
public class SimpleUserDomain {
    private String name;
    private String email;

    public SimpleUserDomain(String name,String email){
        this.name = name;
        this.email = email;
    }
}
