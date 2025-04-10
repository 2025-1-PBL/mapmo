package com.pbl.mapmo.repository;

import com.pbl.mapmo.entity.ArticleComment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ArticleCommentRepository extends JpaRepository<ArticleComment, Integer> {
    List<ArticleComment> findByArticleId(Integer articleId);
    List<ArticleComment> findByUserId(Integer userId);
    void deleteByArticleId(Integer articleId);
}
