package com.pbl.mapmo.dto;

import com.pbl.mapmo.entity.ScheduleComment;
import lombok.*;

import jakarta.validation.constraints.NotBlank;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

public class ScheduleCommentDTO {

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Request {
        @NotBlank(message = "내용은 필수 입력값입니다")
        private String content;

        public ScheduleComment toEntity() {
            return ScheduleComment.builder()
                    .content(content)
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
        private UserDTO.Response user;

        public static Response of(ScheduleComment comment) {
            return Response.builder()
                    .id(comment.getId())
                    .content(comment.getContent())
                    .createdDate(comment.getCreatedDate())
                    .lastModifiedDate(comment.getLastModifiedDate())
                    .user(comment.getUser() != null ? UserDTO.Response.of(comment.getUser()) : null)
                    .build();
        }

        public static List<Response> of(List<ScheduleComment> comments) {
            return comments.stream()
                    .map(Response::of)
                    .collect(Collectors.toList());
        }
    }
}
