package kr.legossol.sampleSecurity.jwt;

import org.springframework.security.config.annotation.SecurityConfigurerAdapter;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.DefaultSecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

//jwtFilter.java와 jwtTokenProvider를 securityconfig에 적용할때 사용하는 파일
public class JwtSecurityConfig extends SecurityConfigurerAdapter<DefaultSecurityFilterChain, HttpSecurity> {

    private JwtTokenProvider jwtTokenProvider;

    public JwtSecurityConfig(JwtTokenProvider jwtTokenProvider){
        this.jwtTokenProvider = jwtTokenProvider;
    }

    /**
     * jwtfilter를 통해 security 로직에 필터로 등록하는 역할
     * @param httpSecurity
     */
    @Override
    public void configure(HttpSecurity httpSecurity){
        JwtFilter customFilter = new JwtFilter(jwtTokenProvider);
        httpSecurity.addFilterBefore(customFilter, UsernamePasswordAuthenticationFilter.class);
    }


}
