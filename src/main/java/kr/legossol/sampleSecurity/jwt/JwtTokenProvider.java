package kr.legossol.sampleSecurity.jwt;


import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import java.security.Key;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.stream.Collectors;
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

@Component
public class JwtTokenProvider implements InitializingBean {

    private final Logger logger = LoggerFactory.getLogger(JwtTokenProvider.class);

    private static final String AUTHORITIES_KEY = "auth";

    private final String secret;
    private final long tokenValidityInMilliseconds;

    private Key key;

    public JwtTokenProvider(@Value("${jwt.secret}") String secret,
                            @Value("${jwt.token-validity-in-seconds}") long tokenValidityInMilliseconds){
        this.secret = secret;
        this.tokenValidityInMilliseconds = tokenValidityInMilliseconds * 1000;
    }
    /**
     * InitializingBean을 임플리먼트해서 afterPropertiesset을 오버라이딩 한것이다
     * 그 이유는 bean이 생성이 되고 의존성도 주입받은 후에 secret값을 base64 decode해서 key 변수에 할당하기 위함니다.
     * */
    @Override
    public void afterPropertiesSet(){
        byte[] keyBytes = Decoders.BASE64.decode(secret);
        this.key = Keys.hmacShaKeyFor(keyBytes);
    }

    /**
     * Authentication객체에 포함되어 있 권한정보를 담 토큰을 생성하는 createToken메소드
     * @param authentication
     * @return
     */
    public String createToken(Authentication authentication){
        String authorities = authentication.getAuthorities().stream()
            .map(GrantedAuthority::getAuthority)
            .collect(Collectors.joining(","));

        long now = (new Date()).getTime();
        Date validity = new Date(now + this.tokenValidityInMilliseconds);

        return Jwts.builder()
            .setSubject(authentication.getName())
            .claim(AUTHORITIES_KEY,authorities)
            .signWith(key, SignatureAlgorithm.HS512)
            .setExpiration(validity)
            .compact();
    }

    /**
     * 위와는 반대로 token을 파라미터로 받아서 토큰에 담겨있 정보(권한)
     * 을 이용해서 Authentication 객체를 리턴하는 메소드 작성
     * @param token
     * @return
     */
    public Authentication getAuthentication(String token){
        //토큰을 이용해서 claims를 만듦
        Claims claims = Jwts.parser()
            .setSigningKey(key)
            .parseClaimsJws(token)
            .getBody();
        //claim에서 권한 정보를 꺼내서 유저객체를 만들어주고 최종적으로 Authentication 객체를 return
        Collection<? extends GrantedAuthority> authorities =
            Arrays.stream(claims.get(AUTHORITIES_KEY).toString().split(","))
            .map(SimpleGrantedAuthority::new)
            .collect(Collectors.toList());

        User principal = new User(claims.getSubject(),"",authorities);//이 user는 security.core.userdetails에 있는 것

        return new UsernamePasswordAuthenticationToken(principal, token,authorities);
    }

    /**
     * 토큰을 파라미터로 받아서 토큰의 유효성 검
     * @param token
     * @return
     */
    public boolean validateToken(String token){
        try{
            Jwts.parser().setSigningKey(key).parseClaimsJws(token);
            return true;
        }catch (io.jsonwebtoken.security.SecurityException| MalformedJwtException e){
            logger.info("잘못된 JWT서명입니다.");
        }catch (ExpiredJwtException e){
            logger.info("만료된 JWT 토큰입니다");
        }catch (UnsupportedJwtException e){
            logger.info("지원되지 않는 JWT 토큰입니다.");
        }catch (IllegalArgumentException e){
            logger.info("JWT 토큰이 잘못되었습니다.");
        }
        return false;
    }

}
