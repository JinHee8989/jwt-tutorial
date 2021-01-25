package me.jinhee.jwttutorial.jwt;

import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.stream.Collectors;

@Component //빈 생성
public class TokenProvider implements InitializingBean {
    private final Logger logger = LoggerFactory.getLogger(TokenProvider.class);

    private static final  String AUTHORITIES_KEY="auth";

    private final String secret;
    private final long tokenValidityInMlliseconds;
    private Key key;

    public TokenProvider(   //빈 주입
                            @Value("${jwt.secret}") String secret,
                            @Value("${jwt.token-validity-in-seconds}") long tokenValidityInSeconds) {
        this.secret = secret;
        this.tokenValidityInMlliseconds = tokenValidityInSeconds * 1000;
    }


    @Override
    public void afterPropertiesSet() throws Exception {
        byte[] keyBytes = Decoders.BASE64.decode(secret); //secret값을 BASE64 Decode
        this.key= Keys.hmacShaKeyFor(keyBytes); //디코드 해준 값을 key변수에 할당
    }


    //토큰 생성 메소드
    public String createToken(Authentication authentication){   //Authenticccation객체의 권한정보를 이용해 토큰을 생성
        String authorities = authentication.getAuthorities().stream()
                            .map(GrantedAuthority::getAuthority)
                            .collect(Collectors.joining(","));
        long now = (new Date()).getTime();
        Date validity = new Date(now + this.tokenValidityInMlliseconds); //application.yml에서 설정해준 유효시간과 현재시간을 더해서 만료시간을 변수에 할당

        return Jwts.builder()
                .setSubject(authentication.getName())
                .claim(AUTHORITIES_KEY, authorities)
                .signWith(key, SignatureAlgorithm.ES512)    //실행할 알고리즘. 여기선 ES512로 진행
                .setExpiration(validity)        //만료시간
                .compact();
    }

    //토큰을 이용해 Authentication 객체를 반환
    public Authentication getAuthentication(String token){
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)  //받은 토큰으로 클레이을 만들어줌
                .getBody();

        Collection<? extends  GrantedAuthority> authorities =
                Arrays.stream(claims.get(AUTHORITIES_KEY).toString().split(","))
                        .map(SimpleGrantedAuthority::new)
                        .collect(Collectors.toList());

        User principal = new User(claims.getSubject(),"",authorities);  //클레임에서 권한정보를 빼내 이용해 유저객체(springframework의 security에서 제공하는 유저객체)를 만듬

        return new UsernamePasswordAuthenticationToken(principal, token, authorities);  //유저객체, 토큰, 권한정보를 이용해 Authentication 객체를 반환

    }

    //토큰의 유효성 검사
    public boolean validateToken(String token){
        try{
            Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token);
            return true;
        }catch (io.jsonwebtoken.security.SecurityException| MalformedJwtException e){
            logger.info("잘못된 JWT서명입니다.");
        }catch (ExpiredJwtException e){
            logger.info("만료된 JWT토큰입니다.");
        }catch (UnsupportedJwtException e){
            logger.info("지원하지 않는 JWT토큰입니다.");
        }catch (IllegalArgumentException e){
            logger.info("JWT토큰이 잘못되었습니다.");
        }

        return false;   //문제가 있으면 false, 정상이면 true;
    }



}
