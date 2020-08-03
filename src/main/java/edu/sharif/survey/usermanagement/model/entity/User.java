package edu.sharif.survey.usermanagement.model.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import edu.sharif.survey.model.ResetRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import javax.persistence.*;


@Data
@Entity
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Table(name = "Users")
@Embeddable
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Integer id;
    private String name;
    //    @Email(message = "Invalid email address.")
    @Column(name = "email", unique = true)
    private String email;
    @JsonIgnore
    private String password;
    private String picture;


    @OneToOne(mappedBy = "user")
    private ResetRequest resetRequest;


    public User(String name, String email, String password) {
        this.name = name;
        this.email = email;
        this.password = password;
        this.picture = null;
    }

}
