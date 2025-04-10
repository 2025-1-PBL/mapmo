package com.pbl.mapmo.dto;

import com.pbl.mapmo.entity.SharedSchedule;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.util.List;
import java.util.stream.Collectors;

public class SharedScheduleDTO {

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Request {
        @NotNull(message = "일정 ID는 필수입니다.")
        private Integer scheduleId;

        @NotNull(message = "공유 대상 사용자 ID 목록은 필수입니다.")
        @Size(min = 1, message = "최소 한 명 이상에게 공유되어야 합니다.")
        private List<Integer> memberIds;
    }

    @Getter
    @Builder
    public static class Response {
        private Integer id;
        private ScheduleDTO.Response schedule;
        private UserDTO.Response userMaster;
        private List<SharedScheduleMemberDTO.Response> sharedMembers;

        public static Response of(SharedSchedule sharedSchedule) {
            return Response.builder()
                    .id(sharedSchedule.getId())
                    .schedule(ScheduleDTO.Response.of(sharedSchedule.getSchedule()))
                    .userMaster(UserDTO.Response.of(sharedSchedule.getUserMaster()))
                    .sharedMembers(sharedSchedule.getSharedMembers() != null ?
                            SharedScheduleMemberDTO.Response.of(sharedSchedule.getSharedMembers()) : null)
                    .build();
        }

        public static List<Response> of(List<SharedSchedule> sharedSchedules) {
            return sharedSchedules.stream()
                    .map(Response::of)
                    .collect(Collectors.toList());
        }
    }
}