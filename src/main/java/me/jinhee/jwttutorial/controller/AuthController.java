package me.jinhee.jwttutorial.controller;

import me.jinhee.jwttutorial.dto.LoginDto;
import me.jinhee.jwttutorial.dto.TokenDto;
import me.jinhee.jwttutorial.jwt.JwtFilter;
import me.jinhee.jwttutorial.jwt.TokenProvider;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

@RestController
@RequestMapping("/api")
public class AuthController {
    private final TokenProvider tokenProvider;
    private final AuthenticationManagerBuilder authenticationManagerBuilder;

    public AuthController(TokenProvider tokenProvider, AuthenticationManagerBuilder authenticationManagerBuilder) {
        this.tokenProvider = tokenProvider;
        this.authenticationManagerBuilder = authenticationManagerBuilder;
    }

    @PostMapping("/authenticate")
    public ResponseEntity<TokenDto> authorize(@Valid @RequestBody LoginDto loginDto){

        //로그인한 유저의 정보로 UsernamePasswordAuthenticationToken을 생성
        UsernamePasswordAuthenticationToken authenticationToken =
                new UsernamePasswordAuthenticationToken(loginDto.getUsername(), loginDto.getPassword());

        //authenticate()에서 CustomerDetailService의 loadUserByUsername메소드가 실행되고 authentication객체를 생성하게 됨
        Authentication authentication = authenticationManagerBuilder.getObject().authenticate(authenticationToken);

        //이 생성된 객체를 SecurityContext에 저장 후 JWT token을 생성
        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = tokenProvider.createToken(authentication);

        //JWT token을 Response Heder에 넣어줌
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.add(JwtFilter.AUTHORIZSTION_HEADER, "Bearer" + jwt);

        //TokenDto를 이용해서 Response Body에도 넣어서 리턴함
        return new ResponseEntity<>(new TokenDto(jwt),httpHeaders, HttpStatus.OK);
    }
}
