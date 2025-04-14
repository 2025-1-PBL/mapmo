package com.pbl.mapmo.dto;

import com.pbl.mapmo.entity.Schedule;
import com.pbl.mapmo.entity.User;
import lombok.*;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

public class ScheduleDto {

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

        @NotNull(message = "날짜는 필수 입력값입니다")
        private LocalDateTime date;

        private Boolean isShared;

        public Schedule toEntity(User user) {
            return Schedule.builder()
                    .title(title)
                    .content(content)
                    .location(location)
                    .latitude(latitude)
                    .longitude(longitude)
                    .date(date)
                    .isShared(isShared != null ? isShared : false)
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
        private String location;
        private Double latitude;
        private Double longitude;
        private LocalDateTime date;
        private Boolean isShared;
        private UserDto.Response user;
        private SharedScheduleDto.Response sharedSchedule;

        public static Response of(Schedule schedule) {
            return Response.builder()
                    .id(schedule.getId())
                    .title(schedule.getTitle())
                    .content(schedule.getContent())
                    .location(schedule.getLocation())
                    .latitude(schedule.getLatitude())
                    .longitude(schedule.getLongitude())
                    .date(schedule.getDate())
                    .isShared(schedule.getIsShared())
                    .user(UserDto.Response.of(schedule.getUser()))
                    .sharedSchedule(schedule.getSharedSchedule() != null ?
                            SharedScheduleDto.Response.of(schedule.getSharedSchedule()) : null)
                    .build();
        }

        public static List<Response> of(List<Schedule> schedules) {
            return schedules.stream()
                    .map(Response::of)
                    .collect(Collectors.toList());
        }
    }
}
