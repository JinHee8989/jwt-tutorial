package me.jinhee.jwttutorial.service;

import me.jinhee.jwttutorial.dto.UserDto;
import me.jinhee.jwttutorial.entity.Authority;
import me.jinhee.jwttutorial.entity.User;
import me.jinhee.jwttutorial.repository.UserRepository;
import me.jinhee.jwttutorial.utill.SecurityUtil;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.Optional;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    //회원가입
    @Transactional
    public User signup(UserDto userDto){

        //기존 가입고객인지 DB 확인
        if(userRepository.findOnewWithAuthoritiesByUsername(userDto.getUserName()).orElse(null)!=null){
            throw new RuntimeException("이미 가입되어있는 유저입니다.");
        }

        //가입되어있지 않다면 권한정보 생성 후 User정보와 권한정보 저장
        Authority authority = Authority.builder()
                .authorityName("ROLE_USER")
                .build();
        User user = User.builder()
                .username(userDto.getUserName())
                .password(passwordEncoder.encode(userDto.getPassword()))
                .nickname(userDto.getNickName())
                .authorities(Collections.singleton(authority))
                .activated(true)
                .build();
        return userRepository.save(user);
    }



    //--------------- 두 메소드의 허용권한을 다르게 해서 권한검증에 대한 부분 테스트하기 -------------------

    @Transactional(readOnly = true)
    public Optional<User> getUserWithAuthorities(String username){
        return userRepository.findOnewWithAuthoritiesByUsername(username);
    }

    @Transactional(readOnly = true)
    public Optional<User> getMyUserWithAuthorities(){
        return SecurityUtil.getCurrentUsername().flatMap(userRepository::findOnewWithAuthoritiesByUsername);
    }

}
