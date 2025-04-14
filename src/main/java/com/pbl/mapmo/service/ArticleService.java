package com.pbl.mapmo.service;

import com.pbl.mapmo.entity.Article;
import com.pbl.mapmo.entity.User;
import com.pbl.mapmo.repository.ArticleRepository;
import com.pbl.mapmo.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class ArticleService {

    private final ArticleRepository articleRepository;
    private final UserRepository userRepository;

    @Autowired
    public ArticleService(ArticleRepository articleRepository, UserRepository userRepository) {
        this.articleRepository = articleRepository;
        this.userRepository = userRepository;
    }

    /**
     * 모든 게시글을 페이징하여 조회합니다.
     *
     * @param pageable 페이징 정보
     * @return 페이징된 게시글 목록
     */
    public Page<Article> getAllArticles(Pageable pageable) {
        return articleRepository.findAll(pageable);
    }

    /**
     * 특정 게시글을 ID로 조회합니다.
     *
     * @param articleId 게시글 ID
     * @return 조회된 게시글
     */
    @Transactional
    public Optional<Article> getArticleById(Integer articleId) {
        Optional<Article> articleOpt = articleRepository.findById(articleId);

        // 조회수 증가
        articleOpt.ifPresent(article -> {
            article.setViews(article.getViews() + 1);
            articleRepository.save(article);
        });

        return articleOpt;
    }

    /**
     * 새로운 게시글을 생성합니다.
     *
     * @param article 생성할 게시글 정보
     * @param userId 작성자 ID
     * @return 생성된 게시글
     */
    @Transactional
    public Article createArticle(Article article, Integer userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

        article.setUser(user);
        article.setViews(0);
        article.setLikes(0);
        article.setDislikes(0);

        return articleRepository.save(article);
    }

    /**
     * 기존 게시글을 수정합니다.
     *
     * @param articleId 수정할 게시글 ID
     * @param updatedArticle 수정된 게시글 정보
     * @param userId 사용자 ID (권한 확인용)
     * @return 수정된 게시글
     */
    @Transactional
    public Article updateArticle(Integer articleId, Article updatedArticle, Integer userId) {
        Article existingArticle = articleRepository.findById(articleId)
                .orElseThrow(() -> new RuntimeException("게시글을 찾을 수 없습니다."));

        // 권한 확인 (자신의 게시글만 수정 가능)
        if (!existingArticle.getUser().getId().equals(userId)) {
            throw new RuntimeException("이 게시글을 수정할 권한이 없습니다.");
        }

        // 필드 업데이트
        existingArticle.setTitle(updatedArticle.getTitle());
        existingArticle.setContent(updatedArticle.getContent());
        existingArticle.setLocation(updatedArticle.getLocation());
        existingArticle.setLatitude(updatedArticle.getLatitude());
        existingArticle.setLongitude(updatedArticle.getLongitude());

        return articleRepository.save(existingArticle);
    }

    /**
     * 게시글을 삭제합니다.
     *
     * @param articleId 삭제할 게시글 ID
     * @param userId 사용자 ID (권한 확인용)
     */
    @Transactional
    public void deleteArticle(Integer articleId, Integer userId) {
        Article article = articleRepository.findById(articleId)
                .orElseThrow(() -> new RuntimeException("게시글을 찾을 수 없습니다."));

        // 권한 확인 (자신의 게시글만 삭제 가능)
        if (!article.getUser().getId().equals(userId)) {
            throw new RuntimeException("이 게시글을 삭제할 권한이 없습니다.");
        }

        articleRepository.delete(article);
    }

    /**
     * 게시글에 좋아요를 추가합니다.
     *
     * @param articleId 게시글 ID
     * @return 업데이트된 게시글
     */
    @Transactional
    public Article likeArticle(Integer articleId) {
        Article article = articleRepository.findById(articleId)
                .orElseThrow(() -> new RuntimeException("게시글을 찾을 수 없습니다."));

        article.setLikes(article.getLikes() + 1);
        return articleRepository.save(article);
    }

    /**
     * 게시글에 싫어요를 추가합니다.
     *
     * @param articleId 게시글 ID
     * @return 업데이트된 게시글
     */
    @Transactional
    public Article dislikeArticle(Integer articleId) {
        Article article = articleRepository.findById(articleId)
                .orElseThrow(() -> new RuntimeException("게시글을 찾을 수 없습니다."));

        article.setDislikes(article.getDislikes() + 1);
        return articleRepository.save(article);
    }

    /**
     * 특정 위치 주변의 게시글을 검색합니다.
     *
     * @param latitude 위도
     * @param longitude 경도
     * @param radius 반경 (km)
     * @return 주변 게시글 목록
     */
    public List<Article> findArticlesNearby(Double latitude, Double longitude, Double radius) {
        // 실제 구현은 위도/경도 기반 거리 계산 로직이 필요합니다
        return articleRepository.findByLatitudeBetweenAndLongitudeBetween(
                latitude - radius, latitude + radius,
                longitude - radius, longitude + radius);
    }

    /**
     * 제목으로 게시글을 검색합니다.
     *
     * @param keyword 검색 키워드
     * @param pageable 페이징 정보
     * @return 검색된 게시글 목록
     */
    public Page<Article> searchArticlesByTitle(String keyword, Pageable pageable) {
        return articleRepository.findByTitleContaining(keyword, pageable);
    }

    /**
     * 내용으로 게시글을 검색합니다.
     *
     * @param keyword 검색 키워드
     * @param pageable 페이징 정보
     * @return 검색된 게시글 목록
     */
    public Page<Article> searchArticlesByContent(String keyword, Pageable pageable) {
        return articleRepository.findByContentContaining(keyword, pageable);
    }
}