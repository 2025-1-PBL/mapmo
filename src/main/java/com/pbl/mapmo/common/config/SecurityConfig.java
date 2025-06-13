package com.pbl.mapmo.common.config;

import com.pbl.mapmo.jwt.JwtSecurityConfig;
import com.pbl.mapmo.jwt.TokenProvider;
import com.pbl.mapmo.oauth2.CustomOAuth2UserService;
import com.pbl.mapmo.oauth2.OAuth2SuccessHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

/**
 * 애플리케이션의 보안 설정을 담당하는 클래스
 * Spring Security 설정을 통해 인증 및 권한 부여를 관리함
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final TokenProvider tokenProvider;
    private final CustomOAuth2UserService customOAuth2UserService;
    private final OAuth2SuccessHandler oAuth2SuccessHandler;

    /**
     * 생성자를 통해 TokenProvider 의존성 주입
     * @param tokenProvider JWT 토큰 생성 및 검증을 담당하는 컴포넌트
     */
    public SecurityConfig(
            TokenProvider tokenProvider,
            CustomOAuth2UserService customOAuth2UserService,
            OAuth2SuccessHandler oAuth2SuccessHandler) {
        this.tokenProvider = tokenProvider;
        this.customOAuth2UserService = customOAuth2UserService;
        this.oAuth2SuccessHandler = oAuth2SuccessHandler;
    }
    /**
     * 비밀번호 암호화에 사용할 인코더 빈 정의
     * @return BCrypt 알고리즘을 사용하는 비밀번호 인코더
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * Spring Security 필터 체인 설정
     * @param http HttpSecurity 객체
     * @return 구성된 SecurityFilterChain
     * @throws Exception 보안 설정 중 발생할 수 있는 예외
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                // CSRF 보호 기능 비활성화 (REST API에서는 일반적으로 불필요)
                .csrf(csrf -> csrf.disable())
                // 세션 관리 정책 설정 - 상태를 저장하지 않는 방식 사용
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                // HTTP 요청에 대한 접근 권한 설정
                .authorizeHttpRequests(authorize -> authorize
                        // 인증 관련 엔드포인트는 모든 사용자에게 접근 허용
                        .requestMatchers("/api/authenticate").permitAll()
                        // 회원가입 엔드포인트는 모든 사용자에게 접근 허용
                        .requestMatchers("/api/signup").permitAll()
                        .requestMatchers("/").permitAll()
                        .requestMatchers("/error").permitAll()
                        .requestMatchers("/oauth2/**").permitAll()
                        .requestMatchers("/**").permitAll()
                        // Swagger & springdoc 경로는 누구나 접근 가능
                        .requestMatchers(
                                "/v3/api-docs/**",
                                "/swagger-ui.html",       // 리다이렉트 엔드포인트
                                "/swagger-ui/**",         // 실제 정적 리소스
                                "/swagger-resources/**")  // (필요 시)
                        .permitAll()
                        // 나머지는 인증 필요
                        .anyRequest().authenticated()
                )
                // 폼 로그인 비활성화
                .formLogin(formLogin -> formLogin.disable())
                // HTTP Basic 인증 비활성화
                .httpBasic(httpBasic -> httpBasic.disable())
                // JWT 보안 설정 적용
                .with(new JwtSecurityConfig(tokenProvider), customizer -> {})
                // OAuth2 로그인 설정 추가
                .oauth2Login(oauth2 -> oauth2
                    .userInfoEndpoint(userInfo -> userInfo
                        .userService(customOAuth2UserService)
                    )
                    .successHandler(oAuth2SuccessHandler)
                );


        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Arrays.asList("http://localhost:3000")); // 프론트엔드 서버 주소
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("Authorization", "Content-Type"));
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}