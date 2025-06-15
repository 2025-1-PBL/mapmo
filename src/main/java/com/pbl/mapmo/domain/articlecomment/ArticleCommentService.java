package com.pbl.mapmo.domain.articlecomment;

import com.pbl.mapmo.domain.article.Article;
import com.pbl.mapmo.domain.article.ArticleRepository;
import com.pbl.mapmo.domain.user.UserRepository;
import com.pbl.mapmo.domain.notification.NotificationMessage;
import com.pbl.mapmo.domain.notification.NotificationService;
import com.pbl.mapmo.domain.notification.NotificationType;
import com.pbl.mapmo.domain.user.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Service
public class ArticleCommentService {

    private final ArticleCommentRepository commentRepository;
    private final ArticleRepository articleRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;
    private final SimpMessageSendingOperations messagingTemplate;

    @Autowired
    public ArticleCommentService(ArticleCommentRepository commentRepository,
                                 ArticleRepository articleRepository,
                                 UserRepository userRepository,
                                 NotificationService notificationService,
                                 SimpMessageSendingOperations messagingTemplate) { // 생성자 매개변수 추가
        this.commentRepository = commentRepository;
        this.articleRepository = articleRepository;
        this.userRepository = userRepository;
        this.notificationService = notificationService;
        this.messagingTemplate = messagingTemplate; // 필드 초기화
    }
    @Transactional
    public ArticleComment createComment(ArticleComment comment, Integer articleId, Integer userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

        Article article = articleRepository.findById(articleId)
                .orElseThrow(() -> new RuntimeException("게시글을 찾을 수 없습니다."));

        comment.setArticle(article);
        comment.setUser(user);

        ArticleComment savedComment = commentRepository.save(comment);

        // 게시글 작성자에게 알림 전송 (자신의 게시글에는 알림이 가지 않도록 함)
        if (!article.getUser().getId().equals(userId)) {
            notificationService.createCommentNotification(
                    article.getUser(),
                    user,
                    articleId.longValue(),
                    savedComment.getId().longValue()
            );
        }

        return savedComment;
    }

    private void sendRealTimeCommentNotification(User user, ArticleComment comment, Article article) {
        String title = "새 댓글 알림";
        String message = String.format("회원님의 게시글 '%s'에 새 댓글이 달렸습니다.", article.getTitle());

        Map<String, String> data = new HashMap<>();
        data.put("type", "NEW_COMMENT");
        data.put("articleId", article.getId().toString());
        data.put("commentId", comment.getId().toString());

        // 1. 푸시 알림 전송 (FCM)
        if (user.getFcmToken() != null) {
            notificationService.sendPushNotification(
                    user,
                    title,
                    message,
                    NotificationType.NEW_COMMENT,
                    Long.valueOf(comment.getId()),
                    data
            );
        }

        // 2. WebSocket 실시간 알림 전송
        NotificationMessage notificationMessage = NotificationMessage.builder()
                .title(title)
                .message(message)
                .type(NotificationType.NEW_COMMENT)
                .referenceId(Long.valueOf(comment.getId()))
                .userId(Long.valueOf(user.getId()))
                .createdAt(LocalDateTime.now())
                .read(false)
                .data(data)
                .build();

        messagingTemplate.convertAndSendToUser(
                user.getId().toString(),
                "/queue/notifications",
                notificationMessage
        );
    }
}