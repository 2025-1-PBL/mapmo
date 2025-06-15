package com.pbl.mapmo.domain.notification;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.Message;
import com.pbl.mapmo.domain.schedule.Schedule;
import com.pbl.mapmo.domain.schedule.ScheduleRepository;
import com.pbl.mapmo.domain.user.User;
import com.pbl.mapmo.domain.user.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 알림 서비스 클래스
 * 알림 생성, 조회, 푸시 알림 전송 등의 비즈니스 로직을 처리합니다.
 */
@Service
@Slf4j  // 로깅을 위한 Lombok 어노테이션
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;
    private final ScheduleRepository scheduleRepository;
    private final SimpMessagingTemplate simpMessagingTemplate;

    // 사용자 ID와 WebSocket 세션 ID를 매핑하는 맵 추가
    private final Map<Integer, String> userSessionMap = new HashMap<>();

    public NotificationService(NotificationRepository notificationRepository,
                               UserRepository userRepository,
                               ScheduleRepository scheduleRepository,
                               SimpMessagingTemplate simpMessagingTemplate) {
        this.notificationRepository = notificationRepository;
        this.userRepository = userRepository;
        this.scheduleRepository = scheduleRepository;
        this.simpMessagingTemplate = simpMessagingTemplate;
    }

    /**
     * 사용자를 알림 시스템에 연결합니다.
     *
     * @param user 연결할 사용자
     * @param sessionId 사용자의 WebSocket 세션 ID
     */
    public void connectUser(User user, String sessionId) {
        log.info("사용자 알림 시스템 연결: 사용자={}, 세션ID={}", user.getId(), sessionId);
        // 구현 예시:
        // 1. 사용자와 세션 ID를 매핑하여 저장
        userSessionMap.put(user.getId(), sessionId);

        // 2. 필요한 경우 미처리된 알림을 즉시 전송
        List<Notification> pendingNotifications = getUnreadNotifications(user);
        sendNotifications(user, pendingNotifications);

        // 현재는 로깅만 수행
        System.out.println("사용자 " + user.getId() + "가 세션 " + sessionId + "로 연결되었습니다.");
    }

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
     * 게시글 댓글에 대한 알림을 생성합니다.
     */
    @Transactional
    public void createCommentNotification(User articleOwner, User commenter, Long articleId, Long commentId) {
        if (articleOwner.getId().equals(commenter.getId())) {
            return; // 자신의 게시글에 자신이 댓글을 달았을 경우 알림 생성 안함
        }

        String title = "새 댓글 알림";
        String message = commenter.getName() + "님이 회원님의 게시글에 댓글을 남겼습니다.";

        Notification notification = saveNotification(articleOwner, title, message,
                NotificationType.NEW_COMMENT, articleId);

        // 푸시 알림 전송
        Map<String, String> data = Map.of(
                "articleId", articleId.toString(),
                "commentId", commentId.toString()
        );

        sendPushNotification(articleOwner, title, message,
                NotificationType.NEW_COMMENT, articleId, data);
    }

    /**
     * 게시글 좋아요에 대한 알림을 생성합니다.
     */
    @Transactional
    public void createArticleLikeNotification(User articleOwner, User liker, Long articleId) {
        if (articleOwner.getId().equals(liker.getId())) {
            return; // 자신의 게시글에 자신이 좋아요를 눌렀을 경우 알림 생성 안함
        }

        String title = "게시글 좋아요 알림";
        String message = liker.getName() + "님이 회원님의 게시글을 좋아합니다.";

        Notification notification = saveNotification(articleOwner, title, message,
                NotificationType.ARTICLE_LIKE, articleId);

        // 푸시 알림 전송
        Map<String, String> data = Map.of("articleId", articleId.toString());

        sendPushNotification(articleOwner, title, message,
                NotificationType.ARTICLE_LIKE, articleId, data);
    }

    /**
     * 게시글 싫어요에 대한 알림을 생성합니다.
     */
    @Transactional
    public void createArticleDislikeNotification(User articleOwner, User disliker, Long articleId) {
        if (articleOwner.getId().equals(disliker.getId())) {
            return; // 자신의 게시글에 자신이 싫어요를 눌렀을 경우 알림 생성 안함
        }

        String title = "게시글 싫어요 알림";
        String message = disliker.getName() + "님이 회원님의 게시글에 싫어요를 표시했습니다.";

        Notification notification = saveNotification(articleOwner, title, message,
                NotificationType.ARTICLE_DISLIKE, articleId);

        // 푸시 알림 전송
        Map<String, String> data = Map.of("articleId", articleId.toString());

        sendPushNotification(articleOwner, title, message,
                NotificationType.ARTICLE_DISLIKE, articleId, data);
    }

    /**
     * 친구 요청에 대한 알림을 생성합니다.
     */
    @Transactional
    public void createFriendRequestNotification(User receiver, User sender, Long friendRequestId) {
        String title = "새 친구 요청";
        String message = sender.getName() + "님이 친구 요청을 보냈습니다.";

        Notification notification = saveNotification(receiver, title, message,
                NotificationType.FRIEND_REQUEST, friendRequestId);

        // 푸시 알림 전송
        Map<String, String> data = Map.of(
                "friendRequestId", friendRequestId.toString(),
                "senderId", sender.getId().toString()
        );

        sendPushNotification(receiver, title, message,
                NotificationType.FRIEND_REQUEST, friendRequestId, data);
    }

    /**
     * 공유 일정 초대에 대한 알림을 생성합니다.
     */
    @Transactional
    public void createScheduleInvitationNotification(User receiver, User sender, Long scheduleId) {
        // Schedule 객체 조회 (ScheduleRepository 필요)
        Schedule schedule = scheduleRepository.findById(Math.toIntExact(scheduleId))
                .orElseThrow(() -> new IllegalArgumentException("해당 일정이 존재하지 않습니다: " + scheduleId));

        String title = "일정 초대";  // 알림 제목 설정
        String message = sender.getName() + "님이 '" + schedule.getTitle() + "' 일정에 초대했습니다.";

        Notification notification = saveNotification(receiver, title, message,
                NotificationType.SCHEDULE_INVITATION, scheduleId);

        // 푸시 알림 전송
        Map<String, String> data = Map.of(
                "scheduleId", scheduleId.toString(),
                "senderId", sender.getId().toString()
        );

        sendPushNotification(receiver, title, message,
                NotificationType.SCHEDULE_INVITATION, scheduleId, data);
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
     * 사용자에게 알림 목록을 전송합니다.
     *
     * @param user 알림을 받을 사용자
     * @param notifications 전송할 알림 목록
     */
    public void sendNotifications(User user, List<Notification> notifications) {
        if (notifications == null || notifications.isEmpty()) {
            return; // 전송할 알림이 없으면 종료
        }

        // 연결된 세션이 있는지 확인
        String sessionId = userSessionMap.get(user.getId());
        if (sessionId == null) {
            log.info("사용자 {}의 활성 세션이 없어 알림을 전송할 수 없습니다.", user.getId());
            return;
        }

        // WebSocket을 통해 알림 전송
        simpMessagingTemplate.convertAndSendToUser(
                sessionId,
                "/queue/notifications",
                notifications
        );

        log.info("사용자 {}에게 {} 개의 알림을 세션 {}로 전송했습니다.",
                user.getId(), notifications.size(), sessionId);
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