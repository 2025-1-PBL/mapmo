package com.pbl.mapmo.domain.article;

import com.pbl.mapmo.common.service.CustomUserDetails;
import com.pbl.mapmo.domain.user.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/articles")
public class ArticleController {

    private final ArticleService articleService;

    @Autowired
    public ArticleController(ArticleService articleService) {
        this.articleService = articleService;
    }

    /**
     * 모든 게시글을 페이징하여 조회합니다.
     *
     * @param pageable 페이징 정보
     * @return 페이징된 게시글 목록
     */
    @GetMapping
    public ResponseEntity<Page<ArticleDto.Response>> getAllArticles(
            @PageableDefault(size = 10) Pageable pageable) {
        Page<Article> articles = articleService.getAllArticles(pageable);
        Page<ArticleDto.Response> response = articles.map(ArticleDto.Response::of);
        return ResponseEntity.ok(response);
    }

    /**
     * 특정 게시글을 ID로 조회합니다.
     *
     * @param articleId 게시글 ID
     * @return 조회된 게시글
     */
    @GetMapping("/{articleId}")
    public ResponseEntity<ArticleDto.Response> getArticleById(@PathVariable Integer articleId) {
        Optional<Article> article = articleService.getArticleById(articleId);
        return article
                .map(value -> ResponseEntity.ok(ArticleDto.Response.of(value)))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    /**
     * 새로운 게시글을 생성합니다.
     *
     * @param request 생성할 게시글 정보
     * @param userDetails 인증된 사용자 정보
     * @return 생성된 게시글
     */
    @PostMapping
    public ResponseEntity<ArticleDto.Response> createArticle(
            @Valid @RequestBody ArticleDto.Request request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        Integer userId = userDetails.getId();
        Article article = request.toEntity(null); // 임시로 null 전달, 서비스에서 유저 조회
        Article createdArticle = articleService.createArticle(article, userId);
        return new ResponseEntity<>(ArticleDto.Response.of(createdArticle), HttpStatus.CREATED);
    }

    /**
     * 기존 게시글을 수정합니다.
     *
     * @param articleId 수정할 게시글 ID
     * @param request 수정된 게시글 정보
     * @param userDetails 인증된 사용자 정보
     * @return 수정된 게시글
     */
    @PutMapping("/{articleId}")
    public ResponseEntity<ArticleDto.Response> updateArticle(
            @PathVariable Integer articleId,
            @Valid @RequestBody ArticleDto.Request request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        Integer userId = userDetails.getId();
        Article article = request.toEntity(null); // 임시로 null 전달, 서비스에서 유저 조회
        try {
            Article updatedArticle = articleService.updateArticle(articleId, article, userId);
            return ResponseEntity.ok(ArticleDto.Response.of(updatedArticle));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
    }

    /**
     * 게시글을 삭제합니다.
     *
     * @param articleId 삭제할 게시글 ID
     * @param userDetails 인증된 사용자 정보
     * @return 삭제 결과
     */
    @DeleteMapping("/{articleId}")
    public ResponseEntity<Void> deleteArticle(
            @PathVariable Integer articleId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        try {
            articleService.deleteArticle(articleId, userDetails.getId());
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
    }

    /**
     * 게시글에 좋아요를 추가합니다.
     *
     * @param articleId 게시글 ID
     * @return 업데이트된 게시글
     */
    @PostMapping("/{articleId}/like")
    public ResponseEntity<ArticleDto.Response> likeArticle(@PathVariable Integer articleId) {
        try {
            Article article = articleService.likeArticle(articleId);
            return ResponseEntity.ok(ArticleDto.Response.of(article));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * 게시글에 싫어요를 추가합니다.
     *
     * @param articleId 게시글 ID
     * @return 업데이트된 게시글
     */
    @PostMapping("/{articleId}/dislike")
    public ResponseEntity<ArticleDto.Response> dislikeArticle(@PathVariable Integer articleId) {
        try {
            Article article = articleService.dislikeArticle(articleId);
            return ResponseEntity.ok(ArticleDto.Response.of(article));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * 특정 위치 주변의 게시글을 검색합니다.
     *
     * @param latitude 위도
     * @param longitude 경도
     * @param radius 반경 (km)
     * @return 주변 게시글 목록
     */
    @GetMapping("/nearby")
    public ResponseEntity<List<ArticleDto.Response>> findArticlesNearby(
            @RequestParam Double latitude,
            @RequestParam Double longitude,
            @RequestParam Double radius) {
        List<Article> articles = articleService.findArticlesNearby(latitude, longitude, radius);
        return ResponseEntity.ok(ArticleDto.Response.of(articles));
    }

    /**
     * 제목으로 게시글을 검색합니다.
     *
     * @param keyword 검색 키워드
     * @param pageable 페이징 정보
     * @return 검색된 게시글 목록
     */
    @GetMapping("/search/title")
    public ResponseEntity<Page<ArticleDto.Response>> searchArticlesByTitle(
            @RequestParam String keyword,
            @PageableDefault(size = 10) Pageable pageable) {
        Page<Article> articles = articleService.searchArticlesByTitle(keyword, pageable);
        Page<ArticleDto.Response> response = articles.map(ArticleDto.Response::of);
        return ResponseEntity.ok(response);
    }

    /**
     * 내용으로 게시글을 검색합니다.
     *
     * @param keyword 검색 키워드
     * @param pageable 페이징 정보
     * @return 검색된 게시글 목록
     */
    @GetMapping("/search/content")
    public ResponseEntity<Page<ArticleDto.Response>> searchArticlesByContent(
            @RequestParam String keyword,
            @PageableDefault(size = 10) Pageable pageable) {
        Page<Article> articles = articleService.searchArticlesByContent(keyword, pageable);
        Page<ArticleDto.Response> response = articles.map(ArticleDto.Response::of);
        return ResponseEntity.ok(response);
    }
}