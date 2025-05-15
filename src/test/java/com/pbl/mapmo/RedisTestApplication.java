package com.pbl.mapmo;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.stereotype.Component;

@SpringBootApplication
public class RedisTestApplication {

    public static void main(String[] args) {
        SpringApplication.run(RedisTestApplication.class, args);
    }

    @Bean
    public RedisConnectionFactory redisConnectionFactory() {
        RedisStandaloneConfiguration configuration = new RedisStandaloneConfiguration();
        configuration.setHostName("ocb.iptime.org");
        configuration.setPort(6379);
        return new LettuceConnectionFactory(configuration);
    }

    @Bean("testRedisTemplate")
    public RedisTemplate<String, String> redisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, String> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);
        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(new StringRedisSerializer());
        return template;
    }

    @Component
    public static class RedisTest implements CommandLineRunner {

        private final RedisTemplate<String, String> redisTemplate;

        // @Qualifier 어노테이션을 사용하여 특정 빈 주입
        public RedisTest(@Qualifier("testRedisTemplate") RedisTemplate<String, String> redisTemplate) {
            this.redisTemplate = redisTemplate;
        }

        @Override
        public void run(String... args) {
            try {
                System.out.println("Redis 연결 테스트 시작...");
                redisTemplate.opsForValue().set("test", "Hello Redis");
                String value = redisTemplate.opsForValue().get("test");
                System.out.println("Redis에서 값 읽기: " + value);
                redisTemplate.delete("test");
                System.out.println("Redis 연결 테스트 성공!");
            } catch (Exception e) {
                System.err.println("Redis 연결 실패: " + e.getMessage());
                System.err.println("원인: " + e.getCause());
                e.printStackTrace();
                // 오류 발생 시 애플리케이션 종료
                System.exit(1);
            }
        }
    }
}