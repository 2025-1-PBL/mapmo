package com.pbl.mapmo.domain.common;

import com.pbl.mapmo.domain.article.Article;
import com.pbl.mapmo.domain.article.ArticleRepository;
import com.pbl.mapmo.domain.articlecomment.ArticleComment;
import com.pbl.mapmo.domain.articlecomment.ArticleCommentRepository;
import com.pbl.mapmo.domain.block.BlockService;
import com.pbl.mapmo.domain.schedule.Schedule;
import com.pbl.mapmo.domain.schedule.ScheduleRepository;
import com.pbl.mapmo.domain.schedulecomment.ScheduleComment;
import com.pbl.mapmo.domain.schedulecomment.ScheduleCommentRepository;
import com.pbl.mapmo.domain.user.User;
import com.pbl.mapmo.domain.user.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageImpl;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CommentService {

    private final ArticleCommentRepository articleCommentRepository;
    private final ScheduleCommentRepository scheduleCommentRepository;
    private final ArticleRepository articleRepository;
    private final ScheduleRepository scheduleRepository;
    private final UserRepository userRepository;
    private final BlockService blockService;

    @Autowired
    public CommentService(ArticleCommentRepository articleCommentRepository,
                          ScheduleCommentRepository scheduleCommentRepository,
                          ArticleRepository articleRepository,
                          ScheduleRepository scheduleRepository,
                          UserRepository userRepository,
                          BlockService blockService) {
        this.articleCommentRepository = articleCommentRepository;
        this.scheduleCommentRepository = scheduleCommentRepository;
        this.articleRepository = articleRepository;
        this.scheduleRepository = scheduleRepository;
        this.userRepository = userRepository;
        this.blockService = blockService;
    }

    /**
     * 게시글의 모든 댓글을 페이징하여 조회합니다.
     *
     * @param articleId 게시글 ID
     * @param pageable 페이징 정보
     * @param currentUserId 현재 사용자 ID (차단 사용자 필터링용)
     * @return 페이징된 댓글 목록
     */
    public Page<ArticleComment> getArticleComments(Integer articleId, Pageable pageable, Integer currentUserId) {
        Page<ArticleComment> comments = articleCommentRepository.findByArticleId(articleId, pageable);

        // 차단 사용자 댓글 필터링 (필요한 경우)
        if (currentUserId != null) {
            List<Integer> blockedUserIds = blockService.getBlockedUserIds(currentUserId);

            // 필터링된 결과를 리스트로 변환
            List<ArticleComment> filteredComments = comments.stream()
                    .filter(comment -> comment.getUser() == null ||
                            !blockedUserIds.contains(comment.getUser().getId()))
                    .collect(Collectors.toList());

            // 필터링된 리스트를 사용하여 새로운 Page 객체 생성
            return new PageImpl<>(filteredComments, pageable, filteredComments.size());
        }

        return comments;
    }


    /**
     * 게시글에 새로운 댓글을 작성합니다.
     *
     * @param articleId 게시글 ID
     * @param comment 댓글 내용
     * @param userId 작성자 ID
     * @return 작성된 댓글
     */
    @Transactional
    public ArticleComment createArticleComment(Integer articleId, ArticleComment comment, Integer userId) {
        Article article = articleRepository.findById(articleId)
                .orElseThrow(() -> new RuntimeException("게시글을 찾을 수 없습니다."));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

        comment.setArticle(article);
        comment.setUser(user);

        return articleCommentRepository.save(comment);
    }

    /**
     * 게시글 댓글을 수정합니다.
     *
     * @param commentId 수정할 댓글 ID
     * @param updatedComment 수정된 댓글 내용
     * @param userId 사용자 ID (권한 확인용)
     * @return 수정된 댓글
     */
    @Transactional
    public ArticleComment updateArticleComment(Integer commentId, ArticleComment updatedComment, Integer userId) {
        ArticleComment existingComment = articleCommentRepository.findById(commentId)
                .orElseThrow(() -> new RuntimeException("댓글을 찾을 수 없습니다."));

        // 권한 확인 (자신의 댓글만 수정 가능)
        if (!existingComment.getUser().getId().equals(userId)) {
            throw new RuntimeException("이 댓글을 수정할 권한이 없습니다.");
        }

        existingComment.setContent(updatedComment.getContent());
        return articleCommentRepository.save(existingComment);
    }

    /**
     * 게시글 댓글을 삭제합니다.
     *
     * @param commentId 삭제할 댓글 ID
     * @param userId 사용자 ID (권한 확인용)
     */
    @Transactional
    public void deleteArticleComment(Integer commentId, Integer userId) {
        ArticleComment comment = articleCommentRepository.findById(commentId)
                .orElseThrow(() -> new RuntimeException("댓글을 찾을 수 없습니다."));

        // 권한 확인 (자신의 댓글만 삭제 가능)
        if (!comment.getUser().getId().equals(userId)) {
            throw new RuntimeException("이 댓글을 삭제할 권한이 없습니다.");
        }

        articleCommentRepository.delete(comment);
    }

    /**
     * 일정의 모든 댓글을 조회합니다.
     *
     * @param scheduleId 일정 ID
     * @return 댓글 목록
     */
    public List<ScheduleComment> getScheduleComments(Integer scheduleId) {
        return scheduleCommentRepository.findByScheduleId(scheduleId);
    }

    /**
     * 일정에 새로운 댓글을 작성합니다.
     *
     * @param scheduleId 일정 ID
     * @param comment 댓글 내용
     * @param userId 작성자 ID
     * @return 작성된 댓글
     */
    @Transactional
    public ScheduleComment createScheduleComment(Integer scheduleId, ScheduleComment comment, Integer userId) {
        Schedule schedule = scheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new RuntimeException("일정을 찾을 수 없습니다."));

        // 공유된 일정만 댓글 작성 가능
        if (!Boolean.TRUE.equals(schedule.getIsShared())) {
            throw new RuntimeException("공유되지 않은 일정에는 댓글을 작성할 수 없습니다.");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

        comment.setSchedule(schedule);
        comment.setUser(user);

        return scheduleCommentRepository.save(comment);
    }

    /**
     * 일정 댓글을 수정합니다.
     *
     * @param commentId 수정할 댓글 ID
     * @param updatedComment 수정된 댓글 내용
     * @param userId 사용자 ID (권한 확인용)
     * @return 수정된 댓글
     */
    @Transactional
    public ScheduleComment updateScheduleComment(Integer commentId, ScheduleComment updatedComment, Integer userId) {
        ScheduleComment existingComment = scheduleCommentRepository.findById(commentId)
                .orElseThrow(() -> new RuntimeException("댓글을 찾을 수 없습니다."));

        // 권한 확인 (자신의 댓글만 수정 가능)
        if (!existingComment.getUser().getId().equals(userId)) {
            throw new RuntimeException("이 댓글을 수정할 권한이 없습니다.");
        }

        existingComment.setContent(updatedComment.getContent());
        return scheduleCommentRepository.save(existingComment);
    }

    /**
     * 일정 댓글을 삭제합니다.
     *
     * @param commentId 삭제할 댓글 ID
     * @param userId 사용자 ID (권한 확인용)
     */
    @Transactional
    public void deleteScheduleComment(Integer commentId, Integer userId) {
        ScheduleComment comment = scheduleCommentRepository.findById(commentId)
                .orElseThrow(() -> new RuntimeException("댓글을 찾을 수 없습니다."));

        // 권한 확인 (자신의 댓글만 삭제 가능)
        if (!comment.getUser().getId().equals(userId)) {
            throw new RuntimeException("이 댓글을 삭제할 권한이 없습니다.");
        }

        scheduleCommentRepository.delete(comment);
    }
}