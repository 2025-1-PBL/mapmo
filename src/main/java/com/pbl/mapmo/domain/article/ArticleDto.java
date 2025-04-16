package com.pbl.mapmo.domain.article;

import com.pbl.mapmo.domain.articlecomment.ArticleCommentDto;
import com.pbl.mapmo.domain.user.User;
import com.pbl.mapmo.domain.user.UserDto;
import lombok.*;

import jakarta.validation.constraints.NotBlank;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

public class ArticleDto {

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Request {
        @NotBlank(message = "제목은 필수 입력값입니다")
        private String title;

        private String content;
        private String location;
        private Double latitude;
        private Double longitude;

        public Article toEntity(User user) {
            return Article.builder()
                    .title(title)
                    .content(content)
                    .location(location)
                    .latitude(latitude)
                    .longitude(longitude)
                    .views(0)
                    .likes(0)
                    .dislikes(0)
                    .user(user)
                    .build();
        }
    }

    @Getter
    @Builder
    public static class Response {
        private Integer id;
        private String title;
        private String content;
        private Integer views;
        private Integer likes;
        private Integer dislikes;
        private String location;
        private Double latitude;
        private Double longitude;
        private LocalDateTime createdDate;
        private LocalDateTime lastModifiedDate;
        private UserDto.Response user;
        private List<ArticleCommentDto.Response> comments;

        public static Response of(Article article) {
            return Response.builder()
                    .id(article.getId())
                    .title(article.getTitle())
                    .content(article.getContent())
                    .views(article.getViews())
                    .likes(article.getLikes())
                    .dislikes(article.getDislikes())
                    .location(article.getLocation())
                    .latitude(article.getLatitude())
                    .longitude(article.getLongitude())
                    .createdDate(article.getCreatedDate())
                    .lastModifiedDate(article.getLastModifiedDate())
                    .user(UserDto.Response.of(article.getUser()))
                    .comments(article.getArticleComments() != null ?
                            ArticleCommentDto.Response.of(article.getArticleComments()) : null)
                    .build();
        }

        public static List<Response> of(List<Article> articles) {
            return articles.stream()
                    .map(Response::of)
                    .collect(Collectors.toList());
        }
    }
}