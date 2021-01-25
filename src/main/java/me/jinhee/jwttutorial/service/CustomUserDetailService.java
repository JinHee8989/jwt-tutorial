package me.jinhee.jwttutorial.service;

import me.jinhee.jwttutorial.entity.User;
import me.jinhee.jwttutorial.repository.UserRepository;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Component("userDetailsService")
public class CustomUserDetailService implements UserDetailsService {

    private final UserRepository userRepository;

    public CustomUserDetailService(UserRepository userRepository){
        this.userRepository = userRepository;
    }

    @Override
    @Transactional  //로그인할때 유저정보랑 권한을 데이터베이스에서 가져와 User객체를 리턴하는 메소드
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return userRepository.findOnewWithAuthoritiesByUsername(username)
                .map(user -> createUser(username,user))
                .orElseThrow(()-> new UsernameNotFoundException(username + " -> 데이터베이스에서 찾을 수 없습니다."));
    }


    private org.springframework.security.core.userdetails.User createUser(String username, User user) {
        if(!user.isActivated()){
            throw new RuntimeException(username+" -> 활성화 되어 있지 않습니다.");
        }
        List<GrantedAuthority> grantedAuthorities = user.getAuthorities().stream()
                .map(authority -> new SimpleGrantedAuthority(authority.getAuthorityName()))
                .collect(Collectors.toList());
        return new org.springframework.security.core.userdetails.User(user.getUsername(),user.getPassword(),grantedAuthorities);
    }
}
