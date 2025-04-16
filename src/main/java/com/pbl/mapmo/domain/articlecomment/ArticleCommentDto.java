package com.pbl.mapmo.domain.articlecomment;

import com.pbl.mapmo.domain.article.Article;
import com.pbl.mapmo.domain.user.User;
import com.pbl.mapmo.domain.user.UserDto;
import lombok.*;

import jakarta.validation.constraints.NotBlank;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

public class ArticleCommentDto {

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Request {
        @NotBlank(message = "내용은 필수 입력값입니다")
        private String content;

        public ArticleComment toEntity(User user, Article article) {
            return ArticleComment.builder()
                    .content(content)
                    .user(user)
                    .article(article)
                    .build();
        }
    }

    @Getter
    @Builder
    public static class Response {
        private Integer id;
        private String content;
        private LocalDateTime createdDate;
        private LocalDateTime lastModifiedDate;
        private UserDto.Response user;

        public static Response of(ArticleComment comment) {
            return Response.builder()
                    .id(comment.getId())
                    .content(comment.getContent())
                    .createdDate(comment.getCreatedDate())
                    .lastModifiedDate(comment.getLastModifiedDate())
                    .user(comment.getUser() != null ? UserDto.Response.of(comment.getUser()) : null)
                    .build();
        }

        public static List<Response> of(List<ArticleComment> comments) {
            return comments.stream()
                    .map(Response::of)
                    .collect(Collectors.toList());
        }
    }
}