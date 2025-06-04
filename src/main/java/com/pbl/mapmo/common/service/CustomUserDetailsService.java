package com.pbl.mapmo.common.service;

import com.pbl.mapmo.domain.user.User;
import com.pbl.mapmo.domain.user.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    public CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        log.debug("사용자 조회: username={}", username);
        return userRepository.findByEmail(username)
                .map(user -> {
                    log.debug("사용자 찾음: {}, 비밀번호 길이: {}", user.getEmail(), user.getPassword().length());
                    return createUserDetails(user);
                })
                .orElseThrow(() -> {
                    log.error("사용자를 찾을 수 없음: {}", username);
                    return new UsernameNotFoundException(username + " -> 데이터베이스에서 찾을 수 없습니다.");
                });
    }

    private UserDetails createUserDetails(User user) {
        List<GrantedAuthority> grantedAuthorities = user.getAuthorities().stream()
                .map(authority -> new SimpleGrantedAuthority(authority.getAuthorityName()))
                .collect(Collectors.toList());

        return new org.springframework.security.core.userdetails.User(
                user.getEmail(),
                user.getPassword(),
                grantedAuthorities
        );
    }
}