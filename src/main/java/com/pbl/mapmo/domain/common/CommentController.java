package com.pbl.mapmo.domain.common;

import com.pbl.mapmo.domain.articlecomment.ArticleComment;
import com.pbl.mapmo.domain.schedulecomment.ScheduleComment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/comments")
public class CommentController {

    private final CommentService commentService;

    @Autowired
    public CommentController(CommentService commentService) {
        this.commentService = commentService;
    }

    /**
     * 게시글의 댓글 목록 조회 (페이징)
     */
    @GetMapping("/articles/{articleId}")
    public ResponseEntity<Page<ArticleComment>> getArticleComments(
            @PathVariable Integer articleId,
            @PageableDefault(size = 10) Pageable pageable,
            @RequestParam(required = false) Integer currentUserId) {
        Page<ArticleComment> comments = commentService.getArticleComments(articleId, pageable, currentUserId);
        return ResponseEntity.ok(comments);
    }

    /**
     * 게시글에 새 댓글 작성
     */
    @PostMapping("/articles/{articleId}")
    public ResponseEntity<ArticleComment> createArticleComment(
            @PathVariable Integer articleId,
            @RequestBody ArticleComment comment,
            @RequestParam Integer userId) {
        try {
            ArticleComment createdComment = commentService.createArticleComment(articleId, comment, userId);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdComment);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * 게시글 댓글 수정
     */
    @PutMapping("/articles/{commentId}")
    public ResponseEntity<ArticleComment> updateArticleComment(
            @PathVariable Integer commentId,
            @RequestBody ArticleComment comment,
            @RequestParam Integer userId) {
        try {
            ArticleComment updatedComment = commentService.updateArticleComment(commentId, comment, userId);
            return ResponseEntity.ok(updatedComment);
        } catch (RuntimeException e) {
            if (e.getMessage().contains("권한이 없습니다")) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * 게시글 댓글 삭제
     */
    @DeleteMapping("/articles/{commentId}")
    public ResponseEntity<Void> deleteArticleComment(
            @PathVariable Integer commentId,
            @RequestParam Integer userId) {
        try {
            commentService.deleteArticleComment(commentId, userId);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            if (e.getMessage().contains("권한이 없습니다")) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * 일정의 댓글 목록 조회
     */
    @GetMapping("/schedules/{scheduleId}")
    public ResponseEntity<List<ScheduleComment>> getScheduleComments(@PathVariable Integer scheduleId) {
        List<ScheduleComment> comments = commentService.getScheduleComments(scheduleId);
        return ResponseEntity.ok(comments);
    }

    /**
     * 일정에 새 댓글 작성
     */
    @PostMapping("/schedules/{scheduleId}")
    public ResponseEntity<ScheduleComment> createScheduleComment(
            @PathVariable Integer scheduleId,
            @RequestBody ScheduleComment comment,
            @RequestParam Integer userId) {
        try {
            ScheduleComment createdComment = commentService.createScheduleComment(scheduleId, comment, userId);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdComment);
        } catch (RuntimeException e) {
            if (e.getMessage().contains("공유되지 않은 일정")) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * 일정 댓글 수정
     */
    @PutMapping("/schedules/{commentId}")
    public ResponseEntity<ScheduleComment> updateScheduleComment(
            @PathVariable Integer commentId,
            @RequestBody ScheduleComment comment,
            @RequestParam Integer userId) {
        try {
            ScheduleComment updatedComment = commentService.updateScheduleComment(commentId, comment, userId);
            return ResponseEntity.ok(updatedComment);
        } catch (RuntimeException e) {
            if (e.getMessage().contains("권한이 없습니다")) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * 일정 댓글 삭제
     */
    @DeleteMapping("/schedules/{commentId}")
    public ResponseEntity<Void> deleteScheduleComment(
            @PathVariable Integer commentId,
            @RequestParam Integer userId) {
        try {
            commentService.deleteScheduleComment(commentId, userId);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            if (e.getMessage().contains("권한이 없습니다")) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
            return ResponseEntity.notFound().build();
        }
    }
}