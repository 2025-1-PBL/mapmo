package com.pbl.mapmo.repository;

import com.pbl.mapmo.entity.Article;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.data.repository.query.Param;

import java.util.List;

@Repository
public interface ArticleRepository extends JpaRepository<Article, Integer> {
    List<Article> findByUserId(Integer userId);
    Page<Article> findByUserId(Integer userId, Pageable pageable);
    List<Article> findByLocationContaining(String location);
    Page<Article> findByLocationContaining(String location, Pageable pageable);
    List<Article> findByTitleContaining(String keyword);
    Page<Article> findByTitleContaining(String keyword, Pageable pageable);
    List<Article> findByContentContaining(String keyword);
    Page<Article> findByContentContaining(String keyword, Pageable pageable);
    List<Article> findByTitleContainingOrContentContaining(String title, String content);
    Page<Article> findByTitleContainingOrContentContaining(String title, String content, Pageable pageable);

    @Query(value = "SELECT * FROM schedule s WHERE " +
            "ST_Distance_Sphere(point(s.longitude, s.latitude), point(:lng, :lat)) <= :distance * 1000",
            nativeQuery = true)
    List<Article> findNearbyArticles(
            @Param("lat") Double lat,
            @Param("lng") Double lng,
            @Param("distance") Double distance);

    @Query("SELECT a FROM Article a ORDER BY a.views DESC")
    List<Article> findTopByViews(Pageable pageable);

    @Query("SELECT a FROM Article a ORDER BY a.likes DESC")
    List<Article> findTopByLikes(Pageable pageable);

}
