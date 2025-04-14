package com.pbl.mapmo.dto;

import com.pbl.mapmo.entity.Event;
import lombok.*;

import jakarta.validation.constraints.NotBlank;
import java.util.List;
import java.util.stream.Collectors;

public class EventDto {

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Request {
        @NotBlank(message = "제목은 필수 입력값입니다")
        private String title;

        private String content;
        private String url;
        private Integer brandId;

        public Event toEntity() {
            return Event.builder()
                    .title(title)
                    .content(content)
                    .url(url)
                    .build();
        }
    }

    @Getter
    @Builder
    public static class Response {
        private Integer id;
        private String title;
        private String content;
        private String url;
        private BrandDto.Response brand;

        public static Response of(Event event) {
            return Response.builder()
                    .id(event.getId())
                    .title(event.getTitle())
                    .content(event.getContent())
                    .url(event.getUrl())
                    .brand(event.getBrand() != null ?
                            BrandDto.Response.builder()
                                    .id(event.getBrand().getId())
                                    .name(event.getBrand().getName())
                                    .build() : null)
                    .build();
        }

        public static List<Response> of(List<Event> events) {
            return events.stream()
                    .map(Response::of)
                    .collect(Collectors.toList());
        }
    }
}