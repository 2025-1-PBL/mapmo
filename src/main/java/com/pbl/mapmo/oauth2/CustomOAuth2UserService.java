package com.pbl.mapmo.oauth2;

import java.util.Map;
import java.util.Optional;

import com.pbl.mapmo.common.service.CustomUserDetails;
import com.pbl.mapmo.domain.authority.Authority;
import com.pbl.mapmo.domain.authority.AuthorityRepository;
import com.pbl.mapmo.domain.user.User;
import com.pbl.mapmo.domain.user.UserRepository;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;
    private final AuthorityRepository authorityRepository;

    public CustomOAuth2UserService(UserRepository userRepository, AuthorityRepository authorityRepository) {
        this.userRepository = userRepository;
        this.authorityRepository = authorityRepository;
    }

    @Override
    @Transactional
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(userRequest);
        
        try {
            return processOAuth2User(userRequest, oAuth2User);
        } catch (Exception e) {
            log.error("OAuth2 인증 처리 중 오류 발생", e);
            throw new OAuth2AuthenticationException(e.getMessage());
        }
    }

    private OAuth2User processOAuth2User(OAuth2UserRequest oAuth2UserRequest, OAuth2User oAuth2User) {
        // 소셜 서비스 확인 (구글, 네이버, 카카오 등)
        String registrationId = oAuth2UserRequest.getClientRegistration().getRegistrationId();
        
        // 소셜 로그인 제공자별 사용자 속성 정보 추출
        OAuth2UserInfo userInfo = getOAuth2UserInfo(registrationId, oAuth2User);
        
        // 이메일로 기존 사용자 확인
        Optional<User> userOptional = userRepository.findByEmail(userInfo.getEmail());
        User user;
        
        if (userOptional.isPresent()) {
            // 기존 사용자인 경우 정보 업데이트
            user = userOptional.get();
            user.setName(userInfo.getName());
            user.setProfilePic(userInfo.getImageUrl());
            userRepository.save(user);
        } else {
            // 새 사용자인 경우 등록
            user = registerNewUser(userInfo, registrationId);
        }
        
        // 사용자 권한 정보와 OAuth2 속성을 포함한 인증 객체 생성 및 반환
        return new CustomUserDetails(user, oAuth2User.getAttributes());
    }

    private OAuth2UserInfo getOAuth2UserInfo(String registrationId, OAuth2User oAuth2User) {
        Map<String, Object> attributes = oAuth2User.getAttributes();
        
        switch(registrationId.toLowerCase()) {
            case "google":
                return new GoogleOAuth2UserInfo(attributes);
            case "kakao":
                return new KakaoOAuth2UserInfo(attributes);
            case "naver":
                return new NaverOAuth2UserInfo(attributes);
            default:
                throw new OAuth2AuthenticationException("지원하지 않는 로그인 서비스입니다.");
        }
    }

    @Transactional
    private User registerNewUser(OAuth2UserInfo oAuth2UserInfo, String provider) {
        // 새 사용자 생성
        User user = User.builder()
                .email(oAuth2UserInfo.getEmail())
                .name(oAuth2UserInfo.getName())
                .profilePic(oAuth2UserInfo.getImageUrl())
                .isDeleted(false)
                .password("") // 소셜 로그인은 비밀번호가 필요 없음
                .build();
        
        User savedUser = userRepository.save(user);
        
        // 기본 사용자 권한 부여
        Authority authority = new Authority();
        authority.setUser(savedUser);
        authority.setAuthorityName("ROLE_USER");
        authorityRepository.save(authority);
        
        return savedUser;
    }
}