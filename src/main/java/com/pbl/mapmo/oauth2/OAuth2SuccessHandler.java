package com.pbl.mapmo.oauth2;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pbl.mapmo.common.dto.TokenDto;
import com.pbl.mapmo.common.service.CustomUserDetails;
import com.pbl.mapmo.jwt.RefreshTokenDto;
import com.pbl.mapmo.jwt.RefreshTokenRepository;
import com.pbl.mapmo.jwt.TokenProvider;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;

@Component
@Slf4j
public class OAuth2SuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final TokenProvider tokenProvider;
    private final RefreshTokenRepository refreshTokenRepository;
    private final ObjectMapper objectMapper;

    public OAuth2SuccessHandler(
            TokenProvider tokenProvider,
            RefreshTokenRepository refreshTokenRepository,
            ObjectMapper objectMapper) {
        this.tokenProvider = tokenProvider;
        this.refreshTokenRepository = refreshTokenRepository;
        this.objectMapper = objectMapper;
    }

    @Override
    public void onAuthenticationSuccess(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication) throws IOException {
        
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        
        // JWT 토큰 생성
        String accessToken = tokenProvider.createToken(authentication);
        String refreshToken = tokenProvider.createRefreshToken(authentication);
        
        // 리프레시 토큰 저장
        refreshTokenRepository.saveRefreshToken(userDetails.getUsername(), refreshToken);
        
        // 응답 생성
        TokenDto tokenDto = new TokenDto(accessToken, refreshToken);
        
        // 프론트엔드로 리다이렉트
        String targetUrl = determineTargetUrl(request, response, authentication);
        
        if (response.isCommitted()) {
            log.debug("응답이 이미 커밋되었습니다. 리다이렉트할 수 없습니다: {}", targetUrl);
            return;
        }
        
        // 프론트엔드로 리다이렉트 (토큰 정보를 쿼리 파라미터로 전달)
        getRedirectStrategy().sendRedirect(request, response, 
                UriComponentsBuilder.fromUriString(targetUrl)
                        .queryParam("token", accessToken)
                        .queryParam("refreshToken", refreshToken)
                        .build().toUriString());
    }
    
    @Override
    protected String determineTargetUrl(HttpServletRequest request, HttpServletResponse response, Authentication authentication) {
        // Flutter 앱용 딥링크로 리다이렉트
        return "mapmo://oauth2/redirect";
    }
}