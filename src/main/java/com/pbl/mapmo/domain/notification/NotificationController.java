package com.pbl.mapmo.domain.notification;

import com.pbl.mapmo.domain.user.User;
import com.pbl.mapmo.domain.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 알림 관련 API 컨트롤러
 * 사용자 알림 조회, 읽음 처리 등의 기능을 제공합니다.
 */
@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;
    private final UserRepository userRepository;

    /**
     * 사용자의 모든 알림 목록을 조회합니다.
     *
     * @param user 현재 인증된 사용자
     * @return 사용자의 모든 알림 목록
     */
    @GetMapping
    public ResponseEntity<List<Notification>> getNotifications(@AuthenticationPrincipal User user) {
        List<Notification> notifications = notificationService.getUserNotifications(user);
        return ResponseEntity.ok(notifications);
    }

    /**
     * 사용자의 읽지 않은 알림 목록을 조회합니다.
     *
     * @param user 현재 인증된 사용자
     * @return 읽지 않은 알림 목록
     */
    @GetMapping("/unread")
    public ResponseEntity<List<Notification>> getUnreadNotifications(@AuthenticationPrincipal User user) {
        List<Notification> notifications = notificationService.getUnreadNotifications(user);
        return ResponseEntity.ok(notifications);
    }

    /**
     * 사용자의 읽지 않은 알림 수를 조회합니다.
     *
     * @param user 현재 인증된 사용자
     * @return 읽지 않은 알림 수
     */
    @GetMapping("/count")
    public ResponseEntity<Map<String, Long>> getUnreadCount(@AuthenticationPrincipal User user) {
        long count = notificationService.getUnreadCount(user);
        return ResponseEntity.ok(Map.of("count", count));
    }

    /**
     * 특정 알림을 읽음 상태로 표시합니다.
     *
     * @param notificationId 읽음 처리할 알림 ID
     * @return 성공 응답
     */
    @PutMapping("/{notificationId}/read")
    public ResponseEntity<?> markAsRead(@PathVariable Long notificationId) {
        notificationService.markAsRead(notificationId);
        return ResponseEntity.ok().build();
    }

    /**
     * 사용자의 모든 알림을 읽음 상태로 표시합니다.
     *
     * @param user 현재 인증된 사용자
     * @return 성공 응답
     */
    @PutMapping("/read-all")
    public ResponseEntity<?> markAllAsRead(@AuthenticationPrincipal User user) {
        notificationService.markAllAsRead(user);
        return ResponseEntity.ok().build();
    }

    /**
     * 사용자의 FCM 토큰을 업데이트합니다.
     * 모바일 디바이스에서 푸시 알림을 받기 위한 설정입니다.
     *
     * @param authentication 현재 인증 정보
     * @param payload FCM 토큰을 포함한 요청 데이터
     * @return 성공 응답
     */
    @PutMapping("/token")
    public ResponseEntity<?> updateFcmToken(Authentication authentication,
                                            @RequestBody Map<String, String> payload) {
        // FCM 토큰 업데이트 로직
        return ResponseEntity.ok().build();
    }

    /**
     * WebSocket 연결 설정을 처리합니다.
     * 클라이언트가 실시간 알림을 받기 위해 연결할 때 호출됩니다.
     *
     * @param message 연결 메시지
     * @param headerAccessor WebSocket 헤더 정보
     */
    @MessageMapping("/notifications.connect")
    public void connect(@Payload Map<String, Object> message,
                        SimpMessageHeaderAccessor headerAccessor) {
        // 1. 메시지에서 사용자 ID 추출
        String userId = (String) message.get("userId");

        // 2. 세션에 사용자 정보 저장
        if (userId != null && !userId.isEmpty()) {
            // Integer로 파싱하여 사용 (Long 대신)
            User user = userRepository.findById(Integer.parseInt(userId))
                    .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다"));

            // 세션에 사용자 정보 저장
            headerAccessor.getSessionAttributes().put("USER_ID", userId);

            // 3. 사용자를 알림 시스템에 연결
            notificationService.connectUser(user, headerAccessor.getSessionId());

            // 4. 로그 기록 (선택사항)
            System.out.println("사용자 " + userId + "가 알림 시스템에 연결되었습니다. 세션 ID: " +
                    headerAccessor.getSessionId());
        }
    }
}