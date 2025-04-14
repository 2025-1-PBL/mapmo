package com.pbl.mapmo.dto;

import com.pbl.mapmo.entity.Franchise;
import lombok.*;

import jakarta.validation.constraints.NotBlank;
import java.util.List;
import java.util.stream.Collectors;

public class FranchiseDto {

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Request {
        @NotBlank(message = "이름은 필수 입력값입니다")
        private String name;

        private String location;
        private Double latitude;
        private Double longitude;
        private Integer brandId;

        public Franchise toEntity() {
            return Franchise.builder()
                    .name(name)
                    .location(location)
                    .latitude(latitude)
                    .longitude(longitude)
                    .build();
        }
    }

    @Getter
    @Builder
    public static class Response {
        private Integer id;
        private String name;
        private String location;
        private BrandDto.Response brand;

        public static Response of(Franchise franchise) {
            return Response.builder()
                    .id(franchise.getId())
                    .name(franchise.getName())
                    .location(franchise.getLocation())
                    .brand(franchise.getBrand() != null ?
                            BrandDto.Response.builder()
                                    .id(franchise.getBrand().getId())
                                    .name(franchise.getBrand().getName())
                                    .build() : null)
                    .build();
        }

        public static List<Response> of(List<Franchise> franchises) {
            return franchises.stream()
                    .map(Response::of)
                    .collect(Collectors.toList());
        }
    }
}