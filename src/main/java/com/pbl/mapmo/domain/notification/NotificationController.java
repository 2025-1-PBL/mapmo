package com.pbl.mapmo.domain.notification;

import com.pbl.mapmo.common.service.CustomUserDetails;
import com.pbl.mapmo.domain.user.User;
import com.pbl.mapmo.domain.user.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 알림 관련 API 컨트롤러
 * 사용자 알림 조회, 읽음 처리 등의 기능을 제공합니다.
 */
@Slf4j
@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;
    private final UserRepository userRepository;

    private NotificationDTO convertToDTO(Notification notification) {
        return NotificationDTO.builder()
                .id(notification.getId())
                .userId(Long.valueOf(notification.getUser().getId()))
                .title(notification.getTitle())
                .message(notification.getMessage())
                .isRead(notification.isRead())
                .createdAt(notification.getCreatedAt())
                .type(notification.getType())
                .referenceId(notification.getReferenceId())
                // 필요한 경우 추가 정보
                .build();
    }

    /**
     * 사용자의 모든 알림 목록을 조회합니다.
     *
     * @param userDetails 현재 인증된 사용자
     * @return 사용자의 모든 알림 목록
     */
    @GetMapping
    public ResponseEntity<List<NotificationDTO>> getNotifications(@AuthenticationPrincipal CustomUserDetails userDetails) {
        User user = userDetails.getUser();
        log.info("알림 목록 요청: 사용자={}", user.getId());
        List<Notification> notifications = notificationService.getUserNotifications(user);
        List<NotificationDTO> dtos = notifications.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
        log.debug("알림 목록 반환: 사용자={}, 알림 수={}", user.getId(), dtos.size());
        return ResponseEntity.ok(dtos);
    }

    /**
     * 사용자의 읽지 않은 알림 목록을 조회합니다.
     *
     * @param userDetails 현재 인증된 사용자
     * @return 읽지 않은 알림 목록
     */
    @GetMapping("/unread")
    public ResponseEntity<List<NotificationDTO>> getUnreadNotifications(@AuthenticationPrincipal CustomUserDetails userDetails) {
        User user = userDetails.getUser();
        List<Notification> notifications = notificationService.getUnreadNotifications(user);
        List<NotificationDTO> dtos = notifications.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    /**
     * 사용자의 읽지 않은 알림 수를 조회합니다.
     *
     * @param userDetails 현재 인증된 사용자
     * @return 읽지 않은 알림 수
     */
    @GetMapping("/count")
    public ResponseEntity<Map<String, Long>> getUnreadCount(@AuthenticationPrincipal CustomUserDetails userDetails) {
        User user = userDetails.getUser();
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
     * @param userDetails 현재 인증된 사용자
     * @return 성공 응답
     */
    @PutMapping("/read-all")
    public ResponseEntity<?> markAllAsRead(@AuthenticationPrincipal CustomUserDetails userDetails) {
        User user = userDetails.getUser();
        notificationService.markAllAsRead(user);
        return ResponseEntity.ok().build();
    }

    /**
     * 사용자의 FCM 토큰을 업데이트합니다.
     * 모바일 디바이스에서 푸시 알림을 받기 위한 설정입니다.
     *
     * @param authentication 현재 인증 정보
     * @param request FCM 토큰을 포함한 요청 데이터
     * @return 성공 응답
     */
    @PostMapping("/fcm-token")
    public ResponseEntity<?> updateFcmToken(Authentication authentication, @RequestBody FcmTokenRequest request) {
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        User user = userRepository.findById(userDetails.getId())
                .orElseThrow(() -> new UsernameNotFoundException("사용자를 찾을 수 없습니다."));

        user.setFcmToken(request.getFcmToken());
        userRepository.save(user);

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
        log.info("웹소켓 알림 연결 요청: 사용자ID={}", userId);

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

        log.debug("웹소켓 알림 연결 완료: 사용자ID={}, 세션ID={}",
                userId, headerAccessor.getSessionId());
    }
}