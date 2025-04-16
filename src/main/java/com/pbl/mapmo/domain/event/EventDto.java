package com.pbl.mapmo.domain.event;

import com.pbl.mapmo.domain.brand.BrandDto;
import lombok.*;

import jakarta.validation.constraints.NotBlank;

import java.time.LocalDate;
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

        // 추가된 필드
        private LocalDate startDate;
        private LocalDate endDate;

        public Event toEntity() {
            Event event = Event.builder()
                    .title(title)
                    .content(content)
                    .url(url)
                    .startDate(startDate)
                    .endDate(endDate)
                    .build();

            // 상태 초기화
            event.updateStatus();

            return event;
        }
    }

    @Getter
    @Builder
    public static class Response {
        private Integer id;
        private String title;
        private String content; // 이미지 URL
        private String url;
        private LocalDate startDate;
        private LocalDate endDate;
        private Event.EventStatus status;
        private BrandDto.Response brand;

        public static Response of(Event event) {
            return Response.builder()
                    .id(event.getId())
                    .title(event.getTitle())
                    .content(event.getContent())
                    .url(event.getUrl())
                    .startDate(event.getStartDate())
                    .endDate(event.getEndDate())
                    .status(event.getStatus())
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