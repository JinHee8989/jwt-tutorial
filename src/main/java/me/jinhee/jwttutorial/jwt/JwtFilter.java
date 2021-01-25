package me.jinhee.jwttutorial.jwt;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.GenericFilterBean;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

public class JwtFilter extends GenericFilterBean {

    private static final Logger logger = LoggerFactory.getLogger(JwtFilter.class);

    public static final String AUTHORIZSTION_HEADER = "Authorization";

    private TokenProvider tokenProvider;

    public JwtFilter(TokenProvider tokenProvider){  //jwtFilter는 tokenProvider를 주입받음
        this.tokenProvider = tokenProvider;
    }

    @Override   //실제 필터링 로직은 여기에  들어가게됨. jwt 토큰 정보를 SecutiryContext에 저장하는 역할
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest httpServletRequest = (HttpServletRequest)request;
        String jwt = resolveToken(httpServletRequest);
        String requestURI = httpServletRequest.getRequestURI();

        if(StringUtils.hasText(jwt) && tokenProvider.validateToken(jwt)){   //토큰이 정상적이면(유효성 검증)
            Authentication authentication = tokenProvider.getAuthentication(jwt);   //토큰에서 authentication객체를 받아옴
            SecurityContextHolder.getContext().setAuthentication(authentication);   // securityContext에 저장해줌
            logger.debug("SEcurity  Context에 '{}'인증 정보를 저장했습니다. uri: {}", authentication.getName(), requestURI);
        }else{
            logger.debug("유효한 JWT 토큰이 없습니다, uri: {}", requestURI);
        }

        filterChain.doFilter(request,response);

    }

    //Request Header에서 토큰 정보를 꺼내오기위한 메소드
    private String resolveToken(HttpServletRequest request){
        String bearerToken = request.getHeader(AUTHORIZSTION_HEADER);
        if(StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")){
            return bearerToken.substring(7);
        }
        return null;
    }
}
