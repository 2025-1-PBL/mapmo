package com.pbl.mapmo.domain.notification;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationDTO {
    private Long id;
    private Long userId;  // User 엔티티 대신 ID만 포함
    private String title;
    private String message;
    private boolean isRead;
    private LocalDateTime createdAt;
    private NotificationType type;
    private Long referenceId;
    
    // 필요하다면 사용자 이름 등 추가 정보 포함 가능
    private String userName;
}