package edu.sharif.survey.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.rest.core.annotation.RestResource;


@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserDomain {
    private String name;
    private String email;
    @RestResource(exported = false)
    private String picture;
}
