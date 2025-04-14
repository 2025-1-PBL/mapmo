package com.pbl.mapmo.repository;

import com.pbl.mapmo.entity.ArticleComment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ArticleCommentRepository extends JpaRepository<ArticleComment, Integer> {
    List<ArticleComment> findByArticleId(Integer articleId);
    Page<ArticleComment> findByArticleId(Integer articleId, Pageable pageable);
    List<ArticleComment> findByUserId(Integer userId);
    void deleteByArticleId(Integer articleId);
}
