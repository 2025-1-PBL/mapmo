package com.pbl.mapmo.jwt;

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

/**
 * JWT 토큰 생성 및 검증을 담당하는 클래스
 * 인증 정보를 기반으로 JWT 토큰을 생성하고, 토큰에서 인증 정보를 추출하는 기능을 제공합니다.
 * InitializingBean을 구현하여 빈 초기화 시 비밀 키를 설정합니다.
 */
@Component
public class TokenProvider implements InitializingBean {
    // 로깅을 위한 Logger 객체 생성
    private final Logger logger = LoggerFactory.getLogger(TokenProvider.class);

    /**
     * JWT 토큰의 권한 정보를 담는 클레임 키
     * 사용자의 권한 정보가 이 키의 값으로 저장됩니다.
     */
    private static final String AUTHORITIES_KEY = "auth"; // auth 키. 사용자의 권한 정보 auth 키의 claim으로 저장.

    /**
     * JWT 서명에 사용할 비밀 키
     * application.properties 또는 application.yml에서 설정 값을 주입받습니다.
     */
    private final String secret;

    /**
     * JWT 토큰의 유효 시간(밀리초 단위)
     * application.properties 또는 application.yml에서 설정 값을 주입받습니다.
     */
    private final long tokenValidityInMilliseconds; //JWT 유효 시간(초 단위 -> 밀리초로)

    /**
     * JWT 서명에 사용할 키 객체
     * 비밀 키 문자열을 기반으로 생성됩니다.
     */
    private Key key; // JWT 서명용 HMAC 키 객체

    /**
     * 생성자: 설정 값을 주입받아 초기화합니다.
     *
     * @param secret JWT 서명용 비밀 키
     * @param tokenValidityInSeconds JWT 토큰 유효 시간(초 단위)
     */
    public TokenProvider( // 비밀 키 초기화 및 jwt 유효 시간 밀리초 변환
            @Value("${jwt.secret}") String secret,
            @Value("${jwt.token-validity-in-seconds}") long tokenValidityInSeconds) {
        this.secret = secret;
        this.tokenValidityInMilliseconds = tokenValidityInSeconds * 1000;
    }

    /**
     * 빈 초기화 시 비밀 키 문자열을 기반으로 서명용 키 객체를 생성합니다.
     */
    @Override
    public void afterPropertiesSet() { // 생성자에서 받은 secret을 base64 디코딩, 디코딩된 바이트 배열을 HMAC-SHA 알고리즘용 암호화 키 생성, 생성된 키는 토큰 서명 및 검증에 사용.
        byte[] keyBytes = Decoders.BASE64.decode(secret);
        this.key = Keys.hmacShaKeyFor(keyBytes);
    }

    /**
     * 인증 정보를 기반으로 액세스 토큰을 생성합니다.
     *
     * @param authentication 사용자 인증 정보
     * @return 생성된 JWT 액세스 토큰
     */
    public String createToken(Authentication authentication) { // 현재 로그인한 사용자 정보 기반으로 jwt 생성, authentication.getAuthorities() 에서 권한 추출, claim에 저장. 유효 기간 생성 후 jwt 반환.
        String authorities = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.joining(","));

        long now = (new Date()).getTime();
        Date validity = new Date(now + this.tokenValidityInMilliseconds);

        return Jwts.builder() // 토큰 빌더 생성
                .setSubject(authentication.getName()) // 사용자 이름(ID)을 토큰의 주체로 설정
                .claim(AUTHORITIES_KEY, authorities) // 사용자 권한 정보
                .signWith(key, SignatureAlgorithm.HS512) // 이전에 생성한 키와 HS512 알고리즘을 사용하여 토큰에 서명
                .setExpiration(validity) // 토큰 만료 시간 설정
                .compact(); // 토큰 반환.
    }

    /**
     * JWT 토큰에서 인증 정보를 추출합니다.
     *
     * @param token JWT 토큰
     * @return Spring Security 인증 객체(Authentication)
     */
    public Authentication getAuthentication(String token) { // 토큰 파싱, Authentication 객체로 변환. 토큰에서 인증 정보 추출.
        Claims claims = Jwts
                .parserBuilder() // jwt 파서 생성
                .setSigningKey(key) // 토큰 검증에 사용할 서명 키 설정
                .build()
                .parseClaimsJws(token) // 토큰 파싱, 검증.
                .getBody(); // 토큰의 본문 claim(사용자 권한) 추출.

        Collection<? extends GrantedAuthority> authorities =
                Arrays.stream(claims.get(AUTHORITIES_KEY).toString().split(","))
                        .map(SimpleGrantedAuthority::new)
                        .collect(Collectors.toList());

        User principal = new User(claims.getSubject(), "", authorities); // usernmae 매개변수로 subject 클레임 사용, password 토큰 기반 인증에서 필요없음, 세 번째 매개변수 권한 컬렉션 사용.

        return new UsernamePasswordAuthenticationToken(principal, token, authorities);
    }

    /**
     * JWT 토큰의 유효성을 검증합니다.
     *
     * @param token 검증할 JWT 토큰
     * @return 토큰 유효 여부(true/false)
     */
    public boolean validateToken(String token) { // 토큰 유효성 검증.
        try {
            Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token); // jwt 파서 빌더 생성, 검증에 사용할 서명 키 설정, build로 파서 생성, 토큰 파싱하고 검증.
            return true;
        } catch (io.jsonwebtoken.security.SecurityException | MalformedJwtException e) {
            logger.info("잘못된 JWT 서명입니다."); // 토큰 서명이 유효하지 않거나 토큰 형식이 잘못된 경우
        } catch (ExpiredJwtException e) {
            logger.info("만료된 JWT 토큰입니다.");
        } catch (UnsupportedJwtException e) {
            logger.info("지원되지 않는 JWT 토큰입니다.");
        } catch (IllegalArgumentException e) {
            logger.info("JWT 토큰이 잘못되었습니다."); // 토큰이 null이거나 잘못된 인자가 전달된 경우
        }
        return false;
    }

    /**
     * 인증 정보를 기반으로 리프레시 토큰을 생성합니다.
     *
     * @param authentication 사용자 인증 정보
     * @return 생성된 JWT 리프레시 토큰
     */
    public String createRefreshToken(Authentication authentication) {
        // 리프레시 토큰 생성 로직 (액세스 토큰보다 긴 유효 기간)
        long refreshTokenValidityInMilliseconds = 14 * 24 * 60 * 60 * 1000; // 14일

        Date validity = new Date(System.currentTimeMillis() + refreshTokenValidityInMilliseconds);

        return Jwts.builder()
                .setSubject(authentication.getName())
                .signWith(key, SignatureAlgorithm.HS512)
                .setExpiration(validity)
                .compact();
    }

    /**
     * JWT 토큰에서 사용자명을 추출합니다.
     *
     * @param token JWT 토큰
     * @return 사용자명(username)
     */
    public String getUsernameFromToken(String token) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();

        return claims.getSubject();
    }
}