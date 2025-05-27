package com.pbl.mapmo.domain.notification;

import com.pbl.mapmo.domain.article.Article;
import com.pbl.mapmo.domain.article.ArticleRepository;
import com.pbl.mapmo.domain.articlecomment.ArticleComment;
import com.pbl.mapmo.domain.articlecomment.ArticleCommentRepository;
import com.pbl.mapmo.domain.user.User;
import com.pbl.mapmo.domain.user.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 알림 스케줄러 컴포넌트
 * 특정 시간에 자동으로 실행되어 필요한 알림을 생성하고 전송합니다.
 */
@Component
@Slf4j  // 로깅을 위한 Lombok 어노테이션
public class NotificationScheduler {

    private final UserRepository userRepository;
    private final NotificationService notificationService;
    private final ArticleCommentRepository articleCommentRepository;
    private final ArticleRepository articleRepository;

    /**
     * 생성자를 통한 의존성 주입
     */
    @Autowired
    public NotificationScheduler(
            UserRepository userRepository,
            NotificationService notificationService,
            ArticleCommentRepository articleCommentRepository,
            ArticleRepository articleRepository) {
        this.userRepository = userRepository;
        this.notificationService = notificationService;
        this.articleCommentRepository = articleCommentRepository;
        this.articleRepository = articleRepository;
    }

    /**
     * 이벤트 알림 전송 메서드 (현재 비활성화 상태)
     * 브랜드 행사 정보는 푸시 알림을 보내지 않도록 설정됨
     */
    // @Scheduled(cron = "0 0 8 * * ?")  // 매일 오전 8시에 실행
    @Transactional(readOnly = true)
    public void sendEventReminders() {
        // 기존 코드 유지 (필요시 비활성화)
    }

    /**
     * 새 댓글 알림 스케줄러
     * 매일 정오(또는 application.yml에 설정된 시간)에 실행되어
     * 지난 24시간 동안 작성된 댓글에 대해 게시글 작성자에게 알림을 전송합니다.
     */
    @Scheduled(cron = "${notification.comment.schedule:0 0 12 * * ?}")  // 기본값은 매일 정오
    @Transactional(readOnly = true)
    public void sendNewCommentNotifications() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime yesterday = now.minusDays(1);

        log.info("새 댓글 알림 스케줄러 실행: {}", now);

        // 지난 24시간 동안 작성된 새 댓글 조회
        List<ArticleComment> newComments = articleCommentRepository.findByCreatedAtBetween(
                yesterday, now
        );

        log.info("새 댓글 수: {}", newComments.size());

        // 각 댓글에 대해 글 작성자에게 알림 전송
        for (ArticleComment comment : newComments) {
            Article article = comment.getArticle();
            User articleAuthor = article.getUser();

            // 본인이 작성한 댓글에는 알림을 보내지 않음
            if (comment.getUser().getId().equals(articleAuthor.getId())) {
                continue;
            }

            sendCommentNotificationToUser(articleAuthor, comment, article);
        }
    }

    /**
     * 댓글 알림을 특정 사용자에게 전송하는 헬퍼 메서드
     *
     * @param user 알림을 받을 사용자 (게시글 작성자)
     * @param comment 작성된 댓글
     * @param article 댓글이 작성된 게시글
     */
    private void sendCommentNotificationToUser(User user, ArticleComment comment, Article article) {
        // FCM 토큰이 없으면 알림을 보낼 수 없음
        if (user.getFcmToken() == null) {
            log.warn("사용자 {}의 FCM 토큰이 없습니다.", user.getId());
            return;
        }

        String title = "새 댓글 알림";
        String message = String.format("회원님의 게시글 '%s'에 새 댓글이 달렸습니다.",
                article.getTitle());

        // 알림에 필요한 추가 데이터 설정
        Map<String, String> data = new HashMap<>();
        data.put("type", "NEW_COMMENT");
        data.put("articleId", article.getId().toString());
        data.put("commentId", comment.getId().toString());

        // 알림 서비스를 통해 푸시 알림 전송
        notificationService.sendPushNotification(
                user,
                title,
                message,
                NotificationType.NEW_COMMENT,
                Long.valueOf(comment.getId()),
                data
        );
    }
}