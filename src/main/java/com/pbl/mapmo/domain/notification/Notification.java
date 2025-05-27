package com.pbl.mapmo.domain.notification;

import com.pbl.mapmo.domain.user.User;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * 알림 엔티티 클래스
 * 사용자에게 전송되는 모든 종류의 알림 정보를 저장합니다.
 */
@Entity
@Table(name = "notification")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)  // 생성 시간 자동 기록을 위한 JPA Auditing 설정
public class Notification {

    /**
     * 알림 고유 식별자 (기본키)
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 알림을 받는 사용자
     * 지연 로딩(LAZY)으로 설정하여 필요할 때만 사용자 정보를 로드
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    /**
     * 알림 제목
     */
    @Column(nullable = false)
    private String title;

    /**
     * 알림 내용 메시지
     */
    @Column(nullable = false)
    private String message;

    /**
     * 알림 읽음 상태
     * true: 읽음, false: 읽지 않음
     */
    @Column(name = "is_read")
    private boolean isRead;

    /**
     * 알림 생성 시간
     * 자동으로 현재 시간이 설정되며 이후 수정 불가
     */
    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    /**
     * 알림 유형
     * 각 알림이 어떤 종류인지 구분하기 위한 열거형 값
     */
    @Column(name = "notification_type")
    @Enumerated(EnumType.STRING)
    private NotificationType type;

    /**
     * 관련 항목의 ID (예: 게시글 ID, 댓글 ID 등)
     * 알림이 어떤 객체와 연관되어 있는지 저장
     */
    @Column(name = "reference_id")
    private Long referenceId;
}