package kr.legossol.sampleSecurity.jwt;

import java.io.IOException;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.GenericFilterBean;


public class JwtFilter extends GenericFilterBean {

    private static final Logger logger = LoggerFactory.getLogger(JwtFilter.class);
    public static final String AUTHORIZATION_HEADER = "Authorization";

    private JwtTokenProvider jwtTokenProvider;

    public JwtFilter(JwtTokenProvider jwtTokenProvider){
        this.jwtTokenProvider = jwtTokenProvider;
    }

    /**
     * jwt 토큰의 인증정보를 현재의 실행중인 시큐리티 컨텍스트에 저장하는 역할
     * @param request
     * @param response
     * @param chain
     * @throws IOException
     * @throws ServletException
     */
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
        throws IOException, ServletException {
        HttpServletRequest httpServletRequest = (HttpServletRequest) request;
        String jwt = resolveToken(httpServletRequest);//리퀘스트에서 토큰을 받아서 jwt에 담는
        String requestURI = httpServletRequest.getRequestURI();

        if(StringUtils.hasText(jwt) && jwtTokenProvider.validateToken(jwt)){//2 : 받아온 토큰의 유효성 검사진
            Authentication authentication = jwtTokenProvider.getAuthentication(jwt);//3 : 토큰이 정상이면 authentication객체를 받아
            SecurityContextHolder.getContext().setAuthentication(authentication);//4 : securityContext에 set 해준다
            logger.debug("Security Context에 '{}' 인증 정보를 저장했습니다, uri : {}",authentication.getName(), requestURI);
        }else{
            logger.debug("유효한 JWT 토큰이 없습니다, uri:{}",requestURI);
        }

    }
    //필터링위해 토큰정보가 있어야함
    private String resolveToken(HttpServletRequest request){
        String bearerToken = request.getHeader(AUTHORIZATION_HEADER);
        if(StringUtils.hasText(bearerToken)&&bearerToken.startsWith("bearer ")){
            return bearerToken.substring(7);
        }
        return null;
    }
}
