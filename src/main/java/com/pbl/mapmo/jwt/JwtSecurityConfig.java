package com.pbl.mapmo.jwt;

import org.springframework.security.config.annotation.SecurityConfigurerAdapter;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.DefaultSecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.context.AbstractSecurityWebApplicationInitializer;

/**
 * JWT 보안 설정을 위한 클래스
 * Spring Security 설정에 JWT 관련 필터를 추가하는 역할을 담당합니다.
 * SecurityConfigurerAdapter를 상속받아 HTTP 보안 설정을 커스터마이징합니다.
 */
public class JwtSecurityConfig extends SecurityConfigurerAdapter<DefaultSecurityFilterChain, HttpSecurity> {

    /**
     * JWT 토큰 생성, 검증 등을 담당하는 TokenProvider 인스턴스
     */
    private TokenProvider tokenProvider;

    /**
     * 생성자: TokenProvider를 주입받아 초기화합니다.
     *
     * @param tokenProvider JWT 토큰 관련 기능을 제공하는 객체
     */
    public JwtSecurityConfig(TokenProvider tokenProvider) {
        this.tokenProvider = tokenProvider;
    }

    /**
     * HttpSecurity 설정을 구성하는 메서드
     * JWT 필터를 Spring Security 필터 체인에 추가합니다.
     *
     * @param http 보안 설정을 위한 HttpSecurity 객체
     */
    @Override
    public void configure(HttpSecurity http) {
        // JwtFilter 인스턴스 생성
        JwtFilter customFilter = new JwtFilter(tokenProvider);
        // JwtFilter를 UsernamePasswordAuthenticationFilter 앞에 추가
        // 이를 통해 JWT 인증이 기본 인증 방식보다 먼저 수행됩니다.
        http.addFilterBefore(customFilter, UsernamePasswordAuthenticationFilter.class);
    }
}