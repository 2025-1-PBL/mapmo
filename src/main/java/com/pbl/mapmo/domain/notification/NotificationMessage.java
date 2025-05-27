package com.pbl.mapmo.domain.notification;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * 알림 메시지 데이터 전송 객체(DTO)
 * 클라이언트에게 전송되는 알림 정보를 담는 객체입니다.
 * WebSocket이나 FCM 푸시 알림에 사용됩니다.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationMessage {
    /**
     * 알림 고유 식별자
     */
    private Long id;

    /**
     * 알림 제목
     */
    private String title;

    /**
     * 알림 내용 메시지
     */
    private String message;

    /**
     * 알림 유형
     */
    private NotificationType type;

    /**
     * 참조 대상 ID (연관된 객체의 ID)
     */
    private Long referenceId;

    /**
     * 수신자 사용자 ID
     */
    private Long userId;

    /**
     * 알림 생성 시간
     */
    private LocalDateTime createdAt;

    /**
     * 읽음 상태
     */
    private boolean read;

    /**
     * 추가 데이터
     * 알림 유형에 따라 필요한 부가 정보를 포함합니다.
     */
    private Map<String, String> data;
}