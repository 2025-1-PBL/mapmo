package com.pbl.mapmo.common.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;

/**
 * Redis 연결 및 설정을 담당하는 설정 클래스
 * Spring Data Redis와의 연동을 위한 템플릿 빈을 정의합니다.
 */
@Configuration
public class RedisConfig {

    @Value("${spring.redis.host}")
    private String redisHost;

    @Value("${spring.redis.port}")
    private int redisPort;

    @Value("${spring.redis.timeout}")
    private int timeout;

    /**
     * Redis 연결 팩토리를 명시적으로 구성합니다.
     */
    @Bean
    public RedisConnectionFactory redisConnectionFactory() {
        RedisStandaloneConfiguration redisConfig = new RedisStandaloneConfiguration(redisHost, redisPort);
        LettuceConnectionFactory connectionFactory = new LettuceConnectionFactory(redisConfig);
        // 연결 설정이 변경되면 재시작하도록 설정
        connectionFactory.setValidateConnection(true);
        return connectionFactory;
    }

    /**
     * Redis 데이터베이스와 통신하기 위한 RedisTemplate 빈을 생성합니다.
     * 이 템플릿은 문자열 키와 문자열 값을 사용하도록 설정되며,
     * 주로 리프레시 토큰 관리 등의 용도로 사용됩니다.
     *
     * @param connectionFactory Redis 연결을 관리하는 팩토리 객체(자동 주입됨)
     * @return 문자열 키/값 직렬화가 설정된 RedisTemplate 객체
     */
    @Bean
    public RedisTemplate<String, String> redisTemplate(RedisConnectionFactory connectionFactory) {
        // 새로운 RedisTemplate 인스턴스 생성
        RedisTemplate<String, String> redisTemplate = new RedisTemplate<>();

        // Redis 연결 팩토리 설정
        redisTemplate.setConnectionFactory(connectionFactory);

        // 키와 값 모두 String 타입으로 직렬화하도록 설정
        // 이렇게 하면 Redis에 저장될 때 사람이 읽을 수 있는 형태로 저장됨
        redisTemplate.setKeySerializer(new StringRedisSerializer());
        redisTemplate.setValueSerializer(new StringRedisSerializer());

        return redisTemplate;
    }
}