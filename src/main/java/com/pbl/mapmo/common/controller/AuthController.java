package com.pbl.mapmo.common.controller;

import com.pbl.mapmo.common.dto.LoginDto;
import com.pbl.mapmo.common.dto.TokenDto;
import com.pbl.mapmo.jwt.JwtFilter;
import com.pbl.mapmo.jwt.RefreshTokenDto;
import com.pbl.mapmo.jwt.RefreshTokenRepository;
import com.pbl.mapmo.jwt.TokenProvider;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class AuthController {
    private final TokenProvider tokenProvider;
    private final AuthenticationManagerBuilder authenticationManagerBuilder;
    private final RefreshTokenRepository refreshTokenRepository;
    private final UserDetailsService userDetailsService;

    public AuthController(
            TokenProvider tokenProvider,
            AuthenticationManagerBuilder authenticationManagerBuilder,
            RefreshTokenRepository refreshTokenRepository,
            UserDetailsService userDetailsService) {
        this.tokenProvider = tokenProvider;
        this.authenticationManagerBuilder = authenticationManagerBuilder;
        this.refreshTokenRepository = refreshTokenRepository;
        this.userDetailsService = userDetailsService;
    }

    @PostMapping("/authenticate")
    public ResponseEntity<TokenDto> authorize(@Valid @RequestBody LoginDto loginDto) {
        UsernamePasswordAuthenticationToken authenticationToken =
                new UsernamePasswordAuthenticationToken(loginDto.getUsername(), loginDto.getPassword());

        Authentication authentication = authenticationManagerBuilder.getObject().authenticate(authenticationToken);
        SecurityContextHolder.getContext().setAuthentication(authentication);

        // 액세스 토큰 생성
        String accessToken = tokenProvider.createToken(authentication);
        // 리프레시 토큰 생성
        String refreshToken = tokenProvider.createRefreshToken(authentication);

        // 리프레시 토큰 저장
        refreshTokenRepository.saveRefreshToken(authentication.getName(), refreshToken);

        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.add(JwtFilter.AUTHORIZATION_HEADER, "Bearer " + accessToken);

        return new ResponseEntity<>(new TokenDto(accessToken, refreshToken), httpHeaders, HttpStatus.OK);
    }

    @PostMapping("/refresh")
    public ResponseEntity<TokenDto> refresh(@Valid @RequestBody RefreshTokenDto refreshTokenDto) {
        // 1. 리프레시 토큰 유효성 검증
        String refreshToken = refreshTokenDto.getRefreshToken();

        if (!tokenProvider.validateToken(refreshToken)) {
            throw new RuntimeException("유효하지 않은 리프레시 토큰입니다.");
        }

        // 2. 사용자 정보 추출
        String username = tokenProvider.getUsernameFromToken(refreshToken);

        // 3. Redis에 저장된 리프레시 토큰과 비교
        String savedRefreshToken = refreshTokenRepository.findRefreshTokenByUsername(username);

        if (savedRefreshToken == null || !savedRefreshToken.equals(refreshToken)) {
            throw new RuntimeException("저장된 리프레시 토큰과 일치하지 않습니다.");
        }

        // 4. 새 액세스 토큰 발급
        UserDetails userDetails = userDetailsService.loadUserByUsername(username);
        Authentication authentication = new UsernamePasswordAuthenticationToken(
                userDetails, null, userDetails.getAuthorities());

        String newAccessToken = tokenProvider.createToken(authentication);

        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.add(JwtFilter.AUTHORIZATION_HEADER, "Bearer " + newAccessToken);

        return new ResponseEntity<>(new TokenDto(newAccessToken, refreshToken), httpHeaders, HttpStatus.OK);
    }
}