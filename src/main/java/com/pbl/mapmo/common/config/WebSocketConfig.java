package com.pbl.mapmo.common.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

/**
 * WebSocket 설정 클래스
 * 웹소켓 연결 및 STOMP 메시지 브로커 구성을 담당합니다.
 */
@Configuration
@EnableWebSocketMessageBroker  // STOMP를 사용한 웹소켓 메시지 브로커 활성화
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    /**
     * STOMP 엔드포인트 등록
     * 클라이언트가 웹소켓 연결을 맺기 위한 엔드포인트를 설정합니다.
     *
     * @param registry STOMP 엔드포인트 레지스트리
     */
    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws")  // 웹소켓 엔드포인트 경로 설정
                .setAllowedOriginPatterns("*")  // CORS 설정 - 모든 오리진 허용
                .withSockJS();  // SockJS 지원 활성화 (웹소켓을 지원하지 않는 브라우저를 위한 대체 옵션)
    }

    /**
     * 메시지 브로커 구성
     * 클라이언트와 서버 간의 메시지 라우팅을 설정합니다.
     *
     * @param registry 메시지 브로커 레지스트리
     */
    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        // 구독 경로 설정 - 클라이언트가 메시지를 수신할 주제(topic)와 큐(queue)
        registry.enableSimpleBroker("/topic", "/queue");
        // 클라이언트가 메시지를 보낼 때 사용할 접두사 설정
        registry.setApplicationDestinationPrefixes("/app");
    }
}