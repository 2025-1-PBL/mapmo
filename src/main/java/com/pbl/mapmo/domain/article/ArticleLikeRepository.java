package com.pbl.mapmo.domain.article;

import com.pbl.mapmo.domain.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ArticleLikeRepository extends JpaRepository<ArticleLike, Integer> {
    Optional<ArticleLike> findByArticleAndUser(Article article, User user);
    boolean existsByArticleAndUser(Article article, User user);
    void deleteByArticleAndUser(Article article, User user);
    int countByArticle(Article article);
}