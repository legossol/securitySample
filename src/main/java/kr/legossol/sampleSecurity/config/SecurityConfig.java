package kr.legossol.sampleSecurity.config;

import kr.legossol.sampleSecurity.jwt.JwtAccessDeniedHandler;
import kr.legossol.sampleSecurity.jwt.JwtAuthenticationEntryPoint;
import kr.legossol.sampleSecurity.jwt.JwtSecurityConfig;
import kr.legossol.sampleSecurity.jwt.JwtTokenProvider;
import lombok.extern.log4j.Log4j2;
import org.springframework.context.annotation.Bean;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * 기본적인 시큐리티 설정을 위한 클래스
 * 기본적인 웹 보안을 활성화하겠다는 어노테이션
 * 추가적인 설정을 위해선 websecurityconfigurer을 implement하거나
 * jwt에 있는 모든 것을 가진다.
 * websecurityconfigureradapter를 extends하는 방법이 있다.
 * */
@Log4j2
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)//나중에 preAuthorize어노테이션을 메소드 단위로 사용하기위해
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    private final JwtTokenProvider jwtTokenProvider;
    private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;
    private final JwtAccessDeniedHandler jwtAccessDeniedHandler;

    //오버로딩하여 h2디비에 접속하기 위한 것.
    @Override
    public void configure(WebSecurity web){
        web.ignoring().antMatchers("/**");
//            .ignoring()
//            .antMatchers(
//                "/h2-console/**",
//                "/favicon.ico"
//            );

    }

    //시큐리티 컨피규 빈 ( 토큰 생성자와 entrypoint, handler를 주입)
   public SecurityConfig(
        JwtTokenProvider jwtTokenProvider,
        JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint,
        JwtAccessDeniedHandler jwtAccessDeniedHandler
    ){
        this.jwtTokenProvider = jwtTokenProvider;
        this.jwtAuthenticationEntryPoint = jwtAuthenticationEntryPoint;
        this.jwtAccessDeniedHandler = jwtAccessDeniedHandler;
    }

    @Bean
    public PasswordEncoder passwordEncoder(){
        return new BCryptPasswordEncoder();
    }

    @Override
    protected void configure(HttpSecurity httpSecurity) throws Exception{
        httpSecurity
            .csrf().disable()
            //토큰 방식을 사용하면 csrf disable을 해줘야한다
            //그대신 아래 handler를 사용하는 것

            .exceptionHandling()
            .authenticationEntryPoint(jwtAuthenticationEntryPoint)
            .accessDeniedHandler(jwtAccessDeniedHandler)

            //h2 설정을 위한 설정
//            .and()
//            .headers()
//            .frameOptions()
//            .sameOrigin()

            //세션 사용을하지 않기때문에 stateless
            .and()
            .sessionManagement()
            .sessionCreationPolicy(SessionCreationPolicy.STATELESS)

            .and()
            .authorizeRequests()//http servletrequest를 사용하는 요청들에 대해서 접근 제한을 설정하겠다
//            .antMatchers("/api/hello").permitAll()
            .antMatchers("/api/authenticate").permitAll()
            .antMatchers("/api/signup").permitAll()
            .anyRequest().authenticated()//그외에는 허용 승인을 받아야한다.

            .and()
            .apply(new JwtSecurityConfig(jwtTokenProvider));
    }
}
