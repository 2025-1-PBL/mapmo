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
import java.util.Arrays;

/**
 * JWT 인증 필터 클래스
 * HTTP 요청에서 JWT 토큰을 추출하고 검증하여 Spring Security의 인증 컨텍스트에 저장하는 역할을 담당합니다.
 * GenericFilterBean을 상속받아 서블릿 필터로 동작합니다.
 */
public class JwtFilter extends GenericFilterBean {

    // 로깅을 위한 Logger 객체 생성
    private static final Logger logger = LoggerFactory.getLogger(JwtFilter.class);

    /**
     * HTTP 요청 헤더에서 JWT 토큰을 찾기 위한 헤더 이름
     * 클라이언트는 이 헤더에 "Bearer {토큰}" 형식으로 JWT를 전송합니다.
     */
    public static final String AUTHORIZATION_HEADER = "Authorization";

    /**
     * 인증이 필요하지 않은 공개 URL 경로 목록
     * 이 경로들에 대한 요청은 JWT 검증을 건너뜁니다.
     */
    private static final String[] PUBLIC_URLS = {
            "/api/authenticate", // 로그인 API
            "/api/signup",       // 회원가입 API
            "/error",            // 오류 페이지
            "/",                 // 루트 경로
            ""                   // 빈 경로
    };

    // JWT 토큰 생성 및 검증을 담당하는 객체
    private TokenProvider tokenProvider;

    /**
     * 생성자: TokenProvider를 주입받아 초기화합니다.
     *
     * @param tokenProvider JWT 토큰 관련 기능을 제공하는 객체
     */
    public JwtFilter(TokenProvider tokenProvider) {
        this.tokenProvider = tokenProvider;
    }

    /**
     * 필터 로직을 처리하는 메서드
     * 모든 HTTP 요청이 이 메서드를 통과하며, JWT 토큰을 검증하고 인증 정보를 설정합니다.
     *
     * @param servletRequest 서블릿 요청 객체
     * @param servletResponse 서블릿 응답 객체
     * @param filterChain 필터 체인
     */
    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain)
            throws IOException, ServletException {
        // ServletRequest를 HttpServletRequest로 변환하여 HTTP 관련 메서드에 접근할 수 있게 함
        HttpServletRequest httpServletRequest = (HttpServletRequest) servletRequest;

        // HTTP 요청 헤더에서 JWT 토큰을 추출
        String jwt = resolveToken(httpServletRequest);

        // 현재 요청의 URI 경로 가져오기
        String requestURI = httpServletRequest.getRequestURI();

        // 공개 URL인 경우 JWT 검증 없이 다음 필터로 요청 전달
        if (Arrays.asList(PUBLIC_URLS).contains(requestURI)) {
            filterChain.doFilter(servletRequest, servletResponse);
            return;
        }

        // JWT 토큰이 존재하고 유효한 경우 인증 처리
        if (StringUtils.hasText(jwt) && tokenProvider.validateToken(jwt)) {
            // 토큰에서 인증 정보(Authentication 객체) 추출
            Authentication authentication = tokenProvider.getAuthentication(jwt);
            // Spring Security의 SecurityContext에 인증 정보 저장
            SecurityContextHolder.getContext().setAuthentication(authentication);
            logger.debug("Security Context에 '{}' 인증 정보를 저장했습니다, uri: {}", authentication.getName(), requestURI);
        } else {
            logger.debug("유효한 JWT 토큰이 없습니다, uri: {}", requestURI);
        }

        // 인증 처리 후 다음 필터로 요청 전달
        filterChain.doFilter(servletRequest, servletResponse);
    }

    /**
     * HTTP 요청 헤더에서 JWT 토큰을 추출하는 메서드
     *
     * @param request HTTP 요청 객체
     * @return JWT 토큰 문자열, 토큰이 없거나 형식이 맞지 않으면 null 반환
     */
    private String resolveToken(HttpServletRequest request) {
        // Authorization 헤더 값 가져오기
        String bearerToken = request.getHeader(AUTHORIZATION_HEADER);
        // "Bearer " 접두사로 시작하는 유효한 토큰인지 확인
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            // "Bearer " 접두사(7자)를 제거하고 실제 JWT 토큰만 반환
            return bearerToken.substring(7);
        }
        return null;
    }
}