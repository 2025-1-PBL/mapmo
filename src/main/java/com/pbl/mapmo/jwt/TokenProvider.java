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

@Component
public class TokenProvider implements InitializingBean {

    private final Logger logger = LoggerFactory.getLogger(TokenProvider.class);

    private static final String AUTHORITIES_KEY = "auth"; // auth 키. 사용자의 권한 정보 auth 키의 claim으로 저장.

    private final String secret;
    private final long tokenValidityInMilliseconds; //JWT 유효 시간(초 단위 -> 밀리초로)

    private Key key; // JWT 서명용 HMAC 키 객체

    public TokenProvider( // 비밀 키 초기화 및 jwt 유효 시간 밀리초 변환
            @Value("${jwt.secret}") String secret,
            @Value("${jwt.token-validity-in-seconds}") long tokenValidityInSeconds) {
        this.secret = secret;
        this.tokenValidityInMilliseconds = tokenValidityInSeconds * 1000;
    }

    @Override
    public void afterPropertiesSet() { // 생성자에서 받은 secret을 base64 디코딩, 디코딩된 바이트 배열을 HMAC-SHA 알고리즘용 암호화 키 생성, 생성된 키는 토큰 서명 및 검증에 사용.
        byte[] keyBytes = Decoders.BASE64.decode(secret);
        this.key = Keys.hmacShaKeyFor(keyBytes);
    }

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

    public String getUsernameFromToken(String token) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();

        return claims.getSubject();
    }
}