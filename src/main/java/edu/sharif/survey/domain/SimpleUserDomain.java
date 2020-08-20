package edu.sharif.survey.domain;


import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class SimpleUserDomain {
    private String name;
    private String email;
}
