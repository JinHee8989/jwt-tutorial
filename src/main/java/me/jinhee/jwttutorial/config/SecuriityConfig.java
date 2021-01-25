package me.jinhee.jwttutorial.config;

import me.jinhee.jwttutorial.jwt.JwtAccessDeniedHandler;
import me.jinhee.jwttutorial.jwt.JwtAuthenticationEntryPoint;
import me.jinhee.jwttutorial.jwt.JwtSecurityConfig;
import me.jinhee.jwttutorial.jwt.TokenProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;


@EnableWebSecurity      //기본적인 웹보안을 활성화하겠다는 의미
@EnableGlobalMethodSecurity(prePostEnabled = true) //@EnableGlobalMethodSecurity은 특정 메소드엥 권한 처리를 하는 메소드시큐리티 설정 기능 제공,
                                                    //각 설정값을 true로 변경하면 사용가능
                                                    // prePostEnble : @prePostAutorize, @postAuthorize 사용하여 인가처리
                                                    // secureEnable : @Secured 사용하여 인가처리
                                                    // jsr250Enable : @RolesAllowed 사용하여 인가처리
public class SecuriityConfig extends WebSecurityConfigurerAdapter {

    //만들어준 tokenProvider와 오류발생클래스들을 의존성주입해줌
    private final TokenProvider tokenProvider;
    private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;
    private final JwtAccessDeniedHandler jwtAccessDeniedHandler;

    public SecuriityConfig(TokenProvider tokenProvider, JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint, JwtAccessDeniedHandler jwtAccessDeniedHandler) {
        this.tokenProvider = tokenProvider;
        this.jwtAuthenticationEntryPoint = jwtAuthenticationEntryPoint;
        this.jwtAccessDeniedHandler = jwtAccessDeniedHandler;
    }

    @Bean
    public PasswordEncoder passwordEncoder(){
        return new BCryptPasswordEncoder();
    }


    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http    .csrf().disable()       //token방삭을 할꺼라 csrf()를 disable()해줌
                                        /** csrf란 웹사이트의 취약점을 이용해 이용자가 의도하지 않은 요청을 통한 공격을 말한다.
                                        **  http 통신의 Stateless 특성을 이용하여 쿠키 정보만 이용해서 사용자가 의도하지 않은 다양한 공격들을 시도할 수 있음.
                                         * 해당 웹 사이트에 로그인한 상태로  https://xxxx.com/logout URL을 호출하게 유도하면 실제 사용자는 의도하지 않은 로그아웃을 요청하게 됨.
                                         * 실제로 로그아웃뿐만 아니라 다른 웹 호출도 가능하게 되기 때문에 보안상 위험.
                                         * 가장 간단한 해결책으로는 CSRF Token 정보를 Header 정보에 포함하여 서버 요청을 시도하는 것
                                         **/

                .exceptionHandling()    //exception이 발생하면 authenticationEntryPoint와 accessDeniedHandler를 쓴다는 것
                .authenticationEntryPoint(jwtAuthenticationEntryPoint)
                .accessDeniedHandler(jwtAccessDeniedHandler)

                .and()  //h2-console을 위한 설정
                .headers()
                .frameOptions()
                .sameOrigin()

                .and()  //세션설정. 여기선 세션을 사용안하므로 STATELESS로 설정
                .sessionManagement()
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS) //stateless : server side에 client와 server의 동작, 상태정보를 저장하지 않는 형태

                .and()
                .authorizeRequests()    //토큰이 없어도 접근이 가능하도록 설정
                .antMatchers("/api/hello").permitAll()
                .antMatchers("/api/authenticate").permitAll()
                .antMatchers("/api/signup").permitAll()
                .anyRequest().authenticated()

                .and()
                .apply(new JwtSecurityConfig(tokenProvider));   // JwtFilter를 addFilterBefore로 등록했던
                                                                // JwtSecurityConfig클래스를 적용해줌
    }

    @Override
    public void configure(WebSecurity web){
        web.ignoring()
                .antMatchers("/h2-console/**","/favicon.ico");
    }

}
