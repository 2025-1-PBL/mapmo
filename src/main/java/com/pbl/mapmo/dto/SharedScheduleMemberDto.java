package com.pbl.mapmo.dto;

import com.pbl.mapmo.entity.SharedScheduleMember;
import lombok.*;

import java.util.List;
import java.util.stream.Collectors;

public class SharedScheduleMemberDto {

    @Getter
    @Builder
    public static class Response {
        private UserDto.Response userMember;

        public static Response of(SharedScheduleMember member) {
            return Response.builder()
                    .userMember(UserDto.Response.of(member.getUserMember()))
                    .build();
        }

        public static List<Response> of(List<SharedScheduleMember> members) {
            return members.stream()
                    .map(Response::of)
                    .collect(Collectors.toList());
        }
    }
}