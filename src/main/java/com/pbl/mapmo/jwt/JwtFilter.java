package com.pbl.mapmo.jwt;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.GenericFilterBean;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import java.io.IOException;

public class JwtFilter extends GenericFilterBean {

    private static final Logger logger = LoggerFactory.getLogger(JwtFilter.class); // 로깅 객체.
    // HTTP 요청 헤더에서 jwt 토큰을 찾기 위한 헤더 이름 정의.
    // 클라이언트는 이 헤더에 "Bearer {토큰}" 형식으로 JWT를 전송.
    public static final String AUTHORIZATION_HEADER = "Authorization";

    private TokenProvider tokenProvider;

    public JwtFilter(TokenProvider tokenProvider) {
        this.tokenProvider = tokenProvider;
    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain)
            throws IOException, ServletException {
        // `ServletRequest`를 `HttpServletRequest`로 캐스팅합니다.
        // ServletRequest란 HTTP 요청 정보를 캡슐화하는 객체
        // 예를 들어, 클라이언트의 IP 주소, 요청 메소드(GET, POST 등), 요청 헤더와 바디 등의 정보가 포함
        HttpServletRequest httpServletRequest = (HttpServletRequest) servletRequest;
        // `resolveToken()` 메서드를 호출하여 HTTP 요청 헤더에서 JWT 토큰을 추출합니다.
        // 이 메서드는 Authorization 헤더에서 "Bearer " 접두사를 제거하고 실제 토큰을 반환합니다.
        String jwt = resolveToken(httpServletRequest);
        // JWT 토큰이 존재하고(`StringUtils.hasText(jwt)`) 유효한지(`tokenProvider.validateToken(jwt)`) 확인합니다.
        // `validateToken()` 메서드는 토큰의 서명, 형식, 만료 여부 등을 검사합니다.
        String requestURI = httpServletRequest.getRequestURI();

        // 토큰이 유효하면, `tokenProvider.getAuthentication(jwt)`를 호출하여 토큰에서 사용자 인증 정보를 추출합니다.
        // 추출된 인증 정보를 `SecurityContextHolder`에 저장합니다. 이렇게 하면 현재 요청에 대한 인증 정보가 Spring Security에 설정됩니다.
        if (StringUtils.hasText(jwt) && tokenProvider.validateToken(jwt)) {
            Authentication authentication = tokenProvider.getAuthentication(jwt);
            SecurityContextHolder.getContext().setAuthentication(authentication);
            logger.debug("Security Context에 '{}' 인증 정보를 저장했습니다, uri: {}", authentication.getName(), requestURI);
        } else {
            logger.debug("유효한 JWT 토큰이 없습니다, uri: {}", requestURI);
        }

        // 인증 처리 후, `filterChain.doFilter()`를 호출하여 다음 필터로 요청을 전달합니다.
        filterChain.doFilter(servletRequest, servletResponse);
    }

    private String resolveToken(HttpServletRequest request) {
        String bearerToken = request.getHeader(AUTHORIZATION_HEADER); // HTTP 요청의 Authorization 헤더 값 추출.
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) { // 헤더 값이 비어있지 않은지, "Bearer"로 시작하는지 확인.
            return bearerToken.substring(7); // 접두사(Bearer )제외하고 실제 jwt 토큰만 반환.
        }
        return null;
    }
}