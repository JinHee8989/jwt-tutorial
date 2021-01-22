package me.jinhee.jwttutorial.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;

import javax.persistence.*;
import java.util.Set;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity         //테이블과 1:1 매핑될때 @Entity씀
@Table(name="user")
public class User {

    @JsonIgnore
    @Id
    @Column(name="user_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long userId;

    @Column(name="user_name", length = 50, unique=true)
    private String username;

    @JsonIgnore
    @Column(name="password", length=100)
    private String password;

    @Column(name="nick_name", length=50)
    private String nickname;

    @JsonIgnore
    private boolean activated;

    @ManyToMany         //@ManyToMany와 @joinTable은  다대다관계를 일대다, 다대일 관계로  만들겠다는 것
    @JoinTable(name="user_authority",
                joinColumns = {@JoinColumn(name="user_id", referencedColumnName = "user_id")},
                inverseJoinColumns = {@JoinColumn(name="authority_name", referencedColumnName = "authority_name")})
    private Set<Authority> authorities;






}
