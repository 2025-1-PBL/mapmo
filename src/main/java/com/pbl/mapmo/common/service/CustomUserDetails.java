package com.pbl.mapmo.common.service;

import com.pbl.mapmo.domain.user.User;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.Collection;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Spring Security와 OAuth2 인증을 위한 사용자 상세 정보 클래스
 * UserDetails와 OAuth2User 인터페이스를 모두 구현하여 일반 로그인과 OAuth2 로그인을 모두 지원
 */
public class CustomUserDetails implements OAuth2User, UserDetails {

    /**
     * 사용자 엔티티
     */
    private User user;

    /**
     * OAuth2 인증에서 제공하는 사용자 속성 정보
     */
    private Map<String, Object> attributes;

    /**
     * 일반 로그인을 위한 생성자
     *
     * @param user 사용자 엔티티
     */
    public CustomUserDetails(User user) {
        this.user = user;
    }

    /**
     * OAuth2 로그인을 위한 생성자
     *
     * @param user 사용자 엔티티
     * @param attributes OAuth2 제공자로부터 받은 사용자 속성 정보
     */
    public CustomUserDetails(User user, Map<String, Object> attributes) {
        this.user = user;
        this.attributes = attributes;
    }

    /**
     * OAuth2User 인터페이스의 메서드 구현
     * OAuth2 인증에서 제공하는 사용자 속성 정보를 반환
     *
     * @return OAuth2 사용자 속성 정보
     */
    @Override
    public Map<String, Object> getAttributes() {
        return attributes;
    }

    /**
     * 사용자의 권한 목록을 반환
     *
     * @return 사용자 권한 컬렉션
     */
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return user.getAuthorities().stream()
                .map(authority -> new SimpleGrantedAuthority(authority.getAuthorityName()))
                .collect(Collectors.toList());
    }

    /**
     * 사용자의 비밀번호를 반환
     *
     * @return 사용자 비밀번호
     */
    @Override
    public String getPassword() {
        return user.getPassword();
    }

    /**
     * 사용자의 식별자(이메일)를 반환
     *
     * @return 사용자 이메일
     */
    @Override
    public String getUsername() {
        return user.getEmail();
    }

    /**
     * 계정 만료 여부 확인
     *
     * @return 계정이 만료되지 않았으면 true
     */
    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    /**
     * 계정 잠금 여부 확인
     *
     * @return 계정이 잠기지 않았으면 true
     */
    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    /**
     * 자격 증명(비밀번호) 만료 여부 확인
     *
     * @return 자격 증명이 만료되지 않았으면 true
     */
    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    /**
     * 계정 활성화 여부 확인
     * 사용자가 삭제되지 않았으면 활성화된 것으로 간주
     *
     * @return 계정이 활성화되어 있으면 true
     */
    @Override
    public boolean isEnabled() {
        return !user.getIsDeleted();
    }

    /**
     * OAuth2User 인터페이스의 메서드 구현
     * 사용자의 이름을 반환
     *
     * @return 사용자 이름
     */
    @Override
    public String getName() {
        return user.getName();
    }

    /**
     * 사용자 ID를 반환하는 편의 메서드
     *
     * @return 사용자 ID
     */
    public Integer getId() {
        return user.getId();
    }
}