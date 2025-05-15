package com.pbl.mapmo.jwt;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import java.util.concurrent.TimeUnit;

/**
 * 리프레시 토큰을 Redis 데이터베이스에 저장하고 관리하는 리포지토리 클래스
 * JWT 인증 시스템에서 사용자의 리프레시 토큰을 저장, 조회, 삭제하는 기능을 제공합니다.
 */
@Repository
public class RefreshTokenRepository {

    /**
     * Redis 데이터베이스와 통신하기 위한 템플릿 객체
     * 문자열 키와 문자열 값을 저장하는데 사용됩니다.
     */
    private final RedisTemplate<String, String> redisTemplate;

    /**
     * 리프레시 토큰의 유효 기간 (14일)
     * 밀리초 단위로 설정되어 있습니다.
     */
    private final static long REFRESH_TOKEN_EXPIRATION_TIME = 14 * 24 * 60 * 60 * 1000; // 14일

    /**
     * 생성자: RedisTemplate을 주입받아 초기화합니다.
     *
     * @param redisTemplate Redis 작업을 위한 템플릿 객체
     */
    public RefreshTokenRepository(RedisTemplate<String, String> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    /**
     * 사용자의 리프레시 토큰을 Redis에 저장합니다.
     * 사용자명을 키로, 리프레시 토큰을 값으로 저장하며 만료 시간을 설정합니다.
     *
     * @param username 사용자명 (Redis의 키로 사용)
     * @param refreshToken 저장할 리프레시 토큰 값
     */
    public void saveRefreshToken(String username, String refreshToken) {
        redisTemplate.opsForValue().set(
                username,
                refreshToken,
                REFRESH_TOKEN_EXPIRATION_TIME,
                TimeUnit.MILLISECONDS
        );
    }

    /**
     * 사용자명으로 저장된 리프레시 토큰을 조회합니다.
     *
     * @param username 조회할 사용자명
     * @return 저장된 리프레시 토큰, 없을 경우 null 반환
     */
    public String findRefreshTokenByUsername(String username) {
        return redisTemplate.opsForValue().get(username);
    }

    /**
     * 사용자명으로 저장된 리프레시 토큰이 존재하는지 확인합니다.
     *
     * @param username 확인할 사용자명
     * @return 리프레시 토큰 존재 여부 (true/false)
     */
    public boolean existsByUsername(String username) {
        return Boolean.TRUE.equals(redisTemplate.hasKey(username));
    }

    /**
     * 사용자명으로 저장된 리프레시 토큰을 삭제합니다.
     * 로그아웃 처리 시 호출됩니다.
     *
     * @param username 삭제할 토큰의 사용자명
     */
    public void deleteByUsername(String username) {
        redisTemplate.delete(username);
    }
}