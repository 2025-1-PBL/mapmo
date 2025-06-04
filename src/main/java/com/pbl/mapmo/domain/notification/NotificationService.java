package com.pbl.mapmo.domain.notification;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.Message;
import com.pbl.mapmo.domain.user.User;
import com.pbl.mapmo.domain.user.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

/**
 * 알림 서비스 클래스
 * 알림 생성, 조회, 푸시 알림 전송 등의 비즈니스 로직을 처리합니다.
 */
@Service
@RequiredArgsConstructor
@Slf4j  // 로깅을 위한 Lombok 어노테이션
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;

    /**
     * 새 알림을 생성하고 저장합니다.
     *
     * @param user 알림을 받을 사용자
     * @param title 알림 제목
     * @param message 알림 내용
     * @param type 알림 유형
     * @param referenceId 참조 대상 ID
     * @return 저장된 알림 객체
     */
    @Transactional
    public Notification saveNotification(User user, String title, String message,
                                         NotificationType type, Long referenceId) {
        Notification notification = Notification.builder()
                .user(user)
                .title(title)
                .message(message)
                .type(type)
                .referenceId(referenceId)
                .isRead(false)  // 기본값은 읽지 않음 상태
                .build();

        return notificationRepository.save(notification);
    }

    /**
     * 특정 사용자의 모든 알림을 조회합니다.
     *
     * @param user 알림을 조회할 사용자
     * @return 사용자의 알림 목록
     */
    @Transactional(readOnly = true)
    public List<Notification> getUserNotifications(User user) {
        return notificationRepository.findByUserOrderByCreatedAtDesc(user);
    }

    /**
     * 특정 사용자의 읽지 않은 알림을 조회합니다.
     *
     * @param user 알림을 조회할 사용자
     * @return 사용자의 읽지 않은 알림 목록
     */
    @Transactional(readOnly = true)
    public List<Notification> getUnreadNotifications(User user) {
        return notificationRepository.findByUserAndIsReadFalseOrderByCreatedAtDesc(user);
    }

    /**
     * 특정 사용자의 읽지 않은 알림 수를 계산합니다.
     *
     * @param user 알림 수를 계산할 사용자
     * @return 읽지 않은 알림 수
     */
    @Transactional(readOnly = true)
    public long getUnreadCount(User user) {
        return notificationRepository.countByUserAndIsReadFalse(user);
    }

    /**
     * 특정 알림을 읽음 상태로 표시합니다.
     *
     * @param notificationId 읽음 처리할 알림 ID
     */
    @Transactional
    public void markAsRead(Long notificationId) {
        notificationRepository.findById(notificationId).ifPresent(notification -> {
            notification.setRead(true);
            notificationRepository.save(notification);
        });
    }

    /**
     * 특정 사용자의 모든 알림을 읽음 상태로 표시합니다.
     *
     * @param user 알림을 읽음 처리할 사용자
     */
    @Transactional
    public void markAllAsRead(User user) {
        List<Notification> unreadNotifications = notificationRepository.findByUserAndIsReadFalseOrderByCreatedAtDesc(user);
        unreadNotifications.forEach(notification -> notification.setRead(true));
        notificationRepository.saveAll(unreadNotifications);
    }

    /**
     * 푸시 알림을 전송합니다.
     * 비동기(@Async)로 실행되어 메인 스레드를 차단하지 않습니다.
     *
     * @param user 알림을 받을 사용자
     * @param title 알림 제목
     * @param body 알림 내용
     * @param type 알림 유형
     * @param referenceId 참조 대상 ID
     * @param data 추가 데이터
     */
    @Async
    public void sendPushNotification(User user, String title, String body,
                                     NotificationType type, Long referenceId,
                                     Map<String, String> data) {
        try {
            // 데이터베이스에 알림 저장
            saveNotification(user, title, body, type, referenceId);

            // FCM 토큰이 있는 경우에만 푸시 알림 전송
            if (user.getFcmToken() != null && !user.getFcmToken().isEmpty()) {
                // Firebase 클라우드 메시징을 위한 알림 객체 생성
                com.google.firebase.messaging.Notification fcmNotification =
                        com.google.firebase.messaging.Notification.builder()
                                .setTitle(title)
                                .setBody(body)
                                .build();

                // 푸시 메시지 구성
                Message message = Message.builder()
                        .setToken(user.getFcmToken())
                        .setNotification(fcmNotification)
                        .putAllData(data)
                        .build();

                // Firebase를 통해 푸시 알림 전송
                FirebaseMessaging.getInstance().send(message);
                log.info("푸시 알림 전송 완료: 사용자 ID={}, 제목={}", user.getId(), title);
            }
        } catch (Exception e) {
            log.error("푸시 알림 전송 실패: {}", e.getMessage(), e);
        }
    }
}