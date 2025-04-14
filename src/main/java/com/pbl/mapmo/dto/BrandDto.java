package com.pbl.mapmo.dto;

import com.pbl.mapmo.entity.Brand;
import lombok.*;

import jakarta.validation.constraints.NotBlank;
import java.util.List;
import java.util.stream.Collectors;

public class BrandDto {

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Request {
        @NotBlank(message = "이름은 필수 입력값입니다")
        private String name;

        public Brand toEntity() {
            return Brand.builder()
                    .name(name)
                    .build();
        }
    }

    @Getter
    @Builder
    public static class Response {
        private Integer id;
        private String name;
        private List<FranchiseDto.Response> franchises;
        private List<EventDto.Response> events;

        public static Response of(Brand brand) {
            return Response.builder()
                    .id(brand.getId())
                    .name(brand.getName())
                    .franchises(brand.getFranchises() != null ?
                            FranchiseDto.Response.of(brand.getFranchises()) : null)
                    .events(brand.getEvents() != null ?
                            EventDto.Response.of(brand.getEvents()) : null)
                    .build();
        }

        public static List<Response> of(List<Brand> brands) {
            return brands.stream()
                    .map(Response::of)
                    .collect(Collectors.toList());
        }
    }
}
