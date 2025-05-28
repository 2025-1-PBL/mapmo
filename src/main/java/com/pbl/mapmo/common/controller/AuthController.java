package com.pbl.mapmo.common.controller;

import com.pbl.mapmo.common.dto.LoginDto;
import com.pbl.mapmo.common.dto.TokenDto;
import com.pbl.mapmo.domain.user.User;
import com.pbl.mapmo.domain.user.UserRepository;
import com.pbl.mapmo.domain.user.UserService;
import com.pbl.mapmo.domain.user.UserDto;
import com.pbl.mapmo.jwt.JwtFilter;
import com.pbl.mapmo.jwt.RefreshTokenDto;
import com.pbl.mapmo.jwt.RefreshTokenRepository;
import com.pbl.mapmo.jwt.TokenProvider;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
@Tag(name = "Auth", description = "인증·인가 API")
@RestController
@RequestMapping("/api")
@Slf4j
public class AuthController {
    private final TokenProvider tokenProvider;
    private final AuthenticationManagerBuilder authenticationManagerBuilder;
    private final RefreshTokenRepository refreshTokenRepository;
    private final UserDetailsService userDetailsService;
    private final UserRepository userRepository; // 추가
    private final PasswordEncoder passwordEncoder; // 추가
    private final UserService userService; // 추가

    public AuthController(
            TokenProvider tokenProvider,
            AuthenticationManagerBuilder authenticationManagerBuilder,
            RefreshTokenRepository refreshTokenRepository,
            UserDetailsService userDetailsService,
            UserRepository userRepository, // 추가
            PasswordEncoder passwordEncoder, // 추가
            UserService userService) { // 추가
        this.tokenProvider = tokenProvider;
        this.authenticationManagerBuilder = authenticationManagerBuilder;
        this.refreshTokenRepository = refreshTokenRepository;
        this.userDetailsService = userDetailsService;
        this.userRepository = userRepository; // 추가
        this.passwordEncoder = passwordEncoder; // 추가
        this.userService = userService; // 추가
    }

    @Operation(summary = "로그인 / 인증")
    @PostMapping("/authenticate")
    public ResponseEntity<TokenDto> authorize(@Valid @RequestBody LoginDto loginDto) {
        log.debug("인증 요청: username={}", loginDto.getUsername());
        UsernamePasswordAuthenticationToken authenticationToken =
                new UsernamePasswordAuthenticationToken(loginDto.getUsername(), loginDto.getPassword());
        log.debug("인증 토큰 생성: username={}, password 길이={}",
                loginDto.getUsername(),
                loginDto.getPassword() == null ? "null" : loginDto.getPassword().length());

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

    @Operation(summary = "회원가입")
    @PostMapping("/signup")
    public ResponseEntity<UserDto.Response> signup(
            @Valid @RequestBody UserDto.Request userDto) {

        // 이메일 중복 검사 (인스턴스 메서드로 호출)
        if (userRepository.existsByEmail(userDto.getEmail())) {
            return ResponseEntity.badRequest().body(null); // 이미 존재하는 이메일
        }

        // 비밀번호 암호화 코드 제거
        User user = userDto.toEntity();
        // 이 줄 제거: user.setPassword(passwordEncoder.encode(user.getPassword()));

        // 사용자 저장 (인스턴스 메서드로 호출)
        User savedUser = userService.registerUser(user);

        // 응답 생성
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(UserDto.Response.of(savedUser));
    }


    @Operation(summary = "액세스 토큰 재발급")
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